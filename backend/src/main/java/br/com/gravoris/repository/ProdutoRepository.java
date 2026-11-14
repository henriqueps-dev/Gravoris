package br.com.gravoris.repository;

import br.com.gravoris.model.Produto;
import br.com.gravoris.model.StatusAnuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrue();

    List<Produto> findByAtivoTrueOrderByIdDesc();

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND (LOWER(p.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Produto> searchByNomeOrMarca(@Param("term") String term);

    boolean existsByMarca(String marca);

    long countByAtivoTrue();

    long countByStatus(StatusAnuncio status);

    @Query("SELECT COALESCE(SUM(p.estoque), 0) FROM Produto p WHERE p.ativo = true")
    long somarEstoqueAtivo();

    @Query("SELECT p FROM Produto p WHERE (LOWER(p.nome) LIKE LOWER(CONCAT('%', :term, '%')) "
            + "OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(p.modelo) LIKE LOWER(CONCAT('%', :term, '%'))) "
            + "ORDER BY p.id DESC")
    List<Produto> searchAdmin(@Param("term") String term);
}
