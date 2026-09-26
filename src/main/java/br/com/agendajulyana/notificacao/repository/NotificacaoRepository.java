package br.com.agendajulyana.notificacao.repository;
import br.com.agendajulyana.notificacao.domain.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface NotificacaoRepository extends JpaRepository<Notificacao,UUID> {}
