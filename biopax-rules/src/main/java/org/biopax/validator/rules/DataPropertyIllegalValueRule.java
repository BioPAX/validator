package org.biopax.validator.rules;

import java.util.Set;

import javax.annotation.Resource;

import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.springframework.stereotype.Component;

/**
 * This class warns on empty or weird property values.
 *
 * @author rodche
 *
 */
@Component
public class DataPropertyIllegalValueRule extends AbstractRule<Model> {
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
		return thing instanceof Model 
			&& !((Model)thing).getObjects().isEmpty();
	}

	public void check(Model model, final boolean fix) {
		EditorMap editorMap = 
			(model.getLevel() == BioPAXLevel.L3)
				? BiopaxValidatorUtils.EDITOR_MAP_L3
					: BiopaxValidatorUtils.EDITOR_MAP_L2;
		
		AbstractTraverser checker = new AbstractTraverser(editorMap) {
			@Override
			protected void visit(Object value, BioPAXElement parent,
					Model model, PropertyEditor editor) {
				if (value != null && !(value instanceof BioPAXElement)) {
					if (warnOnDataPropertyValues.contains(value.toString().trim().toUpperCase())) {
						error(parent, "illegal.property.value", fix, editor.getProperty(), value);
						if(fix) {
							if(editor.isMultipleCardinality())
								editor.removeValueFromBean(value, parent);
							else
								editor.setValueToBean(null, parent);
						}
					}
				}
			}
		};
		
		for(BioPAXElement e: model.getObjects()) {
			checker.traverse(e, model);
		}
	}
	
}
