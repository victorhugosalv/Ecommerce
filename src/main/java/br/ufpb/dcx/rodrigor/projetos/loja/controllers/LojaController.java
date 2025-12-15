package br.ufpb.dcx.rodrigor.projetos.loja.controllers;

import br.ufpb.dcx.rodrigor.projetos.Keys;
import br.ufpb.dcx.rodrigor.projetos.login.Usuario;
import br.ufpb.dcx.rodrigor.projetos.loja.model.Carrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.model.ItemCarrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.services.CarrinhoService;
import br.ufpb.dcx.rodrigor.projetos.product.model.Product;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LojaController {

    private static final Logger logger = LogManager.getLogger(LojaController.class);

    private Carrinho obterCarrinho(Context ctx) {
        Carrinho carrinho = ctx.sessionAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new Carrinho();
            ctx.sessionAttribute("carrinho", carrinho);
        }
        return carrinho;
    }

    /**
     * Exibe a página principal da loja (Vitrine).
     * Busca todos os produtos e define qual layout usar (logado ou público).
     */
    public void mostrarVitrine(Context ctx) {
        try {
            ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());

            List<Product> produtos = service.listarProdutos().stream()
                    .filter(p -> p != null)
                    .collect(Collectors.toList());

            Map<String, Object> model = new HashMap<>();
            model.put("produtos", produtos);
            model.put("carrinho", obterCarrinho(ctx));
            model.put("nomeLayout", (ctx.sessionAttribute("usuario") != null) ? "layout" : "layout-publico");

            ctx.render("/loja/vitrine.html", model);
        } catch (Exception e) {
            logger.error("Erro ao renderizar vitrine", e);
            ctx.status(500).result("Erro interno no servidor");
        }
    }

    /**
     * Endpoint responsável por adicionar um produto ao carrinho de compras.
     * <p>
     * Fluxo de execução:
     * 1. Captura o ID do produto da URL e a quantidade do formulário (padrão = 1).
     * 2. Recupera o usuário logado (se houver) e o carrinho da sessão atual.
     * 3. Delega ao {@link CarrinhoService} a lógica de adicionar o item na memória
     * e sincronizar com o banco de dados.
     * 4. Redireciona o usuário de volta para a Vitrine.
     * </p>
     *
     * @param ctx Contexto da requisição HTTP (Javalin).
     */
    public void adicionarAoCarrinho(Context ctx) {

        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            String qtdStr = ctx.formParam("quantidade");
            int quantidade = (qtdStr != null && !qtdStr.isEmpty()) ? Integer.parseInt(qtdStr) : 1;

            Usuario usuario = ctx.sessionAttribute("usuario");
            Carrinho carrinho = obterCarrinho(ctx);

            CarrinhoService service = ctx.appData(Keys.CARRINHO_SERVICE.key());
            service.adicionarItem(usuario, carrinho, id, quantidade);

            logger.info("Solicitação de adição ao carrinho: Produto ID {}", id);

        } catch (Exception e) {
            logger.warn("Erro ao adicionar ao carrinho", e);
        }
        ctx.redirect("/vitrine");
    }

    /**
     * Exibe a tela do carrinho de compras.
     * Aciona o serviço para validar se existem itens órfãos antes de renderizar.
     */
    public void verCarrinho(Context ctx) {
        Carrinho carrinho = obterCarrinho(ctx);

        CarrinhoService carrinhoService = ctx.appData(Keys.CARRINHO_SERVICE.key());
        carrinhoService.validarItensDoCarrinho(carrinho);

        Map<String, Object> model = new HashMap<>();
        model.put("carrinho", carrinho);
        model.put("nomeLayout", (ctx.sessionAttribute("usuario") != null) ? "layout" : "layout-publico");

        ctx.render("/loja/carrinho.html", model);
    }

    /**
     * Rota para remover um item.
     * Sincroniza a remoção tanto na Sessão quanto no Banco (se logado).
     */
    public void removerDoCarrinho(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Usuario usuario = ctx.sessionAttribute("usuario");

        if (usuario != null) {
            CarrinhoService service = ctx.appData(Keys.CARRINHO_SERVICE.key());
            service.removerItem(usuario, id);
        }

        Carrinho carrinho = ctx.sessionAttribute("carrinho");
        if (carrinho != null) {
            carrinho.removerItem(id);
        }

        ctx.redirect("/carrinho");
    }

    public void aumentarQuantidade(Context ctx) {
        processarAlteracaoQuantidade(ctx, 1);
    }

    public void diminuirQuantidade(Context ctx) {
        processarAlteracaoQuantidade(ctx, -1);
    }

    /**
     * Método auxiliar privado para processar a alteração (+1 ou -1) na quantidade de um item.
     * <p>
     * Centraliza a lógica de captura de parâmetros e delegação para o serviço, evitando
     * repetição de código nos endpoints de aumentar e diminuir quantidade.
     * </p>
     *
     * @param ctx   Contexto da requisição (contém o ID do produto na URL).
     * @param delta A variação da quantidade (ex: +1 para aumentar, -1 para diminuir).
     */
    private void processarAlteracaoQuantidade(Context ctx, int delta) {
        try {
            int produtoId = Integer.parseInt(ctx.pathParam("id"));
            Carrinho carrinho = obterCarrinho(ctx);
            Usuario usuario = ctx.sessionAttribute("usuario");

            // O Controller apenas delega para o Service
            CarrinhoService service = ctx.appData(Keys.CARRINHO_SERVICE.key());
            service.alterarQuantidade(usuario, carrinho, produtoId, delta);

        } catch (NumberFormatException e) {
            logger.warn("ID de produto inválido: {}", ctx.pathParam("id"));
        } catch (Exception e) {
            logger.error("Erro ao alterar quantidade", e);
        }

        ctx.redirect("/carrinho");
    }
}
