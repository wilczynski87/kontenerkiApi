-- Opcjonalne migracje po imporcie zrzutu (gdy DB_AUTO_MIGRATE=false lub migracja Exposed się wywala).
-- Nazwy tabel zgodne z Exposed (wielkość liter ma znaczenie w PostgreSQL).

ALTER TABLE "Invoice" ADD COLUMN IF NOT EXISTS ksef_number VARCHAR(100);

CREATE TABLE IF NOT EXISTS ksef_session_invoice_status (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES "Invoice"(id) ON DELETE CASCADE,
    reference_number VARCHAR(100),
    invoice_number VARCHAR(50),
    ksef_number VARCHAR(100),
    status_code INTEGER,
    status_description VARCHAR(500),
    permanent_storage_date VARCHAR(50)
);

ALTER TABLE submeter ADD COLUMN IF NOT EXISTS foto_url VARCHAR(100);
