package br.com.agendajulyana.auth.repository;

import br.com.agendajulyana.auth.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByUsuarioId(UUID usuarioId);
}
