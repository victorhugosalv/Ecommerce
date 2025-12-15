package br.ufpb.dcx.rodrigor.projetos.loja.model;

import br.ufpb.dcx.rodrigor.projetos.product.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Carrinho {
    private List<ItemCarrinho> itens = new ArrayList<>();

    //Adiciona diretamente o Produto e a Quantidade a Carrinho
    public void adicionarItem(Product produto, int quantidade) {
        Optional<ItemCarrinho> itemExistente = itens.stream()
                .filter(item -> item.getProduto().getId() == produto.getId())
                .findFirst();

        if (itemExistente.isPresent()) {
            itemExistente.get().adicionarQuantidade(quantidade);
        } else {
            itens.add(new ItemCarrinho(produto, quantidade));
        }
    }

    //Retoma os Produtos ao Carrinho
    public void adicionarItem(Product produto) {
        this.adicionarItem(produto, 1);
    }

    public void diminuirItem(int produtoId) {
        Optional<ItemCarrinho> itemExistente = itens.stream()
                .filter(item -> item.getProduto().getId() == produtoId)
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrinho item = itemExistente.get();
            item.removerQuantidade(1);

            // Se zerou, remove da lista
            if (item.getQuantidade() <= 0) {
                itens.remove(item);
            }
        }
    }

    public void removerItem(int produtoId) {
        itens.removeIf(item -> item.getProduto().getId() == produtoId);
    }

    public List<ItemCarrinho> getItens() {
        return itens;
    }

    // Soma total de todos os itens
    public BigDecimal getTotal() {
        return itens.stream()
                .map(ItemCarrinho::getTotalParcial)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getQuantidadeTotal() {
        return itens.stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();
    }
}