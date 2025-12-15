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

    public Carrinho carregarCarrinhoUsuario(Usuario usuario) {
        if (usuario == null) return new Carrinho();
        return carrinhoRepository.carregarCarrinho(usuario.getId(), productService);
    }

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

    public void atualizarItem(Usuario usuario, int produtoId, int novaQuantidade) {
        if (usuario == null) return;
        if (novaQuantidade > 0) {
            carrinhoRepository.salvarItem(usuario.getId(), produtoId, novaQuantidade);
        } else {
            removerItem(usuario, produtoId);
        }
    }

    public void removerItem(Usuario usuario, int produtoId) {
        if (usuario != null) {
            carrinhoRepository.removerItem(usuario.getId(), produtoId);
            logger.info("Produto ID {} removido do carrinho do usuário {}", produtoId, usuario.getLogin());
        }
    }

    public void validarItensDoCarrinho(Carrinho carrinho) {
        // Remove itens que não existem mais no banco de produtos (órfãos)
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
}