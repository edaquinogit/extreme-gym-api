package com.extreme.gym.controller;

import com.extreme.gym.dto.pagamento.PagamentoRequestDTO;
import com.extreme.gym.dto.pagamento.PagamentoResponseDTO;
import com.extreme.gym.service.PagamentoService;
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
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> registrar(@Valid @RequestBody PagamentoRequestDTO request) {
        PagamentoResponseDTO pagamento = pagamentoService.registrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pagamento.id())
                .toUri();

        return ResponseEntity.created(location).body(pagamento);
    }

    @GetMapping
    public ResponseEntity<List<PagamentoResponseDTO>> listar() {
        return ResponseEntity.ok(pagamentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.buscarPorId(id));
    }

    @GetMapping("/matricula/{matriculaId}")
    public ResponseEntity<List<PagamentoResponseDTO>> listarPorMatricula(@PathVariable Long matriculaId) {
        return ResponseEntity.ok(pagamentoService.listarPorMatricula(matriculaId));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PagamentoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.cancelar(id));
    }
}
