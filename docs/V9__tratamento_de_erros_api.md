# V9 — Tratamento de erros da API

## Objetivo

Padronizar as respostas de erro da API para facilitar testes automatizados, consumo pelo frontend e diagnóstico de falhas.

O tratamento é centralizado em `GlobalExceptionHandler`. Erros de autenticação/autorização do Spring Security são tratados por `ApiAuthenticationEntryPoint` e `ApiAccessDeniedHandler`, pois podem ocorrer antes do controller.

## Contrato

Toda resposta de erro da API utiliza:

- `timestamp`: instante da resposta;
- `status`: código HTTP;
- `error`: descrição HTTP;
- `code`: código estável para consumo pelo cliente;
- `message`: mensagem legível;
- `path`: endpoint que originou o erro;
- `fieldErrors`: mapa opcional para erros de validação.

Exemplo:

    {
      "timestamp": "2026-09-27T12:00:00Z",
      "status": 400,
      "error": "Bad Request",
      "code": "VALIDATION_ERROR",
      "message": "Um ou mais campos são inválidos.",
      "path": "/api/auth/register",
      "fieldErrors": {
        "email": "E-mail inválido."
      }
    }

## Política HTTP

| Código | Uso |
|---|---|
| 200 | Operação concluída com corpo |
| 201 | Recurso criado |
| 204 | Operação concluída sem corpo |
| 400 | Requisição malformada ou dados estruturalmente inválidos |
| 401 | Autenticação ausente ou inválida |
| 403 | Usuário autenticado sem permissão |
| 404 | Recurso não encontrado |
| 409 | Conflito ou regra que impede a operação no estado atual |
| 422 | Reservado para erro semântico quando houver necessidade de distingui-lo de 400 |
| 500 | Erro interno inesperado |
| 503 | Dependência externa temporariamente indisponível |

## Mapeamento atual

- `ResponseStatusException` → preserva o status explicitamente informado.
- `BadCredentialsException` → 401.
- `EntityNotFoundException` → 404.
- `IllegalArgumentException` → 400.
- `IllegalStateException` → 409.
- Erros Bean Validation → 400 + `fieldErrors`.
- Requisições malformadas → 400.
- `RestClientException` → 503.
- Exceções não previstas → 500, sem detalhes internos no corpo.

A conversão de `IllegalStateException` para 409 é uma etapa de compatibilidade com o código atual. Conforme as regras de negócio forem recebendo exceções específicas, o mapeamento poderá ficar ainda mais preciso sem alterar o contrato público.

## Segurança

O JSON de erro não deve expor stack trace, SQL, tokens, credenciais, segredos ou detalhes internos de provedores externos.

O webhook do Mercado Pago já possui uma validação própria e continua podendo responder diretamente com 401 quando a assinatura ou o tipo recebido não é aceito.

## Testes

O contrato possui testes automatizados para 400, 404 e 409 e valida os campos principais da resposta. Novos endpoints devem adicionar testes de integração quando uma nova regra de erro for introduzida.

A CI existente executa `./mvnw -B clean verify`, portanto falhas de compilação ou testes impedem o avanço da alteração.
