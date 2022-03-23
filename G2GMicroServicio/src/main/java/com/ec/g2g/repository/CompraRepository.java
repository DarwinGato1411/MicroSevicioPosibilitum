package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.CabeceraCompra;
import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.Factura;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CompraRepository extends CrudRepository<CabeceraCompra, Integer> {
	
	Optional<CabeceraCompra> findByCabNumFactura(String cabNumFactura);
}
