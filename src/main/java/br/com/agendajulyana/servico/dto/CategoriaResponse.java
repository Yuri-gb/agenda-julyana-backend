package br.com.agendajulyana.servico.dto;

import java.util.UUID;

public record CategoriaResponse(
    UUID id,
    String nome,
    String descricao,
    boolean ativo
) {}
