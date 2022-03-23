package com.ec.g2g;

public class ModelIdentificacion {
	private String cedula;
	private Integer codigo;

	public ModelIdentificacion() {
		super();
	}

	public ModelIdentificacion(String cedula, Integer codigo) {
		super();
		this.cedula = cedula;
		this.codigo = codigo;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

}
