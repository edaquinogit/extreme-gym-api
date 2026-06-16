package com.extreme.gym.service;

import com.extreme.gym.entity.AuditoriaAcesso;
import com.extreme.gym.repository.AuditoriaAcessoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditoriaAcessoService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAcessoService.class);

    private final AuditoriaAcessoRepository auditoriaAcessoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String acao, String entidade, Long entidadeId, Long dispositivoId, String origem, String resumo) {
        AuditoriaAcesso auditoria = AuditoriaAcesso.builder()
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .dispositivoId(dispositivoId)
                .origem(origem)
                .resumo(resumo)
                .build();
        auditoriaAcessoRepository.save(auditoria);
        log.info("Auditoria de acesso registrada: acao={}, entidade={}, entidadeId={}", acao, entidade, entidadeId);
    }
}
