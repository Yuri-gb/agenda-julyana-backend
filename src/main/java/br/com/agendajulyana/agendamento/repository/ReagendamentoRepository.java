package br.com.agendajulyana.agendamento.repository;
import br.com.agendajulyana.agendamento.domain.Reagendamento; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ReagendamentoRepository extends JpaRepository<Reagendamento,UUID>{ long countByAgendamentoId(UUID agendamentoId); }