package beans;

import java.util.Vector;

public class OrigenXML {

	private String nombreTag;
	public Vector<Atributo> atributos =  new Vector<Atributo>(); 	//Atributos de la entidad.

	
	/**
	 * Almacena el nombre del tag encontrado en el archivo XML.
	 * @param String nombre
	 */
	public void setNombreTag(String nombre) { this.nombreTag = nombre; }
	/**
	 * Devuelve el nombre del tag.
	 * @return nombreTag
	 */
	public String getnombreTag() { return nombreTag;}
		

	/**
	 * Almacena los atributos y valores encontrados en el tag.
	 * @param atributos Vector que contiene los valores del tag.
	 */
	public void setAtributos(Vector<Atributo> atributos) { this.atributos = atributos; }
	/**
	 * Devuelve los atributos y valores encontrados en el tag.
	 * @return Vector que contiene los valores del tag encontrado.
	 */
	public Vector<Atributo> getAtributos() { return atributos; }

}//fin clase OrigenXML