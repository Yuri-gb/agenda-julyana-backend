package br.com.agendajulyana.agendamento.repository;

import br.com.agendajulyana.agendamento.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface AgendamentoRepository extends JpaRepository<Agendamento,UUID>{
 @Query("""select count(a)>0 from Agendamento a where a.inicio < :fim and a.fim > :inicio and a.status in :status""")
 boolean existeConflito(@Param("inicio") OffsetDateTime inicio,@Param("fim") OffsetDateTime fim,@Param("status") java.util.Collection<AgendamentoStatus> status);
}
