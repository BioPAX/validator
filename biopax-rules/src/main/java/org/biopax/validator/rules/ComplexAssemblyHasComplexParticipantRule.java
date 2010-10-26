package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * This rule checks if ComplexAssembly has at least one complex on one side.
 * 
 */
@Component
public class ComplexAssemblyHasComplexParticipantRule extends AbstractRule<ComplexAssembly>
{

    public void check(ComplexAssembly complexAssembly, boolean fix)
    {
		Set<Complex> complexes = 
			new ClassFilterSet<Complex>(complexAssembly.getParticipant(), Complex.class);
		
		if(complexes.isEmpty()) {
			error(complexAssembly, "complex.not.present");
		}
	
    }

    public boolean canCheck(Object thing) {
        return thing instanceof ComplexAssembly;
    }

}
