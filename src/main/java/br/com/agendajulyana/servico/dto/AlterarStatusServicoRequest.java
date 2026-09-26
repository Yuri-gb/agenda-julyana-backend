package br.com.agendajulyana.servico.dto;

import br.com.agendajulyana.servico.domain.ServicoStatus;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusServicoRequest(@NotNull ServicoStatus status) {}
