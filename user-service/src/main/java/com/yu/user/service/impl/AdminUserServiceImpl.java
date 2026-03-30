package com.yu.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.vo.UserVO;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.CollUtils;
import com.yu.user.domain.po.User;
import com.yu.user.domain.query.UserPageQuery;
import com.yu.user.mapper.UserMapper;
import com.yu.user.service.IAdminUserService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AdminUserServiceImpl extends ServiceImpl<UserMapper, User> implements IAdminUserService {
    @Override
    public TableDataInfo listUsers(UserPageQuery userPageQuery) {
        Page<User> page = new Page<>(userPageQuery.getPageNo(), userPageQuery.getPageSize());
        Page<User> result = lambdaQuery()
                .eq(userPageQuery.getStatus() != null, User::getStatus, userPageQuery.getStatus())
                .like(StrUtil.isNotBlank(userPageQuery.getLevelName()), User::getLevelName, userPageQuery.getLevelName())
                .page(page);
        return TableDataInfo.success(result.getRecords(), result.getTotal());
    }

    @Override
    public List<UserVO> getUserInfoByIds(List<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<User> users = listByIds(ids);
        return BeanUtil.copyToList(users, UserVO.class);
    }
}
