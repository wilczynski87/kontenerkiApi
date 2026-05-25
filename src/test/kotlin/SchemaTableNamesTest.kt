package schema

import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.invoice.*
import com.kontenery.repository.entity.ksef.KsefSessionInvoiceStatusTable
import org.junit.jupiter.api.Test

class SchemaTableNamesTest {
    @Test
    fun `print expected postgres table names`() {
        val tables = listOf(
            AddressTable.tableName,
            ClientPersonalDataTable.tableName,
            ClientCompanyDataTable.tableName,
            ClientTable.tableName,
            ProductTable.tableName,
            ContractTable.tableName,
            DepositTable.tableName,
            Subjects.tableName,
            Positions.tableName,
            InvoiceTable.tableName,
            BillTable.tableName,
            PositionsBill.tableName,
            PaymentTable.tableName,
            PaymentInvoices.tableName,
            ClientBankAccountTable.tableName,
            SubmeterTable.tableName,
            ReadingTable.tableName,
            KsefSessionInvoiceStatusTable.tableName,
        )
        println("EXPECTED_TABLES=" + tables.joinToString(","))
    }
}
