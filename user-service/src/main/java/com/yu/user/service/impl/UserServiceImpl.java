package com.yu.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.MemberClient;
import com.yu.api.vo.MemberVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.BusinessException;
import com.yu.common.utils.CollUtils;
import com.yu.common.utils.AdministratorContext;
import com.yu.user.config.JwtProperties;
import com.yu.user.domain.dto.LoginFormDTO;
import com.yu.user.domain.dto.PasswordDTO;
import com.yu.user.domain.dto.RegisterFormDTO;
import com.yu.user.domain.dto.UserBasicInfoDTO;
import com.yu.user.domain.po.User;
import com.yu.user.domain.query.UserPageQuery;
import com.yu.user.domain.vo.UserLoginVO;
import com.yu.user.domain.vo.UserRegisterVO;
import com.yu.user.domain.vo.UserVO;
import com.yu.user.enums.MemberLevel;
import com.yu.user.enums.UserStatus;
import com.yu.user.mapper.UserMapper;
import com.yu.user.service.IUserService;
import com.yu.user.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final JwtTool jwtTool;

    private final JwtProperties jwtProperties;

    private final FileStorageService fileStorageService;

    private final MemberClient memberClient;

    @Override
    public TableDataInfo listUsers(UserPageQuery userPageQuery) {
        Page<User> p = new Page<>(userPageQuery.getPageNo(), userPageQuery.getPageSize());
        Page<User> page = lambdaQuery().eq(userPageQuery.getStatus() != null, User::getStatus, userPageQuery.getStatus())
                .like(StrUtil.isNotBlank(userPageQuery.getLevelName()), User::getLevelName, userPageQuery.getLevelName())
                .page(p);
        if(CollUtils.isEmpty(page.getRecords())){
            return TableDataInfo.error("没有数据");
        }
        TableDataInfo success = TableDataInfo.success(page.getRecords(), page.getTotal());
        return success;

    }

    //    @Override
//    public boolean updateUserInfo(Long userId, UserBasicInfoDTO userBasicInfoDTO) {
//        Long user = AdministratorContext.getUser();
//        if(user == null){
//            log.error("用户未登录");
//            throw new RuntimeException("用户未登录,请检查登录状态");
//        }
//        boolean update = lambdaUpdate()
//                .eq(User::getId, user)
//                .set(StrUtil.isNotBlank(userBasicInfoDTO.getNickName()), User::getNickName, userBasicInfoDTO.getNickName())
//                .set(StrUtil.isNotBlank(userBasicInfoDTO.getAvatar()), User::getAvatar, userBasicInfoDTO.getAvatar())
//                .set(StrUtil.isNotBlank(userBasicInfoDTO.getPhone()), User::getPhone, userBasicInfoDTO.getPhone())
//                .set(StrUtil.isNotBlank(userBasicInfoDTO.getEmail()), User::getEmail, userBasicInfoDTO.getEmail())
//                .set(userBasicInfoDTO.getGender() != null, User::getGender, userBasicInfoDTO.getGender())
//                .set(userBasicInfoDTO.getBirthday() != null, User::getBirthday, userBasicInfoDTO.getBirthday())
//                .update();
//        return update;
//    }


//    @Override
//    public boolean updatePassword(PasswordDTO passwordDTO) {
//        Long userId = AdministratorContext.getUser();
//        if(userId == null){
//            log.error("用户未登录");
//            throw new RuntimeException("用户未登录,请检查登录状态");
//        }
//        User one = lambdaQuery().eq(User::getId, userId).one();
//        if(one != null && one.getPhone() != null){
//            if(!one.getPhone().equals(passwordDTO.getPhone())){
//                log.warn("手机号不一致");
//                throw new RuntimeException("手机号不一致");
//            }
//        }
//        String pw = DigestUtils.md5DigestAsHex(passwordDTO.getOldPassword().getBytes());
//        if(!pw.equals(one.getPassword())){
//            log.warn("旧密码错误");
//            throw new RuntimeException("旧密码错误");
//        }
//        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
//            log.warn("密码不一致");
//            throw new RuntimeException("密码不一致");
//        }
//        String newPw = DigestUtils.md5DigestAsHex(passwordDTO.getConfirmPassword().getBytes());
//        boolean update = lambdaUpdate().eq(User::getId, userId)
//                .set(User::getPassword, newPw)
//                .update();
//        return update;
//    }

}
