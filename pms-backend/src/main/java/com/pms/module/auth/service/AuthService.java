package com.pms.module.auth.service;

import com.pms.module.auth.dto.LoginRequest;
import com.pms.module.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse.UserInfo getUserInfo(Long userId);
}
