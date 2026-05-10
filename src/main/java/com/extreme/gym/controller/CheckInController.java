package com.extreme.gym.controller;

import com.extreme.gym.dto.checkin.CheckInRequestDTO;
import com.extreme.gym.dto.checkin.CheckInResponseDTO;
import com.extreme.gym.service.CheckInService;
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
@RequestMapping("/checkins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<CheckInResponseDTO> registrar(@Valid @RequestBody CheckInRequestDTO request) {
        CheckInResponseDTO checkIn = checkInService.registrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(checkIn.id())
                .toUri();

        return ResponseEntity.created(location).body(checkIn);
    }

    @GetMapping
    public ResponseEntity<List<CheckInResponseDTO>> listar() {
        return ResponseEntity.ok(checkInService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckInResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(checkInService.buscarPorId(id));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<CheckInResponseDTO>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(checkInService.listarPorAluno(alunoId));
    }
}
