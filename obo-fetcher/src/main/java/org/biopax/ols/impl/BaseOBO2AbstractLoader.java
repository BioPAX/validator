package org.biopax.ols.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.ols.Annotation;
import org.biopax.ols.Constants;
import org.biopax.ols.DbXref;
import org.biopax.ols.Loader;
import org.biopax.ols.Ontology;
import org.biopax.ols.Parser;
import org.biopax.ols.Term;
import org.biopax.ols.TermPath;
import org.biopax.ols.TermRelationship;
import org.biopax.ols.TermSynonym;
import org.obo.datamodel.*;



import java.io.IOException;
import java.util.*;

/**
 * Base class for all loaders. This class will perform OBO-to-OLS object conversion, creating all relevant
 * objects. Three methods are listed as abstract: configure, parse and printusage, which depend on the OBO
 * format being used (GOFF or OBO).
 *
 * @author Richard Cote
 */
public abstract class BaseOBO2AbstractLoader implements Loader {
    protected static Log logger = LogFactory.getLog(BaseOBO2AbstractLoader.class);
    protected HashMap<String, Term> ontologyTerms = new HashMap<>(5000);
    protected String ONTOLOGY_DEFINITION;
    protected String FULL_NAME;
    protected String SHORT_NAME;
    protected String QUERY_URL = null;
    protected String SOURCE_URL = null;
    protected OntologyBean ontBean = null;

    //common objects
    protected TermBean IS_A = null;
    protected TermBean PART_OF = null;
    protected TermBean DEVELOPS_FROM = null;
    protected TermBean ALT_ID = null;
    protected TermBean EXACT = null;
    protected TermBean NARROW = null;
    protected TermBean BROAD = null;
    protected TermBean RELATED = null;
    protected TermBean SYNONYM = null;
    protected final HashSet<String> PART_OF_SET = new HashSet<>();
    protected final HashSet<String> IS_A_SET = new HashSet<>();
    protected final HashSet<String> DEV_FROM_SET = new HashSet<>();
    private HashMap<String, Term> unknown_relations = new HashMap<>();
    private TreeSet<String> rootTerms = new TreeSet<>();
    protected Parser parser;

    //for PSI-MOD
    private Set<String> MOD_NUMERIC_ANNOTATIONS = null;
    private Set<String> MOD_STRING_ANNOTATIONS = null;


    /**
     * batch mode will optimize inserts. set to false if having wonky errors or rdbms does not support it.
     */
    protected static final boolean BATCH_MODE = true;

    protected void setParser(Parser parser) {
        this.parser = parser;
    }

    /**
     * determine which root detection algorithm will be used
     */
    protected boolean useGreedy = false;

    protected void setUseGreedy(boolean useGreedy) {
        this.useGreedy = useGreedy;
    }

    /**
     * will return a collection of TermBean accesssion strings that correspond to the root term
     *
     * @throws IllegalStateException if the parser has not been initialized
     */
    protected Collection<String> getRootTerms() {
        HashSet<String> retval = new HashSet<>();
        if (parser != null) {
            Set<OBOObject> roots = parser.getRootTerms(useGreedy);
            if (roots != null) {
                for (OBOObject root : roots) {
                    retval.add(safeTrim(root.getID()));
                }
            }
        } else {
            throw new IllegalStateException("Parser has not been initialized. Did you run configure()?");
        }
        return retval;
    }

    /**
     * Will iterate through all parsed terms and generate memory structure suitable for loading to db
     *
     * @throws IllegalStateException if the parser has not been initialized
     */
    protected void process() {
        //returns unmodifiable set, so need to create a modifiable one
        Set<OBOObject> terms = new HashSet<>();

        //sanity check
        if (parser == null) {
            throw new IllegalStateException("parser has not been initialized. Did you run configure()?");
        }

        //tmp collection to store terms
        Collection<OBOObject> toAdd;

        //add all terms - this will include obsolete and roots
        //sanity check to avoid NPE
        toAdd = parser.getTerms();
        if (toAdd != null) {
            terms.addAll(toAdd);
        }

        //create common objects needed to build references and synonyms
        initializeCommonObjects();

        //process all terms
        logger.info("Total Terms to process: " + terms.size());

        int count = 0;
        for (Object obj : terms) {
            count++;
            if (obj instanceof OBOObject) {
                processTerm((OBOObject) obj);
                if (count % 1000 == 0) {
                    logger.debug("Terms Processed: " + count);
                }
            } else {
                logger.info("Ignored object: " + obj.toString());
            }
        }
        logger.info("Term processing done");

        //need to iterate again to build relationships
        logger.info("Creating relationships");
        count = 0;
        for (Object obj : terms) {
            count++;
            if (obj instanceof OBOObject) {
                processTermRelationships((OBOObject) obj);
                if (count % 1000 == 0) {
                    logger.debug("Relationships Processed: " + count);
                }
            } else {
                logger.info("Ignored object: " + obj.toString());
            }
        }
        logger.info("Relationship processing done");

        //add to OntologyAccess
        ontBean.setTerms(ontologyTerms.values());
    }


