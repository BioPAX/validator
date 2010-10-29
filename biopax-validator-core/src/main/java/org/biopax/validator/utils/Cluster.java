package org.biopax.validator.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.set.CompositeSet;

public abstract class Cluster<T> {

	/**
	 * Separates the elements into sets of "equivalent" 
	 * (depending on implementation) ones.
	 * 
	 * @param elements
	 * @param clusterMaxSize
	 * @return
	 */
	public CompositeSet<T> groupByEquivalence(T[] elements, int clusterMaxSize) {
		CompositeSet<T> clusters = new CompositeSet<T>();

		for (int i = 0; i < elements.length; i++) {
			T u = elements[i];
			// should not get here, but - doublecheck
			if(clusters.contains(u))
				continue;
			// begin a new cluster
			Set<T> clones = new HashSet<T>();
			clones.add(u);
			for (int j = i+1; j < elements.length; j++) {
				T v = elements[j];
				// if matches and has not been done before -
				if (!clusters.contains(v) && match(u, v)) {
						clones.add(v);
						// num. found exceeds the threshold?
						if (clones.size() > clusterMaxSize) {
							break;
						}
				}
			}

			if (clones.size() > 1) {
				clusters.addComposited(clones);
			}
		}

		return clusters;
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
