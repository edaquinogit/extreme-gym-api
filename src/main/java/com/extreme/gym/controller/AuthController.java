package com.extreme.gym.controller;

import com.extreme.gym.dto.auth.LoginRequest;
import com.extreme.gym.dto.auth.LoginResponse;
import com.extreme.gym.dto.auth.RegisterRequest;
import com.extreme.gym.security.LoginAttemptService;
import com.extreme.gym.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Login e registro de usuarios")
public class AuthController {

    private final AuthService authService;
    private final LoginAttemptService loginAttemptService;

    @Operation(
            summary = "Autenticar usuario",
            description = "Valida credenciais e retorna JWT Bearer para chamadas autenticadas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais invalidas",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                            name = "login",
                            value = """
                                    {
                                      "username": "usuario@exemplo.com",
                                      "password": "sua-senha"
                                    }
                                    """
                    )
            )
    )
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        String remoteAddress = servletRequest.getRemoteAddr();
        loginAttemptService.assertAllowed(request.login(), remoteAddress);

        try {
            LoginResponse response = authService.login(request);
            loginAttemptService.recordSuccess(request.login(), remoteAddress);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException exception) {
            loginAttemptService.recordFailure(request.login(), remoteAddress);
            throw exception;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}
