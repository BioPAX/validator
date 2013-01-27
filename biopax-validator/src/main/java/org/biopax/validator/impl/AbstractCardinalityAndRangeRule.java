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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.ValidatorException;
import org.biopax.validator.api.beans.Validation;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * 
 * @author rodche
 *
 * @param <E> extends BioPAXElement
 */

@Configurable
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
	
	@Override
	public void check(Validation validation, E thing) {
		try {
			Class<? extends BioPAXElement> face 
				= ((BioPAXElement)thing).getModelInterface();
			PropertyEditor editor = 
				editorMap.getEditorForProperty(property, face);
			if(editor == null) {
				throw new ValidatorException(
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
					for (Object val : (Collection<?>)ret) {
						checkRange(validation, thing, val);
					}
					
				} else {
					checkRange(validation, thing, ret);
				}
			} else { // null
				if (minCardinality > 0) {
					String code = (minCardinality==maxCardinality) 
						? "cardinality.violated" 
						: "min.cardinality.violated" ;
					error(validation, thing, code, false, editor.getProperty(), minCardinality);
				}
			}

		} catch (IllegalAccessException e) {
			throw new ValidatorException(e, getProperty(), 
					domain.getSimpleName());
		} catch (InvocationTargetException e) {
			throw new ValidatorException(e, getProperty(), 
					domain.getSimpleName());
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
