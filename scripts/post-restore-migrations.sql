-- Opcjonalne migracje po imporcie zrzutu (gdy DB_AUTO_MIGRATE=false lub migracja Exposed się wywala).
-- PostgreSQL przechowuje niecytowane nazwy jako małe litery (invoice, nie "Invoice").

ALTER TABLE invoice ADD COLUMN IF NOT EXISTS ksef_number VARCHAR(100);

CREATE TABLE IF NOT EXISTS ksef_session_invoice_status (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    reference_number VARCHAR(100),
    invoice_number VARCHAR(50),
    ksef_number VARCHAR(100),
    status_code INTEGER,
    status_description VARCHAR(500),
    permanent_storage_date VARCHAR(50)
);

ALTER TABLE submeter ADD COLUMN IF NOT EXISTS foto_url VARCHAR(100);

CREATE TABLE IF NOT EXISTS gate_event (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    opened_at_epoch_ms BIGINT NOT NULL,
    note VARCHAR(100)
);

ALTER TABLE clients ADD COLUMN IF NOT EXISTS password TEXT;
