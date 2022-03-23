package com.ec.g2g;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.ec.g2g.entidad.Cliente;
import com.ec.g2g.entidad.DetalleFactura;
import com.ec.g2g.entidad.EstadoFacturas;
import com.ec.g2g.entidad.Factura;
import com.ec.g2g.entidad.Parametrizar;
import com.ec.g2g.entidad.Producto;
import com.ec.g2g.entidad.Tipoadentificacion;
import com.ec.g2g.entidad.Tipoambiente;
import com.ec.g2g.entidad.Usuario;
import com.ec.g2g.global.ValoresGlobales;
import com.ec.g2g.quickbook.ManejarToken;
//import com.ec.g2g.quickbook.NotasCreditoQB;
import com.ec.g2g.quickbook.QBOServiceHelper;
import com.ec.g2g.quickbook.RetencionesQB;
import com.ec.g2g.quickbook.TaxCodeQB;
import com.ec.g2g.repository.ClienteRepository;
import com.ec.g2g.repository.DetalleFacturaRepository;
import com.ec.g2g.repository.EstadoFacturaRepository;
import com.ec.g2g.repository.FacturaRepository;
import com.ec.g2g.repository.FormaPagoRepository;
import com.ec.g2g.repository.ParametrizarRepository;
import com.ec.g2g.repository.ProductoRepository;
import com.ec.g2g.repository.TipoAmbienteRepository;
import com.ec.g2g.repository.TipoIdentificacionRepository;
import com.ec.g2g.repository.UsuarioRepository;
import com.ec.g2g.utilitario.ArchivoUtils;
import com.ec.g2g.utilitario.RespuestaDocumentos;
import com.google.gson.Gson;
import com.intuit.ipp.data.CustomField;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.MemoRef;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.TaxCode;
import com.intuit.ipp.data.TaxRate;
import com.intuit.ipp.data.TaxRateDetail;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;

import ch.qos.logback.core.joran.conditional.IfAction;

@SpringBootApplication
@EnableScheduling
public class G2GMicroServicioApplication extends SpringBootServletInitializer {

//	variables de entorno 
	@Autowired
	org.springframework.core.env.Environment env;
	@Autowired
	ValoresGlobales globales;
	@Autowired
	private ValoresGlobales valoresGlobales;

	@Autowired
	ManejarToken manejarToken;

	@Autowired
	public QBOServiceHelper helper;

	private int contador = 0;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private TipoIdentificacionRepository tipoIdentificacionRepository;
	@Autowired
	private EstadoFacturaRepository estadoFacturaRepository;

	@Autowired
	private FacturaRepository facturaRepository;

	@Autowired
	private TipoAmbienteRepository tipoAmbienteRepository;
	@Autowired
	private FormaPagoRepository formaPagoRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private DetalleFacturaRepository detalleFacturaRepository;

//	@Autowired
//	 private RestTemplate restTemplate;

	@Value("${posibilitum.url.facturas}")
	String serviceURLFACTURAS;

	@Value("${posibilitum.nombre.empresa}")
	String NOMBREEMPRESA;

	@Value("${posibilitum.ruc.empresa}")
	String RUCEMPRESA;
	@Value("${server.port}")
	String puerto;

	/* RETENCIOONES */
	@Autowired
	private RetencionesQB retencionesQB;
	/* RETENCIOONES */
	/*
	 * @Autowired private NotasCreditoQB notasCreditoQB;
	 */
	@Autowired
	UsuarioRepository usuarioRepository;

	@Autowired
	ParametrizarRepository parametrizarRepository;

	/* PARA OBTENER LOS IMPUESTOS */
	@Autowired
	TaxCodeQB taxCodeQB;

	@PersistenceContext
	private EntityManager entityManager;

	public List<Factura> findUltimoSecuencial() {
		return entityManager
				.createQuery("SELECT p FROM Factura p WHERE p.codTipoambiente.amRuc=:amRuc ORDER BY p.facNumero DESC",
						Factura.class)
				.setParameter("amRuc", RUCEMPRESA).setMaxResults(1).getResultList();
	}

