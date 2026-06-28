package com.matvey.objectaccountingservice.controller;

import com.matvey.objectaccountingservice.dto.request.RegisterRequestDto;
import com.matvey.objectaccountingservice.dto.response.AuthResponseDto;
import com.matvey.objectaccountingservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
