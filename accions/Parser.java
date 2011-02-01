package accions;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.SAXException;

import beans.Atributo;
import beans.Entidad;

import accions.MostrarEntidades;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.parser.XSOMParser;

public class Parser {
	

	public static XSSchemaSet CrearParser(File archivo) throws SAXException, IOException {
		XSOMParser parser = new XSOMParser();
		parser.parse(archivo);
		return parser.getResult();
	}
	
	public static void restricciones(XSRestrictionSimpleType restriction){
		System.out.println("Tipo base : "+restriction.getBaseType().getName());
        
    	if(restriction != null){
    	
    		Iterator<?extends XSFacet> i = restriction.getDeclaredFacets().iterator();
            while(i.hasNext()){
            	
                XSFacet facet = i.next();
                if(facet.getName().equals(XSFacet.FACET_ENUMERATION)){
                	System.out.println("Restriccion enumeracion: "+facet.getValue().value);
                }
                if(facet.getName().equals(XSFacet.FACET_MAXINCLUSIVE)){
                	System.out.println("Restriccion maxinclusive : "+facet.getValue().value);
                }
                if(facet.getName().equals(XSFacet.FACET_MININCLUSIVE)){
                	System.out.println("Restriccion mininclusive: "+facet.getValue().value);
                }
                if(facet.getName().equals(XSFacet.FACET_MAXEXCLUSIVE)){
                	System.out.println("Restriccion maxExclusive: "+String.valueOf(Integer.parseInt(facet.getValue().value) - 1));
                }
                if(facet.getName().equals(XSFacet.FACET_MINEXCLUSIVE)){
                	System.out.println("Restriccion MinExclusive : "+String.valueOf(Integer.parseInt(facet.getValue().value) + 1));
                }
                if(facet.getName().equals(XSFacet.FACET_LENGTH)){
                	System.out.println("Restriccion Longitud : "+facet.getValue().value);
                }
                if(facet.getName().equals(XSFacet.FACET_MAXLENGTH)){
                	System.out.println("Restriccion MaxLongitud: "+facet.getValue().value);
                }
                if(facet.getName().equals(XSFacet.FACET_MINLENGTH)){
                	System.out.println("Restriccion MinLongitud: "+facet.getValue().value);
                }
                if(facet.getName().equals(XSFacet.FACET_PATTERN)){
                	System.out.println("Restriccion patrones: "+facet.getValue().value);
                }
 
            }  
    	}
	}
	public static Entidad leerAtributos2( XSComplexType complex,Entidad entidad, XSAttributeDecl decl){
		Vector<Atributo> atributos = new Vector();
		for (XSAttributeUse attributeUse : complex.getAttributeUses()) {
			Atributo nuevo_atributo = new Atributo();
  			decl = attributeUse.getDecl();
  			nuevo_atributo.setNombre(decl.getName());
  			nuevo_atributo.setTipo(decl.getType().getName());
  			nuevo_atributo.setNulo(attributeUse.isRequired());
  			
  			 atributos.add(nuevo_atributo);
  		}
		entidad.setAtributos(atributos);
	 	return entidad;
		
	}
	public static Entidad leerAtributos (XSParticle[] particles, Entidad entidad){
		 XSTerm pterm;
		 XSRestrictionSimpleType restriction;
		 Vector<Atributo> atributos = new Vector();
		
		 	for(XSParticle p : particles ){
		 		Atributo nuevo_atributo = new Atributo();
		 		pterm = p.getTerm();
                if(pterm.isElementDecl()){ //xs:element inside complex type
                	//Se verifica si tiene SimpleType y Restricciones
                	if (pterm.asElementDecl().getType().isSimpleType()){
                		//System.out.println("Tiene Restriccion : "+pterm.asElementDecl().getType().asSimpleType().isRestriction());
                		//Se verifican las restricciones existentes
                		restriction = pterm.asElementDecl().getType().asSimpleType().asRestriction();
                		restricciones(restriction);
                	}
                	//Se obtiene el nombre del atributo
                	String nombreAttr = pterm.asElementDecl().getName();
                	//Se obtiene el tipo del atributo
                	String tipoAttr = pterm.asElementDecl().getType().getName();
                	nuevo_atributo.setNombre(nombreAttr);
                	nuevo_atributo.setTipo(tipoAttr);
                	if (p.getMinOccurs()==1){
                		nuevo_atributo.setNulo(false);
                	}
                	//System.out.println("	Atributo: "+ nombreAttr);
                	//System.out.println("	Tipo: "+ tipoAttr);
                	//Se obtiene el min y max de los atributos
                	//System.out.println("	MaxOccurs : " + p.getMaxOccurs() + "    MinOccurs :" + p.getMinOccurs());
                	//System.out.println("");	
                }
                atributos.add(nuevo_atributo);
               
            }
		 	entidad.setAtributos(atributos);
		 	return entidad;
	}
	
