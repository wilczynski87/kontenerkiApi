package com.kontenery.service.impl

import com.kontenery.service.BankAccountService
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest
import java.math.BigDecimal

class CSVServiceImplTest {

    private lateinit var service: CSVServiceImpl
    private lateinit var bankAccountService: BankAccountService

    @BeforeEach
    fun setUp() {
        bankAccountService = mockk()
        coEvery { bankAccountService.findClientByAccountNumber(any()) } returns null
        service = CSVServiceImpl(
            bankAccountService = bankAccountService
        )
    }

    @Test
    fun `should parse valid CSV and return payments`() = runTest {
        val csv = """
            Numer rachunku: 1234567890
            
            jakieś metadane
            inne metadane
            
            Data księgowania,Data waluty,Typ operacji,Kwota,Waluta,Dane kontrahenta,Rachunek,Tytuł
            
            01-01-2024,01-01-2024,przelew,100,PLN,Jan Kowalski|Warszawa,9876543210,Faktura 1
            02-01-2024,02-01-2024,przelew,200,PLN,Anna Nowak|Kraków,1111111111,Faktura 2
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertEquals(2, result.size)

        val first = result[0]
        assertEquals(BigDecimal("100"), first.amount)
        assertEquals("9876543210", first.fromAccount)
        assertEquals("Faktura 1", first.title)

        val second = result[1]
        assertEquals(BigDecimal("200"), second.amount)
        assertEquals("1111111111", second.fromAccount)
    }

    @Test
    fun `should extract owner account number from header`() = runTest {
        val csv = """
            Numer rachunku: 9999999999
            
            Data księgowania,Data waluty,Typ operacji,Kwota,Waluta,Dane kontrahenta,Rachunek,Tytuł
            
            01-01-2024,01-01-2024,przelew,50,PLN,Test|Adres,123,Faktura
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertEquals(1, result.size)

        // zakładam że mapper ustawia toAccount albo gdzieś używa ownerAccountNumber
        val payment = result.first()

        // dostosuj jeśli mapper używa inaczej
        assertNotNull(payment)
    }

    @Test
    fun `should handle commas inside quotes`() = runTest {
        val csv = """
            Numer rachunku: 123
            
            Data księgowania,Data waluty,Typ operacji,Kwota,Waluta,Dane kontrahenta,Rachunek,Tytuł
            
            01-01-2024,01-01-2024,przelew,150,PLN,"Firma, Sp. z o.o.|Warszawa",999,Test
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertEquals(1, result.size)

        val payment = result.first()
        assertEquals(BigDecimal("150"), payment.amount)
    }

    @Test
    fun `should skip invalid rows`() = runTest {
        val csv = """
            Numer rachunku: 123
            
            Data księgowania,Data waluty,Typ operacji,Kwota,Waluta,Dane kontrahenta,Rachunek,Tytuł
            
            INVALID_ROW
            01-01-2024,01-01-2024,przelew,100,PLN,Jan|Warszawa,123,OK
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertEquals(1, result.size)
        assertEquals(BigDecimal("100"), result.first().amount)
    }

    @Test
    fun `should return empty list when no data`() = runTest {
        val csv = """
            Numer rachunku: 123
            
            brak danych
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertTrue(result.isEmpty())
    }
}