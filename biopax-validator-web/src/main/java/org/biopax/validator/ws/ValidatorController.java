package org.biopax.validator.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.Validator;
import org.biopax.validator.result.Validation;
import org.biopax.validator.result.ValidatorResponse;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * TODO e.g., add user sessions, re-use the result if a browser
 * TODO add more form parameters: threshold=error, warning, all; maxErrors= 
 * 
 * @author rodch
 *
 */
@Controller
public class ValidatorController {
	final static Log log = LogFactory.getLog(ValidatorController.class);

	private Validator validator;
	
	public ValidatorController() {
	}

	public ValidatorController(Validator validator) {
		this.validator = validator;
	}	
      
    @RequestMapping(value="/checkUrl", method=RequestMethod.GET)
    public void checkUrl() {};
    
    @RequestMapping(value="/checkUrl", method=RequestMethod.POST)
    public String checkUrl(
    		HttpServletRequest request,
    		@RequestParam(required=false) String url, 
    		@RequestParam(required=false) String retDesired,
    		@RequestParam(required=false) Boolean autofix,
    		@RequestParam(required=false) Boolean normalize,
    		Model model, Writer writer) throws IOException  
    {
    	Resource in = null;
    	String modelName = null;
    	if(url != null && url.length()>0) {
        	if(log.isInfoEnabled() && url != null) 
        		log.info("url : " + url);
        	try {
        		in = new UrlResource(url);
        	} catch (MalformedURLException e) {
        		model.addAttribute("error", e.toString());
        		return "checkUrl";
			}
        	modelName = in.getDescription();
    	} else if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Map files = multiRequest.getFileMap();
			Assert.state(!files.isEmpty(), "No file!");
			Object o = files.values().iterator().next();
			MultipartFile file = (MultipartFile) o;
			String filename = file.getOriginalFilename();
			if(log.isInfoEnabled()) 
				log.info("file : " + filename);
			in = new ByteArrayResource(file.getBytes());
			modelName = filename;
		} else {
			return errorResponse(model, 
				"checkUrl", "No BioPAX input source provided!");
		}
			
    	ValidatorResponse validatorResponse = new ValidatorResponse();
		Validation result = new Validation();
		result.setDescription(modelName);
		if (autofix != null && autofix == true) {
			result.setFix(true);
		}
		if (normalize != null && normalize == true) {
			result.setNormalize(true);
		}
		
		InputStream input = null;
		try {
			input = in.getInputStream();
		} catch (Exception e) {
			return errorResponse(model, 
				"checkUrl", "Bad BioPAX input source: " 
					+ e.toString());
		}
		
		validator.importModel(result, input);
		validator.validate(result);
    	validatorResponse.addValidationResult(result);
    	validator.getResults().remove(result);
    	String owl = result.getFixedOwl();
    	
    	if("xml".equalsIgnoreCase(retDesired)) {
        	BiopaxValidatorUtils.write(result, writer, null);
    	} else if("html".equalsIgnoreCase(retDesired)) {
			if(owl != null) {
				// encode OWL for displaying on the HTML page
				result.setFixedOwl(StringEscapeUtils.escapeHtml(owl));
			}
			model.addAttribute("response", validatorResponse);
			return "groupByCodeResponse";
		} else { // owl only
			if(owl != null)
				writer.write(owl);
			else
				return errorResponse(model, "checkUrl", "No BioPAX data returned.");
		}
    	
    	return null; // (writer used)
    }
	
    private String errorResponse(Model model, String viewName, String msg) {
    	model.addAttribute("error", msg);
		return viewName;
    }
    
    
    @RequestMapping(value="/checkFile", method=RequestMethod.GET)
    public void checkFile() {	
    } // show form
   

    /*
	 * validates several BioPAX files
	 */
    @RequestMapping(value="/checkFile", method = RequestMethod.POST)
	public String checkFile(HttpServletRequest request,
			@RequestParam(value="retDesired", required=false) String retDesired) 
    	throws IOException 
    {			
		if("xml".equalsIgnoreCase(retDesired)) {
			return "forward:xmlResult.html";
		} else {
			return "forward:htmlResult.html";
		}
		
	}
    
    
    @RequestMapping(value="/htmlResult", method = RequestMethod.POST)
	public String checkFile(HttpServletRequest request, Model model) 
    	throws IOException 
	{			
		ValidatorResponse validatorResponse = checkFile(request);
		model.addAttribute("response", validatorResponse);
		return "groupByCodeResponse";
	}
    
    
    @RequestMapping(value="/xmlResult", method = RequestMethod.POST)
    public void getResultsAsXml(HttpServletRequest request, Writer writer) 
    	throws IOException 
    {
    	ValidatorResponse response = checkFile(request);
    	if(response != null) {
    		BiopaxValidatorUtils.write(response, writer, null);
    	} else {
    		writer.write("Empty Result or Error.");
    	}
    }
    
 /*   
    @RequestMapping("/home")
    public void homePage() {}

    
    @RequestMapping("/ws")
    public void wsDescriptionPage() {}
*/    
    
    
	private ValidatorResponse checkFile(HttpServletRequest request) throws IOException 
	{			
		ValidatorResponse validatorResponse = new ValidatorResponse();
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Map files = multiRequest.getFileMap();
			Assert.state(files.size() > 0, "No files were uploaded");
			for (Object o : files.values()) {
				MultipartFile file = (MultipartFile) o;
				String filename = file.getOriginalFilename();
				// a workaround (for some reason there is always a no-name-file;
				// this might be a javascript isue)
				if(file.getBytes().length==0 || filename==null || "".equals(filename)) 
				{
					continue;
				}
				if(log.isInfoEnabled()) 
					log.info("checkFile : " + filename);
				
				Resource in = new ByteArrayResource(file.getBytes());
				Validation validation = new Validation();
				validation.setDescription(filename);
				validator.importModel(validation, in.getInputStream());
				validator.validate(validation);
				validatorResponse.addValidationResult(validation);
				validator.getResults().remove(validation);
			}
			
		}
		
		return validatorResponse;
	}
}