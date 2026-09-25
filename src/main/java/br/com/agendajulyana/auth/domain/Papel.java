package br.com.agendajulyana.auth.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "papel")
public class Papel {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, unique = true, columnDefinition = "papel_nome")
    private PapelNome nome;

    protected Papel() {
    }

    public Papel(PapelNome nome) {
        this.nome = nome;
    }

    public UUID getId() {
        return id;
    }

    public PapelNome getNome() {
        return nome;
    }
}
