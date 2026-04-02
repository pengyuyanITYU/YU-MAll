package com.yu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.api.vo.UserVO;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.user.domain.po.User;
import com.yu.user.domain.query.UserPageQuery;

import java.util.List;

public interface IAdminUserService extends IService<User> {
    TableDataInfo listUsers(UserPageQuery userPageQuery);

    List<UserVO> getUserInfoByIds(List<Long> ids);
}
