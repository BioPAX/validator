package org.biopax.psidev.ontology_manager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

/**
 * Central access to configured OntologyAccess.
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baderlab.org) - re-factored for the BioPAX Validator
 * @since 2.0.0
 */
public class OntologyManagerImpl implements OntologyManager {

  private static final Log log = LogFactory.getLog(OntologyManagerImpl.class);
  private static final ResourceLoader LOADER = new DefaultResourceLoader();

  /**
   * The Map that holds the Ontologies.
   * The key is the ontology ID and the value is a ontology inplementing the OntologyAccess interface.
   */
  private Map<String, OntologyAccess> ontologyMap;

  /**
   * Create a new OntologyManagerImpl with no configuration (no associated ontology).
   */
  public OntologyManagerImpl() {
    ontologyMap = new HashMap<>();
  }

  /**
   * Creates a new OntologyManagerImpl managing the ontology specified in the config map.
   *
   * @param cfg configuration properties for the manager (ID=resource_location).
   * @throws OntologyLoaderException when the configuration could not be parsed or loading of an ontology failed.
   */
  public OntologyManagerImpl(Properties cfg) throws OntologyLoaderException {
    this();
    loadOntologies(cfg);
    log.debug("Successfully configured OntologyManagerImpl.");
  }

  public void putOntology( String ontologyID, OntologyAccess ontologyAccess ) {
    if ( ontologyMap.containsKey( ontologyID ) ) {
      log.warn( "OntologyAccess with the ID '" + ontologyID + "' already exists. Overwriting!" );
    }
    ontologyMap.put( ontologyID, ontologyAccess );
  }

  public Set<String> getOntologyIDs() {
    return ontologyMap.keySet();
  }

  public OntologyAccess getOntology( String ontologyID ) {
    return ontologyMap.get( ontologyID );
  }

  public boolean containsOntology( String ontologyID ) {
    return ontologyMap.containsKey( ontologyID );
  }

  public void loadOntologies( Properties config )
    throws OntologyLoaderException
  {
    if ( config != null && !config.isEmpty()) {
      for ( Object ontId : config.keySet() )
      {
        String key = (String) ontId;
        try {
          URI uri = LOADER.getResource(config.getProperty(key)).getURI();
          log.info( "Loading ontology: ID= " + ontId + ", uri=" + uri);
          OntologyAccess oa = fetchOntology( key, "OBO", uri );
          putOntology(key, oa);
        } catch ( Throwable e ) { //using Throwable because StackOverflowError is also possible here
          throw new OntologyLoaderException("Failed loading/parsing ontology " + key
                                              + " from " + config.getProperty(key), e );
        }
      }
    } else {
      throw new OntologyLoaderException("OntologyAccess configuration map is missing or empty (map)!");
    }
  }

  private OntologyAccess fetchOntology( String ontologyID, String format, URI uri )
    throws OntologyLoaderException {
    OntologyAccess oa = null;

    // check the format
    if ( "OBO".equals( format ) ) {
      if ( uri == null ) {
        throw new IllegalArgumentException( "The given CvSource doesn't have a URL" );
      } else {
        URL url;
        try {
          url = uri.toURL();
        } catch ( MalformedURLException e ) {
          throw new IllegalArgumentException( "The given CvSource doesn't have a valid URL: " + uri );
        }

        // parse the URL and load the ontology
        OboLoader loader = new OboLoader();
        try {
          log.debug( "Parsing ontology at URL: " + url );
          oa = loader.parseOboFile( url, ontologyID );
          oa.setName(ontologyID);
        } catch ( Exception e ) {
          throw new OntologyLoaderException( "OboFile parser failed with Exception: ", e );
        }
      }
    } else {
      throw new OntologyLoaderException( "Unsupported ontology format: " + format );
    }

    log.info( "Successfully created OntologyAccessImpl from values: ontology="
                + ontologyID + " format=" + format + " location=" + uri );

    return oa;
  }

  public Set<OntologyTermI> searchTermByName(String name) {
    return searchTermByName(name, null);
  }

  public Set<OntologyTermI> searchTermByName(String name, Set<String> ontologies) {
    Set<OntologyTermI> found  = new HashSet<OntologyTermI>();
    assert name!=null : "searchTermByName: null arg.";

    Set<String> ontologyIDs = new HashSet<String>(getOntologyIDs());
    if(ontologies != null && !ontologies.isEmpty())
      ontologyIDs.retainAll(ontologies);

    for(String ontologyId: ontologyIDs) {
      OntologyAccess oa = getOntology(ontologyId);
      for(OntologyTermI term : oa.getOntologyTerms()) {
        String prefName = term.getPreferredName();
        if(prefName == null) {
          log.error("searchTermByName: NULL preffered name for term "
                      + term.getTermAccession() + " in " + ontologyId + "; report to authors.");
        } else if(name.equalsIgnoreCase(prefName)) {
          found.add(term);
        } else {
          for(String syn : term.getNameSynonyms()) {
            if(syn.equalsIgnoreCase(name)) {
              found.add(term);
            }
          }
        }
      }
    }

    return found;
  }

