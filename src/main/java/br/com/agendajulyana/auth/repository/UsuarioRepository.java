package br.com.agendajulyana.auth.repository;

import br.com.agendajulyana.auth.domain.PapelNome;
import br.com.agendajulyana.auth.domain.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    @EntityGraph(attributePaths = "papeis")
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select count(u) > 0 from Usuario u join u.papeis p where p.nome = :papel")
    boolean existsByPapelNome(PapelNome papel);
}
