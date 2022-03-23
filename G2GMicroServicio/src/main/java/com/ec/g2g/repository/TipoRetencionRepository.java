package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.Producto;
import com.ec.g2g.entidad.TipoIdentificacionCompra;
import com.ec.g2g.entidad.TipoRetencion;
import com.ec.g2g.entidad.Tipoadentificacion;
import com.ec.g2g.entidad.Tipoambiente;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TipoRetencionRepository extends CrudRepository<TipoRetencion, Integer> {

	Optional<TipoRetencion> findByTireCodigo(String tidCodigo);

}