    /**
     * inetrnal helper method to initialize and reset shared objects
     */
    protected void initializeCommonObjects() {
        //no need to check if parser is not null because
        //of previous sanity check at the start of process()

        //this is not true in the cast of the NEWT loader!!!
        String version;
        if (parser != null) {
            version = parser.getSession().getCurrentHistory().getVersion();

            //if version is not set, set it to file date
            if (version == null) {
                Date tmp = parser.getSession().getCurrentHistory().getDate();
                //if date is not set, set it to current date
                if (tmp != null)
                    version = tmp.toString();
                else
                    version = (new Date()).toString();
            }

        } else {
            version = (new Date()).toString();
        }

        //create ontology
        ontBean = new OntologyBean();
        if (ONTOLOGY_DEFINITION != null && ONTOLOGY_DEFINITION.length() > 2000) {
            logger.warn("ontology definition longer than allowed database column length - truncating");
            ONTOLOGY_DEFINITION = ONTOLOGY_DEFINITION.substring(0, 2000);
        }
        ontBean.setDefinition(ONTOLOGY_DEFINITION);

        if (FULL_NAME != null && FULL_NAME.length() > 128) {
            logger.warn("ontology full name longer than allowed database column length - truncating");
            FULL_NAME = FULL_NAME.substring(0, 128);
        }
        ontBean.setFullOntologyName(FULL_NAME);

        ontBean.setLoadDate(new java.sql.Date(GregorianCalendar.getInstance().getTime().getTime()));

        if (SHORT_NAME != null && SHORT_NAME.length() > 32) {
            logger.warn("ontology short name longer than allowed database column length - truncating");
            SHORT_NAME = SHORT_NAME.substring(0, 32);
        }
        ontBean.setShortOntologyName(SHORT_NAME);

        if (version != null && version.length() > 128) {
            logger.warn("ontology version longer than allowed database column length - truncating");
            version = version.substring(0, 128);
        }
        ontBean.setVersion(version);

        ontBean.setFullyLoaded(false);

        if (QUERY_URL != null && QUERY_URL.length() > 255) {
            logger.warn("ontology query url longer than allowed database column length - truncating");
            QUERY_URL = QUERY_URL.substring(0, 255);
        }
        ontBean.setQueryURL(QUERY_URL);

        if (SOURCE_URL != null && SOURCE_URL.length() > 255) {
            logger.warn("ontology source url longer than allowed database column length - truncating");
            SOURCE_URL = SOURCE_URL.substring(0, 255);
        }
        ontBean.setSourceURL(SOURCE_URL);

        //make certain there's no dirty data (esp if we're using it to load multiple ontologies)
        ontologyTerms.clear();

        //create mapping sets
        IS_A_SET.clear();
        IS_A_SET.add(Constants.IS_A_RELATION_TYPE);
        IS_A_SET.add(Constants.IS_A_RELATION_TYPE.toUpperCase());
        //other mappings seen
        IS_A_SET.add("isa");
        IS_A_SET.add("ISA");
        IS_A_SET.add("OBO_REL:is_a");

        PART_OF_SET.clear();
        PART_OF_SET.add(Constants.PART_OF_RELATION_TYPE);
        PART_OF_SET.add(Constants.PART_OF_RELATION_TYPE.toUpperCase());
        //other mappings seen
        PART_OF_SET.add("partof");
        PART_OF_SET.add("PARTOF");
        PART_OF_SET.add("OBO_REL:part_of");
        PART_OF_SET.add("is_part_of");

        DEV_FROM_SET.clear();
        DEV_FROM_SET.add(Constants.DEVELOPS_FROM_RELATION_TYPE);
        DEV_FROM_SET.add(Constants.DEVELOPS_FROM_RELATION_TYPE.toUpperCase());
        //other mappings seen
        DEV_FROM_SET.add("DERIVED/DEVELOPS_FROM");

        //set PSI-MOD specific xrefs that will be converted to annotations
        MOD_NUMERIC_ANNOTATIONS = new HashSet<String>();
        MOD_NUMERIC_ANNOTATIONS.add("DiffAvg");
        MOD_NUMERIC_ANNOTATIONS.add("DiffMono");
        MOD_NUMERIC_ANNOTATIONS.add("MassAvg");
        MOD_NUMERIC_ANNOTATIONS.add("MassMono");

        MOD_STRING_ANNOTATIONS = new HashSet<String>();
        MOD_STRING_ANNOTATIONS.add("DiffFormula");
        MOD_STRING_ANNOTATIONS.add("Formula");
        MOD_STRING_ANNOTATIONS.add("Source");
        MOD_STRING_ANNOTATIONS.add("Origin");
        MOD_STRING_ANNOTATIONS.add("TermSpec");

        //create relations
        IS_A = initializeTermBean(Constants.IS_A_RELATION_TYPE, Loader.RELATION_TYPE);
        ontologyTerms.put(IS_A.getIdentifier(), IS_A);
        PART_OF = initializeTermBean(Constants.PART_OF_RELATION_TYPE, Loader.RELATION_TYPE);
        ontologyTerms.put(PART_OF.getIdentifier(), PART_OF);
        DEVELOPS_FROM = initializeTermBean(Constants.DEVELOPS_FROM_RELATION_TYPE, Loader.RELATION_TYPE);
        ontologyTerms.put(DEVELOPS_FROM.getIdentifier(), DEVELOPS_FROM);

        //create synonyms
        ALT_ID = initializeTermBean(Constants.ALT_ID_SYNONYM_TYPE, Loader.SYNONYM_TYPE);
        ontologyTerms.put(ALT_ID.getIdentifier(), ALT_ID);

        EXACT = initializeTermBean(Constants.EXACT_SYNONYM_TYPE, Loader.SYNONYM_TYPE);
        ontologyTerms.put(EXACT.getIdentifier(), EXACT);

        NARROW = initializeTermBean(Constants.NARROW_SYNONYM_TYPE, Loader.SYNONYM_TYPE);
        ontologyTerms.put(NARROW.getIdentifier(), NARROW);

        BROAD = initializeTermBean(Constants.BROAD_SYNONYM_TYPE, Loader.SYNONYM_TYPE);
        ontologyTerms.put(BROAD.getIdentifier(), BROAD);

        RELATED = initializeTermBean(Constants.RELATED_SYNONYM_TYPE, Loader.SYNONYM_TYPE);
        ontologyTerms.put(RELATED.getIdentifier(), RELATED);

        SYNONYM = initializeTermBean(Constants.DEFAULT_SYNONYM_TYPE, Loader.SYNONYM_TYPE);
        ontologyTerms.put(SYNONYM.getIdentifier(), SYNONYM);

        //initialize synonymTypeDefs
        if (parser != null) {
            Collection<SynonymType> synonymTypes = parser.getSession().getSynonymTypes();
            if (synonymTypes != null && !synonymTypes.isEmpty()) {
                for (SynonymType st : synonymTypes) {
                    ontologyTerms.put(st.getID(), initializeTermBean(st.getName(), SHORT_NAME + ":" + st.getID(), getSynonymTypeDef(st.getScope())));
                }
            }
        }

//        //get rid of stale data
//        instances.clear();

        //get rid of stale data and get root terms
        rootTerms.clear();
        if (parser != null) {
            rootTerms.addAll(getRootTerms());
        }

    }


