
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.*;

import org.xml.sax.helpers.DefaultHandler;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.XMLReaderAdapter;

class ParserXML extends DefaultHandler {

    private final XMLReader xr;

    public LectorXML() throws SAXException {
        xr = XMLReaderFactory.createXMLReader();
        xr.setContentHandler(this);
        xr.setErrorHandler(this);
    }

    public void leer(final String archivoXML) throws IOException, SAXException {
        FileReader fr = new FileReader(archivoXML);
        xr.parse(new InputSource(fr));
    }

    @Override
    public void startDocument() {
        System.out.println("Comienzo del Documento XML");
    }

    @Override
    public void endDocument() {
        System.out.println("Final del Documento XML");
    }

    @Override
    public void startElement(String uri, String name,
              String qName, Attributes atts) {
        System.out.println("tElemento: " + name);

        for (int i = 0; i < atts.getLength(); i++) {
         System.out.println("ttAtributo: " +
          atts.getLocalName(i) + " = "+ atts.getValue(i));
        }
    }

    @Override
    public void endElement(String uri, String name,
                                 String qName) {
        System.out.println("tFin Elemento: " + name);
    }
    
    public static void main(String[] args) throws IOException, SAXException {
        LectorXML lector = new LectorXML();
        lector.leer("libro.xml");
    }
    
    
}
