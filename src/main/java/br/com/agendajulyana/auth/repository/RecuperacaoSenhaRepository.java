package br.com.agendajulyana.auth.repository;

import br.com.agendajulyana.auth.domain.RecuperacaoSenha;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, UUID> {
    Optional<RecuperacaoSenha> findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(UUID usuarioId);

    Optional<RecuperacaoSenha> findByTokenRedefinicaoHashAndUtilizadoEmIsNull(String tokenRedefinicaoHash);

    void deleteByUsuarioIdAndCriadoEmBefore(UUID usuarioId, OffsetDateTime antes);
}
