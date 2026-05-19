package com.pms.module.auth.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pms.common.exception.BusinessException;
import com.pms.common.result.ResultCode;
import com.pms.common.utils.JwtUtils;
import com.pms.module.auth.dto.LoginRequest;
import com.pms.module.auth.dto.LoginResponse;
import com.pms.module.auth.entity.SysUser;
import com.pms.module.auth.mapper.SysUserMapper;
import com.pms.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        String token = JwtUtils.generate(user.getId(), user.getUsername(), new HashMap<>());
        LoginResponse.UserInfo info = new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getRealName(),
                user.getEmail(), user.getDepartment(), user.getAvatar()
        );
        return new LoginResponse(token, info);
    }

    @Override
    public LoginResponse.UserInfo getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.NOT_FOUND);
        return new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getRealName(),
                user.getEmail(), user.getDepartment(), user.getAvatar()
        );
    }
}
