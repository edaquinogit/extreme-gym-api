package com.extreme.gym.controller;

import com.extreme.gym.dto.matricula.MatriculaRequestDTO;
import com.extreme.gym.dto.matricula.MatriculaResponseDTO;
import com.extreme.gym.service.MatriculaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @PostMapping
    public ResponseEntity<MatriculaResponseDTO> cadastrar(@Valid @RequestBody MatriculaRequestDTO request) {
        MatriculaResponseDTO matricula = matriculaService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(matricula.id())
                .toUri();

        return ResponseEntity.created(location).body(matricula);
    }

    @GetMapping
    public ResponseEntity<List<MatriculaResponseDTO>> listar() {
        return ResponseEntity.ok(matriculaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(matriculaService.buscarPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<MatriculaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(matriculaService.cancelar(id));
    }
}
