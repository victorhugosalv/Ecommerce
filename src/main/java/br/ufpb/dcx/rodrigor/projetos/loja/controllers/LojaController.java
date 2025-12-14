package br.ufpb.dcx.rodrigor.projetos.loja.controllers;

import br.ufpb.dcx.rodrigor.projetos.Keys;
import br.ufpb.dcx.rodrigor.projetos.login.Usuario;
import br.ufpb.dcx.rodrigor.projetos.loja.model.Carrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.model.ItemCarrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.services.CarrinhoService;
import br.ufpb.dcx.rodrigor.projetos.product.model.Product;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LojaController {

    // Auxiliar para pegar carrinho da sessão
    private Carrinho obterCarrinho(Context ctx) {
        Carrinho carrinho = ctx.sessionAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new Carrinho();
            ctx.sessionAttribute("carrinho", carrinho);
        }
        return carrinho;
    }

    // Auxiliar para chamar o SERVICE
    private void persistirSeLogado(Context ctx, int produtoId, int novaQuantidade) {
        Usuario u = ctx.sessionAttribute("usuario");
        if (u != null) {
            CarrinhoService service = ctx.appData(Keys.CARRINHO_SERVICE.key());
            service.atualizarItem(u, produtoId, novaQuantidade);
        }
    }

    public void mostrarVitrine(Context ctx) {
        ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());

        List<Product> produtos = service.listarProdutos().stream()
                .filter(p -> p != null)
                .collect(Collectors.toList());

        Map<String, Object> model = new HashMap<>();
        model.put("produtos", produtos);
        model.put("carrinho", obterCarrinho(ctx));
        model.put("nomeLayout", (ctx.sessionAttribute("usuario") != null) ? "layout" : "layout-publico");

        ctx.render("/loja/vitrine.html", model);
    }

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

                // Pega o item atualizado para saber a quantidade total e manda pro Service
                Optional<ItemCarrinho> item = carrinho.getItens().stream()
                        .filter(i -> i.getProduto().getId() == id).findFirst();

                if(item.isPresent()){
                    persistirSeLogado(ctx, id, item.get().getQuantidade());
                }
            }
            ctx.redirect("/vitrine");
        } catch (Exception e) {
            ctx.redirect("/vitrine");
        }
    }

    public void verCarrinho(Context ctx) {
        Carrinho carrinho = obterCarrinho(ctx);
        ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());

        // Limpeza de itens inválidos
        carrinho.getItens().removeIf(item -> {
            try {
                return service.buscarProduto(item.getProduto().getId()) == null;
            } catch (Exception e) {
                return true;
            }
        });

        Map<String, Object> model = new HashMap<>();
        model.put("carrinho", carrinho);
        model.put("nomeLayout", (ctx.sessionAttribute("usuario") != null) ? "layout" : "layout-publico");

        ctx.render("/loja/carrinho.html", model);
    }

    public void removerDoCarrinho(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        obterCarrinho(ctx).removerItem(id);

        persistirSeLogado(ctx, id, 0); // 0 = remover

        ctx.redirect("/carrinho");
    }

    public void aumentarQuantidade(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());
        Product produto = service.buscarProduto(id);

        if(produto != null) {
            Carrinho c = obterCarrinho(ctx);
            c.adicionarItem(produto, 1);

            c.getItens().stream()
                    .filter(i -> i.getProduto().getId() == id)
                    .findFirst()
                    .ifPresent(item -> persistirSeLogado(ctx, id, item.getQuantidade()));
        }
        ctx.redirect("/carrinho");
    }

    public void diminuirQuantidade(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Carrinho c = obterCarrinho(ctx);
        c.diminuirItem(id);

        Optional<ItemCarrinho> item = c.getItens().stream()
                .filter(i -> i.getProduto().getId() == id).findFirst();

        if (item.isPresent()) {
            persistirSeLogado(ctx, id, item.get().getQuantidade());
        } else {
            persistirSeLogado(ctx, id, 0);
        }

        ctx.redirect("/carrinho");
    }
}
