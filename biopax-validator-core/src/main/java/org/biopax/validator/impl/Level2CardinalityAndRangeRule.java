package org.biopax.validator.impl;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.model.level2.Level2Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class Level2CardinalityAndRangeRule<E extends Level2Element> 
	extends BasicCardinalityAndRangeRule<E> {
	
	public Level2CardinalityAndRangeRule(Class<E> domain, String property,
			int min, int max, Class<?>... ranges) {
		super(domain, property, min, max, ranges);
	}

	@Autowired
	public void setEditorMap(@Qualifier("editorMap2") EditorMap editorMap) {
		this.editorMap = editorMap;
	}

}
