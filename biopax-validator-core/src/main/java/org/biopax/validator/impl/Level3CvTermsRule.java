package org.biopax.validator.impl;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.springframework.beans.factory.annotation.Configurable;

import psidev.ontology_manager.OntologyManager;
import psidev.ontology_manager.OntologyTermI;

/**
 * An abstract class for CV terms checking (BioPAX Level3).
 * 
 * @author rodch
 */
@Configurable
public abstract class Level3CvTermsRule<T extends Level3Element> 
	extends AbstractCvRule<T> {
    
    @Resource
    private EditorMap editorMap3;
  
    /**
     * Constructor.
     * 
     * TODO allow using properties path as the 'property' parameter, i.e., "modificationFeature/modificationType"
     * 
     * @param domain
     * @param property
     * @param restrictions
     */
    public Level3CvTermsRule(Class<T> domain, String property, CvTermRestriction... restrictions)
    {
    	super(domain, property, restrictions);
    }
   
    @PostConstruct
    public void init() {
    	super.init();
		this.editor = (property != null && !ControlledVocabulary.class.isAssignableFrom(domain)) 
			? editorMap3.getEditorForProperty(property, this.domain)
			: null;    	
    };
    
    
	public void check(T thing, boolean fix) {
		// a set of CVs for this rule to validate
		Collection<ControlledVocabulary> vocabularies = new HashSet<ControlledVocabulary>();
		
		// if the editor is null, we expect a ControlledVocabulary object!
		if(editor == null) {
			vocabularies.add((ControlledVocabulary)thing); 
		} else if(editor.isMultipleCardinality()) {
			vocabularies = (Collection<ControlledVocabulary>) editor.getValueFromBean(thing);
		} else {
			ControlledVocabulary value = (ControlledVocabulary) editor.getValueFromBean(thing);
			if(value != null) vocabularies.add(value);
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
				/* won't report/fix what other rules (e.g., 'controlledVocabularyTermCRRule') 
				 * or Normalizercan do! */
			} 
			else {
				//TODO (advanced feature, can/must be a separate rule) check if multiple terms are, in fact, synonyms/equivalent...
				
				final Set<String> badTerms = new HashSet<String>(); // initially - none
				final Set<String> noXrefTerms = new HashSet<String>(cv.getTerm()); // start with all...
				
				// check terms (names)
				for(String name : cv.getTerm()) 
				{
					if(!getValidTerms().contains(name.toLowerCase())) {
						// will report/delete/replace the invalid term below...
						badTerms.add(name);
						noXrefTerms.remove(name); // won't check for its having a valid xref...
					} else { 
						// term is valid; - check whether there is a unification xrefs about this term!
						Set<OntologyTermI> ots = ((OntologyManager) ontologyManager).searchTermByName(name.toLowerCase());
						assert(!ots.isEmpty()); // shouldn't be, because the above getValidTerms() contains the name
						for(OntologyTermI term : ots) {
							String prefname = term.getPreferredName();
							String ontId = term.getOntologyId(); // e.g., "GO" 
							String db = ((OntologyManager) ontologyManager).getOntology(ontId).getName();
							String id = term.getTermAccession();
							// search for the xref with the same xref.id
							for (UnificationXref x : new ClassFilterSet<UnificationXref>(
									cv.getXref(), UnificationXref.class)) {
								/// exclude names "matching" uni. xrefs from the noXrefTerms set
								if(id.equalsIgnoreCase(x.getId())) 
									noXrefTerms.remove(name);
							}
							// TODO fix by creating a new xref (can be tricky/fuzzy.., but possible in principle)
						}
					}
				}
				
				//check that terms that can be inferred from the xref.id are valid (report 'illegal.cv.xref')!
				final Set<UnificationXref> badXrefs = new HashSet<UnificationXref>(); // initially - none
				for (UnificationXref x : new ClassFilterSet<UnificationXref>(
						cv.getXref(), UnificationXref.class)) {
					OntologyTermI ot = ((OntologyManager) ontologyManager).findTermByAccession(x.getId());
					if(ot == null || !getValidTerms().contains(ot.getPreferredName().toLowerCase())) {
						badXrefs.add(x);
					}
				}
				
				// fix / report wrong uni.xrefs (important: before fixing wrong terms!)
				if(!badXrefs.isEmpty()) {
					String bads = badXrefs.toString();
					if(fix) {
						cv.getXref().removeAll(badXrefs);
						bads += " were removed!";
						error(thing, "illegal.cv.xref", true, // fixed!
								bads, cvRuleInfo);
					} else {
						error(thing, "illegal.cv.xref", false, // not fixed!
								bads, cvRuleInfo);
					}
				}
									
				// fix / report wrong terms
				if (!badTerms.isEmpty()) {	
					String badTermInfo = badTerms.toString();
					
					if (fix) {
						cv.getTerm().removeAll(badTerms);
						badTermInfo += " were removed";
						
						// try infer term names from the (valid) unification xrefs!
						Set<String> addTerms = createTermsFromUnificationXrefs(cv);
						if (!addTerms.isEmpty()) {
							cv.getTerm().addAll(addTerms);
							badTermInfo += "; terms added " +
								"(inferred from the unification xref(s)): "
									+ addTerms.toString();
						}
						
						error(thing, "illegal.cv.term", true, // fixed!
								badTermInfo, cvRuleInfo);

					} else { // report not fixed
						error(thing, "illegal.cv.term", false, // not fixed
								badTermInfo, cvRuleInfo);
					}
				}
				
				if (!noXrefTerms.isEmpty()) {	
					error(thing, "no.xref.cv.terms", false, 
						noXrefTerms.toString(), cvRuleInfo);
				}
			} 
		}
	}
		
	
	private Set<String> createTermsFromUnificationXrefs(
			ControlledVocabulary cv) 
	{		
		Set<String> inferred = new HashSet<String>();
		for (UnificationXref x : new ClassFilterSet<UnificationXref>(
				cv.getXref(), UnificationXref.class)) 
		{
			OntologyTermI ot = ((OntologyManager) ontologyManager)
					.findTermByAccession(x.getId());
			if (ot != null) {
				inferred.add(ot.getPreferredName());
			} 
			else {
				if (logger.isWarnEnabled())
					logger.warn("Could not find a term by the xref.id: " + x.getId());
			}
		}
		
		return inferred;
	}       
	
}
