package beans;
import java.util.ArrayList;
import java.util.Vector;


/**
 * Clase Atributo
 * --------------
 * 
 * Clase que contiene las características básicas de los atributos de una entidad en 
 * el modelo ER.
 *
 * 
 * @version 1.0  30/01/11
 * @author:
 *  - Karina Aguiar
 *  - Liliana Barrios
 *  - Consuelo Gómez
 *  - Daniel Pedroza
 * 
 */

public class Atributo {
	public String nombre = null; 
	public String tipo = null;
	public String valorPorDefecto = "";
	public String longitud = null;
	public boolean nulo =  true;
	public ArrayList<String> rango= new ArrayList<String>(); 
	public Vector<String> dominio= new Vector<String>();
	public int minOccurs = 0;
	public int maxOccurs = 0;
	
	
	public int getMinOccurs() {
		return minOccurs;
	}
	public void setMinOccurs(int minOccurs) {
		this.minOccurs = minOccurs;
	}
	public int getMaxOccurs() {
		return maxOccurs;
	}
	public void setMaxOccurs(int maxOccurs) {
		this.maxOccurs = maxOccurs;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getValor() {
		return valorPorDefecto;
	}
	public void setValor(String valor) {
		this.valorPorDefecto = valor;
	}
	public String getLongitud() {
		return longitud;
	}
	public void setLongitud(String longitud) {
		this.longitud = longitud;
	}
	public boolean isNulo() {
		return nulo;
	}
	public void setNulo(boolean nulo) {
		this.nulo = nulo;
	}
	public void setRango(ArrayList<String> rango) {
		this.rango = rango;
	}
	public ArrayList<String> getRango() {
		return rango;
	}
	public Vector<String> getDominio() {
		return dominio;
	}
	public void setDominio(Vector<String> dominio) {
		this.dominio = dominio;
	}
	
}
