package org.biopax.validator;

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.level3.Level3Element;

/**
 * A BioPAX property cardinality and range rule.
 *
 * @author rodche
 *
 * @param <E> extends BioPAXElement
 */
public abstract class CardinalityAndRangeRule<E extends Level3Element> extends AbstractCardinalityAndRangeRule<E>
{
	public CardinalityAndRangeRule(Class<E> domain, String property,
			int min, int max, Class<?>... ranges) {
		super(domain, property, min, max, ranges);
		this.editorMap = SimpleEditorMap.L3;
	}	
}
