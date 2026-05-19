package com.pms.module.auth.controller;

import com.pms.common.result.R;
import com.pms.module.auth.dto.LoginRequest;
import com.pms.module.auth.dto.LoginResponse;
import com.pms.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "认证授权")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public R<LoginResponse.UserInfo> userInfo(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long userId = com.pms.common.utils.JwtUtils.getUserId(token);
        return R.ok(authService.getUserInfo(userId));
    }
}
