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
import com.ec.g2g.entidad.DetalleNotaDebitoCredito;
import com.ec.g2g.entidad.DetalleRetencionCompra;
import com.ec.g2g.entidad.EstadoFacturas;
import com.ec.g2g.entidad.Factura;
import com.ec.g2g.entidad.NotaCreditoDebito;
import com.ec.g2g.entidad.Proveedores;
import com.ec.g2g.entidad.RetencionCompra;
import com.ec.g2g.entidad.TipoIdentificacionCompra;
import com.ec.g2g.entidad.TipoRetencion;
import com.ec.g2g.entidad.Tipoambiente;
import com.ec.g2g.entidad.Tipoivaretencion;
import com.ec.g2g.global.ValoresGlobales;
import com.ec.g2g.repository.CompraRepository;
import com.ec.g2g.repository.DetalleNotaCreditoRepository;
import com.ec.g2g.repository.DetalleRetencionCompraRepository;
import com.ec.g2g.repository.EstadoFacturaRepository;
import com.ec.g2g.repository.FacturaRepository;
import com.ec.g2g.repository.NotaCreditoRepository;
import com.ec.g2g.repository.ProveedorRepository;
import com.ec.g2g.repository.RetencionCompraRepository;
import com.ec.g2g.repository.TipoAmbienteRepository;
import com.ec.g2g.repository.TipoIdentificacionCompraRepository;
import com.ec.g2g.repository.TipoIvaRetencionRepository;
import com.ec.g2g.repository.TipoRetencionRepository;
import com.ec.g2g.utilitario.ArchivoUtils;
import com.google.gson.Gson;
import com.intuit.ipp.data.Line;
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
public class NotasCreditoQB {

	@Autowired
	public QBOServiceHelper helper;

	@Autowired
	private TipoAmbienteRepository tipoAmbienteRepository;
	@Autowired
	private ValoresGlobales valoresGlobales;

	@Autowired
	ManejarToken manejarToken;
	@Autowired
	NotaCreditoRepository notaCreditoRepository;
	@Autowired
	private FacturaRepository facturaRepository;
	private DetalleNotaCreditoRepository detalleNotaCreditoRepository;

	@Value("${posibilitum.nombre.empresa}")
	String NOMBREEMPRESA;

	@Value("${posibilitum.ruc.empresa}")
	String RUCEMPRESA;

	/* PARA OBTENER LOS IMPUESTOS */
	@Autowired
	TaxCodeQB taxCodeQB;
	@PersistenceContext
	private EntityManager entityManager;

	public List<NotaCreditoDebito> findUltimoSecuencial() {
		// cambiar la forma de traer el ultimo secuencial
		return entityManager.createQuery(
				"SELECT p FROM NotaCreditoDebitos p WHERE p.codTipoambiente.amRuc=:amRuc ORDER BY p.rcoSecuencial DESC",
				NotaCreditoDebito.class).setParameter("amRuc", RUCEMPRESA).setMaxResults(1).getResultList();
	}

	public void obtenerNotaCredito() {
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
				List<VendorCredit> notasCredito = (List<VendorCredit>) queryResult.getEntities();
				System.out.println("NOTAS DE CREDITO OBTENIDOS " + notasCredito.size());

				for (VendorCredit vendorCredit : notasCredito) {
					System.out.println("NUMERO DIGITOS  " + vendorCredit.getPrivateNote().length() + "   # DOCUM "
							+ vendorCredit.getDocNumber().toUpperCase());

					if (vendorCredit.getPrivateNote().length() == 17
							&& vendorCredit.getDocNumber().toUpperCase().contains("NC")) {
						String separaNumero[] = vendorCredit.getDocNumber().split("-");
						String numeroRetencion = separaNumero[1];
						/* VALIDAR SI EXISTE LA RETENCION */
//						Optional<RetencionCompra> retencionValida = retencionCompraRepository
//								.findByIdQuickOrRcoSecuencialText(Integer.valueOf(vendorCredit.getId()),
//										numeroRetencion, valoresGlobales.getTIPOAMBIENTE().getAmRuc());
//						if (!retencionValida.isPresent()) {
//							System.out.println("PROCESANDO NOTAS DE CREDITO --> " + mapperVendorToNotaCredito(vendorCredit));
//						} else {
//							System.out.println("LA NOTA DE CREDITO YA EXISTE " + vendorCredit.getDocNumber().toUpperCase());
//
//						}

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

	private String mapperVendorToNotaCredito(VendorCredit vendorCredit) {
		Gson gson = new Gson();
		try {

			/* CREAMOS LA CABECERA DE COMPRA PARA PODER GENERAR LA RETENCION */

			/* INICIA RETENCION */
			NotaCreditoDebito notaCreditoRecup = findUltimoSecuencial().size() > 0 ? findUltimoSecuencial().get(0)
					: null;
//			String JSONRECUPSEC = gson.toJson(cabeceraCompra);
//			System.out.println("CABECERA DE COMPRA  "+cabeceraCompra);
			// Obtiene el secuencial de la nota de credito
			// crear un campom para el secuencial de nota de credito
			Integer numeroRetencion = notaCreditoRecup != null ? notaCreditoRecup.getFacNumero() + 1
					: valoresGlobales.getTIPOAMBIENTE().getAmSecuencialInicioRetencion();
			String numeroRetencionText = numeroFacturaTexto(numeroRetencion);
			System.out.println("numeroRetencionText " + numeroRetencionText);
			NotaCreditoDebito creditoDebito = new NotaCreditoDebito();
//			creditoDebito.setFacDescripcion(vendorCredit.get);
			String claveAcceso = ArchivoUtils.generaClave(vendorCredit.getTxnDate(), "04",
					valoresGlobales.getTIPOAMBIENTE().getAmRuc(), valoresGlobales.getTIPOAMBIENTE().getAmCodigo(),
					valoresGlobales.getTIPOAMBIENTE().getAmEstab() + valoresGlobales.getTIPOAMBIENTE().getAmPtoemi(),
					numeroRetencionText, "12345678", "1");

			//recorre todos los detalles de vendorcredit
			for (Line detalleRet : vendorCredit.getLine()) {

				DetalleNotaDebitoCredito detalle = new DetalleNotaDebitoCredito();
				detalle.setIdNota(notaCreditoRecup);
			}

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
