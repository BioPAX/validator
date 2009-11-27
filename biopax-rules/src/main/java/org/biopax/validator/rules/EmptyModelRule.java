package org.biopax.validator.rules;

import org.biopax.paxtools.model.Model;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks whether the model contains any objects.
 */
@Component
public class EmptyModelRule extends AbstractRule<Model> {
	
	@Override
	protected void fix(Model t, Object... values) {
		// TODO Auto-generated method stub
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model;
	}

	public void check(Model model) {
		if(model.getObjects().isEmpty())
			error(model, "empty.biopax.model", model.getLevel().toString());
	}

}
