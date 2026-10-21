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
- `AUTH_DIR`: diretório de credenciais Baileys. Padrão `/var/data/auth_info`; requer disco persistente montado em `/var/data` no Render.
- `WEBHOOK_URL`: opcional; URL do endpoint do backend para eventos.
- `WEBHOOK_SECRET`: opcional; segredo compartilhado para autenticar webhooks.
- `LOG_LEVEL`: opcional; padrão `info`.

## Render

Configure Root Directory como `services/whatsapp-baileys`, Build Command `npm install`, Start Command `npm start`, runtime Node e um disco persistente montado em `/var/data`. Configure `API_TOKEN` antes de iniciar o deploy. Sem disco persistente, o pareamento pode ser perdido em reinicializações/redeploys.

## Segurança e operação

- Nunca comite `.env` ou a pasta de credenciais.
- Restrinja o acesso à API e use um token longo e aleatório.
- O endpoint de QR deve ser acessado apenas por usuários autorizados.
- Baileys é uma integração não oficial com WhatsApp Web; mudanças no WhatsApp podem quebrar a compatibilidade e o uso deve respeitar os termos aplicáveis.
- Este gateway ainda precisa de testes de integração e de uma validação de compatibilidade com o contrato de webhook esperado pelo backend Java antes de uso em produção.
