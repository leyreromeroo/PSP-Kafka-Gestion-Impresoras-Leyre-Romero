package com.cuatrovientos.kafka.GestionColaImpresion;

public class Documento {
	private String titulo;
	private String contenido;
	private TipoImpresion tipo;
	private String sender;
	
	public Documento() {
	    // Necesario para la deserialización de JSON
	}
	
	public Documento(String titulo, String contenido, TipoImpresion tipo, String sender) {
		this.titulo = titulo;
		this.contenido = contenido;
		this.tipo = tipo;
		this.sender = sender;
	}
	
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getContenido() {
		return contenido;
	}
	public void setContenido(String contenido) {
		this.contenido = contenido;
	}
	public TipoImpresion getTipo() {
		return tipo;
	}
	public void setTipo(TipoImpresion tipo) {
		this.tipo = tipo;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}

	@Override
	public String toString() {
		return "Documento:" + titulo + ", contenido:" + contenido + ", tipo:" + tipo + ", enviado por:" + sender;
	}
	
	
	
}

