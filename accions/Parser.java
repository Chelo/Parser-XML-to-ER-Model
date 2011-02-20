package accions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
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
import com.sun.xml.xsom.XSIdentityConstraint;
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
	public static HashMap<String, Entidad> entidades = new HashMap<String, Entidad>();
	public static HashMap<String, String> nombreEntidades = new HashMap<String, String>();
	
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
	
	public static Vector<String> separaRango(String rango){
		Vector<String> salida = new Vector<String>();
		StringTokenizer st = new StringTokenizer(rango, "|");
		
		while (st.hasMoreTokens()) {
			salida.add(st.nextToken());
		}
		
		return salida;
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
					atributo.setMaxRango(facet.getValue().value);					
				}
				if (facet.getName().equals(XSFacet.FACET_MININCLUSIVE)) {
					atributo.setMinRango(facet.getValue().value);
				}
				if (facet.getName().equals(XSFacet.FACET_MAXEXCLUSIVE)) {
					
					System.out.println("AVERTENCIA: este Maximo se tomará como inclusivo");
					atributo.setMaxRango(facet.getValue().value);
				}
				if (facet.getName().equals(XSFacet.FACET_MINEXCLUSIVE)) {
					System.out.println("AVERTENCIA: este Maximo se tomará como inclusivo");
					atributo.setMinRango(facet.getValue().value);
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
					Vector<String> dominio = new Vector<String>();
					dominio =separaRango(facet.getValue().value);
					if (dominio.size()>1){
						atributo.setDominio(separaRango(facet.getValue().value));
					}else{
						System.out.println("ADVERTENCIA: el campo "+facet.getValue().value+ "perteneciente a la restricción pattern value es inválido en el modelo ER");
					}
					
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
		HashMap<String,Atributo> clave = new HashMap<String,Atributo>();
		
		String id = "ID";
		Entidad entidad = entidades.get(tipoEntidad);
		
		for (XSAttributeUse attributeUse : complex.getAttributeUses()) {
		
			Atributo nuevo_atributo = new Atributo();

			decl = attributeUse.getDecl();
			nuevo_atributo.setNombre(decl.getName());
			nuevo_atributo.setTipo(decl.getType().getName());
			
			if ((decl.getType().getName().equals(id))){
				clave.put(nuevo_atributo.getNombre(),nuevo_atributo);
				entidad.setClave(clave);
			}
			nuevo_atributo.setNulo(!(attributeUse.isRequired()));
			atributos.add(nuevo_atributo);
		}

			
			entidad.setAtributos(atributos);

	}
	
	/**
	 * Crea una nueva entidad para el atributo que es multivaluado
	 * @param atributo al cual se le desea crear una nueva entidad
	 * @param entidad en la cual se encuentra el atributo multivaluado
	 */
	public static void Multivaluado(Atributo atributo, Entidad entidad){
		Entidad nueva = new Entidad();
		//suponiendo que la clave no es compuestas
		System.out.print("Que pasa\n");
		Atributo clave_entidad = entidad.getClave().get(0);
		System.out.print("Que pasa\n");
		HashMap<String,Atributo> clave = new HashMap<String,Atributo>();
		clave.put(clave_entidad.nombre,clave_entidad);
		
		
		clave_entidad.setTipo(entidad.getTipo());
		nueva.setNombre_entidad(atributo.nombre);
		nueva.setAtributo(atributo);
		nueva.setAtributo(clave.get(0));
		nueva.setReferencia(clave_entidad);//setforaneo
		nueva.setTipo(atributo.getNombre());
		clave.put(atributo.nombre,atributo);
		nueva.setClave(clave);
		
		entidades.put(atributo.getNombre(), nueva);
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
	 * @param compuesto es un boleano que indica si estamos leyendo un "element" correspondiente a un atributo compuesto. 
	 * @return arreglo unidimensional de Atributos, donde se almacenar&#225n el resto de los atributos 
	 * que est&#233n definidos bajo el tag "element", pertenecientes a la entidad definida por el ComplexType de nombre "tipo" 
	 * y que ser&#225n parseados por esta funci&#243n. 
	 */
	public static Vector<Atributo> leerElementos(XSParticle[] particles, String tipo, Vector<Atributo> atributos, boolean parteDecompuesto) {
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
		boolean esCompuesto = false;
		Entidad entidad = entidades.get(tipo); // Entidad en donde se encuentran estos elementos
		HashMap<String,Atributo> clave = new HashMap<String,Atributo>();

		
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
				//Verificar que pasa si el usuario no colocó nada
				if (parteDecompuesto)
				{
					nuevo_atributo.setNulo(false);
					nuevo_atributo.setMinOccurs(1);
					//El maxOccurs por defecto ya es 1, por eso no se asigna.
					//Si el usuario puso maxOccurs.. se ignora... no se permiten multivaluados 
					//Esto puede cambiar en el futuro!
				}
				else
				{	
					if (tipoAttr!=null)//Para no tener problemas con el equals
					{
						if(tipoAttr.equals(id))
						{
							if((p.getMinOccurs()!=1) || (p.getMaxOccurs()!=1))
							{
								System.out.println("ALERTA: Tanto el minOccurs como maxOccurs de la clave " +nombreAttr+ 
									    " de la entidad " +entidad.getNombre_entidad()+
										" deben ser 1 \n Se le colocará minOccurs = 1 y maxOccurs = 1 \n");
							}	
							nuevo_atributo.setNulo(false);
							nuevo_atributo.setMinOccurs(1);
							nuevo_atributo.setMaxOccurs(1);
						}	
						else
						{	
							//MinOccurs
							if (p.getMinOccurs() < 0){
								System.out.print("ALERTA: El minOccurs del atributo "+ nombreAttr + " de la entidad "+entidad.getNombre_entidad()+" debe ser mayor o igual a cero. Se colocará 0 por defecto \n");
								nuevo_atributo.setMinOccurs(0);
							}
							else
							{	
								if (p.getMinOccurs() == 1) {
									nuevo_atributo.setNulo(false);
								}
								nuevo_atributo.setMinOccurs(p.getMinOccurs());
							}
							//MaxOccurs
							//Caso unbounded
							if (p.getMaxOccurs() <= 0) {
								if(p.getMaxOccurs() == -1)
								{
									nuevo_atributo.setMaxOccurs(2);
								}
								else
								{	
									System.out.print("ALERTA: El maxOccurs del atributo "+ nombreAttr + " de la entidad "+entidad.getNombre_entidad()+" debe ser mayor que cero. Se colocará 1 por defecto \n");
									nuevo_atributo.setMaxOccurs(1);
								}	
							}
							else
							{	
								nuevo_atributo.setMaxOccurs(p.getMaxOccurs());
							}
						}
					}	
				}
				System.out.print("Lo q el usuario coloco:  "+ nombreAttr + " " +p.getMinOccurs()+ " "+  p.getMaxOccurs() +" \n");
				System.out.print("Asi quedo:  "+ nombreAttr + " " +nuevo_atributo.getMinOccurs()+ " "+nuevo_atributo.getMaxOccurs() +" \n");
				
				//Verificamos si es un atributo compuesto 
				if(tipoAttr == null )
				{
					if  (!pterm.asElementDecl().getType().isSimpleType())
					{
						if (pterm.asElementDecl().getType().isComplexType())
						{
							esCompuesto = true;
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
				
									// se verifica que sea all
									if (!xsModelGroup.getCompositor().toString().equals("all")){
										System.out.println("ALERTA: Los atributos compuestos deben estar definidos " +
												"entre el compositor <all> \n Se creará el atributo compuesto " +nombreAttr+ " de la entidad " +entidad.getNombre_entidad()+
												", sin embargo agregue el compositor <all> para evitar inconsistencias al " +
												"momento de cargar los datos. \n A cada uno de los atributos que constituyen a " +nombreAttr+ " " +
												"se le colocará minOccurs = 1 y maxOccurs = 1 \n");
										}
									else
									{
										System.out.println("ALERTA: A cada uno de los atributos que constituyen al atributo compuesto " +nombreAttr+ 
											    " de la entidad " +entidad.getNombre_entidad()+
												" se le colocará minOccurs = 1 y maxOccurs = 1 \n");
									}
									// Se leen los atributos de las entidades
									atributos = leerElementos(particles1, tipo, atributos,true); //Llamada RECURSIVA
								}
							}
						}
					}
				}
				
				//Se termina de incluir los atributos, referencias o clave a la entidad correspondiente
				//OJO esta porción de código está incluyendo a la clave 2 veces, como atributo y como clave
				//Si colocas la línea entidad.setAtributo(nuevo_atributo); despues del if se evita esta situación
				if (tiposBasicos.contains(tipoAttr)){
				
					if (nuevo_atributo.getMaxOccurs()>1){
						Multivaluado(nuevo_atributo,entidades.get(tipo));
					}else{
						atributos.add(nuevo_atributo);
					}
					
					
					//Se verifica si el atributo es clave y se coloca la clave en la entidad
					if ((tipoAttr.equals(id))){
						clave.put(nuevo_atributo.nombre,nuevo_atributo);
						entidad.setClave(clave);
					}
				}
				else if (tipoAttr!=null){
				 
					 if(entidades.containsKey(tipoAttr)) {
						
						 entidad.setReferencia(nuevo_atributo);
					 }
					 else{
						 if(tipoAttr.equals("anyType"))
						 {
							 System.out.println("ERROR: Debe definirle un tipo al atributo "+ nombreAttr +" de la entidad "+ entidad.getNombre_entidad()+ "\n " +
							 		"El atributo no será creado hasta que no realice los cambios\n");
						 }
						 else
						 { 	 
							 System.out.println("ERROR: El atributo "+ nombreAttr +" de la entidad "+ entidad.getNombre_entidad()+ " es de un tipo que no existe " + tipoAttr +
									 "\n El atributo no será creado hasta que no realice los cambios\n");
						 }	 
					 }

				}
				else {
					if(!esCompuesto)
						System.out.println("ERROR: Debe definirle un tipo al atributo "+ nombreAttr +" de la entidad "+ entidad.getNombre_entidad()+ "\n " +
				 		"El atributo no será creado hasta que no realice los cambios\n");	
				}
			}
		}
		return atributos;
	}
	
	public static void TagRestriccion(List<XSIdentityConstraint> constraint, Entidad entidad){
		System.out.println("CONSTRAINT ");
		
		int i = constraint.size()-1;
		while (i>=0){
			if (constraint.get(i).getCategory()== 0){
				System.out.println("restriccion de clave");
			}else if (constraint.get(i).getCategory()==2){
				System.out.println("restriccion unique");
			}else{
				System.out.println("restriccion no valida");
			}
			System.out.println("Name :"+constraint.get(i).getName());
			System.out.println("Categoria :"+constraint.get(i).getCategory());
			System.out.println("NameSpace :"+constraint.get(i).getTargetNamespace());
			System.out.println("Parent :"+constraint.get(i).getParent());
			System.out.println("Field :"+constraint.get(i).getFields().get(0).getXPath().value);
			System.out.println("Selector :"+constraint.get(i).getSelector().getXPath());
		
			i--;
		}
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
	public static void leerEntidades(Iterator<String> claves, Iterator<XSElementDecl> valores) {
		
		String tipo;
		
		while (claves.hasNext() && valores.hasNext()) {
			Entidad nueva_entidad = new Entidad();
			String nombre = (String) claves.next();
			XSElementDecl element = (XSElementDecl) valores.next();
			List<XSIdentityConstraint> restricciones = element.getIdentityConstraints();
			
			if (restricciones.size()>0){
				System.out.println("Tiene CONSTRAINT "+element.getIdentityConstraints().size());
				TagRestriccion(restricciones, nueva_entidad);
			}
			
			
			tipo = element.getType().getName();
			nueva_entidad.setTipo(tipo);
			nueva_entidad.setNombre_entidad(nombre);
			entidades.put(tipo, nueva_entidad);	
			nombreEntidades.put(nombre, tipo);
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
				System.out.print("ALERTA: El elemento del tipo "+ tipo +" no esta definido.\n " +
						"No se creará el tipo "+ tipo +", ni la Entidad hasta que no realice los cambios \n");
				valores.next();
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
						if(!xsModelGroup.getCompositor().toString().equals("sequence"))
						{
							//Ojo esto podría cambiar cuando se implementen las n-arias
							System.out.println("ALERTA: Los elements (atributos) definidos dentro del complexType <" +tipo+
									"> deben estar definidos entre el compositor <sequence> \n  Agregue el compositor <sequence> " +
									"para evitar inconsistencias al momento de cargar los datos.");	
						}		

						// Se leen los atributos de las entidades
						Vector<Atributo> atributos = entidades.get(tipo).getAtributos();
						atributos = leerElementos(particles,tipo,atributos,false);
						entidades.get(tipo).setAtributos(atributos);
					}
				}
			}
		}
	}
	
	/**
	 * Retorna un String indicado si el atributo puede o no se nulo.
	 * 
	 * @param atributo
	 * @return String que indica la nulidad del atributo
	 */
	public static String Nulidad(Atributo atributo){
		if (!atributo.isNulo()){			
			return "NOT NULL";
		}
		else return "NULL";
	}
	
	/**
	 * Retorna un String indicando el valor por defecto de un atributo en caso
	 * de que esté declarado
	 * 
	 * @param atributo
	 * @return String que indica default del atributo
	 */
	public static String ValorDefecto(Atributo atributo){
		if (atributo.getValor()==""){			
			return "";
		}
		else return "DEFAULT "+atributo.valorPorDefecto;
	}
	
	/**
	 * Retorna un String indicando la longitud del atributo en caso de que 
	 * este indicada en el atributo
	 * 
	 * @param atributo
	 * @return String con la longitud del atributo
	 */
	public static String Longitud(Atributo atributo){
		if (!(atributo.getLongitud()==null)){
			return "("+atributo.getLongitud()+")";
		} else return "";
	}
			
	/**
	 * Retorna el tipo equivalente en Oracle del atributo.
	 * 
	 * @param atributo
	 * @return String con el tipo del atributo
	 */
	public static String TipoDato(Atributo atributo){
		
		HashMap<String, String> tiposBasicos = new HashMap<String,String>();
		//Se colocan las equivalencias de tipo correspondientes con el manejador
		// de Oracle
		tiposBasicos.put("decimal","FLOAT");
		tiposBasicos.put("integer","NUMBER");
		tiposBasicos.put("ID","NUMBER");
		tiposBasicos.put("date","DATE");
		tiposBasicos.put("time","TIMESTAMP");
	
		if (tiposBasicos.containsKey(atributo.getTipo())){
			
			return tiposBasicos.get(atributo.getTipo())+Longitud(atributo);
		}else if(atributo.getTipo().equals("boolean")){
			
			return "CHAR(1)";
		}else
			
			return "VARCHAR"+Longitud(atributo);
		
	}
	
	/**
	 * Retorna un String con el dominio de un Atributo
	 * 
	 * @param valores Vector que contiene los valores que puede tomar un
	 * atributo
	 * @return String con el dominio del un atributo
	 */
	public static String DominioAtributo(Vector<String> valores){
		String str = "";
		int i = valores.size()-1;
		while (i>=0){
			str = str + "'"+valores.get(i)+"',";
			i--;
		}
		return str.substring(0, str.length()-1);
	}
	
	/**
	 * Dada una entidad se varifica que tenga clave primaria y se returna
	 * su valor
	 * @param entidad en la cual se desea búscar la clave.
	 * @return String que contiene la clave de la entidad.
	 */
	public static String retornaClave(Entidad entidad){
		HashMap<String,Atributo> clave = entidad.getClave();
		
		int i = clave.size();
		String salida = "(";
		
			if (i==0){
				return "(Clave no definida";
			}
			else if (i==1){
				return "("+clave.get(0).getNombre().toUpperCase();
			}else{
				i--;
				while (i>=0) {
					salida = salida+clave.get(i).getNombre()+",";
				i--;	
				}
				return salida.substring(0, salida.length()-1);
			}
		
	}
	
	/**
	 * Método que se encarga de crear el archivo sql correspondiente al xml 
	 * schema proporcionado.
	 * 
	 */
	public static void EscribirScript(){
		
		Set<String> tipos = entidades.keySet();
		Iterator<String> cadaTipo = tipos.iterator();
		Entidad entidad = new Entidad();
		Vector<Atributo> atributos = new Vector<Atributo>();
		Vector<Atributo> booleanos = new Vector<Atributo>();
		Vector<Atributo> dominios = new Vector<Atributo>();
		Vector<Atributo> rangos = new Vector<Atributo>();
		int k = 0,j = 0, l = 0;
		
		try{
		    //Se crea el archivo sql de salida.
		    FileWriter fstream = new FileWriter("out.sql");
		    BufferedWriter out = new BufferedWriter(fstream);
		    
			//Se iteran sobre las entidades que se van a crear.
			while (cadaTipo.hasNext()) {
				
				entidad = entidades.get(cadaTipo.next());
				// Se realiza un 'CREATE  TABLE' por cada entidad encontrada
				out.write("CREATE TABLE "+ entidad.getNombre_entidad().
				toUpperCase()+" (\n");
				
				
				j = entidad.getAtributos().size()-1;
				// se inicializan las variables para la nueva entidad
				atributos = entidad.getAtributos();
				booleanos = new Vector<Atributo>();
				dominios = new Vector<Atributo>();
				rangos = new Vector<Atributo>();
				
				//Se agregan los atributos basicos de la entidad.
				while (j >= 0) {
					out.write("	"+atributos.get(j).getNombre().toUpperCase()+
					" "+ TipoDato(atributos.get(j))+" "+ Nulidad(atributos.get(j))+
					" "+ValorDefecto(atributos.get(j))+" ,\n");
					
					//Se verifica el tipo del atributo y se agrega al vector
					//correspondiente
					if (atributos.get(j).getTipo().equals("boolean")){
						booleanos.add(atributos.get(j));
					}
					if (atributos.get(j).getDominio().size()>0){
						dominios.add(atributos.get(j));
					}
					if ( !(atributos.get(j).getMaxRango()=="-1") |
							!(atributos.get(j).getMinRango()=="-1") ){
						rangos.add(atributos.get(j));
					}
					j--;
				}
				
				//Se obtiene los atributos que son referencias
				
				
			
					Vector<Atributo> referencias= entidad.foraneo;
					j= referencias.size()-1;
					//Se agregan los atributos que hacen referencias en la entidad
					while (j >= 0) {
						out.write("	"+referencias.get(j).getNombre().toUpperCase()+
						"	"+ referencias.get(j).getTipo().toUpperCase() +"	"+
						Nulidad(referencias.get(j))+" ,\n");
						j--;
					
					
					j= referencias.size()-1;					
					//Se agregan los contraints de clave foranea a la entidad.
					while (j >= 0) {
						out.write("	FOREIGN KEY "+"("+referencias.get(j).getNombre().
						toUpperCase()+")"+" REFERENCES "+ "("+entidades.
						get(referencias.get(j).getTipo()).nombre_entidad.
						toUpperCase()+")"+" ,\n");
						j--;
					}
				}
				
				k = booleanos.size()-1;
				//Se agregan los contraint de atributo booleano
				while (k >= 0) {
					out.write("	CONTRAINT CHECK_BOOLEAN_"+booleanos.get(k).
					getNombre().toUpperCase()+ " CHECK (" +booleanos.get(k).
					getNombre().toUpperCase() + " IN ('0','1')),\n");
					k--;
				}
				
				l = dominios.size()-1;
				//Se agregan los contraint de dominio a la entidad.
				while (l >= 0) {
				
					out.write("	CONTRAINT CHECK_DOMINIO_"+dominios.get(l).
					getNombre().toUpperCase()+ " CHECK (" +dominios.get(l).
					getNombre().toUpperCase() + " IN ("+DominioAtributo(dominios.
					get(l).getDominio())+")),\n");
					l--;
				}
				
				l = rangos.size()-1;
				//Se agregan los contraint de rango a la entidad
				while (l >= 0) {
					
					if (!(rangos.get(l).getMaxRango().equals("-1")) &&
							!(rangos.get(l).getMinRango().equals("-1"))){
						
					out.write("	CONTRAINT CHECK_RANGO_"+rangos.get(l).
					getNombre().toUpperCase()+ " CHECK (" +rangos.get(l).
					getNombre().toUpperCase() + " BETWEEN "+rangos.get(l).
					getMinRango()+" AND "+rangos.get(l).getMaxRango()+ "),\n");
					
					}else if ((rangos.get(l).getMaxRango()=="-1") &&
							!(rangos.get(l).getMinRango()=="-1")){
		
						out.write("	CONTRAINT CHECK_RANGO_"+rangos.get(l).
						getNombre().toUpperCase()+ " CHECK (" +rangos.get(l).
						getNombre().toUpperCase() + " >= "+rangos.get(l).
						getMinRango()+ "),\n");
						
					}else{
						
						out.write("	CONTRAINT CHECK_RANGO_"+rangos.get(l).
						getNombre().toUpperCase()+ " CHECK (" +rangos.get(l).
						getNombre().toUpperCase() + " <= "+rangos.get(l).
						getMaxRango()+"),\n");
						
					}
					
					l--;
				}
				
				
				//Se agrega la clave primaria a la entidad
				out.write("	CONTRAINT PK_"+entidad.getNombre_entidad().
				toUpperCase()+ " PRIMARY KEY "+ retornaClave(entidad).
				toUpperCase()+");\n\n");
			}  
			
		    //Se cierra el output de escritura en el archivo sql
		    out.close();
		// Se toma la exception si existe
		}catch (Exception e){
		      System.err.println("Error escribiendo el script sql: " + e.getMessage());
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
				System.out.println("		Longitud : " + atributos.get(j).getLongitud());
				System.out.println("		MinOccurs : " + atributos.get(j).getMinOccurs());
				System.out.println("		MaxOccurs : " + atributos.get(j).getMaxOccurs());

				j--;
			}
			
			//Se obitiene un iterador que recorre los vectores que poseen entidades.
			Iterator<Vector<Atributo>> itera = entidad.referencias.values().iterator(); 
			
			System.out.println("-- Atributos Hechos por el usuario--");
			while(itera.hasNext()){
				Vector<Atributo> referencias = itera.next();//Vector a trabajar.

				j= referencias.size()-1;
				
				while (j >= 0) {
					System.out.println("	Nombre : " + referencias.get(j).getNombre());
					System.out.println("		Tipo : " + referencias.get(j).getTipo());
					System.out.println("		Nulo : " + referencias.get(j).isNulo());
					System.out.println("		MinOccurs : " + referencias.get(j).getMinOccurs());
					System.out.println("		MaxOccurs : " + referencias.get(j).getMaxOccurs());

					j--;
				}
				
			}
			
			Vector<Atributo> foraneos =entidad.foraneo;//Vector a trabajar.
			
			
				System.out.println("-- Atributos foraneos!--");
				
				j= foraneos.size()-1;
				
				while (j >= 0) {
					System.out.println("	Nombre : " + foraneos.get(j).getNombre());
					System.out.println("		Tipo : " + foraneos.get(j).getTipo());
					System.out.println("		Nulo : " + foraneos.get(j).isNulo());
					System.out.println("		MinOccurs : " + foraneos.get(j).getMinOccurs());
					System.out.println("		MaxOccurs : " + foraneos.get(j).getMaxOccurs());
	
					j--;
				}
				
				System.out.println("-----Atributos que forman la clave ---------");
				Iterator<String> iter= entidad.clave.keySet().iterator();
				while(iter.hasNext()){
					System.out.println("Nombre: " + iter.next());
				}
			}
	}

	/**
	 * Permite encontrar las relaciones entre las Entidades.
	 */
	public static void VerInterrelaciones(){
		/*
		 * CONVENCION: Cuando se deba crear una nueva tabla para una interrelación, se creará una ENTIDAD del nombre
		 * de uno de los atributos que relacionan a las entidades. El tipo de la entidad debe ser un nombre único, por el hash
		 * por ende colocaré el nombre DEL ATRIBUTO tambien, por ahora.
		 */
		// Recorrere el hash de las entidades para ir viendo el vector de referencias de cada una
		// y asi ir sacando las interrelaciones.
		
		/*
		 * Debo sacar las claves y copiarlas a un vector para asi recorrerlo y que no me genere error
		 * de concurrencias al insertar en entidades las nuevas interrelaciones.
		 */
		Set<String> claves = entidades.keySet();
		Iterator<String> itr= claves.iterator();
		Vector<String> vectTipos= new Vector<String>();
			
		
		while(itr.hasNext()){
			String types= new String(itr.next());
			vectTipos.add(types);
		}
		
		//Hash que controlará la visita de las entidades, para que nos e repitan.
		HashMap<String,Vector<String>> visitados= new HashMap<String, Vector<String>>();
		
		//Recorro el vector nuevo de tipos de entidades.
		Iterator<String> y= vectTipos.iterator();
		while(y.hasNext()){
			System.out.println("------------------Paso a la siguiente entidad-----------------------");
			
			String tipoEnt= y.next(); //Tipo de Ent.
			Entidad ent = entidades.get(tipoEnt); //Entidad a estudiar.
			
			System.out.println("Entidad a tratar "+ ent.nombre_entidad+"\n");
			//Recorro los tipos de las referencias y voy trabajando con los vectores.
			Iterator<String> iter = ent.referencias.keySet().iterator();
			
			visitados.put(tipoEnt, new Vector<String>());
			System.out.println("Meti a "+tipoEnt+" en el hash de visitados \n");
			// Agrego al tipo en el hash de visitados.
			
			while(iter.hasNext()){
				String tipoEnti = iter.next(); //Tipo a tratar de Enti.
				System.out.println("Busco si "+tipoEnti+ " esta en visitados y no soy yo mismo\n");
				if(visitados.containsKey(tipoEnti) && !tipoEnt.equals(tipoEnti)){
					System.out.println("si esta\nAhora busco si en su vector esta "+tipoEnt);
					if(!visitados.get(tipoEnti).contains(tipoEnt))
					{
						System.out.println("Los atributos de tipo "+ tipoEnti+" de la entidad"+
								entidades.get(tipoEnt).nombre_entidad+" no poseen referencia circular"+
								" con la entidad" + entidades.get(tipoEnti).nombre_entidad);
					}
				}
				else
				{
					
					visitados.get(tipoEnt).add(tipoEnti);//Lo agrego porq lo vi.
					System.out.println("Meti a "+ tipoEnti+" en el vector de "+ tipoEnt);
					Entidad enti= entidades.get(tipoEnti);  // Entidad relacionada con ent.
					System.out.println(ent.nombre_entidad+" se relaciona con "+ enti.nombre_entidad+ "\n");	
					
					if (enti.getNombre_entidad().equals(ent.getNombre_entidad())) {
						/*
						 * Es una entidad que se relaciona consigo misma, quedamos que si tiene (1,1) se absorbe.
						 * sino se crea otra entidad. 
						 */
						System.out.println("Es conmigo misma\n");
						Vector<Atributo> refself= ent.referencias.get(tipoEnt); //Atributos de si mismo.
						Iterator<Atributo> i= refself.iterator();
						
						while(i.hasNext()){
							//Para cada atributo a mi mismo, veo si me absorbo o si creo otra entidad.
							Atributo at = i.next();
							if (at.minOccurs + at.maxOccurs == 2) {
								//Se absorbe a si misma
								ent.AgregarForaneo(at);
								System.out.println("1:1, me absorvo\n");
							} 
							else 
							{
								//Se crea entidad.
								System.out.println("No es 1:1, debo crear otra entidad.\n");
								Entidad entidadNueva= new Entidad();
								
								//Introduzco la clave y coloco el atributo tambien como clave alterna.
								//Se debe insertar doble pues recordemos que se referencia a sí misma.
								HashMap<String,Atributo> clave = new HashMap<String,Atributo>();
								
								Iterator<Atributo> iterador= ent.clave.values().iterator();
								
								while(iterador.hasNext()){
									/*
									 * Debo clonar cada atributo y pasarlo al nuevo hash, para evitar paso por
									 * referencia
									 */
									Atributo unaClave= iterador.next();
									clave.put(new String(unaClave.nombre),(Atributo)unaClave.clone());
								}
								
								entidadNueva.setClave(clave);
								
								//Introduzco nombre de la entidad, que por ahora es el nombre del atributo.
								entidadNueva.nombre_entidad= at.nombre;
								 
								//Coloco tipo.
								entidadNueva.tipo= at.nombre; // POR AHORA
								
								//introduzco en el hash.
								entidades.put(entidadNueva.tipo, entidadNueva);
								System.out.println("Cree una nueva entidad llamada "+ entidadNueva.nombre_entidad+"y la introduje en el hash\n");
								

							}
						}
					}
					else
					{
						Vector<Atributo> vectEnt = enti.clona(tipoEnt); //Referencias de Enti del tipo Ent.
						Vector<Atributo> vectEnti = ent.clona(tipoEnti); //Referencias de Ent del tipo Enti.
						/*
						 * Estoy segura que el vector de Ent no es nulo porque de el fue que salio el tipo
						 * de Enti 
						 */
						
						if (vectEnt==null) {
							//VIENE LA PARTE DE KARINA.
							System.out.println("No hay referencia circular entre "+ent.nombre_entidad+" y "+enti.nombre_entidad+" parte de KArina");
							
						} else {
							if (vectEnt.size()+vectEnti.size()==2) {
								//Ambos vectores son de tamaño 1 por ende debo asumir que se relacionan.
								System.out.println(ent.nombre_entidad+" se relaciona bien con "+enti.nombre_entidad+"\n");
								DefInterrelacion(vectEnt.get(0), vectEnti.get(0));
							} 
							else 
							{
								//Recorro un vector en otro buscando parejas por mismo nombre.
								Iterator<Atributo> j=vectEnti.iterator();
								boolean encontro=false;
								while(j.hasNext()){
									Atributo c= j.next();
									String nombre= c.nombre;
									Iterator<Atributo> k= vectEnt.iterator();
									while(k.hasNext()){
										Atributo b= k.next();
										if (b.nombre.equals(nombre)) {
											//Encontre la pareja.
											//Saco a k del vector y los mando a Def.
											vectEnt.remove(b);
											encontro=true;
											System.out.println(ent.nombre_entidad+" se relaciona con "+enti.nombre_entidad+" a través del atributo "+ b.nombre+"\n");
											DefInterrelacion(b,c);
											break;
										}
									}
									if (!encontro) {
										//no hay referencia circular.
										System.out.println("No hay ref circular para el atributo "+ c.nombre+" de la entidad "+ ent.nombre_entidad+ "puede que sea una generalizacion\n");
										//Karina.
									}
								}
								if (vectEnt.size()!=0) {
									Iterator<Atributo> t= vectEnt.iterator();
									while(t.hasNext()){
										System.out.println("No hay ref circular para el atributo "+ t.next().nombre+" de la entidad "+enti.nombre_entidad+" puede que sea una generalizacion");
									}
									//Quiere decir que aun quedaron atributos que no tenian pareja.
									//Karina.
								}
							}
						}
					}
				}
				
			}
			
		}
	}

	/**
	 * Analiza cual entidad absorbe a cual, de acuerdo a las reglas de la cardinalidad y 
	 * participacion. (1:1,1:N,M:N)
	 * 
	 * @param atr1 Atributo que referencia a una dconditione las entidades.
	 * @param atr2 Atributo que referencia a la otra entidad relacionada.
	 */
	public static void DefInterrelacion(Atributo atr1, Atributo atr2){
		int min1= atr1.minOccurs;
		int max1= atr1.maxOccurs;
		int min2= atr2.minOccurs;
		int max2= atr2.maxOccurs;
		if (min1+max1==2) {
			//atr1 es 1:1
			//Entidad del tipo atr2 absorbe a Entidad del atributo tipo atr1
			System.out.println("el atributo "+ atr1.nombre+" tiene min y max 1:1, " +
					"por ende "+ entidades.get(atr2.tipo).nombre_entidad +" absorbe a "+
					entidades.get(atr1.tipo).nombre_entidad+"\n");
			entidades.get(atr2.tipo).AgregarForaneo(atr1);//Entidad que absorbe.
		} 
		else if (min2+max2==2)
		{
			//atr2 es 1:1
			//Entidad del tipo atr1 absorbe a Entidad del atributo tipo atr2
			System.out.println("el atributo "+ atr2.nombre+" tiene min y max 1:1, " +
					"por ende "+ entidades.get(atr1.tipo).nombre_entidad +" absorbe a "+
					entidades.get(atr2.tipo).nombre_entidad+"\n");
			entidades.get(atr1.tipo).AgregarForaneo(atr2);//Entidad que absorbe.
		}
		else if(min1==0 && max1==1)
		{
			if (min2==0 && max2==1) {
				//atr1 es 0:1 y atr2 es 0:1
				System.out.println("Ambos atributos ("+atr1.nombre+ " , "+ atr2.nombre+") tienen 0:1"+
						"por ende se crea una nueva entidad con uno de los dos como clave.");
				//se crea una nueva entidad, donde uno de los dos sera clave y el otro ser alterno.
				Entidad entNueva= CrearEntidadNueva(atr1);
				//Coloco el otro atributo como un atributo foraneo.
				entNueva.AgregarForaneo(atr2);
				entidades.put(entNueva.tipo, entNueva);
				System.out.println("Cree la entidad de nombre "+ entNueva.nombre_entidad+" y la meti en el hash\n");
				
			} else {
				// atr1 es 0:1 y atr2 es 0:N o 1:N
				System.out.println("El atributo "+ atr1.nombre+" tiene 0:1, pero el atributo "+ atr2.nombre+
						"tiene 0:N o 1:N\n");
				// Se crea una nueva entidad donde atr1 sea clave y lo otro sea atributo normal.
				Entidad nueva= CrearEntidadNueva(atr1);
				//Coloco atr2 como foraneo.
				nueva.AgregarForaneo(atr2);
				entidades.put(nueva.tipo, nueva);
				System.out.println("Cree la entidad de nombre "+ nueva.nombre_entidad+" y la meti en el hash\n");
			}
		}
		else if(min2==0 && max2==1){
			//atr2 es 0:1, y atr1 ajuro debe ser 0:N o 1:N
			System.out.println("El atributo "+atr2.nombre+" es 0:1 pero el atributo "+ atr1.nombre+ " es 0:N o 1:N"+
					"por ende se debe crear una nueva entidad cuya clave sea la de la entidad "+ entidades.get(atr2.tipo).nombre_entidad);
			//Se crea una nueva entidad donde atr2 sea clave y lo otro sea atributo normal.
			Entidad nueva=CrearEntidadNueva(atr2);
			
			//coloco a atr1 como foraneo.
			nueva.AgregarForaneo(atr1);
			entidades.put(nueva.tipo, nueva);
			System.out.println("Cree la entidad de nombre "+ nueva.nombre_entidad+" y la agregue al hash\n");
		}
		else {
			System.out.println("Caso M:N Se crea una nueva entidad con clave de ambas entidades\n");
			//M:N
			// Se crea una nueva entidad con clave siendo la compuesta de los dos atr.
			Entidad nueva= CrearEntidadNueva(atr1);
			System.out.println("cree la entidad... ahora metere la otra parte de la clave");
			Iterator<Atributo> h= entidades.get(atr2.tipo).clave.values().iterator();
			while(h.hasNext()){
				//Clone cada atributo de la clave de la entidad de atr2 y la meti en el vector clave de la entidad nueva.
				Atributo atr= h.next();
				nueva.clave.put(new String(atr.nombre),(Atributo)atr.clone());
				System.out.println("Meti la clave "+atr.nombre);
			}
			
			entidades.put(nueva.tipo, nueva);
			System.out.println("Cree la entidad "+ nueva.nombre_entidad+" y la meti en el hash");
		}
	}
	
	/**
	 * Crea una nueva entidad que en realidad es ucna nueva tabla que permite representar una interrelacion.
	 * Le coloca el nombre y el tipo segun el atributo dado y le pasa la clave de la entidad del atrobuto dado.
	 * 
	 * @param atr1 Atributo con datos para la nueva entidad
	 * @return Entidad formada.
	 */
	public static Entidad CrearEntidadNueva(Atributo atr1){
		Entidad entNueva= new Entidad();
		Entidad entidadAtr1= entidades.get(atr1.tipo);// Entidad de Atributo1
		//Coloco el nombre de la entidad
		entNueva.nombre_entidad= atr1.nombre;
		//Coloco el tipo de la entidad.
		entNueva.tipo=atr1.nombre;
		//Debo copiar y clonar el hash de clave para evitar paso por referencia.
		Iterator<Atributo> g= entidadAtr1.clave.values().iterator();
		while(g.hasNext()){
			Atributo atr= g.next();
			entNueva.clave.put(new String(atr.nombre),(Atributo)atr.clone());
			System.out.println("meti la clave "+atr.nombre);
		}
		return entNueva;
	}
	

	/**
	 * Parser de XMLSchema que almacena en determinadas estructuras de datos la informaci&#243n parseada, 
	 * con la finalidad de llevar a cabo una posterior traducci&#243n del XMLSchema al esquema Relacional de base 
	 * de datos
	 * @param args
	 * @throws SAXException
	 * @throws IOException
	 */
	 public static void ParsearXMLSchema(String archivo) throws SAXException, IOException {

		File file = new File(archivo);
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
												 
				// ComplexTypes (al menos los del nivel más externo
				Map<String, XSComplexType> mapa = (Map<String, XSComplexType>) schema.getComplexTypes();
				System.out.print("Tamano: " + ((Map<String, XSComplexType>) mapa).size() + "\n");
				Iterator<String> claves1 = ((Map<String, XSComplexType>) mapa).keySet().iterator(); 
				// Se obtienentodos los complexTypes Iterator<XSComplexType>
				Iterator<XSComplexType>valores1 =((Map<String, XSComplexType>) mapa).values().iterator();
				LeerAtributosEntidades(claves1, valores1);
				
				VerInterrelaciones();
				ImprimirEntidades();
			
			}
		} catch (Exception exp) {
			System.out.println("Error en la formacion del xml Schema\n");
			exp.printStackTrace(System.out);
		}
		EscribirScript();
	}
	
}
