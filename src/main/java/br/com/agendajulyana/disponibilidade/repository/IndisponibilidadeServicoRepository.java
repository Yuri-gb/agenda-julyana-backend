package br.com.agendajulyana.disponibilidade.repository;

import br.com.agendajulyana.disponibilidade.domain.IndisponibilidadeServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface IndisponibilidadeServicoRepository extends JpaRepository<IndisponibilidadeServico, UUID> {
    @Query("""
        select count(i) > 0 from IndisponibilidadeServico i
        where i.servico.id = :servicoId
        and i.inicio < :fim and i.fim > :inicio
        and (:id is null or i.id <> :id)
    """)
    boolean existeSobreposicao(@Param("servicoId") UUID servicoId, @Param("inicio") OffsetDateTime inicio, @Param("fim") OffsetDateTime fim, @Param("id") UUID id);
}
