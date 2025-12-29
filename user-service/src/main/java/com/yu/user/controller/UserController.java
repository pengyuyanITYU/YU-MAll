package com.yu.user.controller;

import cn.hutool.core.bean.BeanUtil;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import com.yu.user.domain.po.User;
import com.yu.user.domain.query.CountUserQuery;
import com.yu.user.domain.query.UserPageQuery;
import com.yu.user.domain.vo.UserVO;
import com.yu.user.enums.UserStatus;
import com.yu.user.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Slf4j
@Api("用户模块")
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

//    @ApiOperation("更改用户信息")
//    @PutMapping
//    public AjaxResult<Void> updateUserInfo(@RequestBody @Valid UserBasicInfoDTO userBasicInfoDTO){
//        Long userId = AdministratorContext.getUser();
//        log.info("开始更改用户信息{}",userBasicInfoDTO);
//        return AjaxResult.toAjax(userService.updateUserInfo(userId, userBasicInfoDTO));
//    }
//
//    @ApiOperation("修改密码")
//    @PutMapping("/password")
//    public AjaxResult updatePassword(@RequestBody @Valid PasswordDTO passwordDTO){
//        Long userId = AdministratorContext.getUser();
//        return AjaxResult.toAjax(userService.updatePassword(passwordDTO));
//    }

    @GetMapping
    @ApiOperation("查询用户列表")
    public TableDataInfo list( UserPageQuery userPageQuery){
        log.info("开始查询用户列表");
        return userService.listUsers(userPageQuery);
    }

    @GetMapping("/{id}")
    @Cacheable(cacheNames = "user", key = "'id:' + #id")
    @ApiOperation("查询用户信息")
    public AjaxResult<User> getUserInfo(@PathVariable Long id){
        log.info("查询用户{}信息",id);
        return AjaxResult.success(userService.getById(id));
    }

    @GetMapping("/all")
    @ApiOperation("批量查询用户信息")
    @Cacheable(cacheNames = "user", key = "#ids + ':'")
    public AjaxResult<List<UserVO>> getUserInfoByIds(@RequestParam List<Long> ids){
        log.info("批量查询用户信息{}",ids);
        List<User> users = userService.listByIds(ids);
        List<UserVO> userVOS = BeanUtil.copyToList(users, UserVO.class);
        return AjaxResult.success(userVOS);
    }


    @PutMapping("/{id}/{status}")
    @ApiOperation("修改用户状态")
    @CacheEvict(cacheNames = "user", allEntries = true)
    public AjaxResult<Void> updateUserStatus(@PathVariable Long id,@PathVariable Integer status){
        log.info("修改用户状态{}",status);
        User byId = userService.getById(id);
        if (byId == null){
            return AjaxResult.error("用户不存在");
        }
        byId.setStatus(UserStatus.of(status));
        return AjaxResult.toAjax(userService.updateById(byId));
    }

    @ApiOperation("导出Excel表")
    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        log.info("开始进行订单导出");
        List<User> list1 = userService.list();
        ExcelUtils.exportEasyExcel(response, list1, User.class, "用户列表");
    }

}
