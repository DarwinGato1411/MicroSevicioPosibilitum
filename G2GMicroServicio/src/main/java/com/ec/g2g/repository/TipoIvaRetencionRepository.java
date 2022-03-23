package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Tipoivaretencion;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TipoIvaRetencionRepository extends CrudRepository<Tipoivaretencion, Integer> {

	Optional<Tipoivaretencion> findByTipivaretDescripcion(String tipivaretDescripcion);
}
