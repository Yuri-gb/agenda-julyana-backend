package br.com.agendajulyana.agendamento.domain;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name="cancelamento")
public class Cancelamento { @Id @GeneratedValue private UUID id; @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="agendamento_id",nullable=false,unique=true) private Agendamento agendamento; @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM) @Column(nullable=false,columnDefinition="cancelamento_origem") private OrigemCancelamento origem; private String motivo; @Column(name="cancelado_em",nullable=false) private OffsetDateTime canceladoEm; @Column(name="cancelado_por") private UUID canceladoPor;
 public enum OrigemCancelamento { CLIENTE, ATENDENTE } protected Cancelamento(){} public Cancelamento(Agendamento a,OrigemCancelamento o,String m,UUID por){agendamento=a;origem=o;motivo=m;canceladoPor=por;canceladoEm=OffsetDateTime.now();}
}