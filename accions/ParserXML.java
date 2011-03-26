package accions;

import beans.Entidad;
import beans.OrigenXML;

import java.util.Iterator;
import java.util.Set;
import java.util.LinkedList;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/** Esta clase permite obtener y manipular la informacion contenida en los tags
 * correspondientes a los archivos XML(puro) para generar comandos de insercion
 * de datos SQL("INSERT INTO") para aquellas tablas que se crearon anteriormente.<br><br>
 * Lee los nodos XML y sus valores.<br><br>
 * Retorna un archivo "insert.sql"
 * @author Daniel Pedroza
 */
public class ParserXML extends DefaultHandler {
	
	private final static String    targetFile     = "insert.sql";		//archivo de salida
	private final static String    xmlErrorFile   = "bad.xml.xml";		//archivo de salida
	private final static String    insertErrorFile= "bad.insert.sql";	//archivo de salida
	private static       String    contenido      = "";	//Contenido entre los Tags
	private static       int       att;					//diferente de -1 si es Atributo
	private static       int   	   nivelActual=0; 		//
	private static       int   	   nivelError =999999; 	//
	private static       boolean   errorFound=false;	//
	private static       OrigenXML data;				//
	private final        XMLReader xr;					//
    
    private static FileWriter       fstream;
    private static FileWriter       fstream2;
    private static FileWriter       fstream3;
    private static BufferedWriter   insert;
    private static BufferedWriter   xmlError;
    private static BufferedWriter   insertError;

