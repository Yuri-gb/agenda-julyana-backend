package br.com.agendajulyana.servico.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record AtualizarServicoRequest(
    @NotBlank @Size(max = 150) String nome,
    @Size(max = 5000) String descricao,
    @NotNull @Positive Integer duracaoMinutos,
    @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal preco,
    @Size(max = 1000) String imagemUrl,
    Set<UUID> categoriaIds
) {}
