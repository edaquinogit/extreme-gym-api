package com.extreme.gym.service;

import com.extreme.gym.dto.evento.EventoAcessoLoteItemResponseDTO;
import com.extreme.gym.dto.evento.EventoAcessoLoteRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoLoteResponseDTO;
import com.extreme.gym.dto.evento.EventoAcessoRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoResponseDTO;
import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.entity.EventoAcesso;
import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import com.extreme.gym.enums.TipoCredencialAcesso;
import com.extreme.gym.repository.EventoAcessoRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventoAcessoService {

    private static final String STATUS_CRIADO = "CRIADO";
    private static final String STATUS_DUPLICADO = "DUPLICADO";

    private final EventoAcessoRepository eventoRepository;

    public EventoAcessoLoteResponseDTO sincronizarLote(
            DispositivoAcesso dispositivo,
            EventoAcessoLoteRequestDTO request
    ) {
        List<EventoAcessoLoteItemResponseDTO> detalhes = new ArrayList<>();
        int criados = 0;
        int duplicados = 0;

        for (EventoAcessoRequestDTO eventoRequest : request.eventos()) {
            EventoAcesso existente = eventoRepository.findByIdempotencyKey(eventoRequest.idempotencyKey()).orElse(null);
            if (existente != null) {
                duplicados++;
                detalhes.add(new EventoAcessoLoteItemResponseDTO(
                        eventoRequest.idempotencyKey(),
                        STATUS_DUPLICADO,
                        existente.getId(),
                        "Evento ja sincronizado"
                ));
                continue;
            }

            EventoAcesso evento = toEntity(dispositivo.getId(), eventoRequest);
            EventoAcesso salvo = eventoRepository.save(evento);
            criados++;
            detalhes.add(new EventoAcessoLoteItemResponseDTO(
                    eventoRequest.idempotencyKey(),
                    STATUS_CRIADO,
                    salvo.getId(),
                    "Evento sincronizado"
            ));
        }

        return new EventoAcessoLoteResponseDTO(
                request.eventos().size(),
                criados,
                duplicados,
                0,
                detalhes
        );
    }

    public EventoAcesso registrarValidacao(
            DispositivoAcesso dispositivo,
            String idempotencyKey,
            Long alunoId,
            Long matriculaId,
            OrigemEventoAcesso origem,
            TipoCredencialAcesso credencialTipo,
            String identificadorExterno,
            ResultadoAcesso resultado,
            String motivo,
            LocalDateTime dataHoraEvento
    ) {
        return eventoRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> eventoRepository.save(EventoAcesso.builder()
                        .alunoId(alunoId)
                        .dispositivoId(dispositivo.getId())
                        .matriculaId(matriculaId)
                        .origem(origem)
                        .modo(ModoEventoAcesso.ONLINE)
                        .resultado(resultado)
                        .motivo(motivo)
                        .dataHoraEvento(dataHoraEvento)
                        .idempotencyKey(idempotencyKey)
                        .credencialTipo(credencialTipo)
                        .identificadorExterno(identificadorExterno)
                        .sincronizado(true)
                        .build()));
    }

    public EventoAcesso registrarValidacao(
            DispositivoAcesso dispositivo,
            String idempotencyKey,
            CredencialAcesso credencial,
            AcessoService.ResultadoAcesso validacao,
            LocalDateTime dataHoraEvento
    ) {
        ResultadoAcesso resultado = validacao.acessoLiberado() ? ResultadoAcesso.LIBERADO : ResultadoAcesso.BLOQUEADO;
        Long matriculaId = validacao.matricula() != null ? validacao.matricula().getId() : null;
        return registrarValidacao(
                dispositivo,
                idempotencyKey,
                credencial.getAlunoId(),
                matriculaId,
                OrigemEventoAcesso.DISPOSITIVO,
                credencial.getTipo(),
                credencial.getIdentificadorExterno(),
                resultado,
                validacao.motivo(),
                dataHoraEvento
        );
    }

    @Transactional(readOnly = true)
    public List<EventoAcessoResponseDTO> listar() {
        return eventoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public EventoAcessoResponseDTO toResponseDTO(EventoAcesso evento) {
        return new EventoAcessoResponseDTO(
                evento.getId(),
                evento.getAlunoId(),
                evento.getDispositivoId(),
                evento.getMatriculaId(),
                evento.getOrigem(),
                evento.getModo(),
                evento.getResultado(),
                evento.getMotivo(),
                evento.getDataHoraEvento(),
                evento.getDataHoraRecebimento(),
                evento.getIdempotencyKey()
        );
    }

    private EventoAcesso toEntity(Long dispositivoId, EventoAcessoRequestDTO request) {
        return EventoAcesso.builder()
                .alunoId(request.alunoId())
                .dispositivoId(dispositivoId)
                .origem(request.origem())
                .modo(request.modo())
                .resultado(request.resultado())
                .motivo(request.motivo())
                .dataHoraEvento(request.dataHoraEvento())
                .identificadorExternoEvento(blankToNull(request.identificadorExternoEvento()))
                .idempotencyKey(request.idempotencyKey())
                .credencialTipo(request.credencialTipo())
                .identificadorExterno(blankToNull(request.identificadorExterno()))
                .sincronizado(true)
                .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
