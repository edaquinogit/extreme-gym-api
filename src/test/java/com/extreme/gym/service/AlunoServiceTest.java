package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.aluno.AlunoRequestDTO;
import com.extreme.gym.dto.aluno.AlunoResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AlunoService alunoService;

    @Test
    void deveCadastrarAlunoComSucesso() {
        AlunoRequestDTO request = new AlunoRequestDTO(
                "Ana Silva",
                "ana@email.com",
                "71999990000"
        );
        LocalDateTime dataCadastro = LocalDateTime.now();

        when(alunoRepository.existsByEmail(request.email())).thenReturn(false);
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno aluno = invocation.getArgument(0);
            aluno.setId(1L);
            aluno.setDataCadastro(dataCadastro);
            return aluno;
        });

        AlunoResponseDTO response = alunoService.cadastrar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo(request.nome());
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.telefone()).isEqualTo(request.telefone());
        assertThat(response.status()).isEqualTo(StatusAluno.ATIVO);
        assertThat(response.dataCadastro()).isEqualTo(dataCadastro);
        verify(alunoRepository).save(any(Aluno.class));
    }

    @Test
    void naoDeveCadastrarAlunoComEmailDuplicado() {
        AlunoRequestDTO request = new AlunoRequestDTO(
                "Ana Silva",
                "ana@email.com",
                "71999990000"
        );

        when(alunoRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> alunoService.cadastrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe aluno cadastrado com este email");

        verify(alunoRepository, never()).save(any(Aluno.class));
    }

    @Test
    void deveLancarErroAoBuscarAlunoInexistente() {
        Long alunoId = 99L;

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.buscarPorId(alunoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aluno nao encontrado com id: 99");
    }
}
