package com.extreme.gym.entity;

import com.extreme.gym.enums.ModoOperacaoDispositivo;
import com.extreme.gym.enums.StatusDispositivoAcesso;
import com.extreme.gym.enums.TipoDispositivoAcesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "dispositivos_acesso")
public class DispositivoAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDispositivoAcesso tipo;

    private String fabricante;

    private String modelo;

    @Column(unique = true)
    private String identificadorExterno;

    private String ipLocal;

    private String unidade;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDispositivoAcesso status = StatusDispositivoAcesso.ATIVO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModoOperacaoDispositivo modoOperacao = ModoOperacaoDispositivo.HIBRIDO;

    private LocalDateTime ultimaComunicacaoEm;

    @Column(name = "api_key_hash")
    private String apiKeyHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null) {
            status = StatusDispositivoAcesso.ATIVO;
        }
        if (modoOperacao == null) {
            modoOperacao = ModoOperacaoDispositivo.HIBRIDO;
        }
        if (criadoEm == null) {
            criadoEm = now;
        }
        if (atualizadoEm == null) {
            atualizadoEm = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
