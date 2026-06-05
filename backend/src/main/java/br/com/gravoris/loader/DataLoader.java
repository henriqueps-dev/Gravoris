package br.com.gravoris.loader;

import br.com.gravoris.model.Categoria;
import br.com.gravoris.model.Produto;
import br.com.gravoris.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * DataLoader insere dados iniciais de produto na base ao iniciar a aplicação.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final ProdutoRepository produtoRepository;

    public DataLoader(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public void run(String... args) {
        if (produtoRepository.count() > 0) {
            return;
        }

        List<Produto> produtos = List.of(
                Produto.builder()
                        .nome("Civic Touring")
                        .marca("Honda")
                        .modelo("Touring")
                        .preco(new BigDecimal("159900.00"))
                        .descricao("Sedan médio com conforto e tecnologia embarcada.")
                        .imagemUrl("https://example.com/images/civic-touring.jpg")
                        .ano(2024)
                        .quilometragem(12000)
                        .categoria(Categoria.SEDAN)
                        .estoque(5)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Corolla Altis")
                        .marca("Toyota")
                        .modelo("Altis")
                        .preco(new BigDecimal("179900.00"))
                        .descricao("Sedan premium com reputação de conforto e economia.")
                        .imagemUrl("https://example.com/images/corolla-altis.jpg")
                        .ano(2024)
                        .quilometragem(8000)
                        .categoria(Categoria.SEDAN)
                        .estoque(7)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Ranger Storm")
                        .marca("Ford")
                        .modelo("Storm")
                        .preco(new BigDecimal("219900.00"))
                        .descricao("Pickup robusta para estrada e trabalho pesado.")
                        .imagemUrl("https://example.com/images/ranger-storm.jpg")
                        .ano(2023)
                        .quilometragem(21000)
                        .categoria(Categoria.PICKUP)
                        .estoque(6)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Tucson Elite")
                        .marca("Hyundai")
                        .modelo("Elite")
                        .preco(new BigDecimal("189900.00"))
                        .descricao("SUV compacto com design moderno e tecnologia intuitiva.")
                        .imagemUrl("https://example.com/images/tucson-elite.jpg")
                        .ano(2024)
                        .quilometragem(10500)
                        .categoria(Categoria.SUV)
                        .estoque(8)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Uno Mille")
                        .marca("Fiat")
                        .modelo("Mille")
                        .preco(new BigDecimal("49900.00"))
                        .descricao("Compacto econômico para uso urbano diário.")
                        .imagemUrl("https://example.com/images/uno-mille.jpg")
                        .ano(2018)
                        .quilometragem(65000)
                        .categoria(Categoria.HATCH)
                        .estoque(10)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Golf GTI")
                        .marca("Volkswagen")
                        .modelo("GTI")
                        .preco(new BigDecimal("229900.00"))
                        .descricao("Hatch esportivo com desempenho de pista e acabamento premium.")
                        .imagemUrl("https://example.com/images/golf-gti.jpg")
                        .ano(2023)
                        .quilometragem(15000)
                        .categoria(Categoria.ESPORTIVO)
                        .estoque(4)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Hilux SRX")
                        .marca("Toyota")
                        .modelo("SRX")
                        .preco(new BigDecimal("239900.00"))
                        .descricao("Pickup de luxo com capacidade para longas jornadas.")
                        .imagemUrl("https://example.com/images/hilux-srx.jpg")
                        .ano(2024)
                        .quilometragem(9000)
                        .categoria(Categoria.PICKUP)
                        .estoque(3)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Polo Comfortline")
                        .marca("Volkswagen")
                        .modelo("Comfortline")
                        .preco(new BigDecimal("129900.00"))
                        .descricao("Hatch com boa dirigibilidade e espaço interno para a família.")
                        .imagemUrl("https://example.com/images/polo-comfortline.jpg")
                        .ano(2024)
                        .quilometragem(6000)
                        .categoria(Categoria.HATCH)
                        .estoque(9)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Tiguan R-Line")
                        .marca("Volkswagen")
                        .modelo("R-Line")
                        .preco(new BigDecimal("259900.00"))
                        .descricao("SUV premium com acabamento esportivo e performance refinada.")
                        .imagemUrl("https://example.com/images/tiguan-rline.jpg")
                        .ano(2024)
                        .quilometragem(7000)
                        .categoria(Categoria.SUV)
                        .estoque(5)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Pulse Impetus")
                        .marca("Fiat")
                        .modelo("Impetus")
                        .preco(new BigDecimal("139900.00"))
                        .descricao("SUV compacto com economia e estilo aventureiro.")
                        .imagemUrl("https://example.com/images/pulse-impetus.jpg")
                        .ano(2024)
                        .quilometragem(9500)
                        .categoria(Categoria.SUV)
                        .estoque(6)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Camaro SS")
                        .marca("Chevrolet")
                        .modelo("SS")
                        .preco(new BigDecimal("349900.00"))
                        .descricao("Esportivo americano com motor potente e presença marcante.")
                        .imagemUrl("https://example.com/images/camaro-ss.jpg")
                        .ano(2023)
                        .quilometragem(13000)
                        .categoria(Categoria.ESPORTIVO)
                        .estoque(2)
                        .ativo(true)
                        .build(),
                Produto.builder()
                        .nome("Toro Volcano")
                        .marca("Fiat")
                        .modelo("Volcano")
                        .preco(new BigDecimal("179900.00"))
                        .descricao("Pickup intermediária com conforto interno e capacidade de carga.")
                        .imagemUrl("https://example.com/images/toro-volcano.jpg")
                        .ano(2024)
                        .quilometragem(9000)
                        .categoria(Categoria.PICKUP)
                        .estoque(6)
                        .ativo(true)
                        .build()
        );

        produtoRepository.saveAll(produtos);
    }
}
