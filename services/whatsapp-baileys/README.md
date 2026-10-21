# Agenda Julyana WhatsApp Gateway

Serviço independente Node.js + Baileys. Não altera o backend Java nem substitui automaticamente o gateway WPPConnect.

## Endpoints

Todos os endpoints `/v1/*` exigem `Authorization: Bearer <API_TOKEN>`.

- `GET /health` — health check público, sem dados sensíveis.
- `POST /v1/session/start` — inicia a conexão.
- `GET /v1/session/status` — estado da conexão.
- `GET /v1/session/qr` — QR em texto e Data URL para exibição.
- `POST /v1/messages` — envia mensagem; body: `{"phone":"55DDDNUMERO","message":"Texto"}`.
- `POST /v1/session/logout` — desconecta e apaga credenciais persistidas.

Eventos opcionais enviados para `WEBHOOK_URL`: `connection.open`, `connection.close`, `message.received`. Se definido, `WEBHOOK_SECRET` é enviado no header `x-webhook-secret`.

## Variáveis de ambiente

- `PORT`: fornecida automaticamente pelo Render.
- `API_TOKEN`: segredo aleatório forte para proteger a API; obrigatório.
- `AUTH_STORAGE`: `postgres` ou `filesystem`. Se `DATABASE_URL` estiver definida, o padrão é `postgres`; caso contrário, usa `filesystem` para compatibilidade.
- `DATABASE_URL`: string de conexão PostgreSQL usada para persistir credenciais e todas as chaves criptográficas do Baileys. Obrigatória quando `AUTH_STORAGE=postgres`.
- `AUTH_SESSION_ID`: identificador lógico da sessão no banco; padrão `agenda-julyana`. Mantenha estável entre deploys e use um ID diferente para cada instância lógica.
- `PGSSL_REJECT_UNAUTHORIZED`: opcional; defina `false` somente se o provedor exigir TLS sem validação do certificado.
- `AUTH_DIR`: diretório local usado apenas com `AUTH_STORAGE=filesystem`; padrão `/var/data/auth_info`.
- `WEBHOOK_URL`: opcional; URL do endpoint do backend para eventos.
- `WEBHOOK_SECRET`: opcional; segredo compartilhado para autenticar webhooks.
- `LOG_LEVEL`: opcional; padrão `info`.

## Render

Configure Root Directory como `services/whatsapp-baileys`, Build Command `npm install` e Start Command `npm start`. Para persistência sem disco local, configure `AUTH_STORAGE=postgres` e `DATABASE_URL` no serviço do gateway, além de `API_TOKEN`. O gateway cria a tabela `baileys_auth_state` automaticamente. Use apenas uma instância ativa por `AUTH_SESSION_ID` para evitar sockets concorrentes.

## Segurança e operação

- Nunca comite `.env` ou a pasta de credenciais.
- Restrinja o acesso à API e use um token longo e aleatório.
- O endpoint de QR deve ser acessado apenas por usuários autorizados.
- Baileys é uma integração não oficial com WhatsApp Web; mudanças no WhatsApp podem quebrar a compatibilidade e o uso deve respeitar os termos aplicáveis.
- Este gateway ainda precisa de testes de integração e de uma validação de compatibilidade com o contrato de webhook esperado pelo backend Java antes de uso em produção.


## Migração para PostgreSQL

1. Crie/prepare o PostgreSQL e configure `DATABASE_URL` no serviço do gateway.
2. Configure `AUTH_STORAGE=postgres` e `AUTH_SESSION_ID=agenda-julyana`.
3. Faça deploy e confira os logs por `Persistência PostgreSQL preparada`.
4. Como a sessão atual pode estar apenas nos arquivos locais efêmeros, esteja preparado para escanear um novo QR Code uma única vez após ativar o armazenamento PostgreSQL. Depois de conectado, as credenciais e chaves serão salvas no banco e reutilizadas em reinicializações.
5. Não execute `POST /v1/session/logout` para testar persistência: esse endpoint desconecta o WhatsApp e apaga a sessão armazenada.

A tabela contém material de autenticação sensível. Restrinja o acesso ao banco, use TLS e nunca exponha `DATABASE_URL` em logs, respostas HTTP ou no repositório.
