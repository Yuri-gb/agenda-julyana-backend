ALTER TABLE pagamento
    ADD COLUMN checkout_url text;

CREATE INDEX idx_pagamento_referencia_externa
    ON pagamento (referencia_externa);
