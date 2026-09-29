package br.com.agendajulyana.pagamento.repository;

import br.com.agendajulyana.pagamento.domain.Reembolso;
import br.com.agendajulyana.pagamento.domain.ReembolsoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReembolsoRepository extends JpaRepository<Reembolso, UUID> {
    boolean existsByPagamentoIdAndStatusIn(UUID pagamentoId, List<ReembolsoStatus> status);
    Optional<Reembolso> findFirstByPagamentoIdAndStatusInOrderBySolicitadoEmDesc(UUID pagamentoId, List<ReembolsoStatus> status);
}