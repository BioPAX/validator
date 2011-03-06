package org.biopax.validator.impl;

import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * 
 * @author rodche
 *
 * @param <E> extends BioPAXElement
 */

@Configurable
public abstract class Level3CardinalityAndRangeRule<E extends Level3Element> 
	extends BasicCardinalityAndRangeRule<E>
{
	// Constructor with arguments
	public Level3CardinalityAndRangeRule(Class<E> domain, String property,
			int min, int max, Class<?>... ranges) {
		super(domain, property, min, max, ranges);
		this.editorMap = BiopaxValidatorUtils.EDITOR_MAP_L3;
	}	
}
