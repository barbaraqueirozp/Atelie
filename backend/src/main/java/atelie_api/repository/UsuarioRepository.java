package atelie_api.repository;

import atelie_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    /** Usa a view criada no banco: obras à venda e total do acervo do artista. */
    @Query(value = """
            SELECT obras_disponiveis AS "obrasAVenda",
                   total_obras       AS "acervoTotal"
            FROM public.vw_estatisticas_vendedor
            WHERE id_usuario = :id
            """, nativeQuery = true)
    Optional<EstatisticasVendedor> buscarEstatisticas(@Param("id") Integer id);
}
