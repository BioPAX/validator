package org.biopax.validator.rules;

import java.util.*;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorException;
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
public final class NextStepShareParticipantsRule extends AbstractRule<PathwayStep>  {

	public boolean canCheck(Object thing) {
		return thing instanceof PathwayStep;
	}

	public void check(PathwayStep step) {
		// get all the participants
		Collection<Entity> thisStepParticipants = getParticipants(step);
		
		// now find participants intersection with each previous step:
		for (PathwayStep prevStep : step.getNextStepOf()) {	
			Collection<Entity> participants = getParticipants(prevStep);
			// first set becomes the intersection of the two:
			participants.retainAll(thisStepParticipants);
			if (participants.isEmpty()) {
				error(step, "empty.participants.intersection", prevStep);
			}
		}
	}

	Collection<Entity> getParticipants(PathwayStep step) {
		// this is a protection from an infinite loop 
		//(which normally should never occur; another rule will check that.)
		Collection<Process> processes = new HashSet<Process>();
		Set<Entity> ret = new HashSet<Entity>();
		for(Process p : step.getStepProcess()) {
			ret.addAll( getParticipants(p, processes) );
		}
		return ret;
	}

	Collection<Entity> getParticipants(Process process, Collection<Process> visited) {
		
		if(visited.contains(process)) {
			throw new BiopaxValidatorException("Pathway Step Processes Form an Infinite Loop!");
		}
		visited.add(process);
		
		if(process instanceof Interaction) {
			return ((Interaction) process).getParticipant();
			// some of participants can be also Processes, but deeper recursion isn't necessary here...
		}
		
		Collection<Entity> ret = new HashSet<Entity>();
		for(Process p : ((Pathway) process).getPathwayComponent()) {
			ret.addAll( getParticipants(p, visited) );
		}
		return ret;
	}
	
	
	public void fix(PathwayStep t, Object... values) {
		// is very difficult ;)
	}

}