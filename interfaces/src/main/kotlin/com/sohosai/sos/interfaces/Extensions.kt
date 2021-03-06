package com.sohosai.sos.interfaces

import com.sohosai.sos.domain.user.User
import com.sohosai.sos.domain.user.UserRepository
import org.koin.core.context.GlobalContext

// TODO: inject this by right way
private val userRepository: UserRepository by GlobalContext.get().koin.inject()

suspend fun AuthContext.toUser(): User {
    return userRepository.findUserByAuthId(authId) ?: throw HttpStatusCodeException(401, "Not registered yet")
}