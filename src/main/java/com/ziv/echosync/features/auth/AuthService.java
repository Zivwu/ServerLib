package com.ziv.echosync.features.auth;

import com.ziv.echosync.features.auth.dto.AuthLoginDTO;
import com.ziv.echosync.features.auth.vo.AuthTokenVO;

public interface AuthService {

    AuthTokenVO login(AuthLoginDTO dto);
}
