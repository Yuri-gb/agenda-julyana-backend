CREATE TABLE verificacao_telefone (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    codigo_hash VARCHAR(64) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    verificado_em TIMESTAMPTZ,
    utilizado_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_verificacao_telefone_usuario_criado
    ON verificacao_telefone (usuario_id, criado_em DESC);

CREATE INDEX idx_verificacao_telefone_ativa
    ON verificacao_telefone (usuario_id, utilizado_em)
    WHERE utilizado_em IS NULL;