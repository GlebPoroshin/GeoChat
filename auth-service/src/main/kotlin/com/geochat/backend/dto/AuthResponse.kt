package com.geochat.backend.dto

import java.util.*

data class AuthResponse(
    val userId: UUID,
    val accessToken: String,
    val refreshToken: String
) 