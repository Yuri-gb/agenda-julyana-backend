package br.com.agendajulyana.notificacao.repository;
import br.com.agendajulyana.notificacao.domain.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface EnvioRepository extends JpaRepository<Envio,UUID> {}
