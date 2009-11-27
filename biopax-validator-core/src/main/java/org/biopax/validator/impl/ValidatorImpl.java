package org.biopax.validator.impl;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.Rule;
import org.biopax.validator.result.Validation;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * BioPAX Validator
 * Checks All the Rules and BioPAX Elements
 * 
 * This is also a registry that keeps validation results:
 * (Validation refers to the BioPAX model and reader, and collects all the errors);
 * 
 * @author rodche
 */
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

    
	@SuppressWarnings("unchecked")
	public void validate(Validation validation) {
		if (validation == null) {
			throw new BiopaxValidatorException("Failed! Did import or add the model?");
		}
		
		for (Model model : getModel(validation)) {
			if (model != null) {
				if (log.isDebugEnabled()) {
					log.debug("validating model: " + model + " that has "
							+ model.getObjects().size() + " objects");
				}
				
				for (Rule rule : rules) {
					// rules can check the model or specific elements
					if (rule.canCheck(model)) {
						rule.check(model);
					} else {
						for (BioPAXElement el : model.getObjects()) {
							if (rule.canCheck(el)) {
								rule.check(el);
							}
						}
					}
				}
				
			} else {
				log.warn("Model is null (" + validation + ")");
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
		if (contains(validation)) {
			log.warn("Key '" + validation + "' has been used already (will merge with those results, if any)!");
			// may cause a ConcurrentModificationException (which is usually ignored by the rest of the app.)
			//free(key);
		} else {
			// register a new validation result:
			results.add(validation);
		}
		
		// add the parser
		SimpleReader simpleReader = new SimpleReader();
		associate(inputStream, validation);
		associate(simpleReader, validation);
	
		// build the model and associate it with the key (for the post-validation, later in the 'validate' method):
		Model model = simpleReader.convertFromOWL(inputStream); // during this here, many errors/warnings may be reported via AOP ;)
		addModel(validation, model);
	}
	
	
	public void addModel(Validation validation, Model model) {
		associate(model, validation);
	}
	
	
	public void associate(Object element, Validation validation) {
		validation.getObjects().add(element);
	}


	public Collection<Validation> findKey(Object o) {
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
			if (log.isWarnEnabled()) {
				log.warn("findKey: no result keys found "
						+ "for the object : " + o);
			}
		}
		
		return keys;
	}

	public void free(Object o, Validation key) {
		key.getObjects().remove(o);
	}


	public boolean contains(Validation key) {
		return results.contains(key);
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

		for (Validation key : findKey(parent)) {
			associate(child, key);
		}
	}
			
	
	public Collection<Model> getModel(Validation key) {
		Collection<Model> mms = new HashSet<Model>();
		mms.addAll(key.getObjects(Model.class));
		return mms;
	}

	
	public void free(Validation key) {
		results.remove(key);
	}
}
