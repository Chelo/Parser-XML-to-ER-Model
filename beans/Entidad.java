package beans;

import java.util.ArrayList;
import java.util.Vector;

public class Entidad {
	
	public String nombre_entidad;
	public Vector<Atributo> atributos=  new Vector<Atributo>();
	public Vector<Atributo> referencias = new Vector<Atributo>();
	public String clave= "NULL";
	public Vector<ArrayList<String>> foraneo; 
	public String tipo;
	
	public String getNombre_entidad() {
		return nombre_entidad;
	}
	
	public void setNombre_entidad(String nombre_entidad) {
		this.nombre_entidad = nombre_entidad;
	}
	
	public Vector<Atributo> getReferencias() {
		return referencias;
	}
	
	public void setReferencias(Vector<Atributo> newreferencias) {
		this.referencias = newreferencias;
	}

	public String getClave() {
		return clave;
	}
	
	public Vector<ArrayList<String>> getForaneo() {
		return foraneo;
	}

	public void setForaneo(Vector<ArrayList<String>> foraneo) {
		this.foraneo = foraneo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}
	
	public Vector<Atributo> getAtributos() {
		return atributos;
	}

	public void setAtributos(Vector<Atributo> atributos) {
		this.atributos = atributos;
	}
	

}