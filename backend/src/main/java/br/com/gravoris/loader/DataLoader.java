package br.com.gravoris.loader;

import br.com.gravoris.model.Categoria;
import br.com.gravoris.model.Produto;
import br.com.gravoris.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * DataLoader insere o catálogo de superesportivos de luxo ao iniciar a aplicação.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final ProdutoRepository produtoRepository;

    public DataLoader(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public void run(String... args) {
        if (produtoRepository.existsByMarca("Bugatti")) {
            return;
        }

        produtoRepository.deleteAll();

        List<Produto> produtos = List.of(
                luxury("Chiron Super Sport", "Bugatti", "Super Sport",
                        "12500000", "W16 8.0L Quad-Turbo", 1600, "1600 Nm", 440, "2.3s",
                        "photo-1618843479313-40f8afb4b4d8", 2024,
                        "O ápice da engenharia francesa. O Chiron Super Sport combina artesanato exclusivo com performance absoluta."),
                luxury("W16 Mistral", "Bugatti", "Mistral",
                        "14000000", "W16 8.0L Quad-Turbo", 1578, "1600 Nm", 420, "2.4s",
                        "photo-1583121274602-3ceb120395a5", 2023,
                        "Roadster definitivo da Bugatti. Design aerodinâmico esculpido pelo vento com o icônico motor W16."),
                luxury("P1", "McLaren", "P1",
                        "8500000", "V8 3.8L Bi-Turbo Híbrido", 916, "900 Nm", 350, "2.8s",
                        "photo-1621138807254-6e1cb1b5e0e0", 2015,
                        "Hypercar híbrido que redefiniu limites. Carbono MonoCage e aerodinâmica ativa."),
                luxury("765LT", "McLaren", "765LT",
                        "3200000", "V8 4.0L Twin-Turbo", 765, "800 Nm", 330, "2.7s",
                        "photo-1544636331-e26879cd4d6b", 2024,
                        "Longtail moderno com peso reduzido e downforce ampliado."),
                luxury("SF90 Stradale", "Ferrari", "SF90",
                        "4500000", "V8 4.0L Bi-Turbo + Elétrico", 1000, "800 Nm", 340, "2.5s",
                        "photo-1580274454331-22ca8d787f03", 2024,
                        "Primeiro Ferrari plug-in hybrid. Três motores elétricos e V8 twin-turbo."),
                luxury("Daytona SP3", "Ferrari", "Daytona SP3",
                        "12000000", "V12 6.5L Atmosférico", 840, "697 Nm", 340, "2.85s",
                        "photo-1563720223185-11003d516935", 2023,
                        "Ícone Icona limitado. Design nostálgico dos anos 60 com V12 atmosférico."),
                luxury("Revuelto", "Lamborghini", "Revuelto",
                        "5200000", "V12 6.5L Híbrido", 1015, "725 Nm", 350, "2.5s",
                        "photo-1544636331-e26879cd4d6b", 2024,
                        "Sucessor espiritual do Aventador. V12 híbrido e três motores elétricos."),
                luxury("Huracán STO", "Lamborghini", "Huracán STO",
                        "2800000", "V10 5.2L Atmosférico", 640, "565 Nm", 310, "3.0s",
                        "photo-1542362567-b07e543c53a2", 2023,
                        "Homologado para rua com DNA de Super Trofeo."),
                luxury("Jesko Absolut", "Koenigsegg", "Jesko Absolut",
                        "11000000", "V8 5.0L Twin-Turbo", 1600, "1500 Nm", 499, "2.5s",
                        "photo-1494976388531-d1058494cdd8", 2024,
                        "Projetado para quebrar recordes de velocidade. Transmissão Light Speed."),
                luxury("Gemera", "Koenigsegg", "Gemera",
                        "9500000", "V8 3.0L Freevalve + Elétrico", 1700, "3500 Nm", 400, "1.9s",
                        "photo-1503376780353-7e6692767b70", 2023,
                        "Mega-GT de quatro lugares com motor Freevalve e torque colossal."),
                luxury("911 GT3 RS", "Porsche", "GT3 RS",
                        "2100000", "Flat-6 4.0L Atmosférico", 525, "465 Nm", 296, "3.2s",
                        "photo-1503376780353-7e6692767b70", 2024,
                        "Referência em precisão alemã. DRS, asa ativa e motor aspirado até 9000 rpm."),
                luxury("Taycan Turbo S", "Porsche", "Taycan Turbo S",
                        "980000", "Dual Motor Elétrico", 761, "1050 Nm", 260, "2.8s",
                        "photo-1614162692292-7a56aaaabe84", 2024,
                        "Elétrico com alma Porsche. Chassi baixo e recarga 800V."),
                luxury("M8 Competition", "BMW", "M8 Competition",
                        "890000", "V8 4.4L Twin-Turbo", 625, "750 Nm", 305, "3.2s",
                        "photo-1555215695-3004980ad54e", 2024,
                        "Grand tourer M com luxo e potência. Interior artesanal e xDrive."),
                luxury("M4 CSL", "BMW", "M4 CSL",
                        "1200000", "Inline-6 3.0L Twin-Turbo", 550, "650 Nm", 302, "3.5s",
                        "photo-1617814076367-7594a49d0e33", 2023,
                        "Edição limitada CSL. Peso reduzido e suspensão ajustada para pista."),
                luxury("AMG GT Black Series", "Mercedes-AMG", "GT Black Series",
                        "3500000", "V8 4.0L Twin-Turbo", 730, "800 Nm", 325, "3.2s",
                        "photo-1618843479313-40f8afb4b4d8", 2023,
                        "O AMG mais extremo já produzido. Aerodinâmica de F1 e V8 biturbo."),
                luxury("AMG ONE", "Mercedes-AMG", "AMG ONE",
                        "15000000", "V6 1.6L Turbo Híbrido F1", 1063, "—", 352, "2.9s",
                        "photo-1617814076367-7594a49d0e33", 2024,
                        "Tecnologia de Fórmula 1 homologada para rua."),
                luxury("R8 V10 Performance", "Audi", "R8 V10",
                        "1450000", "V10 5.2L FSI", 620, "570 Nm", 331, "3.1s",
                        "photo-1606664515524-ed2f786a0bd6", 2024,
                        "Última geração do V10 aspirado da Audi com Quattro permanente."),
                luxury("e-tron GT RS", "Audi", "e-tron GT RS",
                        "780000", "Dual Motor Elétrico", 646, "830 Nm", 250, "3.3s",
                        "photo-1614162692292-7a56aaaabe84", 2024,
                        "Gran Turismo elétrico com silhueta esculpida e interior minimalista."),
                luxury("Phantom Extended", "Rolls-Royce", "Phantom Extended",
                        "6800000", "V12 6.75L Twin-Turbo", 571, "900 Nm", 250, "5.3s",
                        "photo-1563720223185-11003d516935", 2024,
                        "O pináculo do luxo automotivo. Starlight Headliner e artesanato bespoke."),
                luxury("Cullinan Black Badge", "Rolls-Royce", "Cullinan Black Badge",
                        "5200000", "V12 6.75L Twin-Turbo", 600, "900 Nm", 250, "5.0s",
                        "photo-1631296337651-6e9a0e2b0a0e", 2023,
                        "SUV ultra-luxo com espírito Black Badge.", Categoria.SUV),
                luxury("Valkyrie", "Aston Martin", "Valkyrie",
                        "18000000", "V12 6.5L + Híbrido Cosworth", 1160, "900 Nm", 350, "2.5s",
                        "photo-1542362567-b07e543c53a2", 2024,
                        "Co-criado com Red Bull Racing. Aerodinâmica ground-effect."),
                luxury("DBS Superleggera", "Aston Martin", "DBS Superleggera",
                        "2900000", "V12 5.2L Twin-Turbo", 725, "900 Nm", 340, "3.4s",
                        "photo-1606664515524-ed2f786a0bd6", 2023,
                        "GT britânico com alma de James Bond. Superleggera em carbono."),
                luxury("Huayra BC", "Pagani", "Huayra BC",
                        "14000000", "V12 6.0L Twin-Turbo AMG", 800, "1100 Nm", 383, "2.8s",
                        "photo-1492144534655-ae79c964c9d7", 2023,
                        "Obra de arte sobre rodas. Carroceria em titânio e carbono."),
                luxury("Utopia", "Pagani", "Utopia",
                        "16000000", "V12 6.0L Twin-Turbo AMG", 864, "1100 Nm", 350, "2.8s",
                        "photo-1494976388531-d1058494cdd8", 2024,
                        "A nova era Pagani. Design atemporal e artesanato italiano.")
        );

        produtoRepository.saveAll(produtos);
    }

    private Produto luxury(String nome, String marca, String modelo, String preco,
                           String motor, int potencia, String torque, int velocidadeMax,
                           String aceleracao, String imagemId, int ano, String descricao) {
        return luxury(nome, marca, modelo, preco, motor, potencia, torque, velocidadeMax,
                aceleracao, imagemId, ano, descricao, Categoria.ESPORTIVO);
    }

    private Produto luxury(String nome, String marca, String modelo, String preco,
                           String motor, int potencia, String torque, int velocidadeMax,
                           String aceleracao, String imagemId, int ano, String descricao,
                           Categoria categoria) {
        String imagemUrl = "https://images.unsplash.com/" + imagemId + "?auto=format&fit=crop&w=1200&q=80";
        return Produto.builder()
                .nome(nome)
                .marca(marca)
                .modelo(modelo)
                .preco(new BigDecimal(preco))
                .descricao(descricao)
                .imagemUrl(imagemUrl)
                .ano(ano)
                .quilometragem(0)
                .categoria(categoria)
                .estoque(2)
                .motor(motor)
                .potencia(potencia)
                .torque(torque)
                .velocidadeMax(velocidadeMax)
                .aceleracao(aceleracao)
                .ativo(true)
                .build();
    }
}
