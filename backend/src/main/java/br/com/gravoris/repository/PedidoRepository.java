package br.com.gravoris.repository;

import br.com.gravoris.model.Pedido;
import br.com.gravoris.model.StatusPedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.status <> :excluido")
    BigDecimal somarTotalExceto(@Param("excluido") StatusPedido excluido);

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente ORDER BY p.dataPedido DESC")
    List<Pedido> findRecentes(Pageable pageable);

    @Query("SELECT COUNT(i) > 0 FROM ItemPedido i WHERE i.produto.id = :produtoId")
    boolean existsItemComProduto(@Param("produtoId") Long produtoId);
}
