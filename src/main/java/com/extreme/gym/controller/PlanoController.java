package com.extreme.gym.controller;

import com.extreme.gym.dto.plano.PlanoRequestDTO;
import com.extreme.gym.dto.plano.PlanoResponseDTO;
import com.extreme.gym.service.PlanoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService planoService;

    @PostMapping
    public ResponseEntity<PlanoResponseDTO> cadastrar(@Valid @RequestBody PlanoRequestDTO request) {
        PlanoResponseDTO plano = planoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(plano.id())
                .toUri();

        return ResponseEntity.created(location).body(plano);
    }

    @GetMapping
    public ResponseEntity<List<PlanoResponseDTO>> listar() {
        return ResponseEntity.ok(planoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(planoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PlanoRequestDTO request
    ) {
        return ResponseEntity.ok(planoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        planoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
