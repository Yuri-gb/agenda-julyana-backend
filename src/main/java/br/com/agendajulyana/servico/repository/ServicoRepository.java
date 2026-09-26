package br.com.agendajulyana.servico.repository;

import br.com.agendajulyana.servico.domain.Servico;
import br.com.agendajulyana.servico.domain.ServicoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);
    long countByStatus(ServicoStatus status);
}
