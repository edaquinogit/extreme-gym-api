package com.extreme.gym.controller;

import com.extreme.gym.dto.evento.EventoAcessoLoteRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoResponseDTO;
import com.extreme.gym.service.EventoAcessoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/eventos-acesso")
@RequiredArgsConstructor
public class EventoAcessoController {

    private final EventoAcessoService eventoAcessoService;

    @GetMapping
    public ResponseEntity<List<EventoAcessoResponseDTO>> listar() {
        return ResponseEntity.ok(eventoAcessoService.listar());
    }

    @GetMapping("/hoje")
    public ResponseEntity<List<EventoAcessoResponseDTO>> listarHoje() {
        return ResponseEntity.ok(eventoAcessoService.listarHoje());
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<EventoAcessoResponseDTO>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(eventoAcessoService.listarPorAluno(alunoId));
    }

    @GetMapping("/dispositivo/{dispositivoId}")
    public ResponseEntity<List<EventoAcessoResponseDTO>> listarPorDispositivo(@PathVariable Long dispositivoId) {
        return ResponseEntity.ok(eventoAcessoService.listarPorDispositivo(dispositivoId));
    }

    @PostMapping
    public ResponseEntity<EventoAcessoResponseDTO> registrar(@Valid @RequestBody EventoAcessoRequestDTO request) {
        EventoAcessoResponseDTO evento = eventoAcessoService.registrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(evento.id())
                .toUri();
        return ResponseEntity.created(location).body(evento);
    }

    @PostMapping("/sincronizar-lote")
    public ResponseEntity<List<EventoAcessoResponseDTO>> sincronizarLote(
            @Valid @RequestBody EventoAcessoLoteRequestDTO request
    ) {
        return ResponseEntity.ok(eventoAcessoService.sincronizarLote(request));
    }
}
