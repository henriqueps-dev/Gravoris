package br.com.gravoris.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa o produto/veículo anunciado para venda.
 *
 * <p>{@code status} controla o ciclo do anúncio (pendente, aprovado, desativado) e
 * {@code ativo} indica se ele aparece na vitrine. Os dois são mantidos sincronizados
 * aqui, num único lugar: ativo = (status == APROVADO). Registros antigos que só têm
 * {@code ativo} recebem o status equivalente ao serem carregados.</p>
 */
@Entity
@Table(name = "produtos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem_url", length = 1000)
    private String imagemUrl;

    private int ano;

    private int quilometragem;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    private int estoque;

    private String motor;

    private int potencia;

    private String torque;

    private int velocidadeMax;

    private String aceleracao;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusAnuncio status;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public StatusAnuncio getStatus() {
        if (status == null) {
            return ativo ? StatusAnuncio.APROVADO : StatusAnuncio.DESATIVADO;
        }
        return status;
    }

    public void setStatus(StatusAnuncio status) {
        this.status = status;
        if (status != null) {
            this.ativo = status == StatusAnuncio.APROVADO;
        }
    }

    @PrePersist
    void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        if (criadoEm == null) criadoEm = agora;
        atualizadoEm = agora;
        sincronizarStatus();
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
        sincronizarStatus();
    }

    private void sincronizarStatus() {
        setStatus(getStatus());
    }
}
