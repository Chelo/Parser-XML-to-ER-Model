package accions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
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
	public static HashMap<String, XSComplexType> complexSinEntidad = new HashMap<String, XSComplexType>() ;
	public static HashMap<String, XSComplexType> subclases = new HashMap<String, XSComplexType>() ;
	public static HashMap<String, Vector<String>> multivaluados = new HashMap<String, Vector<String>>();  
	public static HashMap<String, Vector<String>> multivaluadosCompuestos = new HashMap<String, Vector<String>>(); 
	public static HashMap<String, Vector<String>> superclases = new HashMap<String, Vector<String>>() ;
	public static HashMap<String, XSComplexType> enearias = new HashMap<String, XSComplexType>(); //Hash cuya clave es el nombre de la entidad enearia y el valor es el complextype que tiene.
	public static int opcionTraduccion;
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
					ReportarError("**ERROR**\n	El campo "+facet.getValue().value+ "perteneciente a la restricción minLength es inválido en el modelo ER.\n");
				}
				if (facet.getName().equals(XSFacet.FACET_PATTERN)) {
					Vector<String> dominio = new Vector<String>();
					dominio =separaRango(facet.getValue().value);
					if (dominio.size()>1){
						atributo.setDominio(separaRango(facet.getValue().value));
					}else{
						ReportarError("**ERROR**\n	El campo "+facet.getValue().value+ "perteneciente a la restricción pattern value es inválido en el modelo ER.\n");
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


		String id = "ID";
		Entidad entidad = entidades.get(tipoEntidad);
		HashMap<String,Atributo> clave = entidad.getClave();
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
	 * Metodo encargado de colocar la referencia hacia la clave primaria de una entidad
	 * que posee un atributo multivaluado y compuesto
	 * @param entidad Entidad donde se encuentra el atributo multivaluado y compuesto
	 * @param nueva Entidad nueva que representa el atributo multivaluado y compuesto.
	 */
	public static void SetForaneoMultivaluado(Entidad entidad, Entidad nueva){
		@SuppressWarnings("unchecked")
		HashMap<String,Atributo> clave_entidad = (HashMap<String, Atributo>) entidad.getClave().clone();
		Vector<Atributo> nuevos_atributos2 = new Vector<Atributo>();
		Atributo aux = new Atributo();
		HashMap<String, Vector<Vector<Atributo>>> foraneo = new HashMap<String,Vector<Vector<Atributo>>>();
		Vector<Vector<Atributo>> agregar_foraneo =  new Vector<Vector<Atributo>>();
		String nombre = "";

		//Se leen los atributos que forman parte de la clave de la entidad inicial
		Iterator<String> iter = clave_entidad.keySet().iterator();
		while (iter.hasNext()){
			
			nombre = iter.next();
			aux= (Atributo) clave_entidad.get(nombre).clone();
			aux.setNulo(false);
			nuevos_atributos2.add(aux);
		}
		
		Collections.reverse(nuevos_atributos2);
		agregar_foraneo.add(nuevos_atributos2 );
		foraneo.put(entidad.tipo,agregar_foraneo);
		
		//Se agregan los atributos que son referencia
		nueva.setForaneo(foraneo);
		
	}
	
	
	/**
	 * Crea una nueva entidad para el atributo que es multivaluado
	 * @param atributo al cual se le desea crear una nueva entidad
	 * @param entidad en la cual se encuentra el atributo multivaluado
	 */
	public static void Multivaluado(Atributo atributo, Entidad entidad){

		Entidad nueva = new Entidad();
		defineClave(entidad);
		@SuppressWarnings("unchecked")
		HashMap<String,Atributo> clave_entidad = (HashMap<String, Atributo>) entidad.getClave().clone();
		Vector<Atributo> nuevos_atributos2 = new Vector<Atributo>();
		Atributo aux = new Atributo();
		HashMap<String, Vector<Vector<Atributo>>> foraneo = new HashMap<String,Vector<Vector<Atributo>>>();
		Vector<Vector<Atributo>> agregar_foraneo = new Vector<Vector<Atributo>>();
		String nombre = "";
		
		if (clave_entidad.keySet().size()==0){
			ReportarError("**ERROR**\n	Los atributos compuestos deben definirse después de la clave en el Schema XML.\n");
			
		}
		Iterator<String> iter = clave_entidad.keySet().iterator();
		while (iter.hasNext()){
			nombre = iter.next();
			aux= (Atributo) clave_entidad.get(nombre).clone();
			aux.setNulo(false);
			nuevos_atributos2.add(aux);
		}
		
		Collections.reverse(nuevos_atributos2);
		agregar_foraneo.add(nuevos_atributos2 );
		foraneo.put(entidad.tipo,agregar_foraneo);
		nueva.setNombre_entidad(atributo.nombre);
		
		atributo.setNulo(false);
		
		
		nueva.setForaneo(foraneo);
		nueva.setTipo(atributo.getNombre());
		clave_entidad.put(atributo.nombre,atributo);
		nueva.setClave(clave_entidad);
		nueva.setAtributos(new Vector<Atributo>());
		nueva.imprimir = false;
		entidades.put(atributo.getNombre(), nueva);
	}
	
	/**
	 * Metodo encarga de crear una nueva entiadad en caso de encontrar un atributo que sea
	 * multivaluado y compuesto.
	 * 
	 * @param nuevo_atributo Atributo compuesto multiuvaluado
	 * @param entidad_multivaluada Entidad nueva que se esta creando a partir del atributo compuesto multivaluada
	 * @param vector_multivaluado	Vector de atributos de la nueva entidad
	 * @param particles
	 */
	public static void CompuestoMultivaluado(Atributo nuevo_atributo, Entidad entidad_multivaluada, Vector<Atributo> vector_multivaluado, XSParticle[] particles ){
		Vector<Atributo> atributos = new Vector<Atributo>();
		nuevo_atributo.setNulo(  false);
		
		if(nuevo_atributo.getMinOccurs()==0)
		{
			atributos = leerElementos(particles, entidad_multivaluada.tipo, vector_multivaluado,true,true); //Llamada RECURSIVA
		}
		else
		{
			atributos = leerElementos(particles,entidad_multivaluada.tipo, vector_multivaluado,true,false); //Llamada RECURSIVA
		}

		entidad_multivaluada.setAtributos(new Vector<Atributo>());
		HashMap<String,Atributo> clave = entidad_multivaluada.getClave();
		
		int j = atributos.size()-1;
		while (j>=0){
			atributos.get(j).setNulo(false);
				clave.put(atributos.get(j).nombre,atributos.get(j));
			j--;	
		}	
		entidad_multivaluada.imprimir = false;
		entidad_multivaluada.setClave(clave);
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
	 * @param parteDecompuesto es un boleano que indica si estamos leyendo un "element" correspondiente a un atributo compuesto.
	 * @param compuestoNulo es un boleano que indica si estamos leyendo un "element" correspondiente a un atributo compuesto cuyo padre es nulo.
	 * @return arreglo unidimensional de Atributos, donde se almacenar&#225n el resto de los atributos
	 * que est&#233n definidos bajo el tag "element", pertenecientes a la entidad definida por el ComplexType de nombre "tipo"
	 * y que ser&#225n parseados por esta funci&#243n.
	 */
	@SuppressWarnings("unchecked")
	public static Vector<Atributo> leerElementos(XSParticle[] particles, String tipo, Vector<Atributo> atributos, boolean parteDecompuesto, boolean compuestoNulo) {

		XSTerm pterm;
		String nombreAttr;
		String tipoAttr=null;

		for (XSParticle p : particles)
		{
			pterm = p.getTerm();

			//Caso de generalización / especialización
			if (pterm.isModelGroup())
			{
				XSModelGroup xsModelGroup2 = pterm.asModelGroup();
				XSParticle [] particles2 = xsModelGroup2.getChildren();
				Vector <String> subclasesValidas = new Vector <String>();

				String compositor = xsModelGroup2.getCompositor().toString();

				// se verifica que sea sequence o choice
				if(compositor.equals(XSModelGroup.Compositor.ALL.toString()))
				{
					ReportarError("**ERROR**\n	Los elements (atributos) definidos dentro del complexType <" +tipo+
							"> que se refieren a una generalización / especialización deben estar definidos entre el " +
							"compositor <sequence> (solapado), <choice> (disjunto) \n+" +
							"	Cambie el compositor o de lo contrario no se creará la generalización /especialización. \n");
				}
				else
				{
					//Ahora leo cada una de las referencias a subclases
					for (XSParticle p1 : particles2)
					{
						XSTerm pterm1 = p1.getTerm();
						if (pterm1.isElementDecl())
						{
							// Se obtiene el nombre del atributo
							nombreAttr = pterm1.asElementDecl().getName();
							// Se obtiene el tipo del atributo
							tipoAttr = pterm1.asElementDecl().getType().getName();
							//System.out.print("Tipo: "+ tipoAttr+"\n");

							if(complexSinEntidad.containsKey(tipoAttr))
							{
								//System.out.print("Si soy complexSinEntidad "+ tipoAttr +"\n");
								XSComplexType complex = (XSComplexType) complexSinEntidad.get(tipoAttr);
								subclasesValidas.add(tipoAttr);
								subclases.put(tipoAttr, complex);
								XSParticle [] particlesSubclase = complex.getContentType().asParticle().getTerm().asModelGroup().getChildren();

								//Se crea una entidad por cada subclase
								Entidad nueva_subclase = new Entidad();
								nueva_subclase.setNombre_entidad(nombreAttr);
								nueva_subclase.setTipo(tipoAttr);
								nueva_subclase.imprimir =false;
								entidades.put(tipoAttr, nueva_subclase);
								Vector<Atributo> atributosSubclase = nueva_subclase.getAtributos();
								leerElementos(particlesSubclase, tipoAttr, atributosSubclase, false,false);
								nueva_subclase.setAtributos(atributosSubclase);
								

							}
							else
							{
								ReportarError("**ERROR**\n	Los tipos de las subclases de una generalización / especialización deben" +
										" corresponder con el nombre de algún complexType al cual no se le haya definido <element> \n" +
										" Este no es el caso de la subclase "+ nombreAttr+ " de tipo " + tipoAttr + "\n No se incluirá a esta subclase" +
										" dentro de la generalización / especialización de la superclase "+entidades.get(tipo).getNombre_entidad()+
								" hasta que realice los cambios.\n");
							}
						}
					}
					if(!subclasesValidas.isEmpty())
					{
						//Una vez leídas todas las subclases se debe crear una relación entre estas
						//y su superclase, para que luego una vez que estemos seguros que se han leído todos los
						//atributos de la superclase, se termine de realizar la traducción,
						//dependiendo de la opción que escoja el usuario
						superclases.put(tipo, subclasesValidas);
						entidades.get(tipo).imprimir = false;
					}
					else
					{
						ReportarError("*ALERTA*\n	No se creará la generalización / especialización de la entidad "+
								entidades.get(tipo).getNombre_entidad() + " pues ninguna subclase está bien definida.\n");
					}
				}
			}

			// Caso atributo normal
			if (pterm.isElementDecl())
			{
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
				//tiposBasicos.add("ID");

				XSRestrictionSimpleType restriction;
				String valorPorDefecto = "";
				boolean esCompuesto = false;
				Entidad entidad = entidades.get(tipo); // Entidad en donde se encuentran estos elementos
				HashMap<String,Atributo> clave = new HashMap<String,Atributo>();
				Atributo nuevo_atributo = new Atributo();

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
					System.out.println("VALOR POR DEFECTO :"+valorPorDefecto);
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
					//MinOccurs
					if(compuestoNulo)
					{
						nuevo_atributo.setMinOccurs(0);
						if(p.getMinOccurs()!=0)
							ReportarError("*ALERTA*\n	El atributo "+ nombreAttr + " de la entidad "
									+entidad.getNombre_entidad()+" forma parte de un " +
							"atributo compuesto Nulo. Se le asignará un minOccurs igual a cero.\n");

					}
					else
					{
						if ((!(p.getMinOccurs()==0)) && (!(p.getMinOccurs()==1)))
						{
							ReportarError("*ALERTA*\n	El minOccurs del atributo "+ nombreAttr + " de la entidad "
									+entidad.getNombre_entidad()+" que forma parte de un " +
							"atributo compuesto debe ser igual a cero o uno. Se colocará 1 por defecto.\n");
							nuevo_atributo.setNulo(false);
							nuevo_atributo.setMinOccurs(1);
						}
						else
						{
							if(p.getMinOccurs()==1)
								nuevo_atributo.setNulo(false);
							nuevo_atributo.setMinOccurs(p.getMinOccurs());
						}
					}
					//MaxOccurs
					if(p.getMaxOccurs()!=1)
					{
						ReportarError("*ALERTA*\n	El maxOccurs del atributo "+ nombreAttr + " de la entidad "
								+entidad.getNombre_entidad()+" que forma parte de un " +
						"atributo compuesto debe ser igual a uno. Se colocará 1 por defecto. \n");
						ReportarError("*ALERTA*\n	No se permiten hojas multivaluadas en los atributos compuestos. \n");
						//El maxOccurs por defecto ya es 1 (al hacer new Atributo), por eso no se asigna.
					}
				}
				else
				{
					if ((tipoAttr!=null) && (tipoAttr.equals(id)))//Para no tener problemas con el equals
					{
						if((p.getMinOccurs()!=1) || (p.getMaxOccurs()!=1))
						{
							ReportarError("*ALERTA*\n	Tanto el minOccurs como maxOccurs de la clave " +nombreAttr+
									" de la entidad " +entidad.getNombre_entidad()+
							" deben ser 1 .\n	Se le colocará minOccurs = 1 y maxOccurs = 1.\n");
						}
						nuevo_atributo.setNulo(false);
						nuevo_atributo.setMinOccurs(1);
						nuevo_atributo.setMaxOccurs(1);
					}
					else
					{
						//MinOccurs
						if (p.getMinOccurs() < 0){
							ReportarError("*ALERTA*\n	El minOccurs del atributo "+ nombreAttr + " de la entidad "+entidad.getNombre_entidad()+" debe ser mayor o igual a cero. Se colocará 1 por defecto.\n");
							nuevo_atributo.setMinOccurs(1);
							nuevo_atributo.setNulo(false);
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
							if(p.getMaxOccurs() == XSParticle.UNBOUNDED)
							{
								nuevo_atributo.setMaxOccurs(2);
							}
							else
							{
								ReportarError("*ALERTA*\n	El maxOccurs del atributo "+ nombreAttr + " de la entidad "+entidad.getNombre_entidad()+" debe ser mayor que cero. Se colocará 1 por defecto.\n");
								nuevo_atributo.setMaxOccurs(1);
							}
						}
						else
						{
							nuevo_atributo.setMaxOccurs(p.getMaxOccurs());
						}
						if(nuevo_atributo.getMinOccurs()>nuevo_atributo.getMaxOccurs())
						{
							ReportarError("*ALERTA*\n	El minOccurs del atributo "+ nombreAttr + " de la entidad "+entidad.getNombre_entidad()+
									" es mayor que el maxOccurs.\n	El minOccurs siempre debe ser menor que el maxOccurs. \n" +
							"	Se colocará minOccurs = 0 y maxOccurs = 1 por defecto.\n");
							nuevo_atributo.setMinOccurs(0);
							nuevo_atributo.setMaxOccurs(1);
						}
					}
				}
				System.out.print("Lo q el usuario coloco: "+ nombreAttr + " " +p.getMinOccurs()+ " "+ p.getMaxOccurs() +" \n");
				System.out.print("Asi quedo: "+ nombreAttr + " " +nuevo_atributo.getMinOccurs()+ " "+nuevo_atributo.getMaxOccurs() +" \n");

				//Verificamos si es un atributo compuesto
				if(tipoAttr == null )
				{
					if (!pterm.asElementDecl().getType().isSimpleType())
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
									if (!xsModelGroup.getCompositor().toString().equals("all"))
									{
										ReportarError("*ALERTA*\n	Los atributos compuestos deben estar definidos " +
												"entre el compositor <all> .\n	Se creará el atributo compuesto " +nombreAttr+ " de la entidad " +entidad.getNombre_entidad()+
												", sin embargo agregue el compositor <all> para evitar inconsistencias al " +
										"momento de cargar los datos.\n");
									}
									// Se leen los atributos de las entidades
									//Caso del compuesto multivaluado, debo crear una nueva entidad, con todos los
									//atributos que son parte del compuesto + una referencia a la clave de la entidad (tipo)
							
									if(nuevo_atributo.getMinOccurs()>1 || nuevo_atributo.getMaxOccurs()>1 ) //|| nuevo_atributo.getMinOccurs()>1 Este caso te está faltando
									{
										//Aqui tienes que hacer tu parte Lili
										System.out.println("Compuesto Multivaluado\n");
								
										//Se crea la nueva entidad conrespondiente al atributo compuesto multivaluado.
										Entidad entidad_multivaluada = new Entidad();
										entidad_multivaluada.nombre_entidad = nuevo_atributo.getNombre();
										entidad_multivaluada.tipo = nuevo_atributo.getNombre();
										Vector<Atributo> vector_multivaluado = new Vector<Atributo>();
										@SuppressWarnings("unused")
										HashMap<String,Atributo> multivaluada_clave = new HashMap<String,Atributo>();
										
										entidades.put(entidad_multivaluada.tipo, entidad_multivaluada);	
										entidad_multivaluada.setClave((HashMap<String, Atributo>) entidad.clave.clone());
										//Se crean las referencias foraneas de la entidad
										SetForaneoMultivaluado(entidad, entidad_multivaluada);
										//Se agregan los atributos a la nueva entidad
										CompuestoMultivaluado(nuevo_atributo,entidad_multivaluada,vector_multivaluado,particles1);	
										
										//Debemos almacenar información de los atributos que son multivaluados, 
										//pues si llegase de dejar de existir la superclase que los contiene
										//(caso de generalizacion/especializacion traduccion 2) habría que cambiar las 
										//foráneas de dichos multivaluados hacia la superclase, hacia las subclases.
										Vector<String> att_multivaluados;
										if (multivaluadosCompuestos.containsKey(tipo))
											att_multivaluados = multivaluadosCompuestos.get(tipo);
										else
											att_multivaluados = new Vector<String>();
										
										att_multivaluados.add(entidad_multivaluada.tipo);
										multivaluadosCompuestos.put(tipo, att_multivaluados);
										
										
									}
									else
									{
										//Caso del compuesto nulo, entonces debo obligar a todos los hijos a ser nulos
										if(nuevo_atributo.getMinOccurs()==0)
										{
											atributos = leerElementos(particles1, tipo, atributos,true,true); //Llamada RECURSIVA
										}
										else
										{
											atributos = leerElementos(particles1, tipo, atributos,true,false); //Llamada RECURSIVA
										}
									}
								}
							}
						}
					}
				}

				//Se termina de incluir los atributos, referencias o clave a la entidad correspondiente
				//OJO esta porción de código está incluyendo a la clave 2 veces, como atributo y como clave
				//Si colocas la línea entidad.setAtributo(nuevo_atributo); despues del if se evita esta situación
				//Se verifica si el atributo es clave y se coloca la clave en la entidad
				
				
				if (tiposBasicos.contains(tipoAttr)){
					if ( nuevo_atributo.getMinOccurs()>1 | nuevo_atributo.getMaxOccurs()>1 ){
						Multivaluado(nuevo_atributo,entidades.get(tipo));
						
						//Debemos almacenar información de los atributos que son multivaluados, 
						//pues si llegase de dejar de existir la superclase que los contiene
						//(caso de generalizacion/especializacion traduccion 2) habría que cambiar las 
						//foráneas de dichos multivaluados hacia la superclase, hacia las subclases.
						Vector<String> att_multivaluados;
						if (multivaluados.containsKey(tipo))
							att_multivaluados = multivaluados.get(tipo);
						else
							att_multivaluados = new Vector<String>();
						
						att_multivaluados.add(nuevo_atributo.nombre);
						multivaluados.put(tipo, att_multivaluados);
					}
					else{
						atributos.add(nuevo_atributo);
					}
				}
				else if (tipoAttr!=null){
					
					if ((tipoAttr.equals(id))){
						clave = entidad.getClave();
						clave.put(nuevo_atributo.nombre,nuevo_atributo);
						entidad.setClave(clave);
					}
					//Agregué esto pues una entidad puede relacionarse
					//con una subclase (complex sin entidad definida)
					if(entidades.containsKey(tipoAttr) || complexSinEntidad.containsKey(tipoAttr)) {

						entidad.setReferencia(nuevo_atributo);

					}
					else{
						if(tipoAttr.equals("anyType"))
						{
							ReportarError("**ERROR**\n	Debe definirle un tipo al atributo "+ nombreAttr +" de la entidad "+ entidad.getNombre_entidad()+ ".\n	" +
									"El atributo no será creado hasta que no realice los cambios.\n");
						}
						else
						{
							if(!tipoAttr.equals("ID"))
							ReportarError("**ERROR**\n	El atributo "+ nombreAttr +" de la entidad "+ entidad.getNombre_entidad()+ " es de un tipo que no existe " + tipoAttr +
							".\n	El atributo no será creado hasta que no realice los cambios\n");
						}
					}

				}
				else {
					if(!esCompuesto)
						ReportarError("**ERROR**\n	Debe definirle un tipo al atributo "+ nombreAttr +" de la entidad "+ entidad.getNombre_entidad()+ ".\n	" +
						"El atributo no será creado hasta que no realice los cambios.\n");
				}
			}
		}
		return atributos;
	}

	/**
	 * Permite identificar las restricciones de tipo <tag> que posee la entidad si las tiene definidas
	 * @param constraint lista con todos los contraints definidos en la entidad
	 * @param entidad en que se definen los contraints
	 */
	public static void TagRestriccion(List<XSIdentityConstraint> constraint, Entidad entidad){
		HashMap<String,Atributo> clave = entidad.getClave();
		HashMap<String,Atributo> unico = entidad.getUnico();
		int i = constraint.size()-1;

		while (i>=0){
			//Se verifica si existe restricciones del tipo <key>
			if (constraint.get(i).getCategory()== 0){
				//se verifica que el atributo este definido
				if (constraint.get(i).getSelector().getXPath().toString().toUpperCase().equalsIgnoreCase(entidad.tipo)){
					clave.put(constraint.get(i).getFields().get(0).getXPath().value,null);
				}
				else ReportarError("*ALERTA*\n	Incorrecta Asociación de la clave "+constraint.get(i).getName()+" en "+ entidad.nombre_entidad+".\n");
				//Se verifican si existen restricciones del tipo <unique>
			}else if (constraint.get(i).getCategory()==2){
				//se verifica que el atributo este definido
				if (constraint.get(i).getSelector().getXPath().toString().toUpperCase().equalsIgnoreCase(entidad.tipo)){
					unico.put(constraint.get(i).getFields().get(0).getXPath().value,null);
				}
				else ReportarError("*ALERTA*\n	Incorrecta Asociación de la clave "+constraint.get(i).getName()+" en "+ entidad.nombre_entidad+".\n");
			}else{
				ReportarError("*ALERTA*\n	Restriccion no valida en el parser.\n");
			}
			i--;
		}
		entidad.setClave(clave);
		entidad.setUnico(unico);
	}

	/**
	 * La funci&#243n leerElementos es la encargada de leer y almacenar en un hash de Entidades,
	 * a todas aquellas entidades definidas dentro del XMLSchema, (que no son más que todos los "element"
	 * tags definidos en el nivel más externo de anidamiento)
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

			tipo = element.getType().getName();
			//-------------- modificación Chelo 21-03 --------------------------
			//CHELOOO AQUI... if tipo es nulo... vas a llamar a una funcion a la que le pasaras
			// el element y preguntaras if element is complex (ve el codigo de kari), si es asi
			// guardo la entidad en una estructura para saber que es especial. LE coloco el nombre como tipo
			// para que se cree la entidad.
			if (tipo==null) {
				enearias.put(nombre, element.getType().asComplexType());
				tipo= nombre;
			}
			nueva_entidad.setTipo(tipo);
			nueva_entidad.setNombre_entidad(nombre);
			entidades.put(tipo, nueva_entidad);
			nombreEntidades.put(nombre, tipo);

			//se verifican las restricciones del tipo <tag>
			if (restricciones.size()>0){
				TagRestriccion(restricciones, nueva_entidad);
			}
		}

	}

	/**
	 * La funci&#243n crearEntidadAPartirDeOtra permite crear una entidad a partir de otra ya definida.
	 * La utilidad de esta función es que permitirá crear a las subclases a partir de la información de la superclase
	 * en caso de que la opción de traduccion sea dos, crear las entidades de atributos multivaludados y multivaluados 
	 * compuestos en caso de que la superclase a la que pertenecen desaparezca.
	 * 	
	 * @param entidadOriginal: Entidad de la cual se extraerá la información.
	 * @param entidadNueva: Entidad a la cual se le agregarán los nuevos datos.
	 * @param caso: int que nos indica en cual de los casos citados anteriormente estamos. 
	 * @param concatena: entero que permite colocar distintos nombres a las claves de 
	 * las subclases de una superclase.
	 * @param subclase: subclase a la cual deberá hacer referencia la nueva entidad que representa al atributo 
	 * multivaluado de la subclase (dicho att multivaluado lo hereda de la superclase)
	 * 
	 * 0 -> entidadOriginal es una Superclase y 
	 *      entidadNueva es una subclase de ella
	 * 1 -> entidadOriginal es una Entidad que representa el atributo multivaluado de una superclase
	 *      entidadNueva es una Entidad que representa el atributo multivaluado de una subclase
	 * 2 -> entidadOriginal es una Entidad que representa el atributo multivaluado compuesto de una superclase
	 *      entidadNueva es una Entidad que representa el atributo multivaluado compuesto de una subclase     
	 */
	@SuppressWarnings("unchecked")
	public static int crearEntidadAPartirDeOtra(Entidad entidadOriginal, Entidad entidadNueva, int caso, int concatena, Entidad subclase)
	{
		
		String subcl = entidadNueva.tipo;
		String sup = entidadOriginal.tipo;
		
		//Se pasan los atributos
		Vector<Atributo> atributosSup= entidadOriginal.getAtributos();
		Vector<Atributo> atributosSub= entidadNueva.atributos;
		atributosSub.addAll(atributosSup);
		
		if(caso == 0)
		{	
			//Se pasa la clave
			HashMap<String,Atributo> claveSub=new HashMap<String,Atributo>();
			HashMap<String,Atributo> claveSup = entidadOriginal.getClave();
			Iterator< Atributo> clav = claveSup.values().iterator();
			
			while(clav.hasNext())
			{
				Atributo at= (Atributo)clav.next().clone();
				at.nombre= at.nombre+concatena;
				claveSub.put(at.nombre, at);
				concatena++;
			}
			entidadNueva.setClave(claveSub);
			
			//Se le agregan a las subclases el resto de las cosas que tenía la superclase
			//las referecias y tenemos que cambiar las referencias circulares que habían hacia
			//la superclase que ahora desaparecerá
			Iterator<Vector<Atributo>> referenciasSupVal = entidadOriginal.getReferencias().values().iterator();
			Vector<String> referencias_agregadas = new Vector<String>();
			while (referenciasSupVal.hasNext())
			{
				Enumeration<Atributo> ref = referenciasSupVal.next().elements();
				while(ref.hasMoreElements())
				{
					//Paso referencias de la superclase a la subclase
					Atributo attr = (Atributo)ref.nextElement().clone();
					entidadNueva.setReferencia(attr);
					//Debo guardar la información de el tipo de la entidad
					//a la cual se hacia referencia, para
					//luego cambiar en ellas el tipo en las referencias circulares,
					//que ahora debe ser hacia la subclase
					String tipo = attr.getTipo();
					if (!referencias_agregadas.contains(tipo))
						referencias_agregadas.add(tipo);
				}
	
			}
			//Cambio el tipo de la referencia circular
			Iterator<String> referencias_a_cambiar = referencias_agregadas.iterator();
			while(referencias_a_cambiar.hasNext())
			{
				String tipo = referencias_a_cambiar.next();
				Vector<Atributo> referenciasCirculares = entidades.get(tipo).getReferencias().get(sup);
				Iterator<Atributo> refCir = referenciasCirculares.iterator();
				while(refCir.hasNext())
				{
					Atributo nuevo_attr = (Atributo)refCir.next().clone();
					nuevo_attr.setTipo(subcl);
					entidades.get(tipo).setReferencia(nuevo_attr);
				}
			}
			//Unico
			HashMap<String,Atributo> unico = entidadOriginal.getUnico();
			entidadNueva.setUnico(unico);
			//Tipo
			String tipo = subcl;
			entidadNueva.setTipo(tipo);
		}
		
		if (caso == 1 || caso ==2)
		{
			//Se pasa la clave
		    entidadNueva.setClave((HashMap<String, Atributo>) entidadOriginal.clave.clone());
			
			//Se le pasan las claves foraneas relacionadas con la primary key, 
			//pero ahora hacen referencia a la subclase en lugar de la superclase
			
			SetForaneoMultivaluado(subclase, entidadNueva);
	    }	
		
		return concatena;
		
	}
	
	/**
	 * La funci&#243n leerAtributosEntidades es la encargada de leer y almacenar por cada ComplexType que defina a una
	 * Entidad dentro del XMLSchema, todos sus atributos con sus restricciones. Esta funci&#243n se apoya de las funciones
	 * auxiliares leerAtributos2 y leerElementos
	 * @param claves Iterador de String, en el que se almacenan los nombres de los ComplexTypes que definen Entidades.
	 * @param valores Iterador de XSComplexType que contiene la informaci&#243n de cada uno de los ComplexType definidos
	 * en el nivel m&#225s externo de anidamiento, que definen a Entidades.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void LeerAtributosEntidades(Iterator<String> claves, Iterator<XSComplexType> valores) throws IOException{

		XSComplexType complex;
		XSContentType contenido;
		XSParticle particle;
		XSTerm term;
		XSModelGroup xsModelGroup;
		XSParticle[] particles;

		//Se leen los complexType que si corresponden a Entidades
		while (claves.hasNext() && valores.hasNext())
		{
			String tipo = (String) claves.next();
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
						ReportarError("*ALERTA*\n	Los elements (atributos) definidos dentro del complexType <" +tipo+
								"> deben estar definidos entre el compositor <sequence> .\n	Agregue el compositor <sequence> " +
						"para evitar inconsistencias al momento de cargar los datos.\n");
					}

					// Se leen los atributos de las entidades
					Vector<Atributo> atributos = entidades.get(tipo).getAtributos();
					atributos = leerElementos(particles,tipo,atributos,false,false);
					System.out.println("AQUII IMPRIMIR " +entidades.get(tipo).imprimir + "de tipo"+tipo +"\n");
					entidades.get(tipo).setAtributos(atributos);
				}
			}
		}
		//Se muestra error para aquellos ComplexType que nunca se les definio
		//entidad, pero que tampoco fueron referenciados como subclases por ninguna entidad (superclase)
		Iterator<String> SinEntidad = complexSinEntidad.keySet().iterator();
		while(SinEntidad.hasNext())
		{
			String next = (String) SinEntidad.next();
			if(!subclases.containsKey(next))
			{
				ReportarError("*ALERTA*\n	El elemento del tipo "+ next +" no esta definido.\n	" +
						"No se creará el tipo "+ next +", ni la Entidad hasta que no realice los cambios .\n\n");
			}
		}
		//En caso de que existan generalización/especialización solapado
		//Se hacen los ajustes necesarios
		/*System.out.println("AQUIIII!!");
		Iterator<String> multi = multivaluados.keySet().iterator(); 
		//Iterator<String> multiComp = multivaluadosCompuestos.keySet().iterator();
		while(multi.hasNext())
		{
			String ent = multi.next();
			System.out.println("Entidad que posee multivaluados"+ ent+ "\n");
			Enumeration<String> entidadesDeMultivaluados = multivaluados.get(ent).elements();
			while (entidadesDeMultivaluados.hasMoreElements())
			{
				System.out.println("Atributos multivaluados "+entidadesDeMultivaluados.nextElement()+ "\n");
			}	
		}*/	
		
		
		if(!superclases.isEmpty())
		{
			Iterator<String> superC = superclases.keySet().iterator();
			Iterator<Vector<String>> subC = superclases.values().iterator();
			while(superC.hasNext() && subC.hasNext())
			{
				String sup = superC.next();
				Vector<String> sub = subC.next();
				HashMap<String,Atributo> claveSup = entidades.get(sup).getClave();
				Enumeration<String> subclass= sub.elements();

				if (opcionTraduccion==2)
				{
					int concatena =0;
					
					while(subclass.hasMoreElements())
					{
						
						String subcl = subclass.nextElement();//Obtengo la subclase
						
						//Agregas a cada subclase todos los atributos de la superclase
						//Y la clave será la clave de la superclase
						//Tambien le debes pasar todas las referencias que tenía la superclase y
						//cambiar las referencias circulares
						 
						concatena = crearEntidadAPartirDeOtra(entidades.get(sup), entidades.get(subcl),0, concatena, null);
						entidades.get(sup).imprimir = false;
						entidades.get(subcl).imprimir = false;
						//Como se escogio opcion de traduccion 2, 
						//debo verificar si la superclase de esta subclase tenía atributos multivaluados, 
						//de ser así debera crear una entidad que represente al atributo multivaluado
						//de dicha subclase
						if(multivaluados.containsKey(sup))
						{
							Iterator<String> attr_mult = multivaluados.get(sup).iterator();
							while(attr_mult.hasNext())
							{	
								String mult = attr_mult.next();
								if(entidades.containsKey(mult))
								{	
									Entidad entidad_multivaluada = new Entidad();
									entidad_multivaluada.nombre_entidad = entidades.get(mult).nombre_entidad + "_" + subcl;
									entidad_multivaluada.tipo = entidades.get(mult).nombre_entidad + "_" + subcl;
								
									crearEntidadAPartirDeOtra(entidades.get(mult), entidad_multivaluada ,1, -1, entidades.get(subcl));
								
									entidad_multivaluada.imprimir = false;
									entidades.put(entidad_multivaluada.tipo, entidad_multivaluada);
								}
							}	
						}
						//Como se escogio opcion de traduccion 2, 
						//debo verificar si la superclase de esta subclase tenía atributos compuestos multivaluados, 
						//de ser así debera crear una entidad que represente al atributo compuesto multivaluado
						//de dicha subclase
						if(multivaluadosCompuestos.containsKey(sup))
						{
							Iterator<String> attr_mult = multivaluadosCompuestos.get(sup).iterator();
							while(attr_mult.hasNext())
							{	
								String mult = attr_mult.next();
								if(entidades.containsKey(mult))
								{
									Entidad entidad_multivaluada = new Entidad();
									entidad_multivaluada.nombre_entidad = entidades.get(mult).nombre_entidad + "_" + subcl;
									entidad_multivaluada.tipo = entidades.get(mult).nombre_entidad + "_" + subcl;
									 
									crearEntidadAPartirDeOtra(entidades.get(mult), entidad_multivaluada ,2, -1, entidades.get(subcl));
									
									entidad_multivaluada.imprimir = false;
									entidades.put(entidad_multivaluada.tipo, entidad_multivaluada);
								}	
							}
						}
					}
					//Eliminamos a la superclase y a todos las entidades que representen 
					//atributos multivaluados y multivaluados compuestos de dicha superclase
					entidades.remove(sup);
					if(multivaluados.containsKey(sup))
					{
						Iterator<String> attr_mult = multivaluados.get(sup).iterator();
						while(attr_mult.hasNext())
						{	
							entidades.remove(attr_mult.next());	
						}
					}	
					if(multivaluadosCompuestos.containsKey(sup))
					{
						Iterator<String> comp_mult = multivaluadosCompuestos.get(sup).iterator();
						while(comp_mult.hasNext())
						{		
							entidades.remove(comp_mult.next());	
						}
					}
					
					//Debes eliminar todas las referencias hechas a la superclase
					Iterator<Entidad> entidads = entidades.values().iterator();
					while(entidads.hasNext())
					{
						Entidad ent = entidads.next();
						if(ent.getReferencias().containsKey(sup))
						{
							ent.getReferencias().remove(sup);
						}
					}
					
				}
				else if (opcionTraduccion == 1)
				{
					//Agregas como clave primaria de cada subclase, la clave primaria de la superclase
					//como foránea
					while(subclass.hasMoreElements())
					{
						String subcl = subclass.nextElement();
						//Se le coloca como clave, la clave de la superclase
						entidades.get(subcl).setClave(claveSup);
						//Esa clave se la agregas como atributo y como foránea
						Iterator<Atributo> clavesSup = claveSup.values().iterator();
						Vector <Atributo> foraneas = new Vector<Atributo>();
						while(clavesSup.hasNext())
						{
							Atributo attr = clavesSup.next();
							//entidades.get(subcl).setAtributo(attr);
							foraneas.add(attr);
						}
						Vector <Vector<Atributo>> attrForaneas = new Vector<Vector<Atributo>>();
						attrForaneas.add(foraneas);
						HashMap<String, Vector <Vector<Atributo>>> foraneasCompleta = new HashMap<String, Vector<Vector<Atributo>>>();
						foraneasCompleta.put(sup, attrForaneas);
						entidades.get(subcl).setForaneo(foraneasCompleta);
					}
				}
			}
		}
	}

	/**
	 * Funcion que permite cambiar el nombre de un atributo para evitar conflictos en la 
	 * escritura del SQL
	 * 
	 * @param subcl String con el tipo de la clase a la cual se le cambiará el nombre del atributo
	 * @param namenew String con el nuevo nombre.
	 * @param nameold String con el nombre viejo para realizar la búsqueda por el vector de atributos.
	 */
	public static void CambiarNombreAtributo(String subcl, String namenew, String nameold){
		Iterator< Atributo> it= entidades.get(subcl).atributos.iterator();
		while(it.hasNext()){
			Atributo atr = it.next();
			if (atr.nombre.equals(nameold)) {
				atr.nombre= new String (namenew);
				break;
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
		if (atributo.getValor()=="" | atributo.getValor().equals("")){
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
	 * Se define la clave de la entidad. Chequea que se este bien definida y verifica que no sea nula
	 * @param entidad a la que se desea definir la entidad
	 */
	@SuppressWarnings("unchecked")
	public static void defineClave(Entidad entidad){
		Vector<Atributo> atributos = entidad.getAtributos();
		HashMap<String,Atributo> clave = entidad.getClave();
		HashMap<String,Atributo> unico = entidad.getUnico();
		unico.remove("");
		clave.remove("");


		
		//Se verifica si se definió la clave de la entidad, en caso de que solo hayan
		//atributos unicos estos son tomados como clave de la entidad

		if (unico.size()>0 && clave.size()==0){
			clave = (HashMap<String, Atributo>) unico.clone();
			unico.clear();
			
		}

		int j = atributos.size()-1;
		while (j>=0){

			if (clave.containsKey(atributos.get(j).nombre) && clave.get(atributos.get(j).nombre)==null ){
		
				clave.remove(atributos.get(j).nombre);
				clave.put(atributos.get(j).nombre,atributos.get(j));

			}j--;
		}


		Iterator<String> iter = clave.keySet().iterator();
		HashMap<String,Atributo> clave2 = (HashMap<String, Atributo>) clave.clone();

		try {
			while (iter.hasNext()){
				String nombre = iter.next();

				if (clave.get(nombre)==null){
					ReportarError("*ALERTA*\n	" +nombre+ " no ha sido definido como atributo en la entidad "+ entidad.nombre_entidad+".\n");
					clave2.remove(nombre);

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		entidad.setClave(clave2);
	}
	
	/**
	 * Se definen los atributos unicos pertenecientes a la entidad.
	 * @param entidad en la cual se estan observando los atributos
	 */
	@SuppressWarnings("unchecked")
	public static void defineUnico(Entidad entidad){
		Vector<Atributo> atributos = entidad.getAtributos();
		HashMap<String,Atributo> unico = entidad.getUnico();
		HashMap<String,Atributo> clave = entidad.getClave();
		unico.remove("");

		if (unico.size()>0 && clave.size()==0){
			clave = (HashMap<String, Atributo>) unico.clone();
			entidad.setClave(clave);
			entidad.setUnico(new HashMap<String, Atributo>());

		}else{
			int j = atributos.size()-1;
			while (j>=0){
				if (unico.containsKey(atributos.get(j).nombre) && unico.get(atributos.get(j).nombre)==null ){
					unico.remove(atributos.get(j).nombre);
					unico.put(atributos.get(j).nombre,atributos.get(j));

				}j--;
			}
			Iterator<String> iter = unico.keySet().iterator();

			HashMap<String,Atributo> clave2 = (HashMap<String, Atributo>) unico.clone();

			try {
				while (iter.hasNext()){
					String nombre = iter.next();

					if (unico.get(nombre)==null){
						ReportarError("*ALERTA*	" +nombre+ " no ha sido definido como atributo en la entidad "+ entidad.nombre_entidad+".\n");
						clave2.remove(nombre);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			entidad.setUnico(clave2);
		}
	}

	/**
	 * Dada una entidad se verifica que tenga clave primaria y se retorna
	 * su valor
	 * @param entidad en la cual se desea búscar la clave.
	 * @return String que contiene la clave de la entidad.
	 */
	public static String retornaClave(Entidad entidad){
		HashMap<String,Atributo> clave = entidad.getClave();
		Iterator<String> iter = clave.keySet().iterator();


		try {
			int i = clave.size();
			String salida = "(";

			if (i==0){
				ReportarError("**ERROR**\n	Clave no definida en la entidad :"+ entidad.nombre_entidad+".\n");
				return "(Clave no definida";
			}
			else if (i==1){
				return "("+clave.get(iter.next()).nombre.toUpperCase();
			}else{

				while (iter.hasNext()) {
					salida = salida+clave.get(iter.next()).nombre+",";

				}
				return salida.substring(0, salida.length()-1).toUpperCase();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}return "Clave Invalida";

	}

	/**
	 * Se verican si existen referencias en la entidad
	 * @param vector que contiene los atributos que son referencias a otras entidades
	 * @return String resultante con los atributos que son referencia a otra entidad
	 */
	public static String retornaForaneos(Vector<Atributo> vector){
		int i = vector.size();
		String salida = "(";
		
		try {
			if (i==0){
				return "(Atributo no definido";
			}
			else if (i==1){
				return "("+vector.get(0).nombre.toUpperCase();
			}else{
				i--;
				while (i>=0) {
					salida = salida+vector.get(i).nombre+",";
					i--;
				}
				return salida.substring(0, salida.length()-1).toUpperCase();
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return "Foraneo Invalido";
	}

	public static void CreaReporte(){
		FileWriter fstream;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter("error");
			out = new BufferedWriter(fstream);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();}
	
	}
	public static void ReportarError(String error){
		FileWriter fstream;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter("error",true);
			out = new BufferedWriter(fstream);
			out.write(error);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();}
	}
	public static String retornaUnico(Collection<Atributo> atributos){
	
		Iterator<Atributo> iter = atributos.iterator();
		String salida = "(";
		
	
			while (iter.hasNext()){
				salida = salida+iter.next().nombre+",";
			}
			return salida.substring(0, salida.length()-1).toUpperCase();
	
	}
	/**
	 * Método que se encarga de crear el archivo sql correspondiente al xml
	 * schema proporcionado.
	 * @throws IOException 
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
		Vector<Collection<Atributo>> unicos_inter = new Vector<Collection<Atributo>>();
		int k = 0,j = 0, l = 0;

		
			//Se crea el archivo sql de salida.
			FileWriter fstream;
			try {
				fstream = new FileWriter("out.sql");
			
			BufferedWriter out = new BufferedWriter(fstream);

			//Se iteran sobre las entidades que se van a crear.
			while (cadaTipo.hasNext()) {

				entidad = entidades.get(cadaTipo.next());
				// Se realiza un 'CREATE TABLE' por cada entidad encontrada
				out.write("CREATE TABLE "+ entidad.getNombre_entidad().
						toUpperCase()+" (\n");

				
				defineClave(entidad);
				HashMap<String,Atributo> clave_1 = entidad.getClave();
				Iterator<Atributo> iter_c = clave_1.values().iterator();
				System.out.println("IMPRIMIENTO CLAVE \n"+ entidad.nombre_entidad);
				try {
					while (iter_c.hasNext()){
						
						Atributo a= iter_c.next(); 
						out.write(" "+a.getNombre().toUpperCase()+
								" "+ TipoDato(a)+" "+ Nulidad(a)+
								" "+ValorDefecto(a)+" ,\n");
						//out.write(" CONSTRAINT "+entidad.getNombre_entidad().toUpperCase()+"_UNIQUE UNIQUE ("+iter_unico.next().nombre.toUpperCase()+"),\n");
					}
				} catch (Exception e3) {
					System.out.println("ERROR: al escribir los atributos claves");
					e3.printStackTrace();
				}
				System.out.println("IMPRIMIENTO ATRIBUTOS \n"+ entidad.nombre_entidad);
				
				j = entidad.getAtributos().size()-1;
				// se inicializan las variables para la nueva entidad
				atributos = entidad.getAtributos();
				booleanos = new Vector<Atributo>();
				dominios = new Vector<Atributo>();
				rangos = new Vector<Atributo>();

				//Se agregan los atributos basicos de la entidad.
				try {
					while (j >= 0) {
						out.write(" "+atributos.get(j).getNombre().toUpperCase()+
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
				} catch (Exception e2) {
					System.out.println("ERROR: al escribir los atributos\n");
					e2.printStackTrace();
				}

				//Se obtiene los atributos que son referencias

				
				System.out.println("IMPRIMIENTO FOREANEOS \n"+ entidad.nombre_entidad);
				Iterator<Vector<Vector<Atributo>>> iter_for_ini = entidad.foraneo.values().iterator();
				Vector <Atributo> foraneos = new Vector<Atributo>();
				try {
					if (entidad.imprimir){
					while(iter_for_ini.hasNext()){

						Vector<Vector<Atributo>> vector_iter_for = iter_for_ini.next();

						int i = vector_iter_for.size()-1;
						
						while (i>=0){
							foraneos = vector_iter_for.get(i);
							j = foraneos.size()-1;

							while (j >= 0) {
								out.write(" "+foraneos.get(j).getNombre().toUpperCase()+
										" "+TipoDato(foraneos.get(j)).toUpperCase() +" "+
										Nulidad(foraneos.get(j))+" "+ValorDefecto(foraneos.get(j))+" ,\n");
								j--;

							} i--;
						}
					}
					}
				} catch (Exception e1) {
					System.out.println("ERROR: al escribir el Constraint de Foreing Key\n");
					e1.printStackTrace();
				}

				System.out.println("IMPRIMIENTO CONSTRAINT FORANEO \n"+ entidad.nombre_entidad);
				try {
					iter_for_ini = entidad.foraneo.values().iterator();
					Iterator<String> iter_for_ini_f = entidad.foraneo.keySet().iterator();
					foraneos = new Vector<Atributo>();

					while(iter_for_ini.hasNext()){

						Vector<Vector<Atributo>> vector_iter_for = iter_for_ini.next();

						int i = vector_iter_for.size()-1;
						String tipo= iter_for_ini_f.next();

						while (i>=0){
							foraneos = vector_iter_for.get(i);
						
							out.write(" CONSTRAINT FK_"+entidad.getNombre_entidad().toUpperCase()+"_"+retornaForaneos(foraneos).substring(1, retornaForaneos(foraneos).length()).replace(',','_')+"_"+i+ " FOREIGN KEY "+retornaForaneos(foraneos)
									+") REFERENCES "+ entidades.get(tipo).nombre_entidad.toUpperCase() +" "+retornaClave(entidades.get(tipo))+"),\n");

							i --;
						}

					}
				} catch (Exception e) {
					
					System.out.println("ERROR: al escribir el Constraint de Foreing Key");
					e.printStackTrace();
				}
				System.out.println("IMPRIMIENTO CONSTRAINT CHECK \n"+ entidad.nombre_entidad);
				k = booleanos.size()-1;
				//Se agregan los contraint de atributo booleano
				try {
					while (k >= 0) {
						out.write(" CONSTRAINT CHECK_BOOLEAN_"+booleanos.get(k).
								getNombre().toUpperCase()+ " CHECK (" +booleanos.get(k).
								getNombre().toUpperCase() + " IN ('0','1')),\n");
						k--;
					}
				} catch (Exception e) {
					System.out.println("ERROR: al escribir el Check Constraint\n");
					e.printStackTrace();
				}

				System.out.println("IMPRIMIENTO CONSTRAINT DOMINIO \n"+ entidad.nombre_entidad);
				l = dominios.size()-1;
				//Se agregan los contraint de dominio a la entidad.
				try {
					while (l >= 0) {

						out.write(" CONSTRAINT CHECK_DOMINIO_"+dominios.get(l).
								getNombre().toUpperCase()+ " CHECK (" +dominios.get(l).
								getNombre().toUpperCase() + " IN ("+DominioAtributo(dominios.
										get(l).getDominio())+")),\n");
						l--;
					}
				} catch (Exception e) {
					System.out.println("ERROR: al escribir el Constraint de Dominio\n");
					e.printStackTrace();
				}

				
				System.out.println("IMPRIMIENTO CONSTRAINT RANGO \n"+ entidad.nombre_entidad);
				l = rangos.size()-1;
				//Se agregan los contraint de rango a la entidad
				try {
					while (l >= 0) {
						if ((rangos.get(l).getMaxRango().equals("-1")) &&
								(rangos.get(l).getMinRango().equals("-1"))){}
						else if (!(rangos.get(l).getMaxRango().equals("-1")) &&
								!(rangos.get(l).getMinRango().equals("-1"))){

							out.write(" CONSTRAINT CHECK_RANGO_"+rangos.get(l).
									getNombre().toUpperCase()+ " CHECK (" +rangos.get(l).
									getNombre().toUpperCase() + " BETWEEN "+rangos.get(l).
									getMinRango()+" AND "+rangos.get(l).getMaxRango()+ "),\n");

						}else if ((rangos.get(l).getMaxRango()=="-1") &&
								!(rangos.get(l).getMinRango()=="-1")){

							out.write(" CONSTRAINT CHECK_RANGO_"+rangos.get(l).
									getNombre().toUpperCase()+ " CHECK (" +rangos.get(l).
									getNombre().toUpperCase() + " >= "+rangos.get(l).
									getMinRango()+ "),\n");

						}else{

							out.write(" CONSTRAINT CHECK_RANGO_"+rangos.get(l).
									getNombre().toUpperCase()+ " CHECK (" +rangos.get(l).
									getNombre().toUpperCase() + " <= "+rangos.get(l).
									getMaxRango()+"),\n");

						}

						l--;
					}
				} catch (Exception e) {
					System.out.println("ERROR: al escribir el Constraint de Rango\n");
					e.printStackTrace();
				}

				System.out.println("IMPRIMIENTO CONSTRAINT UNIQUE \n"+ entidad.nombre_entidad);
				defineUnico(entidad);
				HashMap<String,Atributo> unico = entidad.getUnico();
				Iterator<Atributo> iter_unico = unico.values().iterator();

				try {
					while (iter_unico.hasNext()){
						out.write(" CONSTRAINT "+entidad.getNombre_entidad().toUpperCase()+"_UNIQUE UNIQUE ("+iter_unico.next().nombre.toUpperCase()+"),\n");
					}
				} catch (Exception e1) {
					System.out.println("ERROR: al escribir el Unique Contraint\n");
					e1.printStackTrace();
				}
				
				System.out.println("IMPRIMIENDO CONSTRAINT DE CHELO");
				unicos_inter = entidad.unike;
				int o = unicos_inter.size()-1;
				while (o>=0){
					out.write(" CONSTRAINT "+entidad.getNombre_entidad().toUpperCase()+"_UNIQUE_"+o+ " UNIQUE"+retornaUnico(unicos_inter.get(o))+"),\n");
					o--;
				}
				
				
				
				
				
				System.out.println("IMPRIMIENTO CONSTRAINT PRIMARY KEY  \n"+ entidad.nombre_entidad);
				defineClave(entidad);
				//Se agrega la clave primaria a la entidad
				try {
					out.write(" CONSTRAINT PK_"+entidad.getNombre_entidad().
							toUpperCase()+ " PRIMARY KEY "+ retornaClave(entidad).
							toUpperCase()+")\n);\n");
				} catch (Exception e) {
					System.out.println("ERROR: al escribir el Constraint Primary Key\n");
					e.printStackTrace();
				}
			}

			//Se cierra el output de escritura en el archivo sql
			out.close();
			}
			// Se toma la exce//ion si existe
		catch (Exception e){
			System.err.println("*ERROR*	Escribiendo el script sql: " + e.getMessage());
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

			System.out.println("===== Entidad: " + entidad.getNombre_entidad() + " -- Tipo: " + entidad.getTipo() + " -- Clave: "+entidad.getClave()+ " =====\n");
			//System.out.println("-- Tipo: " + entidad.getTipo() + " --"+ "Clave :"+entidad.getClave()+ " --");

			int j = entidad.getAtributos().size()-1;

			Vector<Atributo> atributos = entidad.getAtributos();
			System.out.println("-- Atributos básicos --");

			while (j >= 0) {
				System.out.println(" Nombre : " + atributos.get(j).getNombre());
				System.out.println(" Tipo : " + atributos.get(j).getTipo());
				System.out.println(" Nulo : " + atributos.get(j).isNulo());
				System.out.println(" Default : " + atributos.get(j).getValor());
				System.out.println(" Longitud : " + atributos.get(j).getLongitud());
				System.out.println(" MinOccurs : " + atributos.get(j).getMinOccurs());
				System.out.println(" MaxOccurs : " + atributos.get(j).getMaxOccurs());

				j--;
			}

			//Se obitiene un iterador que recorre los vectores que poseen entidades.

			//Se imprimen las referencias
			System.out.println("-- Referencias --");
			HashMap<String,Vector<Atributo>> referencias = entidad.getReferencias();
			Iterator<String> ref = referencias.keySet().iterator();
			Iterator<Vector<Atributo>> aref = referencias.values().iterator();

			while (ref.hasNext() && aref.hasNext()) {
				String tipoRef = ref.next();
				Vector<Atributo> elAtrr = aref.next();
				Enumeration<Atributo> x = (Enumeration<Atributo>) elAtrr.elements();
				while(x.hasMoreElements())
					System.out.println("Referencia: " + x.nextElement().getNombre() + " de tipo "+ tipoRef+"\n");
			}


			System.out.println("-- Atributos Foraneos --");

			Iterator<Vector<Vector<Atributo>>> iter_for_ini = entidad.foraneo.values().iterator();
			Vector <Atributo> foraneos = new Vector<Atributo>();

			while(iter_for_ini.hasNext()){

				Vector<Vector<Atributo>> vector_iter_for = iter_for_ini.next();

				int i = vector_iter_for.size()-1;

				while (i>=0){
					foraneos = vector_iter_for.get(i);
					j = foraneos.size()-1;

					while (j >= 0) {
						System.out.println(" Nombre : " + foraneos.get(j).getNombre());
						System.out.println(" Tipo : " + foraneos.get(j).getTipo());
						System.out.println(" Nulo : " + foraneos.get(j).isNulo());
						System.out.println(" MinOccurs : " + foraneos.get(j).getMinOccurs());
						System.out.println(" MaxOccurs : " + foraneos.get(j).getMaxOccurs());

						j--;
					}
					i--;
				}

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

			String tipoEnt= y.next(); //Tipo de Ent.
			Entidad ent = entidades.get(tipoEnt); //Entidad a estudiar.

			//Recorro los tipos de las referencias y voy trabajando con los vectores.
			Iterator<String> iter = ent.referencias.keySet().iterator();

			visitados.put(tipoEnt, new Vector<String>());
			// Agrego al tipo en el hash de visitados.

			while(iter.hasNext()){
				String tipoEnti = iter.next(); //Tipo a tratar de Enti.
				if(visitados.containsKey(tipoEnti) && !tipoEnt.equals(tipoEnti)){
					if(!visitados.get(tipoEnti).contains(tipoEnt))
					{
						ReportarError("**ERROR**\n	Los atributos de tipo "+ tipoEnti+" de la entidad"+
								entidades.get(tipoEnt).nombre_entidad+" no poseen referencia circular"+
								" con la entidad " + entidades.get(tipoEnti).nombre_entidad+".\n");
					}
				}
				else
				{
					visitados.get(tipoEnt).add(tipoEnti);//Lo agrego porq lo vi.
					Entidad enti= entidades.get(tipoEnti); // Entidad relacionada con ent.

					if (enti.getNombre_entidad().equals(ent.getNombre_entidad())) {
						/*
						 * Es una entidad que se relaciona consigo misma, quedamos que si tiene (1,1) se absorbe.
						 * sino se crea otra entidad.
						 */
						Vector<Atributo> refself= ent.referencias.get(tipoEnt); //Atributos de si mismo.
						Iterator<Atributo> i= refself.iterator();

						while(i.hasNext()){
							//Para cada atributo a mi mismo, veo si me absorbo o si creo otra entidad.
							Atributo at = i.next();
							if (at.minOccurs ==1 && at.maxOccurs == 1) {
								//Se absorbe a si misma
								ent.AgregarForaneo(ent.tipo,ent.clave.values());
							}
							else
							{
								//Se crea entidad.
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

								//Introduzco tambien en el foraneo.
								entidadNueva.AgregarForaneo(tipoEnt, ent.clave.values());

								//introduzco en el hash.
								entidades.put(entidadNueva.tipo, entidadNueva);
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
							ReportarError("**ERROR**\n	No hay referencia circular entre "+ent.nombre_entidad+" y "+enti.nombre_entidad+".\n");

						} else {
							if (vectEnt.size()+vectEnti.size()==2) {
								//Ambos vectores son de tamaño 1 por ende debo asumir que se relacionan.
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
											DefInterrelacion(b,c);
											break;
										}
									}
									if (!encontro) {
										//no hay referencia circular.
										ReportarError("**ERROR**\n	No hay ref circular para el atributo "+ c.nombre+" de la entidad "+ ent.nombre_entidad+".\n");
									}
								}
								if (vectEnt.size()!=0) {
									Iterator<Atributo> t= vectEnt.iterator();
									while(t.hasNext()){
										System.out.println("**ERROR**\n	No hay ref circular para el atributo "+ t.next().nombre+" de la entidad "+enti.nombre_entidad+".\n");
									}
									//Quiere decir que aun quedaron atributos que no tenian pareja.
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Función que se encarga de analizar si existen relaciones enearias dentro 
	 * del diagrama ER. Para ello utiliza la estructura global "enearias" y el 
	 * hash global de "entidades".
	 */
	public static void VerEnearias() {

		//Me aseguro de que existan posibles enearias.
		if (!enearias.isEmpty()) {
			//Recorro las enearias.
			Iterator<String> it = enearias.keySet().iterator();
			while(it.hasNext()){
				Entidad enearia = entidades.get(it.next()); // Entidad postulada a ser enearia.
				//Veo si existe más de dos entidades referenciando a la enearia.
				
				int numEntidades=0;
				
				Iterator<Vector<Atributo>> ref= enearia.referencias.values().iterator();
				
				while(ref.hasNext()){
					//Saco cada vector del hash de referencias y veo su tamaño y lo voy sumando,
					//esto es porque puede que una entidad este dos veces en la enearia, hay que 
					// considerarla.
					
					numEntidades= numEntidades+ref.next().size();
				}
				
				boolean error = false;
				if (numEntidades<= 2) {
					//Si esto ocurre es porque la enearia estuvo definida solo con dos o menos entidades, lo cual 
					// implica que hay que revisar que los min y max occurs NO sean 11.
					Iterator<Vector<Atributo>> iter= enearia.referencias.values().iterator();
					while(iter.hasNext()){
						Iterator<Atributo> o= iter.next().iterator();
						while(o.hasNext()){
							Atributo atri= o.next();
							if (atri.minOccurs==1 && atri.maxOccurs==1) {
								ReportarError("**ERROR**\n El atributo "+ atri.nombre + " no puede tener minOccurs y maxOccurs igual a 1\n\n");
								error= true;
								entidades.remove(enearia.tipo);
								break;
							}
						}
					}
					//Se retira esa entidad del hash de entidades para que no sea analizada.
				}
				
				if(!error){
					//Recorro cada una de las entidades a las que hago referencia para ver si hay referencia circular
					// NO DEBE HABER REFERENCIA CIRCULAR, sino no se cumple el concepto de la enearia.
					
					//Recorro las referencias para buscarlas en entidades.
					Iterator<String> iter= enearia.referencias.keySet().iterator();
					boolean esEnearia = true;
					while(iter.hasNext()){
						String tipoEntidad= iter.next();//Tipo de la entidad relacionada en la enearia.
						Entidad ent = entidades.get(tipoEntidad); // Entidad relacionada en la enearia.
						if (ent == null) {
							//Se referencio a una entidad que no existe.
							ReportarError("ERROR: la entidad "+ tipoEntidad +" no esta definida.\n");
							//Remuevo la entidad enearia para que no se le genere tabla pues esta mala.
							entidades.remove(enearia.tipo);
							esEnearia=false;
							break;
						}
						else{
							//Se referencia a una entidad existente.
							//Reviso que en las referencias de "ent" no exista "enearia", si existe sería circular.
							
							if (ent.referencias.containsKey(enearia.tipo))
							{
								//Existe referencia circular. ERROR!
								ReportarError("**ERROR**\n	La interrelacion " + enearia.nombre_entidad + "pareciera ser enearia,"+
										"sin embargo existen referencias circulares entre ella y la entidad "+ ent.nombre_entidad+".\n");
								
								//Remuevo la entidad enearia del hash de entidades para que no se genere tabla.
								entidades.remove(enearia.tipo);
								esEnearia= false;
								break;
							}
						}
						if (!enearia.clave.isEmpty()) {
							//Tiene una clave lo cual no es correcto.
							ReportarError("**ERROR**\n	Las interrelaciones no poseen claves, se ha detectado que la interrelacion "+
									enearia.nombre_entidad + " tiene clave.\n");
							//Remuevo la entidad enearia.
							entidades.remove(enearia.tipo);
							esEnearia=false;
							break;
						}
						
					}
					if (esEnearia) {
						//POR AHORA COLOCARE TODOS LOS ATRIBUTOS COMO CLAVES.
					
						/*Iterator<Vector<Atributo>> itera= enearia.referencias.values().iterator();
						//tengo los vectores.
						while(itera.hasNext()){
							
							Iterator<Atributo> i= itera.next().iterator();
							//Tengo los atributos.
							int contador=1;
							while(i.hasNext()){
								Atributo at = i.next();
								
								//Saco la entidad del atributo para poder extraer su clave.
								Entidad ent= entidades.get(at.tipo); //Entidad del atributo.
								
								//Extraigo la clave y la meto como clave de "enearia"
								Iterator<Atributo> g = ent.clave.values().iterator();
								
								while(g.hasNext()){
									Atributo atributo= g.next();
									if(enearia.clave.containsKey(atributo.nombre))
									{
										System.out.println("Entre con el atributo "+ atributo.nombre);
										//Indica que esta entidad participa mas de una vez en la enearia.
										//Meto de nuevo el atributo pero con otro nombre.
										
										//Clono el atributo y le cambio el nombre.
										Atributo atri= (Atributo)atributo.clone();
										atri.nombre= atri.nombre+contador;
										System.out.println("le cambie el nombre a "+ atri.nombre);
										//Meto el atributo como clave
										enearia.clave.put(atri.nombre, atri);
										contador++;
									}
									else
									{
										//No esta repetida, la meto igual.
										enearia.clave.put(atributo.nombre, (Atributo)atributo.clone());
									}
								}
								//Agrego e	l atributo como foráneo.
								enearia.AgregarForaneo(at.tipo, ent.clave.values());
							}
						}
						//Elimino el hash de referencias para que no exista problemas con "VerInterrelaciones"
						enearia.referencias.clear();*/
						//------------------------------------------------------------------------------------------
						//Como es enearia debo recorrer todas las referencias y ver la cardinalidad para determinar,
						//quien sera clave y quien sera unica.
						
						//Creo un hash que vaya guardando a todos los atributos como clave por si no hay ningun
						// atributo con max 1.
						HashMap<String, Atributo> claveContodas=new HashMap<String, Atributo>(); 
						

						//Cambio imprimir a false pues no deben imprimirse las foraneas, para q no se dupliquen.
						enearia.imprimir=false;
						
						boolean hayClave= false; //Ayuda saber si ya la clave esta definida para no definirla varias veces.
						boolean hayMax1= false;  //Ayuda a saber si nadie tiene max=1 para ir llenando el hash auxiliar de claves.
						String tipoEntidadClave= ""; //String que guardará el tipo de la entidad que es clave en caso de que se dé.
						
						Iterator<Vector<Atributo>> itera= enearia.referencias.values().iterator();
						//Tengo los vectores de atributos.
						while(itera.hasNext()){
							
							Iterator<Atributo> i= itera.next().iterator();
							//Tengo los atributos.
							
							int contando=0;//Lleva la cuenta de los atributos que se meten en el hash de todas como claves.
							
							while(i.hasNext()){
								Atributo at= i.next();
								
								//Saco la clave de la entidad del atributo pues es eso lo q se traerá como foránea.
								Iterator<Atributo> x = entidades.get(at.tipo).clave.values().iterator();
								//Hash que tendrá la copia de la clave.
								HashMap<String, Atributo> copiaClave= new HashMap<String, Atributo>();
								
								
								while(x.hasNext()){
									Atributo atActual= (Atributo)x.next().clone();
									copiaClave.put(atActual.nombre, atActual);
								}
								
								
								if (at.maxOccurs==1) {
									hayMax1=true;
									//Es clave o es unico.
									if (!hayClave) {
										hayClave= true;
										//Coloco como clave la copia.
										enearia.clave= copiaClave;
										tipoEntidadClave= at.tipo;
									}
									else{
										//Ya hay clave pero este es unico.
										enearia.unike.add(copiaClave.values());
										//Meto los atributos como atributos de la entidad.
									}
									
									//Agrego el atributo como foráneo.
									enearia.AgregarForaneoSinDuplicar(at.tipo, copiaClave.values());
								}
								else{
									
									//Agrego el atributo como foráneo.
									enearia.AgregarForaneoSinDuplicar(at.tipo, copiaClave.values());
									
									if (!hayMax1) {
										//Voy llenando el hash por si ninguna es de max1
										
										//Recorro la copia de la clave para meterla en el hash.
										Iterator<Atributo> t= copiaClave.values().iterator();
										while(t.hasNext()){
											Atributo atributico= t.next();
											
											if (claveContodas.containsKey(atributico.nombre)) {
												//Ya se metio este atributo en la clave por ende debo cambiarle el nombre
												//pa colocarlo de nuevo.
												atributico.nombre= atributico.nombre+contando;
												contando++;
											}
											claveContodas.put(atributico.nombre, atributico);
										}
									}
								}
								
								
								
							}
						}
						
						if(!hayClave){
							//Si al salir aun no hay clave TODAS deben ser clave.
							//Se coloca el hash auxiliar como hash de claves.
							enearia.clave=claveContodas;
						}
						else{
							//Debo pasar todo foráneo que no este en clave para atributos para que se impriman.
							
							//La clave es una sola o son todas... nunca habran convinaciones raras. Aqui ajuro es una sola.
							
							//Saco cual es el tipo de la entidad que conforma la clave.
							System.out.println("El tipo de la entidad clave es  "+ tipoEntidadClave);
						
							Iterator<String> itr= enearia.foraneo.keySet().iterator();
							while(itr.hasNext()){
								String entidadForanea= itr.next();
								
								if (entidadForanea.equals(tipoEntidadClave)) {
									//Aqui esta la clave.. a ella no hay q pasarla.
									 
									//Si el vector de Vectores tiene mas de uno significa que hay otro atributo de la
									//misma entidad que no es clave y si hay que pasarlo.
									Vector<Vector<Atributo>> vector =enearia.foraneo.get(tipoEntidadClave);
									
									if(vector.size()>1){
										//Hay mas atributos de esta entidad que debo pasar como atributo.
										//OJO: pilla que agrego desde el 1 y no desde 0 pues el 0 es la clave.
										
										for (int g = 1; g < vector.size(); g++) {
											//Agrego.
											Iterator<Atributo> h= vector.elementAt(g).iterator();
											while(h.hasNext()){
												Atributo pasante= h.next();
												System.out.println("estoy pasando el atributo "+ pasante.nombre+  "con ");
												
												enearia.atributos.add(pasante);
												//OJO: estoy pasando por referencia, no estoy clonando.
											}
										}
									}
									
								} else {
									//No es de la clave por ende lo paso al atributo.
									
									//Saco los vectores del foráneo respectivos
									Iterator<Vector<Atributo>> y= enearia.foraneo.get(entidadForanea).iterator();
									while(y.hasNext()){
										Vector<Atributo> vectorForaneo= y.next();
										//Debo voltearlo para q se imprima bien.
										Iterator<Atributo> z= vectorForaneo.iterator();
										//Recorro el vector seleccionado.
										while(z.hasNext()){
											//Agrego.
											Atributo pasante= z.next();
											System.out
											.println("estoy pasando el atributo "+ pasante.nombre+  "como atributo.");
											enearia.atributos.add(pasante);
											//OJO: estoy pasando por referencia, no estoy clonando.
										}
									}

								}
								
							}
							
						}
						
						//Elimino el hash de referencias para que no exista problemas con "VerInterrelaciones"
						enearia.referencias.clear();
						
					}
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * Función que permite pasar los atributos de una clave como atributos propios de la
	 * relacion, esto para poderlos imprimir en caso de que las foráneas no se deban imprimir.
	 * 
	 * @param atrs Collection de Atributos a pasar 
	 * @param ent Entidad donde se pasarán los atributos.
	 */
	public static void MeterAtributos(Collection<Atributo> atrs, Entidad ent) {
		
		//Recorro toda la colección para meter los atributos.
		Iterator<Atributo> i= atrs.iterator();
		while(i.hasNext()){
			ent.atributos.add(i.next());
		}
	}
	
	
	
	/**
	 * Analiza cual entidad absorbe a cual, de acuerdo a las reglas de la cardinalidad y
	 * participacion. (1:1,1:N,M:N)
	 *
	 * @param atr1 Atributo que referencia a una de las entidades.
	 * @param atr2 Atributo que referencia a la otra entidad relacionada.
	 */
	public static void DefInterrelacion(Atributo atr1, Atributo atr2){
		int min1= atr1.minOccurs;
		int max1= atr1.maxOccurs;
		int min2= atr2.minOccurs;
		int max2= atr2.maxOccurs;

		if (min1==1 && max1==1) {
			//atr1 es 1:1
			//Entidad del tipo atr2 absorbe a Entidad del atributo tipo atr1
			// System.out.println("el atributo "+ atr1.nombre+" tiene min y max 1:1, " +
			// "por ende "+ entidades.get(atr2.tipo).nombre_entidad +" absorbe a "+
			// entidades.get(atr1.tipo).nombre_entidad+"\n");
			Entidad ent1= entidades.get(atr1.tipo);
			entidades.get(atr2.tipo).AgregarForaneo(ent1.tipo,ent1.clave.values());//Entidad que absorbe.
		}
		else if (min2==1 && max2==1)
		{
			//atr2 es 1:1
			//Entidad del tipo atr1 absorbe a Entidad del atributo tipo atr2
			// System.out.println("el atributo "+ atr2.nombre+" tiene min y max 1:1, " +
			// "por ende "+ entidades.get(atr1.tipo).nombre_entidad +" absorbe a "+
			// entidades.get(atr2.tipo).nombre_entidad+"\n");
			Entidad ent2=entidades.get(atr2.tipo);
			entidades.get(atr1.tipo).AgregarForaneo(ent2.tipo,ent2.clave.values());//Entidad que absorbe.
		}
		else if(min1==0 && max1==1)
		{
			if (min2==0 && max2==1) {
				//atr1 es 0:1 y atr2 es 0:1
				// System.out.println("Ambos atributos ("+atr1.nombre+ " , "+ atr2.nombre+") tienen 0:1"+
				// "por ende se crea una nueva entidad con uno de los dos como clave.");
				//se crea una nueva entidad, donde uno de los dos sera clave y el otro ser alterno.
				Entidad entNueva= CrearEntidadNueva(atr1);

				//Coloco el otro atributo como un atributo foraneo.
				Entidad ent2= entidades.get(atr2.tipo);
				entNueva.AgregarForaneo(ent2.tipo,ent2.clave.values());

				entidades.put(entNueva.tipo, entNueva);

			} else {
				// atr1 es 0:1 y atr2 es 0:N o 1:N
				// System.out.println("El atributo "+ atr1.nombre+" tiene 0:1, pero el atributo "+ atr2.nombre+
				// "tiene 0:N o 1:N\n");
				// Se crea una nueva entidad donde atr1 sea clave y lo otro sea atributo normal.
				Entidad nueva= CrearEntidadNueva(atr1);

				//Coloco atr2 como foraneo.
				Entidad ent2= entidades.get(atr2.tipo);
				nueva.AgregarForaneo(ent2.tipo,ent2.clave.values());

				entidades.put(nueva.tipo, nueva);
			}
		}
		else if(min2==0 && max2==1){
			//atr2 es 0:1, y atr1 ajuro debe ser 0:N o 1:N
			// System.out.println("El atributo "+atr2.nombre+" es 0:1 pero el atributo "+ atr1.nombre+ " es 0:N o 1:N"+
			// "por ende se debe crear una nueva entidad cuya clave sea la de la entidad "+ entidades.get(atr2.tipo).nombre_entidad);
			//Se crea una nueva entidad donde atr2 sea clave y lo otro sea atributo normal.
			Entidad nueva=CrearEntidadNueva(atr2);

			//coloco a atr1 como foraneo.
			Entidad ent1= entidades.get(atr1.tipo);
			nueva.AgregarForaneo(ent1.tipo,ent1.clave.values());

			entidades.put(nueva.tipo, nueva);
		}
		else {
			// System.out.println("Caso M:N Se crea una nueva entidad con clave de ambas entidades\n");

			//M:N

			// Se crea una nueva entidad con clave siendo la compuesta de los dos atr.
			Entidad nueva= CrearEntidadNueva(atr1);

			Collection<Atributo> keys=entidades.get(atr2.tipo).clave.values();
			Iterator<Atributo> h=keys.iterator();
			while(h.hasNext()){
				//Clone cada atributo de la clave de la entidad de atr2 y la meti en el vector clave de la entidad nueva.
				Atributo atr= h.next();
				nueva.clave.put(new String(atr.nombre),(Atributo)atr.clone());
			}

			//Ahora metere esta clave como foraneo!
			nueva.AgregarForaneo(atr2.tipo, keys);

			entidades.put(nueva.tipo, nueva);
		}
	}

	/**
	 * Crea una nueva entidad que en realidad es una nueva tabla que permite representar una interrelacion.
	 * Le coloca el nombre y el tipo segun el atributo dado y le pasa la clave de la entidad del atributo dado.
	 *
	 * @param atr1 Atributo con datos para la nueva entidad
	 * @return Entidad formada.
	 */
	public static Entidad CrearEntidadNueva(Atributo atr1){
		Entidad entNueva= new Entidad();
		Entidad entidadAtr1= entidades.get(atr1.tipo);// Entidad de Atributo1
		
		entNueva.imprimir= false;
		//Coloco el nombre de la entidad
		entNueva.nombre_entidad= atr1.nombre;

		//Coloco el tipo de la entidad.
		entNueva.tipo=atr1.nombre;

		//Debo copiar y clonar el hash de clave para evitar paso por referencia.
		Collection<Atributo> claves=entidadAtr1.clave.values();
		Iterator<Atributo> g= claves.iterator();
		while(g.hasNext()){
			Atributo atr= g.next();
			entNueva.clave.put(new String(atr.nombre),(Atributo)atr.clone());
		}

		//Meto la clave ahora en los foraneos, recordemos que pertenecen a otra tabla.
		entNueva.AgregarForaneo(atr1.tipo,claves);

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
	public static void ParsearXMLSchema(String archivo, int opcionTrad) throws SAXException, IOException {

		File file = new File(archivo);
		opcionTraduccion = opcionTrad;
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

				// ComplexTypes (Del nivel más externo)
				Map<String, XSComplexType> mapa = (Map<String, XSComplexType>) schema.getComplexTypes();
				//System.out.print("Tamano: " + ((Map<String, XSComplexType>) mapa).size() + "\n");
				Iterator<String> claves1 = ((Map<String, XSComplexType>) mapa).keySet().iterator();
				Iterator<XSComplexType>valores1 =((Map<String, XSComplexType>) mapa).values().iterator();

				HashMap<String, XSComplexType> nuevo_mapa = new HashMap<String, XSComplexType>();
				//Nos quedamos solo con los complexType que definen a entidades
				while (claves1.hasNext() && valores1.hasNext())
				{
					String tipo = (String) claves1.next();
					XSComplexType complex = (XSComplexType) valores1.next();
					if(!entidades.containsKey(tipo))
						complexSinEntidad.put(tipo, complex);
					else
						nuevo_mapa.put(tipo, complex);
				}
				
				if (!enearias.isEmpty()) {
					Iterator<String> ene= enearias.keySet().iterator();
					while(ene.hasNext()){
						String enearia= ene.next();
						nuevo_mapa.put(enearia, enearias.get(enearia));
					}
				}
				
				claves1 = ((Map<String, XSComplexType>) nuevo_mapa).keySet().iterator();
				valores1 =((Map<String, XSComplexType>) nuevo_mapa).values().iterator();
				LeerAtributosEntidades(claves1, valores1);
				VerEnearias();
				VerInterrelaciones();
				ImprimirEntidades();

			}
		} catch (Exception exp) {
			ReportarError("**ERROR**\n	XML SCHEMA INVALIDO.\n");
			exp.printStackTrace(System.out);
		}
		EscribirScript();
	}

}