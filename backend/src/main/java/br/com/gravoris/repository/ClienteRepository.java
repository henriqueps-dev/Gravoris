package br.com.gravoris.repository;

import br.com.gravoris.model.Cliente;
import br.com.gravoris.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
    Optional<Cliente> findByEmailIgnoreCase(String email);
    Optional<Cliente> findByCpf(String cpf);

    long countByRole(Role role);
    long countByAtivoTrue();
    boolean existsByRole(Role role);
    long countByRoleAndAtivoTrue(Role role);

    @Query("SELECT c FROM Cliente c WHERE (LOWER(c.nome) LIKE LOWER(CONCAT('%', :term, '%')) "
            + "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%')) OR c.cpf LIKE CONCAT('%', :term, '%')) "
            + "ORDER BY c.id DESC")
    List<Cliente> searchAdmin(@Param("term") String term);
}
