package br.com.agendajulyana.disponibilidade.repository;

import br.com.agendajulyana.disponibilidade.domain.IndisponibilidadeServico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IndisponibilidadeServicoRepository extends JpaRepository<IndisponibilidadeServico, UUID> {}