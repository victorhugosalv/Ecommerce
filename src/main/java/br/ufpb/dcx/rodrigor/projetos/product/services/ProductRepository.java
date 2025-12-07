package br.ufpb.dcx.rodrigor.projetos.product.services;

import br.ufpb.dcx.rodrigor.projetos.product.model.Product;
import config.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public ProductRepository() {}

    public List<Product> listarProdutos() throws SQLException {
        List<Product> lista = new ArrayList<>();


        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select * from products");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product p = mapearProduto(rs);
                lista.add(p);
            }
        }
        return lista;
    }


    public Product buscarPorId(int id) throws SQLException {
        String sql = "select * from products where idproduct = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProduto(rs);
                }
            }
        }
        return null;
    }

    public List<Product> buscarPorNome(String termo) throws SQLException {
        List<Product> lista = new ArrayList<>();
        // Use ILIKE para Postgres, se for MySQL use LIKE
        String sql = "SELECT * FROM products WHERE nome ILIKE ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + termo + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProduto(rs));
                }
            }
        }
        return lista;
    }

    public Product inserirProduto(Product p) throws SQLException {
        // Validação de segurança
        if (p.getNome() == null) {
            throw new SQLException("Erro: Nome do produto não pode ser nulo.");
        }

        String sql = "INSERT INTO products (nome, descricao, preco) VALUES (?, ?, ?) RETURNING idproduct";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNome());

            if (p.getDescricao() != null) {
                stmt.setString(2, p.getDescricao());
            } else {
                stmt.setNull(2, java.sql.Types.VARCHAR);
            }

            stmt.setBigDecimal(3, p.getPreco());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    p.setId(rs.getInt("idproduct"));
                }
            }
        }
        return p;
    }

    public boolean alterarProduto(Product p) throws SQLException {
        String sql = "UPDATE products SET nome = ?, descricao = ?, preco = ? WHERE idproduct = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, p.getNome());


            if (p.getDescricao() != null) {
                stmt.setString(2, p.getDescricao());
            } else {
                stmt.setNull(2, java.sql.Types.VARCHAR);
            }

            stmt.setBigDecimal(3, p.getPreco());
            stmt.setInt(4, p.getId());

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }

    public boolean excluirProduto(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE idproduct = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }


    private Product mapearProduto(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("idproduct"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getBigDecimal("preco"));
        return p;
    }
}