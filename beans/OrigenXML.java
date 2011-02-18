package beans;

import java.util.Vector;
import beans.Entidad;

public class OrigenXML {

	private String nombreTag;
	public Vector<Atributo> atributos =  new Vector<Atributo>(); 	//Atributos de la entidad.
	public int indent;
	
	/** constructor de la clase OrigenXML
	 */
	public OrigenXML(Entidad ent){
		this.nombreTag =ent.getNombre_entidad();
		this.atributos =ent.getAtributos();
	}
	
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
	
	
	/**
	 * Almacena el valor de indentacion del tag encontrado en el archivo XML.
	 * @param int inden
	 */
	public void setIndent(int inden) { this.indent = inden; }
	/**
	 * Devuelve el valor de indentacion del tag encontrado en el archivo XML.
	 * @return int indent
	 */
	public int getIndent() { return indent;}

}//fin clase OrigenXML