package com.kontenery.service

data class GoogleUserClaims(
    val sub: String,
    val email: String,
    val emailVerified: Boolean,
)

interface GoogleIdTokenVerifierService {
    fun verify(idToken: String): GoogleUserClaims?
}
