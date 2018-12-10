package org.biopax.validator;

import java.util.*;

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.normalizer.Normalizer;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.psidev.ontology_manager.*;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.beans.Validation;

import javax.annotation.PostConstruct;


/**
 * A BioPAX L3 controlled vocabulary context-aware term validation rule.
 *
 * @author rodch
 */
public abstract class CvTermsRule<T extends Level3Element> extends AbstractCvRule<T>
{

  /**
   * Constructor.
   *
   * TODO: (a feature) to allow using a path in 'property' parameter (e.g., "modificationFeature/modificationType")
   *
   * @param domain biopax property domain (object)
   * @param property biopax property name
   * @param restrictions restrictions on the controlled vocabulary (ontology) terms, given biopax property context
   */
  public CvTermsRule(Class<T> domain, String property, CvRestriction... restrictions)
  {
    super(domain, property, restrictions);
  }

  @PostConstruct
  @Override
  public void init() {
    super.init();
    this.editor = (property != null && !ControlledVocabulary.class.isAssignableFrom(domain))
      ? SimpleEditorMap.L3.getEditorForProperty(property, this.domain)
      : null;
  };


  public void check(Validation validation, T thing) {
    // a set of CVs for this rule to validate
    Collection<ControlledVocabulary> vocabularies = new HashSet<>();

    // if the editor is null, we expect a ControlledVocabulary object!
    if(editor == null) {
      vocabularies.add((ControlledVocabulary)thing);
    } else {
      vocabularies.addAll((Collection<ControlledVocabulary>) editor.getValueFromBean(thing));
    }

    // shortcut
    if(vocabularies.isEmpty()) return;

    // text to report in any CV error case
    String cvRuleInfo = ((editor != null) ? " property: "
      + property : "") + " " + restrictions.toString();

    // check each CV terms against the restrictions
    for (ControlledVocabulary cv : vocabularies)
    {
      if (cv == null) {
        logger.warn(thing
          + " referes to 'null' controlled vocabulary (bug!): "
          + ", domain: " + domain + ", property: " + property);
      } else if(cv.getTerm().isEmpty()) {
        /* won't report/fix what other rules (e.g., 'controlledVocabularyTermCRRule') or Normalizer do */
      }
      else {
        //TODO: check if multiple terms are synonyms (equivalent)

        final Set<String> badTerms = new HashSet<>(); // initially - none
        final Map<String, Set<OntologyTermI>> noXrefTerms = new HashMap<>();
        //original terms set to iterate over (to avoid concurrent modification exceptions - other rules can modify the set simultaneously)
        final Set<String> terms = Collections.unmodifiableSet(new HashSet<>(cv.getTerm()));

        // first, check terms (names) are valid
        for(String name : terms)
        {
          if(!getValidTerms().contains(name.toLowerCase())) {
            // save to report/delete/replace the invalid term later
            badTerms.add(name);
          }
        }
        // report but keep original perhaps illegal terms
        if (!badTerms.isEmpty()) {
          String badTermInfo = badTerms.toString();
          error(validation, thing, "illegal.cv.term", false, badTermInfo, cvRuleInfo);
        }

        /* check if unif. xref.id points to invalid term,
         * and, if so, report 'illegal.cv.xref' error
         */
        final Set<UnificationXref> badXrefs = new HashSet<>();
        for (UnificationXref x : new ClassFilterSet<>(
          cv.getXref(), UnificationXref.class))
        {
          OntologyTermI ot = ontologyUtils.getOntologyManager().findTermByAccession(x.getId());
          if(ot == null || !getValidTerms().contains(ot.getPreferredName().toLowerCase())) {
            badXrefs.add(x);
          }
        }
        // report wrong uni.xrefs
        if(!badXrefs.isEmpty()) {
          String bads = badXrefs.toString();
          // report as not fixed error case (won't fix/remove such xrefs, keep original)
          error(validation, thing, "illegal.cv.xref", false, bads, cvRuleInfo);
        }

        // check valid terms have a uni.xref
        for(String name : terms)
        {
          // only for valid terms
          if(getValidTerms().contains(name.toLowerCase())) {
            // check if there is the corresponding unification xref
            Set<OntologyTermI> ots = ontologyUtils
              .getOntologyManager().searchTermByName(name.toLowerCase(), getOntologyIDs());
            assert(!ots.isEmpty()); // shouldn't be, because the above getValidTerms() contains the name
            boolean noXrefsForTermNameFound = true; // next, - prove otherwise is the case
            terms: for(OntologyTermI term : ots) {
              String id = term.getTermAccession();
              // search for the xref with the same xref.id
              for (UnificationXref x : new ClassFilterSet<>(
                cv.getXref(), UnificationXref.class)) {
                if(id.equalsIgnoreCase(x.getId()))  {
                  noXrefsForTermNameFound = false;
                  break terms; // exit this and outer loops!
                }
              }
            }

            if(noXrefsForTermNameFound)
              noXrefTerms.put(name, ots); //store terms to fix later (to generate xrefs)
          }
        }

        // note: at this point, 'noXrefTerms' (valid terms only) map is defined...
        if (!noXrefTerms.isEmpty()) {
          String noXrefTermsInfo = noXrefTerms.toString();
          boolean fixed = false;

          if(validation.isFix()) {
            /*
             * However, it's not so trivial to fix by adding the xrefs, because:
             * 1) no reference to the parent Model here available
             *    (thus the validator must detect and add new objects automatically! [done!])
             * 2) having the chance of creating several xrefs with the same RDFId requires
             *    a special care or follow-up merging, as simply adding them to a model will
             *    throw the "already have this element" exception!); and other rules
             *    can also generate duplicates...
             * 3) risk that a rule generating/adding a new element may cause
             *    other rules to interfere via AOP and prevent changes in quite
             *    unpredictable manner (...bites its own tail)
             * 4) multiple terms (accession numbers) can result from searching by (synonym) name
             *
             *    Well, let's try to fix, anyway (and modifying ValidatorImpl as well)!
             *    That's awesome!
             */
            Set<OntologyTermI> validTermIs = ontologyUtils.getValidTerms(this);
            for (String name : noXrefTerms.keySet()) {
              //get previously saved valid ontology term beans by name
              Set<OntologyTermI> ots = noXrefTerms.get(name);
              //get only top (parent) valid terms
              Set<OntologyTermI> topvalids = new HashSet<>();
              for (OntologyTermI term : ots) {
                // skip terms that are not applicable although having the same synonym name
                if(validTermIs.contains(term)) {
                  OntologyAccess ont = ontologyUtils.getOntologyManager().getOntology(term.getOntologyId());
                  //if term's parents does not contain any of these terms
                  if(Collections.disjoint(ots, ont.getAllParents(term))) {
                    topvalids.add(term);
                  }
                }
              }
              Set<String> added = new HashSet<>();
              for (OntologyTermI term : topvalids) {
                String ontId = term.getOntologyId();
                String db = ontologyUtils.getOntologyManager().getOntology(ontId).getName();
                String id = term.getTermAccession();
                // auto-create and add the xref to the cv;
                // generate an URI in the same namespace
                String uri = Normalizer.uri(cv.getUri() + "_", db, id, UnificationXref.class);
                if(!added.contains(uri)) {
                  added.add(uri);
                  UnificationXref ux = BioPAXLevel.L3.getDefaultFactory()
                    .create(UnificationXref.class, uri);
                  ux.setDb(db);
                  ux.setId(id);
                  cv.addXref(ux);
                  fixed = true; // 99% true ;-)
                  noXrefTermsInfo += "; " + id + " added!";
                }
              }
            }
          }

          // report
          error(validation, thing, "no.xref.cv.terms",
            fixed, noXrefTermsInfo, cvRuleInfo);
        }

        //if in the fixing mode,
        if (validation != null && validation.isFix()
          //and there were some errors found,
          && !(badTerms.isEmpty() && noXrefTerms.isEmpty()))
        {
          //then add/infer the valid preferred term from the unification xrefs -
          Set<String> addTerms = createTermsFromUnificationXrefs(cv);
          if (!addTerms.isEmpty()) {
            cv.getTerm().addAll(addTerms);
          }
        }

      }
    }
  }

  //discover valid terms by unification xrefs (invalid xrefs won't get you anything)
  private Set<String> createTermsFromUnificationXrefs(
    ControlledVocabulary cv)
  {
    Set<String> inferred = new HashSet<>();
    for (UnificationXref x : new ClassFilterSet<>(
      cv.getXref(), UnificationXref.class))
    {
      OntologyTermI ot = ontologyUtils.getOntologyManager().findTermByAccession(x.getId());
      //if found and valid
      if (ot != null && getValidTerms().contains(ot.getPreferredName().toLowerCase())) {
        inferred.add(ot.getPreferredName());
      }
      else if(ot == null)
        logger.warn("No term found by the xref.id: " + x.getId());
      else
        logger.debug("Invalid (for this CV context) term: " + x.getId());
    }

    return inferred;
  }

}
