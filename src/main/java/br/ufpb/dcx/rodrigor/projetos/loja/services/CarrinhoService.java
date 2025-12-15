package br.ufpb.dcx.rodrigor.projetos.loja.services;

import br.ufpb.dcx.rodrigor.projetos.login.Usuario;
import br.ufpb.dcx.rodrigor.projetos.loja.model.Carrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.model.ItemCarrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.repositories.CarrinhoRepository;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Serviço responsável pela lógica de negócio do Carrinho de Compras.
 * Gerencia a persistência, recuperação e fusão de carrinhos entre sessão e banco de dados.
 */

public class CarrinhoService {
    private static final Logger logger = LogManager.getLogger(CarrinhoService.class);

    private final CarrinhoRepository carrinhoRepository;
    private final ProductService productService;

    public CarrinhoService(ProductService productService) {
        this.carrinhoRepository = new CarrinhoRepository();
        this.productService = productService;
    }

    /**
     * Recupera o carrinho salvo no banco de dados para um usuário.
     * Útil ao fazer login ou recarregar a página.
     *
     * @param usuario O usuário logado.
     * @return Um objeto Carrinho preenchido com os itens do banco, ou vazio se o usuário for nulo.
     */
    public Carrinho carregarCarrinhoUsuario(Usuario usuario) {
        if (usuario == null) return new Carrinho();
        return carrinhoRepository.carregarCarrinho(usuario.getId(), productService);
    }

    /**
     * Funde os itens do carrinho da sessão (anônimo) com o carrinho do banco (logado).
     * <p>
     * Lógica:
     * 1. Carrega o carrinho antigo do banco.
     * 2. Itera sobre os itens da sessão.
     * 3. Soma quantidades se o item já existir.
     * 4. Salva o resultado final no banco.
     * </p>
     *
     * @param usuario O usuário que acabou de fazer login.
     * @param carrinhoSessao O carrinho temporário que estava na memória.
     * @return O carrinho consolidado contendo todos os itens.
     */
    public Carrinho mesclarCarrinhos(Usuario usuario, Carrinho carrinhoSessao) {
        Carrinho carrinhoBanco = carregarCarrinhoUsuario(usuario);

        if (carrinhoSessao != null && !carrinhoSessao.getItens().isEmpty()) {
            logger.info("Mesclando itens da sessão para a conta de: {}", usuario.getLogin());
            for (ItemCarrinho itemSessao : carrinhoSessao.getItens()) {
                carrinhoBanco.adicionarItem(itemSessao.getProduto(), itemSessao.getQuantidade());
                int produtoId = itemSessao.getProduto().getId();

                // Recalcula total e salva
                int novaQuantidadeTotal = carrinhoBanco.getItens().stream()
                        .filter(i -> i.getProduto().getId() == produtoId)
                        .findFirst()
                        .map(ItemCarrinho::getQuantidade)
                        .orElse(itemSessao.getQuantidade());

                atualizarItem(usuario, produtoId, novaQuantidadeTotal);
            }
        }
        return carrinhoBanco;
    }

    /**
     * Atualiza a quantidade de um item no banco de dados.
     * Se a nova quantidade for menor ou igual a zero, o item é removido automaticamente.
     *
     * @param usuario O dono do carrinho.
     * @param produtoId O ID do produto a ser atualizado.
     * @param novaQuantidade A nova quantidade desejada.
     */
    public void atualizarItem(Usuario usuario, int produtoId, int novaQuantidade) {
        if (usuario == null) return;
        if (novaQuantidade > 0) {
            carrinhoRepository.salvarItem(usuario.getId(), produtoId, novaQuantidade);
        } else {
            removerItem(usuario, produtoId);
        }
    }

    /**
     * Remove um item do carrinho no banco de dados e gera um log de auditoria.
     *
     * @param usuario O usuário realizando a ação.
     * @param produtoId O ID do produto a ser removido.
     */
    public void removerItem(Usuario usuario, int produtoId) {
        if (usuario != null) {
            carrinhoRepository.removerItem(usuario.getId(), produtoId);
            logger.info("Produto ID {} removido do carrinho do usuário {}", produtoId, usuario.getLogin());
        }
    }

    /**
     * Verifica a integridade do carrinho removendo itens cujos produtos foram excluídos da loja.
     * Evita erros de "NullPointerException" ao tentar exibir produtos que não existem mais.
     *
     * @param carrinho O carrinho a ser validado.
     */
    public void validarItensDoCarrinho(Carrinho carrinho) {

        boolean houveRemocao = carrinho.getItens().removeIf(item -> {
            try {
                return productService.buscarProduto(item.getProduto().getId()) == null;
            } catch (Exception e) {
                return true;
            }
        });
        if (houveRemocao) {
            logger.warn("Itens inválidos foram removidos automaticamente do carrinho.");
        }
    }

    /**
     * Altera a quantidade de um item no carrinho (Aumenta ou Diminui).
     * Atualiza tanto o objeto em memória quanto o banco de dados (se logado).
     *
     * @param usuario Usuário logado (pode ser null).
     * @param carrinho O objeto Carrinho da sessão.
     * @param produtoId O ID do produto a alterar.
     * @param delta A variação (+1 ou -1).
     */
    public void alterarQuantidade(Usuario usuario, Carrinho carrinho, int produtoId, int delta) {

        if (delta > 0) {
            var produto = productService.buscarProduto(produtoId);
            if (produto != null) {
                carrinho.adicionarItem(produto, delta);
            }
        } else {
            carrinho.diminuirItem(produtoId);
        }

        if (usuario != null) {
            int novaQuantidade = carrinho.getItens().stream()
                    .filter(i -> i.getProduto().getId() == produtoId)
                    .findFirst()
                    .map(ItemCarrinho::getQuantidade)
                    .orElse(0);
            atualizarItem(usuario, produtoId, novaQuantidade);
        }
    }
}