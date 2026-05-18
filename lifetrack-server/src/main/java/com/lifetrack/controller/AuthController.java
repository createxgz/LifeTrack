package com.lifetrack.controller;

import com.lifetrack.common.Result;
import com.lifetrack.dto.*;
import com.lifetrack.service.AuthService;
import com.lifetrack.vo.LoginVO;
import com.lifetrack.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户认证")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册")
    @PostMapping("/auth/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/auth/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @Operation(summary = "获取个人信息")
    @GetMapping("/user/profile")
    public Result<UserProfileVO> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(authService.getProfile(userId));
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/user/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO dto,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.updateProfile(userId, dto);
        return Result.success();
    }

    @Operation(summary = "忘记密码 — 发送重置邮件")
    @PostMapping("/auth/forgot-password")
    public Result<Void> forgotPassword(@RequestBody @Valid ForgotPasswordDTO dto) {
        authService.forgotPassword(dto);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/auth/reset-password")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/user/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.changePassword(userId, dto);
        return Result.success();
    }
}
