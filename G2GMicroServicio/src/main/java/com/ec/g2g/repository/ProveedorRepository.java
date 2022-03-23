package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.Proveedores;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProveedorRepository extends CrudRepository<Proveedores, Integer> {
	Optional<Proveedores> findByProvCedula(String provCedula);

}
