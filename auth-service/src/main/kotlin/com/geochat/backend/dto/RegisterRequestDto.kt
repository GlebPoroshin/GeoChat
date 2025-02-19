package com.geochat.backend.dto

data class RegisterRequestDto(
    val nickname: String,
    val email: String,
    val password: String
) 