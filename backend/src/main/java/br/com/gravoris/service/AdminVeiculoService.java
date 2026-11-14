package br.com.gravoris.service;

import br.com.gravoris.dto.AdminProdutoResponse;
import br.com.gravoris.dto.ProdutoRequest;
import br.com.gravoris.exception.BadRequestException;
import br.com.gravoris.exception.ConflictException;
import br.com.gravoris.exception.ResourceNotFoundException;
import br.com.gravoris.model.Categoria;
import br.com.gravoris.model.Produto;
import br.com.gravoris.model.StatusAnuncio;
import br.com.gravoris.repository.PedidoRepository;
import br.com.gravoris.repository.ProdutoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Gestão de veículos e anúncios pelo painel administrativo.
 */
@Service
public class AdminVeiculoService {

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final StorageService storageService;

    public AdminVeiculoService(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository,
                               StorageService storageService) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<AdminProdutoResponse> listar(String termo, String status) {
        List<Produto> produtos = (termo == null || termo.isBlank())
                ? produtoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                : produtoRepository.searchAdmin(termo.trim());

        StatusAnuncio filtro = (status == null || status.isBlank()) ? null : parseStatus(status);
        return produtos.stream()
                .filter(p -> filtro == null || p.getStatus() == filtro)
                .map(AdminVeiculoService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProdutoResponse buscar(Long id) {
        return toResponse(encontrar(id));
    }

    @Transactional
    public AdminProdutoResponse criar(ProdutoRequest request) {
        Produto produto = new Produto();
        aplicar(produto, request);
        produto.setStatus(request.status() == null || request.status().isBlank()
                ? StatusAnuncio.APROVADO : parseStatus(request.status()));
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public AdminProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = encontrar(id);
        aplicar(produto, request);
        if (request.status() != null && !request.status().isBlank()) {
            produto.setStatus(parseStatus(request.status()));
        }
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public AdminProdutoResponse alterarStatus(Long id, String status) {
        Produto produto = encontrar(id);
        produto.setStatus(parseStatus(status));
        return toResponse(produtoRepository.save(produto));
    }

    /**
     * Exclui o veículo. Veículos que já constam em pedidos não podem ser apagados
     * (o histórico do cliente seria perdido); nesse caso o anúncio deve ser desativado.
     */
    @Transactional
    public void excluir(Long id) {
        Produto produto = encontrar(id);
        if (pedidoRepository.existsItemComProduto(id)) {
            throw new ConflictException("Este veículo já possui pedidos. Desative o anúncio em vez de excluí-lo.");
        }
        String imagem = produto.getImagemUrl();
        produtoRepository.delete(produto);
        produtoRepository.flush();
        storageService.excluirSeForDoBucket(imagem);
    }

    private Produto encontrar(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));
    }

    private void aplicar(Produto produto, ProdutoRequest r) {
        produto.setNome(r.name().trim());
        produto.setMarca(r.brand().trim());
        produto.setModelo(r.model().trim());
        produto.setPreco(r.price());
        produto.setImagemUrl(validarUrlImagem(r.imageUrl()));
        produto.setDescricao(blankToNull(r.description()));
        produto.setAno(r.year());
        produto.setQuilometragem(zeroSeNulo(r.mileage()));
        produto.setCategoria(parseCategoria(r.category()));
        produto.setEstoque(zeroSeNulo(r.stock()));
        produto.setMotor(blankToNull(r.engine()));
        produto.setPotencia(zeroSeNulo(r.power()));
        produto.setTorque(blankToNull(r.torque()));
        produto.setVelocidadeMax(zeroSeNulo(r.topSpeed()));
        produto.setAceleracao(blankToNull(r.acceleration()));
    }

    private String validarUrlImagem(String url) {
        String valor = blankToNull(url);
        if (valor != null && !valor.startsWith("https://") && !valor.startsWith("http://")) {
            throw new BadRequestException("A URL da imagem deve começar com http:// ou https://");
        }
        return valor;
    }

    private static int zeroSeNulo(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private static String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public static StatusAnuncio parseStatus(String status) {
        try {
            return StatusAnuncio.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Status inválido. Use PENDENTE, APROVADO ou DESATIVADO");
        }
    }

    private static Categoria parseCategoria(String categoria) {
        try {
            return Categoria.valueOf(categoria.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Categoria inválida");
        }
    }

    public static AdminProdutoResponse toResponse(Produto p) {
        return new AdminProdutoResponse(
                p.getId(), p.getNome(), p.getMarca(), p.getModelo(), p.getPreco(), p.getImagemUrl(),
                p.getDescricao(), p.getAno(), p.getQuilometragem(),
                p.getCategoria() != null ? p.getCategoria().name() : null,
                p.getEstoque(), p.getMotor(), p.getPotencia(), p.getTorque(), p.getVelocidadeMax(),
                p.getAceleracao(), p.getStatus().name(), p.isAtivo(), p.getCriadoEm(), p.getAtualizadoEm());
    }
}
