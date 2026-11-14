package br.com.gravoris.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Migração única dos dados do MySQL legado para o Supabase (PostgreSQL).
 *
 * <p>Uso (com o schema do Supabase já criado e as tabelas vazias):</p>
 * <pre>
 *   MIGRATION_ENABLED=true
 *   MIGRATION_SOURCE_URL=jdbc:mysql://localhost:3306/gravoris_db
 *   MIGRATION_SOURCE_USER=root
 *   MIGRATION_SOURCE_PASSWORD=...
 *   + variáveis do perfil prod (DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, JWT_SECRET)
 *   java -jar backend.jar --spring.profiles.active=prod
 * </pre>
 *
 * <p>Garantias: o MySQL é apenas LIDO (nada é alterado ou apagado nele); os IDs originais
 * são preservados, então todos os relacionamentos continuam válidos; tudo acontece em uma
 * única transação no destino (se algo falhar, nada é gravado); as contagens de origem e
 * destino são conferidas antes do commit. Ao final, a aplicação encerra.</p>
 */
@Component
@Order(0)
@ConditionalOnProperty(name = "app.migration.enabled", havingValue = "true")
public class MysqlParaSupabaseMigrator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MysqlParaSupabaseMigrator.class);
    private static final List<String> TABELAS = List.of("clientes", "produtos", "pedidos", "item_pedido");

    private final JdbcTemplate destino;
    private final TransactionTemplate transacao;
    private final ConfigurableApplicationContext context;
    private final String origemUrl;
    private final String origemUsuario;
    private final String origemSenha;

    public MysqlParaSupabaseMigrator(DataSource dataSource, TransactionTemplate transacao,
                                     ConfigurableApplicationContext context,
                                     @Value("${app.migration.source.url:}") String origemUrl,
                                     @Value("${app.migration.source.username:}") String origemUsuario,
                                     @Value("${app.migration.source.password:}") String origemSenha) {
        this.destino = new JdbcTemplate(dataSource);
        this.transacao = transacao;
        this.context = context;
        this.origemUrl = origemUrl;
        this.origemUsuario = origemUsuario;
        this.origemSenha = origemSenha;
    }

    @Override
    public void run(ApplicationArguments args) {
        int codigoSaida = 0;
        try {
            migrar();
        } catch (RuntimeException e) {
            log.error("MIGRAÇÃO FALHOU — nenhum dado foi gravado no destino: {}", e.getMessage(), e);
            codigoSaida = 1;
        }
        int codigo = codigoSaida;
        System.exit(SpringApplication.exit(context, () -> codigo));
    }

    private void migrar() {
        if (origemUrl.isBlank()) {
            throw new IllegalStateException("Defina MIGRATION_SOURCE_URL com a URL JDBC do MySQL de origem");
        }
        DataSource ds = DataSourceBuilder.create()
                .url(origemUrl).username(origemUsuario).password(origemSenha)
                .build();
        JdbcTemplate origem = new JdbcTemplate(ds);

        for (String tabela : TABELAS) {
            Long qtd = destino.queryForObject("select count(*) from " + tabela, Long.class);
            if (qtd != null && qtd > 0) {
                throw new IllegalStateException("A tabela de destino '" + tabela + "' não está vazia (" + qtd
                        + " registros). A migração só roda em um banco novo, para não misturar dados.");
            }
        }

        Map<String, Boolean> colunasOrigem = Map.of(
                "clientes.role", existeColuna(origem, "clientes", "role"),
                "clientes.criado_em", existeColuna(origem, "clientes", "criado_em"),
                "produtos.status", existeColuna(origem, "produtos", "status"),
                "produtos.criado_em", existeColuna(origem, "produtos", "criado_em"));

        transacao.executeWithoutResult(status -> {
            LocalDateTime agora = LocalDateTime.now();

            List<Map<String, Object>> clientes = origem.queryForList("select * from clientes order by id");
            for (Map<String, Object> c : clientes) {
                String role = colunasOrigem.get("clientes.role") && c.get("role") != null
                        ? c.get("role").toString() : "CLIENTE";
                destino.update("""
                        insert into clientes (id, nome, email, senha, telefone, cpf, data_nascimento, ativo, role, criado_em)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                        c.get("id"), c.get("nome"), c.get("email"), c.get("senha"), c.get("telefone"),
                        c.get("cpf"), c.get("data_nascimento"), bool(c.get("ativo")), role,
                        ts(colunasOrigem.get("clientes.criado_em") ? c.get("criado_em") : null, agora));
            }

            List<Map<String, Object>> produtos = origem.queryForList("select * from produtos order by id");
            for (Map<String, Object> p : produtos) {
                boolean ativo = bool(p.get("ativo"));
                String st = colunasOrigem.get("produtos.status") && p.get("status") != null
                        ? p.get("status").toString() : (ativo ? "APROVADO" : "DESATIVADO");
                ativo = "APROVADO".equals(st);
                Timestamp criado = ts(colunasOrigem.get("produtos.criado_em") ? p.get("criado_em") : null, agora);
                destino.update("""
                        insert into produtos (id, nome, marca, modelo, preco, descricao, imagem_url, ano, quilometragem,
                            categoria, estoque, motor, potencia, torque, velocidade_max, aceleracao, ativo, status,
                            criado_em, atualizado_em)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                        p.get("id"), p.get("nome"), p.get("marca"), p.get("modelo"), p.get("preco"),
                        p.get("descricao"), p.get("imagem_url"), p.get("ano"), p.get("quilometragem"),
                        p.get("categoria"), p.get("estoque"), p.get("motor"), p.get("potencia"), p.get("torque"),
                        p.get("velocidade_max"), p.get("aceleracao"), ativo, st, criado, criado);
            }

            List<Map<String, Object>> pedidos = origem.queryForList("select * from pedidos order by id");
            for (Map<String, Object> p : pedidos) {
                destino.update("insert into pedidos (id, cliente_id, data_pedido, total, status) values (?, ?, ?, ?, ?)",
                        p.get("id"), p.get("cliente_id"), p.get("data_pedido"), p.get("total"), p.get("status"));
            }

            List<Map<String, Object>> itens = origem.queryForList("select * from item_pedido order by id");
            for (Map<String, Object> i : itens) {
                destino.update("""
                        insert into item_pedido (id, pedido_id, produto_id, quantidade, preco_unitario)
                        values (?, ?, ?, ?, ?)""",
                        i.get("id"), i.get("pedido_id"), i.get("produto_id"), i.get("quantidade"), i.get("preco_unitario"));
            }

            // Ajusta as sequências de identidade para continuar depois do maior ID migrado
            for (String tabela : TABELAS) {
                destino.queryForObject("select setval(pg_get_serial_sequence('" + tabela + "', 'id'), "
                        + "coalesce((select max(id) from " + tabela + "), 0) + 1, false)", Long.class);
            }

            // Conferência: mesma quantidade de registros na origem e no destino
            StringBuilder relatorio = new StringBuilder("\n=== Resultado da migração MySQL -> Supabase ===\n");
            for (String tabela : TABELAS) {
                Long qOrigem = origem.queryForObject("select count(*) from " + tabela, Long.class);
                Long qDestino = destino.queryForObject("select count(*) from " + tabela, Long.class);
                relatorio.append(String.format("  %-12s origem=%-5d destino=%-5d %s%n", tabela, qOrigem, qDestino,
                        qOrigem.equals(qDestino) ? "OK" : "DIVERGENTE"));
                if (!qOrigem.equals(qDestino)) {
                    throw new IllegalStateException("Contagem divergente em " + tabela);
                }
            }
            Long orfaos = destino.queryForObject("""
                    select (select count(*) from pedidos p left join clientes c on c.id = p.cliente_id where c.id is null)
                         + (select count(*) from item_pedido i left join produtos p on p.id = i.produto_id where p.id is null)
                    """, Long.class);
            relatorio.append("  registros órfãos: ").append(orfaos).append('\n');
            log.info(relatorio.toString());
        });

        log.info("MIGRAÇÃO CONCLUÍDA. O banco MySQL de origem não foi alterado. "
                + "Remova MIGRATION_ENABLED do ambiente antes de iniciar o servidor normalmente.");
    }

    private static boolean existeColuna(JdbcTemplate jdbc, String tabela, String coluna) {
        Integer n = jdbc.queryForObject(
                "select count(*) from information_schema.columns where table_schema = database() "
                        + "and table_name = ? and column_name = ?", Integer.class, tabela, coluna);
        return n != null && n > 0;
    }

    private static boolean bool(Object valor) {
        if (valor instanceof Boolean b) return b;
        if (valor instanceof Number n) return n.intValue() != 0;
        if (valor instanceof byte[] bytes) return bytes.length > 0 && bytes[0] != 0;
        return valor != null && Boolean.parseBoolean(valor.toString());
    }

    private static Timestamp ts(Object valor, LocalDateTime padrao) {
        if (valor instanceof Timestamp t) return t;
        if (valor instanceof LocalDateTime l) return Timestamp.valueOf(l);
        return Timestamp.valueOf(padrao);
    }
}
