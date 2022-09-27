package com.ec.g2g.quickbook;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ec.g2g.ModelIdentificacion;
import com.ec.g2g.entidad.DetalleNotaDebitoCredito;
import com.ec.g2g.entidad.Factura;
import com.ec.g2g.entidad.FacturaReporte;
import com.ec.g2g.entidad.NotaCreditoDebito;
import com.ec.g2g.entidad.Producto;
import com.ec.g2g.entidad.Tipoambiente;
import com.ec.g2g.global.ValoresGlobales;
import com.ec.g2g.repository.DetalleNotaCreditoRepository;
import com.ec.g2g.repository.FacturaRepository;
import com.ec.g2g.repository.NotaCreditoRepository;
import com.ec.g2g.repository.ProductoRepository;
import com.ec.g2g.repository.ReporteFacturasRepository;
import com.ec.g2g.repository.TipoAmbienteRepository;
import com.ec.g2g.utilitario.ArchivoUtils;
import com.google.gson.Gson;
import com.intuit.ipp.data.CreditMemo;
import com.intuit.ipp.data.CustomField;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.LinkedTxn;
import com.intuit.ipp.data.MemoRef;
import com.intuit.ipp.data.Payment;
import com.intuit.ipp.data.PaymentMethod;
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
public class ReporteFacturas {

	@Autowired
	public QBOServiceHelper helper;

	@Autowired
	private TipoAmbienteRepository tipoAmbienteRepository;
	@Autowired
	private ValoresGlobales valoresGlobales;

	@Autowired
	ManejarToken manejarToken;
	@Autowired
	ReporteFacturasRepository reporteFacturasRepository;

	@Value("${posibilitum.nombre.empresa}")
	String NOMBREEMPRESA;

	@Value("${posibilitum.ruc.empresa}")
	String RUCEMPRESA;

	@PersistenceContext
	private EntityManager entityManager;

	/* PARA OBTENER LOS IMPUESTOS */

	@Transactional
	public void obtenerReporte(Date inicio, Date fin) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss-'07-00'");

		Optional<Tipoambiente> tipoAmbiente = tipoAmbienteRepository.findByAmEstadoAndAmRuc(Boolean.TRUE, RUCEMPRESA);
		if (tipoAmbiente.isPresent()) {
			valoresGlobales.TIPOAMBIENTE = tipoAmbiente.get();
			System.out.println("TIPO AMBIENTE CARGADO");
		} else {
			System.out.println("TIPO AMBIENTE NULL");
			return;

		}

