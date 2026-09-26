# V6__regra_pagamento

**Projeto:** Sistema de Agendamento — Julyana Lima  
**Versão:** V6  
**Status:** Regra de negócio definida  
**Data:** 26/09/2026

## 1. Objetivo

Definir as modalidades de pagamento disponíveis ao cliente antes do checkout e estabelecer como o sistema calcula a cobrança, confirma o agendamento e trata cancelamentos e reembolsos.

## 2. Modalidades

### ENTRADA
O cliente paga **50% do valor do serviço** no checkout. Os 50% restantes são pagos presencialmente.

### PAGAMENTO_TOTAL
O cliente paga **100% do valor do serviço** no checkout. Não existe valor restante a pagar presencialmente.

A modalidade é escolhida pelo cliente antes de iniciar o checkout.

## 3. Responsabilidades

O backend é responsável por:

- receber a modalidade escolhida;
- validar a modalidade;
- determinar o valor da cobrança;
- registrar a modalidade e o valor efetivamente cobrado;
- controlar a reserva temporária;
- aplicar o resultado do pagamento às regras do agendamento;
- determinar as regras de reembolso.

O Mercado Pago é responsável pelo processamento do pagamento e pelo retorno do resultado da transação. A escolha da modalidade e as regras de negócio permanecem sob responsabilidade do sistema.

## 4. Valores

Para um serviço de R$ 400,00:

| Modalidade | Cobrança no checkout | Restante presencial |
|---|---:|---:|
| ENTRADA | R$ 200,00 | R$ 200,00 |
| PAGAMENTO_TOTAL | R$ 400,00 | R$ 0,00 |

O valor cobrado deve ser calculado pelo backend a partir do valor histórico do serviço armazenado no agendamento.

## 5. Modalidade e status

Modalidade e status representam conceitos diferentes.

**Modalidade:**

- ENTRADA
- PAGAMENTO_TOTAL

**Status do pagamento:**

- PENDENTE
- APROVADO
- RECUSADO
- CANCELADO

A modalidade não deve ser inferida a partir do status.

## 6. Reserva temporária

A reserva temporária permanece válida por **30 minutos**, independentemente da modalidade escolhida.

A seleção de data e horário não confirma o agendamento.

O agendamento somente passa a **CONFIRMADO** quando o pagamento correspondente for aprovado dentro das regras da reserva.

## 7. Pagamento aprovado após expiração

Se a reserva expirar antes da confirmação do pagamento, um pagamento aprovado posteriormente **não revive o agendamento**.

Nesse cenário, o valor pago deve seguir o fluxo de reembolso aplicável.

## 8. Cancelamento pelo cliente

A regra original da entrada permanece:

- o cancelamento pelo cliente não devolve a entrada;
- quando houver direito a reembolso conforme regras futuras específicas, o limite correspondente à entrada é de **50% do valor do serviço**;
- se o cliente tiver escolhido PAGAMENTO_TOTAL, o fato de ter pago 100% não altera essa regra: o limite de reembolso por cancelamento do cliente continua vinculado à parcela de entrada.

O tratamento de prazo e eventual elegibilidade deve permanecer como regra de negócio do sistema, e não ser decidido pelo provedor de pagamento.

## 9. Cancelamento pela profissional/atendente

Quando o cancelamento ocorrer por responsabilidade da profissional/atendente, deve ser devolvido **100% do valor efetivamente pago**.

Exemplo para serviço de R$ 400,00:

- ENTRADA: pagamento de R$ 200,00 → reembolso de R$ 200,00;
- PAGAMENTO_TOTAL: pagamento de R$ 400,00 → reembolso de R$ 400,00.

A modalidade não reduz o reembolso quando o cancelamento é de responsabilidade da profissional/atendente.

## 10. Reembolso

O reembolso deve preservar:

- pagamento de origem;
- valor reembolsado;
- motivo;
- cancelamento relacionado, quando aplicável;
- status do reembolso;
- referência externa do provedor;
- datas de solicitação e processamento.

Estados definidos:

- SOLICITADO
- PROCESSANDO
- CONCLUIDO
- FALHOU

## 11. Histórico financeiro

O histórico deve preservar, no mínimo:

- modalidade escolhida;
- valor histórico do serviço;
- valor efetivamente cobrado;
- status do pagamento;
- tentativas de pagamento;
- referência externa do provedor;
- cancelamento;
- valor e status de eventual reembolso.

## 12. Fluxo consolidado

```text
Serviço
   ↓
Data
   ↓
Horário
   ↓
Modalidade de pagamento
   ├── ENTRADA → 50%
   └── PAGAMENTO_TOTAL → 100%
   ↓
Backend calcula a cobrança
   ↓
Reserva temporária de 30 minutos
   ↓
Mercado Pago Checkout Pro
   ↓
Resultado do pagamento
   ├── APROVADO → confirma agendamento
   ├── RECUSADO → permanece não confirmado
   └── pagamento aprovado após expiração → não revive + reembolso
```

## 13. Invariantes

1. A modalidade deve ser informada antes do checkout.
2. O cliente não informa livremente o valor a ser cobrado.
3. O backend determina o valor com base no serviço e na modalidade.
4. ENTRADA cobra 50% do valor histórico do serviço.
5. PAGAMENTO_TOTAL cobra 100% do valor histórico do serviço.
6. A reserva dura 30 minutos para ambas as modalidades.
7. Pagamento aprovado é condição para confirmação do agendamento.
8. Pagamento aprovado depois da expiração não revive o agendamento.
9. Cancelamento pelo cliente não devolve a entrada conforme a regra definida.
10. Cancelamento pela profissional/atendente devolve 100% do valor efetivamente pago.
11. Modalidade e status são conceitos independentes.
12. A regra de negócio não pode depender de decisões tomadas pelo Mercado Pago.

## 14. Impacto técnico

Esta regra exige alinhamento entre:

- modelo de domínio;
- modelo lógico;
- banco de dados e migrations;
- módulo de agendamento;
- módulo de pagamento;
- API;
- integração com Mercado Pago Checkout Pro;
- tratamento de webhook;
- cancelamentos;
- reembolsos;
- testes automatizados;
- CI.

Nenhuma camada deve criar uma regra própria que contradiga este documento.

## 15. Decisão consolidada

O sistema passa a suportar duas modalidades de pagamento selecionadas antes do checkout: **ENTRADA**, com cobrança de 50%, e **PAGAMENTO_TOTAL**, com cobrança de 100%.

O backend determina o valor e aplica as regras de negócio. O Mercado Pago atua como provedor de processamento do pagamento.

A modalidade, o valor efetivamente cobrado, o status e os dados de reembolso devem permanecer registrados para preservar o histórico financeiro.
