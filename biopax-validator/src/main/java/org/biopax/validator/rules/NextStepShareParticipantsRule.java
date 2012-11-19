package org.biopax.validator.rules;

import java.util.*;

import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * PathwayStep (in Pathway.pathwayOrder), if listed in the 
 * nextStep property of another PathwayStep, must have 
 * not empty intersection of participants of their 
 * stepProcess-es (Process is an interaction or pathway) 
 *
 * @author rodche
 *
 */
@Component
public class NextStepShareParticipantsRule extends AbstractRule<PathwayStep>  
{
	public boolean canCheck(Object thing) {
		return thing instanceof PathwayStep
			&& !((PathwayStep) thing).getNextStepOf().isEmpty();
	}

	public void check(final Validation validation, PathwayStep step) {
		if(step.getNextStepOf().isEmpty())
			return;
		
		// get all the participants
		Collection<Entity> thisStepParticipants = getParticipants(step);
		
		// now find participants intersection with each previous step:
		for (PathwayStep prevStep : step.getNextStepOf()) {	
			Collection<Entity> participants = getParticipants(prevStep);
			// first set becomes the intersection of the two:
			participants.retainAll(thisStepParticipants);
			if (participants.isEmpty()) {
				error(validation, step, "empty.participants.intersection", false, prevStep);
			}
		}
	}

	Collection<Entity> getParticipants(PathwayStep step) {
		// this is a protection from an infinite loop 
		//(which normally should never occur; another rule will check that.)
		Collection<Process> processes = new HashSet<Process>();
		Set<Entity> ret = new HashSet<Entity>();
		if(step instanceof BiochemicalPathwayStep) {
			Conversion c = ((BiochemicalPathwayStep) step).getStepConversion();
			ret.addAll( getParticipants(c, processes) );
		}
		for(Process p : step.getStepProcess()) {
			ret.addAll( getParticipants(p, processes) );
		}
		return ret;
	}

	Collection<Entity> getParticipants(Process process, Collection<Process> visited) 
	{
		Collection<Entity> ret = new HashSet<Entity>();
		
		// escape infinite loop
		if(visited.contains(process)) {
			//"Step Processes Form a Loop!"
			return ret; // empty
		} 
		visited.add(process);
		
		if(process instanceof Interaction) {
			for(Entity pat : ((Interaction) process).getParticipant()) 
			{
				if(pat instanceof PhysicalEntity || pat instanceof Gene) 
					ret.add(pat);
			}
		} else { // a pathway
			for (Process p : ((Pathway) process).getPathwayComponent()) 
			{
				ret.addAll(getParticipants(p, visited));
			}
		}
		
		return ret;
	}
	
	
	private void fix(PathwayStep t, Object... values) {
		// is very difficult ;)
	}

}