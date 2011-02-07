package accions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

public class Parser {
	
	static HashMap<String, Entidad> entidades = new HashMap<String, Entidad>();

	public static XSSchemaSet CrearParser(File archivo) throws SAXException,
			IOException {
		XSOMParser parser = new XSOMParser();
		parser.parse(archivo);
		return parser.getResult();
	}

	public static Vector<Atributo> restricciones(XSRestrictionSimpleType restriction, Atributo atributo,Vector<Atributo> atributos) {
		String id = "ID";		
		//Se verifica los tipos claves y los tipos setteados en las resticciones
		if (restriction.getBaseType().getName() != "anySimpleType"){
			if (!(atributo.getTipo()==null) && !(atributo.getTipo().equals(id))){

				atributo.setTipo(restriction.getBaseType().getName()); atributos.add(atributo);}
			else if ((atributo.getTipo()==null)){atributo.setTipo(restriction.getBaseType().getName()); atributos.add(atributo);}
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
					System.out.println("ADVERTENCIA: el campo "+facet.getValue().value+ "perteneciente a la restricción pattern value es inválido en el modelo ER");
				}

			}
		}return atributos;
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
		String id = "ID";
		String nombreAttr;
		
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
		
		
		Entidad entidad = entidades.get(tipo); // Entidad en donde se encuentran estos elementos
		String tipoAttr=null;
		
		for (XSParticle p : particles) {
			Atributo nuevo_atributo = new Atributo();
			pterm = p.getTerm();
			
			
			if (pterm.isElementDecl()) { // xs:element inside complex type
				// Se verifica si tiene SimpleType y Restricciones
				
				// Se obtiene el nombre del atributo
				nombreAttr = pterm.asElementDecl().getName();
				
				// Se obtiene el tipo del atributo
				tipoAttr = pterm.asElementDecl().getType().getName();
			
				// Se verifica si existe un valor por defecto
				if(!(pterm.asElementDecl().getDefaultValue()== null))
				{	
					nuevo_atributo.setValor(pterm.asElementDecl().getDefaultValue().toString());
				}
				
				nuevo_atributo.setNombre(nombreAttr);
				nuevo_atributo.setTipo(tipoAttr);
			
				//Se obtienen las retricciones
				if (pterm.asElementDecl().getType().isSimpleType()) {
					// System.out.println("Tiene Restriccion : "+pterm.asElementDecl().getType().asSimpleType().isRestriction());
					// Se verifican las restricciones existentes
					restriction = pterm.asElementDecl().getType().asSimpleType().asRestriction();
					atributos = restricciones(restriction,nuevo_atributo,atributos);
					
				}
				
				//Se obtiene el minOccurs y maxOccurs, así como si el atributo es null o not null
				if (p.getMinOccurs() == 1) {
					nuevo_atributo.setNulo(false);
				}
				nuevo_atributo.setMinOccurs(p.getMinOccurs());
				nuevo_atributo.setMaxOccurs(p.getMaxOccurs());
						
				//Coloca el atributo en el vector al que pertenece
				// si es nulo ver si es elemento?

				if (tiposBasicos.contains(tipoAttr)){
					atributos.add(nuevo_atributo);

					//Se verifica si el atributo es clave y se coloca la clave en la entidad
					if ((tipoAttr.equals(id))){

						entidad.setClave(nuevo_atributo.getNombre());
					}
				}
				else{
					if(entidades.containsKey(tipoAttr)) {

						referencias.add(nuevo_atributo);
					} 
					else{
						System.out.println("ERROR: el atributo "+ nombreAttr +" de la entidad "+ entidad.getNombre_entidad()+ " es de un tipo que no existe");
					}

				}

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
				valores.next();
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
	 * Método que se encarga de crear el archivo sql correspondiente al xml 
	 * schema proporcionado.
	 * 
	 */
	public static void EscribirScript(){
		
		Set<String> tipos = entidades.keySet();
		Iterator<String> cadaTipo = tipos.iterator();
		Entidad entidad = new Entidad();
		Vector<Atributo> atributos = new Vector<Atributo>();
		Vector<Atributo> referencias = new Vector<Atributo>();
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
				referencias = entidad.getReferencias();
				
				j= entidad.getReferencias().size()-1;
				//Se agregan los atributos que hacen referencias en la entidad
				while (j >= 0) {
					out.write("	"+referencias.get(j).getNombre().toUpperCase()+
					"	"+ referencias.get(j).getTipo().toUpperCase() +"	"+
					Nulidad(referencias.get(j))+" ,\n");
					j--;
				}
				
				
				j = entidad.getReferencias().size()-1;
				
				//Se agregan los contraints de clave foranea a la entidad.
				while (j >= 0) {
					out.write("	FOREIGN KEY "+"( "+referencias.get(j).getNombre().
					toUpperCase()+" )"+" REFERENCES "+ "( "+entidades.
					get(referencias.get(j).getTipo()).nombre_entidad.
					toUpperCase()+" )"+" ,\n");
					j--;
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
				toUpperCase()+ " PRIMARY KEY "+ entidad.clave.
				toUpperCase()+" );\n\n");
				
			}  
		    //Se cierra el output de escritura en el archivo sql
		    out.close();
		// Se toma la exception si existe
		}catch (Exception e){
		      System.err.println("Error: " + e.getMessage());
		    }
	}
	
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
			Vector<Atributo> referencias = entidad.getReferencias();
			System.out.println("-- Atributos Hechos por el usuario--");
			
			j= entidad.getReferencias().size()-1;
			
			while (j >= 0) {
				System.out.println("	Nombre : " + referencias.get(j).getNombre());
				System.out.println("		Tipo : " + referencias.get(j).getTipo());
				System.out.println("		Nulo : " + referencias.get(j).isNulo());
				System.out.println("		MinOccurs : " + referencias.get(j).getMinOccurs());
				System.out.println("		MaxOccurs : " + referencias.get(j).getMaxOccurs());

				j--;
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
		Vector<String> EntidadesVisitadas=new Vector<String>(); //Permite saber que Entidades ya fueron vistas para no caer en ciclos.
		
		// Recorrere el hash de las entidades para ir viendo el vector de referencias de cada una
		// y asi ir sacando las interrelaciones.
		
		Set<String> claves = entidades.keySet();
		
		
		Iterator<String> itr = claves.iterator();
		
		//Recorro entidades.
		while(itr.hasNext()){
			
			String tipoEntidad= itr.next();
			EntidadesVisitadas.add(tipoEntidad);
			Entidad ent = entidades.get(tipoEntidad); //Entidad a estudiar.
			Vector<Atributo> referencias = ent.getReferencias(); // Referencias que tiene esa entidad.
			
			Iterator<Atributo> ref= referencias.iterator();
			// Recorro cada atributo que hace referencia en la entidad.
			
			while(ref.hasNext()){
				Atributo atributo = ref.next();//Atributo a estudiar.
				String tipo= atributo.getTipo(); // Tipo de ese atributo
				Entidad enti= entidades.get(tipo);  // Entidad relacionada con ent.
				
				int minOccur= atributo.getMinOccurs();
				int maxOccur= atributo.getMaxOccurs();
				
				
				int minOccurRef;
				int maxOccurRef;
				//Aqui se deberían verificar cosas como si min y max son cero, dar error, si min es 1 y max es cero tambien.
				//De hecho no se deberían permitir guardar en referencias atributos con errores en los min y max.
				
				if (minOccur + maxOccur == 2) { //Es decir q el min y el max son 1, por lo tanto absorbe
					AgregarForaneo(ent,enti);
				}
				else if (enti.getNombre_entidad().equals(ent.getNombre_entidad())) {
					/*
					 * Es una entidad que se relaciona consigo misma, quedamos que si tiene (1,1) se absorbe pero eso 
					 * ya lo veo arriba, ahora como no es (1,1) ajuro creo una entidad nueva para la interrelacion.
					 */
					Entidad entidadNueva= new Entidad();
					/*
					 * Debo llenar los datos de la entidadNueva pero como saco la clave? si esta es la unión 
					 * de la clave consigo misma.
					 * Qué atributos le coloco? será que busco el atributo que es clave? y se lo paso como atributo?
					 * no le coloco atributos?
					 */
					
				}
				else
				{
					Vector<Atributo> refsEnti= enti.getReferencias();//referencias de la enti.
					Iterator<Atributo> refEnti = refsEnti.iterator();
					Vector<Atributo> refDelmismoTipo= new Vector<Atributo>();//Vector q guardará los atributos que tengan el mismo tipo.
					
					//Recorro el vector de referencias de enti para encontrar referencia circular.
					while(refEnti.hasNext()){
						Atributo atrRef = refEnti.next(); //Atributo de enti a estuar.
						String tipoRef= atrRef.getTipo(); //Tipo del atributo.
						
						if (tipoRef.equals(tipo)) {
							refDelmismoTipo.add(atrRef);
							
						}
					}
					
					Atributo at;
					if (refDelmismoTipo.isEmpty()) {
						//No se consiguio un atributo que referencie a ent por ende no hay referencia circular, hay error.
						System.out.println("ERROR: No existe referencia circular entre la entidad "+ ent.getNombre_entidad()+" y la entidad "+enti.getNombre_entidad());
					}else if (refDelmismoTipo.size()>= 2) {
						//Quiere decir hay mas de una interrelacion entre las entidades, por ende debo buscar por nombre.
						Iterator<Atributo> iter= refDelmismoTipo.iterator();
						boolean hayRef= false;
						while (iter.hasNext()) {
							at = iter.next();
							if (at.getNombre().equals(atributo.getNombre())) {
								//Son los atributos correspondientes.
								hayRef=true;
								minOccurRef= at.getMinOccurs();
								maxOccurRef= at.getMaxOccurs();
								
								//LLAMO A UNA RUTINA Q VEA LOS 1,1 - 1,0 - M,N
							}
							
						}
						if (!hayRef) {
							//A pesar de haber atributos del mismo tipo, ocurre que ninguno se llama igual al atributo de ent. Por ende 
							//no hay referencia.
							System.out.println("ERROR: No existe referencia circular entre la entidad "+ ent.getNombre_entidad()+" y la entidad "+enti.getNombre_entidad());							
						}
					}
					else{
						//Existe una unica referencia con este tipo, por ende no debo ver si el nombre es igual ni nada.
						at = refDelmismoTipo.get(0);
						minOccurRef= at.getMinOccurs();
						maxOccurRef= at.getMaxOccurs();
						
						//LLAMO A UNA RUTINA Q VEA LOS 1,1 - 1,0 - M,N
					}
				}
			}
		}
		
	}

	/**
	 * Permite insertar en la Entidad base los datos de otra entidad foránea
	 * a la cual esta relacionada.
	 * @param entidadBase Entidad que absorbe a la entidadForánea
	 * @param entidadForanea Entidad cuyos datos serán colocados en la Entidad base.
	 */
	public static void AgregarForaneo(Entidad entidadBase, Entidad entidadForanea){
		
		ArrayList<String> tupla= new ArrayList<String>();//Genero la tupla a insertar
		tupla.add(entidadForanea.getNombre_entidad()); // Introduzco el nombre
		tupla.add(entidadForanea.getClave()); //Introduzco la clave.
		entidadBase.AgregarForaneo(tupla);
		
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
												 
				// ComplexTypes (al menos los del nivel más externo
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
		EscribirScript();
	}
}
