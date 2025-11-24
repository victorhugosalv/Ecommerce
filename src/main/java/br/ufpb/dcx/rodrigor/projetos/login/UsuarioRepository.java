package br.ufpb.dcx.rodrigor.projetos.login;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UsuarioRepository {

    private final String csvPath;
    private static final String DEFAULT_CSV_PATH = "src/main/resources/csv/Usuario.csv";
    private final Map<String, Usuario> usuarios = new HashMap<>();

    public UsuarioRepository() {
        this(DEFAULT_CSV_PATH);
    }

    public UsuarioRepository(String csvPath) {
        this.csvPath = csvPath;
        carregarUsuarios();
    }

    private void carregarUsuarios() {
        usuarios.clear();
        Path path = Paths.get(csvPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao criar arquivo de usuários.", e);
            }
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(",", -1); // -1 para não descartar campos vazios
                if (partes.length == 4) {
                    Usuario u = new Usuario();
                    u.setId(partes[0]);
                    u.setLogin(partes[1]);
                    u.setNome(partes[2]);
                    u.setSenha(partes[3]);
                    usuarios.put(u.getId(), u);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo de usuários.", e);
        }
    }

    public void salvarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || usuario.getLogin() == null) {
            throw new IllegalArgumentException("Usuário ou campos obrigatórios nulos.");
        }
        carregarUsuarios();
        usuarios.put(usuario.getId(), usuario);
        salvarUsuarios();
    }

    private void salvarUsuarios() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvPath))) {
            for (Usuario u : usuarios.values()) {
                writer.write(String.join(",",
                    u.getId(),
                    u.getLogin(),
                    u.getNome(),
                    u.getSenha()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar usuários.", e);
        }
    }

    public void removerUsuario(String id) {
        carregarUsuarios();
        usuarios.remove(id);
        salvarUsuarios();
    }

    public List<Usuario> listarUsuarios() {
        carregarUsuarios();
        return new ArrayList<>(usuarios.values());
    }

    public Usuario buscarUsuarioPorLogin(String login) {
        carregarUsuarios();
        return usuarios.values().stream()
                .filter(u -> login.equals(u.getLogin()))
                .findFirst()
                .orElse(null);
    }

    public boolean existeUsuario(String id) {
        carregarUsuarios();
        return usuarios.containsKey(id);
    }
}