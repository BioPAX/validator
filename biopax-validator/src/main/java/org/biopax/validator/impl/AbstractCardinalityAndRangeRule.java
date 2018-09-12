package org.biopax.validator.impl;

import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.ValidatorException;
import org.biopax.validator.api.beans.Validation;

/**
 * Base BioPAX Rule for checking cardinality and range restrictions.
 *
 * @author rodche
 *
 * @param <E> extends BioPAXElement
 */

public abstract class AbstractCardinalityAndRangeRule<E extends BioPAXElement> 
	extends AbstractRule<E> 
{
	protected EditorMap editorMap;
	private final String property;
	private final Class<E> domain;
	private int minCardinality = 0;
	private int maxCardinality = Integer.MAX_VALUE;
	Class<?>[] ranges; // although the editor has range property, this is for stricter checks
	String rangesAsString = "";
	
	// Constructor with arguments
	public AbstractCardinalityAndRangeRule(
			Class<E> domain, String property, 
			int min, int max, Class<?>... ranges) {
		this.domain = domain;
		this.property = property;
		this.minCardinality = (min >= 0) ? min : 0;
		this.maxCardinality = (max > 0) ? max : Integer.MAX_VALUE;
		this.ranges = ranges;
		for(Class<?> cl : ranges) {
			rangesAsString += cl.getSimpleName() + " ";
		}
	}
	
	
	public boolean canCheck(Object thing) {
		return domain.isInstance(thing);
	}
	
	public void check(Validation validation, E thing) {
		PropertyEditor editor = 
			editorMap.getEditorForProperty(property, thing.getModelInterface());
		if(editor == null) {
			throw new ValidatorException(
				"BUG in " + getClass().getSimpleName() +
				" rule: no editor exists for property '" + 
						property + "' of " + domain);
		}

		//get value(s) from the property of the biopax obj; copy to avoid CMEx...
		Set<?> ret = null;
		//sync to get the property values (there're other rules in separate threads that might check/fix the same thing and property)
		synchronized (thing) {
			ret = new HashSet<Object>(editor.getValueFromBean(thing));
		}
			
		int size = ret.size();
		if (maxCardinality == minCardinality) {
			// exact cardinality check
			if (maxCardinality  > 0 
					&& maxCardinality < Integer.MAX_VALUE 
					&& size != maxCardinality) 
			{
				error(validation, thing, 
						"cardinality.violated", false, editor.getProperty(), maxCardinality);
			}
		} else {
			// min. cardinality check
			if (minCardinality > 0 && size == 0) {
				error(validation, thing, 
						"min.cardinality.violated", false, editor.getProperty(), minCardinality);
			}
			// max. cardinality check
			if (maxCardinality > 0
					&& maxCardinality < Integer.MAX_VALUE
					&& size > maxCardinality) {
				error(validation, thing, 
						"max.cardinality.violated", false, editor.getProperty(), maxCardinality);
			}
		}
		// check range
		for (Object val : ret) {
			checkRange(validation, thing, val);
		}
	}
	
	private void checkRange(Validation validation, E thing, Object p) {
		boolean isViolated = true;
		for (Class<?> range : ranges) {
			if (range.isInstance(p)) {
				isViolated = false;
				break;
			}
		}
		if(isViolated) {
			error(validation, thing, "range.violated", 
					false, getProperty(), 
					p, p.getClass().getSimpleName(), rangesAsString);
		}
	}


	public EditorMap getEditorMap() {
		return editorMap;
	}


	public int getMinCardinality() {
		return minCardinality;
	}


	public void setMinCardinality(int minCardinality) {
		this.minCardinality = minCardinality;
	}


	public int getMaxCardinality() {
		return maxCardinality;
	}


	public void setMaxCardinality(int maxCardinality) {
		this.maxCardinality = maxCardinality;
	}


	public Class<?>[] getRanges() {
		return ranges;
	}


	public void setRanges(Class<?>[] ranges) {
		this.ranges = ranges;
	}


	public String getProperty() {
		return property;
	}


	public Class<E> getDomain() {
		return domain;
	}	
	
}
