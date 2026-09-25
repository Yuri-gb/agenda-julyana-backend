package br.com.agendajulyana.auth.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "auditoria")
public class Auditoria {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id") private Usuario usuario;
    @Column(nullable = false, length = 100) private String acao;
    @Column(name = "recurso_tipo", nullable = false, length = 100) private String recursoTipo;
    @Column(name = "recurso_id", nullable = false) private UUID recursoId;
    @Column(nullable = false, length = 30) private String resultado;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private Map<String,Object> contexto;
    @Column(name = "criado_em", nullable = false) private OffsetDateTime criadoEm;
    protected Auditoria() {}
    public Auditoria(Usuario usuario,String acao,String recursoTipo,UUID recursoId,String resultado,Map<String,Object> contexto){
        this.usuario=usuario;this.acao=acao;this.recursoTipo=recursoTipo;this.recursoId=recursoId;this.resultado=resultado;this.contexto=contexto;this.criadoEm=OffsetDateTime.now();
    }
    public UUID getId(){return id;}
}