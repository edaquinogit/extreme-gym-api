package com.extreme.gym.entity;

import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eventos_acesso")
public class EventoAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long alunoId;

    @Column(nullable = false)
    private Long dispositivoId;

    private Long matriculaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemEventoAcesso origem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModoEventoAcesso modo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoAcesso resultado;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime dataHoraEvento;

    @Column(nullable = false)
    private LocalDateTime dataHoraRecebimento;

    @Column(nullable = false)
    private Boolean sincronizado;

    private String identificadorExternoEvento;

    @Column(unique = true)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (dataHoraRecebimento == null) {
            dataHoraRecebimento = now;
        }
        if (dataHoraEvento == null) {
            dataHoraEvento = now;
        }
        if (sincronizado == null) {
            sincronizado = modo == ModoEventoAcesso.ONLINE;
        }
        if (criadoEm == null) {
            criadoEm = now;
        }
    }
}
