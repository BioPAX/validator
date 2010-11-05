package org.biopax.validator.impl;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.Rule;
import org.biopax.validator.result.ErrorCaseType;
import org.biopax.validator.result.ErrorType;
import org.biopax.validator.result.Validation;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.utils.Normalizer;
import org.biopax.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;


/**
 * BioPAX Validator
 * Checks All the Rules and BioPAX Elements
 * 
 * This is also a registry that keeps validation results:
 * (Validation refers to the BioPAX model and reader, and collects all the errors);
 * 
 * @author rodche
 */
@Configurable
@Service
public class ValidatorImpl implements Validator {	
	private static final Log log = LogFactory.getLog(ValidatorImpl.class);
	
    @Autowired
	private Set<Rule<?>> rules;  
    
	private final Set<Validation> results;
    
	
    public ValidatorImpl() {
		results = new HashSet<Validation>();
	}
    
    
    public void setRules(Set<Rule<?>> rules) {
		this.rules = rules;
	}

    
    public Set<Rule<?>> getRules() {
		return rules;
	}

    
    public Collection<Validation> getResults() {
    	return results;
    }
    
    
	@SuppressWarnings("unchecked")
	public void validate(Validation validation) {
		if (validation == null || validation.getModel() == null) {
			throw new BiopaxValidatorException(
					"Failed! Did you import a model?");
		}

		Model model = validation.getModel();

		if (log.isDebugEnabled()) {
			log.debug("validating model: " + model + " that has "
					+ model.getObjects().size() + " objects");
		}

		// if normalize==true, convert to the L3 first
		if (model.getLevel() != BioPAXLevel.L3 && validation.isNormalize()) {
			if (log.isInfoEnabled())
				log.info("Converting model to BioPAX Level3...");
			model = (new OneTwoThree()).filter(model);
		}

		for (Rule rule : rules) {
			// rules can check the model or specific elements
			if (log.isDebugEnabled())
				log.debug("Current rule is: " + rule.getName());
			if (rule.canCheck(model)) {
				rule.check(model, false);
			} else {
				for (BioPAXElement el : model.getObjects()) {
					if (rule.canCheck(el)) {
						rule.check(el, validation.isFix());
					}
				}
			}
		}

		// normalize?
		if (validation.isNormalize()) {
			(new Normalizer(validation)).normalize(model);
		}

		// add comments and some statistics
		if (model.getLevel() == BioPAXLevel.L3) {
			validation.addComment("number of interactions : "
					+ model.getObjects(Interaction.class).size());
			validation.addComment("number of physical entities : "
					+ model.getObjects(PhysicalEntity.class).size());
			validation.addComment("number of genes : "
					+ model.getObjects(Gene.class).size());
			validation.addComment("number of pathways : "
					+ model.getObjects(Pathway.class).size());
		} else {
			validation.addComment("number of interactions : "
					+ model.getObjects(interaction.class).size());
			validation.addComment("number of participants : "
					+ model.getObjects(physicalEntityParticipant.class).size());
			validation.addComment("number of pathways : "
					+ model.getObjects(pathway.class).size());
		}

	}

	
	public Rule<?> findRuleByName(String name) {
		Rule<?> found = null;
		if (name != null || !"".equals(name)) {
			for (Rule<?> r : rules) {
				if (name.equals(r.getName())) {
					found = r;
					break;
				}
			}
		}
		return found;
	}


	public void importModel(Validation validation, InputStream inputStream) {
		// add the parser
		SimpleReader simpleReader = new SimpleReader();
		simpleReader.mergeDuplicates(true);
		associate(inputStream, validation);
		associate(simpleReader, validation);
		/* 
		 * build a model and associate it with the validation (for post-validation later on);
		 * during this, many errors/warnings may be caught and reported via AOP ;))
		 */
		Model model = simpleReader.convertFromOWL(inputStream); 
		validation.setModel(model);
	}
	
	
	public void associate(Object obj, Validation validation) {
		if (!getResults().contains(validation)) {
			getResults().add(validation); // registered a new validation result
		} else {
			if(log.isDebugEnabled())
				log.debug(obj + " object is associated with existing result");
		}
		validation.getObjects().add(obj);
		
		if(log.isDebugEnabled())
			log.debug("this validator : " + this);
	}


	public Collection<Validation> findValidation(Object o) {
		// add forcedly associated keys
		Collection<Validation> keys = new HashSet<Validation>();	
		
		if(o == null 
				|| o.getClass().isPrimitive()
				|| o instanceof String) {
			return keys;
		}
		
		for(Validation r: results) {
			if (r.getObjects().contains(o) || o.equals(r.getModel())) 
			{
				keys.add(r);
			} 
			else if (o instanceof BioPAXElement) 
			{
				// associate using member models
				BioPAXElement bpe = (BioPAXElement) o;
				for (Model m : r.getObjects(Model.class)) {
					if (m != null && m.contains(bpe)) {
						keys.add(r);
					}
				}
			}
		}

		if (keys.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("findKey: no result keys found "
						+ "for the object : " + o);
			}
		}
		
		return keys;
	}

	public void free(Object o, Validation key) {
		key.getObjects().remove(o);
	}

	
	public void freeObject(Object o) {
		for(Validation r : results) {
			if(r.getObjects().contains(o)) {
				r.getObjects().remove(o);
			}
		}
	}

	
	public void indirectlyAssociate(Object parent, Object child) {
		if (parent == null || child==null 
				|| child.getClass().isPrimitive()
				|| child instanceof String) {
			return; // do not (this is ok)
		}

		for (Validation key : findValidation(parent)) {
			associate(child, key);
		}
	}

    	
    public void report(Object obj, ErrorType err, boolean setFixed) 
    {	
		if(err.getErrorCase().isEmpty()) {
			log.error("Attempted to registed an error " +
				"without any error cases in it: " + err);
			return;
		}
    	
    	Collection<Validation> validations = findValidation(obj);			
		if(validations.isEmpty()) {
			// the object is not associated neither with parser nor model
			if(log.isInfoEnabled())
				log.info("No validations are associated with the object: " 
				+ obj + "; user won't receive this message: " 
				+ err);
		}
		
		// add to the corresponding validation result
		for(Validation v: validations) { 
			if(log.isTraceEnabled()) {
				log.trace("Reporting: " + err.toString() 
						+ " "+ err.getErrorCase().toArray()[0] + 
						" in: " + v.getDescription() + 
						"; fixed=" + setFixed);
			}
			
			// to be consistent with the setFixed parameter!
			for(ErrorCaseType ect : err.getErrorCase()) {
				ect.setFixed(setFixed && v.isFix()); 
			}

			// add or update the error case(s)
			v.addError(err);
		}
	}
}
