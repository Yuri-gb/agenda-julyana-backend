package br.com.agendajulyana.auditoria.repository;

import br.com.agendajulyana.auditoria.domain.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {}