# 🛒 E-commerce Javalin Project

Bem-vindo ao repositório do projeto final da disciplina. Este é um sistema de E-commerce web desenvolvido em Java, utilizando uma arquitetura MVC leve, renderização no servidor (SSR) e persistência de dados.

## 📺 Demonstração em Vídeo

**[CLIQUE AQUI PARA ASSISTIR AO VÍDEO DE APRESENTAÇÃO]**
*(Insira o link do YouTube ou Drive aqui)*

> *Neste vídeo demonstramos: Cadastro de usuário, fluxo de compra, persistência do carrinho (fechando o navegador) e a área administrativa.*

---

## 🚀 Sobre o Projeto

Este sistema simula uma loja virtual completa com diferenciação entre **Cliente** e **Administrador**. O foco principal foi a implementação de persistência de dados em banco relacional, autenticação segura baseada em sessão e manipulação de estado do carrinho de compras.

### Funcionalidades Principais

#### 👤 Para o Cliente (Área Pública)
* **Vitrine de Produtos:** Visualização de produtos cadastrados com preços, descrições e paginação.
* **Carrinho de Compras Inteligente:**
    * Adicionar itens com quantidade personalizada.
    * Aumentar/Diminuir quantidades e remover itens.
    * **Persistência:** O carrinho é salvo no **PostgreSQL**. Se o usuário fechar o navegador e voltar (ou acessar de outro PC), seus itens estarão lá.
    * **Mesclagem:** Itens adicionados anonimamente (antes do login) são transferidos automaticamente para a conta do usuário ao entrar.
* **Cadastro e Login:** Sistema de autenticação e registro de novos usuários.

#### 🛡️ Para o Administrador (Área Restrita)
* **Gestão de Produtos:** CRUD completo (Criar, Ler, Atualizar, Deletar).
* **Proteção de Rotas:** Filtros de segurança (interceptadores) impedem acesso não autorizado às áreas de gestão.
* **Validações:** O sistema impede preços negativos, estoques inconsistentes e erros de formulário.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Framework Web:** [Javalin](https://javalin.io/) (Leve e performático)
* **Template Engine:** [Thymeleaf](https://www.thymeleaf.org/) (Renderização HTML no servidor)
* **Banco de Dados:** PostgreSQL 15
* **Infraestrutura:** Docker & Docker Compose
* **Logs:** Log4j2
* **Build Tool:** Maven

---

## ⚙️ Como Executar o Projeto

Oferecemos duas formas de execução. A **Opção 1 (Docker)** é a recomendada por configurar todo o ambiente automaticamente.

### 🐳 Opção 1: Via Docker (Recomendada)
O Docker se encarrega de subir o banco de dados e criar as tabelas automaticamente.

1. **Pré-requisitos:** Ter Docker e Docker Compose instalados.
2. Na raiz do projeto (onde está o arquivo `docker-compose.yml`), abra o terminal e execute:
   ```bash
   docker-compose up --build
   
3. Aguarde o sistema iniciar. O script de banco (src/main/resources/sql/scripts.sql) será executado automaticamente pelo container na primeira execução.
4. Quando aparecer Javalin started, acesse: http://localhost:8000

### 🔧 Opção 2: Execução Manual (Sem Docker)
Caso prefira rodar localmente configurando o banco manualmente, siga os passos:

### Banco de Dados:

1. Tenha o PostgreSQL instalado e rodando.

2. Crie um banco de dados chamado ecommerce.

3. Localize o script SQL no projeto em: src/main/resources/sql/scripts.sql.

4. Abra o terminal e execute o script no seu banco para criar as tabelas products e itens_carrinho.

### Configuração: 

1. Copie o arquivo de exemplo:
    ```bash
    cp src/main/resources/application.properties.exemplo src/main/resources/application.properties
    ```

3. Edite o arquivo application.properties com seu usuário e senha do banco local.

### Execução:
```bash
      mvn clean install
      java -jar target/ecommerce-1.0-SNAPSHOT.jar
```

**Acesse**: http://localhost:8000

## 🏛️ Arquitetura do Sistema
O projeto segue estritamente o padrão MVC (Model-View-Controller) com Injeção de Dependência manual:

* **Controller (/controllers)**: Gerencia as requisições HTTP e decide qual template renderizar. Não acessa o banco diretamente.

* **Service (/services)**: Contém a regra de negócio. Exemplo: CarrinhoService gerencia a lógica de mesclar carrinhos da sessão com o banco.

* **Repository (/repository)**: Responsável único pelo acesso a dados (SQL puro via JDBC).

* **Model (/model)**: Classes POJO que representam as entidades do sistema.

## 🧪 Usuários de Teste
#### O sistema carrega usuários pré-definidos do arquivo CSV (src/main/resources/csv/Usuario.csv), mas novos podem ser cadastrados via interface.

* **Login Admin**: vh@teste.com
* **Senha: 123456**