package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.Factura;
import com.ec.g2g.entidad.Usuario;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Integer> {
	/*consulta por numero de factura u cliente*/
	Optional<Usuario> findByUsuLogin(String usuLogin);
	


}
