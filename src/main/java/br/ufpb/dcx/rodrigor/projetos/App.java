package br.ufpb.dcx.rodrigor.projetos;

import br.ufpb.dcx.rodrigor.projetos.form.controller.FormController;
import br.ufpb.dcx.rodrigor.projetos.form.services.FormService;
import br.ufpb.dcx.rodrigor.projetos.login.LoginController;
import br.ufpb.dcx.rodrigor.projetos.login.UsuarioController;
import br.ufpb.dcx.rodrigor.projetos.login.UsuarioService;
import br.ufpb.dcx.rodrigor.projetos.loja.controllers.LojaController;
import br.ufpb.dcx.rodrigor.projetos.loja.services.CarrinhoService;
import br.ufpb.dcx.rodrigor.projetos.product.controllers.ProductController;
import br.ufpb.dcx.rodrigor.projetos.product.services.ProductService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    private static final int PORTA_PADRAO = 8000;

    //Propriedades do application.properties:
    private static final String PROP_PORTA_SERVIDOR = "porta.servidor";


    private final Properties propriedades;

    public App() {
        this.propriedades = carregarPropriedades();
    }

    public void iniciar() {
        Javalin app = inicializarJavalin();
        configurarPaginasDeErro(app);
        configurarRotas(app);

        // Lidando com exceções não tratadas
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Erro não tratado", e);
            ctx.status(500);
        });
    }

    private void configurarPaginasDeErro(Javalin app) {
        app.error(404, ctx -> ctx.render("erro_404.html"));
        app.error(500, ctx -> ctx.render("erro_500.html"));
    }

    private Javalin inicializarJavalin() {
        int porta = obterPortaServidor();

        logger.info("Iniciando aplicação na porta {}", porta);

        Consumer<JavalinConfig> configConsumer = this::configureJavalin;

        return Javalin.create(configConsumer).start(porta);
    }

    private void configureJavalin(JavalinConfig config) {
        TemplateEngine templateEngine = configurarThymeleaf();

        config.events(event -> {
            event.serverStarting(() -> {
                logger.info("Servidor Javalin está iniciando...");
                registrarServicos(config);
            });
            event.serverStopping(() -> {
            });
        });
        config.staticFiles.add(staticFileConfig -> {
            staticFileConfig.directory = "/public";
            staticFileConfig.location = Location.CLASSPATH;
        });
        config.fileRenderer(new JavalinThymeleaf(templateEngine));

    }

    private void registrarServicos(JavalinConfig config) {
        ProductService productService = new ProductService();
        config.appData(Keys.USUARIO_SERVICE.key(), new UsuarioService());
        config.appData(Keys.FORM_SERVICE.key(), new FormService());
        config.appData(Keys.PRODUCT_SERVICE.key(), new ProductService());
        config.appData(Keys.CARRINHO_SERVICE.key(), new CarrinhoService(productService));
    }


    private void configurarRotas(Javalin app) {

// --- FILTRO DE SEGURANÇA ---
        app.before(ctx -> {
            String path = ctx.path();

            // Verifica se é uma rota protegida
            if (path.startsWith("/products") ||
                    // Protege /usuarios, MAS libera o cadastro (signup) e o salvamento (cadastrar)
                    (path.startsWith("/usuarios") && !path.equals("/usuarios/signup") && !path.equals("/usuarios/cadastrar"))) {

                if (ctx.sessionAttribute("usuario") == null) {
                    ctx.redirect("/login");
                }
            }
        });

        LoginController loginController = new LoginController();
        app.get("/login", loginController::mostrarPaginaLogin);
        app.post("/login", loginController::processarLogin);
        app.get("/logout", loginController::logout);


        // Rotas para o controlador de usuário
        UsuarioController usuarioController = new UsuarioController();
        app.get("/usuarios", usuarioController::listarUsuarios);
        app.get("/usuarios/novo", usuarioController::mostrarFormularioCadastro);
        app.post("/usuarios/cadastrar", usuarioController::cadastrarUsuario);
        app.get("/usuarios/signup", usuarioController::mostrarFormulario_signup);
        app.get("/usuarios/{id}/remover", usuarioController::removerUsuario);

        // Rotas para o controlador de formulários
        FormController formController = new FormController();
        app.get("/form/{formId}", formController::abrirFormulario);
        app.post("/form/{formId}", formController::validarFormulario);

        //Rotas para o controlador de produtos
        ProductController productController = new ProductController();
        productController.registerRoutes(app);

        //Rotas para o controlador da Loja
        LojaController lojaController = new LojaController();

        app.get("/", lojaController::mostrarVitrine);
        app.get("/vitrine", lojaController::mostrarVitrine);

        //Rotas para o carrinho
        app.get("/carrinho", lojaController::verCarrinho);
        app.post("/carrinho/adicionar/{id}", lojaController::adicionarAoCarrinho);
        app.get("/carrinho/aumentar/{id}", lojaController::aumentarQuantidade);
        app.get("/carrinho/diminuir/{id}", lojaController::diminuirQuantidade);
        app.get("/carrinho/remover/{id}", lojaController::removerDoCarrinho);
    }


    private int obterPortaServidor() {
        if (propriedades.containsKey(PROP_PORTA_SERVIDOR)) {
            try {
                return Integer.parseInt(propriedades.getProperty(PROP_PORTA_SERVIDOR));
            } catch (NumberFormatException e) {
                logger.error("Porta definida no arquivo de propriedades não é um número válido: '{}'", propriedades.getProperty(PROP_PORTA_SERVIDOR));
                System.exit(1);
            }
        } else {
            logger.info("Porta não definida no arquivo de propriedades, utilizando porta padrão {}", PORTA_PADRAO);
        }
        return PORTA_PADRAO;
    }

    private TemplateEngine configurarThymeleaf() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

    public static Properties carregarPropriedades() {
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("application.properties")) {
            if(input == null){
                logger.error("Arquivo de propriedades não encontrado");
                System.exit(1);
            }
            prop.load(input);

            String envDbUrl = System.getenv("DB_URL");
            if (envDbUrl != null) {
                prop.setProperty("db.url", envDbUrl);
                logger.info("Docker: Usando URL do banco via env var");
            }

            String envDbUser = System.getenv("DB_USER");
            if (envDbUser != null) {
                prop.setProperty("db.user", envDbUser); // Sobrescreve o valor do arquivo
            }

            String envDbPassword = System.getenv("DB_PASSWORD");
            if (envDbPassword != null) {
                prop.setProperty("db.password", envDbPassword); // Sobrescreve o valor do arquivo
            }

        } catch (IOException ex) {
            logger.error("Erro ao carregar propriedades", ex);
            System.exit(1);
        }
        return prop;
    }

    public static void main(String[] args) {
        try {
            new App().iniciar();
        } catch (Exception e) {
            logger.error("Erro ao iniciar a aplicação", e);
            System.exit(1);
        }
    }
}