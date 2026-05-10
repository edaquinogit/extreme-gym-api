package com.extreme.gym.controller;

import com.extreme.gym.dto.acesso.AcessoRequestDTO;
import com.extreme.gym.dto.acesso.AcessoResponseDTO;
import com.extreme.gym.service.AcessoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/acessos")
@RequiredArgsConstructor
public class AcessoController {

    private final AcessoService acessoService;

    @PostMapping("/validar")
    public ResponseEntity<AcessoResponseDTO> validar(@Valid @RequestBody AcessoRequestDTO request) {
        return ResponseEntity.ok(acessoService.validar(request));
    }
}
