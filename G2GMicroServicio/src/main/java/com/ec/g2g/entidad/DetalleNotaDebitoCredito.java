/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ec.g2g.entidad;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 *
 * @author Darwin
 */
@Entity
@Table(name = "detalle_nota_debito_credito")
public class DetalleNotaDebitoCredito implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id_detalle_nota")
	private Integer idDetalleNota;
	// @Max(value=?) @Min(value=?)//if you know range of your decimal fields
	// consider using these annotations to enforce field validation
	@Column(name = "det_cantidad")
	private BigDecimal detCantidad;

	@Column(name = "det_descripcion")
	private String detDescripcion;
	@Column(name = "det_subtotal")
	private BigDecimal detSubtotal;
	@Column(name = "det_total")
	private BigDecimal detTotal;

	@Column(name = "det_tipo_venta")
	private String detTipoVenta;
	@Column(name = "det_iva")
	private BigDecimal detIva;
	@Column(name = "det_totalconiva")
	private BigDecimal detTotalconiva;
	@Column(name = "det_pordescuento")
	private BigDecimal detPordescuento;
	@Column(name = "det_valdescuento")
	private BigDecimal detValdescuento;
	@Column(name = "det_subtotaldescuento")
	private BigDecimal detSubtotaldescuento;
	@Column(name = "det_totaldescuento")
	private BigDecimal detTotaldescuento;
	@Column(name = "det_totaldescuentoiva")
	private BigDecimal detTotaldescuentoiva;
	@Column(name = "det_cantpordescuento")
	private BigDecimal detCantpordescuento;
	@Column(name = "det_subtotaldescuentoporcantidad")
	private BigDecimal detSubtotaldescuentoporcantidad;

	@Column(name = "det_cod_tipo_venta")
	private String detCodTipoVenta;
	@JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
	@ManyToOne
	private Producto idProducto;
	@JoinColumn(name = "id_nota", referencedColumnName = "id_nota")
	@ManyToOne
	private NotaCreditoDebito idNota;

	@Column(name = "codigo_producto")
	private String codigoProducto;

	@Column(name = "prod_graba_iva")
	private Boolean prodGrabaIva;

	@Column(name = "codigo_porcentaje")
	private String codigoPorcentaje;
	@Column(name = "porcentaje_iva")
	private String porcentajeIva;

	public DetalleNotaDebitoCredito() {
	}

	public DetalleNotaDebitoCredito(BigDecimal detCantidad, String detDescripcion, BigDecimal detSubtotal,
			BigDecimal detTotal, Producto idProducto, NotaCreditoDebito idNota) {
		this.detCantidad = detCantidad;
		this.detDescripcion = detDescripcion;
		this.detSubtotal = detSubtotal;
		this.detTotal = detTotal;
		this.idProducto = idProducto;
		this.idNota = idNota;
	}

	public DetalleNotaDebitoCredito(BigDecimal detCantidad, String detDescripcion, BigDecimal detSubtotal,
			BigDecimal detTotal, Producto idProducto, NotaCreditoDebito idNota, String detTipoVenta) {
		this.detCantidad = detCantidad;
		this.detDescripcion = detDescripcion;
		this.detSubtotal = detSubtotal;
		this.detTotal = detTotal;
		this.idProducto = idProducto;
		this.idNota = idNota;
		this.detTipoVenta = detTipoVenta;
	}

	public DetalleNotaDebitoCredito(Integer idDetalleNota) {
		this.idDetalleNota = idDetalleNota;
	}

	public Integer getIdDetalleNota() {
		return idDetalleNota;
	}

	public void setIdDetalleNota(Integer idDetalleNota) {
		this.idDetalleNota = idDetalleNota;
	}

	public BigDecimal getDetCantidad() {
		return detCantidad;
	}

	public void setDetCantidad(BigDecimal detCantidad) {
		this.detCantidad = detCantidad;
	}

	public String getDetDescripcion() {
		return detDescripcion;
	}

	public void setDetDescripcion(String detDescripcion) {
		this.detDescripcion = detDescripcion;
	}

	public BigDecimal getDetSubtotal() {
		return detSubtotal;
	}

	public void setDetSubtotal(BigDecimal detSubtotal) {
		this.detSubtotal = detSubtotal;
	}

	public BigDecimal getDetTotal() {
		return detTotal;
	}

	public void setDetTotal(BigDecimal detTotal) {
		this.detTotal = detTotal;
	}

	public String getDetTipoVenta() {
		return detTipoVenta;
	}

	public void setDetTipoVenta(String detTipoVenta) {
		this.detTipoVenta = detTipoVenta;
	}

	public BigDecimal getDetIva() {
		return detIva;
	}

	public void setDetIva(BigDecimal detIva) {
		this.detIva = detIva;
	}

	public BigDecimal getDetTotalconiva() {
		return detTotalconiva;
	}

	public void setDetTotalconiva(BigDecimal detTotalconiva) {
		this.detTotalconiva = detTotalconiva;
	}

	public BigDecimal getDetPordescuento() {
		return detPordescuento;
	}

	public void setDetPordescuento(BigDecimal detPordescuento) {
		this.detPordescuento = detPordescuento;
	}

	public BigDecimal getDetValdescuento() {
		return detValdescuento;
	}

	public void setDetValdescuento(BigDecimal detValdescuento) {
		this.detValdescuento = detValdescuento;
	}

	public BigDecimal getDetSubtotaldescuento() {
		return detSubtotaldescuento;
	}

	public void setDetSubtotaldescuento(BigDecimal detSubtotaldescuento) {
		this.detSubtotaldescuento = detSubtotaldescuento;
	}

	public BigDecimal getDetTotaldescuento() {
		return detTotaldescuento;
	}

	public void setDetTotaldescuento(BigDecimal detTotaldescuento) {
		this.detTotaldescuento = detTotaldescuento;
	}

	public BigDecimal getDetTotaldescuentoiva() {
		return detTotaldescuentoiva;
	}

	public void setDetTotaldescuentoiva(BigDecimal detTotaldescuentoiva) {
		this.detTotaldescuentoiva = detTotaldescuentoiva;
	}

	public BigDecimal getDetCantpordescuento() {
		return detCantpordescuento;
	}

	public void setDetCantpordescuento(BigDecimal detCantpordescuento) {
		this.detCantpordescuento = detCantpordescuento;
	}

	public BigDecimal getDetSubtotaldescuentoporcantidad() {
		return detSubtotaldescuentoporcantidad;
	}

	public void setDetSubtotaldescuentoporcantidad(BigDecimal detSubtotaldescuentoporcantidad) {
		this.detSubtotaldescuentoporcantidad = detSubtotaldescuentoporcantidad;
	}

	public String getDetCodTipoVenta() {
		return detCodTipoVenta;
	}

	public void setDetCodTipoVenta(String detCodTipoVenta) {
		this.detCodTipoVenta = detCodTipoVenta;
	}

	public Producto getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(Producto idProducto) {
		this.idProducto = idProducto;
	}

	public NotaCreditoDebito getIdNota() {
		return idNota;
	}

	public void setIdNota(NotaCreditoDebito idNota) {
		this.idNota = idNota;
	}

	public String getCodigoProducto() {
		return codigoProducto;
	}

	public void setCodigoProducto(String codigoProducto) {
		this.codigoProducto = codigoProducto;
	}

	public Boolean getProdGrabaIva() {
		return prodGrabaIva;
	}

	public void setProdGrabaIva(Boolean prodGrabaIva) {
		this.prodGrabaIva = prodGrabaIva;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (idDetalleNota != null ? idDetalleNota.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof DetalleNotaDebitoCredito)) {
			return false;
		}
		DetalleNotaDebitoCredito other = (DetalleNotaDebitoCredito) object;
		if ((this.idDetalleNota == null && other.idDetalleNota != null)
				|| (this.idDetalleNota != null && !this.idDetalleNota.equals(other.idDetalleNota))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.ec.entidad.DetalleNotaDebitoCredito[ idDetalleNota=" + idDetalleNota + " ]";
	}

	public String getCodigoPorcentaje() {
		return codigoPorcentaje;
	}

	public void setCodigoPorcentaje(String codigoPorcentaje) {
		this.codigoPorcentaje = codigoPorcentaje;
	}

	public String getPorcentajeIva() {
		return porcentajeIva;
	}

	public void setPorcentajeIva(String porcentajeIva) {
		this.porcentajeIva = porcentajeIva;
	}

	

}
