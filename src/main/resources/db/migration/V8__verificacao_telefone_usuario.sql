ALTER TABLE usuario
    ALTER COLUMN telefone DROP NOT NULL;

ALTER TABLE usuario
    ADD COLUMN telefone_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN telefone_verificado_em TIMESTAMPTZ;
