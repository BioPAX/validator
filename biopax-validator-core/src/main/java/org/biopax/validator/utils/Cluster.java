package org.biopax.validator.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Cluster<T> {

	/**
	 * Separates the elements into sets of equivalent ones.
	 * 
	 * @param elements
	 * @param clusterMaxSize TODO
	 * @return
	 */
	public Collection<Set<T>> groupByEquivalence(T[] elements, int clusterMaxSize) {
		Collection<Set<T>> clasters = new HashSet<Set<T>>();

		for (int i = 0; i < elements.length; i++) {
			T u = elements[i];
			Set<T> clones = new HashSet<T>();
			clones.add(u);

			for (int j = i + 1; j < elements.length; j++) {
				T v = elements[j];
				if (match(u, v)) {
					clones.add(v);
				}
				if(clones.size() > clusterMaxSize) {
					break;
				}
			}

			if (clones.size() > 1) {
				clasters.add(clones);
			}

		}

		return clasters;
	}
	
	/**
	 * TODO implement in subclasses
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public abstract boolean match(T a, T b);
	
}
