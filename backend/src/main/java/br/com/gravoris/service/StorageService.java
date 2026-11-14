package br.com.gravoris.service;

import br.com.gravoris.exception.BadRequestException;
import br.com.gravoris.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Envia as imagens dos anúncios para o Supabase Storage pela API REST oficial.
 *
 * <p>A service_role key só existe no backend e só é usada aqui; o navegador nunca a recebe.
 * O bucket é público para leitura, e o envio só é possível por este endpoint administrativo.</p>
 */
@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> EXTENSOES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/avif", "avif");

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public StorageService(@Value("${app.supabase.url:}") String supabaseUrl,
                          @Value("${app.supabase.service-role-key:}") String serviceRoleKey,
                          @Value("${app.supabase.storage-bucket:veiculos}") String bucket) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.replaceAll("/+$", "");
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey;
        this.bucket = bucket;
    }

    public boolean configurado() {
        return !supabaseUrl.isBlank() && !serviceRoleKey.isBlank();
    }

    public String enviarImagem(MultipartFile arquivo) {
        if (!configurado()) {
            throw new ServiceUnavailableException(
                    "Armazenamento de imagens não configurado. Informe uma URL de imagem ou configure o Supabase Storage.");
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BadRequestException("Selecione uma imagem");
        }
        if (arquivo.getSize() > MAX_BYTES) {
            throw new BadRequestException("Arquivo muito grande (máximo de 5 MB)");
        }

        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Não foi possível ler o arquivo");
        }

        // O tipo é decidido pelo conteúdo do arquivo, não pelo nome ou pelo header enviado
        String tipo = detectarTipo(bytes);
        if (tipo == null) {
            throw new BadRequestException("Formato não suportado. Envie JPG, PNG, WEBP ou AVIF");
        }

        String caminho = "anuncios/" + UUID.randomUUID() + "." + EXTENSOES.get(tipo);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + caminho))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("Content-Type", tipo)
                .header("Cache-Control", "max-age=31536000")
                .header("x-upsert", "false")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.error("Falha no upload para o Supabase Storage: HTTP {} {}", response.statusCode(), response.body());
                throw new ServiceUnavailableException("Falha ao enviar a imagem para o armazenamento");
            }
        } catch (IOException e) {
            throw new ServiceUnavailableException("Armazenamento de imagens indisponível");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("Envio da imagem interrompido");
        }

        return urlPublica(caminho);
    }

    /** Remove a imagem do bucket quando ela pertence a este projeto; falhas não impedem a exclusão do anúncio. */
    public void excluirSeForDoBucket(String url) {
        if (!configurado() || url == null) return;
        String prefixo = urlPublica("");
        if (!url.startsWith(prefixo)) return;

        String caminho = url.substring(prefixo.length());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + caminho))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .DELETE()
                .build();
        try {
            http.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException e) {
            log.warn("Não foi possível remover a imagem {} do Storage", caminho);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String urlPublica(String caminho) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + caminho;
    }

    static String detectarTipo(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        if (b.length >= 12 && b[4] == 'f' && b[5] == 't' && b[6] == 'y' && b[7] == 'p'
                && b[8] == 'a' && b[9] == 'v' && b[10] == 'i' && (b[11] == 'f' || b[11] == 's')) {
            return "image/avif";
        }
        return null;
    }
}