		String realmId = valoresGlobales.REALMID;
		// String accessToken = valoresGlobales.TOKEN;
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
		try {

			/* ELIMINA LOS DATOS GUARDADOS */
			int deletedCount = entityManager
					.createQuery("DELETE FROM FacturaReporte a where a.codTipoambiente=:codTipoambiente")
					.setParameter("codTipoambiente", valoresGlobales.getTIPOAMBIENTE()).executeUpdate();

			if (valoresGlobales.REALMID != null && valoresGlobales.REFRESHTOKEN != null) {
				// get DataService
				DataService service = helper.getDataService(realmId, accessToken);

//				String sqlInicial = "select * from Invoice where MetaData.CreateTime >= '" + format.format(inicio)
//				+ "T00:00:00-07:00' order by id asc MAXRESULTS  1";
				String sqlInicial = "select * from Invoice where TxnDate >= '" + format.format(inicio)
						+ "' order by id asc MAXRESULTS  1";

				QueryResult queryResult = service.executeQuery(sqlInicial);

				List<Invoice> facturaInicio = (List<Invoice>) queryResult.getEntities();
				Invoice invoiceInicia = facturaInicio.get(0);
				Integer idInicial = Integer.valueOf(invoiceInicia.getId());

//				String sqlFinal = "select * from Invoice where MetaData.CreateTime >= '" + format.format(inicio)
//				+ "T00:00:00-07:00" + "' and  MetaData.CreateTime <='" + format.format(fin)
//				+ "T23:59:00-07:00'";

				String sqlFinal = "select * from Invoice where TxnDate <='" + format.format(fin)
						+ "' order by id desc MAXRESULTS  1";

				QueryResult queryResultFinal = service.executeQuery(sqlFinal);
				List<Invoice> facturaFinal = (List<Invoice>) queryResultFinal.getEntities();
				Invoice invoiceFinal = facturaFinal.get(0);
				Integer idFinal = Integer.valueOf(invoiceFinal.getId());

//				String sql = "select * from Invoice where MetaData.CreateTime >= '" + format.format(inicio)
//				+ "T00:00:00-07:00" + "' and  MetaData.CreateTime <='" + format.format(fin)
//				+ "T23:59:00-07:00'";

				int j = idInicial + 100;
				for (int i = idInicial; i < idFinal; j = j + 100) {
					/* Colocal el valor del ultimo query */
					String QUERYFINAL ="";
					if (j > idFinal) {
						j = idFinal;
						QUERYFINAL = "select * from Invoice where Id >='" + i + "' and Id <='" + j
								+ "' order by id asc";
					}else{
						
						QUERYFINAL = "select * from Invoice where Id >='" + i + "' and Id <'" + j
								+ "' order by id asc";
					}

//					/* QUERY PARA TRAER DE 100 en 100 */
//					QUERYFINAL = "select * from Invoice where Id >='" + i + "' and Id <'" + j
//							+ "' order by id asc";
					/* valor inicial para el siguiente query */
					i = j;

					// guayaquil 200

					// 10 por minuto

					System.out.println("QUERY REPORTE VENTAS " + QUERYFINAL);
					QueryResult queryResultRecorre = service.executeQuery(QUERYFINAL);
					List<Invoice> facturas = (List<Invoice>) queryResultRecorre.getEntities();
					System.out.println("TOTAL DE FACTURAS OBTENIDAS " + facturas.size());
					FacturaReporte reporte = new FacturaReporte();
					for (Invoice invoice : facturas) {
						reporte = new FacturaReporte();
						reporte.setCodTipoambiente(tipoAmbiente.get());
						reporte.setFacFecha(invoice.getTxnDate());
						reporte.setFacFechaVencimiento(invoice.getTxnDate());
						reporte.setFacTipoTransaccion("VENTA");
						reporte.setFacNumeroText(invoice.getDocNumber());
						reporte.setFacCliente(invoice.getCustomerRef().getName());

						/* Obtener el primer producto */
						List<Line> itemsRef = invoice.getLine();
						// OBTENEMOS EL PRIMERO PRODUCTO
						for (Line item : itemsRef) {
							reporte.setFacProducto(item.getSalesItemLineDetail().getItemRef().getName());
							break;
						}

						/* CAMPOS ADICIONALES VENDEDOR */
						if (invoice.getCustomField().size() > 0) {
							for (CustomField etiquetas : invoice.getCustomField()) {
								if (etiquetas.getName().equals("VENDEDOR")) {
									reporte.setFacVendedor(etiquetas.getStringValue());
								}

							}
						}

						/* CALCULOS PARA IVA Y SIN IVA */
						BigDecimal baseGrabada = BigDecimal.ZERO;
						BigDecimal baseCero = BigDecimal.ZERO;
						BigDecimal VALORIVA = BigDecimal.ZERO;
						for (Line valores : invoice.getTxnTaxDetail().getTaxLine()) {
							if (valores.getTaxLineDetail().getTaxPercent().doubleValue() == 12) {
								baseGrabada = baseGrabada.add(valores.getTaxLineDetail().getNetAmountTaxable());
								VALORIVA = VALORIVA.add(valores.getAmount());
							} else {
								baseCero = baseCero.add(valores.getTaxLineDetail().getNetAmountTaxable());

							}

						}

						reporte.setFacImporteSujetoImpuesto(baseGrabada.add(baseCero));
						reporte.setFacImporteImpuesto(VALORIVA);
						reporte.setFacTotal(invoice.getTotalAmt());

//						BigDecimal saldo = invoice.getTotalAmt().subtract(acumuladoPago);
						reporte.setFacSaldoTotal(invoice.getBalance());

						// OBTENER LOS PAGOS REALIZADOS A LA FACTURA
						List<LinkedTxn> listapago = invoice.getLinkedTxn();
						Payment obtenerPago = null;
						int posicionPago = 1;
						BigDecimal acumuladoPago = BigDecimal.ZERO;
						for (LinkedTxn pago : listapago) {
							Gson gson = new Gson();
							String JSON = gson.toJson(pago);
							System.out.println("FACTURA " + invoice.getId() + "  PAGO A RECORRER  " + JSON);
							if (!pago.getTxnType().equals("Estimate")) {

								obtenerPago = getPayment(pago.getTxnId());

								BigDecimal valorPagoForInvoce = BigDecimal.ZERO;
							
								for (Line linePago : obtenerPago.getLine()) {
									valorPagoForInvoce = BigDecimal.ZERO;
									if (invoice.getId().equals(linePago.getLinkedTxn().get(0).getTxnId())) {

										valorPagoForInvoce = linePago.getAmount();

										System.out.println("NUMERO PAGO " + posicionPago + "    ID PAGO "
												+ pago.getTxnId() + "   PAGO " + valorPagoForInvoce);
										acumuladoPago = acumuladoPago.add(valorPagoForInvoce);
										if (posicionPago == 1) {
											reporte.setFacCobro1(valorPagoForInvoce);
											reporte.setFacFechaCobro1(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago1(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 2) {
											reporte.setFacCobro2(valorPagoForInvoce);
											reporte.setFacFechaCobro2(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago2(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 3) {
											reporte.setFacCobro3(valorPagoForInvoce);
											reporte.setFacFechaCobro3(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago3(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 4) {
											reporte.setFacCobro4(valorPagoForInvoce);
											reporte.setFacFechaCobro4(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago4(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 5) {
											reporte.setFacCobro5(valorPagoForInvoce);
											reporte.setFacFechaCobro5(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago5(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 6) {
											reporte.setFacCobro6(valorPagoForInvoce);
											reporte.setFacFechaCobro6(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago6(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 7) {
											reporte.setFacCobro7(valorPagoForInvoce);
											reporte.setFacFechaCobro7(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago7(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 8) {
											reporte.setFacCobro8(valorPagoForInvoce);
											reporte.setFacFechaCobro8(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago8(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 9) {
											reporte.setFacCobro9(valorPagoForInvoce);
											reporte.setFacFechaCobro9(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago9(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										if (posicionPago == 10) {
											reporte.setFacCobro10(valorPagoForInvoce);
											reporte.setFacFechaCobro10(obtenerPago.getMetaData().getCreateTime());
											reporte.setFacMetodoPago10(obtenerPago.getPaymentMethodRef() != null
													? getPaymentMethod(obtenerPago.getPaymentMethodRef().getValue())
															.getName()
													: "BANCOS");
										}
										posicionPago = posicionPago + 1;
										
									}
									
								}
							}

						}
						reporteFacturasRepository.save(reporte);
					}

				}

			} else {

				System.out.println("REALMID NULL NO PROCESA EL REPORTE");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	// consultar clientes
	private Payment getPayment(String value) {
		String realmId = valoresGlobales.REALMID;
		if (StringUtils.isEmpty(realmId)) {
			System.out.println("No realm ID.  QBO calls only work if the accounting scope was passed!");
			return null;
		}
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);

		try {

			// Dataservice
			DataService service = helper.getDataService(realmId, accessToken);

			// get all Facturas
			String sql = "select * from Payment Where Id='" + value + "'";
			QueryResult queryResult = service.executeQuery(sql);
			Payment payment = (Payment) queryResult.getEntities().get(0);
			return payment;
		}
		/*
		 * Manejo de excepcion error de token
		 */
		catch (InvalidTokenException e) {
			e.printStackTrace();
			return null;

		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			e.printStackTrace();
			return null;

		}

	}

//	

	// consultar clientes
	private PaymentMethod getPaymentMethod(String value) {
		String realmId = valoresGlobales.REALMID;
		if (StringUtils.isEmpty(realmId)) {
			System.out.println("No realm ID.  QBO calls only work if the accounting scope was passed!");
			return null;
		}
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);

		try {

			// Dataservice
			DataService service = helper.getDataService(realmId, accessToken);

			// get all Facturas
			String sql = "select * from PaymentMethod where Id='" + value + "'";
			QueryResult queryResult = service.executeQuery(sql);
			PaymentMethod payment = (PaymentMethod) queryResult.getEntities().get(0);
			return payment;
		}
		/*
		 * Manejo de excepcion error de token
		 */
		catch (InvalidTokenException e) {
			e.printStackTrace();
			return null;

		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			e.printStackTrace();
			return null;

		}

	}

}
