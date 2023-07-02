package org.biopax.validator.utils;

import java.util.*;

import org.apache.commons.collections15.collection.CompositeCollection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.normalizer.Namespace;
import org.biopax.paxtools.normalizer.Normalizer;
import org.biopax.paxtools.normalizer.Resolver;
import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.biopax.psidev.ontology_manager.impl.OntologyManagerImpl;
import org.biopax.psidev.ontology_manager.impl.OntologyTermImpl;
import org.biopax.validator.CvFactory;
import org.biopax.validator.XrefUtils;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRule;
import org.biopax.validator.api.CvUtils;
import org.biopax.validator.api.CvRestriction.UseChildTerms;

import javax.annotation.PostConstruct;


/**
 * Handles BioPAX recommended ontologies, controlled vocabularies and xrefs.
 *
 * @author rodche
 */
public class OntologyUtils implements CvUtils, CvFactory, XrefUtils
{
	private final static Log log = LogFactory.getLog(OntologyUtils.class);

	//see the post-construct init() method, where the following fields get initialized
	private OntologyManager ontologyManager;
    private CompositeCollection<String> allSynonyms; //set in init()!
    private Properties ontologyConfig;

    public void setOntologyConfig(Properties ontologyConfig) {
        this.ontologyConfig = ontologyConfig;
    }

    public OntologyManager getOntologyManager() {
        return ontologyManager;
    }

	public Set<String> getValidTermNames(CvRule<?> cvRule) {
		return getValidTermNamesLowerCase(cvRule.getRestrictions());
	}
	
	public Set<OntologyTermI> getValidTerms(CvRule<?> cvRule) {
		return getValidTerms(cvRule.getRestrictions());
	}

	public Set<String> getValidTermNames(Collection<CvRestriction> restrictions) {
		return new HashSet<>(OntologyManager.getTermNames(getValidTerms(restrictions)));
	}

	public Set<String> getValidTermNamesLowerCase(Collection<CvRestriction> restrictions) {
		Set<String> names = new HashSet<>();
		for(String name : getValidTermNames(restrictions)) {
			names.add(name.toLowerCase());
		}
		return names;
	}

	public Set<String> getTermNames(CvRestriction restriction) {
		return new HashSet<>(OntologyManager.getTermNames(getTerms(restriction)));
	}
	
	public Set<OntologyTermI> getValidTerms(Collection<CvRestriction> restrictions) {
		Set<OntologyTermI> terms = new HashSet<>();
		
		// first, collect all the valid terms
		for(CvRestriction restriction : restrictions) {
			if(!restriction.isNot()) {
				terms.addAll(getTerms(restriction));
			}
		}
		
		// now remove all those where restriction 'not' property set to true
		for(CvRestriction restriction : restrictions) {
			if(restriction.isNot()) {
				terms.removeAll(getTerms(restriction));
			}
		}
		
		return terms;
	}

	public Set<OntologyTermI> getTerms(CvRestriction restriction) {
		Set<OntologyTermI> terms = new HashSet<>();
		OntologyAccess ontologyAccess = ontologyManager.getOntology(restriction.getOntologyId());
		if(ontologyAccess == null) {
			throw new IllegalArgumentException(
					"Cannot get access to the ontology using ID: " 
					+ restriction.getOntologyId() +
					"; I know the following IDs: " 
					+ ontologyManager.getOntologyIDs().toString());
		}
		OntologyTermI term = ontologyAccess.getTermForAccession(restriction.getId());
		if(term == null) {
			log.error("Cannot Get " + restriction.getOntologyId()
					+ " OntologyAccess Term for the Accession: " + restriction.getId());
			return terms;
		}
		
		if(restriction.isTermAllowed()) {
			terms.add(term);
		}
		
		if (restriction.getChildrenAllowed() == UseChildTerms.ALL) {
			terms.addAll(ontologyAccess.getAllChildren(term));
		} else if (restriction.getChildrenAllowed() == UseChildTerms.DIRECT) {
			terms.addAll(ontologyAccess.getDirectChildren(term));
		}
		
		// FIX xml escape symbols that come from the OntologyManagerImpl (a bug?)
		for(OntologyTermI t : terms) {
			t.setPreferredName(StringEscapeUtils.unescapeXml(t.getPreferredName()));
			Set<String> synonyms = new HashSet<>();
			for(String s : t.getNameSynonyms()) {
				synonyms.add(StringEscapeUtils.unescapeXml(s));
			}
			t.setNameSynonyms(synonyms);
		}
		
		return terms;
	}

