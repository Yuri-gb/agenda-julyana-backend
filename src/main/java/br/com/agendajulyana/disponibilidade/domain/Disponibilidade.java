package br.com.agendajulyana.disponibilidade.domain;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "disponibilidade")
public class Disponibilidade {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dia_semana", nullable = false)
    private Short diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Disponibilidade() {}

    public Disponibilidade(Short diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public void atualizar(Short diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public void alterarAtivo(boolean ativo) { this.ativo = ativo; }
    public UUID getId() { return id; }
    public Short getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public boolean isAtivo() { return ativo; }
}