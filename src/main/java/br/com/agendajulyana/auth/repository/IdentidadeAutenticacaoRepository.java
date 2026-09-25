package br.com.agendajulyana.auth.repository;

import br.com.agendajulyana.auth.domain.IdentidadeAutenticacao;
import br.com.agendajulyana.auth.domain.ProvedorAutenticacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface IdentidadeAutenticacaoRepository extends JpaRepository<IdentidadeAutenticacao, UUID> {
    Optional<IdentidadeAutenticacao> findByProvedorAndIdentificadorExterno(
        ProvedorAutenticacao provedor, String identificadorExterno);
}
