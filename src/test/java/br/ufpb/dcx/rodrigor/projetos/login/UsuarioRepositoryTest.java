package br.ufpb.dcx.rodrigor.projetos.login;

import org.junit.jupiter.api.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioRepositoryTest {

    private File tempFile;
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = File.createTempFile("UsuarioTest", ".csv");
        // Garante que o arquivo está vazio
        try (java.io.PrintWriter writer = new java.io.PrintWriter(tempFile)) {
            writer.print("");
        }
        usuarioRepository = new UsuarioRepository(tempFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void testSalvarUsuario() {
        Usuario usuario = new Usuario("login1", "Nome1", "senha1");
        usuarioRepository.salvarUsuario(usuario);

        List<Usuario> usuarios = usuarioRepository.listarUsuarios();
        assertEquals(1, usuarios.size());
        assertEquals("login1", usuarios.get(0).getLogin());
    }

    @Test
    void testListarUsuarios() {
        Usuario usuario1 = new Usuario("login1", "Nome1", "senha1");
        Usuario usuario2 = new Usuario("login2", "Nome2", "senha2");
        usuarioRepository.salvarUsuario(usuario1);
        usuarioRepository.salvarUsuario(usuario2);

        List<Usuario> usuarios = usuarioRepository.listarUsuarios();
        assertEquals(2, usuarios.size());
    }

    @Test
    void testBuscarUsuarioPorLogin() {
        Usuario usuario = new Usuario("login1", "Nome1", "senha1");
        usuarioRepository.salvarUsuario(usuario);

        Usuario encontrado = usuarioRepository.buscarUsuarioPorLogin("login1");
        assertNotNull(encontrado);
        assertEquals("Nome1", encontrado.getNome());
    }

    @Test
    void testRemoverUsuario() {
        Usuario usuario = new Usuario("login1", "Nome1", "senha1");
        usuarioRepository.salvarUsuario(usuario);

        usuarioRepository.removerUsuario(usuario.getId());
        List<Usuario> usuarios = usuarioRepository.listarUsuarios();
        assertTrue(usuarios.isEmpty());
    }
}