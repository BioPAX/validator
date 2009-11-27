package org.biopax.validator.impl;

import java.lang.reflect.Method;
import java.util.Collection;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * @author rodche
 *
 * @param <E> extends BioPAXElement
 */

@Configurable
public abstract class CardinalityAndRangeRule<E extends BioPAXElement> 
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
	public CardinalityAndRangeRule(
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
	
	
	@Autowired
	public void setEditorMap(@Qualifier("editorMap3") EditorMap editorMap) {
		this.editorMap = editorMap;
	}
	
	
	public boolean canCheck(Object thing) {
		return domain.isInstance(thing);
	}
	
	public void check(E thing) {
		try {
			Class<? extends BioPAXElement> face 
				= ((BioPAXElement)thing).getModelInterface();
			PropertyEditor editor = 
				editorMap.getEditorForProperty(property, face);
			if(editor == null) {
				throw new BiopaxValidatorException(
					"Flaw in the " + getClass().getSimpleName()
					+ " rule definition: no editor for the property: " 
					+ property + " of " + domain);
			}
			
			Method method = editor.getGetMethod();
			Object ret = method.invoke(thing, new Object[] {});
			if (ret != null) {
				if (editor.isMultipleCardinality()) {
					int size = ((Collection<?>) ret).size();
					if (maxCardinality == minCardinality) {
						// exact cardinality check
						if (maxCardinality  > 0 
							&& maxCardinality < Integer.MAX_VALUE 
							&& size != maxCardinality) 
						{
							error(thing, "cardinality.violated", 
									editor.getProperty(), maxCardinality);
						}
					} else {
						// min. cardinality check
						if (minCardinality > 0 && size == 0) {
							error(thing, "min.cardinality.violated", 
									editor.getProperty(), minCardinality);
						}
						// max. cardinality check
						if (maxCardinality > 0
								&& maxCardinality < Integer.MAX_VALUE
								&& size > maxCardinality) {
							error(thing, "max.cardinality.violated", 
									editor.getProperty(), maxCardinality);
						}
					}
					// check range
					for (Object val : (Collection<?>)ret) {
						checkRange(thing, val);
					}
					
				} else {
					checkRange(thing, ret);
				}
			} else { // null
				if (minCardinality > 0) {
					String code = (minCardinality==maxCardinality) 
						? "cardinality.violated" 
						: "min.cardinality.violated" ;
					error(thing, code, editor.getProperty(), minCardinality);
				}
			}

		} catch (Exception e) {
			throw new BiopaxValidatorException(e, getProperty(), 
					domain.getSimpleName());
		}
	}

	
	private void checkRange(E thing, Object p) {
		boolean isViolated = true;
		for (Class<?> range : ranges) {
			if (range.isInstance(p)) {
				isViolated = false;
				break;
			}
		}
		if(isViolated) {
			error(thing, "range.violated", getProperty(), 
					p, p.getClass().getSimpleName(), 
					rangesAsString);
		}
	}

	@Override
	protected void fix(E t, Object... values) {
		// TODO Auto-generated method stub
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
