package br.com.agendajulyana.pagamento.repository;

import br.com.agendajulyana.pagamento.domain.Reembolso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReembolsoRepository extends JpaRepository<Reembolso, UUID> {}
