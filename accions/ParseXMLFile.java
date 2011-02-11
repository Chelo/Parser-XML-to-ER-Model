package accions;

import beans.Atributo;
import beans.Entidad;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/** This class represents short example how to parse XML file,
 * get XML nodes values and its values.<br><br>
 * It implements method to save XML document to XML file too
 * @author Martin Glogar
 */
public class ParseXMLFile {
    
    private final static String xmlFileName = "libro.xml";
    private final static String targetFileName = "insert.sql";
    
    static HashMap<Integer, Entidad> entidades = new HashMap<Integer, Entidad>();
    
    /** Crear una instancia de ParseXMLFile */
    public ParseXMLFile() {
        //Parsear archivo xml
    	//devuelve un tipo Document
        Document doc = parseFile(xmlFileName);
        
        //Obtener la raiz del arbol xml
        Node raiz = doc.getDocumentElement();
        
        //Imprimir contenido XML
        System.out.println("________________________\n");
        writeDocumentToOutput(raiz,0);
        System.out.println("________________________\n");

        //Crear el archivo sql insert
        //saveXMLDocument(targetFileName, doc);
       
    }
    
    /** Returns element value
     * @param elem element (it is XML tag)
     * @return Element value otherwise empty String
     */
    public final static String getElementValue( Node elem ) {
        Node kid;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling() ){
                    if( kid.getNodeType() == Node.TEXT_NODE  ){
                        return kid.getNodeValue();
                    }
                }
            }
        }
        return "";
    }
    
    private String getIndentSpaces(int indent) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            buffer.append(" ");
        }
        return buffer.toString();
    }
    
    /** Writes node and all child nodes into System.out
     * @param node XML node from from XML tree wrom which will output statement start
     * @param indent number of spaces used to indent output
     */
    public void writeDocumentToOutput(Node node,int indent) {

    	String nodeName         = node.getNodeName();
        String nodeValue        = getElementValue(node);
        NamedNodeMap attributes = node.getAttributes();
        
        Set<Integer>      tipos    = entidades.keySet();
		Iterator<Integer> cadaTipo = tipos.iterator();
        int             indentTemp = indent;          

    	Entidad entidad = new Entidad();
    	entidad.setNombre_entidad(nodeName);
    	
    	entidades.put(indent, entidad);
        
        
        if (!nodeValue.isEmpty()){

        	System.out.println(getIndentSpaces(indent) + "<" + nodeName + "> " + nodeValue);
        }
        
        //Se imprime el nodo
        if (indentTemp==-1) {
        	
        	
        	System.out.println(getIndentSpaces(indent) + "<" + nodeName + "> " + nodeValue);
        	//System.out.println(nodeValue);
            System.out.println("INSERT INTO "+ nodeName.toUpperCase()+ "\n"+ "VALUES (" + nodeValue + ")\n" );
        }
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            System.out.println(getIndentSpaces(indent + 2) 
            		+ "AttributeName: "    + attribute.getNodeName() 
            		+ ", attributeValue: " + attribute.getNodeValue());
            System.out.println("INSERT INTO "
            		+ nodeName.toUpperCase()
            		+ "\n"+ "VALUES (" 
            		+ attribute.getNodeValue() + ")\n" );
        }
                

        //Escribir toso los hijos nodos "recursivamente"
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                writeDocumentToOutput(child,indent + 2);
            }
        }
    }
    
    /** Guardar archivo SQL para insertar datos
     * @param fileName nombre del archivo SQL
     * @param doc XML document to save
     * @return <B>true</B> if method success <B>false</B> otherwise
     */    
    public boolean saveXMLDocument(String fileName, Document doc) {
        System.out.println(fileName);
        // open output stream where XML Document will be saved
        
        File file = new File(fileName);
        FileOutputStream fos;
        Transformer transformer;
        try {
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {
            System.out.println("Error occured: " + e.getMessage());
            return false;
        }

        // Use a Transformer for output
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformer = transformerFactory.newTransformer();
        }
        catch (TransformerConfigurationException e) {
            System.out.println("Transformer configuration error: " + e.getMessage());
            return false;
        }
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(fos);
        // transform source into result will do save
        try {
            transformer.transform(source, result);
        }
        catch (TransformerException e) {
            System.out.println("Error transform: " + e.getMessage());
        }
        System.out.println(fileName + " Guardado");
        return true;
    }
    
    /** Parsea el Archivo Parses XML file and returns XML document.
     * @param fileName archivo XML que se quiere parsear
     * @return XML document o null si ocurre algun error
     */
    public Document parseFile(String fileName) {
        System.out.println(fileName+ "\n");
        
        DocumentBuilder docBuilder;
        Document doc = null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        }catch (ParserConfigurationException e) {
            System.out.println("Wrong parser configuration: " + e.getMessage());
            return null;
        }
        
        File sourceFile = new File(fileName);
        try {
            doc = docBuilder.parse(sourceFile);
        }
        catch (SAXException e) {
            System.out.println("Wrong XML file structure: " + e.getMessage());
            return null;
        }
        catch (IOException e) {
            System.out.println("Could not read source file: " + e.getMessage());
        }
        System.out.println("ARCHIVO XML LEIDO COMPLETAMENTE");
        return doc;
    }
    
    /** Starts XML parsing example
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ParseXMLFile();
    }
    
}
