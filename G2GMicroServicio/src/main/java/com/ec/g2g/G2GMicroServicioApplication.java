package com.ec.g2g;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
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
import com.ec.g2g.quickbook.FacturasQB;
import com.ec.g2g.quickbook.ManejarToken;
import com.ec.g2g.quickbook.NotasCreditoQB;
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

	@Autowired
	private TipoAmbienteRepository tipoAmbienteRepository;


	@Value("${posibilitum.url.facturas}")
	String serviceURLFACTURAS;

	@Value("${posibilitum.url.reenvio}")
	String serviceURLREENVIO;

	@Value("${posibilitum.nombre.empresa}")
	String NOMBREEMPRESA;

	@Value("${posibilitum.ruc.empresa}")
	String RUCEMPRESA;
	@Value("${server.port}")
	String puerto;

	/* RETENCIOONES */
	@Autowired
	private RetencionesQB retencionesQB;
	@Autowired
	private NotasCreditoQB creditoQB;

	/* FACTURAS */

	@Autowired
	private FacturasQB facturasQB;

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
			tipoambiente.setAmIdNcInicio(99999999);
			tipoambiente.setAmSecuencialInicioNc(99999999);
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
			tipoambienteProd.setAmIdNcInicio(99999999);
			tipoambienteProd.setAmSecuencialInicioNc(99999999);
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

	// dejarlo cada 8 minutos
	@Scheduled(fixedRate = 4 * 60 * 1000)
	public void tareaReenviarFacturas() {
		RestTemplate restTemplate = new RestTemplate();
		RespuestaDocumentos respueta = restTemplate.getForObject(serviceURLREENVIO + RUCEMPRESA,
				RespuestaDocumentos.class);
		Gson gson = new Gson();
		String JSON = gson.toJson(respueta);
		System.out.println("RESPUESTA FACTURAS  AUTORIZADAS" + JSON);

	}

//dejarlo cada 8 minutos
	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void tareaProcesaFacturas() {
		RestTemplate restTemplate = new RestTemplate();
		RespuestaDocumentos respueta = restTemplate.getForObject(serviceURLFACTURAS + RUCEMPRESA,
				RespuestaDocumentos.class);
		Gson gson = new Gson();
		String JSON = gson.toJson(respueta);
		System.out.println("RESPUESTA REENVIO FACTURA" + JSON);

	}

	/* Tiempo de retenciones */
	@Scheduled(fixedRate = 4 * 60 * 1000)
	public void tareaRetenciones() {
		System.out.println("OBTIENE LOS DOCUMENTOS CADA 10 MINUTOS RETENCIONES : ");
		retencionesQB.obtenerRetenciones();
	}

	/* Tiempo de notas de credito */
	@Scheduled(fixedRate = 6 * 60 * 1000)
	public void tareanotaCredito() {
		System.out.println("OBTIENE LOS DOCUMENTOS CADA 10 MINUTOS RETENCIONES : ");
		creditoQB.obtenerNotaCredito();
	}

	/* Tiempo de facturas */
	@Scheduled(fixedRate = 3 * 60 * 1000)
	public void tareaFacturas() {

		facturasQB.obtenerFacturas();

	}



}
