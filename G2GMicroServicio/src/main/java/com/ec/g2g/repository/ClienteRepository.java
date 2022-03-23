package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Cliente;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Integer> {
	Optional<Cliente> findByCliCedula(String cliCedula);
	Optional<Cliente> findByCliCedulaAndCliNombre(String cliCedula, String cliNombre);

}
