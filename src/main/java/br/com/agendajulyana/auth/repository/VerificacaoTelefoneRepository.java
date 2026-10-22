package br.com.agendajulyana.auth.repository;

import br.com.agendajulyana.auth.domain.VerificacaoTelefone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VerificacaoTelefoneRepository extends JpaRepository<VerificacaoTelefone, UUID> {
    Optional<VerificacaoTelefone> findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(UUID usuarioId);
    void deleteByUsuarioIdAndCriadoEmBefore(UUID usuarioId, OffsetDateTime antes);
}
