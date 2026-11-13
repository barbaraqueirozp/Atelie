package atelie_api.controller;

import atelie_api.dto.CadastroRequest;
import atelie_api.dto.LoginRequest;
import atelie_api.dto.PerfilResponse;
import atelie_api.dto.UsuarioResponse;
import atelie_api.model.Atelie;
import atelie_api.model.Usuario;
import atelie_api.repository.AtelieRepository;
import atelie_api.repository.EstatisticasVendedor;
import atelie_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
public class UsuarioController {

    private final UsuarioRepository repo;
    private final AtelieRepository atelies;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository repo, AtelieRepository atelies, PasswordEncoder encoder) {
        this.repo = repo;
        this.atelies = atelies;
        this.encoder = encoder;
    }

    /** Cadastro: POST /usuarios */
    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody CadastroRequest req) {
        if (req.email() == null || req.senha() == null || req.cpf() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados incompletos");
        }
        if (repo.existsByEmailIgnoreCase(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado");
        }
        if (repo.existsByCpf(req.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este CPF já está cadastrado");
        }

        Usuario u = new Usuario();
        u.setNomeCompleto(req.nomeCompleto());
        u.setCpf(req.cpf());
        u.setDataNascimento(req.dataNascimento());
        u.setEmail(req.email().trim().toLowerCase());
        u.setSenha(encoder.encode(req.senha()));   // salva só o hash
        u.setCep(req.cep());
        u.setLogradouro(req.logradouro());
        u.setNumero(req.numero());
        u.setComplemento(req.complemento());
        u.setBairro(req.bairro());
        u.setCidade(req.cidade());
        u.setEstado(req.estado() == null ? null : req.estado().toUpperCase());
        u.setTipoConta("vendedor".equals(req.tipoConta()) ? "vendedor" : "comprador");

        return UsuarioResponse.de(repo.save(u), false);
    }

    /** Login: POST /login */
    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody LoginRequest req) {
        Usuario u = repo.findByEmailIgnoreCase(req.email() == null ? "" : req.email().trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos"));

        if (req.senha() == null || !encoder.matches(req.senha(), u.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos");
        }
        if (!"ativo".equals(u.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário " + u.getStatus());
        }

        u.setUltimoLogin(LocalDateTime.now());
        repo.save(u);
        return UsuarioResponse.de(u, atelies.existsByIdUsuario(u.getIdUsuario()));
    }

    /** Perfil: GET /usuarios/{id}/perfil */
    @GetMapping("/usuarios/{id}/perfil")
    public PerfilResponse perfil(@PathVariable Integer id) {
        Usuario u = repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        EstatisticasVendedor est = repo.buscarEstatisticas(id).orElse(null);
        long aVenda = (est == null || est.getObrasAVenda() == null) ? 0 : est.getObrasAVenda();
        long acervo = (est == null || est.getAcervoTotal() == null) ? 0 : est.getAcervoTotal();

        Atelie atelie = atelies.findByIdUsuario(id).orElse(null);

        return new PerfilResponse(
                u.getIdUsuario(), u.getNomeCompleto(), u.getCidade(), u.getEstado(), u.getTipoConta(),
                aVenda, acervo, atelie != null,
                atelie == null ? null : atelie.getNome(),
                atelie == null ? null : atelie.getEspecialidade());
    }
}
