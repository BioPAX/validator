package org.biopax.validator.ws;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.view.xslt.AbstractXsltView;

/**
 * @author rodch
 * 
 */
public class ValidatorResponseView extends AbstractXsltView {
	final static Log log = LogFactory.getLog(ValidatorResponseView.class);
	
	public ValidatorResponseView() {
		super();
	}
	
	protected Source createXsltSource(Map model, String rootName, 
			HttpServletRequest request, HttpServletResponse response) 
			throws Exception {		
		return (DOMSource) model.get("response");
	}
	
}
