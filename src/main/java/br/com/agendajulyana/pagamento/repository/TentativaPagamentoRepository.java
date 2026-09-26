package br.com.agendajulyana.pagamento.repository;

import br.com.agendajulyana.pagamento.domain.TentativaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TentativaPagamentoRepository extends JpaRepository<TentativaPagamento, UUID> {}
