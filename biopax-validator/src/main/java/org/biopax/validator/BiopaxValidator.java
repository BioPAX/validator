package org.biopax.validator;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.UtilityClass;
import org.biopax.validator.api.ValidatorException;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.Rule;
import org.biopax.validator.api.Validator;
import org.biopax.validator.api.beans.Behavior;
import org.biopax.validator.api.beans.ErrorType;
import org.biopax.validator.api.beans.Validation;
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
public class BiopaxValidator implements Validator {
	private static final Logger log = LoggerFactory.getLogger(BiopaxValidator.class);

	@Autowired
	private Set<Rule<?>> rules;

	private final Set<Validation> results;

	@Autowired
	private ValidatorUtils utils;


	public BiopaxValidator() {
		results = Collections.newSetFromMap(new ConcurrentHashMap<>());
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


	public void validate(final Validation validation) {
		assert(validation != null);

		if (validation == null || validation.getModel() == null) {
			throw new ValidatorException(
				"Failed: no BioPAX model to validate " +
					"(have you successfully imported or created one already?)");
		}

		// register the validation (if not done already)
		if (!getResults().contains(validation)) {
			getResults().add(validation);
		}


		// break if max.errors exceeded (- reported by AOP interceptors, while parsing a file, or - in previous runs)
		if (validation.isMaxErrorsSet()
			&& validation.getNotFixedErrors() > validation.getMaxErrors()) {
			log.info("Errors limit (" + validation.getMaxErrors() + ") is exceeded; exitting...");
			return;
		}

		Model model = (Model) validation.getModel();

		log.debug("validating model: " + model + " that has "
			+ model.getObjects().size() + " objects");

		if(model.getLevel() != BioPAXLevel.L3) {
			model = (new LevelUpgrader()).filter(model);
			validation.setModel(model);
			log.info("Upgraded to BioPAX Level3 model: " + validation.getDescription());
		}

		assert(model != null && model.getLevel() == BioPAXLevel.L3);

		// Check/fix Rule<? extends BioPAXElement> rules concurrently (low risk of getting CMEx), 
		// because they normally do minor changes and simply cannot add/remove
		// elements in the Model (though, can alter a property of any biopax object)
		ExecutorService exec = Executors.newFixedThreadPool(100);

		// First, check/fix individual objects
		// (no need to copy; these rules cannot add/remove objects in model)
		for (BioPAXElement el : model.getObjects())
		{
			// rules can check/fix specific elements
//			for (Rule rule : rules) {				
//				Behavior behavior = utils.getRuleBehavior(rule.getClass().getName(), validation.getProfile());    	
//		        if (behavior == Behavior.IGNORE) 
//		        	continue; // skip					
//				execute(exec, rule, validation, (Object) el);
//			}
			//sequentially apply all (capable,enabled) rules to the object in a separate thread
			execute(exec, rules, validation, (Object) el);
		}
		exec.shutdown(); //end accepting new jobs
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new ValidatorException("Interrupted unexpectedly!");
		}

		//Second, apply all Rule<Model> rules -
		//run Rule<Model> rules concurrently
		exec = Executors.newFixedThreadPool(50);
		for (Rule rule : rules)
		{
			Behavior behavior = utils.getRuleBehavior(rule.getClass().getName(),
        validation.getProfile());
			if (behavior != Behavior.IGNORE)
				execute(exec, rule, validation, model);
		}
		exec.shutdown(); //end accepting jobs
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new ValidatorException("Interrupted unexpectedly!");
		}

		log.debug("All rules checked!");

		if (validation.isFix()) {
			// discover, explicitly add child elements to the model
			model.repair();
			// remove all dangling utility class objects
			ModelUtils.removeObjectsIfDangling(model, UtilityClass.class);
		}

		// add comments and some statistics
		validation.addComment("number of interactions : "
			+ model.getObjects(Interaction.class).size());
		validation.addComment("number of physical entities : "
			+ model.getObjects(PhysicalEntity.class).size());
		validation.addComment("number of genes : "
			+ model.getObjects(Gene.class).size());
		validation.addComment("number of pathways : "
			+ model.getObjects(Pathway.class).size());

