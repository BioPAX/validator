package org.biopax.validator.utils;

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
