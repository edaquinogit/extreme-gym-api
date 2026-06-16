package com.extreme.gym.service;

import com.extreme.gym.dto.evento.EventoAcessoLoteRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoResponseDTO;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.entity.EventoAcesso;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.repository.EventoAcessoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoAcessoService {

    private static final Logger log = LoggerFactory.getLogger(EventoAcessoService.class);

    private final EventoAcessoRepository eventoAcessoRepository;
    private final DispositivoAcessoService dispositivoAcessoService;
    private final AuditoriaAcessoService auditoriaAcessoService;
    private final Clock clock;

    public List<EventoAcessoResponseDTO> listar() {
        return eventoAcessoRepository.findAll().stream().map(evento -> toResponseDTO(evento, false)).toList();
    }

    public List<EventoAcessoResponseDTO> listarHoje() {
        LocalDate hoje = LocalDate.now(clock);
        return eventoAcessoRepository.findByDataHoraEventoBetweenOrderByDataHoraEventoDesc(
                hoje.atStartOfDay(),
                hoje.plusDays(1).atStartOfDay()
        ).stream().map(evento -> toResponseDTO(evento, false)).toList();
    }

    public List<EventoAcessoResponseDTO> listarPorAluno(Long alunoId) {
        return eventoAcessoRepository.findByAlunoIdOrderByDataHoraEventoDesc(alunoId)
                .stream().map(evento -> toResponseDTO(evento, false)).toList();
    }

    public List<EventoAcessoResponseDTO> listarPorDispositivo(Long dispositivoId) {
        return eventoAcessoRepository.findByDispositivoIdOrderByDataHoraEventoDesc(dispositivoId)
                .stream().map(evento -> toResponseDTO(evento, false)).toList();
    }

    @Transactional
    public EventoAcessoResponseDTO registrar(EventoAcessoRequestDTO request) {
        return registrar(request, false);
    }

    @Transactional
    public List<EventoAcessoResponseDTO> sincronizarLote(EventoAcessoLoteRequestDTO request) {
        return request.eventos().stream().map(evento -> registrar(evento, true)).toList();
    }

    @Transactional
    public EventoAcessoResponseDTO registrar(EventoAcessoRequestDTO request, boolean loteOffline) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var existente = eventoAcessoRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existente.isPresent()) {
                log.info("Evento de acesso duplicado ignorado por idempotencyKey");
                return toResponseDTO(existente.get(), true);
            }
        }

        DispositivoAcesso dispositivo = dispositivoAcessoService.buscarEntidadePorId(request.dispositivoId());
        if (dispositivo.getId() == null) {
            throw new BusinessException("Dispositivo de acesso invalido");
        }

        EventoAcesso evento = EventoAcesso.builder()
                .alunoId(request.alunoId())
                .dispositivoId(request.dispositivoId())
                .matriculaId(request.matriculaId())
                .origem(request.origem())
                .modo(request.modo())
                .resultado(request.resultado())
                .motivo(request.motivo())
                .dataHoraEvento(request.dataHoraEvento())
                .sincronizado(request.sincronizado())
                .identificadorExternoEvento(request.identificadorExternoEvento())
                .idempotencyKey(request.idempotencyKey())
                .build();

        EventoAcesso salvo = eventoAcessoRepository.save(evento);
        auditoriaAcessoService.registrar(loteOffline ? "SINCRONIZAR_EVENTO_ACESSO" : "REGISTRAR_EVENTO_ACESSO",
                "EventoAcesso", salvo.getId(), salvo.getDispositivoId(), request.origem().name(),
                "Evento de acesso " + salvo.getResultado());
        log.info("Evento de acesso registrado: id={}, dispositivoId={}, resultado={}",
                salvo.getId(), salvo.getDispositivoId(), salvo.getResultado());
        return toResponseDTO(salvo, false);
    }

    public EventoAcessoResponseDTO toResponseDTO(EventoAcesso evento, boolean duplicado) {
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
                evento.getSincronizado(),
                evento.getIdentificadorExternoEvento(),
                evento.getIdempotencyKey(),
                evento.getCriadoEm(),
                duplicado
        );
    }
}
