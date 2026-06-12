package com.company.dms.module.auth.controller;

import com.company.dms.common.result.R;
import com.company.dms.common.security.SecurityUtil;
import com.company.dms.module.auth.dto.LoginRequest;
import com.company.dms.module.auth.service.AuthService;
import com.company.dms.module.auth.vo.CurrentUserVO;
import com.company.dms.module.auth.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @Operation(summary = "当前登录用户")
    @GetMapping("/me")
    public R<CurrentUserVO> me() {
        return R.ok(authService.currentUser(SecurityUtil.currentUserId()));
    }
}
