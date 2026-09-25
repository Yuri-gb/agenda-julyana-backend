ALTER TABLE usuario ALTER COLUMN telefone DROP NOT NULL;

CREATE TYPE provedor_autenticacao AS ENUM ('LOCAL', 'GOOGLE');

CREATE TABLE identidade_autenticacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    provedor provedor_autenticacao NOT NULL,
    identificador_externo VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(255),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_identidade_provedor_externo UNIQUE (provedor, identificador_externo),
    CONSTRAINT uq_identidade_usuario_provedor UNIQUE (usuario_id, provedor),
    CONSTRAINT ck_identidade_senha CHECK (
        (provedor = 'LOCAL' AND senha_hash IS NOT NULL)
        OR (provedor = 'GOOGLE' AND senha_hash IS NULL)
    )
);

INSERT INTO identidade_autenticacao (usuario_id, provedor, identificador_externo, senha_hash)
SELECT id, 'LOCAL', LOWER(email), senha_hash FROM usuario;

ALTER TABLE usuario DROP COLUMN senha_hash;

CREATE INDEX idx_identidade_usuario ON identidade_autenticacao (usuario_id);
CREATE INDEX idx_identidade_provedor ON identidade_autenticacao (provedor, identificador_externo);
