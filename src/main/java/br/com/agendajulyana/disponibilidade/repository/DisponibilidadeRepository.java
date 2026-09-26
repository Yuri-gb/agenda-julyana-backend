package br.com.agendajulyana.disponibilidade.repository;

import br.com.agendajulyana.disponibilidade.domain.Disponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, UUID> {
    List<Disponibilidade> findByDiaSemanaAndAtivoTrue(Short diaSemana);
}
