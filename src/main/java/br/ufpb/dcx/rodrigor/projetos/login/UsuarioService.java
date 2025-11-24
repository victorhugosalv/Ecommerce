package br.ufpb.dcx.rodrigor.projetos.login;

import java.util.List;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
    }

    public void cadastrarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getLogin() == null || usuario.getSenha() == null) {
            throw new IllegalArgumentException("Usuário inválido ou sem login/senha.");
        }
        usuarioRepository.salvarUsuario(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.listarUsuarios();
    }

    public void removerUsuario(String id) {
        if (id == null || !usuarioRepository.existeUsuario(id)) {
            throw new IllegalArgumentException("Usuário inválido ou não encontrado.");
        }
        usuarioRepository.removerUsuario(id);
    }

    public Usuario buscarUsuarioPorLogin(String login) {
        if (login == null || login.isEmpty()) {
            throw new IllegalArgumentException("Login inválido.");
        }
        return usuarioRepository.buscarUsuarioPorLogin(login);
    }
}