package org.biopax.validator.impl;


import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.model.level3.Level3Element;
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
public abstract class Level3CardinalityAndRangeRule<E extends Level3Element> 
	extends BasicCardinalityAndRangeRule<E>
{
	// Constructor with arguments
	public Level3CardinalityAndRangeRule(Class<E> domain, String property,
			int min, int max, Class<?>... ranges) {
		super(domain, property, min, max, ranges);
	}
	
	
	@Autowired
	public void setEditorMap(@Qualifier("editorMap3") EditorMap editorMap) {
		this.editorMap = editorMap;
	}
	
}
