package br.com.agendajulyana.agendamento.dto;
import jakarta.validation.constraints.NotNull; import java.time.OffsetDateTime;
public record ReagendarAgendamentoRequest(@NotNull OffsetDateTime inicio,String motivo){}