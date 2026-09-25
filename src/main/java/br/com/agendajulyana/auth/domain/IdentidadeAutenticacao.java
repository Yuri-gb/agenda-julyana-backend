package br.com.agendajulyana.auth.domain;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name="identidade_autenticacao", uniqueConstraints={@UniqueConstraint(name="uq_identidade_provedor_externo",columnNames={"provedor","identificador_externo"}),@UniqueConstraint(name="uq_identidade_usuario_provedor",columnNames={"usuario_id","provedor"})})
public class IdentidadeAutenticacao {
 @Id @GeneratedValue private UUID id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ProvedorAutenticacao provedor; @Column(name="identificador_externo",nullable=false,length=255) private String identificadorExterno; @Column(name="senha_hash",length=255) private String senhaHash;
 @Column(name="criado_em",nullable=false) private OffsetDateTime criadoEm; @Column(name="atualizado_em",nullable=false) private OffsetDateTime atualizadoEm; protected IdentidadeAutenticacao(){}
 private IdentidadeAutenticacao(Usuario u,ProvedorAutenticacao p,String i,String s){usuario=u;provedor=p;identificadorExterno=i;senhaHash=s;criadoEm=OffsetDateTime.now();atualizadoEm=OffsetDateTime.now();}
 public static IdentidadeAutenticacao local(Usuario u,String e,String s){return new IdentidadeAutenticacao(u,ProvedorAutenticacao.LOCAL,e.trim().toLowerCase(),s);} public static IdentidadeAutenticacao google(Usuario u,String s){return new IdentidadeAutenticacao(u,ProvedorAutenticacao.GOOGLE,s,null);}
 public UUID getId(){return id;} public Usuario getUsuario(){return usuario;} public ProvedorAutenticacao getProvedor(){return provedor;} public String getIdentificadorExterno(){return identificadorExterno;} public String getSenhaHash(){return senhaHash;}
}
