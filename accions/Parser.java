package accions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.xml.sax.SAXException;
import beans.Atributo;
import beans.Entidad;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.parser.XSOMParser;

public class Parser {
	
	static HashMap<String, Entidad> entidades = new HashMap<String, Entidad>();

	public static XSSchemaSet CrearParser(File archivo) throws SAXException,
			IOException {
		XSOMParser parser = new XSOMParser();
		parser.parse(archivo);
		return parser.getResult();
	}

	public static void restricciones(XSRestrictionSimpleType restriction, Atributo atributo) {
		
		atributo.setTipo(restriction.getBaseType().getName());
		
		if (restriction != null) {

			Iterator<? extends XSFacet> i = restriction.getDeclaredFacets().iterator();
			while (i.hasNext()) {

				XSFacet facet = i.next();
				if (facet.getName().equals(XSFacet.FACET_ENUMERATION)) {
					Vector<String> Dominio= atributo.getDominio();
					Dominio.add(facet.getValue().value);
					atributo.setDominio(Dominio);								
				}
				if (facet.getName().equals(XSFacet.FACET_MAXINCLUSIVE)) {
					ArrayList<String> rango= atributo.getRango();
					rango.add(1, facet.getValue().value);
					atributo.setRango(rango);
									
				}
				if (facet.getName().equals(XSFacet.FACET_MININCLUSIVE)) {
					ArrayList<String> rango= atributo.getRango();
					rango.add(0, facet.getValue().value);
					atributo.setRango(rango);
					
				}
				if (facet.getName().equals(XSFacet.FACET_MAXEXCLUSIVE)) {
					
					System.out.println("AVERTENCIA: este Maximo se tomará como inclusivo");
					ArrayList<String> rango= atributo.getRango();
					rango.add(1, facet.getValue().value);
					atributo.setRango(rango);
				}
				if (facet.getName().equals(XSFacet.FACET_MINEXCLUSIVE)) {
					System.out.println("AVERTENCIA: este Maximo se tomará como inclusivo");
					ArrayList<String> rango= atributo.getRango();
					rango.add(0, facet.getValue().value);
					atributo.setRango(rango);
				}
				if (facet.getName().equals(XSFacet.FACET_LENGTH)) {
					atributo.setLongitud(facet.getValue().value);
					
				}
				if (facet.getName().equals(XSFacet.FACET_MAXLENGTH)) {
					atributo.setLongitud(facet.getValue().value);
				}
				if (facet.getName().equals(XSFacet.FACET_MINLENGTH)) {
					System.out.println("ADVERTENCIA: el campo "+facet.getValue().value+ "perteneciente a la restricción minLength es inválido en el modelo ER"); 
				}
				if (facet.getName().equals(XSFacet.FACET_PATTERN)) {
					System.out.println("ADVERTENCIA: el campo "+facet.getValue().value+ "perteneciente a la restricción pattern value es inválido en el modelo ER");
				}

			}
		}
	}

	public static void leerAtributos2(XSComplexType complex, String tipoEntidad) {
		
		XSAttributeDecl decl = null;
		Vector<Atributo> atributos = new Vector<Atributo>();
	
		
		for (XSAttributeUse attributeUse : complex.getAttributeUses()) {
		
			Atributo nuevo_atributo = new Atributo();
			decl = attributeUse.getDecl();
			nuevo_atributo.setNombre(decl.getName());
			nuevo_atributo.setTipo(decl.getType().getName());
			nuevo_atributo.setNulo(attributeUse.isRequired());
			atributos.add(nuevo_atributo);
		}
			Entidad entidad = entidades.get(tipoEntidad);
			entidad.setAtributos(atributos);
		
		
	}

