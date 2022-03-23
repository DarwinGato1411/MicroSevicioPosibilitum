package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.DetalleFactura;
import com.ec.g2g.entidad.DetalleRetencionCompra;
import com.ec.g2g.entidad.Factura;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DetalleRetencionCompraRepository extends CrudRepository<DetalleRetencionCompra, Integer> {
	/*consulta por numero de factura u cliente*/

}
