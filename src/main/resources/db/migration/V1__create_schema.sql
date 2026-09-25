CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TYPE usuario_status AS ENUM ('ATIVO', 'BLOQUEADO');
CREATE TYPE papel_nome AS ENUM ('CLIENTE', 'ADMIN');
CREATE TYPE servico_status AS ENUM ('ATIVO', 'DESATIVADO');
CREATE TYPE agendamento_status AS ENUM ('AGUARDANDO_PAGAMENTO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO', 'NAO_COMPARECEU');
CREATE TYPE reserva_status AS ENUM ('ATIVA', 'EXPIRADA');
CREATE TYPE pagamento_status AS ENUM ('PENDENTE', 'APROVADO', 'RECUSADO', 'CANCELADO');
CREATE TYPE cancelamento_origem AS ENUM ('CLIENTE', 'ATENDENTE');
CREATE TYPE reembolso_status AS ENUM ('SOLICITADO', 'PROCESSANDO', 'CONCLUIDO', 'FALHOU');
CREATE TYPE notificacao_tipo AS ENUM (
    'AGENDAMENTO_CONFIRMADO',
    'PAGAMENTO_APROVADO',
    'REAGENDAMENTO',
    'CANCELAMENTO',
    'LEMBRETE',
    'PROMOCAO',
    'SISTEMA'
);
CREATE TYPE envio_canal AS ENUM ('EMAIL', 'WHATSAPP');
CREATE TYPE envio_status AS ENUM ('PENDENTE', 'ENVIADO', 'FALHOU');

CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefone VARCHAR(30) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    status usuario_status NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE papel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome papel_nome NOT NULL UNIQUE
);

CREATE TABLE usuario_papel (
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    papel_id UUID NOT NULL REFERENCES papel(id),
    PRIMARY KEY (usuario_id, papel_id)
);

CREATE TABLE cliente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL UNIQUE REFERENCES usuario(id),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE perfil_preferencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL UNIQUE REFERENCES cliente(id),
    preferencias JSONB NOT NULL DEFAULT '{}'::jsonb,
    alergias_restricoes JSONB NOT NULL DEFAULT '[]'::jsonb,
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE servico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    duracao_minutos INTEGER NOT NULL CHECK (duracao_minutos > 0),
    preco NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    imagem_url VARCHAR(1000),
    status servico_status NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE servico_categoria (
    servico_id UUID NOT NULL REFERENCES servico(id),
    categoria_id UUID NOT NULL REFERENCES categoria(id),
    PRIMARY KEY (servico_id, categoria_id)
);

CREATE TABLE indisponibilidade_servico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    servico_id UUID NOT NULL REFERENCES servico(id),
    inicio TIMESTAMPTZ NOT NULL,
    fim TIMESTAMPTZ NOT NULL,
    motivo TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_indisponibilidade_intervalo CHECK (fim > inicio)
);

CREATE TABLE disponibilidade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dia_semana SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_disponibilidade_horario CHECK (hora_fim > hora_inicio)
);

CREATE TABLE bloqueio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inicio TIMESTAMPTZ NOT NULL,
    fim TIMESTAMPTZ NOT NULL,
    motivo TEXT,
    criado_por UUID NOT NULL REFERENCES usuario(id),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_bloqueio_intervalo CHECK (fim > inicio)
);

CREATE TABLE agendamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL REFERENCES cliente(id),
    servico_id UUID NOT NULL REFERENCES servico(id),
    inicio TIMESTAMPTZ NOT NULL,
    fim TIMESTAMPTZ NOT NULL,
    duracao_minutos INTEGER NOT NULL CHECK (duracao_minutos > 0),
    valor_servico NUMERIC(10,2) NOT NULL CHECK (valor_servico >= 0),
    valor_entrada NUMERIC(10,2) NOT NULL CHECK (valor_entrada >= 0),
    status agendamento_status NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_agendamento_intervalo CHECK (fim > inicio),
    CONSTRAINT ck_agendamento_entrada CHECK (valor_entrada <= valor_servico)
);

ALTER TABLE agendamento
ADD CONSTRAINT ex_agendamento_sem_conflito
EXCLUDE USING gist (
    tstzrange(inicio, fim, '[)') WITH &&
)
WHERE (status IN ('AGUARDANDO_PAGAMENTO', 'CONFIRMADO'));

