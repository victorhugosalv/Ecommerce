package br.ufpb.dcx.rodrigor.projetos.loja.model;

import br.ufpb.dcx.rodrigor.projetos.product.model.Product;

import java.math.BigDecimal;

public class ItemCarrinho {
    private Product produto;
    private int quantidade;

    public ItemCarrinho(Product produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
    }

    // Calcula o Subtotal: Preço * Quantidade
    public BigDecimal getTotalParcial() {
        if (produto.getPreco() == null) return BigDecimal.ZERO;
        return produto.getPreco().multiply(new BigDecimal(quantidade));
    }

    public void adicionarQuantidade(int qtd) {
        this.quantidade += qtd;
    }

    public void removerQuantidade(int qtd) {
        this.quantidade -= qtd;
        if (this.quantidade < 0) this.quantidade = 0;
    }
    

    // Getters e Setters
    public Product getProduto() { return produto; }
    public void setProduto(Product produto) { this.produto = produto; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}