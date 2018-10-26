package org.biopax.validator;


import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.api.Identifier;

/**
 * BioPAX object domain specific implementation 
 * of the {@link Identifier} strategy.
 * 
 * @author rodche
 *
 */
public final class BiopaxIdentifier implements Identifier {

	/* (non-Javadoc)
	 * @see org.biopax.validator.api.Identifier#getId(java.lang.Object)
	 */
	public String identify(Object obj) {
    	String id = "";
    	
		if(obj instanceof SimpleIOHandler) {
			SimpleIOHandler r = (SimpleIOHandler) obj;
			id = r.getClass().getSimpleName(); 
	    	try {
	    		id = r.getId(); //current element URI
	    	} catch (Throwable e) {
	    		id = r.getXmlStreamInfo(); //location
			}
		} else if (obj instanceof BioPAXElement 
				&& ((BioPAXElement)obj).getUri() != null) {
			id = ((BioPAXElement) obj).getUri().replaceFirst("^.+#", "");
			// - strictly spk., does not always get the local part (depends on xml:base) but is OK.
		} else if(obj instanceof Model) {
			id = obj + "; xml:base=" + ((Model) obj).getXmlBase();
		} else {
			id = "" + obj;
		}
		
		return id;
	}
}
