package accions;
import java.util.Vector;

import beans.Atributo;
import beans.Entidad;

public class MostrarEntidades {
	public void imprimirEntidades(Vector<Entidad> entidades){
		int i =entidades.size()-1;
		int j = 0;
		while(i>=0){
			
			System.out.println(i);
			System.out.println("-- "+entidades.get(i).getNombre_entidad()+" --");
			j = entidades.get(i).getAtributos().size()-1;
			Vector<Atributo> atributos = entidades.get(i).getAtributos();
			while(j>=0){
			System.out.println("	Nombre : "+ atributos.get(j).nombre +"	Clave : "+entidades.get(i).getClave() );
			System.out.println("		Tipo : "+ atributos.get(j).tipo);
			System.out.println("		Nulo : "+ atributos.get(j).nulo);
			
			j--;
			}i--;
			
		}
		
	}

}
