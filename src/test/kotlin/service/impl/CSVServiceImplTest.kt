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
            Numer rachunku: 51187010452078108959440001
            Właściciel: KONTENERY MAGAZYNOWE SPÓŁKA Z OGRANICZONĄ ODPOWIEDZIALNOŚCIĄ
            Historia operacji za okres od 2026-01-01 do 13.05.2026
            Liczba operacji: 24
            Suma uznań: 22095.89 PLN
            Suma obciążeń: 0 PLN
            Data księgowania,Data operacji,Rodzaj operacji,Kwota,Waluta,Dane kontrahenta,Numer rachunku kontrahenta,Tytuł operacji,Saldo po operacji
            15-04-2026,15-04-2026,Przelewy przychodzące,984,PLN,KRUSZWIL MAREK KRUSZEL|UL.SIENNA 9 70-542 SZCZECIN,72114020040000320278657853,18/4/2026,9419.4
            13-04-2026,13-04-2026,Przelewy przychodzące,350,PLN,MARCIN NALAZEK UL. WOLBROMSKA 18/1B|53-148 WROCŁAW,82109025290000000151502141,Faktura numer 1/4/2026,4472.03
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertEquals(2, result.size)

        val first = result[0]
        assertEquals(BigDecimal("984"), first.amount)
        assertEquals("72114020040000320278657853", first.fromAccount)
        assertEquals("18/4/2026", first.title)

        val second = result[1]
        assertEquals(BigDecimal("350"), second.amount)
        assertEquals("82109025290000000151502141", second.fromAccount)
        assertEquals("Faktura numer 1/4/2026", second.title)
    }

    @Test
    fun `should extract owner account number from header`() = runTest {
        val csv = """
            Numer rachunku: 51187010452078108959440001
            Właściciel: KONTENERY MAGAZYNOWE SPÓŁKA Z OGRANICZONĄ ODPOWIEDZIALNOŚCIĄ
            Data księgowania,Data operacji,Rodzaj operacji,Kwota,Waluta,Dane kontrahenta,Numer rachunku kontrahenta,Tytuł operacji,Saldo po operacji
            15-04-2026,15-04-2026,Przelewy przychodzące,492,PLN,FLORIS OGRODY SPÓŁKA Z OGRANICZONĄ ODPOWIEDZIALNOŚCIĄ|UL.ŚW. MIKOŁAJA 30/31 M.1C 50-128 WROCŁAW,66114020040000330282694955,9/4/2026,8435.4
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertEquals(1, result.size)

        val payment = result.first()
        assertNotNull(payment)
        assertEquals(BigDecimal("492"), payment.amount)
        assertEquals("66114020040000330282694955", payment.fromAccount)
    }

    @Test
    fun `should handle commas inside quotes`() = runTest {
        val csv = """
            Numer rachunku: 51187010452078108959440001
            Data księgowania,Data operacji,Rodzaj operacji,Kwota,Waluta,Dane kontrahenta,Numer rachunku kontrahenta,Tytuł operacji,Saldo po operacji
            08-05-2026,08-05-2026,Przelewy przychodzące,1275.51,PLN,MYSZKOWSKI BENEDYKT|SZKLARKA MYŚLNIEWSKA 28 63-500 OSTRZESZÓW,92124019941111001006915996,"Przelew środków, Nr faktury: Faktura NR 25/5/2026, Kwota VAT: 238,51, Identyfikator: 8943278612",11061.1
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertEquals(1, result.size)

        val payment = result.first()
        assertEquals(BigDecimal("1275.51"), payment.amount)
        assertEquals("92124019941111001006915996", payment.fromAccount)
    }

    @Test
    fun `should skip invalid rows`() = runTest {
        val csv = """
            Numer rachunku: 51187010452078108959440001
            Data księgowania,Data operacji,Rodzaj operacji,Kwota,Waluta,Dane kontrahenta,Numer rachunku kontrahenta,Tytuł operacji,Saldo po operacji
            INVALID_ROW
            13-04-2026,13-04-2026,Przelewy przychodzące,553.5,PLN,ŚWIAT GROUP SPÓŁKA Z OGRANICZONĄ ODPOWIEDZIALNOŚCIĄ|RYNEK 60 M.2 50-116 WROCŁAW,79114020040000390285343594,FAKTURA VAT 28/4/2026,4122.03
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertEquals(1, result.size)
        assertEquals(BigDecimal("553.5"), result.first().amount)
    }

    @Test
    fun `should return empty list when no data`() = runTest {
        val csv = """
            Numer rachunku: 51187010452078108959440001
            Właściciel: KONTENERY MAGAZYNOWE SPÓŁKA Z OGRANICZONĄ ODPOWIEDZIALNOŚCIĄ
            Historia operacji za okres od 2026-01-01 do 13.05.2026
            Liczba operacji: 0
        """.trimIndent()

        val result = service.readCSVNest(csv)

        assertTrue(result.isEmpty())
    }
}