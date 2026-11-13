package atelie_api.repository;

import atelie_api.model.ObraDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObraDisponivelRepository extends JpaRepository<ObraDisponivel, Integer> {

    List<ObraDisponivel> findByTecnicaIgnoreCaseOrderByDataCadastroDesc(String tecnica);

    List<ObraDisponivel> findAllByOrderByDataCadastroDesc();

    /** Obras à venda de um artista (tela de perfil). */
    List<ObraDisponivel> findByIdVendedorOrderByDataCadastroDesc(Integer idVendedor);
}
