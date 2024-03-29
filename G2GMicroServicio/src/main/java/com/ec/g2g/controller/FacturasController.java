package com.ec.g2g.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ec.g2g.global.ValoresGlobales;
import com.ec.g2g.quickbook.FacturasQB;
import com.ec.g2g.quickbook.ManejarToken;
import com.ec.g2g.quickbook.NotasCreditoQB;
import com.ec.g2g.quickbook.OAuth2PlatformClientFactory;
import com.ec.g2g.quickbook.QBOServiceHelper;
import com.ec.g2g.quickbook.ReporteFacturas;
import com.ec.g2g.quickbook.RetencionesQB;
import com.ec.g2g.repository.ReporteFacturasRepository;
import com.ec.g2g.utilitario.RealmId;
import com.intuit.ipp.data.Attachable;
import com.intuit.ipp.data.AttachableRef;
import com.intuit.ipp.data.ObjectNameEnumType;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import com.intuit.oauth2.client.DiscoveryAPIClient;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.data.DiscoveryAPIResponse;
import com.intuit.oauth2.exception.ConnectionException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author dderose
 *
 */

@RestController
@RequestMapping("/api")
@Api(value = "Facturas", tags = "Consulta facturas")
public class FacturasController {

	@Autowired
	OAuth2PlatformClientFactory factory;

	@Autowired
	public QBOServiceHelper helper;
	@Autowired
	HttpServletResponse response;
	@Autowired
	ManejarToken manejarToken;
	OAuth2PlatformClient client;

	@Autowired
	org.springframework.core.env.Environment env;

	@Autowired
	private ValoresGlobales valoresGlobales;

	@Autowired
	ReporteFacturas reporteFacturas;
	@Autowired
	ReporteFacturasRepository repository;

	private static final Logger logger = Logger.getLogger(FacturasController.class);
	private static final String failureMsg = "Failed";

	@Autowired
	private RetencionesQB retencionesQB;
	@Autowired
	private NotasCreditoQB creditoQB;

	@Autowired
	private FacturasQB facturasQB;

	@ResponseBody
	@RequestMapping("/conectar")
	@ApiOperation(value = "conectar", tags = "datos: conectar")
	public ResponseEntity<?> conectar() throws Exception {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");

		// initialize the config (set client id, secret)
		OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(env.getProperty("OAuth2AppClientId"),
				env.getProperty("OAuth2AppClientSecret")).callDiscoveryAPI(Environment.PRODUCTION).buildConfig();

		// generate csrf token
		String token = oauth2Config.generateCSRFToken();

		String redirectUri = env.getProperty("OAuth2AppRedirectUri");
		valoresGlobales.TOKEN = token;
		try {
			// prepare scopes
			List<Scope> scopes = new ArrayList<Scope>();
			scopes.add(Scope.Accounting);

			DiscoveryAPIResponse discoveryAPIResponse = new DiscoveryAPIClient()
					.callDiscoveryAPI(Environment.PRODUCTION);

			System.out.println("ENPOINTS DOSCPVERY: " + discoveryAPIResponse.getAuthorizationEndpoint());
			// prepare OAuth2Platform client
			OAuth2PlatformClient client = new OAuth2PlatformClient(oauth2Config);

			// retrieve access token by calling the token endpoint
			// BearerTokenResponse bearerTokenResponse =
			// client.retrieveBearerTokens(authCode, redirectUri);

			System.out.println("redirectUri " + redirectUri);
			System.out.println("URL DE AUTENTICASCION " + oauth2Config.prepareUrl(scopes, redirectUri, token));
			return new ResponseEntity<String>(oauth2Config.prepareUrl(scopes, redirectUri, token), httpHeaders,
					HttpStatus.OK);
			// return new RedirectView(oauth2Config.prepareUrl(scopes, redirectUri, token),
			// true, true, false);
			// response.sendRedirect(oauth2Config.prepareUrl(scopes, redirectUri, csrf));
		} catch (ConnectionException e) {
			logger.error("Exception calling connectToQuickbooks ", e);
			logger.error("intuit_tid: " + e.getIntuit_tid());
			logger.error("More info: " + e.getResponseContent());
			return new ResponseEntity<String>("ERROR " + e.getErrorMessage(), httpHeaders, HttpStatus.OK);
		}
		// return null;

	}

	public OAuth2PlatformClient getOAuth2PlatformClient() {
		return client;
	}

