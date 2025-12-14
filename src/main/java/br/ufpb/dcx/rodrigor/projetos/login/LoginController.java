package br.ufpb.dcx.rodrigor.projetos.login;

import br.ufpb.dcx.rodrigor.projetos.Keys;
import br.ufpb.dcx.rodrigor.projetos.loja.model.Carrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.model.ItemCarrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.services.CarrinhoRepository;
import br.ufpb.dcx.rodrigor.projetos.loja.services.CarrinhoService;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);

    // Método mostrarPaginaLogin continua igual...
    public void mostrarPaginaLogin(Context ctx) {
        String teste = ctx.queryParam("teste");
        if(teste != null){
            throw new RuntimeException("Erro de teste");
        }
        ctx.attribute("esconderCarrinho", true);
        ctx.render("/login/login.html");
    }

    public void processarLogin(Context ctx) {
        String login = ctx.formParam("login");
        String senha = ctx.formParam("senha");

        UsuarioService usuarioService = ctx.appData(Keys.USUARIO_SERVICE.key());
        Usuario usuario = usuarioService.buscarUsuarioPorLogin(login);

        if (usuario != null && BCrypt.checkpw(senha, usuario.getSenha())) {
            ctx.sessionAttribute("usuario", usuario);
            logger.info("Usuário '{}' autenticado com sucesso.", login);

            // --- USO CORRETO DO SERVICE ---
            try {
                CarrinhoService carrinhoService = ctx.appData(Keys.CARRINHO_SERVICE.key());
                Carrinho carrinhoSessaoAtual = ctx.sessionAttribute("carrinho");

                // O Service cuida de buscar no banco e misturar com o da sessão
                Carrinho carrinhoFinal = carrinhoService.mesclarCarrinhos(usuario, carrinhoSessaoAtual);

                // Atualiza a sessão com o resultado final
                ctx.sessionAttribute("carrinho", carrinhoFinal);

            } catch (Exception e) {
                logger.error("Erro ao processar carrinho no login", e);
            }
            // -----------------------------

            ctx.redirect("/vitrine");
        } else {
            logger.warn("Tentativa de login falhou: {}", login);
            ctx.attribute("erro", "Usuário ou senha inválidos");
            ctx.render("/login/login.html");
        }
    }

    public void logout(Context ctx) {
        ctx.sessionAttribute("usuario", null);
        ctx.sessionAttribute("carrinho", null);
        ctx.redirect("/login");
    }
}