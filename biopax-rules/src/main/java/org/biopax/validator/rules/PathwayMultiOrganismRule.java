package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.impl.TraverserRunner;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.stereotype.Component;

/**
 * Pathway.organism - for multi-organism networks must specify 
 * organism on the component physical entities (within interactions) 
 * and pathways, instead of using this property. 
 * What to do if it's not the case? 
 * Three options: skip, delete this one, 
 * or rewrite nested organism properties with this value.
 * 
 * @author rodche
 */
@Component
public class PathwayMultiOrganismRule extends AbstractRule<Pathway>
{

	@Resource
	EditorMap editorMap3;
	
    public void check(final Pathway pathway)   {
    	
    	final Collection<BioPAXElement> organisms = new HashSet<BioPAXElement>();
    	final BioSource organism = pathway.getOrganism();
    	
    	TraverserRunner runner = new TraverserRunner(editorMap3) {
    		BioSource bioSrc = organism;
    		@Override
			protected void visitObjectValue(BioPAXElement value, Model model, PropertyEditor editor) {
				if(value instanceof BioSource) {
					if(bioSrc == null) {
						bioSrc = (BioSource) value; 
						organisms.add(value);
						return;
					}		
					if(!value.isEquivalent(bioSrc)) {
						organisms.add(value);
					}
				} else {
					traverse(value, model);
				}
			}
    	};
    	
   		runner.run(pathway, null);
   		
		if(organisms.size()>0) {
			error(pathway, "multi.organism.pathway", organism,
				BiopaxValidatorUtils.toString(organisms));
		}
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Pathway;
    }

    @Override
	protected void fix(Pathway t, Object... values) {
		// TODO Auto-generated method stub
	}
}
