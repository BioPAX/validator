package org.biopax.validator.utils;

import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DbSynonyms {
	private static final Log log = LogFactory.getLog(DbSynonyms.class);
	
	@Resource(name="manuallyAddedDbSynonyms")
	private Collection<List<String>> groups;
	
	public void setGroups(Set<List<String>> manuallyAddedDbSynonyms) {
		this.groups = manuallyAddedDbSynonyms;
	}
	
	public Collection<List<String>> getGroups() {
		return groups;
	}
	
	/**
	 * Adds alternative database name. 
	 * 
	 * @param newName
	 * @param name
	 * @param isToBePrimary
	 */
	public void addSynonym(final String newName, final String name, boolean isToBePrimary) {
		String synonym = dbName(newName);
		String member = dbName(name);
		List<String> list = getSynonyms(member);
		int idx = (isToBePrimary) ? 0 : list.size();
		if(list.isEmpty()) {
			list.add(member);
			groups.add(list);
			list.add(idx, synonym);
		} else {
			boolean alreadyPresent = list.contains(synonym);
			if(alreadyPresent && isToBePrimary) { 	
				list.remove(synonym);
			}
			list.add(idx, synonym);
		}
	}
	
	public void addSynonyms(Collection<String> synonyms, String member) {
		String dbName = dbName(member);
		List<String> g = getSynonyms(dbName);
		if(g.isEmpty()) {
			g.add(dbName);
			groups.add(g);
		}
		
		for(String n : synonyms) {
			n = dbName(n);
			if(!g.contains(n)) {
				g.add(n);
			}
		}
	}

	public List<String> getSynonyms(String name) {
		String dbName = dbName(name);
		for(List<String> g : groups) {
			if (g.contains(dbName)) {
				return g;
			}
		}
		// return empty list
		return new ArrayList<String>();
	}
	
	public String dbName(String name) {
		return name.trim().toUpperCase();
	}
       
}
