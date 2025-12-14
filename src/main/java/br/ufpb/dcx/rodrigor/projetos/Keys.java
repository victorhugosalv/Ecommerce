package br.ufpb.dcx.rodrigor.projetos;

import br.ufpb.dcx.rodrigor.projetos.form.services.FormService;
import br.ufpb.dcx.rodrigor.projetos.login.UsuarioService;
import br.ufpb.dcx.rodrigor.projetos.loja.services.CarrinhoService;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import io.javalin.config.Key;

public enum Keys {
    FORM_SERVICE(new Key<FormService>("form-service")),
    USUARIO_SERVICE(new Key<UsuarioService>("usuario-service")),
    PRODUCT_SERVICE(new Key<ProductService>("product-service")),
    CARRINHO_SERVICE(new Key<CarrinhoService>("carrinho-service"));

    private final Key<?> k;

    <T> Keys(Key<T> key) {
        this.k = key;
    }

    public <T> Key<T> key() {
        @SuppressWarnings("unchecked")
        Key<T> typedKey = (Key<T>) this.k;
        return typedKey;
    }
}