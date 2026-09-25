package br.com.agendajulyana.auth.repository;

import br.com.agendajulyana.auth.domain.Papel;
import br.com.agendajulyana.auth.domain.PapelNome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PapelRepository extends JpaRepository<Papel, UUID> {
    Optional<Papel> findByNome(PapelNome nome);
}
