package com.yu.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.admin.config.JwtProperties;
import com.yu.admin.domain.dto.LoginFormDTO;
import com.yu.admin.domain.dto.RegisterFormDTO;
import com.yu.admin.domain.po.Administrator;
import com.yu.admin.domain.query.AdministratorPageQuery;
import com.yu.admin.domain.vo.AdministratorVO;
import com.yu.admin.enums.AdministratorStatus;
import com.yu.admin.mapper.AdministratorMapper;
import com.yu.admin.service.IAdminService;
import com.yu.admin.utils.JwtTool;
import com.yu.api.client.MemberClient;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.BusinessException;
import com.yu.common.utils.AdministratorContext;
import com.yu.common.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdministratorMapper, Administrator> implements IAdminService {

    private final JwtTool jwtTool;

    private final JwtProperties jwtProperties;

    private final FileStorageService fileStorageService;

    private final MemberClient memberClient;


    @Override
    public AdministratorVO login(LoginFormDTO loginFormDTO) {
        String Administratorname = loginFormDTO.getUsername();
        String pw = loginFormDTO.getPassword();

        log.info("用户登录：{},dto{}", Administratorname, loginFormDTO);
        Administrator Administrator = lambdaQuery().eq(com.yu.admin.domain.po.Administrator::getUsername, Administratorname).one();
        Assert.notNull(Administrator, "用户不存在");
        log.info("用户登录zhe：{}",BCrypt.checkpw(pw, Administrator.getPassword()));
        Assert.isTrue(BCrypt.checkpw(pw, Administrator.getPassword()), "密码错误");
        if(Objects.equals(Administrator.getStatus(), AdministratorStatus.FROZEN)){
            log.warn("用户{}被禁用",AdministratorContext.getUser());
            throw new BusinessException("用户被禁用");
        }
        String token = jwtTool.createToken(Administrator.getUserId(), jwtProperties.getTokenTTL());
        log.debug("生成token:{}", token);
        AdministratorVO AdminVO = new AdministratorVO();
        AdminVO
                .setToken(token)
                .setAvatar(Administrator.getAvatar())
                .setNickName(Administrator.getNickName())
                .setUserId(Administrator.getUserId());
        return AdminVO;
    }

    @Override
    @Transactional
    public AdministratorVO register(RegisterFormDTO registerFormDTO) {
        String Administratorname = registerFormDTO.getUsername();
        String password = registerFormDTO.getPassword();
        String phone = registerFormDTO.getPhone();
        String nickName = registerFormDTO.getNickName();
        String hashPassword = BCrypt.hashpw( password, BCrypt.gensalt());
        Administrator Administrator  = new Administrator()
                .setUsername(Administratorname)
                .setPassword(hashPassword)
                .setAvatar(registerFormDTO.getAvatar())
                .setNickName(nickName);
        save(Administrator);
        log.info("用户注册成功：{}", Administrator);
        Administrator u = lambdaQuery().eq(com.yu.admin.domain.po.Administrator::getUsername, Administratorname).one();
        AdministratorVO AdministratorRegisterVO = new AdministratorVO();
        AdministratorRegisterVO.setUserId(u.getUserId());
         AdministratorRegisterVO.setUsername(u.getUsername());
         AdministratorRegisterVO.setAvatar(u.getAvatar());
         AdministratorRegisterVO.setNickName(u.getNickName());
        AdministratorRegisterVO.setToken(jwtTool.createToken(u.getUserId(), jwtProperties.getTokenTTL()));
        return AdministratorRegisterVO;
    }


//    @Override
//    public boolean updateAdministratorInfo(Long AdministratorId, AdministratorBasicInfoDTO AdministratorBasicInfoDTO) {
//        Long Administrator = AdministratorContext.getAdministrator();
//        if(Administrator == null){
//            log.error("用户未登录");
//            throw new RuntimeException("用户未登录,请检查登录状态");
//        }
//        boolean update = lambdaUpdate()
//                .eq(Administrator::getId, Administrator)
//                .set(StrUtil.isNotBlank(AdministratorBasicInfoDTO.getNickName()), Administrator::getNickName, AdministratorBasicInfoDTO.getNickName())
//                .set(StrUtil.isNotBlank(AdministratorBasicInfoDTO.getAvatar()), Administrator::getAvatar, AdministratorBasicInfoDTO.getAvatar())
//                .set(StrUtil.isNotBlank(AdministratorBasicInfoDTO.getPhone()), Administrator::getPhone, AdministratorBasicInfoDTO.getPhone())
//                .set(StrUtil.isNotBlank(AdministratorBasicInfoDTO.getEmail()), Administrator::getEmail, AdministratorBasicInfoDTO.getEmail())
//                .set(AdministratorBasicInfoDTO.getGender() != null, Administrator::getGender, AdministratorBasicInfoDTO.getGender())
//                .set(AdministratorBasicInfoDTO.getBirthday() != null, Administrator::getBirthday, AdministratorBasicInfoDTO.getBirthday())
//                .update();
//        return update;
//    }


//    @Override
//    public boolean updatePassword(PasswordDTO passwordDTO) {
//        Long AdministratorId = AdministratorContext.getAdministrator();
//        if(AdministratorId == null){
//            log.error("用户未登录");
//            throw new RuntimeException("用户未登录,请检查登录状态");
//        }
//        Administrator one = lambdaQuery().eq(Administrator::getId, AdministratorId).one();
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
//        boolean update = lambdaUpdate().eq(Administrator::getId, AdministratorId)
//                .set(Administrator::getPassword, newPw)
//                .update();
//        return update;
//    }


    @Override
    public AdministratorVO getAdministratorInfo() {
        Long Administrator = AdministratorContext.getUser();
        if(Administrator == null){
            log.error("用户未登录");
            throw new RuntimeException("用户未登录,请检查登录状态");
        }
        Administrator one = lambdaQuery().eq(com.yu.admin.domain.po.Administrator::getUserId, Administrator).one();
        AdministratorVO AdministratorVO = new AdministratorVO();
        BeanUtil.copyProperties(one, AdministratorVO, CopyOptions.create().setIgnoreCase( true).setIgnoreProperties("id"));
        AdministratorVO.setUserId(one.getUserId());
        return AdministratorVO;

    }

    @Override
    public TableDataInfo listAdmins(AdministratorPageQuery  administratorPageQuery) {
        Page<Administrator> p = new Page<>(administratorPageQuery.getPageNo(), administratorPageQuery.getPageSize());
        lambdaQuery().eq(administratorPageQuery.getStatus() != null,Administrator::getStatus, administratorPageQuery.getStatus())
                .like(StrUtil.isNotBlank(administratorPageQuery.getNickName()), Administrator::getNickName, administratorPageQuery.getNickName())
                .page(p);
        if (CollUtils.isEmpty(p.getRecords())){
            log.warn("用户列表为空");
            return TableDataInfo.error("用户列表为空");
        }
        IPage<AdministratorVO> convert = p.convert(administrator -> {
            AdministratorVO AdministratorVO = new AdministratorVO();
            BeanUtil.copyProperties(administrator, AdministratorVO);
            return AdministratorVO;
        });
        return TableDataInfo.success(convert.getRecords(), convert.getTotal());
    }
}
