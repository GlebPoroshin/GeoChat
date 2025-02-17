package com.geochat.backend.controller

import com.geochat.backend.dto.AuthResponse
import com.geochat.backend.dto.RegisterRequestDto
import com.geochat.backend.service.AuthService
import com.geochat.backend.service.PasswordResetService
import com.geochat.backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val passwordResetService: PasswordResetService,
    private val userService: UserService
) {
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequestDto): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(userService.register(
            nickname = request.nickname,
            email = request.email,
            password = request.password
        ))
    }

    @PostMapping("/login")
    fun login(
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.authenticate(email, password))
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestParam email: String,
        @RequestParam refreshToken: String
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.refreshToken(email, refreshToken))
    }

    @PostMapping("/logout")
    fun logout(@RequestParam email: String): ResponseEntity<String> {
        authService.logout(email)
        return ResponseEntity.ok("Logged out successfully")
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestParam email: String): ResponseEntity<String> {
        passwordResetService.generateResetCode(email)
        return ResponseEntity.ok("Код восстановления отправлен на email")
    }

    @PostMapping("/verify-code")
    fun verifyCode(@RequestParam email: String, @RequestParam code: String): ResponseEntity<Boolean> {
        return ResponseEntity.ok(passwordResetService.verifyResetCode(email, code))
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestParam email: String,
        @RequestParam code: String,
        @RequestParam newPassword: String
    ): ResponseEntity<String> {
        passwordResetService.resetPassword(email, code, newPassword)
        return ResponseEntity.ok("Пароль успешно изменён")
    }
}
