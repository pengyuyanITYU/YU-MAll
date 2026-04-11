package com.yu.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.admin.domain.dto.LoginFormDTO;
import com.yu.admin.domain.dto.RegisterFormDTO;
import com.yu.admin.domain.po.Administrator;
import com.yu.admin.domain.query.AdministratorPageQuery;
import com.yu.admin.domain.vo.AdministratorVO;
import com.yu.common.domain.page.TableDataInfo;

public interface IAdminService extends IService<Administrator> {
    AdministratorVO login(LoginFormDTO loginFormDTO);

    AdministratorVO register(RegisterFormDTO loginFormDTO);



//    boolean updateUserInfo(Long userId,  UserBasicInfoDTO userBasicInfoDTO);

//    boolean updatePassword(@Valid PasswordDTO passwordDTO);

    AdministratorVO getAdministratorInfo();

    TableDataInfo listAdmins(AdministratorPageQuery  administratorPageQuery);
}
