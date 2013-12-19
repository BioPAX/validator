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

import static org.junit.Assert.*;

import java.io.*;

import javax.xml.transform.stream.StreamSource;

import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.beans.Behavior;
import org.biopax.validator.api.beans.ErrorCaseType;
import org.biopax.validator.api.beans.ErrorType;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.api.beans.ValidatorResponse;
import org.biopax.validator.impl.IdentifierImpl;
import org.junit.Before;
import org.junit.Test;

public class BiopaxValidatorUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testMarshalUnmarshalValidationResponse() throws Exception {
		ValidatorResponse response = new ValidatorResponse();
		Validation validation = new Validation(new IdentifierImpl(), "test", false, Behavior.WARNING, 0, null);
		ErrorType e = new ErrorType("foo.bar", Behavior.ERROR);
		ErrorCaseType err = new ErrorCaseType("junit-test", "Test", "test error message");
		e.addErrorCase(err);
		validation.addError(e);
		validation.addComment("test comment");
		response.addValidationResult(validation);
	
		Writer writer = new StringWriter();
		ValidatorUtils.write(response, writer, null);
		String xml = writer.toString();
		assertTrue(xml.length()>0);
		
		ValidatorResponse resp = (ValidatorResponse) ValidatorUtils.getUnmarshaller()
			.unmarshal(new StreamSource(new StringReader(xml)));
		assertTrue(resp.getValidationResult().size() == 1);
		
		writer = new StringWriter();
		ValidatorUtils.write(resp, writer, null);
		String xml2 = writer.toString();
		assertTrue(xml2.length()>0);
		
		System.out.println(xml+"\n\n\n"+xml2);
		assertEquals(xml, xml2);
		
		// test pretty html output works (by XSLT)
		writer = new StringWriter();
		ValidatorUtils.write(response, writer, new StreamSource(getClass().getResourceAsStream("/html-result.xsl")));
		String html = writer.toString();
		
//		System.out.println(html);
		assertTrue(html.length()>0);
		
		writer = new PrintWriter(new FileOutputStream(getClass().getClassLoader().getResource("")
				.getPath() + File.separator + "testXsltReport.html"));
			((PrintWriter)writer).println(html);
		writer.flush();
	}

	
	@Test
	public final void testMarshalUnmarshalFromMultipleResults() throws Exception {
		// build two val. results
		Validation validation1 = new Validation(new IdentifierImpl());
		ErrorType e = new ErrorType("foo.bar", Behavior.ERROR);
		ErrorCaseType err = new ErrorCaseType("junit-test", "Test", "test1 error message");
		e.addErrorCase(err);
		validation1.addError(e);
		validation1.addComment("test1 comment");
		Validation validation2 = new Validation(new IdentifierImpl());
		e = new ErrorType("foo.boo", Behavior.WARNING);
		err = new ErrorCaseType("junit-test", "Test", "test2 error message");
		e.addErrorCase(err);
		validation2.addError(e);
		validation2.addComment("test2 comment");
		
		// add to a val. response and serialize
		ValidatorResponse resp1 = new ValidatorResponse();
		resp1.addValidationResult(validation1);
		resp1.addValidationResult(validation2);
		StringWriter writer = new StringWriter();
		ValidatorUtils.write(resp1, writer, null);
		String xmlresp1 = writer.toString();
		assertTrue(xmlresp1.length()>0);
		//System.out.println(xmlresp1);
		
		// Now,
		// serialize the first result only
		writer = new StringWriter();
		ValidatorUtils.write(validation1, writer, null);
		String xml1 = writer.toString();
		assertTrue(xml1.length()>0);
		// serialize the second result only as well
		writer = new StringWriter();
		ValidatorUtils.write(validation2, writer, null);
		String xml2 = writer.toString();
		assertTrue(xml2.length()>0);
		
		
		// unmarshall each (independent xml results), 
		// then build a new response from them, and serialize
		ValidatorResponse res = (ValidatorResponse) ValidatorUtils.getUnmarshaller()
			.unmarshal(new StreamSource(new StringReader(xml1)));
		assertNotNull(res);
		assertFalse(res.getValidationResult().isEmpty());
		Validation res1 = res.getValidationResult().get(0);
		assertTrue(res1.getError().size() == 1);
		
		res = (ValidatorResponse) ValidatorUtils.getUnmarshaller()
			.unmarshal(new StreamSource(new StringReader(xml2)));
		assertNotNull(res);
		assertFalse(res.getValidationResult().isEmpty());
		Validation res2 = res.getValidationResult().get(0);
		assertTrue(res2.getError().size() == 1);
		ValidatorResponse resp2 = new ValidatorResponse();
		resp2.addValidationResult(res1);
		resp2.addValidationResult(res2);
		
		writer = new StringWriter();
		ValidatorUtils.write(resp2, writer, null);
		String xmlresp2 = writer.toString();
		assertTrue(xmlresp2.length()>0);
		
		assertEquals(xmlresp1, xmlresp2);	
	}
}
