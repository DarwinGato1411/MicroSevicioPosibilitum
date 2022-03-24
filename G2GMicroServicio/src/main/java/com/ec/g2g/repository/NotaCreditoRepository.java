package com.ec.g2g.repository;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.Factura;
import com.ec.g2g.entidad.NotaCreditoDebito;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotaCreditoRepository extends CrudRepository<NotaCreditoDebito, Integer> {

	/* consulta por numero de factura u cliente */

	@Query("SELECT u FROM NotaCreditoDebito u WHERE  u.txnId =:txnId  and u.codTipoambiente =:codTipoambiente")
	Optional<NotaCreditoDebito>findByTxnId(@Param("txnId") Integer idQuick, @Param("codTipoambiente") Integer amRuc);
}
