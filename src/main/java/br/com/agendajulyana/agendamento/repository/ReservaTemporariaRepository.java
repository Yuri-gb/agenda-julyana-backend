package br.com.agendajulyana.agendamento.repository;
import br.com.agendajulyana.agendamento.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.OffsetDateTime; import java.util.Optional; import java.util.UUID;
public interface ReservaTemporariaRepository extends JpaRepository<ReservaTemporaria,UUID>{
 Optional<ReservaTemporaria> findByAgendamentoId(UUID agendamentoId);
 java.util.List<ReservaTemporaria> findByStatusAndExpiraEmBefore(ReservaStatus status,OffsetDateTime agora);
}