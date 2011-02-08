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
import com.sun.xml.xsom.parser.XSOMParser;


/**
 * La clase Parser parsea documentos XMLSchema y asocia los diferentes 
 * elementos definidos dentro de estos esquemas con las estructuras de datos 
 * que representar&#225n elementos dentro del modelo Relacional.
 * 
 * @author 
 * Karina Aguiar
 * Liliana Barrios y  
 * Consuelo G&#243mez
 * 
 */
public class Parser {
	 
	//La clave del HashMap es el nombre del complexType dentro del cual se definen los atributos de la correspondiente entidad
	static HashMap<String, Entidad> entidades = new HashMap<String, Entidad>();

	
	/**
	 * El m&#233todo CrearParser es el encargado de crear un nuevo 
	 * XSOM parser y parsear el archivo (XMLSchema) indicado el 
	 * cual se retornar&#225 en un XSSchemaSet
	 * 
	 * @param archivo El XMLSchema a parsear
	 * @return Archivo parseado (XSSchemaSet)
	 * @throws SAXException
	 * @throws IOException
	 */
	public static XSSchemaSet CrearParser(File archivo) throws SAXException,
			IOException {
		XSOMParser parser = new XSOMParser();
		parser.parse(archivo);
		return parser.getResult();
	}

	/**
	 * La funci&#243n restricciones es la encargada de asociar las "restriction" tags
	 * del XMLSchema con restricciones del modelo Relacional.
	 * 
	 * Alguna de las restricciones que se identifican son:
	 * - Enumeraci&#243n (asociado a restricciones de dominio).
	 * - El m&#237nimo y m&#225ximo valor que puede tomar un atributo num&#233rico.
	 * - El m&#225ximo n&#250mero de caracteres que puede tomar un atributo de tipo String. 
	 * 
	 * @param restriction XSRestrictionSimpleType
	 * @param atributo atributo al cual pertenecen las restricciones
	 * @return atributo al cual pertenecen las restricciones con las restricciones incluidas
	 */
	public static Atributo restricciones(XSRestrictionSimpleType restriction, Atributo atributo) {
		String id = "ID";
		
		//Se verifica los tipos claves y los tipos setteados en las resticciones
		if (restriction.getBaseType().getName() != "anySimpleType"){
			if (!(atributo.getTipo()==null) && !(atributo.getTipo().equals(id)))
				atributo.setTipo(restriction.getBaseType().getName()); 
			else if ((atributo.getTipo()==null))
				atributo.setTipo(restriction.getBaseType().getName()); 		
		}
	
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
		}return atributo;
	}

	/**
	 * La funci&#243n leerAtributos2 es la encargada de leer los atributos de una
	 * entidad que est&#233n definidos bajo el tag "attribute"dentro de un ComplexType 
	 *  
	 * @param complex el ComplexType (Tipo que define a una entidad) del cual se quieren extraer (leer) 
	 * sus atributos  
	 * @param tipoEntidad nombre del ComplexType del cual se quieren extraer (leer) sus atributos. 
	 */
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

	/**
	 * La funci&#243n leerElementos es la encargada de leer los atributos de una
	 * entidad que est&#233n definidos bajo el tag "element" dentro de un ComplexType 
	 * 
	 * @param particles es una arreglo unidimensional de XSParticle, el cual contiene en cada casilla la informaci&#243n
	 * particular de cada uno de los elementos "element" definidos dentro de un ComplexType (Tipo que define
	 * una entidad)
	 * @param tipo nombre del ComplexType al cual pertenece el arreglo de XSParticles (particles) y del cual se 
	 * quieren extraer (leer) sus atributos.
	 * @param atributos arreglo unidimensional de Atributos, donde est&#225n almacenados los atributos de 
	 * la entidad definida por el ComplexType bajo el nombre "tipo"   
	 * @return arreglo unidimensional de Atributos, donde se almacenar&#225n el resto de los atributos 
	 * que est&#233n definidos bajo el tag "element", pertenecientes a la entidad definida por el ComplexType de nombre "tipo" 
	 * y que ser&#225n parseados por esta funci&#243n. 
	 */
	public static Vector<Atributo> leerElementos(XSParticle[] particles, String tipo, Vector<Atributo> atributos) {
		XSTerm pterm;
		XSRestrictionSimpleType restriction;
		String id = "ID";
		
		// Se crea el vector de los tipos básicos y se le meten esos valores
		Vector<String> tiposBasicos = new Vector<String>();
		tiposBasicos.add("string");
		tiposBasicos.add("decimal");
		tiposBasicos.add("integer");
		tiposBasicos.add("boolean");
		tiposBasicos.add("date");
		tiposBasicos.add("time");
		//OJO ESTO PUEDE SER PELIGROSO
		//tiposBasicos.add("anySimpleType");
		//tiposBasicos.add("null");
		tiposBasicos.add("ID");
		
		String nombreAttr;
		String tipoAttr=null;
		String valorPorDefecto;
		Entidad entidad = entidades.get(tipo); // Entidad en donde se encuentran estos elementos

		
		int i = 0;
		for (XSParticle p : particles) 
		{
			Atributo nuevo_atributo = new Atributo();
			pterm = p.getTerm();
			i++;
			
			if (pterm.isElementDecl()) { // xs:element inside complex type
				
				// Se obtiene el nombre del atributo
				nombreAttr = pterm.asElementDecl().getName();
				nuevo_atributo.setNombre(nombreAttr);
				
				// Se obtiene el tipo del atributo
				tipoAttr = pterm.asElementDecl().getType().getName();
				nuevo_atributo.setTipo(tipoAttr);
				
				// Se verifica si existe un valor por defecto
				if(!(pterm.asElementDecl().getDefaultValue()== null))
				{	
					valorPorDefecto = pterm.asElementDecl().getDefaultValue().toString();
					nuevo_atributo.setValor(valorPorDefecto);
				}
				
				//Se obtienen las retricciones
				if (pterm.asElementDecl().getType().isSimpleType()) {
					// System.out.println("Tiene Restriccion : "+pterm.asElementDecl().getType().asSimpleType().isRestriction());
					// Se verifican las restricciones existentes
					restriction = pterm.asElementDecl().getType().asSimpleType().asRestriction();
					nuevo_atributo = restricciones(restriction,nuevo_atributo);
					tipoAttr = nuevo_atributo.getTipo();
				}
				
				//Se obtiene el minOccurs y maxOccurs, así como si el atributo es null o not null
				if (p.getMinOccurs() == 1) {
					nuevo_atributo.setNulo(false);
				}
				nuevo_atributo.setMinOccurs(p.getMinOccurs());
				nuevo_atributo.setMaxOccurs(p.getMaxOccurs());
				
				
				//Verificamos si es un atributo compuesto 
				if(tipoAttr == null )
				{
					if  (!pterm.asElementDecl().getType().isSimpleType())
					{
						if (pterm.asElementDecl().getType().isComplexType())
						{
							XSComplexType atributo_compuesto = pterm.asElementDecl().getType().asComplexType();
							XSContentType contenido = atributo_compuesto.getContentType();
							XSParticle particle = contenido.asParticle(); // Se optienen los elementos dentro del complexType
				
							// Se verifica si los elementos tienen atributos con el tipo ATTRIBUTE y se agregan estos a su respectiva entidad
							leerAtributos2(atributo_compuesto,tipo);
				
							// Se verifica que el complexType sea diferente de nulo
							if (particle != null) 
							{
								XSTerm term = particle.getTerm();
								if (term.isModelGroup()) 
								{
									XSModelGroup xsModelGroup = term.asModelGroup();
									XSParticle[] particles1 = xsModelGroup.getChildren();
				
									// se verifica que sea sequence, all o choice
									System.out.println("Compositor "+ xsModelGroup.getCompositor().toString());
									
									// Se leen los atributos de las entidades
									atributos = leerElementos(particles1, tipo, atributos); //Llamada RECURSIVA
								}
							}
						}
					}
				}
				
				//Se termina de incluir los atributos, referencias o clave a la entidad correspondiente
				//OJO esta porción de código está incluyendo a la clave 2 veces, como atributo y como clave
				//Si colocas la línea entidad.setAtributo(nuevo_atributo); despues del if se evita esta situación
				if (tiposBasicos.contains(tipoAttr)){
					atributos.add(nuevo_atributo);
					
					//Se verifica si el atributo es clave y se coloca la clave en la entidad
					if ((tipoAttr.equals(id))){
						
						entidad.setClave(nuevo_atributo.getNombre());
					}
				}
				else if (tipoAttr!=null){
					entidad.setReferencia(nuevo_atributo);
				}
			}
		}
		return atributos;
	}



	/**
	 *  La funci&#243n leerElementos es la encargada de leer y almacenar en un hash de Entidades, 
	 *  a todas aquellas entidades definidas dentro del XMLSchema, (que no son más que todos los "element" 
	 *  tags definidos en el nivel más externo de anidamiento) 
	 * @param claves Iterador de String, en el que se almacenan los nombres de las Entidades (nombre del element)
	 * @param valores Iterador de XSElementDecl que contiene la informaci&#243n de todos los "element" 
	 * que representen a Entidades (que no son más que todos los "element" tags definidos en el nivel 
	 * más externo de anidamiento) 
	 */
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

	/**
	 *  La funci&#243n leerAtributosEntidades es la encargada de leer y almacenar por cada ComplexType que defina a una 
	 *  Entidad dentro del XMLSchema, todos sus atributos con sus restricciones. Esta funci&#243n se apoya de las funciones
	 *  auxiliares leerAtributos2 y leerElementos
	 * @param claves Iterador de String, en el que se almacenan los nombres de los ComplexTypes que pudieran definir Entidades.
	 * @param valores Iterador de XSComplexType que contiene la informaci&#243n de cada uno de los ComplexType definidos
	 * en el nivel m&#225s externo de anidamiento, que pudieran estar definiendo a Entidades.
	 */
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
				complex = (XSComplexType) valores.next();
				
				// Se verifica si los elementos tienen atributos con el tipo ATTRIBUTE y se agregan estos a su respectiva entidad
				leerAtributos2(complex,tipo);
				
				// Estamos en busqueda de examinar los atributos de tipo ELEMENT
				contenido = complex.getContentType();
				particle = contenido.asParticle(); // Se optienen los elementos dentro del complexType
	
				// Se verifica que el complexType sea diferente de nulo
				if (particle != null) {
					term = particle.getTerm();
					if (term.isModelGroup()) {
						xsModelGroup = term.asModelGroup();
						particles = xsModelGroup.getChildren();
	
						// se verifica que sea sequence, all o choice
						System.out.println("Compositor "+ xsModelGroup.getCompositor().toString());
						
						// Se leen los atributos de las entidades
						Vector<Atributo> atributos = entidades.get(tipo).getAtributos();
						atributos = leerElementos(particles,tipo,atributos);
						entidades.get(tipo).setAtributos(atributos);
					}
				}
			}
		}
	}
	
	/**
	 * El m&#233todo ImprimirEntidades es el encargado de desplegar por pantalla toda la informaci&#243n relevante 
	 * de cada Entidad (nombre, clave, atributos b&#225sicos y referencias a otras entidades)
	 */
	public static void ImprimirEntidades() {

		Set<String> tipos = entidades.keySet();
		Iterator<String> cadaTipo = tipos.iterator();

		while (cadaTipo.hasNext()) {
			Entidad entidad = entidades.get(cadaTipo.next());

			System.out.println("===== Entidad: " + entidad.getNombre_entidad()	+ " -- Tipo: " + entidad.getTipo() + " -- Clave: "+entidad.getClave()+ " =====\n");
			//System.out.println("-- Tipo:    " + entidad.getTipo() + " --"+ "Clave :"+entidad.getClave()+ " --");

			int j = entidad.getAtributos().size()-1;

			Vector<Atributo> atributos = entidad.getAtributos();
			System.out.println("-- Atributos básicos --");

			while (j >= 0) {
				System.out.println("	Nombre : " + atributos.get(j).getNombre());
				System.out.println("		Tipo : " + atributos.get(j).getTipo());
				System.out.println("		Nulo : " + atributos.get(j).isNulo());
				System.out.println("		Default : " + atributos.get(j).getValor());

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




	/**
	 * Parser de XMLSchema que almacena en determinadas estructuras de datos la informaci&#243n parseada, 
	 * con la finalidad de llevar a cabo una posterior traducci&#243n del XMLSchema al esquema Relacional de base 
	 * de datos
	 * @param args
	 * @throws SAXException
	 * @throws IOException
	 */
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
