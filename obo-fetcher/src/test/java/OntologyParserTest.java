/*
 * #%L
 * Ontologies Access
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
import static org.junit.Assert.*;

import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.biopax.psidev.ontology_manager.impl.OntologyAccessImpl;
import org.biopax.psidev.ontology_manager.impl.OntologyLoaderException;
import org.biopax.psidev.ontology_manager.impl.OntologyManagerContext;
import org.biopax.psidev.ontology_manager.impl.OntologyManagerImpl;
import org.junit.*;


import java.util.Collection;
import java.util.Properties;

public class OntologyParserTest {
	 
	@Test
	public void ontologyLoading() throws OntologyLoaderException {
		
		final Properties cfg = new Properties();
		cfg.put("SO", "classpath:so.obo");
		cfg.put("MI", "classpath:mi.obo");
		cfg.put("MOD", "classpath:mod.obo");
		
		OntologyManager manager = new OntologyManagerImpl(cfg);
		
		Collection<String> ontologyIDs = manager.getOntologyIDs();
		assertTrue(ontologyIDs.contains("MOD"));
		assertTrue(ontologyIDs.contains("SO"));
		assertTrue(ontologyIDs.contains("MI"));

		OntologyAccess oa2 = manager.getOntology("MOD");
		assertNotNull(oa2);
		assertTrue(oa2 instanceof OntologyAccessImpl);
		
		OntologyTermI t = oa2.getTermForAccession("MOD:00048");
		assertNotNull(t);
		//test that apostrophe is not escaped (-due to a bug in the OBO parser, part of ols-1.18)!
		assertTrue(t.getPreferredName().equalsIgnoreCase("O4'-phospho-L-tyrosine"));
		

		oa2 = manager.getOntology("SO");
		Assert.assertNotNull(oa2);
		Assert.assertTrue(oa2 instanceof OntologyAccessImpl);

		oa2 = manager.getOntology("MI");
		assertNotNull(oa2);
		assertTrue(oa2 instanceof OntologyAccessImpl);
	}
}
