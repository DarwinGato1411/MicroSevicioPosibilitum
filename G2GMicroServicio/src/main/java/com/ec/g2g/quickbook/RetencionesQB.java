package com.ec.g2g.quickbook;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ec.g2g.ModelIdentificacion;
import com.ec.g2g.entidad.CabeceraCompra;
import com.ec.g2g.entidad.DetalleRetencionCompra;
import com.ec.g2g.entidad.EstadoFacturas;
import com.ec.g2g.entidad.Factura;
import com.ec.g2g.entidad.Proveedores;
import com.ec.g2g.entidad.RetencionCompra;
import com.ec.g2g.entidad.TipoIdentificacionCompra;
import com.ec.g2g.entidad.TipoRetencion;
import com.ec.g2g.entidad.Tipoambiente;
import com.ec.g2g.entidad.Tipoivaretencion;
import com.ec.g2g.global.ValoresGlobales;
import com.ec.g2g.repository.CompraRepository;
import com.ec.g2g.repository.DetalleRetencionCompraRepository;
import com.ec.g2g.repository.EstadoFacturaRepository;
import com.ec.g2g.repository.ProveedorRepository;
import com.ec.g2g.repository.RetencionCompraRepository;
import com.ec.g2g.repository.TipoAmbienteRepository;
import com.ec.g2g.repository.TipoIdentificacionCompraRepository;
import com.ec.g2g.repository.TipoIvaRetencionRepository;
import com.ec.g2g.repository.TipoRetencionRepository;
import com.ec.g2g.utilitario.ArchivoUtils;
import com.google.gson.Gson;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.MemoRef;
import com.intuit.ipp.data.TaxCode;
import com.intuit.ipp.data.TaxRate;
import com.intuit.ipp.data.TaxRateDetail;
import com.intuit.ipp.data.Vendor;
import com.intuit.ipp.data.VendorCredit;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;

import ch.qos.logback.core.joran.conditional.IfAction;

@Service
public class RetencionesQB {

	@Autowired
	public QBOServiceHelper helper;

	@Autowired
	private TipoAmbienteRepository tipoAmbienteRepository;
	@Autowired
	private ValoresGlobales valoresGlobales;

	@Autowired
	ManejarToken manejarToken;
	@Autowired
	RetencionCompraRepository retencionCompraRepository;
	@Autowired
	private EstadoFacturaRepository estadoFacturaRepository;
	@Autowired
	private TipoIdentificacionCompraRepository tipoIdentificacionCompra;

	@Autowired
	private ProveedorRepository proveedorRepository;

	@Autowired
	private CompraRepository compraRepository;
	@Autowired
	private TipoRetencionRepository tipoRetencionRepository;
	@Autowired
	private TipoIvaRetencionRepository tipoIvaRetencionRepository;
	@Autowired
	private DetalleRetencionCompraRepository detalleRetencionCompraRepository;

	@Value("${posibilitum.nombre.empresa}")
	String NOMBREEMPRESA;

	@Value("${posibilitum.ruc.empresa}")
	String RUCEMPRESA;

	/* PARA OBTENER LOS IMPUESTOS */
	@Autowired
	TaxCodeQB taxCodeQB;
	@PersistenceContext
	private EntityManager entityManager;

	public List<RetencionCompra> findUltimoSecuencial() {
		return entityManager.createQuery(
				"SELECT p FROM RetencionCompra p WHERE p.codTipoambiente.amRuc=:amRuc ORDER BY p.rcoSecuencial DESC",
				RetencionCompra.class).setParameter("amRuc", RUCEMPRESA).setMaxResults(1).getResultList();
	}

	public void obtenerRetenciones() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Optional<Tipoambiente> tipoAmbiente = tipoAmbienteRepository.findByAmEstadoAndAmRuc(Boolean.TRUE, RUCEMPRESA);
		if (tipoAmbiente.isPresent()) {
			valoresGlobales.TIPOAMBIENTE = tipoAmbiente.get();
			System.out.println("TIPO AMBIENTE CARGADO");
		} else {
			System.out.println("TIPO AMBIENTE NULL NO PROCESA LAS RETENCIONES");
			return;

		}

