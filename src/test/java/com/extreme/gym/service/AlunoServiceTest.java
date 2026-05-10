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
import java.util.List;
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
    void deveListarAlunos() {
        Aluno ana = criarAluno(1L, "Ana Silva", "ana@email.com", "71999990000");
        Aluno bruno = criarAluno(2L, "Bruno Souza", "bruno@email.com", "71988880000");

        when(alunoRepository.findAll()).thenReturn(List.of(ana, bruno));

        List<AlunoResponseDTO> response = alunoService.listar();

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting(AlunoResponseDTO::email)
                .containsExactly("ana@email.com", "bruno@email.com");
    }

    @Test
    void deveBuscarAlunoPorId() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, "Ana Silva", "ana@email.com", "71999990000");

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        AlunoResponseDTO response = alunoService.buscarPorId(alunoId);

        assertThat(response.id()).isEqualTo(alunoId);
        assertThat(response.nome()).isEqualTo(aluno.getNome());
        assertThat(response.email()).isEqualTo(aluno.getEmail());
        assertThat(response.telefone()).isEqualTo(aluno.getTelefone());
        assertThat(response.status()).isEqualTo(StatusAluno.ATIVO);
        assertThat(response.dataCadastro()).isEqualTo(aluno.getDataCadastro());
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
    void deveAtualizarAlunoComSucesso() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, "Ana Silva", "ana@email.com", "71999990000");
        AlunoRequestDTO request = new AlunoRequestDTO(
                "Ana Oliveira",
                "ana@email.com",
                "71988887777"
        );

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.existsByEmailAndIdNot(request.email(), alunoId)).thenReturn(false);
        when(alunoRepository.save(aluno)).thenReturn(aluno);

        AlunoResponseDTO response = alunoService.atualizar(alunoId, request);

        assertThat(response.id()).isEqualTo(alunoId);
        assertThat(response.nome()).isEqualTo(request.nome());
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.telefone()).isEqualTo(request.telefone());
        verify(alunoRepository).save(aluno);
    }

    @Test
    void naoDeveAtualizarAlunoInexistente() {
        Long alunoId = 99L;
        AlunoRequestDTO request = new AlunoRequestDTO(
                "Ana Oliveira",
                "ana@email.com",
                "71988887777"
        );

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.atualizar(alunoId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aluno nao encontrado com id: 99");

        verify(alunoRepository, never()).existsByEmailAndIdNot(request.email(), alunoId);
        verify(alunoRepository, never()).save(any(Aluno.class));
    }

    @Test
    void naoDeveAtualizarAlunoUsandoEmailDeOutroAluno() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, "Ana Silva", "ana@email.com", "71999990000");
        AlunoRequestDTO request = new AlunoRequestDTO(
                "Ana Oliveira",
                "bruno@email.com",
                "71988887777"
        );

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.existsByEmailAndIdNot(request.email(), alunoId)).thenReturn(true);

        assertThatThrownBy(() -> alunoService.atualizar(alunoId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe aluno cadastrado com este email");

        verify(alunoRepository, never()).save(any(Aluno.class));
    }

    @Test
    void deveRemoverAlunoComSucesso() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, "Ana Silva", "ana@email.com", "71999990000");

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.remover(alunoId);

        verify(alunoRepository).delete(aluno);
    }

    @Test
    void naoDeveRemoverAlunoInexistente() {
        Long alunoId = 99L;

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.remover(alunoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aluno nao encontrado com id: 99");

        verify(alunoRepository, never()).delete(any(Aluno.class));
    }

    @Test
    void deveLancarErroAoBuscarAlunoInexistente() {
        Long alunoId = 99L;

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.buscarPorId(alunoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aluno nao encontrado com id: 99");
    }

    private Aluno criarAluno(Long id, String nome, String email, String telefone) {
        return Aluno.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .status(StatusAluno.ATIVO)
                .dataCadastro(LocalDateTime.now())
                .build();
    }
}
