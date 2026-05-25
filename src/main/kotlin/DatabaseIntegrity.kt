package com.kontenery

import java.sql.DriverManager

/**
 * Po imporcie zrzutu mogą zostać faktury z seller_id/customer_id bez rekordu w subjects.
 * To blokuje migrację Exposed i zapis nowych faktur (FK fk_invoice_seller_id__id).
 */
internal fun repairInvoiceForeignKeysIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_REPAIR_FK")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            val broken = stmt.executeQuery(
                """
                SELECT COUNT(*) AS cnt FROM invoice i
                WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.seller_id)
                   OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.customer_id)
                """.trimIndent(),
            )
            val count = if (broken.next()) broken.getLong("cnt") else 0L
            if (count == 0L) return
            println("DB_REPAIR_FK: removing $count invoice row(s) with invalid seller_id/customer_id")
            stmt.execute(
                """
                DELETE FROM ksef_session_invoice_status WHERE invoice_id IN (
                  SELECT i.id FROM invoice i
                  WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.seller_id)
                     OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.customer_id)
                )
                """.trimIndent(),
            )
            stmt.execute(
                """
                DELETE FROM paymentinvoices WHERE invoice_id IN (
                  SELECT i.id FROM invoice i
                  WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.seller_id)
                     OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.customer_id)
                )
                """.trimIndent(),
            )
            stmt.execute(
                """
                DELETE FROM positions WHERE invoice_id IN (
                  SELECT i.id FROM invoice i
                  WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.seller_id)
                     OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.customer_id)
                )
                """.trimIndent(),
            )
            stmt.execute(
                """
                DELETE FROM invoice
                WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = seller_id)
                   OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = customer_id)
                """.trimIndent(),
            )
            stmt.execute(
                """
                DELETE FROM positionsbill WHERE bill_id IN (
                  SELECT b.id FROM bill b
                  WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = b.seller_id)
                     OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = b.customer_id)
                )
                """.trimIndent(),
            )
            stmt.execute(
                """
                DELETE FROM bill
                WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = seller_id)
                   OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = customer_id)
                """.trimIndent(),
            )
        }
    }
}
