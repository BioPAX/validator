/*
 * #%L
 * BioPAX Validator Client
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
import java.io.*;

import javax.xml.bind.JAXBException;

import org.biopax.validator.BiopaxValidatorClient;
import org.biopax.validator.BiopaxValidatorClient.RetFormat;
import org.biopax.validator.jaxb.ValidatorResponse;
import org.junit.*;

// remove @Ignore when biopax.org/biopax-validator/  is available
@Ignore
public class BiopaxValidatorClientTest {

	@Test
	public void testClientHtml() throws IOException {
		BiopaxValidatorClient client = new BiopaxValidatorClient();
		
		File[] files = new File[] {
				new File(getClass().getResource(
						File.separator + "testBiopaxElementIdRule.owl").getFile())
		};
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.validate(false, null, RetFormat.HTML, null, null, null, files, baos);
		
		System.out.println(baos.toString());
    }
	
	@Test
	public void testClientXml() throws IOException, JAXBException {
		BiopaxValidatorClient client = new BiopaxValidatorClient();
		
		File[] files = new File[] {
				new File(getClass().getResource(
					File.separator + "testBiopaxElementIdRule.owl").getFile())
		};
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.validate(true, null, RetFormat.XML, null, null, null, files, baos);
		
		//System.out.println(baos.toString());
		
		Assert.assertTrue(baos.size()>0);
		
		ValidatorResponse resp = client.unmarshal(baos.toString());
		
		Assert.assertNotNull(resp);
		Assert.assertFalse(resp.getValidation().isEmpty());
		
		System.out.println(resp.getValidation().get(0).getSummary()
				+ "; cases: " + resp.getValidation().get(0).getTotalProblemsFound());
    }
	
}