    /**
     * helper method to create a TermBean given a name and a definition. This is mostly for terms
     * associated with an ontology but not defined by it (eg relations types, synonym types, etc)
     *
     * @param name - this value will be used to set the term.name and term.identifier
     * @param def  - the term definition.
     * @return a valid TermBean object
     * @throws IllegalArgumentException if the term name is null.
     */
    private TermBean initializeTermBean(String name, String def) {
        if (name != null && !"".equals(name.trim())) {
            TermBean bean = new TermBean();
            bean.setName(name.trim());
            bean.setIdentifier(SHORT_NAME + ":" + name.toUpperCase().trim());
            bean.setDefinition(safeTrim(def));
            bean.setParentOntology(ontBean);
            //must set PK here because OJB will now not set it automatically
            //PK will be term_ac+ont+fully_loaded_false
            bean.setTermPk(bean.getIdentifier() + SHORT_NAME + "0");
            return bean;
        } else {
            throw new IllegalArgumentException("Can't have a non-null term name!");
        }
    }

    /**
     * helper method to create a TermBean given a name, an accession and a definition.
     *
     * @param name      - this value will be used to set the term.name
     * @param accession - this value will be used to set the term.identifier
     * @param def       - the term definition. 
     * @return a valid TermBean object
     * @throws IllegalArgumentException if the term name or accession is null.
     */
    protected TermBean initializeTermBean(String name, String accession, String def) {
        if (accession != null && !"".equals(accession.trim())) {
            TermBean bean = initializeTermBean(name, def);
            bean.setIdentifier(accession.trim());
            bean.setTermPk(bean.getIdentifier() + SHORT_NAME + "0");
            return bean;
        } else {
            throw new IllegalArgumentException("Can't have a non-null term name!");
        }
    }

    /**
     * This method will convert an OBOEdit model term into a valid TermBean, while creating synonyms,
     * xrefs and annotations. The valid TermBean generated will be added to a global HashMap that will
     * be used at a later stage.
     *
     * @param obj being an OBOObject object obtained from the parser
     */
    protected void processTerm(OBOObject obj) {

        if (obj.getID().startsWith("obo:")) {
            /*
                obo:datatype
                obo:property
                obo:class
            */
            logger.debug("bogus term: " + obj.getID());
            return;
        }

        TermBean trm = new TermBean();
        //must set PK here because OJB will now not set it automatically
        //PK will be term_ac+ont+fully_loaded_false
        trm.setTermPk(safeTrim(obj.getID()) + SHORT_NAME + "0");
        if (trm.getTermPk().length() > 255) {
            throw new IllegalStateException("term PK longer than allowed database column length: " + trm.getTermPk());
        }

        //trim definition 
        trm.setDefinition(safeTrim(obj.getDefinition()));
        if (trm.getDefinition() != null && trm.getDefinition().length() > 4000) {
            logger.warn("term definition longer than allowed database column length - truncating" + trm.getIdentifier());
            trm.setDefinition(trm.getDefinition().substring(0, 4000));
        }

        //trim ID 
        trm.setIdentifier(safeTrim(obj.getID()));
        if (trm.getIdentifier() != null && trm.getIdentifier().length() > 255) {
            logger.warn("term identifier longer than allowed database column length - truncating" + trm.getIdentifier());
            trm.setIdentifier(trm.getIdentifier().substring(0, 255));
        }
        //set as root term if required
        if (rootTerms.contains(safeTrim(obj.getID()))) {
            trm.setRootTerm(true);
            logger.info(obj.getID() + " is a root term");
        }
        //trim name 
        trm.setName(safeTrim(obj.getName()));
        //trim namespace 
        if (trm.getName() != null && trm.getName().length() > 2000) {
            logger.warn("term name longer than allowed database column length - truncating" + trm.getIdentifier());
            trm.setName(trm.getName().substring(0, 2000));
        }

        Namespace nspace = obj.getNamespace();
        if (nspace != null) {
            trm.setNamespace(safeTrim(nspace.getID()));
            if (trm.getNamespace() != null && trm.getNamespace().length() > 255) {
                logger.warn("term namespace longer than allowed database column length - truncating" + trm.getIdentifier());
                trm.setNamespace(trm.getNamespace().substring(0, 255));
            }
        }
        //set if obsolete
        trm.setObsolete(obj.isObsolete());
        //set parent ontology
        trm.setParentOntology(ontBean);
        //process synonyms
        trm.setSynonyms(processSynonyms(obj, trm));
        //process xrefs
        trm.setXrefs(processXrefs(obj, trm));
        //process annotations
        trm.setAnnotations(processAnnotations(obj, trm));

        //set number of children
        int nbChild = 0;
        if (obj.getChildren() != null) {
            nbChild = obj.getChildren().size();
        }

        //set leaf status
        if (nbChild > 0) {
            trm.setLeaf(false);
        } else {
            trm.setLeaf(true);
        }

//        if (obj instanceof Instance) {
//            trm.setInstance(true);
//            //store type (eg objID is_instance_of typeID
//            instances.put(obj.getType().getID(), obj.getID());
//        } else {
//            trm.setInstance(false);
//        }

        //add to global storage
        ontologyTerms.put(trm.getIdentifier(), trm);

    }

