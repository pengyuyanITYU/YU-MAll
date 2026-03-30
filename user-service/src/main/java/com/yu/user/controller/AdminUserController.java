package com.yu.user.controller;

import com.yu.api.vo.UserVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.UnauthorizedException;
import com.yu.common.utils.AdministratorContext;
import com.yu.common.utils.ExcelUtils;
import com.yu.user.domain.po.User;
import com.yu.user.domain.query.UserPageQuery;
import com.yu.user.enums.UserStatus;
import com.yu.user.service.IAdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Slf4j
@Api("admin user module")
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IAdminUserService adminUserService;

    @ModelAttribute
    public void checkAdminLogin() {
        if (AdministratorContext.getUser() == null) {
            throw new UnauthorizedException("unauthorized");
        }
    }

    @GetMapping
    @ApiOperation("query user list")
    public TableDataInfo list(UserPageQuery userPageQuery) {
        return adminUserService.listUsers(userPageQuery);
    }

    @GetMapping("/{id}")
    @ApiOperation("query user detail")
    public AjaxResult<User> getUserInfo(@PathVariable Long id) {
        return AjaxResult.success(adminUserService.getById(id));
    }

    @GetMapping("/all")
    @ApiOperation("batch query user detail")
    public AjaxResult<List<UserVO>> getUserInfoByIds(@RequestParam List<Long> ids) {
        return AjaxResult.success(adminUserService.getUserInfoByIds(ids));
    }

    @PutMapping("/{id}/{status}")
    @ApiOperation("update user status")
    public AjaxResult<Void> updateUserStatus(@PathVariable Long id, @PathVariable Integer status) {
        User user = adminUserService.getById(id);
        if (user == null) {
            return AjaxResult.error("user not found");
        }
        user.setStatus(UserStatus.of(status));
        return AjaxResult.toAjax(adminUserService.updateById(user));
    }

    @ApiOperation("export excel")
    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        List<User> users = adminUserService.list();
        ExcelUtils.exportEasyExcel(response, users, User.class, "user list");
    }
}
