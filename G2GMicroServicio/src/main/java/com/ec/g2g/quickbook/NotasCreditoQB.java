package com.ec.g2g.quickbook;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ec.g2g.ModelIdentificacion;
import com.ec.g2g.entidad.DetalleNotaDebitoCredito;
import com.ec.g2g.entidad.Factura;
import com.ec.g2g.entidad.NotaCreditoDebito;
import com.ec.g2g.entidad.Producto;
import com.ec.g2g.entidad.Tipoambiente;
import com.ec.g2g.global.ValoresGlobales;
import com.ec.g2g.repository.DetalleNotaCreditoRepository;
import com.ec.g2g.repository.FacturaRepository;
import com.ec.g2g.repository.NotaCreditoRepository;
import com.ec.g2g.repository.ProductoRepository;
import com.ec.g2g.repository.TipoAmbienteRepository;
import com.ec.g2g.utilitario.ArchivoUtils;
import com.google.gson.Gson;
import com.intuit.ipp.data.CreditMemo;
import com.intuit.ipp.data.CustomField;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.MemoRef;
import com.intuit.ipp.data.TaxCode;
import com.intuit.ipp.data.TaxRate;
import com.intuit.ipp.data.TaxRateDetail;
import com.intuit.ipp.data.Vendor;
import com.intuit.ipp.data.VendorCredit;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;

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
	@Autowired
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

	@Autowired
	private ProductoRepository productoRepository;

	public List<NotaCreditoDebito> findUltimoSecuencial() {
		// cambiar la forma de traer el ultimo secuencial
		return entityManager.createQuery(
				"SELECT u FROM NotaCreditoDebito u WHERE u.codTipoambiente =:codTipoambiente ORDER BY u.facNumero DESC",
				NotaCreditoDebito.class).setParameter("codTipoambiente", valoresGlobales.getTIPOAMBIENTE().getCodTipoambiente()).setMaxResults(1).getResultList();
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
//			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaConsulta = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(fechaConsulta);
			// reta loos dias que necesitas
			c.add(Calendar.DATE, -5);
			fechaConsulta = c.getTime();

			if (valoresGlobales.REALMID != null && valoresGlobales.REFRESHTOKEN != null) {
				// get DataService
				DataService service = helper.getDataService(realmId, accessToken);
				String WHERE = "";
				String ORDERBY = " ORDER BY Id ASC";
				WHERE = " WHERE Id > '" + valoresGlobales.getTIPOAMBIENTE().getAmIdNcInicio()
						+ "'  AND MetaData.CreateTime >= '" + format.format(fechaConsulta) + "'";

				String sql = "select * from creditmemo ";
				String QUERYFINAL = sql + WHERE + ORDERBY;
				System.out.println("QUERYFINAL " + QUERYFINAL);
				QueryResult queryResult = service.executeQuery(QUERYFINAL);
				List<CreditMemo> notasCredito = (List<CreditMemo>) queryResult.getEntities();
				System.out.println("NOTAS DE CREDITO OBTENIDOS " + notasCredito.size());

				for (CreditMemo vendorCredit : notasCredito) {
//					System.out.println("NUMERO DIGITOS  " + vendorCredit.getPrivateNote().length() + "   # DOCUM "
//							+ vendorCredit.getDocNumber().toUpperCase());

//					if (vendorCredit.getPrivateNote().length() == 17
//							&& vendorCredit.getDocNumber().toUpperCase().contains("NC")) {
//						String separaNumero[] = vendorCredit.getDocNumber().split("-");
//						String numeroRetencion = separaNumero[1];
						/* VALIDAR SI EXISTE LA RETENCION */
						Optional<NotaCreditoDebito> verificaDoc = notaCreditoRepository.findByTxnId(
								Integer.valueOf(vendorCredit.getId()),
								valoresGlobales.getTIPOAMBIENTE().getCodTipoambiente());
						if (!verificaDoc.isPresent()) {
							System.out.println(
									"PROCESANDO NOTAS DE CREDITO --> " + mapperVendorToNotaCredito(vendorCredit));
						} else {
							System.out.println(
									"LA NOTA DE CREDITO YA EXISTE " + vendorCredit.getDocNumber().toUpperCase());

						}

//					} else {
//						System.out.println("RETENCION NO PROCESADA EL NUMERO DE FACTURA NO CUMPLE CON LA ESTRUCTURA "
//								+ vendorCredit.getDocNumber());
//
//					}

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private String mapperVendorToNotaCredito(CreditMemo vendorCredit) {
		Gson gson = new Gson();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		try {
			String realmId = valoresGlobales.REALMID;
			// String accessToken = valoresGlobales.TOKEN;
			String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);

			// get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			/* CREAMOS LA CABECERA DE COMPRA PARA PODER GENERAR LA RETENCION */

			/* INICIA RETENCION */
			NotaCreditoDebito notaCreditoRecup = findUltimoSecuencial().size() > 0 ? findUltimoSecuencial().get(0)
					: null;
//			String JSONRECUPSEC = gson.toJson(cabeceraCompra);
//			System.out.println("CABECERA DE COMPRA  "+cabeceraCompra);
			// Obtiene el secuencial de la nota de credito
			// crear un campom para el secuencial de nota de credito
			Integer numeroNotaCredito = notaCreditoRecup != null ? notaCreditoRecup.getFacNumero() + 1
					: valoresGlobales.getTIPOAMBIENTE().getAmSecuencialInicioNc();
			String numeroRetencionText = numeroFacturaTexto(numeroNotaCredito);
			System.out.println("numeroRetencionText " + numeroRetencionText);
			NotaCreditoDebito notacredito = new NotaCreditoDebito();
			if (vendorCredit.getCustomField().size() > 0) {
				for (CustomField etiquetas : vendorCredit.getCustomField()) {
					if (etiquetas.getDefinitionId().equals("1")) {
						try {
							notacredito.setFechaEmisionFactura(format.parse(etiquetas.getStringValue()));
							notacredito.setFacFechaSustento(format.parse(etiquetas.getStringValue()));
						} catch (Exception e) {
							// TODO: handle exception
						}
					} else if (etiquetas.getDefinitionId().equals("2")) {
						notacredito.setFacDescripcion(etiquetas.getStringValue()); 
					} else if (etiquetas.getDefinitionId().equals("3")) {

						notacredito.setNumeroFactura(etiquetas.getStringValue());
					}

				}
			}

			/* CALCULOS PARA IVA Y SIN IVA */
			BigDecimal baseGrabada = BigDecimal.ZERO;
			BigDecimal baseCero = BigDecimal.ZERO;
			BigDecimal valorIva = BigDecimal.ZERO;
			BigDecimal subtotalFac = BigDecimal.ZERO;
			BigDecimal montoDescuento = BigDecimal.ZERO;
			List<Line> itemsRefDesc = vendorCredit.getLine();
			BigDecimal porcentajeDescuento = BigDecimal.ZERO;

			BigDecimal valorSinDescuento = BigDecimal.ZERO;
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
//					.value().equals("SUB_TOTAL_LINE_DETAIL")
					valorSinDescuento = valorSinDescuento.add(item.getAmount());
				}

			}

			if (porcentajeDescuento == BigDecimal.ZERO) {

				porcentajeDescuento = (montoDescuento.multiply(BigDecimal.valueOf(100)).divide(valorSinDescuento, 8,
						RoundingMode.FLOOR));

//				porcentajeDescuento = ArchivoUtils.redondearDecimales(porcentajeDescuento, 8);
			}

			for (Line valores : vendorCredit.getTxnTaxDetail().getTaxLine()) {
				if (valores.getTaxLineDetail().getTaxPercent().doubleValue() == 12) {
					baseGrabada = baseGrabada.add(valores.getTaxLineDetail().getNetAmountTaxable());
				} else {
					baseCero = baseCero.add(valores.getTaxLineDetail().getNetAmountTaxable());

				}

			}
			valorIva = baseGrabada.multiply(valoresGlobales.SACARIVA);
			/*CONSULTA A QB Y OBTENER LOS DATOS*/
			Optional<Factura> facturaRecup = facturaRepository.findByFacNumeroText(notacredito.getNumeroFactura());
			if (!facturaRecup.isPresent()) {
				return "No existe una factura para crear una nota de credito";
			}
			notacredito.setIdFactura(facturaRecup.get());
			notacredito.setFacFecha(vendorCredit.getTxnDate());
//			creditoDebito.setFacDescripcion(vendorCredit.get);
			String claveAcceso = ArchivoUtils.generaClave(vendorCredit.getTxnDate(), "04",
					valoresGlobales.getTIPOAMBIENTE().getAmRuc(), valoresGlobales.getTIPOAMBIENTE().getAmCodigo(),
					valoresGlobales.getTIPOAMBIENTE().getAmEstab() + valoresGlobales.getTIPOAMBIENTE().getAmPtoemi(),
					numeroRetencionText, "12345678", "1");
			subtotalFac = baseGrabada.add(baseCero);
			notacredito.setFacSubtotal(subtotalFac);
			BigDecimal valorTotalFact = ArchivoUtils.redondearDecimales(subtotalFac.add(valorIva), 2);
			notacredito.setFacIva(valorIva);
			notacredito.setFacTotal(valorTotalFact);
			notacredito.setFacEstado("EM");
			notacredito.setFacTipo("NCRE");
			notacredito.setFacAbono(BigDecimal.ZERO);
			notacredito.setFacSaldo(BigDecimal.ZERO);
//			notacredito.setFacDescripcion("NC QUICKBOOKS");
			notacredito.setFacNumProforma(0);
			notacredito.setTipodocumento("04");
			notacredito.setPuntoemision(valoresGlobales.TIPOAMBIENTE.getAmPtoemi());
			notacredito.setCodestablecimiento(valoresGlobales.TIPOAMBIENTE.getAmEstab());

			notacredito.setFacNumeroText(numeroFacturaTexto(numeroNotaCredito));
			notacredito.setFacDescuento(BigDecimal.ZERO);
			notacredito.setFacCodIce("3");
			notacredito.setFacCodIva("2");
			notacredito.setFacTotalBaseCero(baseCero);
			notacredito.setFacTotalBaseGravaba(baseGrabada);
			notacredito.setCodigoPorcentaje("2");
			notacredito.setFacPorcentajeIva("12");
			notacredito.setFacMoneda(vendorCredit.getCurrencyRef().getValue());

			notacredito.setFacPlazo(BigDecimal.ZERO);
			notacredito.setFacUnidadTiempo("DIAS");
			notacredito.setEstadosri("PENDIENTE");
			notacredito.setCodTipoambiente(valoresGlobales.getTIPOAMBIENTE().getCodTipoambiente());
			notacredito.setTipodocumentomod("01");
			notacredito.setFacNumero(numeroNotaCredito);
			notacredito.setFacClaveAcceso(claveAcceso);
			notacredito.setFacClaveAutorizacion(claveAcceso);
			notacredito.setTxnId(Integer.valueOf(vendorCredit.getId()));
			// recorre todos los detalles de vendorcredit

			notaCreditoRepository.save(notacredito);
			Item itemProd = null;
			Producto producto = new Producto();
			DetalleNotaDebitoCredito detalle = new DetalleNotaDebitoCredito();
			for (Line item : vendorCredit.getLine()) {

				detalle = new DetalleNotaDebitoCredito();
				detalle.setIdNota(notacredito);

				if (item.getGroupLineDetail() != null) {
					if (item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail() == null) {
						System.out.println("getSalesItemLineDetail NULL ");
						break;
					}
				} else {
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

				List<TaxRate> rateDetail = null;
				TaxRate taxRatePorcet = null;

				TaxCode taxCode = taxCodeQB.obtenerTaxCode(item.getGroupLineDetail() != null
						? item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail().getTaxCodeRef().getValue()
						: item.getSalesItemLineDetail().getTaxCodeRef().getValue());

				for (TaxRateDetail detail : taxCode.getSalesTaxRateList().getTaxRateDetail()) {
//				JSONCLIENTE = gson.toJson(detail);

					if (detail.getTaxRateRef().getName().contains("Ventas")) {
						for (TaxRate taxrate : taxCodeQB.obtenerTaxRateDetail(detail.getTaxRateRef().getValue())) {
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
				producto.setPordCostoVentaFinal(
						prodGrabaIva
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
					detalle.setIdProducto(prodRecup.get());
				} else {
					productoRepository.save(producto);
					detalle.setIdProducto(producto);
				}

				// revision con Paul es el campo getUnitPrice
				BigDecimal precioUnitario = item.getGroupLineDetail() != null
						? item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail().getUnitPrice()
						: item.getSalesItemLineDetail() != null ? item.getSalesItemLineDetail().getUnitPrice()
								: BigDecimal.ZERO;
				BigDecimal valorDescuento = BigDecimal.ZERO;
				if (precioUnitario.doubleValue() > 0 && porcentajeDescuento.doubleValue() > 0) {
					valorDescuento = precioUnitario.multiply(porcentajeDescuento).divide(BigDecimal.valueOf(100));
				}

				BigDecimal precioConDescuento = precioUnitario
						.subtract(precioUnitario.multiply(porcentajeDescuento).divide(BigDecimal.valueOf(100)));
				BigDecimal cantidadProductos = item.getGroupLineDetail() != null
						? item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail().getQty()
						: item.getSalesItemLineDetail() != null ? item.getSalesItemLineDetail().getQty() != null
								? item.getSalesItemLineDetail().getQty()
								: BigDecimal.ONE : BigDecimal.ONE;

				/* obtiene la cantidad dependiendo si es un item o grupo de items */
				BigDecimal cantidad = item.getGroupLineDetail() != null
						? item.getGroupLineDetail().getLine().get(0).getSalesItemLineDetail().getQty()
						: item.getSalesItemLineDetail().getQty();
				detalle.setDetCantidad(cantidad);
				detalle.setDetDescripcion(item.getDescription());
				detalle.setDetSubtotal(precioUnitario);

				BigDecimal ivaDet = BigDecimal.ZERO;

				ivaDet = prodGrabaIva
						? (precioConDescuento.multiply(valoresGlobales.SACARIVA).multiply(cantidadProductos))
						: BigDecimal.ZERO;
				detalle.setDetTotal(
						prodGrabaIva ? precioConDescuento.multiply(valoresGlobales.SUMARIVA) : precioConDescuento);
				detalle.setDetTipoVenta("NORMAL");
//			System.out.println("det.getDetTotal() " + det.getDetTotal());
//			System.out.println("cantidadProductos " + cantidadProductos);
				detalle.setDetTotalconiva(detalle.getDetTotal().multiply(cantidadProductos));

				detalle.setDetIva(prodGrabaIva ? ivaDet : BigDecimal.ZERO);
				detalle.setDetPordescuento(ArchivoUtils.redondearDecimales(porcentajeDescuento, 4));
				detalle.setDetValdescuento(valorDescuento);
				detalle.setDetSubtotaldescuento(precioConDescuento);
				detalle.setDetTotaldescuento(valorDescuento.multiply(cantidadProductos));
				detalle.setDetTotaldescuentoiva(detalle.getDetTotal().multiply(cantidadProductos));
				detalle.setDetCantpordescuento(valorDescuento.multiply(cantidadProductos));
				detalle.setDetSubtotaldescuentoporcantidad(precioConDescuento.multiply(cantidadProductos));
				detalle.setDetTipoVenta("0");
				detalle.setCodigoProducto(itemProd.getSku() == null ? "001" : itemProd.getSku());
				detalle.setProdGrabaIva(prodGrabaIva);
//				/* Detalle de factura */
				detalleNotaCreditoRepository.save(detalle);

			}

			/* REGISTRAR EL SECUENCIAL EN quICKbOOKS */
			MemoRef memoRef = new MemoRef();
			memoRef.setValue(claveAcceso);
			/* Cambia el secuencial en la plataforma de QuickBooks */
			vendorCredit.setDocNumber(numeroFacturaTexto(numeroNotaCredito));
			vendorCredit.setCustomerMemo(memoRef);
//			vendorCredit.setAllowIPNPayment(Boolean.TRUE);
			/* actualizo la factura en QB */
			service.update(vendorCredit);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "ERROR AL CREAR LA NOTA DE CREDITO " + e.getMessage();
		}
		return "NOTA DE CREDITO REGISTRADA";
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

	// consultar producto
	private Item getProduct(String value) {
		String realmId = valoresGlobales.REALMID;
		if (StringUtils.isEmpty(realmId)) {
//				logger.info("No realm ID.  QBO calls only work if the accounting scope was passed!");
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
//				logger.error("Error while calling executeQuery :: " + e.getMessage());
			e.printStackTrace();
			return null;

		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
//				list.forEach(error -> logger.error("Error while calling executeQuery :: " + error.getMessage()));
			return null;

		}

	}
}
