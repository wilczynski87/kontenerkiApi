package com.kontenery.data.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BankAccountNormalizeTest {

    @Test
    fun `normalize adds PL prefix when missing`() {
        assertEquals(
            "PL25114010100000536605001001",
            BankAccount.normalize("25114010100000536605001001"),
        )
    }

    @Test
    fun `normalize keeps existing country prefix`() {
        assertEquals(
            "PL36188000090000001102843000",
            BankAccount.normalize("PL36188000090000001102843000"),
        )
    }

    @Test
    fun `normalize strips whitespace`() {
        assertEquals(
            "PL25114010100000536605001001",
            BankAccount.normalize("PL25 1140 1010 0000 5366 0500 1001"),
        )
    }

    @Test
    fun `lookupVariants include prefixed and bare forms`() {
        assertEquals(
            listOf("PL25114010100000536605001001", "25114010100000536605001001"),
            BankAccount.lookupVariants("25114010100000536605001001"),
        )
        assertEquals(
            listOf("PL25114010100000536605001001", "25114010100000536605001001"),
            BankAccount.lookupVariants("PL25114010100000536605001001"),
        )
    }
}
