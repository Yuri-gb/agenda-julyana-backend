package br.com.agendajulyana.agendamento.repository;

import br.com.agendajulyana.agendamento.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReservaTemporariaRepository extends JpaRepository<ReservaTemporaria,UUID>{}