	public static Vector leerEntidades(Iterator<String> claves, Iterator<XSComplexType> valores ){
		 String nombre;
		 XSComplexType complex;
		 XSContentType contenido;
		 XSParticle particle;
		 XSAttributeDecl decl = null;
		 XSTerm term;
		 XSModelGroup xsModelGroup;
		 XSParticle[] particles;
		 Vector<Entidad> entidades = new Vector<Entidad>();
		
		 
		  	while(claves.hasNext() && valores.hasNext())  {
		  		Entidad nueva_entidad = new Entidad();
		  		nombre = (String)claves.next();
		  		nueva_entidad.setNombre_entidad(nombre);
		  		System.out.print("-------Entidad/ComplexType------  "+ nueva_entidad.getNombre_entidad() + "\n");
	    	 
		  		//Estamos en busqueda de examinar sus elements a nivel interno (atributos)
		  		complex = (XSComplexType)valores.next();
		  		contenido = complex.getContentType();
		  		particle = contenido.asParticle(); //Se optienen los elementos dentro del complexType
	    	 
		  		//Se verifica si los elementos tienen atributos con el tipo ATTRIBUTE
		  		leerAtributos2(complex,nueva_entidad,decl);
		  		
	    	 
		  		//Se verifica que el complexType sea diferente de nulo
		  		if(particle != null){
		  			term = particle.getTerm();
			        if(term.isModelGroup()){
			        	xsModelGroup = term.asModelGroup();
			            particles = xsModelGroup.getChildren();
			            
			            //se verifica que sea sequence, all o choice
			            System.out.println("Compositor " +xsModelGroup.getCompositor().toString());
			            
			            // Se leen los atributos de las entidades
			            nueva_entidad=leerAtributos(particles,nueva_entidad);
			        }
			    }
		  		entidades.add(nueva_entidad);
	    } return entidades;
	}

	public static void main(final String[] args) throws SAXException, IOException
	{
		MostrarEntidades m = new MostrarEntidades();
		File file = new File("ejemplo.xml");
		try {
		
			XSSchemaSet result = CrearParser(file);
		   
			//Iteramos sobre los diferentes schemas que pudieran estar definidos 
			//en un solo documento
			Iterator<XSSchema> itr = result.iterateSchema();
			while( itr.hasNext() ) 
			{
				//Ahora iteramos sobre cada uno de los schemas individualmente
				XSSchema schema = (XSSchema)itr.next();
				System.out.print("Esquema nuevo: \n ");
				
				//ComplexTypes (al menos los del nivel más externo// 
			    Map<String, XSComplexType> mapa = (Map<String, XSComplexType>) schema.getComplexTypes();  
			    System.out.print("Tamano: "+ ((Map<String, XSComplexType>) mapa).size()+ "\n"); 
			    Iterator<String> claves = ((Map<String, XSComplexType>) mapa).keySet().iterator();	//Se obtienen todos los complexTypes
			    Iterator<XSComplexType> valores = ((Map<String, XSComplexType>) mapa).values().iterator();
			
			    //Se itera sobre los complexTypes del Schema.
			    
			    m.imprimirEntidades(leerEntidades(claves,valores));
			
			}
		}
		catch (Exception exp) {
			exp.printStackTrace(System.out);
		}
	}
	



}

