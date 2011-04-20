package org.biopax.validator.ws;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.result.*;
import org.biopax.validator.Validator;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.utils.Normalizer.NormalizerOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * TODO e.g., add user sessions, re-use the result if a browser
 * 
 * @author rodche
 *
 */
@Controller
public class ValidatorController {
	final static Log log = LogFactory.getLog(ValidatorController.class);

	private Validator validator;
	
	private final static DefaultResourceLoader LOADER = new DefaultResourceLoader();
	
	private static final String NEWLINE = System.getProperty ( "line.separator" );
	
	public ValidatorController() {
	}

	public ValidatorController(Validator validator) {
		this.validator = validator;
	}	
      
    @RequestMapping(value="/check", method=RequestMethod.GET)
    public void check(Model model) {
    	model.addAttribute("options", new NormalizerOptions());
    }   
    
    @RequestMapping(value="/check", method=RequestMethod.POST)
    public String check( 
    		//use of smth. like @ModelAttribute("validation") Validation validation is inconvenient when multiple files...
    		HttpServletRequest request,
    		@RequestParam(required=false) String url, 
    		@RequestParam(required=false) String retDesired,
    		@RequestParam(required=false) Boolean autofix,
    		@RequestParam(required=false) Boolean normalize,
    		@RequestParam(required=false) Behavior filter,
    		@RequestParam(required=false) Integer maxErrors,
    		//extra options (same for all files/url to check)
    		@ModelAttribute("options") NormalizerOptions options,
    		Model model, Writer writer) throws IOException  
    {
    	Resource in = null; // a resource to validate
    	
    	// create the response container
    	ValidatorResponse validatorResponse = new ValidatorResponse();
    	
    	if(url != null && url.length()>0) {
        	if(log.isInfoEnabled() && url != null) 
        		log.info("url : " + url);
        	try {
        		in = new UrlResource(url);
        	} catch (MalformedURLException e) {
        		model.addAttribute("error", e.toString());
        		return "check";
			}
        	
        	Validation v = newValidation(in.getDescription(), autofix, normalize, filter);
        	if(maxErrors != null) {
        		v.setMaxErrors(maxErrors.intValue());
        		log.info("Using max. errors limit=" + maxErrors 
        			+ "; success= " + v.isMaxErrorsSet());
        	}
        	
        	if(normalize!= null && normalize == true && options != null) {
        		v.setNormalizerOptions(options);
        	}
        	
    		try {
    			doCheck(v, in);
    	    	validatorResponse.addValidationResult(v);
    		} catch (Exception e) {
    			return errorResponse(model, 
    				"check", "Exception: " + e);
    		}
    	} else if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Map files = multiRequest.getFileMap();
			Assert.state(!files.isEmpty(), "No file!");
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
					log.info("check : " + filename);
				
				in = new ByteArrayResource(file.getBytes());
	    		try {
	    			Validation v = newValidation(filename, autofix, normalize, filter);
	    			if(maxErrors != null) {
	            		v.setMaxErrors(maxErrors.intValue());
	            		log.info("Using max. errors limit=" + maxErrors 
	            			+ "; success= " + v.isMaxErrorsSet());
	            	}
	            	if(normalize!= null && normalize == true && options != null) {
	            		v.setNormalizerOptions(options);
	            	}
	    			doCheck(v, in);
	    	    	validatorResponse.addValidationResult(v);
	    		} catch (Exception e) {
	    			return errorResponse(model, 
	    				"check", "Exception: " + e);
	    		}
			}			
		} else {
			return errorResponse(model, 
				"check", "No BioPAX input source provided!");
		}
		
    	if("xml".equalsIgnoreCase(retDesired)) {
        	BiopaxValidatorUtils.write(validatorResponse, writer, null);
    	} else if("html".equalsIgnoreCase(retDesired)) {
			model.addAttribute("response", validatorResponse);
			return "groupByCodeResponse";
		} else { // owl only
			// write all the OWL results one after another TODO any better solution?
			for(Validation result : validatorResponse.getValidationResult()) 
			{
				if(result.getModelSerialized() != null)
					writer.write(result.getModelSerialized() + NEWLINE);
				else
					// empty result
					writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
						"<rdf:RDF></rdf:RDF>" + NEWLINE);
			}
		}
    	
    	return null; // (Writer is used instead)
    }
	
    
    private void doCheck(Validation v, Resource in) throws IOException {
		InputStream input = in.getInputStream();
		validator.importModel(v, input);
		validator.validate(v);
		if(v.isFix() || v.isNormalize()) {
			v.updateModelSerialized();
		}
    	validator.getResults().remove(v);
	}

    
	private Validation newValidation(String name, Boolean autofix, 
    		Boolean normalize, Behavior filterBy) 
    {
    	Validation v = new Validation(name);
		
    	if (autofix != null && autofix == true) {
			v.setFix(true);
		}
		if (normalize != null && normalize == true) {
			v.setNormalize(true);
		}
		if (filterBy != null) {
			v.setThreshold(filterBy);
		}
		
		return v;
    }

    
    private String errorResponse(Model model, String viewName, String msg) {
    	model.addAttribute("error", msg);
		return viewName;
    }

    
    @RequestMapping(value="/schema")
    public void getSchema(Writer writer) throws IOException {
    	if(log.isDebugEnabled())
    		log.debug("XML Schema requested.");
    	
    	BufferedReader bis = new BufferedReader(new InputStreamReader(
    		LOADER.getResource("classpath:validator-response-2.0.xsd")
    			.getInputStream(), "UTF-8"));
    	
    	String line = null;
    	while((line = bis.readLine()) != null) {
    		writer.write(line + NEWLINE);
    	}
    } 
}