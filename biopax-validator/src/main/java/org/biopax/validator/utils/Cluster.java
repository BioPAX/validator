package org.biopax.validator.utils;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.*;

public abstract class Cluster<T> {
	
	/**
	 * Implemented in concrete subclasses.
	 * Must be "symmetric"; may be "transitive" or not (you decide) - 
	 * @see Cluster#groupTransitive(Object[], int)
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public abstract boolean match(T a, T b);
	
	
	
	/**
	 * Puts elements into sets of "similar" ones. 
	 * Every object belongs to exactly one cluster.
	 * Clusters contain at least one object.
	 * 
	 * If concrete {@link #match(Object, Object)} method
	 * is not transitive, this produces such groups, where,
	 * match(A,B) is not necessarily true for all pairs in
	 * the same cluster, but there exists Z, such as,
	 * match(X,Z) and match(Y,Z) are true.
	 * 
	 * @see #match(Object, Object)
	 * 
	 * @param elements
	 * @param clusterMaxSize
	 * @return
	 */
	public final Set<Set<T>> cluster(Collection<T> elements, int clusterMaxSize) {
		Set<Set<T>> clusters = new HashSet<Set<T>>();
		for (T u : new HashSet<T>(elements)) {
			Set<Set<T>> toMerge = new HashSet<Set<T>>();
			for (Set<T> clu : clusters) {
				if(!clu.isEmpty() && matchAny(u, clu)) {
					clu.add(u);
					toMerge.add(clu);
				}
			}
			
			if(toMerge.isEmpty()) { //create a new cluster 
				Set<T> group = new HashSet<T>();
				group.add(u);
				clusters.add(group);
			} else if (toMerge.size() > 1) {
				// merge several groups into the first one
				Iterator<Set<T>> it = toMerge.iterator();
				Set<T> group = it.next();
				assert !group.isEmpty();
				while(it.hasNext()) {
					Set<T> g = it.next();
					group.addAll(g);
					g.clear();
				}
			}
		}
		
		//remove empty groups
		Set<Set<T>> result = new HashSet<Set<T>>();
		for(Set<T> clu : clusters) {
			if(!clu.isEmpty())
				result.add(clu);
		}
		
		return result;
	}
	
	
	private boolean matchAny(T u, Set<T> clu) {
		for(T t : clu) {
			if(match(u, t) || match(t, u))
				return true;
		}
		return false;
	}
	
}