    /**
     * internal method to create AnnotationBeans objects from values extracted from an OBOEdit term object
     * and properly setup associations to the parent OLS term objet. Annotations can include comments and
     * replacement term ids for obsolete or misused terms, as well as defined subsets.
     *
     * @param obj - the OBOEdit object to extract information from
     * @param trm - the parent term to link the annotations to
     * @return a collection of properly created AnnotationBeans
     */
    private Collection<Annotation> processAnnotations(OBOObject obj, TermBean trm) {

        ArrayList<Annotation> retval = new ArrayList<Annotation>();

        String comment = safeTrim(obj.getComment());
        if (comment != null) {
            AnnotationBean annot = new AnnotationBean();
            annot.setAnnotationStringValue(comment);
            annot.setAnnotationType(Annotation.OBO_COMMENT);
            if (comment != null && comment.length() > 2000) {
                logger.warn("annotation comment longer than allowed database column length - truncating " + trm.getIdentifier());
                annot.setAnnotationStringValue(annot.getAnnotationStringValue().substring(0, 2000));
            }

            annot.setParentTerm(trm);
            retval.add(annot);
        }

        Set<ObsoletableObject> considers = obj.getConsiderReplacements();
        for (ObsoletableObject obsolete : considers) {
            AnnotationBean annot = new AnnotationBean();
            annot.setAnnotationType(Annotation.OBO_CONSIDER_REPLACEMENT);
            String val = obsolete.getID();
            if (obsolete.getName() != null) {
                val += ": " + obsolete.getName();
            }
            annot.setAnnotationStringValue(val);
            if (val != null && val.length() > 2000) {
                logger.warn("annotation value longer than allowed database column length - truncating " + trm.getIdentifier());
                annot.setAnnotationStringValue(annot.getAnnotationStringValue().substring(0, 2000));
            }
            annot.setParentTerm(trm);
            retval.add(annot);
        }

        Set<ObsoletableObject> replacers = obj.getReplacedBy();
        for (ObsoletableObject replacedby : replacers) {
            AnnotationBean annot = new AnnotationBean();
            annot.setAnnotationType(Annotation.OBO_REPLACED_BY);
            String val = replacedby.getID();
            if (replacedby.getName() != null) {
                val += ": " + replacedby.getName();
            }
            annot.setAnnotationStringValue(val);
            if (val != null && val.length() > 2000) {
                logger.warn("annotation value longer than allowed database column length - truncating " + trm.getIdentifier());
                annot.setAnnotationStringValue(annot.getAnnotationStringValue().substring(0, 2000));
            }
            annot.setParentTerm(trm);
            retval.add(annot);
        }

        Set<TermSubset> subsets = obj.getSubsets();
        for (TermSubset subset : subsets) {
            AnnotationBean annot = new AnnotationBean();
            annot.setAnnotationType(Annotation.SUBSET + "_" + subset.getName());
            String val = subset.getDesc();
            annot.setAnnotationStringValue(val);
            if (val != null && val.length() > 2000) {
                logger.warn("annotation value longer than allowed database column length - truncating " + trm.getIdentifier());
                annot.setAnnotationStringValue(annot.getAnnotationStringValue().substring(0, 2000));
            }
            annot.setParentTerm(trm);
            retval.add(annot);
        }

        Set<PropertyValue> propVal = obj.getPropertyValues();
        for (PropertyValue pv : propVal) {
            AnnotationBean annot = new AnnotationBean();
            //property_value: EFO:definition_editor "James Malone" xsd:string
            //parses to property = property_value
            //             value = EFO:definition_editor "James Malone" xsd:string
            //so manually process the value to something more informative
            //          property = EFO:definition_editor
            //             value = "James Malone"
            try {
                if (pv.getValue() == null) {
                    //invalidly constucted property_value!
                    logger.warn("Error parsing property_value - Ignoring null value: " + pv.toString());
                    continue;
                }

                int ndx;

                //parse property type
                String tmpStr = pv.getValue();
                ndx = tmpStr.indexOf(' ');
                if (ndx > 0) {
                    tmpStr = tmpStr.substring(0, ndx).trim();
                    if (tmpStr.endsWith(":")) {
                        tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
                    }
                    logger.debug("Setting property type: " + tmpStr);
                    annot.setAnnotationType(tmpStr.trim());
                    if (annot.getAnnotationType() != null && annot.getAnnotationType().length() > 2000) {
                        logger.warn("annotation type longer than allowed database column length - truncating " + trm.getIdentifier());
                        annot.setAnnotationType(annot.getAnnotationType().substring(0, 2000));
                    }

                    //parse property value
                    tmpStr = pv.getValue();

                    tmpStr = tmpStr.substring(ndx + 1);
                    ndx = tmpStr.indexOf("xsd:");
                    if (ndx > 0) {
                        tmpStr = tmpStr.substring(0, ndx);
                    }
                    tmpStr = tmpStr.trim();
                    if (tmpStr.startsWith("\"") && tmpStr.endsWith("\"")) {
                        tmpStr = tmpStr.substring(1, tmpStr.length() - 1);
                    }
                    logger.debug("Setting property value: " + tmpStr.trim());
                    annot.setAnnotationStringValue(tmpStr.trim());
                    if (tmpStr != null && tmpStr.length() > 2000) {
                        logger.warn("annotation value longer than allowed database column length - truncating " + trm.getIdentifier());
                        annot.setAnnotationStringValue(annot.getAnnotationStringValue().substring(0, 2000));
                    }

                } else {

                    //this will capture occations where people just put random key-value tags
                    //as annotations
                    annot.setAnnotationType(pv.getProperty());
                    if (pv.getProperty() != null && pv.getProperty().length() > 2000) {
                        logger.warn("annotation type longer than allowed database column length - truncating " + trm.getIdentifier());
                        annot.setAnnotationType(annot.getAnnotationType().substring(0, 2000));
                    }
                    annot.setAnnotationStringValue(pv.getValue());
                    if (pv.getValue() != null && pv.getValue().length() > 2000) {
                        logger.warn("annotation value longer than allowed database column length - truncating " + trm.getIdentifier());
                        annot.setAnnotationStringValue(annot.getAnnotationStringValue().substring(0, 2000));
                    }

                }

                annot.setParentTerm(trm);

            } catch (RuntimeException re) {
                logger.warn("Error parsing property_value - Ignoring : " + pv.toString());
                logger.debug("pv.getProperty() = " + pv.getProperty());
                logger.debug("pv.getValue() = " + pv.getValue());
                continue;
            }
            retval.add(annot);
        }

        if ("MOD".equals(SHORT_NAME)) {

            for (Object xrObj : obj.getDbxrefs()) {

                Dbxref xref = (Dbxref) xrObj;
                if (MOD_STRING_ANNOTATIONS.contains(safeTrim(xref.getDatabase()))) {
                    //create string annotation
                    AnnotationBean annot = new AnnotationBean();
                    annot.setAnnotationType(safeTrim(xref.getDatabase()));
                    annot.setAnnotationStringValue(safeTrim(xref.getDesc()));
                    annot.setParentTerm(trm);
                    retval.add(annot);
                } else if (MOD_NUMERIC_ANNOTATIONS.contains(safeTrim(xref.getDatabase()))) {
                    //create numeric annotation
                    AnnotationBean annot = new AnnotationBean();
                    annot.setAnnotationType(safeTrim(xref.getDatabase()));
                    annot.setAnnotationDoubleValue(safeTrim(xref.getDesc()));
                    annot.setParentTerm(trm);
                    retval.add(annot);
                }
            }
        }

        return retval;
    }

