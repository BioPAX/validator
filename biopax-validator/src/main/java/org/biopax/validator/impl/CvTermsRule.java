package org.biopax.validator.impl;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.*;

import javax.annotation.PostConstruct;

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.psidev.ontology_manager.*;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Normalizer;


/**
 * An abstract class for CV terms checking (BioPAX Level3).
 * 
 * @author rodch
 */
public abstract class CvTermsRule<T extends Level3Element> 
	extends AbstractCvRule<T> {
  
    /**
     * Constructor.
     * 
     * TODO a feature: to allow using a properties path in the 'property' parameter, like "modificationFeature/modificationType"
     * 
     * @param domain
     * @param property
     * @param restrictions
     */
    public CvTermsRule(Class<T> domain, String property, CvRestriction... restrictions)
    {
    	super(domain, property, restrictions);
    }
   
    @PostConstruct
    public void init() {
    	super.init();
		this.editor = (property != null && !ControlledVocabulary.class.isAssignableFrom(domain)) 
			? SimpleEditorMap.L3.getEditorForProperty(property, this.domain)
			: null;    	
    };
    
    
	public void check(Validation validation, T thing) {
		// a set of CVs for this rule to validate
		Collection<ControlledVocabulary> vocabularies = new HashSet<ControlledVocabulary>();
		
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
				/* won't report/fix what other rules (e.g., 'controlledVocabularyTermCRRule') 
				 * or Normalizercan do! */
			} 
			else {
				//TODO (an advanced feature, a separate rule - ) to check if multiple terms are synonyms (equivalent)...
				
				final Set<String> badTerms = new HashSet<String>(); // initially - none
				final Map<String, Set<OntologyTermI>> noXrefTerms = new HashMap<String, Set<OntologyTermI>>();				
				//original terms set to iterate over (to avoid concurrent modification exceptions - other rules can modify the set simultaneously)
				final Set<String> terms = Collections.unmodifiableSet(new HashSet<String>(cv.getTerm()));
				
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
				final Set<UnificationXref> badXrefs = new HashSet<UnificationXref>();
				for (UnificationXref x : new ClassFilterSet<Xref,UnificationXref>(
						cv.getXref(), UnificationXref.class)) 
				{
					OntologyTermI ot = ((OntologyManager) ontologyManager).findTermByAccession(x.getId());
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
						Set<OntologyTermI> ots = ((OntologyManager) ontologyManager)
								.searchTermByName(name.toLowerCase(), getOntologyIDs());
						assert(!ots.isEmpty()); // shouldn't be, because the above getValidTerms() contains the name
						boolean noXrefsForTermNameFound = true; // next, - prove otherwise is the case
						terms: for(OntologyTermI term : ots) {
							String id = term.getTermAccession();
							// search for the xref with the same xref.id
							for (UnificationXref x : new ClassFilterSet<Xref,UnificationXref>(
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
						Set<OntologyTermI> validTermIs = ontologyManager.getValidTerms(this);
						for (String name : noXrefTerms.keySet()) {
							//get previously saved valid ontology term beans by name
							Set<OntologyTermI> ots = noXrefTerms.get(name);
							//get only top (parent) valid terms
							Set<OntologyTermI> topvalids = new HashSet<OntologyTermI>();
							for (OntologyTermI term : ots) {
								// skip terms that are not applicable although having the same synonym name
								if(validTermIs.contains(term)) {
									Ontology ont = ((OntologyManager)ontologyManager).getOntology(term.getOntologyId());
									//if term's parents does not contain any of these terms
									if(Collections.disjoint(ots, ont.getAllParents(term))) {
										topvalids.add(term);
									}
								}
							}
							Set<String> added = new HashSet<String>();
							for (OntologyTermI term : topvalids) {								
								String ontId = term.getOntologyId();
								String db = ((OntologyManager) ontologyManager).getOntology(ontId).getName();
								String id = term.getTermAccession();
								// auto-create and add the xref to the cv;
								// generate an URI in the same namespace
								String uri = Normalizer.uri(cv.getRDFId()+"_", db, id, UnificationXref.class);
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
			}
			
			//if fixing, finally, add valid preferred term by xref
			if (validation!=null && validation.isFix()) {
				Set<String> addTerms = createTermsFromUnificationXrefs(cv);
				if (!addTerms.isEmpty()) {
					cv.getTerm().addAll(addTerms);
				}
			}
		}
	}
		
	//discover valid terms by unification xrefs (invalid xrefs won't get you anything)
	private Set<String> createTermsFromUnificationXrefs(
			ControlledVocabulary cv) 
	{		
		Set<String> inferred = new HashSet<String>();
		for (UnificationXref x : new ClassFilterSet<Xref,UnificationXref>(
				cv.getXref(), UnificationXref.class)) 
		{
			OntologyTermI ot = ((OntologyManager) ontologyManager)
					.findTermByAccession(x.getId());
			//if found and valid
			if (ot != null && getValidTerms().contains(ot.getPreferredName().toLowerCase())) {
				inferred.add(ot.getPreferredName());
			} 
			else {
				logger.warn("Could not find a term by the xref.id: " + x.getId());
			}
		}
		
		return inferred;
	}       
	
}
