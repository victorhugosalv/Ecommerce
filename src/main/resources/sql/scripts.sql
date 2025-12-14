-- ============================================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS - ECOMMERCE
-- ============================================================

-- 1. Criação da Tabela de Produtos
CREATE TABLE IF NOT EXISTS products (
    idproduct SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    descricao TEXT NOT NULL,
    preco NUMERIC(10,2) CHECK (preco >= 0)
    );

-- 2. Criação da Tabela de Itens do Carrinho
CREATE TABLE IF NOT EXISTS itens_carrinho (
    usuario_id VARCHAR(255) NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL CHECK (quantidade > 0),

    CONSTRAINT pk_itens_carrinho PRIMARY KEY (usuario_id, produto_id),
    CONSTRAINT fk_produto FOREIGN KEY (produto_id) REFERENCES products(idproduct) ON DELETE CASCADE
    );

-- 3. Dados Iniciais (Opcional, para teste)
INSERT INTO products (nome, descricao, preco) VALUES
    ('Notebook Gamer', 'Processador i7, 16GB RAM', 4500.00),
    ('Mouse Sem Fio', 'Ergonômico e silencioso', 120.50),
    ('Teclado Mecânico', 'Switch Blue, RGB', 350.00);