    /**
     * internal method to create DbXrefBean objects from values extracted from an OBOEdit term object
     * and properly setup associations to the parent OLS term objet. Xrefs will be generated for analog
     * xrefs, definition xrefs and synonym xrefs.
     *
     * @param obj - the OBOEdit object to extract information from
     * @param trm - the parent term to link the annotations to
     * @return a collection of properly created DbXrefBean
     */
    private Collection<DbXref> processXrefs(OBOObject obj, TermBean trm) {

        HashSet<DbXref> retval = new HashSet<DbXref>();
        for (Object xrObj : obj.getDbxrefs()) {
            Dbxref xref = (Dbxref) xrObj;

            if (!"MOD".equals(SHORT_NAME)) {
                //todo - hardcode analog for now and remove it when bug is fixed
                //fix oboedit codebase error
                retval.add(createDbXref(xref, Dbxref.ANALOG));
            } else {
                if (MOD_STRING_ANNOTATIONS.contains(safeTrim(xref.getDatabase()))) {
                    //do nothing - annotation will be created later
                } else if (MOD_NUMERIC_ANNOTATIONS.contains(safeTrim(xref.getDatabase()))) {
                    //do nothing - annotation will be created later
                } else {
                    //todo - hardcode analog for now and remove it when bug is fixed
                    //fix oboedit codebase error
                    retval.add(createDbXref(xref, Dbxref.ANALOG));
                }
            }
        }

        //todo - remove this when bug is fixed
        for (Object xrObj : obj.getDefDbxrefs()) {
            Dbxref xref = (Dbxref) xrObj;
            retval.add(createDbXref(xref, Dbxref.DEFINITION));
        }

        return retval;

    }

    /**
     * helper method to create and populate a DbXrefBean object from an OBOEdit Dbxref object
     *
     * @param xref     - the OBOEdit object to extract information from
     * @param xrefType - the xref type
     * @return a valid OLS model DbXrefBean object
     */
    private DbXref createDbXref(Dbxref xref, int xrefType) {

        DbXrefBean retval = new DbXrefBean();
        retval.setDbName(safeTrim(xref.getDatabase()));
        if (retval.getDbName() != null && retval.getDbName().length() > 255) {
            logger.warn("dbxref dbname longer than allowed database column length - truncating " + retval.getDbName());
            retval.setDbName(retval.getDbName().substring(0, 255));
        }
        //stupid oboeit artifact
        if (xref.getDatabaseID() != null && !xref.getID().trim().equals("none")) {
            retval.setAccession(safeTrim(xref.getDatabaseID()));
//            if (retval.getAccession() != null && retval.getAccession().length() > 512) {
            if (retval.getAccession() != null && retval.getAccession().length() > 256) {
//                System.out.println("retval.getAccession().length() = " + retval.getAccession().length());
                logger.warn("dbxref accession longer than allowed database column length: " + retval.getAccession());
                retval.setAccession(retval.getAccession().substring(0, 256));
//                retval.setAccession(retval.getAccession().substring(0, 512));
            }
        }
        //stupid oboeit artifact
        if (xref.getDesc() != null && !xref.getDesc().trim().equals("none")) {
            retval.setDescription(safeTrim(xref.getDesc()));
            if (retval.getDescription() != null && retval.getDescription().length() > 2000) {
                logger.warn("dbxref description longer than allowed database column length: " + retval.getDescription());
                retval.setDescription(retval.getDescription().substring(0, 2000));
            }
        }
        retval.setXrefType(xrefType);
        return retval;

    }

