package br.com.agendajulyana.servico.repository;

import br.com.agendajulyana.servico.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);
}
