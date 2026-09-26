package br.com.agendajulyana.agendamento.domain;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name="reagendamento",uniqueConstraints=@UniqueConstraint(name="uq_reagendamento_numero",columnNames={"agendamento_id","numero"}))
public class Reagendamento { @Id @GeneratedValue private UUID id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="agendamento_id",nullable=false) private Agendamento agendamento; @Column(nullable=false) private short numero; @Column(name="inicio_anterior",nullable=false) private OffsetDateTime inicioAnterior; @Column(name="inicio_novo",nullable=false) private OffsetDateTime inicioNovo; private String motivo; @Column(name="realizado_em",nullable=false) private OffsetDateTime realizadoEm;
 protected Reagendamento(){} public Reagendamento(Agendamento a,short n,OffsetDateTime antigo,OffsetDateTime novo,String motivo){this.agendamento=a;numero=n;inicioAnterior=antigo;inicioNovo=novo;this.motivo=motivo;realizadoEm=OffsetDateTime.now();}
 public short getNumero(){return numero;} public UUID getId(){return id;}
}