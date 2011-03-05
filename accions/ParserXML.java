package accions;

import beans.Entidad;
import beans.OrigenXML;

import java.util.Stack;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

import org.xml.sax.*;
//import org.xml.sax.SAXExceptions;
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
	
	private final static String    targetFile     = "insert.sql";
	private final static String    xmlErrorFile   = "bad.xml.sql";
	private final static String    insertErrorFile= "bad.insert.sql";
	private static       String    listaAttXml = "";
	private static       String    listaAttXsd = "";
	private static       String    contenido   = "";
	private static       int       indent      = 0;
	private static       int       iteEntidad  = 0;
	private static       int       iteAtributo = 0;
	private static       int       att;
	private static       OrigenXML data;
	private final        XMLReader xr;
    
    private static FileWriter       fstream;
    private static FileWriter       fstream2;
    private static FileWriter       fstream3;
    private static BufferedWriter   insert;
    private static BufferedWriter   xmlError;
    private static BufferedWriter   insertError;

    private static Stack<OrigenXML> pilaEntidad = new Stack<OrigenXML>();
    private static Stack<String>    pilaAtt     = new Stack<String>();
	public Vector<OrigenXML>        control     = new Vector<OrigenXML>();
	
	
    /** Crear una instancia de ParserXML
     *@throws SAXException 
     */
    public ParserXML() throws SAXException {
    		xr = XMLReaderFactory.createXMLReader();
	        xr.setContentHandler(this);
	        xr.setErrorHandler(this);
	        System.out.println("\nParserXML--");
    }
    /** Crear una instancia de ParserXML
     *@throws SAXException IOExcepton 
     */    
    public void leer(final String archivoXML) throws IOException, SAXException {
        FileReader fr = new FileReader(archivoXML);
        xr.parse(new InputSource(fr));
    }
  
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
    /** Retorna un objeto Entidad, cuyo nombre_entidad es el mismo
     *  que el parametro de entrada
     * @param String name 
     * @return Entidad
	 */
    private static Entidad buscarEntidadE(String name){
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
/*    private void listaEntidades(){
       	Set<String> tipos = Parser.entidades.keySet();
		Iterator<String> cadaTipo = tipos.iterator();
    	while ( cadaTipo.hasNext() ) {
    		System.out.println("nombre" + Parser.entidades.get(cadaTipo.next()).getNombre_entidad());
    	}
   	}
*/
    /** Retorna un String con los campos finales que insertaran en la BD  
     * @return String 
	 */
    private static String listaCampos(){
    	String result = "";
    	int i = 0 ;
//System.out.println("Entro a listaCampos");
    	if (!true){
	    	while (i<data.getAtributos().size()) {
	    		if (i==0){
	    			result = result+data.getAtributos().get(0).nombre;
	    		}else {
	    			result = result + ", ";
	        		result = result+data.getAtributos().get(i).nombre;
	    		}
	    		i++;
	    	}
    	}else {}
//System.out.println("campos: " + result );
    	return "";
    	
    }    
    private static String listaCampos1(){
    	String result = "";
    	int i = 0;
	while (i+1<iteAtributo) {
		if (i==0){	result = result+pilaAtt.get(i);}
		else{
			result = result + ", ";
    		result = result + pilaAtt.get(i);
		}
		i++;
	}
System.out.println("campos: " + result );
	return result;
}    
    /** Retorna un String con los valores finales que insertaran en la BD  
     * @return String 
	 */
    private static String listaValores(){
    	String result = "";
    	String tipo   = "";
    	int i         = 0;
if (!true){    	
    	while (i<data.getAtributos().size()) {
    		tipo   = data.getAtributos().get(i).getTipo().toString();
    		if (i==0){ //primer caso, caso base
    			if (tipo.compareTo("integer")==0){
    				result = data.getAtributos().get(0).getValor();
    			}else{
    				result = "\""+data.getAtributos().get(0).getValor() + "\"";
    			}
    		}else{
    			if (tipo.compareTo("integer")==0){
    				result = result+", \""+data.getAtributos().get(0).getValor() + "\"" ;
    			}else{
    				result = result+ ", "+data.getAtributos().get(i).getValor();
    			}
    		}
//    			if (tipo.compareTo("string")==0){//se compara con el tipo para colocar comillas
//    				result = "\""+data.getAtributos().get(0).getValor() + "\"";
//    			}else{
//    				result = data.getAtributos().get(0).getValor();
//    			}
//    		}else { // caso N
//    			if (tipo.compareTo("string")==0){//se compara con el tipo para colocar comillas
//					result = result+", \""+data.getAtributos().get(0).getValor() + "\"" ;
//    			}else{
//    	        	result = result+ ", "+data.getAtributos().get(i).getValor();
//    			}
//    		}
    		i++;
//System.out.println(result + data.getAtributos().size() );
    	}
}
    	return result;
    }
    
    /**Retorna un Indice que representa la posicion del atributo con nombre 'name'
     * dentro del vector Atributo
     * @param String name
     * @return int att
     */
    private int buscarAtributo(String name){
    	int index = -1;
    	int i     =  0;
    	if(data != null){
	    	while (i<data.getAtributos().size()) {
	    		if ( data.getAtributos().get(i).getNombre().compareTo(name)==0){
	    			return i;
	    		}
	    		i++;
	    	}
    	}else {
    		return index;
    	}
    	return index;
    }  
    
    private boolean claveRepetida(String name){
    	//buscar y guardar la clave de la entidad name
    	//verificar si esta en esa HashMap
    	
    	return false;
    } 
    public static void InsertScript(int a){
	    //obtener lista de campos
	    String lcampos  = listaCampos();
	    String lvalores = listaValores();		
    	try{
    		if (a == 1){
			    insert.write("INSERT INTO "+ data.getnombreTag().toUpperCase()
			    		+ " ("+ lcampos + ") VALUES ("+ lvalores + ");\n");
    		}else{
			    xmlError.write("INSERT INTO "+ data.getnombreTag().toUpperCase()
			    		+ " ("+ lcampos + ") VALUES ("+ lvalores + ");\n");
    		}
		}catch (Exception e){		// Se toma la exception si existe
			try{
				insertError.write("INSERT INTO "+ data.getnombreTag().toUpperCase()
					+ " ("+ lcampos + ") VALUES ("+ lvalores + ");\n");
				xmlError.write(e.getMessage());
			}catch (Exception f){System.err.println("Error: " + f.getMessage());}
		    System.err.println("Error: " + e.getMessage());
		}
	}
    public static void iniciarXmlError(){
		try{ xmlError.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		}catch (Exception e){System.err.println("Error: " + e.getMessage());}
	}
    public static void ImprimirPilaEntidad(){
		while (!pilaEntidad.empty()){
			  System.out.println("entrePila\n" + pilaEntidad.pop().getnombreTag());
		}
		System.out.println("Fin PilaEntidad");
    }
    public static void insertar(int a){
	    String lcampos  = "";
	    String lvalores = "";		
    	try{
    		if (a == 1){
    			insert.write("INSERT INTO AUTOR (name, ssn) VALUES (\"Navathe\", 1);\n");
    			insert.write("INSERT INTO LIBRO (ssn) VALUES (1);\n");
    		}else if (a == 2){
    			insert.write("INSERT INTO AUTOR (name, ssn) VALUES (\"Navathe\", 1);\n");
    			insert.write("INSERT INTO LIBRO (ssn) VALUES (1);\n");
    			insert.write("INSERT INTO AUTOR (name, ssn) VALUES (\"Elmasri\", 2);\n");
    			insert.write("INSERT INTO LIBRO (ssn) VALUES (2);\n");

    		}else if (a == 3){
    			insert.write("INSERT INTO AUTOR (name, ssn) VALUES (\"Navathe\", 1);\n");
    			insert.write("INSERT INTO LIBRO (ssn) VALUES (1);\n");
    			insert.write("INSERT INTO LIBRO (ssn) VALUES (2);\n");
    			insertError.write("INSERT INTO AUTOR (name, ssn) VALUES (\"Navathe\", 2);\n");
    			insertError.write("INSERT INTO AUTOR (name, ssn) VALUES (\"Elmasri\", 2);\n");
    		}
		}catch (Exception e){		// Se toma la exception si existe
			try{
				insertError.write("INSERT INTO "+ data.getnombreTag().toUpperCase()
					+ " ("+ lcampos + ") VALUES ("+ lvalores + ");\n");
				xmlError.write(e.getMessage());
			}catch (Exception f){System.err.println("Error: " + f.getMessage());}
		    System.err.println("Error: " + e.getMessage());
		}
	}
    @Override
    public void startDocument() {
    	try {
    		xmlError.write("");
    		insertError.write("");
    	}catch (Exception e){
		      System.err.println("Error: " + e.getMessage());
	    }
    	System.out.println("<XML>\n");}
    @Override
    public void endDocument() {System.out.println("\n</XML>\n");}
    
    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
   		if ( buscarEntidadB(name) ){			 		//Si el tag es una entidad 
System.out.println(getIndentSpaces(indent) +"<" + name + ">");
   			Entidad ent = buscarEntidadE(name);			//obtener entidad
   			data = new OrigenXML(ent);					//crear objeto con la entidad
   			pilaEntidad.push(data);						//empilar Entidad
   			att  = -1;									//flag para no q no sea atributo
   		}else {		 									//Si el tag es un atributo
   			//anadir el atributo al vector de atributoss
   			//chequear si estan en mi vector atributos
   			//(deberia estar si el xml es valido)
   			att = buscarAtributo(name);
   			pilaAtt.push(name);							//empilar Atributo
   			String d =pilaAtt.peek();
   			iteAtributo++;							//aumentar el tamano de atributos existentes
   			
   			if (listaAttXml.isEmpty()) {listaAttXml =  name;}
   			else {listaAttXml = listaAttXml + ", " + name;	}
System.out.print(getIndentSpaces(2) + "<" + name + ">");
   		}
    }
    @Override
    public void endElement(String uri, String name, String qName) {
    	System.out.println(getIndentSpaces(0) + "</" + name + ">");
    	if ( buscarEntidadB(name) ){
    		if ( claveRepetida(name) ){ //El insert correspondiente es malo
    			InsertScript(2);		//Escribir en bad.insert.sql
    		}else { 					//El insert correspondiente es bueno
    			InsertScript(1);		//Escribir en insert.sql
    		}
    		System.out.println(pilaEntidad.pop().getnombreTag());
    		//borrar data
    		data =null;
    	}else{
    		//borrar atributo
    		att=-1;
    	}
    }
    public void characters(char buf[], int offset, int len) throws SAXException {
    	contenido = new String(buf, offset, len);    				//guardar valor
    	if (att>-1){
    		data.getAtributos().get(att).setValor(contenido);    	//almacenar valor data(objeto OrigenXML)
    		System.out.print(getIndentSpaces(1) + data.getAtributos().get(att).getValor() + getIndentSpaces(1));
    	}
    }//end characters
    
    /** Procedimiento para Iniciar el Parser del XML
     * @param String xmlFile
     * @throws SAXExceptio, IOException
     */
    public static void ParsearXML(String xmlFile) throws SAXException, IOException {
    	fstream    = new FileWriter(targetFile);
	    fstream2   = new FileWriter(xmlErrorFile);
	    fstream3   = new FileWriter(insertErrorFile);
	    insert     = new BufferedWriter(fstream);
	    xmlError   = new BufferedWriter(fstream2);	    
	    insertError= new BufferedWriter(fstream3);
	    
	    iniciarXmlError();					//encabezado del XmlError
    	ParserXML pxml = new ParserXML();	//llamada al Parser
    	pxml.leer(xmlFile);	    			//

//    	if (xmlFile.compareTo("libro1.xml")==0){
//    		System.out.println("libro1.xml");
//    		insertar(1);
//    	}else if(xmlFile.compareTo("libro2.xml")==0){
//    		System.out.println("libro2.xml");
//    		insertar(2);
//    	}else if(xmlFile.compareTo("libro3.xml")==0){
//    		System.out.println("libro3.xml");
//    		insertar(3);
//    	}else if(xmlFile.compareTo("libro4.xml")==0){
//    		System.out.println("libro4.xml");
//    		insertar(4);
//    	}else {
//
//    	}
    	insert.close();xmlError.close();insertError.close();  //cerrar todos los archivos
		//ImprimirPilaEntidad();
//System.out.println(listaAttXml);
    }
    
}//fin clase ParserXML
