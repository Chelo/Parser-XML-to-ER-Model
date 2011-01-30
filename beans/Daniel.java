package beans;

import java.util.Vector;

public class Daniel {
	
	public String nombre_entidad;
	public Vector<Atributo> atributos;
	
	public String getNombre_entidad() {
		return nombre_entidad;
	}
	
	public void setNombre_entidad(String nombre_entidad) {
		this.nombre_entidad = nombre_entidad;
	}

	public Vector<Atributo> getAtributos() {
		return atributos;
	}

	public void setAtributos(Vector<Atributo> atributos) {
		this.atributos = atributos;
	}
	

}
