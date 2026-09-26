package br.com.agendajulyana.disponibilidade.repository;

import br.com.agendajulyana.disponibilidade.domain.Bloqueio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface BloqueioRepository extends JpaRepository<Bloqueio, UUID> {
    @Query("""
        select count(b) > 0 from Bloqueio b
        where b.inicio < :fim and b.fim > :inicio
        and (:id is null or b.id <> :id)
    """)
    boolean existeSobreposicao(@Param("inicio") OffsetDateTime inicio, @Param("fim") OffsetDateTime fim, @Param("id") UUID id);
}
