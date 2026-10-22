# V10__integracao_whatsapp_wppconnect

**Projeto:** Sistema de Agendamento — Julyana Lima  
**Versão:** V10  
**Status:** Migração em andamento  
**Data:** 29/09/2026

## 1. Objetivo

Registrar a migração da integração de WhatsApp para WPPConnect dentro da branch `feat/whatsapp-wppconnect`.

A integração oficial da Meta permanece presente nesta etapa. O objetivo é comprovar o WPPConnect antes de remover a implementação anterior.

## 2. Estratégia

```text
feat/whatsapp-wppconnect
        │
        ├── WPPConnect
        ├── Docker
        ├── Render
        ├── conexão da sessão
        ├── envio de mensagem
        ├── integração com backend
        ├── testes automatizados
        └── CI
```

A branch `feat/fechamento-agendamento-pagamento` não faz parte desta migração.

## 3. Arquitetura de transição

O backend utiliza a porta `WhatsAppMessageSender`.

Implementações disponíveis:

- Meta: `WhatsAppMessageClient`
- WPPConnect: `WppConnectClient`

A seleção é feita por `WHATSAPP_PROVIDER`:

- `wppconnect` — padrão nesta branch;
- `meta` — mantém a integração anterior disponível durante a migração.

## 4. WPPConnect

O serviço dedicado utiliza a imagem `wppconnect/wppconnect-server:2.10.27`.

O backend conversa com a API do WPPConnect para:

- iniciar a sessão;
- consultar o status;
- enviar o código de verificação;
- receber eventos pelo webhook separado.

## 5. Persistência

A sessão do WhatsApp precisa sobreviver a reinícios.

No Compose, os dados do servidor são persistidos em volumes Docker.

No Render, o serviço possui disco persistente montado em `/usr/src/wpp-server`.

## 6. Webhooks

O endpoint legado da Meta permanece em:

`/api/webhooks/whatsapp`

O endpoint do WPPConnect fica separado em:

`/api/webhooks/whatsapp/wppconnect`

Nenhum dos dois substitui o outro durante esta fase.

## 7. Configuração do backend

```text
WHATSAPP_PROVIDER=wppconnect
WPP_CONNECT_BASE_URL=http://localhost:21465
WPP_CONNECT_TOKEN=<token do WPPConnect>
WPP_CONNECT_SESSION=agenda-julyana
```

Em produção, `WPP_CONNECT_BASE_URL` deverá apontar para o serviço WPPConnect na rede privada do Render quando aplicável.

## 8. Testes

Foram adicionados testes para:

- envio pelo cliente WPPConnect;
- autenticação Bearer;
- consulta do status da sessão;
- webhook WPPConnect;
- fluxo existente de verificação de telefone usando a abstração do provedor.

O teste automatizado não substitui o teste real com uma conta WhatsApp autorizada.

## 9. Critério de remoção da Meta

A implementação da Meta somente deverá ser removida depois de:

1. WPPConnect saudável no Render;
2. sessão WhatsApp conectada;
3. envio real validado;
4. verificação de telefone validada pelo backend;
5. webhook validado;
6. CI verde;
7. ausência de regressões.

Somente então a integração Meta poderá ser retirada nesta mesma branch.