    /**
     * Once all the term objects have been created, this method will create the relationships and paths
     * to link all the children terms to a given parent term and update the OLS TermBean object with
     * the proper information from the global term storage.
     *
     * @param obj - the OBOEdit term object that will be used to extract information from
     */
    private void processTermRelationships(OBOObject obj) {

        if (obj.getID().startsWith("obo:")) {
            /*
                obo:datatype
                obo:property
                obo:class
            */
            logger.debug("bogus term: " + obj.getID());
            return;
        }

        TermBean trm = (TermBean) ontologyTerms.get(safeTrim(obj.getID()));
        trm.setRelationships(processRelationships(obj, trm));
        trm.setPaths(processPaths(obj, trm));

        //update term object
        ontologyTerms.put(trm.getIdentifier(), trm);
    }

    /**
     * internal helper method to create TermRelationshipBeans for a given term.
     * <pre>
     *        term1
     *            |_ child1        child1 IS_A term1
     *            |_ child2        child2 IS_A term1
     *                             subject pred object
     * </pre>
     *
     * @param obj - the OBOEdit term object to extract information from
     * @param trm - the OLS parent term to link to
     * @return a Collection of valid TermRelationshipBeans
     */
    private Collection<TermRelationship> processRelationships(OBOObject obj, TermBean trm) {

        HashSet<TermRelationship> retval = new HashSet<TermRelationship>();

        Collection<Link> children = obj.getChildren();
        for (Link lnk : children) {
            //get the child term from the link
            //use its ID to lookup in the map we created
            //if null, continue
            /*
              term1
                  |_ child1        child1 IS_A term1
                  |_ child2        child2 IS_A term1
                                   subject pred object
            */
            Term childTrm = ontologyTerms.get(safeTrim(lnk.getChild().getID()));
            if (childTrm != null) {
                TermRelationshipBean trb = new TermRelationshipBean();
                trb.setSubjectTerm(childTrm);
                trb.setObjectTerm(trm);

                //set predicate type
                if (IS_A_SET.contains(lnk.getType().getID())) {
                    trb.setPredicateTerm(IS_A);
                } else if (PART_OF_SET.contains(lnk.getType().getID())) {
                    trb.setPredicateTerm(PART_OF);
                } else if (DEV_FROM_SET.contains(lnk.getType().getID())) {
                    trb.setPredicateTerm(DEVELOPS_FROM);
                } else {
                    TermBean otherRelation = getUnknownRelationTermBean(lnk.getType().getID());
                    if (otherRelation != null) {
                        trb.setPredicateTerm(otherRelation);
                    } else {
                        logger.warn("Unable to create unknown relation type: >" + lnk.getType().getID() + "<");
                        continue;
                    }
                }
                //set ontology
                trb.setParentOntology(ontBean);
                //add to retval
                retval.add(trb);
            } else {
                logger.debug("No object term found for link: " + lnk.toString());
            }
        }
        return retval;
    }

    /**
     * helper method to create unknow relationship terms as they are required. These terms will also
     * be added to the global term storage for persistence with the ontology.
     *
     * @param relationType - the string that defines the relationship from the ontology
     * @return a valid TermBean
     */
    protected TermBean getUnknownRelationTermBean(String relationType) {

        TermBean retval = null;
        if (relationType != null) {
            retval = (TermBean) unknown_relations.get(relationType.trim().toUpperCase());
            if (retval == null) {
                retval = initializeTermBean(relationType.trim(), Loader.RELATION_TYPE);
                logger.info("Created unkonwn relation type: " + relationType);
                unknown_relations.put(relationType.trim().toUpperCase(), retval);
                //add to storage map so it'll get persisted with the rest
                ontologyTerms.put(retval.getIdentifier(), retval);
            }
        }

        return retval;

    }

    /**
     * internal helper method to create TermPathBeans for a given term. This method will
     * precompute all paths from a parent to all its children for the 3 major relationship types:
     * IS_A, PART_OF and DEVELOPS_FROM. The PART_OF and DEVELOPS_FROM relations can traverse IS_A
     * relations for maximal completeness and still be semantically correct, but IS_A relationships
     * cannot traverse other relation types.
     * <pre>
     *        term1
     *            |_ child1        child1 IS_A term1
     *            |_ child2        child2 IS_A term1
     *                             subject pred object
     * </pre>
     *
     * @param obj - the OBOEdit term object to extract information from
     * @param trm - the OLS parent term to link to
     * @return a Collection of valid TermRelationshipBeans
     */
    private Collection<TermPath> processPaths(OBOObject obj, TermBean trm) {

        HashSet<TermPath> retval = new HashSet<TermPath>();

        HashMap<String, Integer> paths = parser.computeChildPaths(1, IS_A_SET, obj);
        retval.addAll(createTermPathBeans(paths, Constants.IS_A_RELATION_TYPE_ID, IS_A, trm));

        //the part_of relation can traverse is_a relations to generate term_paths
        //so the set passed to computeChildPaths needs to contain both PART_OF and IS_A labels.
        HashSet<String> traversingSet = new HashSet<String>();
        traversingSet.addAll(PART_OF_SET);
        traversingSet.addAll(IS_A_SET);
        paths = parser.computeChildPaths(1, traversingSet, obj);
        retval.addAll(createTermPathBeans(paths, Constants.PART_OF_RELATION_TYPE_ID, PART_OF, trm));

        //the dev_from relation can traverse is_a relations to generate term_paths
        //so the set passed to computeChildPaths needs to contain both DEV_FROM and IS_A labels.
        traversingSet.clear();
        traversingSet.addAll(DEV_FROM_SET);
        traversingSet.addAll(IS_A_SET);
        paths = parser.computeChildPaths(1, traversingSet, obj);
        retval.addAll(createTermPathBeans(paths, Constants.DEVELOPS_FROM_RELATION_TYPE_ID, DEVELOPS_FROM, trm));

        return retval;
    }