  public OntologyTermI findTermByAccession(String acc) {
    OntologyTermI term = null;

    for(String ontologyId : getOntologyIDs()) {
      term = getOntology(ontologyId).getTermForAccession(acc);
      if(term != null)
        break;
    }

    return term;
  }

  /*
   * 	Some CV URI/URLs may include
   *  'obo.' in it (now deprecated) or not, like e.g.
   *  'obo.so', 'obo.go' vs. simply 'so', 'go'
   */
  public OntologyTermI getTermByUri(String uri) {
    if (uri.startsWith("urn:miriam:obo.")) {
      int pos = uri.indexOf(':', 15); //e.g. the colon after 'go' in "...:obo.go:GO%3A0005654"
      String acc = uri.substring(pos + 1);
      acc = urlDecode(acc);
      OntologyTermI term = findTermByAccession(acc); // acc. is globally unique in CvService!..
      return term;
    } else if (uri.startsWith("http://identifiers.org/obo.")) {
      int pos = uri.indexOf('/', 27); //e.g. the slash after 'go' in "...obo.go/GO:0005654"
      String acc = uri.substring(pos + 1);
      OntologyTermI term = findTermByAccession(acc);
      return term;
    } else if (uri.startsWith("urn:miriam:")) {
      int pos = uri.indexOf(':', 11); //e.g. the last colon in "...:go:GO%3A0005654"
      String acc = uri.substring(pos + 1);
      acc = urlDecode(acc);
      OntologyTermI term = findTermByAccession(acc);
      return term;
    } else if (uri.startsWith("http://identifiers.org/")) {
      int pos = uri.indexOf('/', 23); //e.g. the slash after 'org/go' in "...org/go/GO:0005654"
      String acc = uri.substring(pos + 1);
      OntologyTermI term = findTermByAccession(acc);
      return term;
    } else {
      if (log.isDebugEnabled())
        log.debug("Cannot Decode not a Controlled Vocabulary's URI : " + uri);
      return null;
    }
  }

  private String urlDecode(String acc) {
    try {
      return URLDecoder.decode(acc,"UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e.toString());
    }
  }

  public Set<OntologyTermI> getDirectChildren(String urn) {
    OntologyTermI term = getTermByUri(urn);
    OntologyAccess ontologyAccess = getOntology(term.getOntologyId());
    Set<OntologyTermI> terms = ontologyAccess.getDirectChildren(term);
    return terms;
  }

  public Set<OntologyTermI> getDirectParents(String urn) {
    OntologyTermI term = getTermByUri(urn);
    OntologyAccess ontologyAccess = getOntology(term.getOntologyId());
    Set<OntologyTermI> terms = ontologyAccess.getDirectParents(term);
    return terms;
  }

  public Set<OntologyTermI> getAllChildren(String urn) {
    OntologyTermI term = getTermByUri(urn);
    OntologyAccess ontologyAccess = getOntology(term.getOntologyId());
    Set<OntologyTermI> terms = ontologyAccess.getAllChildren(term);
    return terms;
  }

  public Set<OntologyTermI> getAllParents(String urn) {
    OntologyTermI term = getTermByUri(urn);
    OntologyAccess ontologyAccess = getOntology(term.getOntologyId());
    Set<OntologyTermI> terms = ontologyAccess.getAllParents(term);
    return terms;
  }

  public boolean isChild(String parentUrn, String urn) {
    return getAllParents(urn).contains(parentUrn)
             || getAllChildren(parentUrn).contains(urn);
  }

  public OntologyTermI findTerm(OntologyAccess ontologyAccess, String term)
  {
    OntologyTermI ot;

    if (ontologyAccess == null) {
      ot = findTermByAccession(term);
    } else {
      //otherwise continue with more specific lookup
      ot = ontologyAccess.getTermForAccession(term);

      if (ot == null) {
        //search again using the parameter as term's name/synonym
        Set<OntologyTermI> ots = searchTermByName(term,
          Collections.singleton(ontologyAccess.getName()));
        if (ots.size() == 1) //use if unambiguous
          ot = ots.iterator().next();
        else
          log.info("ambiguous term: " + term +
                     " found by searchig in ontology: " + ontologyAccess.getName());
      }
    }

    return ot;
  }

}