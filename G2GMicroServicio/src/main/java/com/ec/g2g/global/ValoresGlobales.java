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
	
	public BigDecimal IVA5=BigDecimal.valueOf(5);
	public BigDecimal SACARIVA5=BigDecimal.valueOf(0.05);
	public BigDecimal SUMARIVA5=BigDecimal.valueOf(1.05);
	
	public BigDecimal IVA15=BigDecimal.valueOf(15);
	public BigDecimal SACARIVA15=BigDecimal.valueOf(0.15);
	public BigDecimal SUMARIVA15=BigDecimal.valueOf(1.15);
	

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

	public BigDecimal getIVA5() {
		return IVA5;
	}

	public void setIVA5(BigDecimal iVA5) {
		IVA5 = iVA5;
	}

	public BigDecimal getSACARIVA5() {
		return SACARIVA5;
	}

	public void setSACARIVA5(BigDecimal sACARIVA5) {
		SACARIVA5 = sACARIVA5;
	}

	public BigDecimal getSUMARIVA5() {
		return SUMARIVA5;
	}

	public void setSUMARIVA5(BigDecimal sUMARIVA5) {
		SUMARIVA5 = sUMARIVA5;
	}

	public BigDecimal getIVA15() {
		return IVA15;
	}

	public void setIVA15(BigDecimal iVA15) {
		IVA15 = iVA15;
	}

	public BigDecimal getSACARIVA15() {
		return SACARIVA15;
	}

	public void setSACARIVA15(BigDecimal sACARIVA15) {
		SACARIVA15 = sACARIVA15;
	}

	public BigDecimal getSUMARIVA15() {
		return SUMARIVA15;
	}

	public void setSUMARIVA15(BigDecimal sUMARIVA15) {
		SUMARIVA15 = sUMARIVA15;
	}
	
	
	
}
