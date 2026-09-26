package br.com.agendajulyana.pagamento.repository;

import br.com.agendajulyana.pagamento.domain.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    Optional<Pagamento> findByAgendamentoId(UUID agendamentoId);

    Optional<Pagamento> findByReferenciaExterna(String referenciaExterna);
}