    /**
     * Internal method that actually does all the precomputing of paths
     */
    private Collection<TermPath> createTermPathBeans(HashMap<String, Integer> paths, int relationTypeId,
                                                     TermBean relationBean, TermBean trm)
    {
        HashSet<TermPath> retval = new HashSet<TermPath>();

        //get the child term from the link
        //use its ID to lookup in the map we created
        //if null, continue
        /*
          term1
              |_ child1        child1 IS_A term1
              |_ child2        child2 IS_A term1
                               subject pred object
        */
        Term objTrm;
        for (String termId : paths.keySet()) {
            //key = termID, value = distance
            int distance = paths.get(termId);
            objTrm = ontologyTerms.get(termId);
            if (objTrm != null) {
                //create bean
                TermPathBean tpb = new TermPathBean();
                //set distance
                tpb.setDistance(distance);
                //set subject term
                tpb.setSubjectTerm(objTrm);
                //set object
                tpb.setObjectTerm(trm);
                //set predicateTerm - is_a, part_of, develops_from
                tpb.setPredicateTerm(relationBean);
                //set relationshipType
                tpb.setRelationshipTypeId((long) relationTypeId);
                //set ontology
                tpb.setParentOntology(ontBean);
                //add to retval
                retval.add(tpb);
            } else {
                logger.debug("No object term found for term path: " + trm.getIdentifier() + "->" + termId);
            }
        }
        return retval;
    }

    private String getSynonymTypeDef(int scope) {
        switch (scope) {
            case Synonym.EXACT_SYNONYM:
                return "Exact synonym type";
            case Synonym.NARROW_SYNONYM:
                return "Narrow synonym type";
            case Synonym.BROAD_SYNONYM:
                return "Broad synonym type";
            case Synonym.RELATED_SYNONYM:
                return "Related synonym type";
            case Synonym.UNKNOWN_SCOPE:
            default:
                return "Unknown synonym type";
        }
    }


    /**
     * Builds the synonyms for a given term
     *
     * @param obj OBOObject representing the term
     * @param trm TermBean to link to the TermSynonym objects being created
     * @return a collection of properly constructed and linked TermSynonymBean objects
     */
    private Collection<TermSynonym> processSynonyms(OBOObject obj, TermBean trm) {

        HashSet<TermSynonym> retval = new HashSet<TermSynonym>();

        //loop over synonyms
        Set<Synonym> syns = obj.getSynonyms();
        int synCount = 1;
        for (Synonym aSyn : syns) {

            TermSynonymBean tsb = new TermSynonymBean();
            //link parent term
            tsb.setParentTerm(trm);

            String synVal = safeTrim(aSyn.getText());
            if (synVal != null) {

                //set value
                tsb.setSynonym(synVal);

                if (synVal.length() > 2000) {
                    logger.warn("synonym value longer than allowed database column length - truncating " + trm.getIdentifier());
                    tsb.setSynonym(tsb.getSynonym().substring(0, 2000));
                }

                //check to see if there's a defined synonymType for it
                if (aSyn.getSynonymType() != null) {

                    logger.debug("using user-defined synonym type: " + aSyn.getSynonymType().getName());
                    Term synTrm = ontologyTerms.get(aSyn.getSynonymType().getID());
                    if (synTrm != null) {
                        tsb.setSynonymType(synTrm);
                    } else {
                        throw new IllegalStateException(
                                "Attempting to use user-defined synonym type that has not been initialized in common objects: "
                                        + aSyn.getSynonymType().getID()
                        );
                    }

                } else {

                    //logger.debug("using old-style synonym types");
                    //link synonymType Term
                    switch (aSyn.getScope()) {
                        case Synonym.EXACT_SYNONYM:
                            tsb.setSynonymType(EXACT);
                            break;
                        case Synonym.NARROW_SYNONYM:
                            tsb.setSynonymType(NARROW);
                            break;
                        case Synonym.BROAD_SYNONYM:
                            tsb.setSynonymType(BROAD);
                            break;
                        case Synonym.RELATED_SYNONYM:
                            tsb.setSynonymType(RELATED);
                            break;
                        case Synonym.UNKNOWN_SCOPE:
                        default:
                            tsb.setSynonymType(SYNONYM);
                            break;
                    }

                }

                Collection<Dbxref> oboSynXrefs = aSyn.getXrefs();
                if (oboSynXrefs != null) {
                    Collection<DbXref> xrefs = new HashSet<DbXref>();
                    for (Dbxref xref : oboSynXrefs) {
                        xrefs.add(createDbXref(xref, Dbxref.RELATED_SYNONYM));
                    }
                    tsb.setSynonymXrefs(xrefs);
                }

                //set synonym primary key because it is no longer being set by OJB
                //use syncount to avoid tsb hashcode collisions for a single trm
                tsb.setSynonymPk(SHORT_NAME + (synCount++ * tsb.hashCode()) + "!" + trm.getTermPk());
                if (tsb.getSynonymPk().length() > 255) {
                    throw new IllegalStateException("synonym PK longer than allowed database column length: " + tsb.getSynonymPk());
                }

                //add TermSynonym to retval collection
                retval.add(tsb);

            } else {
                logger.debug("Null Synonym value encountered for " + trm.getIdentifier());
            }

        }

        //check for alt_ids
        Set<String> altIDs = obj.getSecondaryIDs();
        String altID;
        for ( Iterator<String> i = altIDs.iterator(); i.hasNext(); ) {
            altID = i.next();
            TermSynonymBean tsb = new TermSynonymBean();
            //link parent term
            tsb.setParentTerm(trm);
            //set def
            tsb.setSynonym(safeTrim(altID));
            if (altID != null && altID.length() > 2000) {
                logger.warn("synonym value longer than allowed database column length - truncating " + trm.getIdentifier());
                tsb.setSynonym(tsb.getSynonym().substring(0, 2000));
            }

            //set synType
            tsb.setSynonymType(ALT_ID);

            //set synonym primary key because it is no longer being set by OJB
            //use syncount to avoid tsb hashcode collisions for a single trm
            tsb.setSynonymPk(SHORT_NAME + (synCount++ * tsb.hashCode()) + "!" + trm.getTermPk());
            if (tsb.getSynonymPk().length() > 255) {
                throw new IllegalStateException("synonym PK longer than allowed database column length: " + tsb.getSynonymPk());
            }

            //add TermSynonym to retval collection
            retval.add(tsb);
        }

        //if main termID is URL, eg http://www.ebi.ac.uk/EFO_1234
        //add synonym with just final portion of URL, eg EFO_1234
        try {
            if (trm.getIdentifier().toLowerCase().startsWith("http:")) {
                altID = trm.getIdentifier().substring(trm.getIdentifier().lastIndexOf("/") + 1);

                TermSynonymBean tsb = new TermSynonymBean();
                //link parent term
                tsb.setParentTerm(trm);
                //set def
                tsb.setSynonym(safeTrim(altID));
                if (altID != null && altID.length() > 2000) {
                    logger.warn("synonym value longer than allowed database column length - truncating " + trm.getIdentifier());
                    tsb.setSynonym(tsb.getSynonym().substring(0, 2000));
                }

                //set synType
                tsb.setSynonymType(ALT_ID);

                //set synonym primary key because it is no longer being set by OJB
                //use syncount to avoid tsb hashcode collisions for a single trm
                tsb.setSynonymPk(SHORT_NAME + (synCount++ * tsb.hashCode()) + "!" + trm.getTermPk());
                if (tsb.getSynonymPk().length() > 255) {
                    throw new IllegalStateException("synonym PK longer than allowed database column length: " + tsb.getSynonymPk());
                }

                //add TermSynonym to retval collection
                retval.add(tsb);

            }
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Could not create alt_id from URL from term: " + trm.getIdentifier());
        }

        return retval;
    }

