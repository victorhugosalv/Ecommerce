package br.ufpb.dcx.rodrigor.projetos.loja.controllers;

import br.ufpb.dcx.rodrigor.projetos.Keys;
import br.ufpb.dcx.rodrigor.projetos.loja.model.Carrinho;
import br.ufpb.dcx.rodrigor.projetos.product.model.Product;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LojaController {

    private Carrinho obterCarrinho(Context ctx) {
        Carrinho carrinho = ctx.sessionAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new Carrinho();
            ctx.sessionAttribute("carrinho", carrinho);
        }
        return carrinho;
    }

    public void mostrarVitrine(Context ctx) {
        ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());

        // Remove produtos nulos para evitar erro OGNL no HTML
        List<Product> produtos = service.listarProdutos().stream()
                .filter(p -> p != null)
                .collect(Collectors.toList());

        Map<String, Object> model = new HashMap<>();
        model.put("produtos", produtos);
        model.put("carrinho", obterCarrinho(ctx));

        // Define layout dinamicamente
        if (ctx.sessionAttribute("usuario") != null) {
            model.put("nomeLayout", "layout");
        } else {
            model.put("nomeLayout", "layout-publico");
        }

        ctx.render("/loja/vitrine.html", model);
    }

    // Processa o formulário de compra
    public void adicionarAoCarrinho(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            // Pega a quantidade escolhida (padrão é 1)
            String qtdStr = ctx.formParam("quantidade");
            int quantidade = (qtdStr != null && !qtdStr.isEmpty()) ? Integer.parseInt(qtdStr) : 1;

            ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());
            Product produto = service.buscarProduto(id);

            if (produto != null) {
                Carrinho carrinho = obterCarrinho(ctx);
                carrinho.adicionarItem(produto, quantidade);
            }

            // REDIRECIONA DE VOLTA PARA A VITRINE
            ctx.redirect("/vitrine");

        } catch (Exception e) {
            ctx.redirect("/vitrine");
        }
    }


    public void verCarrinho(Context ctx) {
        Carrinho carrinho = obterCarrinho(ctx);
        ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());

        // Verifica cada item do carrinho. Se o produto não existir mais no serviço, marca para remoção.
        // Usamos removeIf: "Remova se... buscarProduto retornar null ou der erro"
        carrinho.getItens().removeIf(item -> {
            try {
                Product p = service.buscarProduto(item.getProduto().getId());
                return p == null; // Se for null, remove (true)
            } catch (Exception e) {
                return true; // Se der erro ao buscar (ex: id não encontrado), remove também
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
        ctx.redirect("/carrinho");
    }

    public void aumentarQuantidade(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());
        Product produto = service.buscarProduto(id);
        if(produto != null) obterCarrinho(ctx).adicionarItem(produto, 1);
        ctx.redirect("/carrinho");
    }

    public void diminuirQuantidade(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        obterCarrinho(ctx).diminuirItem(id);
        ctx.redirect("/carrinho");
    }
}
