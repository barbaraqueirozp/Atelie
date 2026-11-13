package atelie_api.repository;

import atelie_api.model.Atelie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AtelieRepository extends JpaRepository<Atelie, Integer> {

    Optional<Atelie> findByIdUsuario(Integer idUsuario);

    boolean existsByIdUsuario(Integer idUsuario);
}