    //private static Stack<OrigenXML>       pilaEntidad    = new Stack<OrigenXML>();
    private static LinkedList<OrigenXML>  listaEntidad   = new LinkedList<OrigenXML>();
	
	
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
    	if (data !=null){
	    	while (i<data.getAtributos().size()) {
	    		if( data.getAtributos().get(i).check){
	    			
		    		if (result.compareTo("")==0){
		    			result = data.getAtributos().get(0).nombre;
		    		}else {
		    			result = result + ", ";
		        		result = result+data.getAtributos().get(i).nombre;
		    		}
	    		}
		    		i++;
	    	}
    	}
    	return result;

    }    
 
    
    /** Retorna un String con los valores finales que insertaran en la BD  
     * @return String 
	 */
    private static String listaValores(){
    	String result = "";
    	String tipo   = "";
    	int i         = 0;
    	if (data !=null){    	
	    	while (i<data.getAtributos().size()) {
	    		if( data.getAtributos().get(i).check){
		    		tipo   = data.getAtributos().get(i).getTipo().toString();
		    		if (result.compareTo("")==0){					 //primer caso, caso base
		    			if ((tipo.compareTo("integer")==0) || (tipo.compareTo("ID")==0) ){
		    				result = data.getAtributos().get(i).getValor();
		    			}else{result = "\""+data.getAtributos().get(i).getValor() + "\"";}
		    		}else{
		    			if ((tipo.compareTo("integer")==0) || (tipo.compareTo("ID")==0) ){
		    				result = result + ", "+ data.getAtributos().get(i).getValor();
		    			}else{result = result+ ", "+"\""+data.getAtributos().get(i).getValor() + "\"";}
		    		}
	    		}
		    		i++;
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

    /** Procedimiento que se encarga de escribir en los archivos de salida, 
     * si es correcto en insert.sql, si hay errores en insertError.sql,
     * si hay errores en el archivo en bad.xml.xml  
     *
     */
    /*public static void InsertScript(int a){
	    //obtener lista de campos
	    String lcampos  = listaCampos();
	    String lvalores = listaValores();		
    	try{
    		if (a == 1){
    			insert.write("INSERT INTO "+ data.getnombreTag().toUpperCase()
    					+ " ("+ lcampos + ") VALUES ("+ lvalores + ");\n");
			    //insert.write("INSERT INTO "+ pilaEntidad.peek().getnombreTag().toUpperCase()
    			//+ " ("+ lcampos + ") VALUES ("+ lvalores + ");\n");
    			
    		}else{
			    xmlError.write("INSERT INTO "+ data.getnombreTag().toUpperCase()
			    		+ " ("+ lcampos + ") VALUES ("+ lvalores + ");\n");
    		}
		}catch (Exception e){		// Se toma la exception si existe
			try{
				insertError.write("INSERT INTO "+ data.getnombreTag().toUpperCase()
					+ " ("+ lcampos + ") VALUES ("+ lvalores + ");\n");
				xmlError.write(e.getMessage());
			}catch (Exception f){System.err.println("Error"+ a + ": " + f.getMessage());}
		    System.err.println("Error: " + e.getMessage());
		}
<<<<<<< HEAD
	}
    public static void VaciarCola(int a){
=======
	}*/
    
    
    public static void VaciarCola(int a){

	    String lcampos ;
	    String lvalores;
     	while (listaEntidad.size()!=0) {
    		data = listaEntidad.getFirst();
    		lcampos  = listaCampos();	        	//obtener lista de campos
    	    lvalores = listaValores();
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
    			}catch (Exception f){System.err.println("Error"+ a + ": " + f.getMessage());}
    		    System.err.println("Error: " + e.getMessage());
    		}
    		data = listaEntidad.removeFirst();
    	}
	}//fin VaciarCola()*/
    
    public static void iniciarXmlError(){
		try{ xmlError.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		}catch (Exception e){System.err.println("Error: " + e.getMessage());}
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
System.out.print("*"+nivelActual);
    	if (errorFound){
    		try{xmlError.write(getIndentSpaces(nivelActual) +"<"+name+">");}catch (Exception e){}
    		nivelActual++;
    	}else{
	    	if ( buscarEntidadB(name) ){			 		//Si el tag es una entidad
	    		System.out.println(getIndentSpaces(nivelActual) +"<"+name+">");
	    		nivelActual++;	      					  	//sumar nivel actual
	   			Entidad ent = buscarEntidadE(name);			//obtener entidad
	   			data = new OrigenXML(ent);					//crear objeto con la entidad
	   			//pilaEntidad.push(data);						//empilar Entidad
	   			listaEntidad.addLast(data);					//encolar Entidad en una lista
	   			//controlEntidad.add(data);					//encolar Entidad en un vector
	   			att  = -1;									//flag para no q no sea atributo
	   		}else {		 									
	   			System.out.print(getIndentSpaces(nivelActual) + "<" + name + ">");
	   			att = buscarAtributo(name);
	   			if (buscarAtributo(name)!=-1) {				//Si el tag es un atributo
	   				data.getAtributos().get(att).check = true;
	   			}else if (buscarAtributo(name)==-1 ){
	   				System.out.println("caso "+name);	  		//caso <dblp>   no hacer nada
	   			}else {						//Si tag no esta definido, no entidad, no atributo
	   				try{xmlError.write(getIndentSpaces(nivelActual) +"<"+name+">");}catch (Exception e){}
	   				System.out.println("caso no definido");
	   				nivelError=nivelActual;
	   				errorFound =true;
	   			}
	   		}//fin else(tag atributo
   		}
    }//fin startElement

    @Override
    public void endElement(String uri, String name, String qName) {
    	System.out.println(getIndentSpaces(0) + "</" +name + ">");
    	if (errorFound){
    		//System.out.println("Erorrrrr");
    		try{xmlError.write(getIndentSpaces(nivelActual) + "</" + name + ">\n");}catch (Exception e){}
    		if (nivelActual==nivelError){errorFound=false;}
    		nivelActual--;
    	}else{    	
	    	if ( buscarEntidadB(name) ){
	    		nivelActual--;
	    		//El insert correspondiente es malo => bad.insert.sql
	    		if (claveRepetida(name)){
	    			VaciarCola(2);}
	    			/*InsertScript(2);*///}
	    		else {			//El insert correspondiente es bueno =>insert.sql
	    			if (nivelActual==0) {
	    				//InsertScript(1);
	    				VaciarCola(1);
	    				}
	    		}
	    	}else{ att=-1;}    							//Borrar atributo
    	}
    }//fin endElment*/
    public void characters(char buf[], int offset, int len) throws SAXException {
    	contenido = new String(buf, offset, len);    	//guardar valor
    	if (errorFound){
    		try{xmlError.write(getIndentSpaces(nivelActual) + contenido );}catch (Exception e){}
    	}else{  
	    	if (att>-1){ 				//almacenar contenido en data(objeto OrigenXML)
	    		data.getAtributos().get(att).setValor(contenido); 
	    		System.out.print(getIndentSpaces(1) 
	    				+ data.getAtributos().get(att).getValor() + getIndentSpaces(1));
	    	}
    	}
    }//end characters
    
    /** Procedimiento para iniciar el Parser del XML
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
	    
	    iniciarXmlError();					//Encabezado para archivos de salida
    	ParserXML pxml = new ParserXML();	//Crear el Objeto Parser
    	pxml.leer(xmlFile);	    			//Ejecutar el parser

    	insert.close();xmlError.close();insertError.close();  //cerrar todos los archivos
    	//imprimirPilaEntidad();
    }
    
}//fin clase ParserXML
