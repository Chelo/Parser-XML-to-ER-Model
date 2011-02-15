package accions;

import java.io.FileReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/** Esta clase permite obtener y manipular la informacion contenida en los tags correspondientes 
 * a los archivos XML(puro) para generar comandos de insercion de datos SQL("INSERT INTO") para 
 * aquellas tablas que se crearon anteriormente.<br><br>
 * Lee los nodos XML y sus valores.<br><br>
 * Retorna un archivo "insert.sql"
 * @author Daniel Pedroza
 */
public class ParserXML extends DefaultHandler {

	private final static String xmlFileName    = "libro.xml";
	private final static String targetFileName = "insert.sql";
    private final static int identificador = 0;
	
    
    
    /** Crear una instancia de ParserXML 
     *
    */
    public ParserXML() {
    	
    	
    	
    	
    	
        //Parsear archivo xml
    	//devuelve un tipo Document (usa libreria Dom)
        //Document doc = parseFile(xmlFileName);
        
        //Obtener la raiz del arbol xml
        //Node raiz = doc.getDocumentElement();
        
        //Imprimir contenido XML
        System.out.println("______________________________\n");
        //writeDocumentToOutput(raiz,0,identificador);
        //insertScript();
        System.out.println("______________________________\n");

        //Crear el archivo sql insert
        //saveXMLDocument(targetFileName, doc);
       
    }
	
	
	
}//fin clase ParserXML
