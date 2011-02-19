package accions;

import java.io.IOException;
import java.util.Iterator;

import org.xml.sax.SAXException;

public class Principal {
	
	public static void main(String[] args) throws SAXException, IOException {
				
		Parser.ParsearXMLSchema("ejemplo.xml");
		//Estas líneas son solo para probar el nuevo hash que necesita Daniel,(nombreEntidad, tipoEntidad) 
		//(Luego se pueden borrar estas líneas)
		//Para hacer uso del hash simplemente hacen Parser.nombreEntidades
		Iterator<String> nombres = Parser.nombreEntidades.keySet().iterator();
		Iterator<String> tipos = Parser.nombreEntidades.values().iterator();
		
		while(nombres.hasNext() & tipos.hasNext())
		{	
			System.out.print("Nombre "+nombres.next()+"\n");
			System.out.print("Tipo "+tipos.next() +"\n");
		}
		//ParserXML.ParsearXML("libro.xml");
	}
}
