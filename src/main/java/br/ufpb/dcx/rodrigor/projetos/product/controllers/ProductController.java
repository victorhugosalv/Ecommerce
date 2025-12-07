package br.ufpb.dcx.rodrigor.projetos.product.controllers;

import br.ufpb.dcx.rodrigor.projetos.Keys;
import br.ufpb.dcx.rodrigor.projetos.product.model.Product;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import io.javalin.Javalin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductController {

    public void registerRoutes(Javalin app){

        // 1. LISTAR PRODUTOS (Com Busca)
        app.get("/products", ctx -> {
            try{
                ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());
                String termoBusca = ctx.queryParam("busca");
                List<Product> products = new ArrayList<>();

                if (termoBusca != null && !termoBusca.isEmpty()) {
                    // Lógica de Busca Híbrida
                    try {
                        // Tenta transformar em número para buscar por ID
                        int id = Integer.parseInt(termoBusca);
                        try {
                            Product p = service.buscarProduto(id);
                            products.add(p);
                        } catch (Exception e) {
                            //Não encontrou nada
                        }
                    } catch (NumberFormatException e) {
                        products = service.buscarProdutosPorNome(termoBusca);
                    }
                } else {
                    // Sem busca: traz tudo
                    products = service.listarProdutos();
                }

                Map<String, Object> model = new HashMap<>();
                model.put("products", products);
                model.put("busca", termoBusca);

                ctx.render("products/lista_produtos", model);

            } catch ( RuntimeException e ){
                ctx.status(500);
                ctx.result("Erro no servidor: " + e.getMessage());
            }
        });

        // 2. ABRIR FORMULÁRIO DE NOVO PRODUTO
        app.get("/products/novo", ctx -> {
            Map<String, Object> model = new HashMap<>();
            // Manda um produto vazio para o Thymeleaf não reclamar dos campos nulos
            model.put("product", new Product());
            ctx.render("products/form_produto", model);
        });

        // 3. ABRIR FORMULÁRIO DE EDIÇÃO
        app.get("/products/editar/{id}", ctx -> {
            try {
                ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());
                int id = Integer.parseInt(ctx.pathParam("id"));

                Product p = service.buscarProduto(id);

                Map<String, Object> model = new HashMap<>();
                model.put("product", p); // Manda o produto existente para preencher os inputs
                ctx.render("products/form_produto", model);

            } catch (Exception e) {
                ctx.status(404).result("Produto não encontrado para edição.");
            }
        });

        // 4. SALVAR

        app.post("/products/salvar", ctx -> {
            ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());

            String idTxt = ctx.formParam("id");
            if (idTxt == null || idTxt.isEmpty()) idTxt = ctx.queryParam("id");

            String nome = ctx.formParam("nome");
            String precoTxt = ctx.formParam("preco");
            String descricao = ctx.formParam("descricao");

            Product p = new Product();
            p.setNome(nome);
            p.setDescricao(descricao);


            if (precoTxt != null) precoTxt = precoTxt.replace(",", ".");
            try {
                p.setPreco(new java.math.BigDecimal(precoTxt));
            } catch (Exception e) {
                p.setPreco(java.math.BigDecimal.ZERO); // Valor padrão se falhar a conversão
            }

            try {
                // TENTA SALVAR
                if (idTxt != null && !idTxt.isEmpty()) {
                    int id = Integer.parseInt(idTxt);
                    p.setId(id);
                    service.alterarProduto(id, p);
                } else {
                    service.inserirProduto(p);
                }


                ctx.redirect("/products");

            } catch (Exception e) {
                Map<String, Object> model = new HashMap<>();
                model.put("product", p);


                String msgOriginal = e.getMessage();
                String mensagemAmigavel = "Erro ao salvar o produto.";


                if (msgOriginal.contains("estouro de campo") || msgOriginal.contains("numeric field overflow")) {
                    mensagemAmigavel = "O preço informado é muito alto! O limite é 9.999.999.999,99.";
                } else if (msgOriginal.contains("viola a restrição")) {
                    mensagemAmigavel = "Valor inválido para o banco de dados.";
                } else {
                    mensagemAmigavel += " Detalhe: " + msgOriginal;
                }

                model.put("erro", mensagemAmigavel);
                ctx.render("form-produto", model);
            }
        });

        // 5. REMOVER
        app.get("/products/remover/{id}", ctx -> {
            try {
                ProductService service = ctx.appData(Keys.PRODUCT_SERVICE.key());
                int id = Integer.parseInt(ctx.pathParam("id"));

                service.excluirProduto(id);

                ctx.redirect("/products");

            } catch (Exception e) {
                ctx.status(500).result("Erro ao excluir: " + e.getMessage());
            }
        });
    }
}