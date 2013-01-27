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

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.level3.Level3Element;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * 
 * @author rodche
 *
 * @param <E> extends BioPAXElement
 */

@Configurable
public abstract class CardinalityAndRangeRule<E extends Level3Element> 
	extends AbstractCardinalityAndRangeRule<E>
{
	// Constructor with arguments
	public CardinalityAndRangeRule(Class<E> domain, String property,
			int min, int max, Class<?>... ranges) {
		super(domain, property, min, max, ranges);
		this.editorMap = SimpleEditorMap.L3;
	}	
}
