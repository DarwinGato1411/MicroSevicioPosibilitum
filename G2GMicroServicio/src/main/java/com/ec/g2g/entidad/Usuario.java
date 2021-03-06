/*
 * To change this template, choose Tools | Templates
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
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author gato
 */
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_usuario")
    private Integer idUsuario;
    @Size(max = 100)
    @Column(name = "usu_nombre")
    private String usuNombre;
    @Size(max = 100)
    @Column(name = "usu_login")
    private String usuLogin;
    @Size(max = 100)
    @Column(name = "usu_password")
    private String usuPassword;
    @Size(max = 100)
    @Column(name = "usu_correo")
    private String usuCorreo;
    @Column(name = "usu_nivel")
    private Integer usuNivel;
    @Lob
    @Column(name = "usu_foto")
    private byte[] usuFoto;
    @Size(max = 100)
    @Column(name = "usu_tipo_usuario")
    private String usuTipoUsuario;
    @OneToMany(mappedBy = "idUsuario")
    private Collection<Factura> facturaCollection;
    @OneToMany(mappedBy = "idUsuario")
    private Collection<CabeceraCompra> cabeceraCompraCollection;
    @OneToMany(mappedBy = "idUsuario")
    private Collection<CierreCaja> cierreCajaCollection;
 
    @OneToMany(mappedBy = "idUsuario")
    private Collection<OrdenTrabajo> ordenTrabajoCollection;
    
    @OneToMany(mappedBy = "idUsuario")
    private Collection<FacturaReporte> facturaReporteCollection;

    public Usuario() {
    }

    public Usuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuNombre() {
        return usuNombre;
    }

    public void setUsuNombre(String usuNombre) {
        this.usuNombre = usuNombre;
    }

    public String getUsuLogin() {
        return usuLogin;
    }

    public void setUsuLogin(String usuLogin) {
        this.usuLogin = usuLogin;
    }

    public String getUsuPassword() {
        return usuPassword;
    }

    public void setUsuPassword(String usuPassword) {
        this.usuPassword = usuPassword;
    }

    public String getUsuCorreo() {
        return usuCorreo;
    }

    public void setUsuCorreo(String usuCorreo) {
        this.usuCorreo = usuCorreo;
    }

    public Integer getUsuNivel() {
        return usuNivel;
    }

    public void setUsuNivel(Integer usuNivel) {
        this.usuNivel = usuNivel;
    }

    public byte[] getUsuFoto() {
        return usuFoto;
    }

    public void setUsuFoto(byte[] usuFoto) {
        this.usuFoto = usuFoto;
    }

    public String getUsuTipoUsuario() {
        return usuTipoUsuario;
    }

    public void setUsuTipoUsuario(String usuTipoUsuario) {
        this.usuTipoUsuario = usuTipoUsuario;
    }

	

    @XmlTransient
    public Collection<Factura> getFacturaCollection() {
        return facturaCollection;
    }

    public void setFacturaCollection(Collection<Factura> facturaCollection) {
        this.facturaCollection = facturaCollection;
    }

    @XmlTransient
    public Collection<CabeceraCompra> getCabeceraCompraCollection() {
        return cabeceraCompraCollection;
    }

    public void setCabeceraCompraCollection(Collection<CabeceraCompra> cabeceraCompraCollection) {
        this.cabeceraCompraCollection = cabeceraCompraCollection;
    }

    @XmlTransient
    public Collection<CierreCaja> getCierreCajaCollection() {
        return cierreCajaCollection;
    }

    public void setCierreCajaCollection(Collection<CierreCaja> cierreCajaCollection) {
        this.cierreCajaCollection = cierreCajaCollection;
    }

    public Collection<OrdenTrabajo> getOrdenTrabajoCollection() {
        return ordenTrabajoCollection;
    }

    public void setOrdenTrabajoCollection(Collection<OrdenTrabajo> ordenTrabajoCollection) {
        this.ordenTrabajoCollection = ordenTrabajoCollection;
    }

    public Collection<FacturaReporte> getFacturaReporteCollection() {
		return facturaReporteCollection;
	}

	public void setFacturaReporteCollection(Collection<FacturaReporte> facturaReporteCollection) {
		this.facturaReporteCollection = facturaReporteCollection;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (idUsuario != null ? idUsuario.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) object;
        if ((this.idUsuario == null && other.idUsuario != null) || (this.idUsuario != null && !this.idUsuario.equals(other.idUsuario))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ec.entidad.Usuario[ idUsuario=" + idUsuario + " ]";
    }
}