  public <T extends ControlledVocabulary> T getControlledVocabulary(
    String uri, Class<T> cvClass, String xmlBase)
  {
    OntologyTermI term = ontologyManager.getTermByUri(uri);
    return getControlledVocabulary(term, cvClass, xmlBase);
  }

  public Set<String> getDirectChildren(String uri) {
    return ontologyTermsToUris(ontologyManager.getDirectChildren(uri));
  }

  public Set<String> getDirectParents(String uri) {
    return ontologyTermsToUris(ontologyManager.getDirectParents(uri));
  }

  public Set<String> getAllChildren(String uri) {
    return ontologyTermsToUris(ontologyManager.getAllChildren(uri));
  }

  public Set<String> getAllParents(String uri) {
    return ontologyTermsToUris(ontologyManager.getAllParents(uri));
  }

  public boolean isChild(String parentUrn, String uri) {
    return ontologyManager.isChild(parentUrn, uri);
  }

  private <T extends ControlledVocabulary> T getControlledVocabulary(
    OntologyTermI term, Class<T> cvClass, String xmlBase) {
    if (term == null) {
      return null;
    }

    BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
    String uri = ontologyTermToUri(term);
    T cv = factory.create(cvClass, uri);
    cv.addTerm(term.getPreferredName());
    String ontId = term.getOntologyId(); // like "GO"
    String db = ontologyManager.getOntology(ontId).getName(); // names were fixed in the constructor!
    String xUri = Normalizer.uri(xmlBase, db, term.getTermAccession(), UnificationXref.class);
    UnificationXref ux = factory.create(UnificationXref.class, xUri);
    ux.setDb(db.toLowerCase());
    ux.setId(term.getTermAccession());
    cv.addXref(ux);

    return cv;
  }

  private Set<String> ontologyTermsToUris(Collection<OntologyTermI> terms) {
    Set<String> urns = new HashSet<>();
    for (OntologyTermI term : terms) {
      urns.add(ontologyTermToUri(term));
    }
    return urns;
  }

  private String ontologyTermToUri(OntologyTermI term) {
    String uri = null;
    if (term != null) {
      String ontologyID = term.getOntologyId();
      String accession = term.getTermAccession();
      uri = Resolver.getURI(ontologyID, accession);
    }
    return uri;
  }

