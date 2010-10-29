package org.biopax.validator.rules;

import java.util.Map;

import javax.annotation.Resource;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * displayName length shouldn't exceed the specified limit.
 * @author rodche
 */
@Component
public class DisplayNameRule extends AbstractRule<Named> {
    public static final int MAX_DISPLAYNAME_LEN = 25;
    @Resource
    Map<Class<? extends BioPAXElement>, Integer> maxDisplayNameLengths;

	public boolean canCheck(Object thing) {
		return (thing instanceof Named); 
	}
    
    public void check(Named named, boolean fix) {
    	String name = named.getDisplayName();
        if (name != null) {
        	Class<? extends BioPAXElement> cl = ((BioPAXElement)named).getModelInterface();
        	Integer max = (maxDisplayNameLengths.containsKey(cl)) 
        		? maxDisplayNameLengths.get(cl)	: MAX_DISPLAYNAME_LEN;
        	if (name.length() > max)
				error(named, "too.long.name", false, name, name.length(), max);
        }
    }

}
