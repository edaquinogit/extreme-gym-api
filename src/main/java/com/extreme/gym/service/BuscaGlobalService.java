package com.extreme.gym.service;

import com.extreme.gym.dto.buscaglobal.BuscaGlobalResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import com.extreme.gym.repository.PlanoRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscaGlobalService {

    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PlanoRepository planoRepository;

    @Value("${app.global-search.enabled:true}")
    private boolean buscaGlobalHabilitada;

    public BuscaGlobalResponseDTO buscar(String termo, int limit, Authentication authentication) {
        if (!buscaGlobalHabilitada || termo == null || termo.trim().length() < 2) {
            return vazio();
        }
        int limite = Math.max(1, Math.min(limit, 20));
        PageRequest page = PageRequest.of(0, limite);
        String termoNormalizado = termo.trim();
        boolean catraca = possuiRole(authentication, "ROLE_CATRACA");

        List<BuscaGlobalResponseDTO.Item> alunos = alunoRepository.buscarResumoGlobal(termoNormalizado, page)
                .stream().map(this::alunoItem).toList();
        List<BuscaGlobalResponseDTO.Item> matriculas = matriculaRepository.buscarResumoGlobal(termoNormalizado, page)
                .stream().map(this::matriculaItem).toList();
        List<BuscaGlobalResponseDTO.Item> planos = planoRepository.buscarResumoGlobal(termoNormalizado, page)
                .stream().map(this::planoItem).toList();
        List<BuscaGlobalResponseDTO.Item> pagamentos = catraca
                ? List.of()
                : pagamentoRepository.buscarResumoGlobal(termoNormalizado, page).stream().map(this::pagamentoItem).toList();

        return new BuscaGlobalResponseDTO(alunos, matriculas, pagamentos, planos);
    }

    private BuscaGlobalResponseDTO vazio() {
        return new BuscaGlobalResponseDTO(List.of(), List.of(), List.of(), List.of());
    }

    private boolean possuiRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private BuscaGlobalResponseDTO.Item alunoItem(Aluno aluno) {
        return new BuscaGlobalResponseDTO.Item(aluno.getId(), "ALUNO", aluno.getNome(),
                aluno.getStatus().name(), aluno.getStatus().name(), "/alunos/" + aluno.getId(),
                Map.of("alunoId", aluno.getId()));
    }

    private BuscaGlobalResponseDTO.Item matriculaItem(Matricula matricula) {
        return new BuscaGlobalResponseDTO.Item(matricula.getId(), "MATRICULA",
                "Matricula #" + matricula.getId(), matricula.getAluno().getNome(),
                matricula.getStatus().name(), "/matriculas/" + matricula.getId(),
                Map.of("alunoId", matricula.getAluno().getId(), "planoId", matricula.getPlano().getId()));
    }

    private BuscaGlobalResponseDTO.Item pagamentoItem(Pagamento pagamento) {
        return new BuscaGlobalResponseDTO.Item(pagamento.getId(), "PAGAMENTO",
                "Pagamento #" + pagamento.getId(), pagamento.getMatricula().getAluno().getNome(),
                pagamento.getStatus().name(), "/pagamentos/" + pagamento.getId(),
                Map.of("matriculaId", pagamento.getMatricula().getId()));
    }

    private BuscaGlobalResponseDTO.Item planoItem(Plano plano) {
        return new BuscaGlobalResponseDTO.Item(plano.getId(), "PLANO", plano.getNome(),
                plano.getAtivo() ? "Ativo" : "Inativo", plano.getAtivo() ? "ATIVO" : "INATIVO",
                "/planos/" + plano.getId(), Map.of("duracaoEmDias", plano.getDuracaoEmDias()));
    }
}
