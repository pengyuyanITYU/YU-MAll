package com.yu.user.controller;

import com.yu.api.query.admin.AdminUserPageQuery;
import com.yu.api.vo.admin.AdminUserExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.user.domain.po.User;
import com.yu.user.domain.query.UserPageQuery;
import com.yu.user.enums.UserStatus;
import com.yu.user.service.IAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/users")
public class InternalAdminUserController {

    private final IAdminUserService adminUserService;

    @GetMapping
    public TableDataInfo list(AdminUserPageQuery query) {
        UserPageQuery target = new UserPageQuery();
        if (query != null) {
            BeanUtils.copyProperties(query, target);
        }
        return adminUserService.listUsers(target);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return AjaxResult.success(adminUserService.getById(id));
    }

    @GetMapping("/all")
    public AjaxResult<?> getByIds(@RequestParam List<Long> ids) {
        return AjaxResult.success(adminUserService.getUserInfoByIds(ids));
    }

    @PutMapping("/{id}/{status}")
    public AjaxResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        User user = adminUserService.getById(id);
        if (user == null) {
            return AjaxResult.error("user not found");
        }
        user.setStatus(UserStatus.of(status));
        return AjaxResult.toAjax(adminUserService.updateById(user));
    }

    @GetMapping("/export-data")
    public AjaxResult<List<AdminUserExportVO>> exportData() {
        return AjaxResult.success(adminUserService.list().stream().map(this::toExportVO).collect(Collectors.toList()));
    }

    private AdminUserExportVO toExportVO(User user) {
        AdminUserExportVO exportVO = new AdminUserExportVO();
        BeanUtils.copyProperties(user, exportVO);
        if (user.getStatus() != null) {
            exportVO.setStatus(user.getStatus().getValue());
        }
        return exportVO;
    }
}
