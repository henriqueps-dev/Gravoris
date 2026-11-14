package br.com.gravoris.model;

/**
 * Papéis de acesso da plataforma (RBAC).
 * O cadastro público sempre cria CLIENTE; ADMINISTRADOR só é atribuído
 * pelo processo de criação inicial ou por outro administrador.
 */
public enum Role {
    CLIENTE,
    ADMINISTRADOR
}
