package com.ec.g2g.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ec.g2g.entidad.FacturaReporte;
import com.ec.g2g.entidad.Tipoambiente;

/**
 * Spring Data JPA repository for the Products entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReporteFacturasRepository extends CrudRepository<FacturaReporte, Integer> {

long deleteByCodTipoambiente(Tipoambiente codTipoambiente);
long deleteByIdFacturaRep(Integer idFacturaRep);
}
