package com.yu.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.MemberClient;
import com.yu.api.client.OrderClient;
import com.yu.api.client.PayClient;
import com.yu.api.enums.OrderStatus;
import com.yu.api.po.Order;
import com.yu.api.po.PayOrder;
import com.yu.api.vo.MemberVO;
import com.yu.common.constant.HttpStatus;
import com.yu.common.domain.AjaxResult;
import com.yu.common.exception.BusinessException;
import com.yu.common.exception.CommonException;
import com.yu.common.utils.CollUtils;
import com.yu.common.utils.UserContext;
import com.yu.user.config.JwtProperties;
import com.yu.user.domain.dto.DeductLocalMoneyDTO;
import com.yu.user.domain.dto.LoginFormDTO;
import com.yu.user.domain.dto.PasswordDTO;
import com.yu.user.domain.dto.RegisterFormDTO;
import com.yu.user.domain.dto.UserBasicInfoDTO;
import com.yu.user.domain.po.User;
import com.yu.user.domain.vo.UserLoginVO;
import com.yu.user.domain.vo.UserRegisterVO;
import com.yu.user.domain.vo.UserVO;
import com.yu.user.enums.MemberLevel;
import com.yu.user.enums.UserStatus;
import com.yu.user.mapper.UserMapper;
import com.yu.user.service.IUserService;
import com.yu.user.support.AuthDistributedLock;
import com.yu.user.support.AuthRiskPolicy;
import com.yu.user.utils.JwtTool;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;
    private final FileStorageService fileStorageService;
    private final MemberClient memberClient;
    private final PayClient payClient;
    private final OrderClient orderClient;
    private final AuthRiskPolicy authRiskPolicy;
    private final AuthDistributedLock authDistributedLock;

    @Override
    public UserLoginVO login(LoginFormDTO loginFormDTO) {
        String username = StrUtil.trim(loginFormDTO.getUsername());
        String rawPassword = loginFormDTO.getPassword();
        String hashedPassword = DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));

        User user = lambdaQuery().eq(User::getUsername, username).one();
        if (user == null || !BCrypt.checkpw(hashedPassword, user.getPassword())) {
            authRiskPolicy.recordLoginFailure(username);
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != UserStatus.NORMAL) {
            throw new BusinessException("用户已被禁用");
        }

        authRiskPolicy.clearLoginFailures(username);
        syncMemberLevel(user);

        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO
                .setToken(jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL()))
                .setAvatar(user.getAvatar())
                .setNickName(user.getNickName())
                .setUserId(user.getId())
                .setBalance(user.getBalance())
                .setCurrentPoints(user.getCurrentPoints())
                .setLevelName(user.getLevelName());
        return userLoginVO;
    }

    @Override
    @Transactional
    public UserRegisterVO register(RegisterFormDTO registerFormDTO) {
        String username = StrUtil.trim(registerFormDTO.getUsername());
        String phone = StrUtil.trim(registerFormDTO.getPhone());
        String rawPassword = registerFormDTO.getPassword();
        String encodedPassword = BCrypt.hashpw(
                DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8)),
                BCrypt.gensalt()
        );

        if (lambdaQuery().eq(User::getUsername, username).count() > 0) {
            throw new CommonException("用户名已存在", HttpStatus.CONFLICT);
        }

        AuthDistributedLock.LockToken lockToken =
                authDistributedLock.tryLockRegister(username, phone, Duration.ofSeconds(10));
        if (lockToken == null) {
            throw new BusinessException("注册请求处理中，请稍后再试");
        }

        try {
            if (lambdaQuery().eq(User::getUsername, username).count() > 0) {
                throw new CommonException("用户名已存在", HttpStatus.CONFLICT);
            }

            User user = new User()
                    .setUsername(username)
                    .setPassword(encodedPassword)
                    .setPhone(phone)
                    .setStatus(UserStatus.NORMAL)
                    .setLevelName("普通会员")
                    .setAvatar(registerFormDTO.getAvatar())
                    .setNickName(registerFormDTO.getNickName())
                    .setBalance(0L)
                    .setMemberLevelId(MemberLevel.GENERAL.getId())
                    .setCurrentPoints(0)
                    .setPayPassword(encodedPassword);

            save(user);
            User savedUser = lambdaQuery().eq(User::getUsername, username).one();
            if (savedUser == null) {
                throw new BusinessException("注册失败，请稍后重试");
            }

            UserRegisterVO userRegisterVO = new UserRegisterVO();
            userRegisterVO.setUserId(savedUser.getId());
            userRegisterVO.setBalance(savedUser.getBalance());
            userRegisterVO.setUsername(savedUser.getUsername());
            userRegisterVO.setAvatar(savedUser.getAvatar());
            userRegisterVO.setNickName(savedUser.getNickName());
            userRegisterVO.setLevelName(savedUser.getLevelName());
            userRegisterVO.setToken(jwtTool.createToken(savedUser.getId(), jwtProperties.getTokenTTL()));
            return userRegisterVO;
        } finally {
            authDistributedLock.unlock(lockToken);
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, name = "yu-mall-user-center-service")
    public void deductMoney(DeductLocalMoneyDTO deductLocalMoneyDTO) {
        Long userId = deductLocalMoneyDTO.getUserId();
        User user = getById(userId);
        if (user == null) {
            log.error("用户不存在");
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == UserStatus.FROZEN) {
            log.error("用户被冻结");
            throw new RuntimeException("用户被冻结");
        }
        String password = DigestUtils.md5DigestAsHex(
                deductLocalMoneyDTO.getPayPassword().getBytes(StandardCharsets.UTF_8)
        );
        if (!BCrypt.checkpw(password, user.getPayPassword())) {
            log.error("支付密码错误");
            throw new RuntimeException("支付密码错误");
        }
        AjaxResult<MemberVO> memberVOData = memberClient.getMemberById(user.getMemberLevelId());
        if (!memberVOData.isSuccess() || memberVOData.getData() == null) {
            log.error("会员服务异常");
            throw new RuntimeException("会员服务异常");
        }
        if (deductLocalMoneyDTO.getOrderBizId() == null) {
            log.error("订单用户扣减余额服务 id 为空");
            throw new RuntimeException("订单用户扣减余额服务 id 为空");
        }
        AjaxResult<Order> byId = orderClient.getById(deductLocalMoneyDTO.getOrderBizId());
        Order data = byId.getData();
        OrderStatus orderStatus = data == null ? null : OrderStatus.of(data.getStatus());
        if (data == null || !byId.isSuccess() || orderStatus != OrderStatus.UNPAID) {
            log.warn("提前校验拦截：订单{}的状态为{}，跳过处理",
                    deductLocalMoneyDTO.getOrderBizId(),
                    orderStatus == null ? "未知" : orderStatus);
            return;
        }
        AjaxResult<PayOrder> payOrderByOrderId = payClient.getPayOrderByOrderId(deductLocalMoneyDTO.getOrderBizId());
        if (payOrderByOrderId.getData() != null
                && payOrderByOrderId.isSuccess()
                && OrderStatus.UNPAID == OrderStatus.of(payOrderByOrderId.getData().getStatus())) {
            log.warn("该订单流水已经存在");
            throw new BusinessException("该订单流水已经存在，请勿重复提交");
        }
        MemberVO memberVO = memberVOData.getData();
        BigDecimal amountBd = new BigDecimal(deductLocalMoneyDTO.getAmount());
        BigDecimal discountedBd = amountBd.multiply(memberVO.getDiscountRate());
        Long amount = discountedBd.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        if (user.getBalance() < amount) {
            log.error("余额不足");
            throw new BusinessException("余额不足");
        }
        boolean update = lambdaUpdate().eq(User::getId, userId)
                .setSql(amount != null, "balance = balance -" + amount)
                .setSql(amount != null, "current_points = current_points +" + amount)
                .update();
        if (!update) {
            log.error("更新用户余额失败");
            throw new RuntimeException("更新用户余额失败");
        }
    }

    @Override
    public boolean updateUserInfo(Long userId, UserBasicInfoDTO userBasicInfoDTO) {
        Long user = UserContext.getUser();
        if (user == null) {
            log.error("用户未登录");
            throw new RuntimeException("用户未登录，请检查登录状态");
        }
        return lambdaUpdate()
                .eq(User::getId, user)
                .set(StrUtil.isNotBlank(userBasicInfoDTO.getNickName()), User::getNickName, userBasicInfoDTO.getNickName())
                .set(StrUtil.isNotBlank(userBasicInfoDTO.getAvatar()), User::getAvatar, userBasicInfoDTO.getAvatar())
                .set(StrUtil.isNotBlank(userBasicInfoDTO.getPhone()), User::getPhone, userBasicInfoDTO.getPhone())
                .set(StrUtil.isNotBlank(userBasicInfoDTO.getEmail()), User::getEmail, userBasicInfoDTO.getEmail())
                .set(userBasicInfoDTO.getGender() != null, User::getGender, userBasicInfoDTO.getGender())
                .set(userBasicInfoDTO.getBirthday() != null, User::getBirthday, userBasicInfoDTO.getBirthday())
                .update();
    }

    @Override
    public boolean updatePassword(PasswordDTO passwordDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            log.error("用户未登录");
            throw new RuntimeException("用户未登录，请检查登录状态");
        }
        User user = lambdaQuery().eq(User::getId, userId).one();
        if (user != null && user.getPhone() != null && !user.getPhone().equals(passwordDTO.getPhone())) {
            log.warn("手机号不一致");
            throw new RuntimeException("手机号不一致");
        }
        String oldPassword = DigestUtils.md5DigestAsHex(passwordDTO.getOldPassword().getBytes(StandardCharsets.UTF_8));
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            log.warn("旧密码错误");
            throw new RuntimeException("旧密码错误");
        }
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            log.warn("密码不一致");
            throw new RuntimeException("密码不一致");
        }
        String newPassword = DigestUtils.md5DigestAsHex(passwordDTO.getConfirmPassword().getBytes(StandardCharsets.UTF_8));
        String encodedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        return lambdaUpdate().eq(User::getId, userId)
                .set(User::getPassword, encodedPassword)
                .set(User::getPayPassword, encodedPassword)
                .update();
    }

    @Override
    public UserVO getUserInfo() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            log.error("用户未登录");
            throw new RuntimeException("用户未登录，请检查登录状态");
        }
        User user = lambdaQuery().eq(User::getId, userId).one();
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO, CopyOptions.create().setIgnoreCase(true).setIgnoreProperties("id"));
        userVO.setUserId(user.getId());
        userVO.setPhone(user.getPhone().substring(0, 3) + "****");
        return userVO;
    }

    private void syncMemberLevel(User user) {
        AjaxResult<List<MemberVO>> memberVOList = memberClient.listByMember();
        List<MemberVO> data = memberVOList.getData();
        if (CollUtils.isEmpty(data)) {
            log.error("会员服务数据异常，无法查询到所有会员");
            throw new RuntimeException("会员服务数据异常，无法查询到所有会员");
        }
        for (MemberVO datum : data) {
            if (datum.getMinPoints() <= user.getCurrentPoints() && datum.getMaxPoints() >= user.getCurrentPoints()) {
                lambdaUpdate().eq(User::getId, user.getId())
                        .set(StrUtil.isNotBlank(datum.getLevelName()), User::getLevelName, datum.getLevelName())
                        .set(datum.getId() != null, User::getMemberLevelId, datum.getId())
                        .update();
                user.setLevelName(datum.getLevelName());
                user.setMemberLevelId(datum.getId());
                return;
            }
        }
    }
}
