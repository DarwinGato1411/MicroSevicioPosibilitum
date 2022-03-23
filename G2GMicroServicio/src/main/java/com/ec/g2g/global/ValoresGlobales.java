package com.ec.g2g.global;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ec.g2g.entidad.Tipoambiente;

@Component
public class ValoresGlobales {

	public String CSRF;
	public String REALMID;
	public String TOKEN;
	public String REFRESHTOKEN;
	public Tipoambiente TIPOAMBIENTE;
	public BigDecimal IVA=BigDecimal.valueOf(12);
	public BigDecimal SACARIVA=BigDecimal.valueOf(0.12);
	public BigDecimal SUMARIVA=BigDecimal.valueOf(1.12);
	

	public String getCSRF() {
		return CSRF;
	}

	public void setCSRF(String cSRF) {
		CSRF = cSRF;
	}

	public String getREALMID() {
		return REALMID;
	}

	public void setREALMID(String rEALMID) {
		REALMID = rEALMID;
	}

	public String getTOKEN() {
		return TOKEN;
	}

	public void setTOKEN(String tOKEN) {
		TOKEN = tOKEN;
	}

	public String getREFRESHTOKEN() {
		return REFRESHTOKEN;
	}

	public void setREFRESHTOKEN(String rEFRESHTOKEN) {
		REFRESHTOKEN = rEFRESHTOKEN;
	}

	public Tipoambiente getTIPOAMBIENTE() {
		return TIPOAMBIENTE;
	}

	public void setTIPOAMBIENTE(Tipoambiente tIPOAMBIENTE) {
		TIPOAMBIENTE = tIPOAMBIENTE;
	}

	public BigDecimal getIVA() {
		return IVA;
	}

	public void setIVA(BigDecimal iVA) {
		IVA = iVA;
	}

	public BigDecimal getSACARIVA() {
		return SACARIVA;
	}

	public void setSACARIVA(BigDecimal sACARIVA) {
		SACARIVA = sACARIVA;
	}

	public BigDecimal getSUMARIVA() {
		return SUMARIVA;
	}

	public void setSUMARIVA(BigDecimal sUMARIVA) {
		SUMARIVA = sUMARIVA;
	}
	
	
	
}
