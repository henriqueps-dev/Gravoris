package br.com.gravoris.controller;

import br.com.gravoris.dto.PedidoRequest;
import br.com.gravoris.dto.PedidoResponse;
import br.com.gravoris.model.Pedido;
import br.com.gravoris.service.PedidoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller REST responsável por receber os pedidos do frontend.
 */
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/pedidos", produces = MediaType.APPLICATION_JSON_VALUE)
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(@Valid @RequestBody PedidoRequest request) {
        Pedido pedido = pedidoService.salvar(request);
        PedidoResponse response = new PedidoResponse(
                pedido.getId(),
                pedido.getTotal(),
                pedido.getStatus().name(),
                pedido.getDataPedido().toString()
        );
        return ResponseEntity.ok(response);
    }
}
