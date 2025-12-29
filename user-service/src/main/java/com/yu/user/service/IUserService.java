package com.yu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.user.domain.dto.LoginFormDTO;
import com.yu.user.domain.dto.PasswordDTO;
import com.yu.user.domain.dto.RegisterFormDTO;
import com.yu.user.domain.dto.UserBasicInfoDTO;
import com.yu.user.domain.po.User;
import com.yu.user.domain.query.UserPageQuery;
import com.yu.user.domain.vo.UserLoginVO;
import com.yu.user.domain.vo.UserRegisterVO;
import com.yu.user.domain.vo.UserVO;

import javax.validation.Valid;

public interface IUserService extends IService<User> {

//
//    boolean updateUserInfo(Long userId,  UserBasicInfoDTO userBasicInfoDTO);
//
//    boolean updatePassword(@Valid PasswordDTO passwordDTO);
//

    TableDataInfo listUsers(UserPageQuery userPageQuery);
}
