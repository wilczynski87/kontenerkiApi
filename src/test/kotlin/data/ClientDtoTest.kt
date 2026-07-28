package com.kontenery.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ClientDtoTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `toDto copies public fields and omits password`() {
        val client = Client(
            id = 10,
            clientPrivate = ClientPersonalData(
                firstName = "Jan",
                lastName = "Kowalski",
                email = "jan@example.com",
                pesel = "90010112345",
            ),
            clientCompany = ClientCompanyData(
                name = "Firma Sp. z o.o.",
                email = "firma@example.com",
                needInvoice = true,
            ),
            password = "sekretne-haslo",
            isActive = true,
            createdAt = LocalDate(2026, 1, 15),
            updatedAt = LocalDate(2026, 2, 20),
            invoiceTitle = "Faktura za kontener",
            bankAccounts = listOf("PL61109010140000071219812874"),
        )

        val dto = client.toDto()

        assertEquals(10, dto.id)
        assertEquals(client.clientPrivate, dto.clientPrivate)
        assertEquals(client.clientCompany, dto.clientCompany)
        assertEquals(true, dto.isActive)
        assertEquals(LocalDate(2026, 1, 15), dto.createdAt)
        assertEquals(LocalDate(2026, 2, 20), dto.updatedAt)
        assertEquals("Faktura za kontener", dto.invoiceTitle)
        assertEquals(listOf("PL61109010140000071219812874"), dto.bankAccounts)
    }

    @Test
    fun `serialized ClientDto does not contain password field`() {
        val dto = Client(
            id = 5,
            password = "nie-powinno-wyciec",
            clientPrivate = ClientPersonalData(email = "a@b.c"),
            isActive = true,
        ).toDto()

        val encoded = json.encodeToString(dto)

        assertFalse(encoded.contains("password"))
        assertFalse(encoded.contains("nie-powinno-wyciec"))
    }

    @Test
    fun `toDto works when optional fields are null`() {
        val dto = Client(id = 1, password = "x").toDto()

        assertEquals(1, dto.id)
        assertNull(dto.clientPrivate)
        assertNull(dto.clientCompany)
        assertNull(dto.isActive)
        assertNull(dto.createdAt)
        assertNull(dto.updatedAt)
        assertNull(dto.invoiceTitle)
        assertNull(dto.bankAccounts)
    }
}
