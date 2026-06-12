package com.company.dms.module.auth.service;

import com.company.dms.module.auth.dto.LoginRequest;
import com.company.dms.module.auth.vo.CurrentUserVO;
import com.company.dms.module.auth.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginRequest request);
    CurrentUserVO currentUser(Long userId);
}
