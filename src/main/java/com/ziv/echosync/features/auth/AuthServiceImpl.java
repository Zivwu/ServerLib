package com.ziv.echosync.features.auth;

import com.ziv.echosync.features.auth.dto.AuthLoginDTO;
import com.ziv.echosync.features.auth.vo.AuthTokenVO;
import com.ziv.echosync.utils.JwtUtils;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthTokenVO login(AuthLoginDTO dto) {
        String token = JwtUtils.generateToken(dto.getDeviceName());
        AuthTokenVO vo = new AuthTokenVO();
        vo.setToken(token);
        vo.setDeviceName(dto.getDeviceName());
        return vo;
    }
}
