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

    public void mostrarPaginaLogin(Context ctx) {
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
            logger.info("Login efetuado com sucesso: {}", login);

            // --- Integração com Carrinho Service ---
            try {
                CarrinhoService carrinhoService = ctx.appData(Keys.CARRINHO_SERVICE.key());
                Carrinho carrinhoSessaoAtual = ctx.sessionAttribute("carrinho");

                // Mescla o carrinho da sessão com o do banco
                Carrinho carrinhoFinal = carrinhoService.mesclarCarrinhos(usuario, carrinhoSessaoAtual);

                ctx.sessionAttribute("carrinho", carrinhoFinal);
            } catch (Exception e) {
                logger.error("Erro crítico ao processar carrinho no login", e);
                // Não bloqueamos o login por erro no carrinho, mas logamos o erro
            }


            ctx.redirect("/vitrine");
        } else {
            logger.warn("Falha de login para: {}", login);
            ctx.attribute("erro", "Usuário ou senha inválidos");
            ctx.render("/login/login.html");
        }
    }

    public void logout(Context ctx) {
        logger.info("Logout: {}", ((Usuario)ctx.sessionAttribute("usuario")).getLogin());
        ctx.sessionAttribute("usuario", null);
        ctx.sessionAttribute("carrinho", null);
        ctx.redirect("/login");
    }
}