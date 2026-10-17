package com.lumatech.voltup

import kotlinx.coroutines.flow.StateFlow

data class AuthResult(
    val isSuccess: Boolean,
    val user: VoltUpUser? = null,
    val errorMessage: String? = null
)

interface VoltUpAuthService {
    val currentUser: StateFlow<VoltUpUser?>

    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(email: String, password: String, userName: String, avatarId: Int): AuthResult
    suspend fun sendPasswordReset(email: String): AuthResult
    suspend fun changePassword(oldPassword: String, newPassword: String): AuthResult
    suspend fun updateProfile(userName: String, avatarId: Int): AuthResult
    suspend fun deleteAccount(): AuthResult
    suspend fun signOut()
}

expect fun getAuthService(): VoltUpAuthService
