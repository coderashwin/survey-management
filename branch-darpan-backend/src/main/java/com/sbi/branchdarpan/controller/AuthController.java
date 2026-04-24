package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.auth.AuthDtos.AuthResponse;
import static com.sbi.branchdarpan.model.dto.auth.AuthDtos.MessageResponse;
import static com.sbi.branchdarpan.model.dto.auth.AuthDtos.SsoLoginRequest;
import static com.sbi.branchdarpan.model.dto.auth.AuthDtos.UserProfile;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.branchdarpan.security.UserPrincipal;
import com.sbi.branchdarpan.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sso/login")
    public AuthResponse login(@Valid @RequestBody SsoLoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/sso/callback")
    public AuthResponse callback(@RequestParam("token") String token) {
        return authService.callback(token);
    }

    @GetMapping("/sso/url")
    public MessageResponse getSsoUrl() {
        return new MessageResponse(authService.getSsoLoginUrl());
    }

    @PostMapping("/validate")
    public UserProfile validate(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.validate(principal);
    }

    @PostMapping("/logout")
    public MessageResponse logout() {
        return authService.logout();
    }
}
