package br.com.agendajulyana.disponibilidade.repository;

import br.com.agendajulyana.disponibilidade.domain.Bloqueio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BloqueioRepository extends JpaRepository<Bloqueio, UUID> {}