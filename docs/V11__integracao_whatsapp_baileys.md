# V11 — Integração WhatsApp com Baileys

## Objetivo

O backend Java envia códigos de verificação de telefone pelo serviço independente `services/whatsapp-baileys`, que usa Baileys para entregar mensagens via sessão do WhatsApp Web. O envio é implementado por `BaileysMessageClient`, que usa a interface `WhatsAppMessageSender`; a regra de negócio e a confirmação do código continuam em `VerificacaoTelefoneService`.

## Configuração do backend

Defina no ambiente do backend:

- `WHATSAPP_PROVIDER=baileys`
- `BAILEYS_BASE_URL=https://<host-do-gateway>`
- `BAILEYS_API_TOKEN=<mesmo-token-configurado-no-gateway>`

O token deve ser secreto e igual ao `API_TOKEN` do serviço Baileys. Não o versionar no repositório.

Para rollback temporário, `WHATSAPP_PROVIDER=wppconnect` continua selecionando a implementação antiga, com as variáveis `WPP_CONNECT_*`. A integração Meta continua disponível com `WHATSAPP_PROVIDER=meta`.

## Endpoint de verificação no backend

### Solicitar código

`POST /api/auth/me/telefone/verificacao`

Requer autenticação Bearer do próprio backend. Body JSON:

```json
{
  "telefone": "75999999999"
}
```

O backend normaliza o telefone, salva o hash do código e a expiração, e solicita o envio. A resposta é `202 Accepted`, sem retornar o código.

### Confirmar código

`POST /api/auth/me/telefone/verificacao/confirmar`

Body JSON:

```json
{
  "codigo": "123456"
}
```

O código tem seis dígitos, expira em cinco minutos, permite até cinco tentativas e há intervalo de 60 segundos entre solicitações.

## Mensagem enviada pelo Baileys

O backend envia texto simples, não um template aprovado da WhatsApp Business Platform:

`Seu código de verificação é: 123456`

O código de exemplo é ilustrativo; em produção, o backend gera um código aleatório de seis dígitos. O gateway recebe `POST /v1/messages` com `Authorization: Bearer <API_TOKEN>` e body:

```json
{
  "phone": "5575999999999",
  "message": "Seu código de verificação é: 123456"
}
```

Números brasileiros locais de 10 ou 11 dígitos recebem o DDI `55` no cliente Baileys. Telefones que já contêm DDI são enviados apenas com os dígitos.

## Deploy do gateway no Render

O `render.yaml` define o serviço Node em `services/whatsapp-baileys`, health check `/health`, token `API_TOKEN` configurado manualmente e disco persistente montado em `/var/data`. Configure o mesmo token no ambiente do backend como `BAILEYS_API_TOKEN`.

Depois do deploy, valide `GET /health`, conecte a sessão via `POST /v1/session/start` e `GET /v1/session/qr`, confirme `GET /v1/session/status` e teste o envio de uma mensagem antes de considerar a migração pronta para produção.

## Observações

- O backend não recebe nem armazena o código em texto puro; armazena o hash.
- O envio é texto simples por Baileys. Se o provider for `meta`, a classe `WhatsAppMessageClient` usa o template configurado por `WHATSAPP_TEMPLATE_NAME` e o idioma `WHATSAPP_TEMPLATE_LANGUAGE`.
- Baileys é uma integração não oficial com WhatsApp Web; monitore compatibilidade e respeite os termos aplicáveis.
