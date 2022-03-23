package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.Producto;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductoRepository extends CrudRepository<Producto, Integer> {
	//busquesa por el codigo de producto
	Optional<Producto> findByProdCodigo(String prodCodigo);

}
