package org.biopax.validator.rules;

import java.util.Set;

import javax.annotation.Resource;

import org.biopax.validator.result.Validation;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Level3Element;
import org.springframework.stereotype.Component;

/**
 * This class warns on empty or weird property values.
 *
 * @author rodche
 *
 */
@Component
public class DataPropertyIllegalValueRule extends AbstractRule<BioPAXElement> {
	/*
	 * @Autowired, even with @Qualifier, won't work here, 
	 * because we need to inject a particular bean (<util:set>) 
	 * of type Set<String>, rather than a set of beans of String type :)
	 */
	@Resource
	private Set<String> warnOnDataPropertyValues;
    
	public void setWarnOnDataPropertyValues(Set<String> warnOnDataPropertyValues) 
	{
		this.warnOnDataPropertyValues = warnOnDataPropertyValues;
	}
	
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	@Override
	public void check(final Validation validation, BioPAXElement bpe) {
		EditorMap editorMap = (bpe instanceof Level3Element)
			? SimpleEditorMap.get(BioPAXLevel.L3)
				: SimpleEditorMap.get(BioPAXLevel.L2);
		
		AbstractTraverser checker = new AbstractTraverser(editorMap) {
			@Override
			protected void visit(Object value, BioPAXElement parent,
					Model model, PropertyEditor editor) {
				if (value != null && !(value instanceof BioPAXElement)) {
					if (warnOnDataPropertyValues.contains(value.toString().trim().toUpperCase())) {
						error(validation, parent, "illegal.property.value", validation.isFix(), editor.getProperty(), value);
						if(validation.isFix()) {
							if(editor.isMultipleCardinality())
								editor.removeValueFromBean(value, parent);
							else
								editor.setValueToBean(null, parent);
						}
					}
				}
			}
		};
		
		checker.traverse(bpe, null);
	}
	
}
