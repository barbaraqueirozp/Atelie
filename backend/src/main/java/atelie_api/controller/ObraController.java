package atelie_api.controller;

import atelie_api.model.ObraDisponivel;
import atelie_api.repository.ObraDisponivelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/obras")
public class ObraController {

    private final ObraDisponivelRepository repo;

    public ObraController(ObraDisponivelRepository repo) {
        this.repo = repo;
    }

    /** GET /obras  |  GET /obras?categoria=Pintura  |  GET /obras?vendedor=1 */
    @GetMapping
    public List<ObraDisponivel> listar(@RequestParam(required = false) String categoria,
                                       @RequestParam(required = false) Integer vendedor) {
        if (vendedor != null) {
            return repo.findByIdVendedorOrderByDataCadastroDesc(vendedor);
        }
        if (categoria == null || categoria.isBlank() || categoria.equalsIgnoreCase("Em alta")) {
            return repo.findAllByOrderByDataCadastroDesc();
        }
        return repo.findByTecnicaIgnoreCaseOrderByDataCadastroDesc(categoria);
    }

    /** GET /obras/3 */
    @GetMapping("/{id}")
    public ObraDisponivel buscar(@PathVariable Integer id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Obra não encontrada"));
    }
}
