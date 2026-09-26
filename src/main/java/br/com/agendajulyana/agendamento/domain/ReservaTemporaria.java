package br.com.agendajulyana.agendamento.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="reserva_temporaria")
public class ReservaTemporaria {
 @Id @GeneratedValue private UUID id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="agendamento_id",nullable=false,unique=true) private Agendamento agendamento;
 @Column(nullable=false) private OffsetDateTime inicio;
 @Column(name="expira_em",nullable=false) private OffsetDateTime expiraEm;
 @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.NAMED_ENUM) @Column(nullable=false,columnDefinition="reserva_status") private ReservaStatus status;
 @Column(name="criado_em",nullable=false) private OffsetDateTime criadoEm;
 protected ReservaTemporaria(){}
 public ReservaTemporaria(Agendamento a,OffsetDateTime agora){agendamento=a;inicio=agora;expiraEm=agora.plusMinutes(30);status=ReservaStatus.ATIVA;criadoEm=agora;}
 public UUID getId(){return id;} public Agendamento getAgendamento(){return agendamento;} public OffsetDateTime getInicio(){return inicio;} public OffsetDateTime getExpiraEm(){return expiraEm;} public ReservaStatus getStatus(){return status;}
 public boolean estaExpirada(OffsetDateTime agora){return !agora.isBefore(expiraEm);}
 public void expirar(){status=ReservaStatus.EXPIRADA;}
}
