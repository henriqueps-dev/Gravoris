package br.com.gravoris.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa o usuário da loja (cliente ou administrador).
 * A camada de modelo encapsula os dados persistidos no banco.
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String telefone;

    @Column(nullable = false, unique = true)
    private String cpf;

    private LocalDate dataNascimento;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    /** Papel de acesso. Registros antigos sem valor são tratados como CLIENTE. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Role role = Role.CLIENTE;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    public Role getRole() {
        return role != null ? role : Role.CLIENTE;
    }

    public boolean isAdministrador() {
        return getRole() == Role.ADMINISTRADOR;
    }

    @PrePersist
    void prePersist() {
        if (role == null) role = Role.CLIENTE;
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }
}
