ALTER TABLE usuario
    ADD COLUMN telefone_verificado BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE usuario
    ADD COLUMN telefone_verificado_em TIMESTAMPTZ;

ALTER TABLE agendamento
    ADD COLUMN google_event_id VARCHAR(255);

CREATE INDEX idx_agendamento_google_event_id
    ON agendamento (google_event_id)
    WHERE google_event_id IS NOT NULL;

CREATE INDEX idx_usuario_telefone_verificado
    ON usuario (telefone_verificado, telefone);
