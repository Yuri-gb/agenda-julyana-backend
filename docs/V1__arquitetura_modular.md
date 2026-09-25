# V1__arquitetura_modular

**Projeto:** Sistema de Agendamento — Julyana Lima  
**Versão:** V1  
**Status:** Definido  
**Data:** 25/09/2026

---

## 1. Objetivo

Definir a organização arquitetural do backend do Sistema de Agendamento — Julyana Lima.

O projeto adotará uma **arquitetura modular orientada aos contextos de negócio**.

A decisão busca manter alta coesão entre componentes relacionados ao mesmo contexto, reduzir a dispersão de funcionalidades e facilitar a evolução do sistema conforme novos módulos sejam adicionados.

---

## 2. Decisão arquitetural

A aplicação será organizada primeiro por **módulos de negócio** e, dentro de cada módulo, por responsabilidades técnicas.

Estrutura conceitual:

```text
br.com.agendajulyana/
├── auth/
├── servico/
├── agendamento/
├── disponibilidade/
├── pagamento/
├── notificacao/
├── cliente/
├── auditoria/
└── ...
```

Um módulo pode conter componentes como:

```text
servico/
├── controller/
├── domain/
├── dto/
├── repository/
└── service/
```

A existência de uma pasta interna depende da necessidade real do módulo. Não é obrigatório criar todas as camadas em todos os módulos.

---

## 3. Conceito de módulo

Um módulo representa um contexto funcional/coeso do sistema.

Exemplos:

- `auth`: autenticação, identidade e recuperação de acesso;
- `servico`: serviços oferecidos e categorias;
- `agendamento`: ciclo de vida dos agendamentos;
- `disponibilidade`: regras de disponibilidade e bloqueios;
- `pagamento`: pagamentos, tentativas e reembolsos;
- `notificacao`: notificações e seus envios;
- `cliente`: contexto específico do cliente;
- `auditoria`: registro de ações relevantes.

A divisão deve acompanhar o domínio do sistema e não simplesmente a quantidade de classes.

---

## 4. Organização interna

Dentro de cada módulo, as responsabilidades técnicas permanecem separadas.

### domain

Contém os conceitos do domínio representados pelo módulo, incluindo entidades e tipos relacionados.

Exemplo:

```text
servico/domain/
├── Servico.java
├── Categoria.java
└── ServicoStatus.java
```

### repository

Contém os componentes responsáveis pela persistência daquele módulo.

### service

Contém a lógica de aplicação/orquestração daquele módulo.

### controller

Contém os endpoints HTTP expostos pelo módulo.

### dto

Contém os contratos de entrada e saída da API daquele módulo.

---

## 5. Dependências entre módulos

Um módulo não deve acessar indiscriminadamente os detalhes internos de outro módulo.

Ao criar uma dependência entre módulos, deve-se avaliar:

1. se a dependência representa uma necessidade real do domínio;
2. se existe uma forma mais coesa de manter a regra no módulo de origem;
3. se a dependência cria acoplamento desnecessário;
4. se a regra crítica está sendo protegida no backend.

As integrações externas devem permanecer isoladas de forma que detalhes de provedores não se espalhem pelo domínio.

---

## 6. Relação com outras abordagens

A adoção de arquitetura modular **não significa que o projeto seja automaticamente DDD, Clean Architecture ou Hexagonal Architecture**.

Essas abordagens podem ser estudadas e aplicadas pontualmente quando houver uma necessidade arquitetural clara.

Nesta etapa, a decisão oficial do projeto é:

> **Arquitetura modular orientada aos contextos de negócio.**

---

## 7. Critérios para criação de novos módulos

Um novo módulo deve ser criado quando existir um contexto de negócio suficientemente coeso e que se beneficie de isolamento.

Não criar módulos apenas para:

- reduzir artificialmente o número de arquivos em uma pasta;
- seguir uma quantidade fixa de classes;
- reproduzir uma estrutura arquitetural sem necessidade.

A estrutura deve evoluir junto com o domínio.

---

## 8. Relação com o modelo de domínio

Os módulos devem ser derivados dos conceitos e responsabilidades já definidos no modelo de domínio e no modelo lógico do projeto.

A arquitetura de código não deve alterar as regras de negócio definidas nesses modelos.

Quando uma decisão arquitetural exigir mudança de domínio ou de dados, a documentação correspondente deverá ser atualizada antes da implementação.

---

## 9. Regra para novos desenvolvimentos

Antes de criar uma nova classe, o desenvolvimento deve identificar:

1. a qual módulo a funcionalidade pertence;
2. qual é sua responsabilidade;
3. quais outros módulos precisam ser acessados;
4. se a dependência é realmente necessária;
5. quais testes devem acompanhar a mudança.

Novas funcionalidades devem seguir a organização modular definida neste documento.

---

## 10. Evolução arquitetural

A arquitetura não é considerada imutável.

Mudanças estruturais relevantes deverão ser avaliadas antes da implementação e documentadas quando alterarem a organização ou os princípios arquiteturais do sistema.

A evolução deve preservar, sempre que possível:

- coesão dos módulos;
- baixo acoplamento;
- separação de responsabilidades;
- testabilidade;
- clareza do domínio;
- proteção das regras de negócio.

---

## 11. Decisão consolidada

O backend do Sistema de Agendamento — Julyana Lima utilizará **arquitetura modular orientada aos contextos de negócio**, com organização interna por responsabilidades técnicas.

Essa decisão passa a ser a referência para a organização dos próximos módulos do projeto.