	public static void leerElementos(XSParticle[] particles, String tipo) {
		XSTerm pterm;
		XSRestrictionSimpleType restriction;
		Vector<Atributo> atributos = new Vector<Atributo>();
		Vector<Atributo> referencias= new Vector<Atributo>();

		// Se crea el vector de los tipos básicos y se le meten esos valores
		
		Vector<String> tiposBasicos = new Vector<String>();
		tiposBasicos.add("string");
		tiposBasicos.add("decimal");
		tiposBasicos.add("integer");
		tiposBasicos.add("boolean");
		tiposBasicos.add("date");
		tiposBasicos.add("time");
		
		Entidad entidad = entidades.get(tipo); // Entidad en donde se encuentran estos elementos
		String tipoAttr=null;
		
		for (XSParticle p : particles) {
			Atributo nuevo_atributo = new Atributo();
			pterm = p.getTerm();
			
			if (pterm.isElementDecl()) { // xs:element inside complex type
				// Se verifica si tiene SimpleType y Restricciones
				
				// Se obtiene el nombre del atributo
				String nombreAttr = pterm.asElementDecl().getName();
				
				// Se obtiene el tipo del atributo
				tipoAttr = pterm.asElementDecl().getType().getName();
				//Se obtiene el valor por defecto OJOO: Aun no esta implementado
				/*String def=null;
				if(pterm.asElementDecl().getDefaultValue().value != ("null"))
				{
					def= pterm.asElementDecl().getDefaultValue().toString();
				}
				else
				{
				
					System.out.print(pterm.asElementDecl().getDefaultValue());
				}*/
				nuevo_atributo.setNombre(nombreAttr);
				nuevo_atributo.setTipo(tipoAttr);
				//nuevo_atributo.setValor(def);
				
				//Se obtienen las retricciones
				if (pterm.asElementDecl().getType().isSimpleType()) {
					// System.out.println("Tiene Restriccion : "+pterm.asElementDecl().getType().asSimpleType().isRestriction());
					// Se verifican las restricciones existentes
					restriction = pterm.asElementDecl().getType().asSimpleType().asRestriction();
					restricciones(restriction,nuevo_atributo);
				}
				
				//Se obtiene el minOccurs y maxOccurs, así como si el atributo es null o not null
				if (p.getMinOccurs() == 1) {
					nuevo_atributo.setNulo(false);
				}
				nuevo_atributo.setMinOccurs(p.getMinOccurs());
				nuevo_atributo.setMaxOccurs(p.getMaxOccurs());
				
				
				// System.out.println("	Atributo: "+ nombreAttr);
				// System.out.println("	Tipo: "+ tipoAttr);
				// Se obtiene el min y max de los atributos
				// System.out.println("	MaxOccurs : " + p.getMaxOccurs() +
				// "    MinOccurs :" + p.getMinOccurs());
				// System.out.println("");
			}
			
			//Coloca el atributo en el vector al que pertenece
			if(tiposBasicos.contains(tipoAttr))
			{
			  atributos.add(nuevo_atributo);
			}
			else{
				referencias.add(nuevo_atributo);
			}

		}
		entidad.setAtributos(atributos);
		entidad.setReferencias(referencias);
			
	}

	private static void leerEntidades(Iterator<String> claves, Iterator<XSElementDecl> valores) {
		
		String tipo;
		while (claves.hasNext() && valores.hasNext()) {
			Entidad nueva_entidad = new Entidad();
			String nombre = (String) claves.next();
			XSElementDecl element = (XSElementDecl) valores.next();
			tipo = element.getType().getName();
			nueva_entidad.setTipo(tipo);
			nueva_entidad.setNombre_entidad(nombre);
			entidades.put(tipo, nueva_entidad);			
		}
		
	}

