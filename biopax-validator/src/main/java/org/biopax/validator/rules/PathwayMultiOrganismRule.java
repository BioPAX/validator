package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.util.Filter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.validator.result.Validation;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.springframework.stereotype.Component;

/**
 * Warn if a pathway and its component have different (not null) 'organism' values.
 * What to do? (ignore, delete this value, or override nested organism properties with pathway's value)
 * 
 * @author rodche
 */
@Component
public class PathwayMultiOrganismRule extends AbstractRule<Pathway> 
{	
	private final static Filter<PropertyEditor> filter = new Filter<PropertyEditor>() {
		@Override
		public boolean filter(PropertyEditor editor) {
			return !"nextStep".equals(editor.getProperty());
		}
	};
	
    public void check(final Validation validation, final Pathway pathway) {
    	final Collection<BioPAXElement> organisms = new HashSet<BioPAXElement>();
    	final BioSource organism = pathway.getOrganism(); // not null - due to the canCheck method!
    	//but..
    	if(organism==null) return; // we do not care
    	
    	AbstractTraverser runner = new AbstractTraverser(
    			SimpleEditorMap.L3, filter) 
    	{
    		@Override
			protected void visit(Object value, BioPAXElement parent, 
					Model model, PropertyEditor editor) 
    		{
				if(value instanceof BioSource) {	
					if(!((BioPAXElement) value).isEquivalent(organism)) {
						organisms.add((BioPAXElement) value);
					}
				} 
				else if (value instanceof BioPAXElement) {
					if (log.isTraceEnabled())
						log.trace("Traverse into " + value + " "
								+ value.getClass().getSimpleName());
					traverse((BioPAXElement) value, model);
				}
			}
    	};
    	
   		runner.traverse(pathway, null);
   		
		if(organisms.size()>0) {
			error(validation, pathway, "multi.organism.pathway",
				false, organism, utils.errorMsgArgument(organisms));
		}
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Pathway
        	&& ((Pathway)thing).getOrganism() != null;
    }

}
