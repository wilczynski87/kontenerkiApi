package com.kontenery.service.impl

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.kontenery.service.GoogleIdTokenVerifierService
import com.kontenery.service.GoogleUserClaims

class GoogleIdTokenVerifierServiceImpl(
    clientIds: List<String>,
) : GoogleIdTokenVerifierService {

    private val verifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
    )
        .setAudience(clientIds.filter { it.isNotBlank() && it != "1234567890" })
        .build()

    override fun verify(idToken: String): GoogleUserClaims? {
        val token = idToken.trim().takeUnless { it.isBlank() } ?: return null
        if (verifier.audience.isEmpty()) return null

        val googleToken = try {
            verifier.verify(token)
        } catch (_: IllegalArgumentException) {
            return null
        } ?: return null
        val payload = googleToken.payload
        val email = payload.email?.trim()?.lowercase()?.takeUnless { it.isBlank() } ?: return null
        val sub = payload.subject?.trim()?.takeUnless { it.isBlank() } ?: return null

        return GoogleUserClaims(
            sub = sub,
            email = email,
            emailVerified = payload.emailVerified == true,
        )
    }
}