	public static void main(String[] args) {
		SpringApplication.run(G2GMicroServicioApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(G2GMicroServicioApplication.class);
	}

//
	@PostConstruct
	public void init() {
		System.out.println("puerto " + puerto);
//		Optional<Cliente> clienetRecup = clienteRepository.findByCliCedulaAndCliNombre("1718264839",nombreCliente);
		Optional<Usuario> usuarioRecup = usuarioRepository.findByUsuLogin(RUCEMPRESA);
		if (!usuarioRecup.isPresent()) {
			Usuario usuario = new Usuario();
			usuario.setUsuNombre(NOMBREEMPRESA);
			usuario.setUsuLogin(RUCEMPRESA);
			usuario.setUsuPassword(RUCEMPRESA);
			usuario.setUsuNivel(1);
			usuario.setUsuTipoUsuario("ADMINISTRADOR");
			usuarioRepository.save(usuario);
		}

		// verifica si existe sino lo crea

		Optional<Tipoambiente> tipoAmbiente = tipoAmbienteRepository.findByAmEstadoAndAmRuc(Boolean.TRUE, RUCEMPRESA);
		if (!tipoAmbiente.isPresent()) {
			// PRUEBAS
			Tipoambiente tipoambiente = new Tipoambiente();

			tipoambiente.setAmDirBaseArchivos("//DOCUMENTOSRI");
			tipoambiente.setAmCodigo("1");
			tipoambiente.setAmDescripcion("PRUEBAS");
			tipoambiente.setAmEstado(Boolean.TRUE);
			tipoambiente.setAmIdEmpresa(1);
			tipoambiente.setAmUsuariosri("PRUEBA");
			tipoambiente.setAmUrlsri("celcer.sri.gob.ec");

			tipoambiente.setAmDirReportes("REPORTES");
			tipoambiente.setAmGenerados("GENERADOS");
			tipoambiente.setAmDirXml("XML");
			tipoambiente.setAmFirmados("FIRMADOS");
			tipoambiente.setAmTrasmitidos("TRASMITIDOS");
			tipoambiente.setAmDevueltos("DEVUELTOS");
			tipoambiente.setAmFolderFirma("FIRMA");
			tipoambiente.setAmAutorizados("AUTORIZADOS");
			tipoambiente.setAmNoAutorizados("NOAUTORIZADOS");
			tipoambiente.setAmTipoEmision("1");
			tipoambiente.setAmEnviocliente("ENVIARCLIENTE");
			tipoambiente.setAmRuc(RUCEMPRESA);
			tipoambiente.setAmNombreComercial(NOMBREEMPRESA);
			tipoambiente.setAmRazonSocial(NOMBREEMPRESA);
			tipoambiente.setAmDireccionMatriz("QUITO");
			tipoambiente.setAmDireccionSucursal("QUITO");
			tipoambiente.setAmIdFacturaInicio(99999999);
			tipoambiente.setAmSecuencialInicio(99999999);
			tipoambiente.setAmEnvioAutomatico(Boolean.FALSE);
			tipoambiente.setAmTaxCodRef("12");
			tipoambiente.setAmCargaInicial(Boolean.TRUE);
			tipoambiente.setAmPort("587");
			tipoambiente.setAmProtocol("smtp");
			tipoambiente.setAmIdRetencionInicio(99999999);
			tipoambiente.setAmSecuencialInicioRetencion(99999999);
			tipoambiente.setAmMicroEmp(Boolean.FALSE);
			tipoambiente.setAmAgeRet(Boolean.FALSE);
			tipoambiente.setAmContrEsp(Boolean.FALSE);
			tipoambiente.setAmExp(Boolean.FALSE);
			tipoambiente.setAmstadoPosibilitum(Boolean.FALSE);
			tipoambiente.setAmPuerto(puerto);
			tipoambiente.setAmRipme(Boolean.FALSE);
			tipoAmbienteRepository.save(tipoambiente);

			// PRODUCCION
			Tipoambiente tipoambienteProd = new Tipoambiente();
			tipoambienteProd.setAmDirBaseArchivos("//DOCUMENTOSRI");
			tipoambienteProd.setAmCodigo("2");
			tipoambienteProd.setAmDescripcion("PRODUCCION");
			tipoambienteProd.setAmEstado(Boolean.FALSE);
			tipoambienteProd.setAmIdEmpresa(1);
			tipoambienteProd.setAmUsuariosri("PRODUCCION");
			tipoambienteProd.setAmUrlsri("cel.sri.gob.ec");
			tipoambienteProd.setAmFolderFirma("FIRMA");
			tipoambienteProd.setAmDirReportes("REPORTES");
			tipoambienteProd.setAmGenerados("GENERADOS");
			tipoambienteProd.setAmDirXml("XML");
			tipoambienteProd.setAmFirmados("FIRMADOS");
			tipoambienteProd.setAmTrasmitidos("TRASMITIDOS");
			tipoambienteProd.setAmDevueltos("DEVUELTOS");
			tipoambienteProd.setAmAutorizados("AUTORIZADOS");
			tipoambienteProd.setAmNoAutorizados("NOAUTORIZADOS");
			tipoambienteProd.setAmTipoEmision("1");
			tipoambienteProd.setAmEnviocliente("ENVIARCLIENTE");
			tipoambienteProd.setAmRuc(RUCEMPRESA);
			tipoambienteProd.setAmNombreComercial(NOMBREEMPRESA);
			tipoambienteProd.setAmRazonSocial(NOMBREEMPRESA);
			tipoambienteProd.setAmDireccionMatriz("QUITO");
			tipoambienteProd.setAmDireccionSucursal("QUITO");
			tipoambienteProd.setAmIdFacturaInicio(1);
			tipoambienteProd.setAmSecuencialInicio(1);
			tipoambienteProd.setAmEnvioAutomatico(Boolean.FALSE);
			tipoambienteProd.setAmTaxCodRef("12");
			tipoambienteProd.setAmPort("587");
			tipoambienteProd.setAmProtocol("smtp");
			tipoambienteProd.setAmCargaInicial(Boolean.TRUE);
			tipoambienteProd.setAmIdRetencionInicio(99999999);
			tipoambienteProd.setAmSecuencialInicioRetencion(99999999);
			tipoambienteProd.setAmMicroEmp(Boolean.FALSE);
			tipoambienteProd.setAmAgeRet(Boolean.FALSE);
			tipoambienteProd.setAmContrEsp(Boolean.FALSE);
			tipoambienteProd.setAmExp(Boolean.FALSE);
			tipoambienteProd.setAmPuerto(puerto);
			tipoambienteProd.setAmstadoPosibilitum(Boolean.FALSE);
			tipoambienteProd.setAmRipme(Boolean.FALSE);
			tipoAmbienteRepository.save(tipoambienteProd);

			Parametrizar parametrizar = new Parametrizar();
			parametrizar.setParContactoEmpresa(tipoambiente.getAmRazonSocial());
			parametrizar.setParEmpresa(tipoambiente.getAmNombreComercial());
			parametrizar.setParRucEmpresa(tipoambiente.getAmRuc());
			parametrizar.setParIva(BigDecimal.valueOf(12));
			parametrizar.setParUtilidad(BigDecimal.ZERO);
			parametrizar.setParUtilidadPreferencial(BigDecimal.TEN);
			parametrizar.setParUtilidadPreferencialDos(BigDecimal.ZERO);
			parametrizar.setParEstado(Boolean.FALSE);
			parametrizar.setIsprincipal(Boolean.TRUE);
			parametrizar.setParDescuentoGeneral(BigDecimal.ZERO);
			parametrizar.setParCodigoIva("2");
			parametrizar.setParIvaActual(BigDecimal.valueOf(12));
			parametrizarRepository.save(parametrizar);
		}

	}

//dejarlo cada 8 minutos
	@Scheduled(fixedRate = 8 * 60 * 1000)
	public void tareaProcesaFacturas() {
		RestTemplate restTemplate = new RestTemplate();
		RespuestaDocumentos respueta = restTemplate.getForObject(serviceURLFACTURAS + RUCEMPRESA,
				RespuestaDocumentos.class);
		Gson gson = new Gson();
		String JSON = gson.toJson(respueta);
		System.out.println("RESPUESTA FACTURAS  AUTORIZADAS" + JSON);

	}

	/* Tiempo de retenciones */
	@Scheduled(fixedRate = 4 * 60 * 1000)
	public void tareaRetenciones() {
		System.out.println("OBTIENE LOS DOCUMENTOS CADA 10 MINUTOS RETENCIONES : ");
		retencionesQB.obtenerRetenciones();
	}

	/* Tiempo de notas de credito */
	/*
	 * @Scheduled(fixedRate = 12 * 60 * 1000) public void tareaNotaCredito() {
	 * System.out.
	 * println("OBTIENE LOS DOCUMENTOS CADA 12 MINUTOS NOTAS DE CREDITRO : "); //
	 * notasCreditoQB.obtenerRetenciones(); }
	 */

	/* Tiempo de facturas */
	@Scheduled(fixedRate = 3 * 60 * 1000)
	public void tareaFacturas() {

		Optional<Tipoambiente> tipoAmbiente = tipoAmbienteRepository.findByAmEstadoAndAmRuc(Boolean.TRUE, RUCEMPRESA);
		if (tipoAmbiente.isPresent()) {
			valoresGlobales.TIPOAMBIENTE = tipoAmbiente.get();
			System.out.println("TIPO AMBIENTE CARGADO");
		} else {
			System.out.println("TIPO AMBIENTE NULL NO PROCESA LAS FACTURAS");
			return;

		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date fechaConsulta = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(fechaConsulta);
		// reta loos dias que necesitas
		c.add(Calendar.DATE, -5);
		fechaConsulta = c.getTime();

		System.out.println("format.fechaConsulta  " + format.format(fechaConsulta));
		contador++;
		System.out.println("OBTIENE LOS DOCUMENTOS CADA 5 MINUTOS FACTURAS --> CONTADOR : " + contador);

		if (valoresGlobales.REALMID != null && valoresGlobales.REFRESHTOKEN != null) {
			Tipoambiente recup = tipoAmbiente.get();
			if (!recup.getAmstadoPosibilitum()) {
				recup.setAmstadoPosibilitum(Boolean.TRUE);
				tipoAmbienteRepository.save(recup);

			}

			String realmId = valoresGlobales.REALMID;
			// String accessToken = valoresGlobales.TOKEN;
			String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
			try {

				// get DataService
				DataService service = helper.getDataService(realmId, accessToken);
				String WHERE = "";
				String ORDERBY = " ORDER BY DocNumber ASC";

				if (valoresGlobales.getTIPOAMBIENTE().getAmCargaInicial()) {
					WHERE = " WHERE Id > '" + valoresGlobales.getTIPOAMBIENTE().getAmIdFacturaInicio()
							+ "'  AND MetaData.CreateTime >= '" + format.format(fechaConsulta) + "'";
					// + "' AND MetaData.CreateTime >= '2021-11-04' ";
				} else {

					WHERE = " WHERE MetaData.CreateTime >= '" + format.format(new Date()) + "'";
				}

				String sql = "select * from invoice ";
				String QUERYFINAL = sql + WHERE + ORDERBY;
				System.out.println("QUERYFINAL " + QUERYFINAL);
				QueryResult queryResult = service.executeQuery(QUERYFINAL);

				List<Invoice> facturas = (List<Invoice>) queryResult.getEntities();
				System.out.println("QueryResult numero de elementos " + facturas.size());
				/* crear el cliente */

				Producto producto = new Producto();
				Factura factura = new Factura();
				DetalleFactura det = new DetalleFactura();

				for (Invoice invoice : facturas) {
					Gson gson = new Gson();
					String JSON = gson.toJson(invoice);
					System.out.println("JSON FACTURAAAAAAAAAAAAAAAA " + JSON);
					Optional<Factura> facturaRegistrada = facturaRepository.findByTxnId(
							Integer.valueOf(invoice.getId()), valoresGlobales.getTIPOAMBIENTE().getAmRuc());

//					Customer cliente = getFacturaCostumer("24");
//					String JSONCLIENTE = gson.toJson(customer);
					if (!facturaRegistrada.isPresent()) {
						/* ACTUALIZA EL DATOS DEL CLIENTE */
						ReferenceType clienteRef = invoice.getCustomerRef();
						Customer customer = getFacturaCostumer(clienteRef.getValue());

						// System.out.println("FECHA " + new Timestamp(invoice.getTxnDate().getTime()));
						factura = new Factura();

						factura.setTxnId(Integer.valueOf(invoice.getId()));
//					System.out.println("JSON CLIENTE " + JSONCLIENTE);
						// verifica si existe el cliente
						EstadoFacturas estadoFacturas = estadoFacturaRepository.findByEstCodigo("PE");
						// agrego el cliente
						factura.setIdCliente(mapperCliente(customer));
						// estado de la factura interno
						factura.setIdEstado(estadoFacturas);
						// numero de documento desde quick en el campo facNotaEntregaProcess numero de
						// factura quick
						factura.setFacSecuencialUnico(invoice.getDocNumber());
//					ultima factura registrada
						Factura factRecup = findUltimoSecuencial().size() > 0 ? findUltimoSecuencial().get(0) : null;
						// si no exite una factura coloca el numero 1
						Integer numeroFactura = factRecup != null ? factRecup.getFacNumero() + 1
								: valoresGlobales.getTIPOAMBIENTE().getAmSecuencialInicio();

						/* CALCULOS PARA IVA Y SIN IVA */
						BigDecimal baseGrabada = BigDecimal.ZERO;
						BigDecimal baseCero = BigDecimal.ZERO;

						for (Line valores : invoice.getTxnTaxDetail().getTaxLine()) {
							if (valores.getTaxLineDetail().getTaxPercent().doubleValue() == 12) {
								baseGrabada = baseGrabada.add(valores.getTaxLineDetail().getNetAmountTaxable());
							} else {
								baseCero = baseCero.add(valores.getTaxLineDetail().getNetAmountTaxable());

							}

						}

						/* para verificar el descuento */
						List<Line> itemsRefDesc = invoice.getLine();
						BigDecimal porcentajeDescuento = BigDecimal.ZERO;
						BigDecimal montoDescuento = BigDecimal.ZERO;
						BigDecimal valorSinDescuento = BigDecimal.ZERO;
						BigDecimal subtotalFac = BigDecimal.ZERO;
						BigDecimal valorIva = BigDecimal.ZERO;

						for (Line item : itemsRefDesc) {

							if (item.getDetailType() == LineDetailTypeEnum.DISCOUNT_LINE_DETAIL) {
								montoDescuento = item.getAmount();

								if (item.getDiscountLineDetail().isPercentBased()) {

									porcentajeDescuento = item.getDiscountLineDetail() != null
											? item.getDiscountLineDetail().getDiscountPercent()
											: BigDecimal.ZERO;
								}
							}

							if (item.getDetailType() == LineDetailTypeEnum.SUB_TOTAL_LINE_DETAIL) {
//								.value().equals("SUB_TOTAL_LINE_DETAIL")
								valorSinDescuento = valorSinDescuento.add(item.getAmount());
							}

						}

						if (porcentajeDescuento == BigDecimal.ZERO) {

							porcentajeDescuento = (montoDescuento.multiply(BigDecimal.valueOf(100))
									.divide(valorSinDescuento, 8, RoundingMode.FLOOR));

//							porcentajeDescuento = ArchivoUtils.redondearDecimales(porcentajeDescuento, 8);
						}

						subtotalFac = baseGrabada.add(baseCero);
						valorIva = baseGrabada.multiply(valoresGlobales.SACARIVA);
						factura.setFacFecha(invoice.getTxnDate());
						factura.setFacFechaCobroPlazo(invoice.getDueDate());
						/* CALCULAR LOS DIAS DE PLAZO */
						long diffrence = 0;
						if (invoice.getDueDate() != null) {
							long diff = invoice.getDueDate().getTime() - invoice.getTxnDate().getTime();
							TimeUnit time = TimeUnit.DAYS;
							diffrence = time.convert(diff, TimeUnit.MILLISECONDS);
							System.out.println("The difference in days is : " + diffrence);
						}

						// subtotal
						factura.setFacSubtotal(subtotalFac);
//					 Iva
						factura.setFacIva(valorIva);
//					 TOTAL DE LA FATURA
						BigDecimal valorTotalFact = ArchivoUtils.redondearDecimales(subtotalFac.add(valorIva), 2);
						factura.setFacTotal(valorTotalFact);
						factura.setFacObservacion(invoice.getPrivateNote() != null ? invoice.getPrivateNote() : "");
						factura.setFacEstado("PA");
						factura.setFacTipo("FACT");
						factura.setFacAbono(BigDecimal.ZERO);
						factura.setFacSaldo(BigDecimal.ZERO);
						factura.setFacDescripcion("S/N");
						factura.setTipodocumento("01");
						factura.setFacNumProforma(0);
						factura.setPuntoemision(valoresGlobales.TIPOAMBIENTE.getAmPtoemi());
						factura.setCodestablecimiento(valoresGlobales.TIPOAMBIENTE.getAmEstab());
						factura.setFacNumero(numeroFactura);
						factura.setFacNumeroText(numeroFacturaTexto(numeroFactura));
						factura.setFacDescuento(montoDescuento);
						factura.setFacCodIce("3");
						factura.setFacCodIva("2");
						factura.setFacTotalBaseCero(baseCero);
						factura.setFacTotalBaseGravaba(baseGrabada);
						factura.setCodigoPorcentaje("2");
						factura.setFacPorcentajeIva("12");
						factura.setFacMoneda(invoice.getCurrencyRef().getValue());
						factura.setIdFormaPago(formaPagoRepository.findById(7).get());
						factura.setFacPlazo(BigDecimal.valueOf(diffrence));
						factura.setFacUnidadTiempo("DIAS");
						factura.setEstadosri("PENDIENTE");
						factura.setCodTipoambiente(valoresGlobales.getTIPOAMBIENTE());
						factura.setFacSubsidio(BigDecimal.ZERO);
						factura.setFacValorSinSubsidio(BigDecimal.ZERO);

						/* vendedor */
						if (invoice.getCustomField().size() > 0) {
							for (CustomField etiquetas : invoice.getCustomField()) {
								if (etiquetas.getDefinitionId().equals("1")) {
									factura.setFacPlaca(etiquetas.getName());
									factura.setFacMarca(etiquetas.getStringValue());
								} else if (etiquetas.getDefinitionId().equals("2")) {
									factura.setFacCilindraje(etiquetas.getName());
									factura.setFacKilometraje(etiquetas.getStringValue());
								} else if (etiquetas.getDefinitionId().equals("3")) {
									factura.setFacChasis(etiquetas.getName());
									factura.setFacMadre(etiquetas.getStringValue());
								}

							}
						}

						// INGRESO EL NOMBRE DE LA EMPRESA
						factura.setFacTipoIdentificadorComprobador(NOMBREEMPRESA);

						/* CALVE DE ACCESO */
						String claveAcceso = ArchivoUtils.generaClave(factura.getFacFecha(), "01",
								valoresGlobales.getTIPOAMBIENTE().getAmRuc(),
								valoresGlobales.getTIPOAMBIENTE().getAmCodigo(),
								valoresGlobales.getTIPOAMBIENTE().getAmEstab()
										+ valoresGlobales.getTIPOAMBIENTE().getAmPtoemi(),
								factura.getFacNumeroText(), "12345678", "1");
						factura.setFacClaveAcceso(claveAcceso);
						facturaRepository.save(factura);

//					Producto

						System.out.println("DETALLE FACTURAAAAAAAAAAAAAAAAAAZ ");
						List<Line> itemsRef = invoice.getLine();
						Item itemProd = null;
						System.out.println("NUMERO DE DETALLES " + itemsRef.size());
						String JSONDETALLE = gson.toJson(itemsRef);

						System.out.println("DETALLE FACTURAAAAAAAAAAAAAAAAAAZ " + JSONDETALLE);
						int contadorLine = 0;
						for (Line item : itemsRef) {
							// detalle de factura
							det = new DetalleFactura();
							contadorLine++;

							
							if (item.getGroupLineDetail()!= null) {
								if (item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail() == null) {
									System.out.println("getSalesItemLineDetail NULL ");
									break;
								}
							}else {
								if (item.getSalesItemLineDetail() == null) {
									System.out.println("getSalesItemLineDetail NULL ");
									break;
								}
							}
							

							if (item.getGroupLineDetail() != null) {

								
									itemProd = getProduct(item.getGroupLineDetail().getGroupItemRef() != null
											? item.getGroupLineDetail().getGroupItemRef().getValue()
											: "0");
							

							} else {
								itemProd = getProduct(item.getSalesItemLineDetail() != null
										? item.getSalesItemLineDetail().getItemRef().getValue()
										: "0");

							}

							String JSONPRODCUTO = gson.toJson(itemProd);

							System.out.println("JSONPRODCUTO " + JSONPRODCUTO);
							if (itemProd == null) {
								System.out.println("itemProd NULL ");
								break;

							}

							producto = new Producto();

							/* EL PRODUCTO GRABA IVA */
							// cambiar para verificar si graba o no iva dependiendo del detalle de la
							// factura con taxcoderef
//						Boolean prodGrabaIva = itemProd.getSalesTaxCodeRef() != null
//								? (itemProd.getSalesTaxCodeRef().getValue().contains("12%") ? Boolean.TRUE
//										: Boolean.FALSE)
//								: Boolean.FALSE;

							List<TaxRate> rateDetail = null;
							TaxRate taxRatePorcet = null;

							TaxCode taxCode = taxCodeQB
									.obtenerTaxCode(item.getGroupLineDetail()!=null?item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail().getTaxCodeRef().getValue(): item.getSalesItemLineDetail().getTaxCodeRef().getValue());

							for (TaxRateDetail detail : taxCode.getSalesTaxRateList().getTaxRateDetail()) {
//							JSONCLIENTE = gson.toJson(detail);

								if (detail.getTaxRateRef().getName().contains("Ventas")) {
									for (TaxRate taxrate : taxCodeQB
											.obtenerTaxRateDetail(detail.getTaxRateRef().getValue())) {
										if (taxrate.getName().contains("Ventas")) {
											taxRatePorcet = taxrate;
										}

									}
								}

							}

							Boolean prodGrabaIva = taxRatePorcet != null
									? taxRatePorcet.getRateValue().doubleValue() == 0 ? Boolean.FALSE : Boolean.TRUE
									: Boolean.FALSE;

							producto.setProdCodigo(itemProd.getSku() == null ? "001" : itemProd.getSku());
							producto.setProdNombre(itemProd.getDescription());
							producto.setPordCostoCompra(BigDecimal.ZERO);
							producto.setPordCostoVentaRef(BigDecimal.ZERO);
							producto.setPordCostoVentaFinal(prodGrabaIva
									? (itemProd.getUnitPrice() == null ? BigDecimal.ZERO
											: itemProd.getUnitPrice().multiply(valoresGlobales.IVA))
									: itemProd.getUnitPrice());
							producto.setProdEstado(1);
							producto.setProdTrasnporte(BigDecimal.ZERO);
							producto.setProdIva(BigDecimal.ZERO);
							producto.setProdUtilidadNormal(BigDecimal.ZERO);
							producto.setProdManoObra(BigDecimal.ZERO);
							producto.setProdUtilidadPreferencial(BigDecimal.ZERO);
							producto.setProdCostoPreferencial(BigDecimal.ZERO);
							producto.setProdCostoPreferencialDos(BigDecimal.ZERO);
							producto.setProdCostoPreferencialTres(BigDecimal.ZERO);
							producto.setProdPrincipal(1);
							producto.setProdAbreviado("S/N");
							producto.setProdIsPrincipal(Boolean.FALSE);
							producto.setPordCostoCompra(BigDecimal.ZERO);
							producto.setProdCantidadInicial(0);
							producto.setProdUtilidadDos(BigDecimal.ZERO);
							producto.setProdCantMinima(BigDecimal.ZERO);
							producto.setProdPathCodbar("");
							producto.setProdImprimeCodbar(Boolean.FALSE);
							producto.setProdGrabaIva(prodGrabaIva);
							producto.setProdEsproducto(Boolean.FALSE);
							producto.setProdSubsidio(BigDecimal.ZERO);
							producto.setProdTieneSubsidio("N");
							producto.setProdPrecioSinSubsidio(BigDecimal.ZERO);
							producto.setProGlp("");
							producto.setPordCostoPromedioCompra(BigDecimal.ZERO);
							producto.setProdFactorConversion(BigDecimal.ONE);
							producto.setProdUnidadMedida("UNIDAD");
							producto.setProdUnidadConversion("UNIDAD");

							Optional<Producto> prodRecup = productoRepository
									.findByProdCodigo(itemProd.getSku() == null ? "001" : itemProd.getSku());

							if (prodRecup.isPresent()) {
								det.setIdProducto(prodRecup.get());
							} else {
								productoRepository.save(producto);
								det.setIdProducto(producto);
							}
							// revision con Paul es el campo getUnitPrice
							BigDecimal precioUnitario = item.getGroupLineDetail()!=null?item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail().getUnitPrice()
									: item.getSalesItemLineDetail() != null
									? item.getSalesItemLineDetail().getUnitPrice()
									: BigDecimal.ZERO;
							BigDecimal valorDescuento = BigDecimal.ZERO;
							if (precioUnitario.doubleValue() > 0 && porcentajeDescuento.doubleValue() > 0) {
								valorDescuento = precioUnitario.multiply(porcentajeDescuento)
										.divide(BigDecimal.valueOf(100));
							}

//							if (item.getDetailType() == LineDetailTypeEnum.DISCOUNT_LINE_DETAIL) {
//								if (!item.getDiscountLineDetail().isPercentBased()) {
//
//									valorDescuento = precioUnitario.multiply(porcentajeDescuento)
//											.divide(BigDecimal.valueOf(100));
//								}
//							}

							BigDecimal precioConDescuento = precioUnitario.subtract(
									precioUnitario.multiply(porcentajeDescuento).divide(BigDecimal.valueOf(100)));
							BigDecimal cantidadProductos = item.getGroupLineDetail()!=null?item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail().getQty()
									:item.getSalesItemLineDetail() != null
									? item.getSalesItemLineDetail().getQty() != null
											? item.getSalesItemLineDetail().getQty()
											: BigDecimal.ONE
									: BigDecimal.ONE;

							det.setIdFactura(factura);
							/*obtiene la cantidad dependiendo si es un item o grupo de items*/
							BigDecimal cantidad=item.getGroupLineDetail()!=null?item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail().getQty()
									: item.getSalesItemLineDetail().getQty();
							det.setDetCantidad(cantidad);
							det.setDetDescripcion(item.getDescription());
							det.setDetSubtotal(precioUnitario);

							BigDecimal ivaDet = BigDecimal.ZERO;

							ivaDet = prodGrabaIva
									? (precioConDescuento.multiply(valoresGlobales.SACARIVA)
											.multiply(cantidadProductos))
									: BigDecimal.ZERO;
							det.setDetTotal(prodGrabaIva ? precioConDescuento.multiply(valoresGlobales.SUMARIVA)
									: precioConDescuento);
							det.setDetTipoVenta("NORMAL");
//						System.out.println("det.getDetTotal() " + det.getDetTotal());
//						System.out.println("cantidadProductos " + cantidadProductos);
							det.setDetTotalconiva(det.getDetTotal().multiply(cantidadProductos));

							det.setDetIva(prodGrabaIva ? ivaDet : BigDecimal.ZERO);
							det.setDetPordescuento(ArchivoUtils.redondearDecimales(porcentajeDescuento, 4));
							det.setDetValdescuento(valorDescuento);
							det.setDetSubtotaldescuento(precioConDescuento);
							det.setDetTotaldescuento(valorDescuento.multiply(cantidadProductos));
							det.setDetTotaldescuentoiva(det.getDetTotal().multiply(cantidadProductos));
							det.setDetCantpordescuento(valorDescuento.multiply(cantidadProductos));
							det.setDetSubtotaldescuentoporcantidad(precioConDescuento.multiply(cantidadProductos));
							det.setDetTipoVenta("0");
							det.setDetCodIva("2");
							det.setDetTarifa(prodGrabaIva ? BigDecimal.valueOf(12) : BigDecimal.ZERO);
							det.setDetCodPorcentaje(prodGrabaIva ? "2" : "0");
							/* Detalle de factura */
							detalleFacturaRepository.save(det);
						}

						/* REGISTRAR EL SECUENCIAL EN quICKbOOKS */
						MemoRef memoRef = new MemoRef();
						memoRef.setValue(claveAcceso);
						/* Cambia el secuencial en la plataforma de QuickBooks */
						invoice.setDocNumber(numeroFacturaTexto(numeroFactura));
						invoice.setCustomerMemo(memoRef);
						invoice.setAllowIPNPayment(Boolean.TRUE);
						/* actualizo la factura en QB */
						service.update(invoice);
					}
				}
				// return new ResponseEntity<QueryResult>(queryResult, httpHeaders,
				// HttpStatus.OK);

			}
			/*
			 * Handle 401 status code - If a 401 response is received, refresh tokens should
			 * be used to get a new access token, and the API call should be tried again.
			 */
			catch (InvalidTokenException e) {
				e.printStackTrace();
				logger.error("Error while calling executeQuery :: " + e.getMessage());
				// return new ResponseEntity<String>("ERROR " + e.getMessage(), httpHeaders,
				// HttpStatus.BAD_REQUEST);

			} catch (FMSException e) {
				e.printStackTrace();
				// return new ResponseEntity<String>("ERROR " + e.getMessage(), httpHeaders,
				// HttpStatus.BAD_REQUEST);
				logger.error("Error while calling FMSException :: " + e.getMessage());
			}
		} else {
			System.out.println("REALMID NULL");
			Tipoambiente recup = tipoAmbiente.get();
			recup.setAmstadoPosibilitum(Boolean.FALSE);
			tipoAmbienteRepository.save(recup);

		}
	}

	// consultar clientes
	private Customer getFacturaCostumer(String value) {
		String realmId = valoresGlobales.REALMID;
		if (StringUtils.isEmpty(realmId)) {
			logger.info("No realm ID.  QBO calls only work if the accounting scope was passed!");
			return null;
		}
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);

		try {

			// Dataservice
			DataService service = helper.getDataService(realmId, accessToken);

			// get all Facturas
			String sql = "select * from customer where id = '" + value + "'";
			QueryResult queryResult = service.executeQuery(sql);
			Customer cliente = (Customer) queryResult.getEntities().get(0);
			return cliente;
		}
		/*
		 * Manejo de excepcion error de token
		 */
		catch (InvalidTokenException e) {
			logger.error("Error while calling executeQuery :: " + e.getMessage());
			e.printStackTrace();
			return null;

		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling executeQuery :: " + error.getMessage()));
			e.printStackTrace();
			return null;

		}

	}

	// consultar producto
	private Item getProduct(String value) {
		String realmId = valoresGlobales.REALMID;
		if (StringUtils.isEmpty(realmId)) {
			logger.info("No realm ID.  QBO calls only work if the accounting scope was passed!");
			return null;
		}
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);

		try {

			// Dataservice
			DataService service = helper.getDataService(realmId, accessToken);
			// System.out.println("CONSULYTA A PRODUCTO select * from Item where id =" +
			// value);
			// get all Facturas
			String sql = "select * from Item where id = '" + value + "'";
			QueryResult queryResult = service.executeQuery(sql);
			Item resultado = (Item) (queryResult.getEntities().size() > 0 ? queryResult.getEntities().get(0) : null);
			return resultado;
		}
		/*
		 * Manejo de excepcion error de token
		 */
		catch (InvalidTokenException e) {
			logger.error("Error while calling executeQuery :: " + e.getMessage());
			e.printStackTrace();
			return null;

		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling executeQuery :: " + error.getMessage()));
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

	private Cliente mapperCliente(Customer customer) {
		String identificacion = "9999999999999";
		if (customer.getPrimaryTaxIdentifier() != null) {
			identificacion = customer.getPrimaryTaxIdentifier().contains("X")
					? customer.getNotes() != null ? customer.getNotes() : "-1"
					: customer.getPrimaryTaxIdentifier();
		}

		Cliente cliente = null;
		String nombreCliente = customer.getFullyQualifiedName() != null ? customer.getFullyQualifiedName() : "S/N";
		Optional<Cliente> clienetRecup = clienteRepository.findByCliCedulaAndCliNombre(identificacion, nombreCliente);
		if (clienetRecup.isPresent()) {

			Cliente cliente2 = clienetRecup.get();
			cliente2.setCliCedula(identificacion);
			cliente2.setCliCorreo(
					customer.getPrimaryEmailAddr() != null ? customer.getPrimaryEmailAddr().getAddress() : "S/N");
			cliente2.setCliTelefono(
					customer.getPrimaryPhone() != null ? customer.getPrimaryPhone().getFreeFormNumber() : "");
			cliente2.setCliDireccion(customer.getBillAddr() != null ? customer.getBillAddr().getLine1() : "");
			clienteRepository.save(cliente2);
			return cliente2;
		} else {
			cliente = new Cliente();

			cliente.setCiudad(customer.getShipAddr() != null ? customer.getShipAddr().getCity() : "QUITO");
			cliente.setCliApellidos(customer.getFamilyName() != null ? customer.getFamilyName() : "S/N");
			cliente.setCliCedula(identificacion);
			cliente.setCliNombre(customer.getFullyQualifiedName() != null ? customer.getFullyQualifiedName() : "S/N");
			cliente.setCliRazonSocial(
					customer.getFullyQualifiedName() != null ? customer.getFullyQualifiedName() : "S/N");
			cliente.setCliDireccion(customer.getBillAddr() != null ? customer.getBillAddr().getLine1() : "S/N");
			cliente.setCliTelefono(
					customer.getPrimaryPhone() != null ? customer.getPrimaryPhone().getFreeFormNumber() : "");
			cliente.setCliMovil("");
			cliente.setCliCorreo(
					customer.getPrimaryEmailAddr() != null ? customer.getPrimaryEmailAddr().getAddress() : "S/N");
			cliente.setClietipo(0);
			// buscar el identificador

			Optional<Tipoadentificacion> tipoadentificacion = tipoIdentificacionRepository
					.findById(validarCedulaRuc(identificacion).getCodigo());
			cliente.setIdTipoIdentificacion(tipoadentificacion.get());
			cliente.setCliNombres(customer.getGivenName() != null ? customer.getGivenName() : "S/N");
			cliente.setCliApellidos(customer.getFamilyName() != null ? customer.getFamilyName() : "S/N");
			// guarda y registra el clinete en la cabecera de la factura
			clienteRepository.save(cliente);
			return cliente;
		}
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

}