	/* para generar la prueba */
	@RequestMapping("/oauth2redirect")
	@ApiOperation(value = "oauth2redirect", tags = "datos: oauth2redirect")
	public String callBackFromOAuth(@RequestParam("code") String authCode, @RequestParam("state") String state,
			@RequestParam(value = "realmId", required = false) String realmId, HttpSession session) {
		logger.info("inside oauth2redirect of sample");
		try {
			String csrfToken = valoresGlobales.TOKEN;
			valoresGlobales.REALMID = realmId;
			if (csrfToken.equals(state)) {
//				session.setAttribute("realmId", realmId);
//				session.setAttribute("auth_code", authCode);

				OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
				String redirectUri = factory.getPropertyValue("OAuth2AppRedirectUri");
				logger.info("inside oauth2redirect of sample -- redirectUri " + redirectUri);

				BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);
				valoresGlobales.REFRESHTOKEN = bearerTokenResponse.getRefreshToken();
				valoresGlobales.TOKEN = bearerTokenResponse.getAccessToken();
//				session.setAttribute("access_token", bearerTokenResponse.getAccessToken());
//				session.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());

				// Update your Data store here with user's AccessToken and RefreshToken along
				// with the realmId

				return "connected";
			}
			logger.info("csrf token mismatch ");
		} catch (Exception e) {
			logger.error("Exception in callback handler ", e);
		}
		return null;
	}

	/* prebas de desarrollo */
