package br.ufpb.dcx.rodrigor.projetos.loja.services;

import br.ufpb.dcx.rodrigor.projetos.login.Usuario;
import br.ufpb.dcx.rodrigor.projetos.loja.model.Carrinho;
import br.ufpb.dcx.rodrigor.projetos.loja.model.ItemCarrinho;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CarrinhoService {
    private static final Logger logger = LogManager.getLogger(CarrinhoService.class);

    private final CarrinhoRepository carrinhoRepository;
    private final ProductService productService;

    // Injeção de dependência: O Service precisa do Repository e do ProductService
    public CarrinhoService(ProductService productService) {
        this.carrinhoRepository = new CarrinhoRepository();
        this.productService = productService;
    }

    // Regra de Negócio 1: Carregar carrinho do banco
    public Carrinho carregarCarrinhoUsuario(Usuario usuario) {
        if (usuario == null) return new Carrinho();
        return carrinhoRepository.carregarCarrinho(usuario.getId(), productService);
    }

    // Regra de Negócio 2: Mesclar Carrinho da Sessão (Anônimo) com o do Banco (Login)
    public Carrinho mesclarCarrinhos(Usuario usuario, Carrinho carrinhoSessao) {
        // 1. Carrega o que já está salvo no banco
        Carrinho carrinhoBanco = carregarCarrinhoUsuario(usuario);

        // 2. Se tiver coisas na sessão, joga pro banco
        if (carrinhoSessao != null && !carrinhoSessao.getItens().isEmpty()) {
            logger.info("Mesclando carrinho da sessão com o do usuário {}", usuario.getLogin());

            for (ItemCarrinho item : carrinhoSessao.getItens()) {
                // Atualiza o objeto em memória
                carrinhoBanco.adicionarItem(item.getProduto(), item.getQuantidade());
                // Persiste no banco imediatamente
                carrinhoRepository.salvarItem(usuario.getId(), item.getProduto().getId(), item.getQuantidade());
            }
        }
        return carrinhoBanco;
    }

    // Regra de Negócio 3: Atualizar item (Salvar ou Remover dependendo da qtd)
    public void atualizarItem(Usuario usuario, int produtoId, int novaQuantidade) {
        if (usuario == null) return; // Se não tem usuário, não salva no banco (só sessão)

        if (novaQuantidade > 0) {
            carrinhoRepository.salvarItem(usuario.getId(), produtoId, novaQuantidade);
        } else {
            carrinhoRepository.removerItem(usuario.getId(), produtoId);
        }
    }

    // Regra de Negócio 4: Limpar carrinho do usuário (ex: após finalizar compra)
    public void limparCarrinho(Usuario usuario) {
        if (usuario != null) {
            // Implementar método no repositório se necessário, ou remover um a um
            // carrinhoRepository.limparTudo(usuario.getId());
        }
    }
}
