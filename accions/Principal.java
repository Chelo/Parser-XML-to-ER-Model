package accions;

import java.io.IOException;

import org.xml.sax.SAXException;

public class Principal {
	
	public static void main(String[] args) throws SAXException, IOException {
				
		Parser.ParsearXMLSchema("ejemplo2.xml");
		//Estas tres líneas son solo para probar que dentro del main se puede 
		//utilizar el hash de entidades (las pueden borrar)
		//Para hacer uso del hash simplemente hacen Parser.entidades
		//System.out.print("Tamano del Hash de entidades/ Numero de entidades "+Parser.entidades.size()+ "\n");
		//System.out.print("Nombre de la primera entidad "+Parser.entidades.get("User").getNombre_entidad()+"\n");
		//System.out.print("Nombre de su primer atributo "+Parser.entidades.get("User").getAtributos().get(0).getNombre()+"\n");

		ParserXML.ParsearXML("libro.xml");
	}
}
