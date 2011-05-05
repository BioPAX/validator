package org.biopax.validator.impl;

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.level2.Level2Element;

public abstract class Level2CardinalityAndRangeRule<E extends Level2Element> 
	extends BasicCardinalityAndRangeRule<E> {
	
	public Level2CardinalityAndRangeRule(Class<E> domain, String property,
			int min, int max, Class<?>... ranges) {
		super(domain, property, min, max, ranges);
		this.editorMap = SimpleEditorMap.L2;
	}
}
