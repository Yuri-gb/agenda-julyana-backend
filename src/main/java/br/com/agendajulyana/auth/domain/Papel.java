package br.com.agendajulyana.auth.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "papel")
public class Papel {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
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
