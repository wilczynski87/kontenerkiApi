-- Run once on databases created before KSeF integration (when API_ENV=PROD or schema sync was skipped).
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS ksef_number VARCHAR(100);
