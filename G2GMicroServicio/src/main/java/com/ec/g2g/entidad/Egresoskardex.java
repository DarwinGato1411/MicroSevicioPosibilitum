/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package  com.ec.g2g.entidad;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Darwin
 */
@Entity
@Table(name = "egresoskardex")
public class Egresoskardex implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id    
    @Column(name = "id_kardex")
    private Integer idKardex;
    @Column(name = "egresos")
    private BigDecimal egresos;


    public Egresoskardex() {
    }

    public BigDecimal getEgresos() {
        return egresos;
    }

    public void setEgresos(BigDecimal egresos) {
        this.egresos = egresos;
    }

    public Integer getIdKardex() {
        return idKardex;
    }

    public void setIdKardex(Integer idKardex) {
        this.idKardex = idKardex;
    }
    
}
