package org.biopax.validator.rules;

import java.util.Set;

import javax.annotation.Resource;

import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.impl.TraverserRunner;
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
 * TODO re-write using EditorMap and no reflection directly.
 *
 */
@Component
public class DataPropertyIlliegalValueRule extends AbstractRule<Model> {
	
	@Resource
	private EditorMap editorMap3;
	
	@Resource 
	private EditorMap editorMap2;
	
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

	public void setEditorMap3(EditorMap editorMap3) {
		this.editorMap3 = editorMap3;
	}
	
	public void setEditorMap2(EditorMap editorMap2) {
		this.editorMap2 = editorMap2;
	}
	
	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& !((Model)thing).getObjects().isEmpty();
	}

	public void check(Model model) {
		EditorMap editorMap = 
			(model.getLevel() == BioPAXLevel.L3)
				? editorMap3 : editorMap2;
		
		TraverserRunner checker = new TraverserRunner(editorMap) {

			@Override
			protected void visitObjectValue(BioPAXElement value, Model model,
					PropertyEditor editor) {
				// nothing to do
			}
			
			@Override
			protected void visitDataValue(Object value, BioPAXElement parent,
					Model model, PropertyEditor editor) {
				super.visitDataValue(value, parent, model, editor);
				if (value != null) {
					if (warnOnDataPropertyValues.contains(value.toString().trim().toUpperCase())) {
						error(parent, "illegal.property.value", editor.getProperty(), value);
					}
				}
			}
			
		};
		
		checker.run(null, model);

	}
	
	@Override
	protected void fix(Model t, Object... values) {
		// TODO Auto-generated method stub
		
	}
}
