package br.ufpb.dcx.rodrigor.projetos.product.model;

import java.math.BigDecimal;

public class Product {

    private int idProduct;
    private String nome;
    private String descricao;
    private BigDecimal preco;

    public Product(int idProduct, String nome, String descricao, BigDecimal preco) {
        this.idProduct = idProduct;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
    }

    public Product(String nome, BigDecimal preco, String descricao) {
        this.nome = nome;
        this.preco = preco;
        this.descricao = descricao;
    }

    public Product(){

    }

    public int getId() {
        return idProduct;
    }

    public void setId(int id) {
        this.idProduct = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
}