CREATE TABLE reserva_temporaria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agendamento_id UUID NOT NULL UNIQUE REFERENCES agendamento(id),
    inicio TIMESTAMPTZ NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    status reserva_status NOT NULL DEFAULT 'ATIVA',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_reserva_expiracao CHECK (expira_em > inicio)
);

CREATE TABLE pagamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agendamento_id UUID NOT NULL UNIQUE REFERENCES agendamento(id),
    valor NUMERIC(10,2) NOT NULL CHECK (valor >= 0),
    status pagamento_status NOT NULL DEFAULT 'PENDENTE',
    provedor VARCHAR(100) NOT NULL,
    referencia_externa VARCHAR(255),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tentativa_pagamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reserva_id UUID NOT NULL REFERENCES reserva_temporaria(id),
    pagamento_id UUID NOT NULL REFERENCES pagamento(id),
    referencia_provedor VARCHAR(255),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reagendamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agendamento_id UUID NOT NULL REFERENCES agendamento(id),
    numero SMALLINT NOT NULL CHECK (numero IN (1, 2)),
    inicio_anterior TIMESTAMPTZ NOT NULL,
    inicio_novo TIMESTAMPTZ NOT NULL,
    motivo TEXT,
    realizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reagendamento_numero UNIQUE (agendamento_id, numero),
    CONSTRAINT ck_reagendamento_horario CHECK (inicio_novo <> inicio_anterior)
);

CREATE TABLE cancelamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agendamento_id UUID NOT NULL UNIQUE REFERENCES agendamento(id),
    origem cancelamento_origem NOT NULL,
    motivo TEXT,
    cancelado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelado_por UUID REFERENCES usuario(id)
);

CREATE TABLE reembolso (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pagamento_id UUID NOT NULL REFERENCES pagamento(id),
    cancelamento_id UUID REFERENCES cancelamento(id),
    valor NUMERIC(10,2) NOT NULL CHECK (valor > 0),
    status reembolso_status NOT NULL DEFAULT 'SOLICITADO',
    referencia_externa VARCHAR(255),
    solicitado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processado_em TIMESTAMPTZ,
    motivo TEXT
);

CREATE TABLE notificacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    agendamento_id UUID REFERENCES agendamento(id),
    tipo notificacao_tipo NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensagem TEXT NOT NULL,
    criada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lida_em TIMESTAMPTZ
);

CREATE TABLE envio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacao_id UUID NOT NULL REFERENCES notificacao(id),
    canal envio_canal NOT NULL,
    status envio_status NOT NULL DEFAULT 'PENDENTE',
    enviado_em TIMESTAMPTZ,
    tentativas INTEGER NOT NULL DEFAULT 0 CHECK (tentativas >= 0),
    erro TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auditoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID REFERENCES usuario(id),
    acao VARCHAR(100) NOT NULL,
    recurso_tipo VARCHAR(100) NOT NULL,
    recurso_id UUID NOT NULL,
    resultado VARCHAR(50) NOT NULL,
    contexto JSONB,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_agendamento_cliente_inicio ON agendamento (cliente_id, inicio);
CREATE INDEX idx_agendamento_servico_inicio ON agendamento (servico_id, inicio);
CREATE INDEX idx_agendamento_inicio_fim_status ON agendamento (inicio, fim, status);
CREATE INDEX idx_reserva_status_expira ON reserva_temporaria (status, expira_em);
CREATE INDEX idx_pagamento_status ON pagamento (status);
CREATE INDEX idx_pagamento_referencia_externa ON pagamento (referencia_externa);
CREATE INDEX idx_notificacao_usuario_criada ON notificacao (usuario_id, criada_em);
CREATE INDEX idx_notificacao_usuario_lida ON notificacao (usuario_id, lida_em);
CREATE INDEX idx_envio_notificacao_status ON envio (notificacao_id, status);
CREATE INDEX idx_auditoria_recurso ON auditoria (recurso_tipo, recurso_id);
CREATE INDEX idx_auditoria_usuario_criada ON auditoria (usuario_id, criado_em);
CREATE INDEX idx_auditoria_criada ON auditoria (criado_em);

INSERT INTO papel (nome) VALUES ('CLIENTE'), ('ADMIN');
