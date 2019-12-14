package com.sohosai.sos.domain.user

import io.ktor.auth.Principal
import java.util.*

data class User(
    val id: UUID,
    val name: String,
    val kanaName: String,
    val email: Email,
    val phoneNumber: PhoneNumber,
    val studentId: String,
    val affiliation: Affiliation,
    val role: Role
): Principal {
    fun hasPrivilege(role: Role): Boolean {
        return role.ordinal >= this.role.ordinal
    }
}