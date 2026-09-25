package br.com.agendajulyana.auth.repository;
import br.com.agendajulyana.auth.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface IdentidadeAutenticacaoRepository extends JpaRepository<IdentidadeAutenticacao,UUID>{Optional<IdentidadeAutenticacao> findByProvedorAndIdentificadorExterno(ProvedorAutenticacao p,String id);}
