package br.com.agendajulyana.agendamento.repository;
import br.com.agendajulyana.agendamento.domain.Cancelamento; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CancelamentoRepository extends JpaRepository<Cancelamento,UUID>{ boolean existsByAgendamentoId(UUID agendamentoId); }