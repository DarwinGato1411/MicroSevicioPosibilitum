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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Darwin
 */
@Entity
@Table(name = "transportista")
public class Transportista implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_transportista")
    private Integer idTransportista;
    @Size(max = 100)
    @Column(name = "trp_cedula")
    private String trpCedula;
    @Size(max = 150)
    @Column(name = "trp_razon_social")
    private String trpRazonSocial;
    @Size(max = 150)
    @Column(name = "trp_direccion")
    private String trpDireccion;
    @Size(max = 20)
    @Column(name = "trp_telefono")
    private String trpTelefono;
    @Size(max = 20)
    @Column(name = "trp_movil")
    private String trpMovil;
    @Size(max = 100)
    @Column(name = "trp_correo")
    private String trpCorreo;
    @Size(max = 100)
    @Column(name = "trp_ciudad")
    private String trpCiudad;
    @Size(max = 10)
    @Column(name = "trp_clave")
    private String trpClave;
    @JoinColumn(name = "id_tipo_identificacion", referencedColumnName = "id_tipo_identificacion")
    @ManyToOne
    private Tipoadentificacion idTipoIdentificacion;
    @OneToMany(mappedBy = "idTransportista")
    private Collection<Guiaremision> guiaremisionCollection;
    

    public Transportista() {
    }

    public Transportista(Integer idTransportista) {
        this.idTransportista = idTransportista;
    }

    public Integer getIdTransportista() {
        return idTransportista;
    }

    public void setIdTransportista(Integer idTransportista) {
        this.idTransportista = idTransportista;
    }

    public String getTrpCedula() {
        return trpCedula;
    }

    public void setTrpCedula(String trpCedula) {
        this.trpCedula = trpCedula;
    }

    public String getTrpRazonSocial() {
        return trpRazonSocial;
    }

    public void setTrpRazonSocial(String trpRazonSocial) {
        this.trpRazonSocial = trpRazonSocial;
    }

    public String getTrpDireccion() {
        return trpDireccion;
    }

    public void setTrpDireccion(String trpDireccion) {
        this.trpDireccion = trpDireccion;
    }

    public String getTrpTelefono() {
        return trpTelefono;
    }

    public void setTrpTelefono(String trpTelefono) {
        this.trpTelefono = trpTelefono;
    }

    public String getTrpMovil() {
        return trpMovil;
    }

    public void setTrpMovil(String trpMovil) {
        this.trpMovil = trpMovil;
    }

    public String getTrpCorreo() {
        return trpCorreo;
    }

    public void setTrpCorreo(String trpCorreo) {
        this.trpCorreo = trpCorreo;
    }

    public String getTrpCiudad() {
        return trpCiudad;
    }

    public void setTrpCiudad(String trpCiudad) {
        this.trpCiudad = trpCiudad;
    }

    public String getTrpClave() {
        return trpClave;
    }

    public void setTrpClave(String trpClave) {
        this.trpClave = trpClave;
    }

    public Tipoadentificacion getIdTipoIdentificacion() {
        return idTipoIdentificacion;
    }

    public void setIdTipoIdentificacion(Tipoadentificacion idTipoIdentificacion) {
        this.idTipoIdentificacion = idTipoIdentificacion;
    }
   @XmlTransient
    public Collection<Guiaremision> getGuiaremisionCollection() {
        return guiaremisionCollection;
    }

    public void setGuiaremisionCollection(Collection<Guiaremision> guiaremisionCollection) {
        this.guiaremisionCollection = guiaremisionCollection;
    }
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTransportista != null ? idTransportista.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Transportista)) {
            return false;
        }
        Transportista other = (Transportista) object;
        if ((this.idTransportista == null && other.idTransportista != null) || (this.idTransportista != null && !this.idTransportista.equals(other.idTransportista))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ec.entidad.Transportista[ idTransportista=" + idTransportista + " ]";
    }
    
}
