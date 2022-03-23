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

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FacturaRepository extends CrudRepository<Factura, Integer> {

	/* consulta por numero de factura u cliente */
	Optional<Factura> findByFacNumeroAndIdCliente(String cliCedula, Cliente cliente);

	Optional<Factura> findByFacSecuencialUnico(String facSecuencialUnico);

	@Query("SELECT u FROM Factura u WHERE  u.txnId =:txnId  and u.codTipoambiente.amRuc =:amRuc")
	Optional<Factura>findByTxnId(@Param("txnId") Integer idQuick, @Param("amRuc") String amRuc);

//	Factura findFirstByOrderByFacNumeroDescCodTipoambienteAmRuc(String amRuc);

}
