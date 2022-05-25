/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ec.g2g.entidad	;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

/**
 *
 * @author Darwin
 */
@Entity
@Table(name = "factura_reporte")
public class FacturaReporte implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_factura_rep")
    private Integer idFacturaRep;
    @Column(name = "fac_fecha")
    @Temporal(TemporalType.DATE)
    private Date facFecha;
    @Column(name = "fac_fecha_vencimiento")
    @Temporal(TemporalType.DATE)
    private Date facFechaVencimiento;
    @Size(max = 20)
    @Column(name = "fac_tipo_transaccion")
    private String facTipoTransaccion;
    @Column(name = "fac_numero")
    private Integer facNumero;
    @Size(max = 2147483647)
    @Column(name = "fac_numero_text")
    private String facNumeroText;
    @Size(max = 2147483647)
    @Column(name = "fac_cliente")
    private String facCliente;
    @Size(max = 2147483647)
    @Column(name = "fac_producto")
    private String facProducto;
    @Size(max = 2147483647)
    @Column(name = "fac_vendedor")
    private String facVendedor;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "fac_importe_sujeto_impuesto")
    private BigDecimal facImporteSujetoImpuesto;
    @Column(name = "fac_importe_impuesto")
    private BigDecimal facImporteImpuesto;
    @Column(name = "fac_total")
    private BigDecimal facTotal;
    @Size(max = 2147483647)
    @Column(name = "fac_estado")
    private String facEstado;
    @Column(name = "fac_saldo_total")
    private BigDecimal facSaldoTotal;
    @Column(name = "fac_fecha_cobro1")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro1;
    @Column(name = "fac_cobro1")
    private BigDecimal facCobro1;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago1")
    private String facMetodoPago1;
    @Column(name = "fac_fecha_cobro2")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro2;
    @Column(name = "fac_cobro2")
    private BigDecimal facCobro2;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago2")
    private String facMetodoPago2;
    @Column(name = "fac_fecha_cobro3")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro3;
    @Column(name = "fac_cobro3")
    private BigDecimal facCobro3;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago3")
    private String facMetodoPago3;
    @Column(name = "fac_fecha_cobro4")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro4;
    @Column(name = "fac_cobro4")
    private BigDecimal facCobro4;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago4")
    private String facMetodoPago4;
    @Column(name = "fac_fecha_cobro5")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro5;
    @Column(name = "fac_cobro5")
    private BigDecimal facCobro5;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago5")
    private String facMetodoPago5;
    @Column(name = "fac_fecha_cobro6")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro6;
    @Column(name = "fac_cobro6")
    private BigDecimal facCobro6;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago6")
    private String facMetodoPago6;
    @Column(name = "fac_fecha_cobro7")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro7;
    @Column(name = "fac_cobro7")
    private BigDecimal facCobro7;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago7")
    private String facMetodoPago7;
    @Column(name = "fac_fecha_cobro8")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro8;
    @Column(name = "fac_cobro8")
    private BigDecimal facCobro8;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago8")
    private String facMetodoPago8;
    @Column(name = "fac_fecha_cobro9")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro9;
    @Column(name = "fac_cobro9")
    private BigDecimal facCobro9;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago9")
    private String facMetodoPago9;
    @Column(name = "fac_fecha_cobro10")
    @Temporal(TemporalType.DATE)
    private Date facFechaCobro10;
    @Column(name = "fac_cobro10")
    private BigDecimal facCobro10;
    @Size(max = 2147483647)
    @Column(name = "fac_metodo_pago10")
    private String facMetodoPago10;
    @Column(name = "fac_saldo_total_factura")
    private BigDecimal facSaldoTotalFactura;
 
    @Column(name = "txn_id")
    private Integer txnId;
    @JoinColumn(name = "cod_tipoambiente", referencedColumnName = "cod_tipoambiente")
    @ManyToOne
    private Tipoambiente codTipoambiente;
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    @ManyToOne
    private Usuario idUsuario;

    public FacturaReporte() {
    }

    public FacturaReporte(Integer idFacturaRep) {
        this.idFacturaRep = idFacturaRep;
    }

    public Integer getIdFacturaRep() {
        return idFacturaRep;
    }

    public void setIdFacturaRep(Integer idFacturaRep) {
        this.idFacturaRep = idFacturaRep;
    }

    public Date getFacFecha() {
        return facFecha;
    }

    public void setFacFecha(Date facFecha) {
        this.facFecha = facFecha;
    }

    public Date getFacFechaVencimiento() {
        return facFechaVencimiento;
    }

    public void setFacFechaVencimiento(Date facFechaVencimiento) {
        this.facFechaVencimiento = facFechaVencimiento;
    }

    public String getFacTipoTransaccion() {
        return facTipoTransaccion;
    }

    public void setFacTipoTransaccion(String facTipoTransaccion) {
        this.facTipoTransaccion = facTipoTransaccion;
    }

    public Integer getFacNumero() {
        return facNumero;
    }

    public void setFacNumero(Integer facNumero) {
        this.facNumero = facNumero;
    }

    public String getFacNumeroText() {
        return facNumeroText;
    }

    public void setFacNumeroText(String facNumeroText) {
        this.facNumeroText = facNumeroText;
    }

    public String getFacCliente() {
        return facCliente;
    }

    public void setFacCliente(String facCliente) {
        this.facCliente = facCliente;
    }

    public String getFacProducto() {
        return facProducto;
    }

    public void setFacProducto(String facProducto) {
        this.facProducto = facProducto;
    }

    public String getFacVendedor() {
        return facVendedor;
    }

    public void setFacVendedor(String facVendedor) {
        this.facVendedor = facVendedor;
    }

    public BigDecimal getFacImporteSujetoImpuesto() {
        return facImporteSujetoImpuesto;
    }

    public void setFacImporteSujetoImpuesto(BigDecimal facImporteSujetoImpuesto) {
        this.facImporteSujetoImpuesto = facImporteSujetoImpuesto;
    }

    public BigDecimal getFacImporteImpuesto() {
        return facImporteImpuesto;
    }

    public void setFacImporteImpuesto(BigDecimal facImporteImpuesto) {
        this.facImporteImpuesto = facImporteImpuesto;
    }

    public BigDecimal getFacTotal() {
        return facTotal;
    }

    public void setFacTotal(BigDecimal facTotal) {
        this.facTotal = facTotal;
    }

    public String getFacEstado() {
        return facEstado;
    }

    public void setFacEstado(String facEstado) {
        this.facEstado = facEstado;
    }

    public BigDecimal getFacSaldoTotal() {
        return facSaldoTotal;
    }

    public void setFacSaldoTotal(BigDecimal facSaldoTotal) {
        this.facSaldoTotal = facSaldoTotal;
    }

    public Date getFacFechaCobro1() {
        return facFechaCobro1;
    }

    public void setFacFechaCobro1(Date facFechaCobro1) {
        this.facFechaCobro1 = facFechaCobro1;
    }

    public BigDecimal getFacCobro1() {
        return facCobro1;
    }

    public void setFacCobro1(BigDecimal facCobro1) {
        this.facCobro1 = facCobro1;
    }

    public String getFacMetodoPago1() {
        return facMetodoPago1;
    }

    public void setFacMetodoPago1(String facMetodoPago1) {
        this.facMetodoPago1 = facMetodoPago1;
    }

    public Date getFacFechaCobro2() {
        return facFechaCobro2;
    }

    public void setFacFechaCobro2(Date facFechaCobro2) {
        this.facFechaCobro2 = facFechaCobro2;
    }

    public BigDecimal getFacCobro2() {
        return facCobro2;
    }

    public void setFacCobro2(BigDecimal facCobro2) {
        this.facCobro2 = facCobro2;
    }

    public String getFacMetodoPago2() {
        return facMetodoPago2;
    }

    public void setFacMetodoPago2(String facMetodoPago2) {
        this.facMetodoPago2 = facMetodoPago2;
    }

    public Date getFacFechaCobro3() {
        return facFechaCobro3;
    }

    public void setFacFechaCobro3(Date facFechaCobro3) {
        this.facFechaCobro3 = facFechaCobro3;
    }

    public BigDecimal getFacCobro3() {
        return facCobro3;
    }

    public void setFacCobro3(BigDecimal facCobro3) {
        this.facCobro3 = facCobro3;
    }

    public String getFacMetodoPago3() {
        return facMetodoPago3;
    }

    public void setFacMetodoPago3(String facMetodoPago3) {
        this.facMetodoPago3 = facMetodoPago3;
    }

    public Date getFacFechaCobro4() {
        return facFechaCobro4;
    }

    public void setFacFechaCobro4(Date facFechaCobro4) {
        this.facFechaCobro4 = facFechaCobro4;
    }

    public BigDecimal getFacCobro4() {
        return facCobro4;
    }

    public void setFacCobro4(BigDecimal facCobro4) {
        this.facCobro4 = facCobro4;
    }

    public String getFacMetodoPago4() {
        return facMetodoPago4;
    }

    public void setFacMetodoPago4(String facMetodoPago4) {
        this.facMetodoPago4 = facMetodoPago4;
    }

    public Date getFacFechaCobro5() {
        return facFechaCobro5;
    }

    public void setFacFechaCobro5(Date facFechaCobro5) {
        this.facFechaCobro5 = facFechaCobro5;
    }

    public BigDecimal getFacCobro5() {
        return facCobro5;
    }

    public void setFacCobro5(BigDecimal facCobro5) {
        this.facCobro5 = facCobro5;
    }

    public String getFacMetodoPago5() {
        return facMetodoPago5;
    }

    public void setFacMetodoPago5(String facMetodoPago5) {
        this.facMetodoPago5 = facMetodoPago5;
    }

    public Date getFacFechaCobro6() {
        return facFechaCobro6;
    }

    public void setFacFechaCobro6(Date facFechaCobro6) {
        this.facFechaCobro6 = facFechaCobro6;
    }

    public BigDecimal getFacCobro6() {
        return facCobro6;
    }

    public void setFacCobro6(BigDecimal facCobro6) {
        this.facCobro6 = facCobro6;
    }

    public String getFacMetodoPago6() {
        return facMetodoPago6;
    }

    public void setFacMetodoPago6(String facMetodoPago6) {
        this.facMetodoPago6 = facMetodoPago6;
    }

    public Date getFacFechaCobro7() {
        return facFechaCobro7;
    }

    public void setFacFechaCobro7(Date facFechaCobro7) {
        this.facFechaCobro7 = facFechaCobro7;
    }

    public BigDecimal getFacCobro7() {
        return facCobro7;
    }

    public void setFacCobro7(BigDecimal facCobro7) {
        this.facCobro7 = facCobro7;
    }

    public String getFacMetodoPago7() {
        return facMetodoPago7;
    }

    public void setFacMetodoPago7(String facMetodoPago7) {
        this.facMetodoPago7 = facMetodoPago7;
    }

    public Date getFacFechaCobro8() {
        return facFechaCobro8;
    }

    public void setFacFechaCobro8(Date facFechaCobro8) {
        this.facFechaCobro8 = facFechaCobro8;
    }

    public BigDecimal getFacCobro8() {
        return facCobro8;
    }

    public void setFacCobro8(BigDecimal facCobro8) {
        this.facCobro8 = facCobro8;
    }

    public String getFacMetodoPago8() {
        return facMetodoPago8;
    }

    public void setFacMetodoPago8(String facMetodoPago8) {
        this.facMetodoPago8 = facMetodoPago8;
    }

    public Date getFacFechaCobro9() {
        return facFechaCobro9;
    }

    public void setFacFechaCobro9(Date facFechaCobro9) {
        this.facFechaCobro9 = facFechaCobro9;
    }

    public BigDecimal getFacCobro9() {
        return facCobro9;
    }

    public void setFacCobro9(BigDecimal facCobro9) {
        this.facCobro9 = facCobro9;
    }

    public String getFacMetodoPago9() {
        return facMetodoPago9;
    }

    public void setFacMetodoPago9(String facMetodoPago9) {
        this.facMetodoPago9 = facMetodoPago9;
    }

    public Date getFacFechaCobro10() {
        return facFechaCobro10;
    }

    public void setFacFechaCobro10(Date facFechaCobro10) {
        this.facFechaCobro10 = facFechaCobro10;
    }

    public BigDecimal getFacCobro10() {
        return facCobro10;
    }

    public void setFacCobro10(BigDecimal facCobro10) {
        this.facCobro10 = facCobro10;
    }

    public String getFacMetodoPago10() {
        return facMetodoPago10;
    }

    public void setFacMetodoPago10(String facMetodoPago10) {
        this.facMetodoPago10 = facMetodoPago10;
    }

    public BigDecimal getFacSaldoTotalFactura() {
        return facSaldoTotalFactura;
    }

    public void setFacSaldoTotalFactura(BigDecimal facSaldoTotalFactura) {
        this.facSaldoTotalFactura = facSaldoTotalFactura;
    }

    public Integer getTxnId() {
        return txnId;
    }

    public void setTxnId(Integer txnId) {
        this.txnId = txnId;
    }

  

    public Tipoambiente getCodTipoambiente() {
        return codTipoambiente;
    }

    public void setCodTipoambiente(Tipoambiente codTipoambiente) {
        this.codTipoambiente = codTipoambiente;
    }

    public Usuario getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Usuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idFacturaRep != null ? idFacturaRep.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FacturaReporte)) {
            return false;
        }
        FacturaReporte other = (FacturaReporte) object;
        if ((this.idFacturaRep == null && other.idFacturaRep != null) || (this.idFacturaRep != null && !this.idFacturaRep.equals(other.idFacturaRep))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ec.entidad.FacturaReporte[ idFacturaRep=" + idFacturaRep + " ]";
    }
    
}
