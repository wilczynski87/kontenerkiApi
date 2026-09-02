package com.kontenery.service.impl

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class GoogleIdTokenVerifierServiceImplTest {

    private val verifier = GoogleIdTokenVerifierServiceImpl(
        listOf("695782084929-utc9fibe15r1hirugo5g2trfnc80souv.apps.googleusercontent.com"),
    )

    @Test
    fun `verify returns null for invalid token without throwing`() {
        assertDoesNotThrow {
            assertNull(verifier.verify("invalid"))
        }
    }

    @Test
    fun `verify returns null when audience is not configured`() {
        val unconfigured = GoogleIdTokenVerifierServiceImpl(listOf("1234567890"))
        assertNull(unconfigured.verify("any-token"))
    }
}
