package accions;
	
import java.io.IOException;
import java.util.Iterator;

import org.xml.sax.SAXException;

public class Principal {
	
	public static void main(String[] args) throws SAXException, IOException {

		Parser.CreaReporte();


		Parser.ParsearXMLSchema("EjemploPruebas.xml",2);
		

		//Estas tres líneas son solo para probar que dentro del main se puede 
		//utilizar el hash de entidades (las pueden borrar)
		//Para hacer uso del hash simplemente hacen Parser.entidades
		//System.out.print("Tamano del Hash de entidades/ Numero de entidades "+Parser.entidades.size()+ "\n");
		//System.out.print("Nombre de la primera entidad "+Parser.entidades.get("User").getNombre_entidad()+"\n");
		//System.out.print("Nombre de su primer atributo "+Parser.entidades.get("User").getAtributos().get(0).getNombre()+"\n");

		//Estas líneas son solo para probar el nuevo hash que necesita Daniel,(nombreEntidad, tipoEntidad) 
		//(Luego se pueden borrar estas líneas)
		//Para hacer uso del hash simplemente hacen Parser.nombreEntidades
		//Iterator<String> nombres = Parser.nombreEntidades.keySet().iterator();
		//Iterator<String> tipos = Parser.nombreEntidades.values().iterator();
		
		//while(nombres.hasNext() & tipos.hasNext())
		//{	
			//System.out.print("Nombre "+nombres.next()+"\n");
			//System.out.print("Tipo "+tipos.next() +"\n");
		//}

		/**Iterator<String> nombres = Parser.nombreEntidades.keySet().iterator();
		Iterator<String> tipos   = Parser.nombreEntidades.values().iterator();
		while(nombres.hasNext() & tipos.hasNext()){	
			System.out.print("Nombre "+nombres.next()+"\n");
			System.out.print("Tipo "+tipos.next() +"\n");
		}*/


	//	ParserXML.ParsearXML("libro1.xml");
	}
}
