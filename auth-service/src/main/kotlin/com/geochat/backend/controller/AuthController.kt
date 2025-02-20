package com.geochat.backend.controller

import com.geochat.backend.dto.AuthResponse
import com.geochat.backend.dto.RegisterRequestDto
import com.geochat.backend.service.AuthService
import com.geochat.backend.service.EmailService
import com.geochat.backend.service.UserService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val emailService: EmailService,
    private val redisTemplate: StringRedisTemplate
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

    //TODO: ПЕРЕИСПОЛЬЗОВАТЬ БЛЯДСКИЕ МЕТОДЫ ИЗ СЕРВИСА PASSWORDSERVICE
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestParam email: String) {
        val code = Random.nextInt(100000, 999999).toString()
        redisTemplate.opsForValue().set("password-reset:$email", code, 30, TimeUnit.MINUTES)
        emailService.sendVerificationCode(email, code)
    }

    @PostMapping("/verify-code")
    fun verifyCode(@RequestParam email: String, @RequestParam code: String): Boolean {
        val storedCode = redisTemplate.opsForValue().get("password-reset:$email")
        return storedCode == code
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestParam email: String, @RequestParam newPassword: String): Boolean {
        if(redisTemplate.opsForValue().get("password-reset:$email").isNullOrEmpty())  return false

        userService.updatePassword(email, newPassword)
        redisTemplate.delete("password-reset:$email")
        return true
    }
}