//	consultar companias

	@ResponseBody
	@RequestMapping("/facturas")
	public ResponseEntity<?> facturas() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");
		String realmId = valoresGlobales.REALMID;
		if (StringUtils.isEmpty(realmId)) {
			return new ResponseEntity<String>("ERROR", httpHeaders, HttpStatus.BAD_REQUEST);
		}
		// String accessToken = valoresGlobales.TOKEN;
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
		try {

			// get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			// get all companyinfo
			// String sql = "select * from companyinfo";
			String sql = "select * from invoice";
			QueryResult queryResult = service.executeQuery(sql);

			System.out.println("QueryResult " + queryResult.getMaxResults());
			return new ResponseEntity<QueryResult>(queryResult, httpHeaders, HttpStatus.OK);

		}
		/*
		 * Handle 401 status code - If a 401 response is received, refresh tokens should
		 * be used to get a new access token, and the API call should be tried again.
		 */
		catch (InvalidTokenException e) {
			logger.error("Error while calling executeQuery :: " + e.getMessage());
			return new ResponseEntity<String>("ERROR " + e.getMessage(), httpHeaders, HttpStatus.BAD_REQUEST);

		} catch (FMSException e) {
			return new ResponseEntity<String>("ERROR " + e.getMessage(), httpHeaders, HttpStatus.BAD_REQUEST);
		}

	}

	@ResponseBody
	@RequestMapping("/verificar")
	public ResponseEntity<?> verificarconexion() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");
		String realmId = valoresGlobales.REALMID;
		if (StringUtils.isEmpty(realmId)) {
			return new ResponseEntity<String>("VERIFICAR", httpHeaders, HttpStatus.OK);
		}
		// String accessToken = valoresGlobales.TOKEN;
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
		try {

			// get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			// get all companyinfo
			// String sql = "select * from companyinfo";
			String sql = "select * from invoice";
			QueryResult queryResult = service.executeQuery(sql);

			System.out.println("QueryResult " + queryResult.getMaxResults());
			return new ResponseEntity<String>("numerofacturas: " + queryResult.getMaxResults(), httpHeaders,
					HttpStatus.OK);

		}
		/*
		 * Handle 401 status code - If a 401 response is received, refresh tokens should
		 * be used to get a new access token, and the API call should be tried again.
		 */
		catch (InvalidTokenException e) {
			logger.error("Error while calling executeQuery :: " + e.getMessage());
			return new ResponseEntity<String>("VERIFICAR", httpHeaders, HttpStatus.OK);

		} catch (FMSException e) {
			return new ResponseEntity<String>("VERIFICAR", httpHeaders, HttpStatus.OK);
		}

	}

	/* REPORTE DE FACTUTRAS */
	@RequestMapping(value = "/reporte-factura", method = RequestMethod.GET)
	@ApiOperation(tags = "Reporte de factura", value = "Obtiene las factura	s por un rango de tiempo formato 01-05-2022 (dd-MM-yyyy)")
	public ResponseEntity<?> reporteFacturas(String inicio, String fin) {

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");

//		repository.deleteByIdFacturaRep(13886);
		try {
			reporteFacturas.obtenerReporte(formatter.parse(inicio), formatter.parse(fin));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<String>("ERROR AL PROCESAR LOS DATOS", httpHeaders,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<String>("VERIFICAR", httpHeaders, HttpStatus.OK);
	}

	/* ENVIAMOS EL PDF A QUICK BOOKS */
	@RequestMapping(value = "/enviar-documentos", method = RequestMethod.GET)
	@ApiOperation(tags = "Envio de documentos a Quick Books", value = "Enviar documentos")
	public ResponseEntity<?> obtenerRealmId(String nombreArchivo, String pathArchivo, String tipoDocumento,
			String txtId) {

		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");

		try {

			// Dataservice
			String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
			DataService service = helper.getDataService(valoresGlobales.REALMID, accessToken);

			List<AttachableRef> listaAttach = new ArrayList<AttachableRef>();
			AttachableRef attachableRef = new AttachableRef();

			ReferenceType referenceType = new ReferenceType();

			if (tipoDocumento.equals("FACT")) {
				referenceType.setType("Invoice");
			} else if (tipoDocumento.equals("RET")) {
				referenceType.setType("VendorCredit");

			} else if (tipoDocumento.equals("NTC")) {
				referenceType.setType("CreditMemo");
			}
//				ObjectNameEnumType.CREDIT_MEMO.toString()

//			referenceType.setType(ObjectNameEnumType.VENDOR_CREDIT.toString());

			referenceType.setValue(txtId);

			attachableRef.setEntityRef(referenceType);

			listaAttach.add(attachableRef);

			// For attaching to Invoice or Bill or any Txn entity, Uncomment and replace the
			// Id and type of the Txn in below code
			Attachable attachable = new Attachable();
			attachable.setAttachableRef(listaAttach);

			attachable.setFileName(nombreArchivo);
			attachable.setContentType("application/pdf");
//			"/opt/posibilitum/FACT-003001000002354.pdf"
			java.io.File initialFile = new java.io.File(pathArchivo);
			InputStream targetStream = new FileInputStream(initialFile);
			Attachable uploaded = service.upload(attachable, targetStream);

			return new ResponseEntity<>(uploaded, httpHeaders, HttpStatus.OK);
		} catch (FMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<RealmId>(new RealmId(valoresGlobales.REALMID, valoresGlobales.TOKEN), httpHeaders,
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<RealmId>(new RealmId(valoresGlobales.REALMID, valoresGlobales.TOKEN), httpHeaders,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/* METODOS PARA PROBAR LOS DOCUMENTOS */
	/* ENVIAMOS EL PDF A QUICK BOOKS */
	@RequestMapping(value = "/probar-factura", method = RequestMethod.GET)
	@ApiOperation(tags = "Traer Documentos", value = "Facturas")
	public ResponseEntity<?> probarFacturas() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");

		try {
			facturasQB.obtenerFacturas();
			return new ResponseEntity<>("Correcto", httpHeaders, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<RealmId>(new RealmId(valoresGlobales.REALMID, valoresGlobales.TOKEN), httpHeaders,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/probar-retencion", method = RequestMethod.GET)
	@ApiOperation(tags = "Traer Documentos", value = "Retencion")
	public ResponseEntity<?> probarRetencion() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");

		try {

			retencionesQB.obtenerRetenciones();

			return new ResponseEntity<>("Correcto", httpHeaders, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<RealmId>(new RealmId(valoresGlobales.REALMID, valoresGlobales.TOKEN), httpHeaders,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/probar-nota-credito", method = RequestMethod.GET)
	@ApiOperation(tags = "Traer Documentos", value = "Nota de credito")
	public ResponseEntity<?> probarNotaCredito() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");

		try {
			creditoQB.obtenerNotaCredito();

			return new ResponseEntity<>("Correcto", httpHeaders, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<RealmId>(new RealmId(valoresGlobales.REALMID, valoresGlobales.TOKEN), httpHeaders,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/probar-factura-id", method = RequestMethod.GET)
	@ApiOperation(tags = "Traer Documentos", value = "Facturas")
	public ResponseEntity<?> probarFacturas(Integer idFactura) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("STATUS", "0");

		try {
			facturasQB.traerporId(idFactura);
			return new ResponseEntity<>("Correcto", httpHeaders, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<RealmId>(new RealmId(valoresGlobales.REALMID, valoresGlobales.TOKEN), httpHeaders,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
