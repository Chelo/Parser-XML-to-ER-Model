package beans;

import java.util.HashMap;
import java.util.Iterator;
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
	
	public String nombre_entidad;																	//Nombre de la entidad.
	public Vector<Atributo> atributos=  new Vector<Atributo>(); 									//Atributos de la entidad.
	public HashMap<String,Vector<Atributo>> referencias = new HashMap<String,Vector<Atributo>>();	//Atributos que representan interrelación con otras Entidades.
																									//Es un hashMap cuya clave es el tipo del atributo y el valor es un vector 
																									// con atributos de ese tipo.
	public HashMap<String,Atributo> clave = new HashMap<String,Atributo>();							//Clave de la Entidad.

	public Vector<Atributo> foraneo = new Vector<Atributo>();									    //Valores foráneos, ArrayList de tipo <NombreEntidad,clave>.
	

	public String tipo;																				//Tipo de la Entidad según el XMLSchema.
	
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
	 * @return HashMap cuya clave es el tipo de los atributos y el valor es un vector
	 * 		   que contiene a los atributos de ese tipo.
	 */
	public HashMap<String,Vector<Atributo>> getReferencias() {
		return referencias;
	}
	
	/**
	 * Permite colocar los atributos que representan las relaciones con 
	 * las cuales se interrelaciona la Entidad.
	 * 
	 * @param newreferencias HashMap cuyas claves son los tipos de los Atributos
	 * 		  y los valores son vectores que poseen los Atributos de ese tipo.
	 */
	public void setReferencias(HashMap<String,Vector<Atributo>> newreferencias) {
		this.referencias = newreferencias;
	}
	

	/**
	 * Devuelve los datos de las tablas foráneas.
	 * 
	 * @return Vector de ArrayList de tamano dos, donde cada ArrayList contiene
	 * la Entidad a la cual se referencia, y su clave.
	 */
	public Vector<Atributo> getForaneo() {
		return foraneo;
	}

	/**
	 * Coloca los valores de los datos de las tablas que son foráneos
	 * a la Entidad.
	 * 
	 * @param foraneo Vector de ArrayList de tamano dos, donde cada ArrayList contiene
	 * la Entidad a la cual se referencia y su clave.
	 */
	public void setForaneo(Vector<Atributo> foraneo) {
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


	


	public HashMap<String, Atributo> getClave() {
		return clave;
	}

	public void setClave(HashMap<String, Atributo> clave) {
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
	public void AgregarForaneo(Atributo foraneo){
		this.foraneo.add(foraneo);

	}

	//Nuevos métodos agregados por KARINA
	/**
	 * Permite colocar un atributo básico más dentro del vector de atributos.
	 * @param atributo Atributo a agregar.
	 */
	public void setAtributo(Atributo atributo) {
		this.atributos.add(atributo);
	}
	
	/**
	 * Permite colocar una referencia mas en el hash de referencias.
	 * @param newreferencia Atributo que representa la referencia.
	 */
	public void setReferencia(Atributo newreferencia) {
		String tipo =newreferencia.getTipo();
		if (referencias.containsKey(tipo)) {
			/*
			 * El tipo de este vector ya esta en el hash, entonces agrego
			 * el atributo a su vector de atributos.
			 */
			referencias.get(tipo).add(newreferencia);
			
		} 
		else{
			/*
			 * El tipo no esta en el vector, por ende debo agregar una nueva clave 
			 * con ese valor.
			 */
			Vector<Atributo> nuevoVector= new Vector<Atributo>();
			nuevoVector.add(newreferencia);
			referencias.put(tipo, nuevoVector);
			
		}
	}
	
	/**
	 * Clona el vector de referencias del tipo ingresado.
	 * 
	 * @param tipo String que indica el tipo del vector a clonar.
	 * @return Vector clonado.
	 */
	public Vector<Atributo> clona(String tipo){
		Vector<Atributo> newRef = new Vector<Atributo>();
		Vector<Atributo> refer = referencias.get(tipo);
		
		if (refer == null) {
			return null;
		}
		else {
			Iterator<Atributo>  itr=refer.iterator();
		
			while(itr.hasNext()){
				Atributo clon = (Atributo)itr.next().clone();//Se clona el atributo.
				newRef.add(clon); // Se mete en el nuevo vector.
			}
			return newRef;
		}
	}
}	