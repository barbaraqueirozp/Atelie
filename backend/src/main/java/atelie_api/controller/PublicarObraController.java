package atelie_api.controller;

import atelie_api.config.UploadConfig;
import atelie_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Publicar obra: POST /obras (multipart/form-data)
 * Recebe os dados + a foto, salva a foto em uploads/obras
 * e grava em obra_arte e imagem_obra.
 */
@RestController
public class PublicarObraController {

    private static final Set<String> CATEGORIAS = Set.of("Pintura", "Escultura", "Fotografia", "Digital");
    private static final Set<String> TIPOS_IMAGEM = Set.of("image/jpeg", "image/png", "image/webp");

    private final JdbcTemplate jdbc;
    private final UsuarioRepository usuarios;

    public PublicarObraController(JdbcTemplate jdbc, UsuarioRepository usuarios) {
        this.jdbc = jdbc;
        this.usuarios = usuarios;
    }

    @PostMapping(value = "/obras", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> publicar(@RequestParam Integer idVendedor,
                                        @RequestParam String titulo,
                                        @RequestParam(required = false) String descricao,
                                        @RequestParam String tecnica,
                                        @RequestParam BigDecimal preco,
                                        @RequestParam(required = false) String dimensoes,
                                        @RequestParam MultipartFile foto) throws IOException {

        // ---------- validações ----------
        var vendedor = usuarios.findById(idVendedor).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        if (!"vendedor".equals(vendedor.getTipoConta())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cadastre seu ateliê para vender obras");
        }
        if (titulo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o título");
        }
        if (!CATEGORIAS.contains(tecnica)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inválida");
        }
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço deve ser maior que zero");
        }
        if (foto.isEmpty() || !TIPOS_IMAGEM.contains(foto.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie uma foto JPG, PNG ou WEBP");
        }

        // ---------- salva a foto com um nome único ----------
        String extensao = switch (foto.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String nomeArquivo = UUID.randomUUID() + extensao;
        Path pasta = UploadConfig.PASTA_UPLOADS.resolve("obras");
        Files.createDirectories(pasta);
        Path destino = pasta.resolve(nomeArquivo);
        foto.transferTo(destino);

        try {
            // ---------- grava a obra e a imagem de capa ----------
            Integer idObra = jdbc.queryForObject("""
                    INSERT INTO public.obra_arte
                        (id_vendedor, titulo, descricao, tecnica, preco, dimensoes, status)
                    VALUES (?, ?, ?, ?, ?, ?, 'disponivel')
                    RETURNING id_obra
                    """, Integer.class,
                    idVendedor, titulo.trim(), vazioParaNull(descricao), tecnica, preco,
                    vazioParaNull(dimensoes));

            String url = "/uploads/obras/" + nomeArquivo;
            jdbc.update("""
                    INSERT INTO public.imagem_obra (id_obra, url_imagem, eh_capa)
                    VALUES (?, ?, TRUE)
                    """, idObra, url);

            return Map.of("idObra", idObra, "imagemCapa", url);

        } catch (RuntimeException erro) {
            // Se o banco recusar, apaga a foto para não deixar arquivo órfão
            Files.deleteIfExists(destino);
            throw erro;
        }
    }

    private static String vazioParaNull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
