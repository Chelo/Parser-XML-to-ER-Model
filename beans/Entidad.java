package beans;

import java.util.Collection;
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
	public HashMap<String,Vector<Atributo>> referencias = new HashMap<String,Vector<Atributo>>();	//Hash cuya clave es el tipo de la entidad y el valor es el vector de atributos del tipo de la entidad
	public HashMap<String,Atributo> clave = new HashMap<String,Atributo>();							//Hash cuya clave es el nombre del Atributo que se encuentra como valor.
	public HashMap<String,Atributo> unico = new HashMap<String,Atributo>();							//Hash cuya clave es el nombre del Atributo que se encuentra como valor.
	public HashMap<String, Vector<Vector<Atributo>>> foraneo = new HashMap<String,Vector<Vector<Atributo>>>();//Hash, con clave el nombre de la entidad foránea, valor, el vector de Vectores de Atributos
	public String tipo;																				//Tipo de la Entidad según el XMLSchema.
	
	/**
	 * Retorna los atributos unicos de la entidad
	 * @return Hashmap con los atributos unicos de la entidad
	 */
	public HashMap<String, Atributo> getUnico() {
		return unico;
	}

	/**
	 * Coloca los atributos unicos pertenecientes a la entidad
	 * @param unico
	 */
	public void setUnico(HashMap<String, Atributo> unico) {
		this.unico = unico;
	}
	/**
	 *  Retorna todos los atributos que hacen referencias a otras entidades.
	 * @return HashMap con los atributos que hacen referencias a otras entidades
	 */
	public HashMap<String, Vector<Vector<Atributo>>> getForaneo() {
		return foraneo;
	}
	/**
	 * Permite almacenar los atributos que hacen referencia hacia otras entidades
	 * @param foraneo
	 */
	public void setForaneo(HashMap<String, Vector<Vector<Atributo>>> foraneo) {
		this.foraneo = foraneo;
	}


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

	/**
	 * Permite obtener los atributos que son forman la clave entidad
	 * @return HashMa con los atributos que forman las clave primaria de la entidad
	 */
	public HashMap<String, Atributo> getClave() {
		return clave;
	}

	/**
	 * Permite almacenar los atributos que forman la clave de la entidad
	 * @param clave
	 */
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
	
	
	/**
	 * Se encarga de colocar nuevos valores foraneos a la entidad actual.
	 * Los datos entrantes son de la entidad que absorbera la entidad actual.
	 * 
	 * @param nombre String que indica el tipo de la entidad.
	 * @param clave Collection con los atributos que forman la clave de la Entidad a
	 * 			absorber.
	 */
	public void AgregarForaneo(String nombre, Collection<Atributo> clave){
		/*
		 * Recordemos que lo único que puede ser foráneo son las claves.
		 */
		
		//Recorro la colletion de claves.
		Iterator<Atributo> itr = clave.iterator();
		
		Vector<Atributo> vector= new Vector<Atributo>();// Vector donde se meteran los clones.
		
		Vector<Vector<Atributo>> vectores=new Vector<Vector<Atributo>>(); //vector donde se meteran los vectores creados.
		
		if (!foraneo.containsKey(nombre)) {
			//Si la entidad foranea no esta creo todo nuevo y la inserto en el hash.
		
			while(itr.hasNext()){
				//Clono a cada atributo de la clave y lo paso al vector
				Atributo atri=(Atributo)itr.next().clone();
				if (nombre.equals(tipo)) {
					//Soy yo mismo, debo cambiar el nombre del atributo.
					atri.nombre=atri.nombre+"_F";
				}
				
				vector.add(atri);
			}
			vectores.add(vector);
			foraneo.put(nombre,vectores);
		}
		else 
		{
			//Si la entidad ya esta, agrego un nuevo vector con la clave.
			vectores= foraneo.get(nombre);
			String concatena=Integer.toString(vectores.size());
			
			while(itr.hasNext()){
				Atributo atributo=(Atributo)itr.next().clone();
				//Cambio el nombre de cada atributo para que se coloque con calma en el SQL.
				atributo.nombre=atributo.nombre+concatena;
				vector.add(atributo);
			}
			vectores.add(vector);
		}
	}
}	