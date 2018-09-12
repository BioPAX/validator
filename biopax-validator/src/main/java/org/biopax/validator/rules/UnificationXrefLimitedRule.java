package org.biopax.validator.rules;

/*
 *
 */

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.RnaReference;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UnificationXref applicability rule 
 * (BioPAX class - allowed/denied unification xref db names)
 *
 * @author rodche
 */
@Component
public class UnificationXrefLimitedRule extends AbstractRule<UnificationXref> {

	private static final Map<Class<? extends BioPAXElement>, Set<String>> allow = 
			new ConcurrentHashMap<Class<? extends BioPAXElement>, Set<String>>();
    private static final Map<Class<? extends BioPAXElement>, Set<String>> deny = 
    		new ConcurrentHashMap<Class<? extends BioPAXElement>, Set<String>>();
	
    static {
    	// allow ONLY listed db names (synonyms will be considered too) for member UnificationXrefs of:
    	allow.put(BioSource.class, new HashSet<String>(Arrays.asList("taxonomy")));
    	allow.put(Provenance.class, new HashSet<String>(Arrays.asList("miriam")));
    	allow.put(CellVocabulary.class, new HashSet<String>(Arrays.asList("cl")));
    	allow.put(TissueVocabulary.class, new HashSet<String>(Arrays.asList("bto")));
    	allow.put(CellularLocationVocabulary.class, new HashSet<String>(Arrays.asList("go")));
    	allow.put(EvidenceCodeVocabulary.class, new HashSet<String>(Arrays.asList("mi")));
    	allow.put(ExperimentalFormVocabulary.class, new HashSet<String>(Arrays.asList("mi")));
    	allow.put(InteractionVocabulary.class, new HashSet<String>(Arrays.asList("mi")));
    	allow.put(SequenceModificationVocabulary.class, new HashSet<String>(Arrays.asList("so","mod")));
    	allow.put(PhenotypeVocabulary.class, new HashSet<String>(Arrays.asList("pato")));
    	allow.put(RelationshipTypeVocabulary.class, new HashSet<String>(Arrays.asList("mi")));
    	allow.put(SequenceRegionVocabulary.class, new HashSet<String>(Arrays.asList("so")));
    	
    	// not recommended xref.db names (and all synonyms) for UnificationXrefs of
    	deny.put(Dna.class, new HashSet<String>(Arrays.asList("uniprot","pubmed")));
    	deny.put(Rna.class, new HashSet<String>(Arrays.asList("uniprot")));
    	deny.put(DnaReference.class, new HashSet<String>(Arrays.asList("uniprot","pubmed")));
    	deny.put(RnaReference.class, new HashSet<String>(Arrays.asList("uniprot")));
    	deny.put(SmallMoleculeReference.class, new HashSet<String>(Arrays.asList("uniprot")));
    	deny.put(SmallMolecule.class, new HashSet<String>(Arrays.asList("uniprot")));
    	deny.put(PhysicalEntity.class, new HashSet<String>(Arrays.asList("go")));
    	deny.put(ProteinReference.class, new HashSet<String>(Arrays.asList("OMIM","Entrez Gene")));
    	deny.put(Interaction.class, new HashSet<String>(Arrays.asList("mi")));
    }
    
    private XrefHelper helper;
    
    private boolean ready = false;
    
    // to init on the first check(..) call
    void initInternalMaps() {
    	if(!ready) {
    		for (Class<? extends BioPAXElement> clazz : allow.keySet()) {
    			final Set<String> a = allow.get(clazz);
    			for(String db : new HashSet<String>(a)) {
    				Collection<String> synonymsOfDb = helper.getSynonymsForDbName(db);
    				a.addAll(synonymsOfDb);
    			}
    		}
    		
    		for (Class<? extends BioPAXElement> clazz : deny.keySet()) {
    			final Set<String> a = deny.get(clazz);
    			for(String db : new HashSet<String>(a)) {
    				Collection<String> synonymsOfDb = helper.getSynonymsForDbName(db);
    				a.addAll(synonymsOfDb);
    			}
    		}
    		
    		ready = true;
    	}
    }
    
    
    /**
     * Constructor requires the two sets to be defined in 
     * the Spring application context.
     * 
     * @param xrefHelper
     */
    @Autowired
    public UnificationXrefLimitedRule (XrefHelper xrefHelper) {
    	helper = xrefHelper;
    }
    
    
	public boolean canCheck(Object thing) {
		return thing instanceof UnificationXref;
	}
    
	public void check(final Validation validation, UnificationXref x) {
		if(!ready)
			initInternalMaps();
		
		if (x.getDb() == null 
			|| helper.getPrimaryDbName(x.getDb())==null) 
		{
			// ignore for unknown databases (another rule checks)
			return;
		}

		// fix case sensitivity
		final String xdb = helper.dbName(x.getDb());
		
		// check constrains for each element containing this unification xref 
		for (XReferrable bpe : x.getXrefOf()) {
			for (Class<? extends BioPAXElement> c : allow.keySet()) {
				if (c.isInstance(bpe)) {
					if (!allow.get(c).contains(xdb)) {
						error(validation, x, "not.allowed.xref", false, x.getDb(), 
							bpe, c.getSimpleName(), allow.get(c).toString());
					}
				}
			}
			for (Class<? extends BioPAXElement> c : deny.keySet()) {
				if (c.isInstance(bpe)) {
					if (deny.get(c).contains(xdb)) {
						error(validation, x, "denied.xref", false, x.getDb(), 
							bpe, c.getSimpleName(), deny.get(c).toString());
					}
				}
			}
		}
	}

}
