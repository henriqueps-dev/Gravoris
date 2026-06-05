package br.com.gravoris.service;

import br.com.gravoris.dto.ClienteRequest;
import br.com.gravoris.dto.LoginRequest;
import br.com.gravoris.dto.LoginResponse;
import br.com.gravoris.dto.RedefinirSenhaRequest;
import br.com.gravoris.exception.BadRequestException;
import br.com.gravoris.exception.ResourceNotFoundException;
import br.com.gravoris.model.Cliente;
import br.com.gravoris.repository.ClienteRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pelos casos de uso de cliente.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Cliente salvar(ClienteRequest request) {
        clienteRepository.findByEmail(request.email())
                .ifPresent(cliente -> {
                    throw new BadRequestException("Email já cadastrado");
                });

        clienteRepository.findByCpf(request.cpf())
                .ifPresent(cliente -> {
                    throw new BadRequestException("CPF já cadastrado");
                });

        Cliente cliente = Cliente.builder()
                .nome(request.name())
                .email(request.email())
                .senha(passwordEncoder.encode(request.password()))
                .telefone(request.phone())
                .cpf(request.cpf())
                .ativo(true)
                .build();

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente alterar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        if (!cliente.getEmail().equals(request.email())) {
            clienteRepository.findByEmail(request.email())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BadRequestException("Email já cadastrado por outro cliente");
                        }
                    });
        }

        if (!cliente.getCpf().equals(request.cpf())) {
            clienteRepository.findByCpf(request.cpf())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BadRequestException("CPF já cadastrado por outro cliente");
                        }
                    });
        }

        cliente.setNome(request.name());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.phone());
        cliente.setCpf(request.cpf());
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    public LoginResponse login(LoginRequest request) {
        Cliente cliente = clienteRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), cliente.getSenha())) {
            throw new BadRequestException("Credenciais inválidas");
        }

        String token = UUID.randomUUID().toString();
        return new LoginResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(), token);
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        Cliente cliente = clienteRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        cliente.setSenha(passwordEncoder.encode(request.newPassword()));
        clienteRepository.save(cliente);
    }
}
