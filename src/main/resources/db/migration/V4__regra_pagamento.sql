CREATE TYPE pagamento_modalidade AS ENUM ('ENTRADA', 'PAGAMENTO_TOTAL');

ALTER TABLE pagamento
    ADD COLUMN modalidade pagamento_modalidade;

UPDATE pagamento
SET modalidade = 'ENTRADA'
WHERE modalidade IS NULL;

ALTER TABLE pagamento
    ALTER COLUMN modalidade SET NOT NULL;

CREATE INDEX idx_pagamento_agendamento_status
    ON pagamento (agendamento_id, status);