  /*
   * Post-construct initializer.
   *
   * Load, merge bio identifiers db/datasource names and synonyms
   * from Miriam registry, MI ontology "database citation" branch, and other (custom) name variants.
   */
  @PostConstruct //vital
  public synchronized void init() {
    try {
      //create new ontology manager and load OBO files as specified in the properties.
      ontologyManager = new OntologyManagerImpl();
      ontologyManager.loadOntologies(ontologyConfig);
      //Normalize ontology names
      for (String id : ontologyManager.getOntologyIDs()) {
        Namespace ns = Resolver.getNamespace(id);
        String officialName = id;
        if(ns != null) {
          officialName = ns.getName();
        }
        ontologyManager.getOntology(id).setName(officialName);
        log.debug(id + " (" + officialName + ")");
      }
    } catch (Throwable e) {
      throw new RuntimeException("Failed to load ontologies", e);
    }

    // Build collections of recommended (valid) xref.db names and corresponding id patterns
    this.allSynonyms = new CompositeCollection<>();

    // a temporary local collection (hides the member/field: allSynonyms)
    CompositeCollection<String> allSynonyms = new CompositeCollection<>();

    // first, we prepare lists of db synonyms from
    //  - Bioregistry.io (get each entry's prefix, name, synonyms)
    //  - MI (OBO terms under the "database citation" root)
    for (Namespace ns : Resolver.getNamespaces().values()) {
      String name = dbName(ns.getName()); //trim,uppercase
      String prefix = dbName(ns.getPrefix());
      final List<String> synonyms = new ArrayList<>();
      synonyms.add(name);
      synonyms.add(prefix);
      //add synonyms from the registry
      ns.getSynonyms().forEach((s) -> synonyms.add(dbName(s)));
      //add custom synonyms of given prefix from the Resolver's map
      Resolver.getSynonymap().entrySet().stream().filter(e -> StringUtils.equals(ns.getPrefix(),e.getValue()))
              .forEach(e -> synonyms.add(dbName(e.getKey())));
      allSynonyms.addComposited(synonyms);
    }

    // load all names from MI 'database citation'
    OntologyAccess mi = ontologyManager.getOntology("MI");
    Collection<OntologyTermI> terms = mi.getAllChildren(new OntologyTermImpl("MI:0444"));
    for (OntologyTermI term : terms) {
      List<String> synonyms = new ArrayList<>();
      synonyms.add(dbName(term.getPreferredName()));
      for (String name : term.getNameSynonyms()) {
        String s = dbName(name);
        if (!synonyms.contains(s))
          synonyms.add(s);
      }
      allSynonyms.addComposited(synonyms);
    }

    // find synonyms groups that overlap,
    Cluster<Collection<String>> clus = new Cluster<>() {
      @Override
      public boolean match(Collection<String> a, Collection<String> b) {
        return !Collections.disjoint(a, b);
      }
    };

    // find all overlapping groups of names (can be single-group "clusters" as well)
    // get the set of clusters (sets) of overlapping synonym lists!
    // (i.e., we're in fact clustering List<String> objects)
    Set<Set<Collection<String>>> groupsOfGroups = clus.cluster(allSynonyms.getCollections(), Integer.MAX_VALUE);

    // perform overlapping groups merging so that preferred name goes first
    for (Set<Collection<String>> groupsToMerge : groupsOfGroups) {
      if (groupsToMerge.size() > 1) {
        List<String> merged = new ArrayList<>();
        for (Collection<String> group : groupsToMerge) {
          for (String name : group) {
            if (!merged.contains(name))
              merged.add(name);
          }
        }
        //if possible, move the preferred name on top
        String topName = merged.get(0);
        Namespace ns = Resolver.getNamespace(topName);
        if(ns != null) {
          String preferName = dbName(ns.getName());
          merged.add(0, preferName);
        }
        this.allSynonyms.addComposited(merged);
      } else {
        assert !groupsToMerge.isEmpty(); //one!
        //not overlapping (with other) single group
        this.allSynonyms.addComposited(groupsToMerge.iterator().next());
      }
    }
  }

  @Override
  public String dbName(String name) {
    return (StringUtils.isBlank(name)) ? null : name.trim().toUpperCase();
  }

  @Override
  public List<String> getSynonymsForDbName(String name) {
    String dbName = dbName(name);
    for (Collection<String> group : allSynonyms.getCollections()) {
      if (group.contains(dbName)) {
        return (List<String>) group;
      }
    }
    return Collections.emptyList();
  }

  @Override
  public String getPrimaryDbName(String name) {
    List<String> names = getSynonymsForDbName(name);
    return (names.isEmpty()) ? null : names.iterator().next();
  }

  @Override
  public boolean checkIdFormat(String db, String id) {
    db = getPrimaryDbName(db); //allow synonyms otherwise unknown to Resolver (e.g. from MI but not in bioregistry/miriam)
    return Resolver.checkRegExp(id, db);
  }

  @Override
  public boolean canCheckIdFormatIn(String name) {
    String db = getPrimaryDbName(name);
    Namespace ns = Resolver.getNamespace(db);
    return  ns != null && StringUtils.isNotBlank(ns.getPattern());
  }

  @Override
  public String getRegexpString(String db) {
    Namespace ns = Resolver.getNamespace(getPrimaryDbName(db));
    return  (ns != null) ? ns.getPattern() : null;
  }

  @Override
  public boolean isUnofficialOrMisspelledDbName(final String db) {
    String name = dbName(db);
    return !allSynonyms.contains(name) && Resolver.isKnownNameOrVariant(name);
  }

}
