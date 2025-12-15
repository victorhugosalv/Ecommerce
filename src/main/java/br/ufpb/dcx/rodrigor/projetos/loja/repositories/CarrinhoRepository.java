package br.ufpb.dcx.rodrigor.projetos.loja.repositories;

import br.ufpb.dcx.rodrigor.projetos.login.Usuario;
import br.ufpb.dcx.rodrigor.projetos.loja.model.Carrinho;
import br.ufpb.dcx.rodrigor.projetos.product.model.Product;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import config.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CarrinhoRepository {

    // Salva ou Atualiza um item (Upsert)
    public void salvarItem(String usuarioId, int produtoId, int quantidade) {
        String sql = "INSERT INTO itens_carrinho (usuario_id, produto_id, quantidade) VALUES (?, ?, ?) " +
                "ON CONFLICT (usuario_id, produto_id) DO UPDATE SET quantidade = EXCLUDED.quantidade";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);
            stmt.setInt(2, produtoId);
            stmt.setInt(3, quantidade);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar item no carrinho", e);
        }
    }

    public void removerItem(String usuarioId, int produtoId) {
        String sql = "DELETE FROM itens_carrinho WHERE usuario_id = ? AND produto_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);
            stmt.setInt(2, produtoId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover item do carrinho", e);
        }
    }

    public Carrinho carregarCarrinho(String usuarioId, ProductService productService) {
        Carrinho carrinho = new Carrinho();
        String sql = "SELECT produto_id, quantidade FROM itens_carrinho WHERE usuario_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int prodId = rs.getInt("produto_id");
                int qtd = rs.getInt("quantidade");

                try {
                    // Busca o produto completo para preencher o carrinho
                    Product p = productService.buscarProduto(prodId);
                    carrinho.adicionarItem(p, qtd);
                } catch (Exception e) {
                    // Se o produto foi deletado da loja, ignoramos ele no carrinho
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar carrinho", e);
        }
        return carrinho;
    }
}
