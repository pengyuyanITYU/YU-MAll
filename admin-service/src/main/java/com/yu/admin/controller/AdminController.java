package com.yu.admin.controller;


import com.yu.admin.domain.dto.LoginFormDTO;
import com.yu.admin.domain.dto.RegisterFormDTO;
import com.yu.admin.domain.po.Administrator;
import com.yu.admin.domain.query.AdministratorPageQuery;
import com.yu.admin.domain.vo.AdministratorVO;
import com.yu.admin.enums.AdministratorStatus;
import com.yu.admin.service.IAdminService;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {

    private final IAdminService administratorService;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public AjaxResult<AdministratorVO> login(@RequestBody  LoginFormDTO loginFormDTO){
        log.info("用户登录：{}", loginFormDTO);
        return AjaxResult.success(administratorService.login(loginFormDTO));
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public AjaxResult<AdministratorVO> register(@RequestBody RegisterFormDTO registerFormDTO){
        log.info("用户注册：{}", registerFormDTO);
        return AjaxResult.success(administratorService.register(registerFormDTO));
    }

    @GetMapping
    @ApiOperation("获取所有管理员信息")
    public TableDataInfo listAdmins(AdministratorPageQuery administratorPageQuery){
       log.info("获取所有管理员信息");
       return administratorService.listAdmins(administratorPageQuery);
    }

    @PutMapping("/{id}/{status}")
    @ApiOperation("修改用户状态")
    @CacheEvict(value = "administrator",allEntries = true)
    public AjaxResult<Void> updateUserStatus(@PathVariable Long id,@PathVariable Integer status){
        log.info("修改用户状态{}",status);
        Administrator byId = administratorService.getById(id);
        if (byId == null){
            return AjaxResult.error("用户不存在");
        }
        byId.setStatus(AdministratorStatus.of(status));
        return AjaxResult.toAjax(administratorService.updateById(byId));
    }

    @ApiOperation("导出Excel表")
    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        log.info("开始进行订单导出");
        List<Administrator> list1 = administratorService.list();
        ExcelUtils.exportEasyExcel(response, list1, Administrator.class, "管理员列表");
    }


}
