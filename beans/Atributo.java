package beans;
//se lo voli a colocar
import java.util.Vector;

public class Atributo {
	public String nombre = null;
	public String tipo = null;
	public String valor = null;
	public int longitud = 0;
	public boolean nulo =  true;
	public Vector<String> dominio;
	
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
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public int getLongitud() {
		return longitud;
	}
	public void setLongitud(int longitud) {
		this.longitud = longitud;
	}
	public boolean isNulo() {
		return nulo;
	}
	public void setNulo(boolean nulo) {
		this.nulo = nulo;
	}
	public Vector<String> getDominio() {
		return dominio;
	}
	public void setDominio(Vector<String> dominio) {
		this.dominio = dominio;
	}
}

