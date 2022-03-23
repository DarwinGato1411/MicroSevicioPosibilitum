/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package  com.ec.g2g.entidad;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Darwin
 */
@Entity
@Table(name = "tipo_retencion")
public class TipoRetencion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "tire_codigo")
    private String tireCodigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "tire_nombre")
    private String tireNombre;

    @Basic(optional = false)
    @NotNull
    @Column(name = "tire_porcentaje_retencion")
    private double tirePorcentajeRetencion;
    @Column(name = "tire_tipo")
    private String tireTipo;
    @OneToMany(mappedBy = "tireCodigo")
    private Collection<DetalleRetencionCompra> detalleRetencionCompraCollection;

    public TipoRetencion() {
    }

    public TipoRetencion(String tireCodigo) {
        this.tireCodigo = tireCodigo;
    }

    public TipoRetencion(String tireCodigo, String tireNombre, double tirePorcentajeRetencion) {
        this.tireCodigo = tireCodigo;
        this.tireNombre = tireNombre;
        this.tirePorcentajeRetencion = tirePorcentajeRetencion;
    }

    public String getTireCodigo() {
        return tireCodigo;
    }

    public void setTireCodigo(String tireCodigo) {
        this.tireCodigo = tireCodigo;
    }

    public String getTireNombre() {
        return tireNombre;
    }

    public void setTireNombre(String tireNombre) {
        this.tireNombre = tireNombre;
    }

    public double getTirePorcentajeRetencion() {
        return tirePorcentajeRetencion;
    }

    public void setTirePorcentajeRetencion(double tirePorcentajeRetencion) {
        this.tirePorcentajeRetencion = tirePorcentajeRetencion;
    }

    public String getTireTipo() {
        return tireTipo;
    }

    public void setTireTipo(String tireTipo) {
        this.tireTipo = tireTipo;
    }

    @XmlTransient
    public Collection<DetalleRetencionCompra> getDetalleRetencionCompraCollection() {
        return detalleRetencionCompraCollection;
    }

    public void setDetalleRetencionCompraCollection(Collection<DetalleRetencionCompra> detalleRetencionCompraCollection) {
        this.detalleRetencionCompraCollection = detalleRetencionCompraCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tireCodigo != null ? tireCodigo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoRetencion)) {
            return false;
        }
        TipoRetencion other = (TipoRetencion) object;
        if ((this.tireCodigo == null && other.tireCodigo != null) || (this.tireCodigo != null && !this.tireCodigo.equals(other.tireCodigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ec.entidad.TipoRetencion[ tireCodigo=" + tireCodigo + " ]";
    }

}
