package br.ufpb.dcx.rodrigor.projetos.projeto.services;

import br.ufpb.dcx.rodrigor.projetos.participante.services.ParticipanteService;
import br.ufpb.dcx.rodrigor.projetos.projeto.model.Projeto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class ProjetoService {

    private final ParticipanteService participanteService;

    private final Map<String,Projeto> projetos;

    private static final Logger logger = LogManager.getLogger(ProjetoService.class);

    public ProjetoService(ParticipanteService participanteService) {
        this.projetos = new HashMap<String,Projeto>();
        this.participanteService = participanteService;
    }



    public List<Projeto> listarProjetos() {

        return projetos.values().stream()
                .sorted(Comparator.comparing(Projeto::getDataInicio))
                .toList();
    }

     public Optional<Projeto> buscarProjetoPorId(String id) {
        return Optional.ofNullable(projetos.get(id));
    }


    public void adicionarProjeto(Projeto projeto) {
        this.projetos.put(projeto.getId(), projeto);
    }

    public void atualizarProjeto(Projeto projetoAtualizado) {
        if(projetoAtualizado == null || projetoAtualizado.getId() == null) {
            throw new IllegalArgumentException("Projeto inválido ou sem ID.");
        }
        String id = projetoAtualizado.getId();

        projetos.put(id, projetoAtualizado);
    }

    public void removerProjeto(String id) {
        this.projetos.remove(id);
    }


}