-- Usuwa faktury/rachunki z seller_id lub customer_id nieistniejącym w subjects
-- (typowy problem po częściowym imporcie zrzutu lub uszkodzonych FK w dumpie).
-- Uruchom po imporcie, przed startem API: psql ... -f scripts/fix-invoice-fk-integrity.sql

DO $$
DECLARE
  broken_invoices INT;
  broken_bills INT;
BEGIN
  SELECT COUNT(*) INTO broken_invoices
  FROM invoice i
  WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.seller_id)
     OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.customer_id);

  SELECT COUNT(*) INTO broken_bills
  FROM bill b
  WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = b.seller_id)
     OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = b.customer_id);

  RAISE NOTICE 'Broken invoice rows: %, broken bill rows: %', broken_invoices, broken_bills;

  IF broken_invoices > 0 THEN
    DELETE FROM ksef_session_invoice_status
    WHERE invoice_id IN (
      SELECT i.id FROM invoice i
      WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.seller_id)
         OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.customer_id)
    );
    DELETE FROM paymentinvoices
    WHERE invoice_id IN (
      SELECT i.id FROM invoice i
      WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.seller_id)
         OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.customer_id)
    );
    DELETE FROM positions
    WHERE invoice_id IN (
      SELECT i.id FROM invoice i
      WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.seller_id)
         OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = i.customer_id)
    );
    DELETE FROM invoice
    WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = seller_id)
       OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = customer_id);
  END IF;

  IF broken_bills > 0 THEN
    DELETE FROM positionsbill
    WHERE bill_id IN (
      SELECT b.id FROM bill b
      WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = b.seller_id)
         OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = b.customer_id)
    );
    DELETE FROM bill
    WHERE NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = seller_id)
       OR NOT EXISTS (SELECT 1 FROM subjects s WHERE s.id = customer_id);
  END IF;
END $$;
