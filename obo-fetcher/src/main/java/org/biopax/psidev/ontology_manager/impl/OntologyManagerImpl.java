package org.biopax.psidev.ontology_manager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Access to the specified bio/chem ontologies (loaded from several OBO format files).
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baderlab.org) - simplified it for BioPAX Validator
 * @since 2.0.0
 */
public class OntologyManagerImpl implements OntologyManager {

  private static final Log log = LogFactory.getLog(OntologyManagerImpl.class);
  private static final String TMPDIR = System.getProperty("java.io.tmpdir");

  /**
   * The Map that holds the Ontologies.
   * The key is the ontology ID and the value is an ontology implementing the OntologyAccess interface.
   */
  private Map<String, OntologyAccess> ontologyMap = new HashMap<>();

  public OntologyManagerImpl() {
  }

  public void putOntology( String ontologyID, OntologyAccess ontologyAccess ) {
    if ( ontologyMap.containsKey( ontologyID ) ) {
      log.warn( "OntologyAccess with id='" + ontologyID + "' already exists; overwriting" );
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

  public synchronized void loadOntologies( Properties config )
    throws OntologyLoaderException
  {
    if ( config != null && !config.isEmpty()) {
      for ( Object ontId : config.keySet() ) {
        String key = (String) ontId;
        String resource = config.getProperty(key);
        try {
          log.info( "Loading ontology: " + key + ", " + resource);
          OntologyAccess oa = fetchOntology( key, resource);
          putOntology(key, oa);
        } catch ( Throwable e ) {
          throw new OntologyLoaderException("Failed fetching ontology " + key + " from " + resource, e);
        }
      }
    } else {
      throw new OntologyLoaderException("OntologyAccess config map is missing or empty");
    }
  }

  private synchronized OntologyAccess fetchOntology( String ontologyID, String resource) throws Exception {
    OntologyAccess oa = null; // to make

    final Path serialized = Paths.get(TMPDIR,ontologyID + "_" + resource.hashCode() + ".bin");
    //deserialize if the cache file exists
    if(Files.exists(serialized)) {
      try {
        oa = (OntologyAccess) new ObjectInputStream(Files.newInputStream(serialized, StandardOpenOption.READ))
          .readObject();
        log.info( "Loaded ontology " + ontologyID + " from cache " + serialized );
      } catch (Exception e) {
        log.error("Failed to deserialize ontology from cache " + serialized, e);
      }
    }

    if(oa == null) {// load the ontology from the resource
      try {
        URL url = ResourceUtils.getURL(resource);
        oa = new OboLoader().parseOboFile(url, ontologyID);
        oa.setName(ontologyID);
        log.info( "Loaded ontology " + ontologyID + " from " + resource );
        // serialize (reusable cache file)
        try {
          new ObjectOutputStream(Files.newOutputStream(serialized,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)
          ).writeObject(oa);
          log.info( "Serialized " + ontologyID + " to a cache file " + serialized);
        } catch (Exception e) {
          log.error("Failed to serialize " + ontologyID + " to: " + serialized, e);
        }
      } catch (Exception e) {
        throw new OntologyLoaderException("OBO file loader failed", e);
      }
    }

    return oa;
  }

  public Set<OntologyTermI> searchTermByName(String name) {
    return searchTermByName(name, null);
  }

  public Set<OntologyTermI> searchTermByName(String name, Set<String> ontologies) {
    Set<OntologyTermI> found  = new HashSet<>();
    assert name!=null : "searchTermByName: null arg.";

    Set<String> ontologyIDs = new HashSet<>(getOntologyIDs());
    if(ontologies != null && !ontologies.isEmpty())
      ontologyIDs.retainAll(ontologies);

    for(String ontologyId: ontologyIDs) {
      OntologyAccess oa = getOntology(ontologyId);
      for(OntologyTermI term : oa.getOntologyTerms()) {
        String prefName = term.getPreferredName();
        if(prefName == null) {
          log.error("searchTermByName: null preferred name for term "
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

  public OntologyTermI getTermByUri(String uri) {
    if(!StringUtils.hasText(uri)) {
      return null;
    }
    if (uri.toLowerCase().startsWith("urn:miriam:")) {
      int pos = uri.lastIndexOf(':'); //e.g. the last colon in "urn:miriam:go:GO%3A0005654"
      String acc = uri.substring(pos + 1);
      acc = urlDecode(acc);
      OntologyTermI term = findTermByAccession(acc);
      return term;
    } else if (uri.toLowerCase().contains("identifiers.org") || uri.contains("bioregistry.io")) {
      int pos = uri.lastIndexOf('/');
      String acc = uri.substring(pos + 1);
      OntologyTermI term = findTermByAccession(acc);
      if(term == null) {
        term = findTermByAccession(acc.toUpperCase()); //might help "GO:1234" vs "go:1234" cases
      }
      return term;
    } else { // CURIE (e.g. "go:1234")?
      OntologyTermI term = findTermByAccession(uri);
      return (term != null) ? term : findTermByAccession(uri.toUpperCase());
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
          log.info("ambiguous term: " + term + " found by searchig in ontology: " + ontologyAccess.getName());
      }
    }

    return ot;
  }

}