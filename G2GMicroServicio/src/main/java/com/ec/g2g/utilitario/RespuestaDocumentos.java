package com.ec.g2g.utilitario;

public class RespuestaDocumentos {
	private String descripcion;
	private String estado;

	public RespuestaDocumentos(String descripcion, String estado) {
		this.descripcion = descripcion;
		this.estado = estado;
	}

	public RespuestaDocumentos() {
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

}