	public static void LeerAtributosEntidades(Iterator<String> claves, Iterator<XSComplexType> valores){	
		
		XSComplexType complex;
		XSContentType contenido;
		XSParticle particle;
		XSTerm term;
		XSModelGroup xsModelGroup;
		XSParticle[] particles;
		
		while (claves.hasNext() && valores.hasNext()) {
			
			String tipo = (String) claves.next();
			if(!entidades.containsKey(tipo)){
				System.out.print("ALERTA: El elemento del tipo "+ tipo +" no esta definido.\n");
			}
			else
			{
			
				// Estamos en busqueda de examinar los elements a nivel interno (atributos)
				complex = (XSComplexType) valores.next();
				contenido = complex.getContentType();
				particle = contenido.asParticle(); // Se optienen los elementos dentro del complexType
	
				// Se verifica si los elementos tienen atributos con el tipo ATTRIBUTE y se agregan estos a su respectiva entidad
				leerAtributos2(complex,tipo);
	
				// Se verifica que el complexType sea diferente de nulo
				if (particle != null) {
					term = particle.getTerm();
					if (term.isModelGroup()) {
						xsModelGroup = term.asModelGroup();
						particles = xsModelGroup.getChildren();
	
						// se verifica que sea sequence, all o choice
						System.out.println("Compositor "+ xsModelGroup.getCompositor().toString());
	
						// Se leen los atributos de las entidades
						leerElementos(particles, tipo);
					}
				}
			}
		}
	}
	
	public static void ImprimirEntidades() {

		Set<String> tipos = entidades.keySet();
		Iterator<String> cadaTipo = tipos.iterator();

		while (cadaTipo.hasNext()) {
			Entidad entidad = entidades.get(cadaTipo.next());

			System.out.println("-- Entidad: " + entidad.getNombre_entidad()	+ " --");
			System.out.println("-- Tipo:    " + entidad.getTipo() + " --");

			int j = entidad.getAtributos().size() - 1;

			Vector<Atributo> atributos = entidad.getAtributos();
			System.out.println("-- Atributos básicos --");

			while (j >= 0) {
				System.out.println("	Nombre : " + atributos.get(j).getNombre());
				System.out.println("		Tipo : " + atributos.get(j).getTipo());
				System.out.println("		Nulo : " + atributos.get(j).isNulo());

				j--;
			}
			Vector<Atributo> referencias = entidad.getReferencias();
			System.out.println("-- Atributos Hechos por el usuario--");
			
			j= entidad.getReferencias().size()-1;
			
			while (j >= 0) {
				System.out.println("	Nombre : " + referencias.get(j).getNombre());
				System.out.println("		Tipo : " + referencias.get(j).getTipo());
				System.out.println("		Nulo : " + referencias.get(j).isNulo());

				j--;
			}
			

		}
		
	}

	public static void main(final String[] args) throws SAXException, IOException {
		File file = new File("ejemplo.xml");
		try {

			XSSchemaSet result = CrearParser(file);

			// Iteramos sobre los diferentes schemas que pudieran estar
			// definidos
			// en un solo documento
			Iterator<XSSchema> itr = result.iterateSchema();
			while (itr.hasNext()) {
				// Ahora iteramos sobre cada uno de los schemas individualmente
				itr.next();
				XSSchema schema = (XSSchema) itr.next();
				System.out.print("Esquema nuevo: \n ");

				
				/* 
				 * Elements (al menos del nivel mas externo)
				 * (Entidades)(verificar que sean de un tipo definido por el
				 * usuario
				 * (algún complexType))
				 */
				
				Map<String, XSElementDecl> mapa1 = (Map<String, XSElementDecl>) schema.getElementDecls();
				Iterator<String> claves = ((Map<String, XSElementDecl>) mapa1).keySet().iterator();
				Iterator<XSElementDecl> valores = ((Map<String, XSElementDecl>) mapa1).values().iterator();
				
				leerEntidades(claves, valores);
												 
				// ComplexTypes (al menos los del nivel más externo//
				Map<String, XSComplexType> mapa = (Map<String, XSComplexType>) schema.getComplexTypes();
				System.out.print("Tamano: " + ((Map<String, XSComplexType>) mapa).size() + "\n");
				Iterator<String> claves1 = ((Map<String, XSComplexType>) mapa).keySet().iterator(); 
				// Se obtienentodos los complexTypes Iterator<XSComplexType>
				Iterator<XSComplexType>valores1 =((Map<String, XSComplexType>) mapa).values().iterator();
				LeerAtributosEntidades(claves1, valores1);
				 

				ImprimirEntidades();

			}
		} catch (Exception exp) {
			exp.printStackTrace(System.out);
		}
	}
}
