package org.biopax.validator.utils;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.collections15.collection.CompositeCollection;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.normalizer.MiriamLink;
import org.biopax.paxtools.normalizer.Normalizer;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


/**
 * Handles BioPAX recommended ontologies, controlled vocabularies and xrefs.
 * 
 * @author rodche
 */
@Component
@Scope("singleton")
public class OntologyUtils implements CvUtils, CvFactory, XrefUtils
{
	private final static Log log = LogFactory.getLog(OntologyUtils.class);

	//see the post-construct init() method, where the following fields get initialized
	private OntologyManager ontologyManager;
  private CompositeCollection<String> allSynonyms; //set in init()!
  private Set<String> unofficialDbNames; // to be generated
	private Map<String, Pattern> dataPatterns;
  private CompositeCollection<String> extraGroups; //set in Constructor
  private Properties ontologyConfig;

	@Autowired
  @Resource(name = "extraDbSynonyms")
  public void setExtraGroups(Set<List<String>> extraDbSynonyms) {
    // normalize and organize provided synonyms
    this.extraGroups = new CompositeCollection<>();
    if (extraDbSynonyms != null) {
      for (List<String> group : extraDbSynonyms) {
        Collection<String> newG = new ArrayList<>();
        for (String s : group)
          newG.add(dbName(s)); //trim, uppercase, add
        this.extraGroups.addComposited(newG);
      }
    }
  }

  @Override
  public OntologyManager getOntologyManager() {
    return ontologyManager;
  }

  /* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvUtils#getValidTermNames(org.biopax.validator.CvRule)
	 */
	public Set<String> getValidTermNames(CvRule<?> cvRule) {
		return getValidTermNamesLowerCase(cvRule.getRestrictions());
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvUtils#getValidTerms(org.biopax.validator.CvRule)
	 */
	public Set<OntologyTermI> getValidTerms(CvRule<?> cvRule) {
		return getValidTerms(cvRule.getRestrictions());
	}

	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvUtils#getValidTermNames(java.util.Collection)
	 */
	public Set<String> getValidTermNames(Collection<CvRestriction> restrictions) {
		return new HashSet<>(OntologyManager.getTermNames(getValidTerms(restrictions)));
	}

	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvUtils#getValidTermNamesLowerCase(java.util.Collection)
	 */
	public Set<String> getValidTermNamesLowerCase(Collection<CvRestriction> restrictions) {
		Set<String> names = new HashSet<>();
		for(String name : getValidTermNames(restrictions)) {
			names.add(name.toLowerCase());
		}
		return names;
	}

	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvUtils#getTermNames(org.biopax.validator.impl.CvTermRestriction)
	 */
	public Set<String> getTermNames(CvRestriction restriction) {
		return new HashSet<>(OntologyManager.getTermNames(getTerms(restriction)));
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvUtils#getValidTerms(java.util.Collection)
	 */
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

	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvUtils#getTerms(org.biopax.validator.impl.CvTermRestriction)
	 */
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
    String db, String id, Class<T> cvClass, String xmlBase)
  {
    OntologyAccess ontologyAccess = ontologyManager.getOntology(db);
    if (ontologyAccess == null) // it may be urn - try again
      ontologyAccess = getOntologyByUrn(db);
    OntologyTermI term = ontologyManager.findTerm(ontologyAccess, id);

    return (term != null) ? getControlledVocabulary(term, cvClass, xmlBase) : null;
  }

  public <T extends ControlledVocabulary> T getControlledVocabulary(
    String urn, Class<T> cvClass, String xmlBase)
  {
    OntologyTermI term = ontologyManager.getTermByUri(urn);
    T cv = getControlledVocabulary(term, cvClass, xmlBase);
    if (cv != null)
      cv.addComment("auto-generated");
    return cv;
  }

  public Set<String> getDirectChildren(String urn) {
    return ontologyTermsToUris(ontologyManager.getDirectChildren(urn));
  }

  public Set<String> getDirectParents(String urn) {
    return ontologyTermsToUris(ontologyManager.getDirectParents(urn));
  }

  public Set<String> getAllChildren(String urn) {
    return ontologyTermsToUris(ontologyManager.getAllChildren(urn));
  }

  public Set<String> getAllParents(String urn) {
    return ontologyTermsToUris(ontologyManager.getAllParents(urn));
  }

  public boolean isChild(String parentUrn, String urn) {
    return ontologyManager.isChild(parentUrn, urn);
  }

  <T extends ControlledVocabulary> T getControlledVocabulary(
    OntologyTermI term, Class<T> cvClass, String xmlBase)
  {
    if (term == null)
      return null;

    BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

    String urn = ontologyTermToUri(term);
    T cv = factory.create(cvClass, urn);
    cv.addTerm(term.getPreferredName());

    String ontId = term.getOntologyId(); // like "GO"
    String db = ontologyManager.getOntology(ontId).getName(); // names were fixed in the constructor!
    String rdfid = Normalizer.uri(xmlBase, db, term.getTermAccession(), UnificationXref.class);
    UnificationXref uref = factory.create(UnificationXref.class, rdfid);
    uref.setDb(db);
    uref.setId(term.getTermAccession());
    cv.addXref(uref);

    return cv;
  }

  /*
   * Gets OntologyAccess by (Miriam's) datatype URI
   */
  private OntologyAccess getOntologyByUrn(String dtUrn) {
    for (String id : ontologyManager.getOntologyIDs()) {
      OntologyAccess ont = ontologyManager.getOntology(id);
      String urn = MiriamLink.getDataTypeURI(id);
      if (dtUrn.equalsIgnoreCase(urn)) {
        return ont;
      }
    }
    return null;
  }

  public Set<String> ontologyTermsToUris(Collection<OntologyTermI> terms) {
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
      uri = MiriamLink.getIdentifiersOrgURI(ontologyID, accession);
    }
    return uri;
  }

