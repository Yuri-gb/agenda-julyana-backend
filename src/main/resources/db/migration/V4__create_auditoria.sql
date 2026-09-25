CREATE TABLE auditoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID REFERENCES usuario(id),
    acao VARCHAR(100) NOT NULL,
    recurso_tipo VARCHAR(100) NOT NULL,
    recurso_id UUID,
    resultado VARCHAR(30) NOT NULL,
    contexto JSONB,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auditoria_usuario ON auditoria(usuario_id);
CREATE INDEX idx_auditoria_recurso ON auditoria(recurso_tipo, recurso_id);
CREATE INDEX idx_auditoria_criado_em ON auditoria(criado_em);
