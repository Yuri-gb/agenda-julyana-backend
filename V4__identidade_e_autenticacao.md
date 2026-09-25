# V4__identidade_e_autenticacao

**Projeto:** Sistema de Agendamento — Julyana Lima  
**Versão:** V4  
**Status:** Evolução da identidade e autenticação

## Recuperação de senha por e-mail

A recuperação de senha por e-mail utiliza código temporário e não envia senhas por e-mail.

### Fluxo

1. Usuário informa o e-mail.
2. O sistema responde de forma genérica, sem revelar se o e-mail existe.
3. Quando a conta possui autenticação local, o sistema gera um código de 6 dígitos.
4. O código é enviado por e-mail.
5. O código expira após 15 minutos.
6. O código possui limite de 5 tentativas.
7. Após validar o código, o sistema emite um token temporário de redefinição.
8. O token de redefinição expira após 10 minutos e pode ser usado uma única vez.
9. O usuário informa e confirma a nova senha.
10. A nova senha é armazenada somente como hash.
11. Após a utilização, o token não pode ser reutilizado.

### Contas Google

Contas que possuem somente identidade GOOGLE não possuem senha local para recuperação.

Nessas contas, a autenticação continua sendo realizada pelo Google.

### Segurança

- Não revelar se um e-mail possui conta.
- Não enviar senha em texto.
- Armazenar somente hash do código e da senha.
- Limitar tentativas de validação.
- Invalidar o fluxo após utilização.
- Manter o código e o token com validade limitada.

### Fora do escopo desta etapa

O envio por WhatsApp continua previsto no produto, mas não faz parte desta implementação.
