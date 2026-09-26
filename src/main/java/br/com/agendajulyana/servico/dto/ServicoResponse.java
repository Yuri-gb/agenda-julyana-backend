package br.com.agendajulyana.servico.dto;

import br.com.agendajulyana.servico.domain.ServicoStatus;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ServicoResponse(
    UUID id,
    String nome,
    String descricao,
    int duracaoMinutos,
    BigDecimal preco,
    String imagemUrl,
    ServicoStatus status,
    Set<CategoriaResumo> categorias
) {
    public record CategoriaResumo(UUID id, String nome) {}
}
