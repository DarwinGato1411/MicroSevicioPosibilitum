package com.ec.g2g.utilitario;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArchivoUtils {
	public static String generaClave(Date fechaEmision, String tipoComprobante, String ruc, String ambiente, String serie,
			String numeroComprobante, String codigoNumerico, String tipoEmision) /*     */ {
		String claveGenerada = "";
		/* 37 */ int verificador = 0;
		/*     */
// if ((ruc != null) && (ruc.length() < 13)) {
//    ruc = String.format("%013d", new Object[]{ruc});
//       }

		String numeroCedulaText = "";
		for (int i = ruc.length(); i < 13; i++) {
			numeroCedulaText = numeroCedulaText + "0";
		}

		ruc = numeroCedulaText + ruc;
		System.out.println("RUC CON CEROS AUTO DOC" + ruc);

		/* 44 */ SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		/* 45 */ String fecha = dateFormat.format(fechaEmision);
		/*     */
		/* 47 */ StringBuilder clave = new StringBuilder(fecha);
		/* 48 */ clave.append(tipoComprobante);
		/* 49 */ clave.append(ruc);
		/* 50 */ clave.append(ambiente);
		/* 51 */ clave.append(serie);
		/* 52 */ clave.append(numeroComprobante);
		/* 53 */ clave.append(codigoNumerico);
		/* 54 */ clave.append(tipoEmision);
		/*     */
		/* 57 */ verificador = generaDigitoModulo11(clave.toString());
		/*     */
		/* 59 */ clave.append(Integer.valueOf(verificador));
		/* 60 */ claveGenerada = clave.toString();
		/*     */
		/* 62 */ if (clave.toString().length() != 49) {
			/* 63 */ claveGenerada = null;
			/*     */ }
		/* 65 */ return claveGenerada;
		/*     */ }
	
	
	public static int generaDigitoModulo11(String cadena) {
        int baseMultiplicador = 7;
        System.out.println("CADENA-->" + cadena);
        int[] aux = new int[cadena.length()];
        int multiplicador = 2;
        int total = 0;
        int verificador = 0;
        for (int i = aux.length - 1; i >= 0; i--) {
            aux[i] = Integer.parseInt("" + cadena.charAt(i));
            aux[i] *= multiplicador;
            multiplicador++;
            if (multiplicador > baseMultiplicador) {
                multiplicador = 2;
            }
            total += aux[i];
        }
        if ((total == 0) || (total == 1)) {
            verificador = 0;
        } else {
            verificador = 11 - total % 11 == 11 ? 0 : 11 - total % 11;
        }

        if (verificador == 10) {
            verificador = 1;
        }

        return verificador;
    }
	
	 public static BigDecimal redondearDecimales(BigDecimal valorInicial, int numeroDecimales) {
	        double parteEntera, resultado;
	        resultado = valorInicial.doubleValue();
	        parteEntera = Math.floor(resultado);
	        Double resutl = BigDecimal.valueOf(resultado).subtract(BigDecimal.valueOf(parteEntera)).doubleValue();
	     
	        resultado = resutl * Math.pow(10, numeroDecimales);
	        resultado = Math.round(resultado);
	        BigDecimal resulDes = BigDecimal.valueOf(resultado);	      
	        
	        BigDecimal divide = BigDecimal.valueOf(resulDes.doubleValue()).divide(BigDecimal.valueOf(Math.pow(10, numeroDecimales)));
	        resultado = (divide.add(BigDecimal.valueOf(parteEntera))).doubleValue();
	        return BigDecimal.valueOf(resultado);
	    }

}
