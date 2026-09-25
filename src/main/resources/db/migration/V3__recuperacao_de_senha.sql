CREATE TABLE recuperacao_senha (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    codigo_hash VARCHAR(255) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    verificado_em TIMESTAMPTZ,
    token_redefinicao_hash VARCHAR(64),
    token_redefinicao_expira_em TIMESTAMPTZ,
    utilizado_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recuperacao_senha_usuario
    ON recuperacao_senha (usuario_id, criado_em DESC);

CREATE INDEX idx_recuperacao_senha_token
    ON recuperacao_senha (token_redefinicao_hash)
    WHERE token_redefinicao_hash IS NOT NULL;
