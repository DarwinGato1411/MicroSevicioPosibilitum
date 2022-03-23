package com.ec.g2g.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.Factura;
import com.ec.g2g.entidad.RetencionCompra;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RetencionCompraRepository extends CrudRepository<RetencionCompra, Integer> {
	/* ultimo secuencial */
	RetencionCompra findFirstByOrderByRcoSecuencialDesc();

	@Query("SELECT u FROM RetencionCompra u WHERE  (u.idQuick =:idQuick or u.rcoSecuencialText=:rcoSecuencialText) and u.codTipoambiente.amRuc =:amRuc")
	Optional<RetencionCompra> findByIdQuickOrRcoSecuencialText(@Param("idQuick") Integer idQuick,
			@Param("rcoSecuencialText") String rcoSecuencialText, @Param("amRuc") String amRuc);

}