  /*
   * Post-construct initializer.
   *
   * Loads and merges bio database/datasource names and synonyms
   * from Miriam resource, PSI-MI ontology ("database citation" branch),
   * and extra (configured by user) names.
   */
  @PostConstruct //vital
  public synchronized void init() {
    try {
      log.info("Loading the configuration from obo.properties and building ontology trees...");
      PropertiesFactoryBean oboPropertiesFactoryBean = new PropertiesFactoryBean();
      oboPropertiesFactoryBean.setLocation(new ClassPathResource("obo.properties"));
      oboPropertiesFactoryBean.setLocalOverride(false);
      //create new ontology manager and load/parse OBO files as specified in the properties.
      oboPropertiesFactoryBean.afterPropertiesSet();
      this.ontologyConfig = oboPropertiesFactoryBean.getObject();
      //create new ontology manager and load/parse OBO files as specified in the properties.
      this.ontologyManager = new OntologyManagerImpl(this.ontologyConfig);
      //Normalize ontology names
      for (String id : ontologyManager.getOntologyIDs()) {
        String officialName = MiriamLink.getName(id);
        ontologyManager.getOntology(id).setName(officialName);
        log.debug(id + " (" + officialName + ")");
      }
    } catch (Throwable e) {
      throw new RuntimeException("Failed to load or parse all required biological ontologies!", e);
    }

    // Build collections of the recommended xref.db names and synonyms and corresponding id patterns
    this.dataPatterns = new ConcurrentHashMap<>();
    this.allSynonyms = new CompositeCollection<>();
    this.unofficialDbNames = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // need a temporaty, local "all synonyms" collection
    // (hide the member one)
    CompositeCollection<String> allSynonyms = new CompositeCollection<>();
    // first, we simple prepare lists of db synonyms,
    // from  Miriam, MI, and "extra" set (already done - constructor arg.)
    for (String dt : MiriamLink.getDataTypesName()) {
      String regexp = MiriamLink.getDataTypePattern(dt);
      Pattern pattern = null;
      try {
        pattern = Pattern.compile(regexp);
      } catch (PatternSyntaxException e) {
        log.error(
          "Pattern compilation failed for MIRIAM " +
            "db=" + dt + "; regexp=" + dt + "; " + e);
      }
      String db = dbName(dt); //uppercase
      List<String> synonyms = new ArrayList<>();
      synonyms.add(db);
      String[] names = MiriamLink.getNames(dt);
      if (names != null)
        for (String name : names) {
          String s = dbName(name);
          if (!synonyms.contains(s))
            synonyms.add(s);
        }
      //save
      allSynonyms.addComposited(synonyms);
      // also associate primary name with ID patterns
      if (pattern != null) //null value is not allowed for ConcurrentHashMap
        dataPatterns.put(db, pattern); // will be used with all synonyms
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

    // second, we populate unofficialDbNames by comparing
    // current allSynonyms (from Miriam and MI:0444) with extraSynonyms (from config. file)
    for (String db : extraGroups)
      if (!allSynonyms.contains(db))
        unofficialDbNames.add(db);

    extraGroups.getCollections().forEach(allSynonyms::addComposited);
    // find synonyms groups that overlap,
    Cluster<Collection<String>> clus = new Cluster<Collection<String>>() {
      @Override
      public boolean match(Collection<String> a, Collection<String> b) {
        return !Collections.disjoint(a, b);
      }
    };

    // find all overlapping groups of names (can be single-group "clusters" as well))
    // get the set of clusters (sets) of overlapping synonym lists! (i.e., we're in fact clustering List<String> objects)
    Set<Set<Collection<String>>> groupsOfGroups = clus.cluster(allSynonyms.getCollections(), Integer.MAX_VALUE);

    // perform overlapping groups merging
    // so that Miriam's preferred name goes first
    for (Set<Collection<String>> groupsToMerge : groupsOfGroups) {
      if (groupsToMerge.size() > 1) {
        List<String> merged = new ArrayList<>();
        String primary = null;
        for (Collection<String> group : groupsToMerge) {
          for (String name : group) {
            if (dataPatterns.containsKey(name))
              primary = name;
            if (!merged.contains(name))
              merged.add(name);
          }
        }

        if (primary != null) {
          merged.remove(primary);
          merged.add(0, primary);
        }
        this.allSynonyms.addComposited(merged);
      } else {
        assert !groupsToMerge.isEmpty(); //one!
        //not overlapping (with other) single group
        this.allSynonyms.addComposited(groupsToMerge.iterator().next());
      }
    }

    //TODO: move sanity checks to the integration tests
    assert (!getSynonymsForDbName("EntrezGene").isEmpty());
    assert (!getSynonymsForDbName("chebi").isEmpty());
    assert xcheck();
  }

  @Override
  public String dbName(String name) {
    return name.trim().toUpperCase();
  }

  @Override
  public List<String> getSynonymsForDbName(String name) {
    return getSynonymsForName(name, allSynonyms);
  }

  private List<String> getSynonymsForName(String name, CompositeCollection<String> groups) {
    String dbName = dbName(name);

    for (Collection<String> group : groups.getCollections()) {
      if (group.contains(dbName)) {
        return (List<String>) group;
      }
    }
    return Collections.emptyList();
  }

  @Override
  public String getPrimaryDbName(String name) {
    List<String> names = getSynonymsForDbName(name);
    //get the first name
    return (names.isEmpty()) ? null : names.iterator().next();
  }

  @Override
  public boolean checkIdFormat(String db, String id) {
    String name = getPrimaryDbName(db);
    Pattern p = dataPatterns.get(name);
    return p.matcher(id).find();
  }

  @Override
  public boolean canCheckIdFormatIn(String name) {
    String db = getPrimaryDbName(name);
    return (db == null) ? false : dataPatterns.get(db) != null;
  }

  @Override
  public String getRegexpString(String db) {
    String name = getPrimaryDbName(db);
    Pattern p = dataPatterns.get(name);
    return (p != null) ? p.pattern() : null;
  }

  @Override
  public boolean isUnofficialOrMisspelledDbName(final String db) {
    return unofficialDbNames.contains(dbName(db));
  }

  //sanity test
  boolean xcheck() {
    Collection<String>[] lists = allSynonyms.getCollections().toArray(new Collection[]{});
    for (int i = 0; i < lists.length; i++) {
      Collection<String> li = lists[i];
      for (int j = i + 1; j < lists.length; j++) {
        if (!Collections.disjoint(li, lists[j])) {
          log.error("Different synonym groups overlap: "
                      + li.toString() + " has names in common with "
                      + lists[j].toString());
          return false;
        }
      }
    }

    return true; //no overlap
  }

}
