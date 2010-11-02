package org.biopax.validator.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
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
		if (validation == null) {
			throw new BiopaxValidatorException("Failed! Did you import or add a model?");
		}
		
		// usually, there is one model, but if they are many...
		for (Model model : findModel(validation)) {
			
				if (log.isDebugEnabled()) {
					log.debug("validating model: " + model + " that has "
							+ model.getObjects().size() + " objects");
				}
				
				// if normalize==true, convert to the L3 first
				if (model.getLevel() != BioPAXLevel.L3
						&& validation.isNormalize()) {
					if (log.isInfoEnabled())
						log.info("Converting model to BioPAX Level3...");
					model = (new OneTwoThree()).filter(model);
				}
				
				for (Rule rule : rules) {
					// rules can check the model or specific elements
					if(log.isDebugEnabled())
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
							+ model.getObjects(physicalEntityParticipant.class)
									.size());
					validation.addComment("number of pathways : "
							+ model.getObjects(pathway.class).size());
				}
				
				/* TODO think how to better return the fixed OWL when the validation has multiple models...
				 * For now, it will simply append - 
				 * <?xml?><rdf:RDF>..</rdf:RDF>
				 * <?xml?><rdf:RDF>..</rdf:RDF> 
				 * <?xml?> etc...
				*/ 
				if(validation.isFix() || validation.isNormalize()) {
					try {
						String owlModel = null;
						// normalize?
						if(validation.isNormalize()) {
							Normalizer normalizer = new Normalizer(validation);
							owlModel = normalizer.normalize(model);
						} else {
							// export the model to OWL
							SimpleExporter exporter = new SimpleExporter(model.getLevel());
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							exporter.convertToOWL(model, outputStream);
							owlModel = outputStream.toString("UTF-8");
						}
						// append to previous owl data (of other models, if any, for the same validation obj.)
						String owl = validation.getFixedOwl();
						if(owl == null) 
							owl = "";
						owl +=  owlModel + System.getProperty("line.separator");
						validation.setFixedOwl(owl);
					} catch (IOException e) {
						throw new BiopaxValidatorException(
							"Failed to export modified model!", e);
					}
				}
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
		// build the model and associate it with the key (for the post-validation, later in the 'validate' method):
		Model model = simpleReader.convertFromOWL(inputStream); // during this here, many errors/warnings may be reported via AOP ;)
		associate(model, validation);
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
			if (r.getObjects().contains(o)) {
				keys.add(r);
			} else if (o instanceof BioPAXElement) {
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
			
	
	public Collection<Model> findModel(Validation key) {
		Collection<Model> mms = new HashSet<Model>();
		mms.addAll(key.getObjects(Model.class));
		return mms;
	}

    
    public void report(Object obj, ErrorType errorWithSingleCase, boolean setFixed) 
    {	
		if(errorWithSingleCase.getErrorCase().isEmpty()) {
			log.error("Attempted to registed an error " +
				"without any error cases in it: " + errorWithSingleCase);
			return;
		}
    	
    	Collection<Validation> validations = findValidation(obj);			
		if(validations.isEmpty()) {
			// the object is not associated neither with parser nor model
			if(log.isInfoEnabled())
				log.info("No active validations exist for the object " 
				+ obj + "; so end user won't receive this message: " 
				+ errorWithSingleCase);
		}
		
		// add to the corresponding validation result
		for(Validation v: validations) { 
			if(log.isTraceEnabled()) {
				log.trace("Reporting: " + errorWithSingleCase.toString() 
						+ " "+ errorWithSingleCase.getErrorCase().toArray()[0] + 
						" in: " + v.getDescription() + 
						"; fixed=" + setFixed);
			}
			
			// to be consistent with the setFixed parameter!
			for(ErrorCaseType ect : errorWithSingleCase.getErrorCase()) {
				ect.setFixed(setFixed && v.isFix()); 
			}
			// add or update the error case(s)
			v.addError(errorWithSingleCase);
		}
	}
}
