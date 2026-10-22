package br.com.agendajulyana.agendamento.repository;

import br.com.agendajulyana.agendamento.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface AgendamentoRepository extends JpaRepository<Agendamento,UUID>{
 @Query("""
    select count(a) > 0
    from Agendamento a
    where a.inicio < :fim
      and a.fim > :inicio
      and a.status in :status
      and (
        a.status <> br.com.agendajulyana.agendamento.domain.AgendamentoStatus.AGUARDANDO_PAGAMENTO
        or exists (
          select r.id
          from ReservaTemporaria r
          where r.agendamento.id = a.id
            and r.status = br.com.agendajulyana.agendamento.domain.ReservaStatus.ATIVA
            and r.expiraEm > :agora
        )
      )
    """)
 boolean existeConflito(@Param("inicio") OffsetDateTime inicio,
                        @Param("fim") OffsetDateTime fim,
                        @Param("status") java.util.Collection<AgendamentoStatus> status,
                        @Param("agora") OffsetDateTime agora);
}
