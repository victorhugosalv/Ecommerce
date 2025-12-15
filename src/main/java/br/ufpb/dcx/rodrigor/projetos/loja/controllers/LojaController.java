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

    private void persistirSeLogado(Context ctx, int produtoId, int novaQuantidade) {
        Usuario u = ctx.sessionAttribute("usuario");
        if (u != null) {
            CarrinhoService service = ctx.appData(Keys.CARRINHO_SERVICE.key());
            service.atualizarItem(u, produtoId, novaQuantidade);
        }
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
     * Endpoint POST para adicionar item ao carrinho.
     * <p>
     * 1. Adiciona na memória (Sessão).
     * 2. Se o usuário estiver logado, persiste no Banco via Service.
     * </p>
     *
     * @param ctx Contexto da requisição (contém ID do produto e quantidade).
     */
    public void adicionarAoCarrinho(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            String qtdStr = ctx.formParam("quantidade");
            int quantidade = (qtdStr != null && !qtdStr.isEmpty()) ? Integer.parseInt(qtdStr) : 1;

            ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());
            Product produto = service.buscarProduto(id);

            if (produto != null) {
                Carrinho carrinho = obterCarrinho(ctx);
                carrinho.adicionarItem(produto, quantidade);

                Optional<ItemCarrinho> item = carrinho.getItens().stream()
                        .filter(i -> i.getProduto().getId() == id).findFirst();

                if(item.isPresent()){
                    persistirSeLogado(ctx, id, item.get().getQuantidade());
                }
                logger.info("Produto {} adicionado ao carrinho. Qtd: {}", id, quantidade);
            }
        } catch (Exception e) {
            logger.warn("Tentativa inválida de adicionar ao carrinho", e);
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

    // Método privado auxiliar para evitar repetição de código no Controller
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
