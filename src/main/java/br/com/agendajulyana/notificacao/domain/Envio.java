package br.com.agendajulyana.notificacao.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="envio")
public class Envio {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="notificacao_id",nullable=false) private Notificacao notificacao;
    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable=false,columnDefinition="envio_canal") private EnvioCanal canal;
    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable=false,columnDefinition="envio_status") private EnvioStatus status;
    @Column(name="enviado_em") private OffsetDateTime enviadoEm;
    @Column(nullable=false) private int tentativas;
    private String erro;
    @Column(name="criado_em",nullable=false) private OffsetDateTime criadoEm;
    protected Envio(){}
    public Envio(Notificacao notificacao,EnvioCanal canal){this.notificacao=notificacao;this.canal=canal;this.status=EnvioStatus.PENDENTE;this.criadoEm=OffsetDateTime.now();}
    public UUID getId(){return id;} public Notificacao getNotificacao(){return notificacao;} public EnvioCanal getCanal(){return canal;}
    public EnvioStatus getStatus(){return status;} public int getTentativas(){return tentativas;} public String getErro(){return erro;}
    public void marcarEnviado(){status=EnvioStatus.ENVIADO;enviadoEm=OffsetDateTime.now();tentativas++;}
    public void marcarFalha(String erro){status=EnvioStatus.FALHOU;this.erro=erro;tentativas++;}
}
