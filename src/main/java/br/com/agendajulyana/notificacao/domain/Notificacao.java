package br.com.agendajulyana.notificacao.domain;

import br.com.agendajulyana.agendamento.domain.Agendamento;
import br.com.agendajulyana.auth.domain.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="notificacao")
public class Notificacao {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="agendamento_id") private Agendamento agendamento;
    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable=false,columnDefinition="notificacao_tipo") private NotificacaoTipo tipo;
    @Column(nullable=false,length=200) private String titulo;
    @Column(nullable=false) private String mensagem;
    @Column(name="criada_em",nullable=false) private OffsetDateTime criadaEm;
    @Column(name="lida_em") private OffsetDateTime lidaEm;
    protected Notificacao(){}
    public Notificacao(Usuario usuario,Agendamento agendamento,NotificacaoTipo tipo,String titulo,String mensagem){
        this.usuario=usuario;this.agendamento=agendamento;this.tipo=tipo;this.titulo=titulo;this.mensagem=mensagem;this.criadaEm=OffsetDateTime.now();
    }
    public UUID getId(){return id;} public Usuario getUsuario(){return usuario;} public Agendamento getAgendamento(){return agendamento;}
    public NotificacaoTipo getTipo(){return tipo;} public String getTitulo(){return titulo;} public String getMensagem(){return mensagem;}
    public OffsetDateTime getCriadaEm(){return criadaEm;} public OffsetDateTime getLidaEm(){return lidaEm;}
    public void marcarComoLida(){lidaEm=OffsetDateTime.now();}
}
