package br.ufpb.dcx.rodrigor.projetos.form.services;

import br.ufpb.dcx.rodrigor.projetos.form.model.Campo;
import br.ufpb.dcx.rodrigor.projetos.form.model.Formulario;
import br.ufpb.dcx.rodrigor.projetos.form.model.validadores.ValidadorEmail;
import br.ufpb.dcx.rodrigor.projetos.form.model.validadores.ValidadorSenha;
import br.ufpb.dcx.rodrigor.projetos.form.model.validadores.ValidadorTexto;
import br.ufpb.dcx.rodrigor.projetos.login.UsuarioService;
import br.ufpb.dcx.rodrigor.projetos.usuario.service.PersistenciaUsuario;

import java.util.LinkedHashMap;
import java.util.Map;

public class FormService {

    private final Map<String, Formulario> formularios = new LinkedHashMap<>();

    public FormService(){
        iniciarFormularios();
    }


    public Formulario getFormulario(String id){
        return formularios.get(id);
    }

    public void iniciarFormularios(){
        Formulario form = new Formulario("usuario", "Cadastro de Usuário");
        form.addCampo(new Campo("nome", "Nome", new ValidadorTexto(3, 100), true));
        form.addCampo(new Campo("email", "Email",new ValidadorEmail(), true));
        form.addCampo(new Campo("senha", "Senha", new ValidadorSenha(6, 20), true));
        form.setPersistencia(new PersistenciaUsuario(new UsuarioService()));
        formularios.put(form.getId(), form);
    }
}
