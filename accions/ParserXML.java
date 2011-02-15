package accions;

//import beans.Atributo;
//import beans.OrigenXML;

import java.io.FileReader;
import java.io.IOException;

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
	//private final static String targetFileName = "insert.sql";
	private static String contenido      = "";
	private static int    indent         = 0;
    
	private final XMLReader xr;
    
    
    //En el main la llamada sera algo del tipo: 
    //new ParserXML();
    //ParserXML Pxml = new ParserXML(); 

    /** Crear una instancia de ParserXML */
    public ParserXML() throws SAXException {
    		xr = XMLReaderFactory.createXMLReader();
	        xr.setContentHandler(this);
	        xr.setErrorHandler(this);
	        System.out.println("ParserXML--");
    }
    public void leer(final String archivoXML) throws IOException, SAXException {
        FileReader fr = new FileReader(archivoXML);
        System.out.println("Leer--");
        xr.parse(new InputSource(fr));
    }
    
    private String getIndentSpaces(int indent) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            buffer.append(" ");
        }
        return buffer.toString();
    }
    @Override
    public void startDocument() {System.out.println("<XML>\n");}
    @Override
    public void endDocument() {System.out.println("</XML>");}
    
    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
    	//hallar indentacion ,calcular e imprimir 
    	indent++;
    	//imprimir indentacion
    	System.out.print(getIndentSpaces(indent) +"<" + name + ">");
        //crear new
        //OrigenXML temp = new OrigenXML();
    }
    @Override
    public void endElement(String uri, String name, String qName) {
    	System.out.print(getIndentSpaces(indent) + "</" + name + ">");
    	indent--;
    }
    public void characters(char buf[], int offset, int len) throws SAXException {
    	indent=indent+2;
    	//ojo revisar cuando es fecha 
    	//guardar en un String y luego almacenar en OrigenXML
    	contenido = new String(buf, offset, len);
    	System.out.print(getIndentSpaces(indent) + contenido);
    	indent=indent-2;
    }
    
    /** Starts XML parsing example
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SAXException, IOException {
    	ParserXML pxml = new ParserXML();
    	pxml.leer(xmlFileName);
    }
    
}//fin clase ParserXML
