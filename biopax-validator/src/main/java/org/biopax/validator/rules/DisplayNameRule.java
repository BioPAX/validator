package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * displayName length shouldn't exceed the specified limit.
 * @author rodche
 */
@Component
public class DisplayNameRule extends AbstractRule<Named> {
    public static final int MAX_DISPLAYNAME_LEN = 25;

	public boolean canCheck(Object thing) {
		return (thing instanceof Named); 
	}
    
    public void check(final Validation validation, Named named) 
    {
    	boolean fixed = false;
    	
    	if (named.getDisplayName() == null) {
    		if(validation.isFix()) {
    			// use the standardName if present
				if (named.getStandardName() != null) {
					named.setDisplayName(named.getStandardName());
					fixed = true;
				} // otherwise, use the shortest name, if anything...
				else if (!named.getName().isEmpty()) {
					String dsp = named.getName().iterator().next();
					for (String name : named.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					named.setDisplayName(dsp);
					fixed = true;
				}
			}
    		// report
			error(validation, named, "no.display.name", fixed && validation.isFix());
		} 
    	
    	// check max. length
    	String name = named.getDisplayName();
    	if (name != null) { // if existed or was added above
        	Integer max = (named instanceof Provenance) ? 50 : MAX_DISPLAYNAME_LEN;
        	if (name.length() > max)
				error(validation, named, "too.long.display.name", false
					, name 
						+ ((fixed) ? "(auto-created form other names!)" : ""), name.length(), max);
		}
    }

}
