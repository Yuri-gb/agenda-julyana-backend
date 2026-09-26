package br.com.agendajulyana.disponibilidade.repository;

import br.com.agendajulyana.disponibilidade.domain.Disponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, UUID> {
    List<Disponibilidade> findByDiaSemanaAndAtivoTrue(Short diaSemana);

    @Query("""
        select count(d) > 0 from Disponibilidade d
        where d.diaSemana = :diaSemana and d.ativo = true
        and d.horaInicio < :horaFim and d.horaFim > :horaInicio
        and (:id is null or d.id <> :id)
    """)
    boolean existeSobreposicao(@Param("diaSemana") Short diaSemana, @Param("horaInicio") LocalTime horaInicio, @Param("horaFim") LocalTime horaFim, @Param("id") UUID id);
}