    /**
     * for debugging
     */
    protected void dumpOntology() {

        for (String id : getRootTerms()) {
            logger.debug("Root term: " + id);
            dumpTerm(ontologyTerms.get(id), "");
        }
    }

    /**
     * for debugging
     */
    protected void dumpOntologyStats() {

        int ts = 0, tp = 0, tr = 0, ta = 0;
        for (Term tb : ontologyTerms.values()) {

            if (tb.getSynonyms() != null) {
                ts += tb.getSynonyms().size();
            }
            if (tb.getPaths() != null) {
                tp += tb.getPaths().size();
            }
            if (tb.getRelationships() != null) {
                tr += tb.getRelationships().size();
            }
            if (tb.getAnnotations() != null) {
                ta += tb.getAnnotations().size();
            }
        }

        logger.info("Number of terms: " + ontologyTerms.size());
        logger.info("Number of synonyms: " + ts);
        logger.info("Number of relationships: " + tr);
        logger.info("Number of paths: " + tp);
        logger.info("Number of annotations: " + ta);

    }


    /**
     * for debugging
     *
     * @param term   - term to dump
     * @param indent - spaces to indent
     */
    protected void dumpTerm(Term term, String indent) {

        if (indent.length() > 15) {
            return;
        }
        if (term != null) {
            logger.debug(indent + "id: " + term.getIdentifier());
            logger.debug(indent + "name: " + term.getName());
            if (term.getSynonyms() != null)
                logger.debug(indent + "nb syn: " + term.getSynonyms().size());
            if (term.getAnnotations() != null)
                logger.debug(indent + "nb annot: " + term.getAnnotations().size());
            if (term.getRelationships() != null) {
                for (TermRelationship tr : term.getRelationships()) {
                    String relationStr = indent + tr.getSubjectTerm().getName() + " " + tr.getPredicateTerm().getName() + " " + tr.getObjectTerm().getName();
                    logger.debug(relationStr);
                    dumpTerm(tr.getSubjectTerm(), indent + " ");
                }
            }
        }
    }

    /**
     * takes a string and trims whitespace. if resulting string is empty, return null;
     *
     * @param inStr string to trim; if null, return null
     */
    protected String safeTrim(String inStr) {
        if (inStr != null) {
            String tmp = inStr.trim();
            if (tmp.length() > 0) {
                return tmp;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * returns the OntologyAccess that has been loaded from file.
     *
     * @return returns the OntologyAccess that has been loaded from file.
     * @throws IllegalStateException if the bean has not been properly initialized.
     */
    public Ontology getOntology() throws IOException {
        if (ontBean != null)
            return ontBean;
        else {
            throw new IllegalStateException("OntologyAccess bean not properly initialized. " +
              "Did you call the proper sequence of methods: configure(), parse(), process()?");
        }
    }

}