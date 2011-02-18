package accions;

//import beans.Atributo;
import beans.Entidad;
import beans.OrigenXML;

import java.io.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
//import java.util.Vector;

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

	//private final static String targetFileName = "insert.sql";
	private static String contenido      = "";
	private static int    indent         = 0;
	private static OrigenXML data;
	private static int att; 
	private final XMLReader xr;
    
    /** Crear una instancia de ParserXML */
    public ParserXML() throws SAXException {
    		xr = XMLReaderFactory.createXMLReader();
	        xr.setContentHandler(this);
	        xr.setErrorHandler(this);
	        System.out.println("\nParserXML--");
    }
    public void leer(final String archivoXML) throws IOException, SAXException {
        FileReader fr = new FileReader(archivoXML);
        System.out.println("Leer--");
        xr.parse(new InputSource(fr));
    }

    /** Aumenta el valor de la variable Indent
     * @param int a
     */
    private void sumarIndent(int a) { indent=indent+a; }
    /** Disminuye el valor de la variable Indent
     * @param int a
     */
    private void restarIndent(int a) { indent=indent-a; }  
    /** Permite la indetacion dentro del  
     * @param int indent
     * @return String buffer
	 */
    private String getIndentSpaces(int indent) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            buffer.append(" ");
        }
        return buffer.toString();
    }
    /** Realiza una busqueda del String name entre los nombres 
     * de las entidades arrojadas por el parser XMLSchema 
     * @param String name
     * @return boleean 
	 */
    private boolean buscarEntidadB(String name){
    	Set<String> tipos = Parser.entidades.keySet();
		Iterator<String> cadaTipo = tipos.iterator();
		while ( cadaTipo.hasNext() ) {
			if (Parser.entidades.get(cadaTipo.next()).getNombre_entidad().compareTo(name)==0) {
				return true;
			}
    	}
		return false;
    }
    private Entidad buscarEntidadE(String name){
    	Entidad ent;
    	
    	Set<String> tipos = Parser.entidades.keySet();
		Iterator<String> cadaTipo = tipos.iterator();
		while ( cadaTipo.hasNext() ) {
			String actual = cadaTipo.next();
			if (Parser.entidades.get(actual).getNombre_entidad().compareTo(name)==0) {
				ent = Parser.entidades.get(actual);
				return ent;
			}
    	}
		return null;
    }
//    private void listaEntidades(){
//       	Set<String> tipos = Parser.entidades.keySet();
//		Iterator<String> cadaTipo = tipos.iterator();
//    	while ( cadaTipo.hasNext() ) {
//    		System.out.println("nombre" + Parser.entidades.get(cadaTipo.next()).getNombre_entidad());
//    	}
//    }
    private static String listaCampos(){
    	String result = "";
    	int i=0;
    	while (i<data.getAtributos().size()) {
    		if (i==0){
    			result = result+data.getAtributos().get(0).nombre;
    		}else {
    			result = result + ", ";
        		result = result+data.getAtributos().get(i).nombre;
    		}
    		i++;
    	}
    	return result;
    }    
    private static String listaValores(){
    	String result = "";
    	int i=0;
    	while (i<data.getAtributos().size()) {
    		if (i==0){
    			result = result+data.getAtributos().get(0).getValor();
    		}else {
    			result = result + ", ";
        		result = result+data.getAtributos().get(i).getValor();
    		}
    		i++;
    		System.out.println(result + data.getAtributos().size() );
    	}
    	return result;
    }  
    private int buscarAtributo(String name){
    	int index = -1;
    	int i=0;
    	while (i<data.getAtributos().size()) {
    		//System.out.println(data.getAtributos().get(i).getNombre().compareTo(name));
    		if ( data.getAtributos().get(i).getNombre().compareTo(name)==0){
    			//System.out.println("entre____________");
    			return i;
    		}
    		i++;
    	}
    	return index;
    }  
    
    public static void InsertScript(){
		
    	//int k = 0;
		try{
		    //Se crea el archivo sql de salida.
		    FileWriter fstream = new FileWriter("./insert.sql");
		    BufferedWriter insert = new BufferedWriter(fstream);
		    
		    //obtener lista de campos
		    String lcampos = listaCampos();
		    String lvalores = listaValores();
		    
			// Se realiza un 'INTO' por cada entidad encontrada			    
		    insert.write("INSERT INTO "+ data.getnombreTag().toUpperCase()
		    		+ " ("+ lcampos + ") VALUES ("+ lvalores + ");");
	    
		    
			//Se iteran sobre las entidades que se van a crear.
		    //borrar luego este while
						
			//insert.write(");\n");
		    //Se cierra el output de escritura en el archivo sql
			insert.close();
		// Se toma la exception si existe
		}catch (Exception e){
		      System.err.println("Error: " + e.getMessage());
		    }
	}

    @Override
    public void startDocument() {System.out.println("<XML>\n");}
    @Override
    public void endDocument() {System.out.println("</XML>");}
    
    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
    	//listaEntidades();
   		if ( buscarEntidadB(name) ){
   			//obtener entidad
   			Entidad ent = buscarEntidadE(name);
		
   			//llenar vector atributos
   			//crear objeto con el nombre
   			data = new OrigenXML(ent); 
   			att  = -1;
   			//System.out.println("<" +data.getnombreTag() + data.getAtributos().elementAt(0).nombre);
   			
   			//crear pila
   			//llenar objeto con el nombre
   			//data.setNombreTag(name);
   			//copiar los atributos
   		}else {
   			//chequear si estan en mi vector atributos
   				//(deberia estar si el xml es valido)
   			att = buscarAtributo(name);
   			//System.out.print("att: " + att);

   			//escribir en el objeto recientemente creado
   			//System.out.println("encontrado: " + Parser.entidades.get(name).getNombre_entidad());
   			//imprimir indentacion
   			System.out.print(getIndentSpaces(indent) +"<" + name + ">");
   		}
    }
    @Override
    public void endElement(String uri, String name, String qName) {
    	System.out.print(getIndentSpaces(indent) + "</" + name + ">");
    	if ( buscarEntidadB(name) ){
        	//llamar a InsertScript(); y los datos en data
    		InsertScript();
    		//borrar data
    		data =null;
    	}else{
    		//borrar atributo
    		att=-1;
    	}
    }
    public void characters(char buf[], int offset, int len) throws SAXException {
    	sumarIndent(2);
    	//guardar valor
    	contenido = new String(buf, offset, len);
    	//almacenar valor en el vector Atributo del atributo
    	if (att>-1){
    		data.getAtributos().get(att).setValor(contenido);
    		System.out.print(getIndentSpaces(indent+2) + data.getAtributos().get(att).getValor());
    	}
    	//System.out.print(getIndentSpaces(indent+2) + data.getAtributos().get(att).getValor());
    	restarIndent(2);
    }

    
    
    /** Starts XML parsing example
     * @param args the command line arguments
     */
    public static void ParsearXML(String xmlFile) throws SAXException, IOException {
    	ParserXML pxml = new ParserXML();
    	pxml.leer(xmlFile);
    }
    
}//fin clase ParserXML
