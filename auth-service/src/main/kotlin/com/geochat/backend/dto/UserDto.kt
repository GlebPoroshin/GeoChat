package com.geochat.backend.dto

import java.util.*

data class UserDto(
    val id: UUID?,
    val nickname: String,
    val email: String,
    val roles: Set<String> = setOf()
) 