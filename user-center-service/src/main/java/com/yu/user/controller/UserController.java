package com.yu.user.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yu.common.domain.AjaxResult;
import com.yu.common.exception.RateLimitException;
import com.yu.common.utils.UserContext;
import com.yu.common.utils.WebUtils;
import com.yu.user.domain.dto.DeductLocalMoneyDTO;
import com.yu.user.domain.dto.LoginFormDTO;
import com.yu.user.domain.dto.PasswordDTO;
import com.yu.user.domain.dto.RegisterFormDTO;
import com.yu.user.domain.dto.UserBasicInfoDTO;
import com.yu.user.domain.vo.UserLoginVO;
import com.yu.user.domain.vo.UserRegisterVO;
import com.yu.user.domain.vo.UserVO;
import com.yu.user.service.IUserService;
import com.yu.user.support.AuthRiskPolicy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@RestController
@Slf4j
@Api("用户模块")
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final AuthRiskPolicy authRiskPolicy;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public AjaxResult<UserLoginVO> login(@RequestBody @Valid LoginFormDTO loginFormDTO) {
        return withSentinel("user-login", () -> {
            authRiskPolicy.guardLogin(
                    loginFormDTO.getUsername(),
                    WebUtils.getClientIp(),
                    loginFormDTO.getCaptchaTicket()
            );
            return AjaxResult.success(userService.login(loginFormDTO));
        });
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public AjaxResult<UserRegisterVO> register(@RequestBody @Valid RegisterFormDTO registerFormDTO) {
        return withSentinel("user-register", () -> {
            authRiskPolicy.guardRegister(
                    registerFormDTO.getUsername(),
                    registerFormDTO.getPhone(),
                    WebUtils.getClientIp(),
                    registerFormDTO.getCaptchaTicket()
            );
            return AjaxResult.success(userService.register(registerFormDTO));
        });
    }

    @ApiOperation("修改用户信息")
    @PutMapping
    public AjaxResult<Void> updateUserInfo(@RequestBody @Valid UserBasicInfoDTO userBasicInfoDTO) {
        Long userId = UserContext.getUser();
        return AjaxResult.toAjax(userService.updateUserInfo(userId, userBasicInfoDTO));
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public AjaxResult<Void> updatePassword(@RequestBody @Valid PasswordDTO passwordDTO) {
        return AjaxResult.toAjax(userService.updatePassword(passwordDTO));
    }

    @PutMapping("/money/deduct")
    @ApiOperation("扣减用户余额")
    public AjaxResult<Void> deductMoney(@RequestBody DeductLocalMoneyDTO deductLocalMoneyDTO) {
        userService.deductMoney(deductLocalMoneyDTO);
        return AjaxResult.success();
    }

    @GetMapping
    @ApiOperation("查询用户信息")
    public AjaxResult<UserVO> getUserInfo() {
        return AjaxResult.success(userService.getUserInfo());
    }

    private <T> AjaxResult<T> withSentinel(String resource, Supplier<AjaxResult<T>> supplier) {
        Entry entry = null;
        try {
            entry = SphU.entry(resource);
            return supplier.get();
        } catch (BlockException e) {
            throw new RateLimitException("操作过于频繁，请稍后再试", e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