		String realmId = valoresGlobales.REALMID;
		// String accessToken = valoresGlobales.TOKEN;
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
		try {
			if (valoresGlobales.REALMID != null && valoresGlobales.REFRESHTOKEN != null) {
				// get DataService
				DataService service = helper.getDataService(realmId, accessToken);
				String WHERE = "";
				String ORDERBY = " ORDER BY Id ASC";
				if (valoresGlobales.getTIPOAMBIENTE().getAmCargaInicial()) {
					WHERE = " WHERE Id > '" + valoresGlobales.getTIPOAMBIENTE().getAmIdRetencionInicio() + "'";
					// + "' AND MetaData.CreateTime >= '2021-11-04' ";
				} else {

					WHERE = " WHERE MetaData.CreateTime >= '" + format.format(new Date()) + "'";
				}

				String sql = "select * from vendorcredit ";
				String QUERYFINAL = sql + WHERE + ORDERBY;
				System.out.println("QUERYFINAL " + QUERYFINAL);
				QueryResult queryResult = service.executeQuery(QUERYFINAL);
				List<VendorCredit> retenciones = (List<VendorCredit>) queryResult.getEntities();
				System.out.println("retenciones OBTENIDOS " + retenciones.size());

				for (VendorCredit vendorCredit : retenciones) {
					System.out.println("NUMERO DIGITOS  " + vendorCredit.getPrivateNote().length() + "   # DOCUM "
							+ vendorCredit.getDocNumber().toUpperCase());

					if (vendorCredit.getPrivateNote().length() == 17
							&& vendorCredit.getDocNumber().toUpperCase().contains("RT")) {
						String separaNumero[] = vendorCredit.getDocNumber().split("-");
						String numeroRetencion = separaNumero[1];
						/* VALIDAR SI EXISTE LA RETENCION */
						Optional<RetencionCompra> retencionValida = retencionCompraRepository
								.findByIdQuickOrRcoSecuencialText(Integer.valueOf(vendorCredit.getId()),
										numeroRetencion, valoresGlobales.getTIPOAMBIENTE().getAmRuc());
						if (!retencionValida.isPresent()) {
							System.out.println(
									"PROCESANDO RETENCION --> " + mapperVendorToRetencion(vendorCredit, service));
						} else {
							System.out.println("LA RETENCION YA EXISTE " + vendorCredit.getDocNumber().toUpperCase());

						}

					} else {
						System.out.println("RETENCION NO PROCESADA " + vendorCredit.getDocNumber());

					}

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private String mapperVendorToRetencion(VendorCredit vendorCredit, DataService service) {
		Gson gson = new Gson();

		try {
			/* CREAT PROVEEDOR */

			Vendor vendor = obtenerVendor(vendorCredit.getVendorRef().getValue());

			// campo OTROS de QB
			String cedulaRUC = vendor.getAlternatePhone() != null ? vendor.getAlternatePhone().getFreeFormNumber() : "";
			Optional<Proveedores> proveedoresRecup = proveedorRepository.findByProvCedula(cedulaRUC);
			// pendiente por definir el campo (FAX O CODIGO POSTAL)
			Proveedores proveedores = new Proveedores();
			if (!proveedoresRecup.isPresent()) {
				proveedores = new Proveedores();
				proveedores.setProvCedula(cedulaRUC);
				proveedores.setProvNombre(vendor.getPrintOnCheckName());
				proveedores.setProvNomComercial(vendor.getPrintOnCheckName());
				proveedores.setProvDireccion(vendor.getBillAddr() != null ? vendor.getBillAddr().getLine1() : "QUITO");
				proveedores.setProvTelefono("999999999");
				proveedores.setProvMovil("999999999");
				proveedores
						.setProvCorreo(vendor.getPrimaryEmailAddr() != null ? vendor.getPrimaryEmailAddr().getAddress()
								: valoresGlobales.getTIPOAMBIENTE().getAmUsuarioSmpt());
				// VERIFICAR EN QUE CAMPO RETORNA EL VALOR DEL DOCUMENTO
				Optional<TipoIdentificacionCompra> tipoadentificacion = tipoIdentificacionCompra
						.findById(validarCedulaRuc(cedulaRUC).getCodigo());
				proveedores.setIdTipoIdentificacionCompra(tipoadentificacion.get());
				/* GUARDAMOS EL PROVEEDOR */
				proveedorRepository.save(proveedores);
			} else {

				proveedores = proveedoresRecup.get();
				proveedores.setProvCedula(cedulaRUC);
				proveedorRepository.save(proveedores);
			}

			/* CREAMOS LA CABECERA DE COMPRA PARA PODER GENERAR LA RETENCION */
			System.out.println("CREAMOS LA CABECERA DE COMPRA");
			EstadoFacturas estadoFacturas = estadoFacturaRepository.findByEstCodigo("PE");
			CabeceraCompra cabeceraCompra = new CabeceraCompra();
			cabeceraCompra.setIdEstado(estadoFacturas);
			/* numero de QB */

			String numeroQB = vendorCredit.getPrivateNote();

			String separaNumero[] = numeroQB.split("-");
			String establecimiento = "";
			String puntoEmision = "";
			String numFactura = "";
			if (separaNumero.length == 1) {
				establecimiento = separaNumero[0];
			} else if (separaNumero.length == 2) {
				establecimiento = separaNumero[0];
				puntoEmision = separaNumero[1];
			} else if (separaNumero.length == 3) {
				establecimiento = separaNumero[0];
				puntoEmision = separaNumero[1];
				numFactura = separaNumero[2];
			}

//			Optional<CabeceraCompra> cabeceraComprasRecup = compraRepository.findByCabNumFactura(numFactura);
//			if (!cabeceraComprasRecup.isPresent()) {
				System.out.println("INSERTA LA CABECERA DE COMPRA NUEVA");
				cabeceraCompra.setCabNumFactura(numFactura);
				cabeceraCompra.setCabFecha(vendorCredit.getMetaData().getCreateTime());
				cabeceraCompra.setCabSubTotal(BigDecimal.ZERO);
				cabeceraCompra.setCabIva(BigDecimal.ZERO);
				cabeceraCompra.setCabTotal(BigDecimal.ZERO);
				cabeceraCompra.setDrcCodigoSustento("01");
				cabeceraCompra.setCabRetencionAutori("N");
				cabeceraCompra.setIdProveedor(proveedores);
				cabeceraCompra.setCabEstado("PA");
				cabeceraCompra.setCabProveedor(vendor.getDisplayName());
				// fecha de la factura
				cabeceraCompra.setCabFechaEmision(vendorCredit.getTxnDate());
				cabeceraCompra.setCabEstablecimiento(establecimiento);
				cabeceraCompra.setCabPuntoEmi(puntoEmision);
				compraRepository.save(cabeceraCompra);

//			} else {
//				cabeceraCompra = cabeceraComprasRecup.get();
//				System.out.println("ACTUALIZA LA COMPRA PARA CREAR UNA NUEVA RETENCION");
//			}

			/* INICIA RETENCION */
			RetencionCompra retecionRecup = findUltimoSecuencial().size() > 0 ? findUltimoSecuencial().get(0) : null;
//			String JSONRECUPSEC = gson.toJson(cabeceraCompra);
//			System.out.println("CABECERA DE COMPRA  "+cabeceraCompra);
			// si no exite una factura coloca el numero 1
			Integer numeroRetencion = retecionRecup != null ? retecionRecup.getRcoSecuencial() + 1
					: valoresGlobales.getTIPOAMBIENTE().getAmSecuencialInicioRetencion();
			String numeroRetencionText = numeroFacturaTexto(numeroRetencion);
			System.out.println("numeroRetencionText " + numeroRetencionText);
			RetencionCompra retencionCompra = new RetencionCompra();
			retencionCompra.setRcoDetalle("RETENCION QUICK BOOKS");
			retencionCompra.setRcoFecha(vendorCredit.getTxnDate());
			retencionCompra.setRcoIva(Boolean.FALSE);
			retencionCompra.setRcoPorcentajeIva(12);
			retencionCompra.setRcoPuntoEmision(valoresGlobales.getTIPOAMBIENTE().getAmPtoemi());
			retencionCompra.setRcoSecuencial(numeroRetencion);
			retencionCompra.setRcoSerie("1");
			retencionCompra.setRcoValorRetencionIva(BigDecimal.ZERO);
			retencionCompra.setIdCabecera(cabeceraCompra);
			retencionCompra.setCabFechaEmision(vendorCredit.getTxnDate());
			retencionCompra.setDrcEstadosri("PENDIENTE");
			retencionCompra.setIdQuick(Integer.valueOf(vendorCredit.getId()));
			// generar la clave de acceso y autorizacion
			String claveAcceso = ArchivoUtils.generaClave(vendorCredit.getTxnDate(), "07",
					valoresGlobales.getTIPOAMBIENTE().getAmRuc(), valoresGlobales.getTIPOAMBIENTE().getAmCodigo(),
					valoresGlobales.getTIPOAMBIENTE().getAmEstab() + valoresGlobales.getTIPOAMBIENTE().getAmPtoemi(),
					numeroRetencionText, "12345678", "1");
			System.out.println("claveAcceso " + claveAcceso);
			retencionCompra.setRcoAutorizacion(claveAcceso);
			retencionCompra.setRcoSecuencialText(numeroRetencionText);
			retencionCompra.setCodTipoambiente(valoresGlobales.getTIPOAMBIENTE());

			if (!claveAcceso.contains("null")) {
				retencionCompraRepository.save(retencionCompra);
			}
			DetalleRetencionCompra detalleRetencionCompra = new DetalleRetencionCompra();

			/* REGISTRAMOS EL DETALLE DE LA RETENCION */
			for (Line detalleRet : vendorCredit.getLine()) {
				detalleRetencionCompra = new DetalleRetencionCompra();
				detalleRetencionCompra.setDrcBaseImponible(detalleRet.getDescription() != null
						? BigDecimal.valueOf(Double.valueOf(detalleRet.getDescription()))
						: BigDecimal.ZERO);
				List<TaxRate> rateDetail = null;
				TaxRate taxRatePorcet = null;
				TaxCode taxCode = taxCodeQB
						.obtenerTaxCode(detalleRet.getAccountBasedExpenseLineDetail().getTaxCodeRef().getValue());

				for (TaxRateDetail detail : taxCode.getPurchaseTaxRateList().getTaxRateDetail()) {
					if (detail.getTaxRateRef().getName().contains("Compras")) {
						for (TaxRate taxrate : taxCodeQB.obtenerTaxRateDetail(detail.getTaxRateRef().getValue())) {
							if (taxrate.getName().contains("Compras")) {
								taxRatePorcet = taxrate;
							}

						}
					}

				}

				detalleRetencionCompra
						.setDrcPorcentaje(taxRatePorcet != null ? taxRatePorcet.getRateValue() : BigDecimal.ZERO);
				detalleRetencionCompra.setDrcValorRetenido(detalleRet.getAmount());
				detalleRetencionCompra.setRcoCodigo(retencionCompra);
				// codigo de retenion
				// verificar como se enviaria el codigo
				// PARA EL CASO DEL IVA
				if (detalleRet.getAccountBasedExpenseLineDetail().getAccountRef().getName().toUpperCase()
						.contains("IVA")) {

					TipoRetencion tipoRetencion = tipoRetencionRepository.findByTireCodigo("001").get();
					detalleRetencionCompra.setTireCodigo(tipoRetencion);

					System.out.println("VENDOR PORCENTAJE TIPO IVA " + taxRatePorcet.getRateValue().toString());
					Optional<Tipoivaretencion> tipoIva = tipoIvaRetencionRepository.findByTipivaretDescripcion(
							taxRatePorcet != null ? String.valueOf(taxRatePorcet.getRateValue().intValue()) : "0");
					if (tipoIva.isPresent()) {
						detalleRetencionCompra.setIdTipoivaretencion(tipoIva.get());

					}

				} else {
					// PARA EL CASO DEL RENTA
					TipoRetencion tipoRetencion = tipoRetencionRepository.findByTireCodigo(taxCode.getName()).get();
					detalleRetencionCompra.setTireCodigo(tipoRetencion);

				}

				// 1 ES RENTA 2 ES IVA
				detalleRetencionCompra.setDrcCodImpuestoAsignado(detalleRet.getAccountBasedExpenseLineDetail()
						.getAccountRef().getName().toUpperCase().contains("IVA") ? "2" : "1");

				// dependiendo la funcionalidad lo llenamos
				detalleRetencionCompra.setDrcDescripcion(detalleRet.getAccountBasedExpenseLineDetail().getAccountRef()
						.getName().toUpperCase().contains("IVA") ? "IVA" : "RENTA");
				detalleRetencionCompra.setDrcTipoRegistro(detalleRet.getAccountBasedExpenseLineDetail().getAccountRef()
						.getName().toUpperCase().contains("IVA") ? "IVA" : "R");
				detalleRetencionCompraRepository.save(detalleRetencionCompra);

			}

			/* Cambia el secuencial en la plataforma de QuickBooks */
			vendorCredit.setDocNumber("RT-"+numeroRetencionText);
			service.update(vendorCredit);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "ERROR AL CREAR LA RETENCION " + e.getMessage();
		}
		return "RETENCION REGISTRADA";
	}

	/* GENERA EL NUMERO DE DOCUMENTO DE LA FATURA */
	private String numeroFacturaTexto(Integer numeroFactura) {
		String numeroFacturaText = "";

//	      Integer numeroFactura=factRecup.getFacNumero();
		for (int i = numeroFactura.toString().length(); i < 9; i++) {
			numeroFacturaText = numeroFacturaText + "0";
		}
		numeroFacturaText = numeroFacturaText + numeroFactura;
		return numeroFacturaText;
		// System.out.println("nuemro texto " + numeroFacturaText);
	}

	private Vendor obtenerVendor(String idVendor) {
		String sql = "select * from vendor where id='" + idVendor + "'";
		// get DataService
		String realmId = valoresGlobales.REALMID;
		// String accessToken = valoresGlobales.TOKEN;
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
		DataService service;
		try {
			service = helper.getDataService(realmId, accessToken);
			QueryResult queryResult = service.executeQuery(sql);
			List<VendorCredit> retenciones = (List<VendorCredit>) queryResult.getEntities();
			System.out.println("VENDOR OBTENIDOS " + retenciones.size());
			Vendor resultado = (Vendor) (queryResult.getEntities().size() > 0 ? queryResult.getEntities().get(0)
					: null);
			return resultado;
		} catch (FMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	private ModelIdentificacion validarCedulaRuc(String valor) {
		ModelIdentificacion validador = new ModelIdentificacion("SIN VALIDAR", 4);
		try {
			if (valor.length() == 10) {
				validador = new ModelIdentificacion("C", 2);

			} else if (valor.length() == 13) {
				validador = new ModelIdentificacion("R", 1);
			} else {
				validador = new ModelIdentificacion("P", 3);
			}
		} catch (Exception e) {
			// TODO: handle exception
			validador = new ModelIdentificacion("NO SE PUEDE VALIDAR", 4);
			e.printStackTrace();
		}
		return validador;

	}
}