		//update all error counts (total, fixed, notfixed)
		for(ErrorType errorType : validation.getError()) {
			errorType.setTotalCases(errorType.countErrors(null, null, false));
			errorType.setNotFixedCases(errorType.countErrors(null, null, true));
		}
		validation.setNotFixedProblems(validation.countErrors(null, null, null, null, false, true));
		validation.setNotFixedErrors(validation.countErrors(null, null, null, null, true, true));
		validation.setTotalProblemsFound(validation.countErrors(null, null, null, null, false, false));
		validation.setSummary("different types of problem: " + validation.getError().size());
	}

	private void execute(ExecutorService exec, final Rule rule,
											 final Validation validation, final Object obj)
	{
		exec.execute(new Runnable() {
			@SuppressWarnings("unchecked") //obj can be either Model or a BPE
			public void run() {
				try {
					if (rule.canCheck(obj))
						rule.check(validation, obj);
				} catch (Throwable t) {
					//if we're here, there is probably a bug in the rule or validator!
					String id = validation.identify(obj);
					log.error(rule + ".check(" + id
						+ ") threw the exception: " + t.toString(), t);
					// anyway, report it almost normally (for a user to see this in the results too)
					validation.addError(utils.createError(id, "exception",
						rule.getClass().getName(), null, false, t));
				}
			}
		});
	}

	private void execute(ExecutorService exec, final Set<Rule<?>> rules,
											 final Validation validation, final Object obj)
	{
		//obj can be either Model or a BPE
		exec.execute(() -> {
			for(Rule rule : rules) {
				Behavior behavior = utils.getRuleBehavior(rule.getClass().getName(),
validation.getProfile());
				if (behavior == Behavior.IGNORE)
					continue; // skip disabled rule

				try {
					if (rule.canCheck(obj))
						rule.check(validation, obj);
				} catch (Throwable t) {
					//if we're here, there is probably a bug in the rule or validator!
					String id = validation.identify(obj);
					log.error(rule + ".check(" + id
						+ ") threw the exception: " + t.toString(), t);
					// anyway, report it almost normally (for a user to see this in the results too)
					validation.addError(utils.createError(id, "exception",
						rule.getClass().getName(), null, false, t));
				}
			}
		});
	}


	public void importModel(Validation validation, InputStream inputStream) {
		// add the parser
		SimpleIOHandler simpleReader = new SimpleIOHandler();
		simpleReader.mergeDuplicates(true);
		associate(inputStream, validation);
		associate(simpleReader, validation);
		/*
		 * build a model and associate it with the validation (for post-validation later on);
		 * during this, many errors/warnings may be caught and reported via AOP ;))
		 */
		Model model = simpleReader.convertFromOWL(inputStream);

		if(model == null)
			throw new ValidatorException("Failed importing a BioPAX model!");

		associate(model, validation);
	}

	public void associate(Object obj, Validation validation) {
		assert(validation != null);

		if (!getResults().contains(validation)) {
			if(validation != null)
				getResults().add(validation); // registered a new one
			else
				log.warn("Object " + obj +
					" is being associated with NULL (Validation)!");
		}

		if(obj instanceof Model) {
			validation.setModel(obj);
		} else if(obj != null) {
			validation.getObjects().add(obj);
		}
	}


	public Collection<Validation> findValidation(Object o)
	{
		// add forcedly associated keys
		Collection<Validation> keys = new HashSet<Validation>();

		if(o == null
			|| o.getClass().isPrimitive()
			|| o instanceof String) {
			return keys;
		}

		for(Validation r: results) {
			if (r.getObjects().contains(o)
				|| o.equals(r.getModel()))
			{
				keys.add(r);
			}
			else if (o instanceof BioPAXElement)
			{
				// associate using member models
				BioPAXElement bpe = (BioPAXElement) o;
				Model m = (Model) r.getModel();
				if (m != null && m.contains(bpe)) {
					keys.add(r);
				}
			}
		}

		if (keys.isEmpty())
			log.debug("findKey: no result keys found "
				+ "for the object : " + o);

		return keys;
	}

	public void indirectlyAssociate(Object parent, Object child) {
		if (parent == null || child==null
			|| child.getClass().isPrimitive()
			|| child instanceof String) {
			return;
		}

		for (Validation key : findValidation(parent)) {
			associate(child, key);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Saves or updates an error/warning case, found by an external tool, in all
	 * {@link Validation} tasks known to this validator instance. 
	 * This method is used by, e.g., AOP interceptors, which sense, catch, and
	 * register problems that occur in external modules, such as Paxtools,
	 * during the data read and/or modify.
	 *
	 */
	public void report(Object obj, String errorCode, String reportedBy, boolean isFixed, Object... args)
	{
		if(obj == null) {
			log.error("Attempted to registed an error " +
				errorCode + " by " + reportedBy + " with NULL object");
			return;
		}


		Collection<Validation> validations = findValidation(obj);
		if(validations.isEmpty()) {
			// the object is not associated neither with parser nor model
			log.warn("No validations are associated with the object: "
				+ obj + "; user won't receive this message: " + errorCode
				+ "(fixed=" + isFixed + ") by " + reportedBy + "; " + args );
		}

		// add to the corresponding validation result
		for(Validation v: validations) {
			ErrorType err = utils.createError(v.identify(obj),
				errorCode, reportedBy, v.getProfile(), isFixed, args);

			// add or update: if there was the same type error,
			// this will update/add to existing error cases (unique type-object-reporter combinations) 
			v.addError(err);

			log.debug(v.getDescription() + " - added/updated " + err + " for " + obj + " as: " + isFixed);
		}

	}
}
