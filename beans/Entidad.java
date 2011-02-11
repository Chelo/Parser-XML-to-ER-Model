package beans;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Clase Entidad
 * -------------
 * 
 * Clase que permite representar las Entidades del modelo de Bases de 
 * datos ER, junto con sus características principales, como atributos,
 * nombre, clave, etc.
 * 
 * @version 1.0 6/02/11
 * @author 
 * 		- Karina Aguiar.
 *  	- Liliana Barrios.
 *  	- Consuelo Gómez.
 *
 */

public class Entidad {
	
	public String nombre_entidad;									//Nombre de la entidad.
	public Vector<Atributo> atributos=  new Vector<Atributo>(); 	//Atributos de la entidad.
	public Vector<Atributo> referencias = new Vector<Atributo>();	//Atributos que representan interrelación con otras Entidades.
	public Vector<Atributo> clave = new Vector<Atributo>();								//Clave de la Entidad.
	public Vector<ArrayList<String>> foraneo; 						//Valores foráneos, ArrayList de tipo <NombreEntidad,clave>.
	public String tipo;												//Tipo de la Entidad según el XMLSchema.
	
	/**
	 * Retorna el nombre de la Entidad.
	 * 
	 * @return String con el nombre de la entidad.
	 */
	public String getNombre_entidad() {
		return nombre_entidad;
	}
	
	/**
	 * Coloca un nombre a la Entidad.
	 * 
	 * @param nombre_entidad nombre que se le colocará a la Entidad.
	 */
	public void setNombre_entidad(String nombre_entidad) {
		this.nombre_entidad = nombre_entidad;
	}
	
	/**
	 * Obtiene los aributos del XMLSchema que representan las interrelaciones
	 * entre las Entidades.
	 * 
	 * @return Vector con los atributos que hacen referecia a las interrelaciones.
	 */
	public Vector<Atributo> getReferencias() {
		return referencias;
	}
	
	/**
	 * Permite colocar los atributos que representan las relaciones con 
	 * las cuales se interrelaciona la Entidad.
	 * 
	 * @param newreferencias Vector con los atributos que referencian.
	 */
	public void setReferencias(Vector<Atributo> newreferencias) {
		this.referencias = newreferencias;
	}


	
	/**
	 * Devuelve los datos de las tablas foráneas.
	 * 
	 * @return Vector de ArrayList de tamano dos, donde cada ArrayList contiene
	 * la Entidad a la cual se referencia, y su clave.
	 */
	public Vector<ArrayList<String>> getForaneo() {
		return foraneo;
	}

	/**
	 * Coloca los valores de los datos de las tablas que son foráneos
	 * a la Entidad.
	 * 
	 * @param foraneo Vector de ArrayList de tamano dos, donde cada ArrayList contiene
	 * la Entidad a la cual se referencia y su clave.
	 */
	public void setForaneo(Vector<ArrayList<String>> foraneo) {
		this.foraneo = foraneo;
	}

	/**
	 * Devuelve el tipo de la Entidad.
	 * 
	 * @return String con el tipo de la Entidad.
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * Coloca el tipo a la Entidad.
	 * 
	 * @param tipo String con el nombre del tipo de la Entidad.
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}


	
	public Vector<Atributo> getClave() {
		return clave;
	}

	public void setClave(Vector<Atributo> clave) {
		this.clave = clave;
	}

	/**
	 * Devuelve los atributos de tipos básicos de la Entidad.
	 * 
	 * @return Vector que contiene los atributos que caracterizan 
	 * a la Entidad.
	 */
	public Vector<Atributo> getAtributos() {
		return atributos;
	}

	/**
	 * Coloca los atributos de tipos básicos a la entidad.
	 * 
	 * @param atributos Vector que contiene los atributos de la Entidad.
	 */
	public void setAtributos(Vector<Atributo> atributos) {
		this.atributos = atributos;
	}
	
	/**
	 * Agrega los datos (NombreEntidad, ClaveEntidad) de una Entidad foránea
	 * a la Entidad actual.
	 * 
	 * @param foraneo ArrayList que funciona como tupla de la forma <NombreEntidad,ClaveEntidad>
	 */
	public void AgregarForaneo(ArrayList<String> foraneo){
		this.foraneo.add(foraneo);

	}

	//Nuevos métodos agregados por KARINA
	public void setAtributo(Atributo atributo) {
		this.atributos.add(atributo);
	}
	
	public void setReferencia(Atributo newreferencia) {
		this.referencias.add(newreferencia);
	}
}	