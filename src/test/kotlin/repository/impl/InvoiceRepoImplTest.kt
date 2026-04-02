package repository.impl

import com.kontenery.repository.entity.invoice.InvoiceTable
import com.kontenery.repository.entity.invoice.Subjects
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.assertEquals
import com.kontenery.data.utils.startOfCurrentMonth
import com.kontenery.repository.entity.invoice.*
import com.kontenery.repository.impl.InvoiceRepoImpl
import kotlinx.datetime.minus


class InvoiceRepoImplTest {

    @BeforeEach
    fun setUp() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver"
        )

        // Create tables
        transaction {
            SchemaUtils.create(InvoiceTable, Subjects) // Subjects = your seller/customer table
        }
    }

    @AfterEach
    fun tearDown() {
        transaction {
            SchemaUtils.drop(InvoiceTable, Subjects)
        }
    }

    @Test
    fun `should return invoice with highest number in current month`() = runBlocking {
        // given
        val currentMonth = LocalDate.startOfCurrentMonth()

        transaction {
            val seller = SubjectEntity.new {
                name = "Test Seller"
                type = "SELLER"
                email = "test@test.pl"
            }
            val customer = SubjectEntity.new {
                name = "Test Customer"
                type = "CUSTOMER"
                email = "test@test.pl"
            }

            InvoiceEntity.new {
                invoiceNumber = "1/05/2025"
                invoiceTitle = "Invoice 1"
                invoiceDate = currentMonth
                this.seller = seller
                this.customer = customer
                vatAmountSum = "0"
                priceSum = "100"
                priceWithVatSum = "123"
                paymentDay = currentMonth
                mainAccount = "123"
                invoiceSendToClient = null
            }

            InvoiceEntity.new {
                invoiceNumber = "5/05/2025"
                invoiceTitle = "Invoice 5"
                invoiceDate = currentMonth
                this.seller = seller
                this.customer = customer
                vatAmountSum = "0"
                priceSum = "500"
                priceWithVatSum = "615"
                paymentDay = currentMonth
                mainAccount = "123"
                invoiceSendToClient = null
            }

            // This one is higher but from a different month
            InvoiceEntity.new {
                invoiceNumber = "10/04/2025"
                invoiceTitle = "Invoice 10"
                invoiceDate = currentMonth.minus(1, kotlinx.datetime.DateTimeUnit.MONTH)
                this.seller = seller
                this.customer = customer
                vatAmountSum = "0"
                priceSum = "1000"
                priceWithVatSum = "1230"
                paymentDay = currentMonth.minus(1, kotlinx.datetime.DateTimeUnit.MONTH)
                mainAccount = "123"
                invoiceSendToClient = null
            }
        }

        // when
        val repo = InvoiceRepoImpl()
        val result = repo.getLastInvoiceNumber()

        // then
        assertEquals("5/5/2025", result)
    }
}