package br.ufpb.dcx.rodrigor.projetos.product.services;

import br.ufpb.dcx.rodrigor.projetos.login.LoginController;
import br.ufpb.dcx.rodrigor.projetos.product.model.Product;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductRepository repo = new ProductRepository();

    private static final Logger logger = LogManager.getLogger(ProductService.class);

    public ProductService() {}

    public List<Product> listarProdutos() {
        try {
            return repo.listarProdutos();
        }catch (SQLException e){
            logger.error("Falha ao listar produtos", e);
            throw new RuntimeException("Erro ao listar produtos;\n" + e.getMessage());
        }
    }

    public Product inserirProduto(Product produto) {
        try{
            Product novoProduto =  repo.inserirProduto(produto);

            logger.info("Produto adicionado: ID=[{}] Nome='{}' Preço={}", novoProduto.getId(), novoProduto.getNome(), novoProduto.getPreco());

            return novoProduto;
        } catch (SQLException e) {
            logger.error("Erro ao tentar inserir o produto: {}", produto.getNome());
            throw new RuntimeException("Erro ao inserir produto;\n" + e.getMessage());
        }
    }

    public Product buscarProduto(int id) {
        try{
            Product p = repo.buscarPorId(id);
            if (p == null){
                logger.warn("Busca por produto inexistente: ID {}", id);
                throw new RuntimeException("Produto com ID " + id + " não encontrado");
            }
            return p;
        } catch (SQLException e) {
            logger.error("Erro ao buscar produto ID {}", id, e);
            throw new RuntimeException("Erro ao buscar produto;\n" + e.getMessage());
        }
    }

    public Product alterarProduto(int id, Product novoProduto) {
        try {
            Product existente = repo.buscarPorId(id);
            if (existente == null){
                throw new RuntimeException("Produto com ID " + id + " não existe");
            }

            String nomeAntigo = existente.getNome();

            existente.setNome(novoProduto.getNome());
            existente.setDescricao(novoProduto.getDescricao());
            existente.setPreco(novoProduto.getPreco());

            boolean ok = repo.alterarProduto(existente);
            if (!ok)
                throw new RuntimeException("Falha ao atualizar produto");

            logger.info("Produto Alterado: ID=[{}] '{}' -> '{}'", id, nomeAntigo, novoProduto.getNome());

            return existente;


        } catch (SQLException e){
            logger.error("Erro ao alterar produto ID {}", id, e);
            throw new RuntimeException("Erro ao alterar produto;\n" + e.getMessage());
        }
    }

    public List<Product> buscarProdutosPorNome(String termo) {
        try {
            return repo.buscarPorNome(termo);
        } catch (SQLException e) {
            logger.error("Erro na busca por nome: '{}'", termo, e);
            throw new RuntimeException("Erro ao buscar produtos por nome:\n" + e.getMessage());
        }
    }

    public void excluirProduto(int id) {
        try{
            boolean ok = repo.excluirProduto(id);
            if (!ok){
                logger.warn("Tentativa de excluir produto inexistente: ID {}", id);
                throw new RuntimeException("Produto com Id " + id + " não existe");
            }

            logger.info("PRODUTO REMOVIDO: ID=[{}]", id);

        }catch(SQLException e){
            logger.error("Erro ao excluir produto ID {}", id, e);
            throw new RuntimeException("Erro ao remover produto;\n" + e.getMessage());
        }
    }
}
