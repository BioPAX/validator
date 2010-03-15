package org.biopax.miriam;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
//import javax.xml.validation.Schema;
//import javax.xml.validation.SchemaFactory;
//import javax.xml.XMLConstants;

import net.biomodels.miriam.Miriam;
import net.biomodels.miriam.Resource;
import net.biomodels.miriam.Resources;
import net.biomodels.miriam.Synonyms;
import net.biomodels.miriam.Uri;
import net.biomodels.miriam.UriType;
import net.biomodels.miriam.Uris;
import net.biomodels.miriam.Miriam.Datatype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MiriamLink
{
	private static final Log log = LogFactory.getLog(MiriamLink.class);
	private static final String[] ARRAY_OF_STRINGS = {}; // a template to convert a Collection<T> to String[]
	
	
	/** default address to access to the services */
	public static final String DEFAULT_ADDRESS = "http://www.ebi.ac.uk/miriam/main/XMLExport";
	/** package name for jaxb to use */
	public static final String BINDING = "net.biomodels.miriam";
	public static final String SCHEMA_LOCATION = "http://www.ebi.ac.uk/compneur-srv/miriam/static/main/xml/MiriamXML.xsd";

	private String address;
    
    /** object of the generated from the Miriam schema type */
    private Miriam miriam;
    
	
	/**
	 * Default constructor: initialisation of some parameters
	 */
	public MiriamLink() {
		this.address = DEFAULT_ADDRESS;
		init();
    }
	
	
	void init()
	{
		if(log.isInfoEnabled()) {
			log.info("Getting the latest Miriam XML...");
			log.info("Address used: " + DEFAULT_ADDRESS);
		}
		try
	    {
			String query = URLEncoder.encode("fileName=Miriam.xml", "UTF-8");
			URL url = new URL(address);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(query);
			wr.flush();
            JAXBContext jc = JAXBContext.newInstance(BINDING);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            //SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            //Schema schema = schemaFactory.newSchema(new URL(SCHEMA_LOCATION));
            //unmarshaller.setSchema(schema);
            this.miriam = (Miriam) unmarshaller.unmarshal(conn.getInputStream());

            if (log.isDebugEnabled()) {
	            log.debug("MIRIAM XML imported, version: "
	                + miriam.getDataVersion() + ", datatypes: "
	                + miriam.getDatatype().size());
	        }
	    }
	    catch (Exception e)
	    {
	        throw new RuntimeException(e);
	    }
	}

	
	/**
     * Retrieves the current version of MIRIAM Web Services.  
     * @return Current version of the Web Services
	 */
    public String getServicesVersion()
    {
        return miriam.getDate().toString() 
        	+ "; " +  miriam.getDataVersion().toString();
    }
       
     
    /**
     * Retrieves the unique (official) URI of a data type (example: "urn:miriam:uniprot").
     * @param name name, synonym, or deprecated URI (URN or URL) of a data type (examples: "UniProt")
     * @return unique URI of the data type
     */
    public String getDataTypeURI(String name)
    {
    	Datatype datatype = getDatatype(name);
    	return getOfficialDataTypeURI(datatype); 
    }
     
     
    /**
     * Retrieves all the URIs of a data type, including all the deprecated ones 
     * (examples: "urn:miriam:uniprot", "http://www.uniprot.org/", "urn:lsid:uniprot.org:uniprot", ...).
     * 
     * @param name name (or synonym) or deprecated URI (URN or URL) of the data type (examples: "ChEBI", "UniProt")
     * @return all the URIs of a data type (including the deprecated ones)
     */
    public String[] getDataTypeURIs(String name)
    {
       	Set<String> alluris = new HashSet<String>();
    	Datatype datatype = getDatatype(name);
    	for(Uris uris : datatype.getUris()) {
    		for(Uri uri : uris.getUri()) {
    			alluris.add(uri.getValue());
    		}
    	}
    	return alluris.toArray(ARRAY_OF_STRINGS);
    }
	
	
	/**
	 * Retrieves the location (or country) of a resource (example: "United Kingdom").
	 * @param id identifier of a resource (example: "MIR:00100009")
	 * @return the location (the country) where the resource is managed
	 */
	public String getResourceLocation(String id)
	{
		Resource resource = getResource(id);
    	return resource.getDataLocation();
	}
	
	
	/**
	 * Retrieves the institution which manages a resource (example: "European Bioinformatics Institute").
	 * @param id identifier of a resource (example: "MIR:00100009")
	 * @return the institution managing the resource
	 */
	public String getResourceInstitution(String id)
	{
		Resource resource = getResource(id);
    	return resource.getDataInstitution();
	}
	  
    
    /**
     * Retrieves the unique MIRIAM URI of a specific entity (example: "urn:miriam:obo.go:GO%3A0045202").
     * @param name name of a data type (examples: "ChEBI", "UniProt")
     * @param id identifier of an enity within the data type (examples: "GO:0045202", "P62158")
     * @return unique MIRIAM URI of a given entity
     */
    public String getURI(String name, String id)
    {
    	Datatype datatype = getDatatype(name);
        return getOfficialDataTypeURI(datatype) 
        	+ ":" + URLEncoder.encode(id);
    }
    
    
    /**
	 * Retrieves the definition of a data type.
	 * @param nickname name or URI (URN or URL) of a data type
	 * @return definition of the data type
	 */
    public String getDataTypeDef(String nickname)
    {
    	Datatype datatype = getDatatype(nickname);
    	return datatype.getDefinition();
    }
       
     
    /**
     * Retrieves the physical locationS (URLs) of web pageS providing knowledge about an entity.
     * @param nickname name (can be a synonym) or URI of a data type (examples: "Gene Ontology", "UniProt")
     * @param id identifier of an entity within the given data type (examples: "GO:0045202", "P62158")
     * @return physical locationS of web pageS providing knowledge about the given entity
     */
    public String[] getLocations(String nickname, String id)
    {
       	Set<String> locations = new HashSet<String>();
		Datatype datatype = getDatatype(nickname);
		Resources resources = datatype.getResources();
		if (resources != null) {
			for (Resource resource : resources.getResource()) {
				String link = resource.getDataEntry();
				link = link.replaceFirst("\\$id", URLEncoder.encode(id));
				locations.add(link);
			}
		}
    	return locations.toArray(ARRAY_OF_STRINGS);
    }
       
    
    /**
     * Retrieves all the physical locations (URLs) of the services providing the data type (web page).
     * @param nickname name (can be a synonym) or URL or URN of a data type name (or synonym) or URI (URL or URN)
     * @return array of strings containing all the address of the main page of the resources of the data type
	 */
    public String[] getDataResources(String nickname)
    {
       	Set<String> locations = new HashSet<String>();
    	Datatype datatype = getDatatype(nickname);
    	Resources resources = datatype.getResources();
		if (resources != null) {
			for (Resource resource : resources.getResource()) {
				String link = resource.getDataResource();
				locations.add(link);
			}
		}
    	return locations.toArray(ARRAY_OF_STRINGS);
    }
    
    
    /**
	 * To know if a URI of a data type is deprecated.
	 * @param uri (URN or URL) of a data type
	 * @return answer ("true" or "false") to the question: is this URI deprecated?
	 */
    public boolean isDeprecated(String uri)
    {
    	Datatype datatype = getDatatype(uri);
    	String urn = getOfficialDataTypeURI(datatype);
    	return !uri.equalsIgnoreCase(urn);
    }
    
   
    /**
     * Retrieves the pattern (regular expression) used by the identifiers within a data type.
     * @param nickname data type name (or synonym) or URI (URL or URN)
     * @return pattern of the data type
	 */
    public String getDataTypePattern(String nickname)
    {
    	Datatype datatype = getDatatype(nickname);
    	return datatype.getPattern();
    }
    
    
    /**
     * Retrieves all the synonym names of a data type (this list includes the original name).
     * @param name name or synonym of a data type
     * @return all the synonym names of the data type
	 */
    public String[] getDataTypeSynonyms(String name)
    {
    	Datatype datatype = getDatatype(name);
    	Synonyms synonyms = datatype.getSynonyms();
    	return (synonyms != null) ? synonyms.getSynonym().toArray(ARRAY_OF_STRINGS) : ARRAY_OF_STRINGS;
    }
    
    
    /**
	 * Retrieves the common name of a data type.
	 * @param uri URI (URL or URN), or nickname of a data type
	 * @return the common name of the data type
	 */
    public String getName(String uri)
    {
    	Datatype datatype = getDatatype(uri);
    	return datatype.getName();
    }
    
    
    /**
	 * Retrieves all the names (with synonyms) of a data type.
	 * @param uri URI (URL or URN) of a data type
	 * @return the common name of the data type and all the synonyms
	 */
    public String[] getNames(String uri)
    {
    	Set<String> names = new HashSet<String>();
    	Datatype datatype = getDatatype(uri);
    	names.add(datatype.getName());
    	for(String name : datatype.getSynonyms().getSynonym()) {
    		names.add(name);
    	}
    	return names.toArray(ARRAY_OF_STRINGS);
    }
    
    /**
     * Retrieves the list of names of all the data types available.
     * @return list of names of all the data types
     */
    public String[] getDataTypesName()
    {
        Set<String> dataTypeIds = new HashSet<String>();
        for(Datatype datatype : miriam.getDatatype()) {
        	dataTypeIds.add(datatype.getName());
        }
        return dataTypeIds.toArray(ARRAY_OF_STRINGS);
    }
    
    
    /**
     * Retrieves the internal identifier (stable and perennial) of all the data types (for example: "MIR:00000005").
     * @return list of the identifier of all the data types
     */
    public String[] getDataTypesId()
    {
        Set<String> dataTypeIds = new HashSet<String>();
        for(Datatype datatype : miriam.getDatatype()) {
        	dataTypeIds.add(datatype.getId());
        }
        return dataTypeIds.toArray(new String[] {});
    }
    
    
    /**
     * Retrieves the official URI (it will always be URN) of a data type corresponding to the deprecated one.
     * @param uri deprecated URI (URN or URL) of a data type 
     * @return the official URI of a data type corresponding to the deprecated one
     * @deprecated use getDataTypeURI instead
     */
    public String getOfficialDataTypeURI(String uri)
    {
        return getDataTypeURI(uri);
    }
    
    
    /**
     * Checks if the identifier given follows the regular expression of its data type (also provided).
     * @param identifier internal identifier used by the data type
     * @param datatype name, synonym or URI of a data type
     * @return "true" if the identifier follows the regular expression, "false" otherwise
     */
    public boolean checkRegExp(String identifier, String datatype)
    {
    	Datatype dt = getDatatype(datatype);
    	Pattern pattern = Pattern.compile(dt.getPattern());
    	return pattern.matcher(identifier).find();
    } 
        

    /**
     * Retrieves the unique (official) URI of a data type (example: "urn:miriam:uniprot").
     * @param datatype net.biomodels.miriam.Miriam.Datatype
     * @return
     */
    public String getOfficialDataTypeURI(Datatype datatype) {
    	for(Uris uris : datatype.getUris()) {
    		for(Uri uri : uris.getUri()) {
    			if(!isDeprecated(uri) && uri.getType() == UriType.URN) {
    				return uri.getValue();
    			}
    		}
    	}
    	
    	return null;
    }
    
    
    private boolean isDeprecated(Uri uri) {
		if(uri.isDeprecated()!=null && uri.isDeprecated()) {
			return true;
		} else {
			return false;
		}
	}


	/**
     * Gets Miriam Datatype by its ID, Name, Synonym, or URI (URN/URL) 
     * 
     * @param datatype
     * @return
     */
	public Datatype getDatatype(String datatype) {
		
		// name or id (quick scan)?
		for(Datatype dt : miriam.getDatatype()) {
			if(dt.getName().equalsIgnoreCase(datatype) 
					|| dt.getId().equalsIgnoreCase(datatype)) {
				return dt;
			}
		}
		
		// well, otherwise it'll take more time ;)
		for(Datatype dt : miriam.getDatatype()) {
			// synonym name?
			Synonyms synonyms = dt.getSynonyms();
			if (synonyms != null) {
				for (String s : synonyms.getSynonym()) {
					if (s.equalsIgnoreCase(datatype)) {
						return dt;
					}
				}
			}
			
			// URI?
			URI datatypeUri = URI.create(datatype);
			for(Uris uris : dt.getUris()) {
				for(Uri uri : uris.getUri()) {
					if(datatypeUri.equals(URI.create(uri.getValue()))) {
						return dt;
					}
				}
			}	
		}
		
		throw new IllegalArgumentException("Datatype not found : " + datatype);
	}


    /**
     * Retrieves the internal identifier (stable and perennial) of all the resources (for example: "MIR:00100008" (bind) ).
     * @return list of the identifier of all the data types
     */
    public String[] getResourcesId()
    {
        Set<String> ids = new HashSet<String>();
        for(Datatype datatype : miriam.getDatatype()) {
        	Resources resources = datatype.getResources();
			if (resources != null) {
				for (Resource resource : resources.getResource()) {
					ids.add(resource.getId());
				}
			}
        }
        return ids.toArray(new String[] {});
    }
	
    
    /**
     * Retrieves the resource by id (for example: "MIR:00100008" (bind) ).
     * @return list of the identifier of all the data types
     */
    public Resource getResource(String id)
    {
        for(Datatype datatype : miriam.getDatatype()) {
			Resources resources = datatype.getResources();
			if (resources != null) {
				for (Resource resource : resources.getResource()) {
					if (resource.getId().equalsIgnoreCase(id)) {
						return resource;
					}
				}
			}
        }
        
        throw new IllegalArgumentException("Resource not found : " + id);
    }

    
	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}
}
