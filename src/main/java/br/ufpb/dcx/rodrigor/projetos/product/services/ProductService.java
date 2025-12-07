package br.ufpb.dcx.rodrigor.projetos.product.services;

import br.ufpb.dcx.rodrigor.projetos.product.model.Product;

import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private ProductRepository repo = new ProductRepository();

    public ProductService() {}

    public List<Product> listarProdutos() {
        try {
            return repo.listarProdutos();
        }catch (SQLException e){
            throw new RuntimeException("Erro ao listar produtos;\n" + e.getMessage());
        }
    }

    public Product inserirProduto(Product produto) {
        try{
            return repo.inserirProduto(produto);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto;\n" + e.getMessage());
        }
    }

    public Product buscarProduto(int id) {
        try{
            Product p = repo.buscarPorId(id);
            if (p == null){
                throw new RuntimeException("Produto com ID " + id + " não encontrado");
            }
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto;\n" + e.getMessage());
        }
    }

    public Product alterarProduto(int id, Product novoProduto) {
        try {
            Product existente = repo.buscarPorId(id);
            if (existente == null){
                throw new RuntimeException("Produto com ID " + id + " não existe");
            }

            existente.setNome(novoProduto.getNome());
            existente.setDescricao(novoProduto.getDescricao());
            existente.setPreco(novoProduto.getPreco());

            boolean ok = repo.alterarProduto(existente);
            if (!ok)
                throw new RuntimeException("Falha ao atualizar produto");

            return existente;


        } catch (SQLException e){
            throw new RuntimeException("Erro ao alterar produto;\n" + e.getMessage());
        }
    }

    public List<Product> buscarProdutosPorNome(String termo) {
        try {
            return repo.buscarPorNome(termo);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produtos por nome:\n" + e.getMessage());
        }
    }

    public void excluirProduto(int id) {
        try{
            boolean ok = repo.excluirProduto(id);
            if (!ok){
                throw new RuntimeException("Produto com Id " + id + " não existe");
            }
        }catch(SQLException e){
            throw new RuntimeException("Erro ao remover produto;\n" + e.getMessage());
        }
    }
}
