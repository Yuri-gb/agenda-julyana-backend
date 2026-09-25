package br.com.agendajulyana.auth.repository;

import br.com.agendajulyana.auth.domain.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {}
