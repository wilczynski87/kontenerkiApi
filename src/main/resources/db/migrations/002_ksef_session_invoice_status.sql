-- Run once on databases created before KSeF session status persistence.
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
