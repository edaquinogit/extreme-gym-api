package com.extreme.gym.controller;

import com.extreme.gym.dto.buscaglobal.BuscaGlobalResponseDTO;
import com.extreme.gym.service.BuscaGlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/busca-global")
@RequiredArgsConstructor
public class BuscaGlobalController {

    private final BuscaGlobalService buscaGlobalService;

    @GetMapping
    public ResponseEntity<BuscaGlobalResponseDTO> buscar(
            @RequestParam String termo,
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication
    ) {
        return ResponseEntity.ok(buscaGlobalService.buscar(termo, limit, authentication));
    }
}
