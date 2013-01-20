/*
 * #%L
 * BioPAX Validator Integration Tests
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
import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;

import org.biopax.paxtools.io.*;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;

/**
 *
 * This test does not use a Spring context nor AOP,
 * so no methods and exceptions are intercepted
 * in paxtools.
 *
 *
 * Required JVM OPTIONS: -Xmx2048m
 *
 * @author rodche
 */

public class LibsTest {
	
	static final String L2_SHORT_MET_PATHWAY = "biopax_id_557861_mTor_signaling.owl";
	static final String L3_SHORT_MET_PATHWAY = "biopax3-short-metabolic-pathway.owl";	   

    @Test
    public void testBuildPaxtoolsL2ModelSimple() throws FileNotFoundException  {
        System.out.println("with Level2 data");
        InputStream is = getClass().getResourceAsStream(L2_SHORT_MET_PATHWAY);
        SimpleIOHandler io = new SimpleIOHandler();
        io.mergeDuplicates(true);
        Model model = io.convertFromOWL(is);
        assertNotNull(model);
        assertFalse(model.getObjects().isEmpty());
    }

   
    @Test
    public void testBuildPaxtoolsL3ModelSimple() throws FileNotFoundException {
        System.out.println("with Level3 data");
        InputStream is = getClass().getResourceAsStream(L3_SHORT_MET_PATHWAY);
        SimpleIOHandler simpleReader = new SimpleIOHandler();
        simpleReader.mergeDuplicates(true);
        Model model = simpleReader.convertFromOWL(is);
        assertNotNull(model);
        assertFalse(model.getObjects().isEmpty());
        
//        for(BioPAXElement e: model.getObjects()) {
//        	if(e instanceof Named) {
//        		System.out.println(e + " " + e.getModelInterface().getSimpleName() + 
//        				" name:"+ ((Named)e).getStandardName()
//        				+ ", displayName: " + ((Named)e).getDisplayName());
//        	} else if(e instanceof Xref) {
//        		System.out.println(e + " " + e.getModelInterface().getSimpleName() + 
//        				" db:"+ ((Xref)e).getDb()
//        				+ ", id: " + ((Xref)e).getId());
//        	}
//        }
        
    }
    
    
    @Test
    public void testIsEquivalentUnificationXref() {
    	BioPAXFactory factory3 = BioPAXLevel.L3.getDefaultFactory();
    	UnificationXref x1 = factory3.create(UnificationXref.class, "x1");
    	x1.addComment("x1");
    	x1.setDb("db");
    	x1.setId("id");
    	UnificationXref x2 = factory3.create(UnificationXref.class, "x2");
    	x2.addComment("x2");
    	x2.setDb("db");
    	x2.setId("id");
    	
    	assertTrue(x1.isEquivalent(x2));
    	
    	UnificationXref x3 = factory3.create(UnificationXref.class, "x1");
    	x3.addComment("x3");
    	x3.setDb(null);
    	x3.setId("doesn't matter");
    	assertFalse(x1.isEquivalent(x3)); // same ID does not matter anymore (since Apr'2011)!
    	
    	x3.setDb("db");
    	x3.setId("id");
    	assertTrue(x1.isEquivalent(x3)); 
    	
    	x3 = x1;
    	assertTrue(x1.isEquivalent(x3)); 
    }

}
