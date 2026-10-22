package br.com.agendajulyana.agendamento.repository;

import br.com.agendajulyana.agendamento.domain.Agendamento;
import br.com.agendajulyana.agendamento.domain.AgendamentoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM agendamento a
            WHERE a.inicio < :fim
              AND a.fim > :inicio
              AND CAST(a.status AS text) IN (:status)
              AND (
                  CAST(a.status AS text) <> 'AGUARDANDO_PAGAMENTO'
                  OR EXISTS (
                      SELECT 1
                      FROM reserva_temporaria r
                      WHERE r.agendamento_id = a.id
                        AND CAST(r.status AS text) = 'ATIVA'
                        AND r.expira_em > :agora
                  )
              )
        )
        """, nativeQuery = true)
    boolean existeConflito(@Param("inicio") OffsetDateTime inicio,
                           @Param("fim") OffsetDateTime fim,
                           @Param("status") Collection<String> status,
                           @Param("agora") OffsetDateTime agora);
}
