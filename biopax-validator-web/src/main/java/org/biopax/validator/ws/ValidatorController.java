package org.biopax.validator.ws;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.result.*;
import org.biopax.validator.Validator;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.utils.Normalizer;
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
    	model.addAttribute("normalizer", new Normalizer());
    }   
    
    
    /**
     * JSP pages and RESTful web services controller, the main one that checks BioPAX data.
     * All parameter names are important, i.e., these are part of public API (for clients)
     * 
     * Framework's built-in parameters:
     * @param request the web request object (may contain multi-part data, i.e., multiple files uploaded)
     * @param mvcModel Spring MVC Model
     * @param writer HTTP response writer
     * 
     * BioPAX Validator/Normalizer query parameters:
     * @param url
     * @param retDesired
     * @param autofix
     * @param normalize
     * @param filter
     * @param maxErrors
     * @param profile
     * @param normalizer binds to three boolean options: normalizer.fixDisplayName, normalizer.inferPropertyOrganism, normalizer.inferPropertyDataSource
     * @return
     * @throws IOException
     */
    @RequestMapping(value="/check", method=RequestMethod.POST)
    public String check( 
    		//use of smth. like @ModelAttribute("validation") Validation validation is inconvenient when multiple files...
    		HttpServletRequest request, Model mvcModel, Writer writer, 
    		@RequestParam(required=false) String url, 
    		@RequestParam(required=false) String retDesired,
    		@RequestParam(required=false) Boolean autofix,
    		@RequestParam(required=false) Behavior filter,
    		@RequestParam(required=false) Integer maxErrors,
    		@RequestParam(required=false) String profile, 
    		@ModelAttribute("normalizer") Normalizer normalizer) 	throws IOException  
    {
    	Resource resource = null; // a resource to validate
    	
    	// create the response container
    	ValidatorResponse validatorResponse = new ValidatorResponse();
    	
    	if(url != null && url.length()>0) {
        	if(log.isInfoEnabled() && url != null) 
        		log.info("url : " + url);
        	try {
        		resource = new UrlResource(url);
        	} catch (MalformedURLException e) {
        		mvcModel.addAttribute("error", e.toString());
        		return "check";
			}       	
        	
        	try {
				Validation v = execute(resource, resource.getDescription(), maxErrors, autofix, filter, profile, normalizer);
				validatorResponse.addValidationResult(v);
			} catch (Exception e) {
				return errorResponse(mvcModel, "check", "Exception: " + e);
			}   		
    		
    	} else if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Map files = multiRequest.getFileMap();
			Assert.state(!files.isEmpty(), "No files to validate");
			for (Object o : files.values()) {
				MultipartFile file = (MultipartFile) o;
				String filename = file.getOriginalFilename();
				// a workaround (for some reason there is always a no-name-file;
				// this might be a javascript isue)
				if(file.getBytes().length==0 || filename==null || "".equals(filename)) 
					continue;

				if(log.isInfoEnabled()) 
					log.info("check : " + filename);
				
				resource = new ByteArrayResource(file.getBytes());				
				
				try {
					Validation v = execute(resource, filename, maxErrors, autofix, filter, profile, normalizer);
					validatorResponse.addValidationResult(v);
				} catch (Exception e) {
	    			return errorResponse(mvcModel, "check", "Exception: " + e);
				}    		
	    		
			}			
		} else {
			return errorResponse(mvcModel, 
				"check", "No BioPAX input source provided!");
		}
		
    	if("xml".equalsIgnoreCase(retDesired)) {
        	BiopaxValidatorUtils.write(validatorResponse, writer, null);
    	} else if("html".equalsIgnoreCase(retDesired)) {
    		/* could also use BiopaxValidatorUtils.write with a xml-to-html xslt source
    		 but using JSP here makes it easier to keep the same style, header, footer*/
			mvcModel.addAttribute("response", validatorResponse);
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
	
    
    
    /**
     * 
     * @param biopaxResource
     * @param resultName
     * @param maxErrors
     * @param autofix
     * @param errorLevel
     * @param profile name of a pre-defined validation profile (controls rules's behavior)
     * @param normalizer
     * @return
     * @throws IOException when cannot get the input stream from the resource
     * @throws BiopaxValidatorException when there was an exception in the validator/normalizer
     */
	private Validation execute(Resource biopaxResource, String resultName, Integer maxErrors,
			Boolean autofix, Behavior errorLevel, String profile, Normalizer normalizer) 
					throws IOException 
	{	
    	int errMax = 0;
    	if(maxErrors != null) {
    		errMax = maxErrors.intValue();
    		log.info("Limiting max no. errors to " + maxErrors);
    	}
    
    	boolean isFix = Boolean.TRUE.equals(autofix);
    	Validation validationResult = new Validation(resultName, isFix, errorLevel, errMax, profile);
    	
		validator.importModel(validationResult, biopaxResource.getInputStream());
		validator.validate(validationResult);
    	validator.getResults().remove(validationResult);   	    	
	
       	if(isFix) { // do normalize too
       		org.biopax.paxtools.model.Model m = (org.biopax.paxtools.model.Model) validationResult.getModel();
   			normalizer.normalize(m);
   			validationResult.updateModelSerialized(m);
       	}
       	
       	return validationResult;
	}

	    
    private String errorResponse(Model model, String viewName, String msg) {
    	model.addAttribute("error", msg);
		return viewName;
    }

    
    /**
     * Prints the XML schema.
     * 
     * @param writer
     * @throws IOException
     */
    //TODO consider using mvc:resources instead of the classpath and/or dependency on biopax-validator-client
    @RequestMapping(value="/schema")
    public void getSchema(Writer writer, HttpServletResponse response) 
    		throws IOException 
    {
    	if(log.isDebugEnabled())
    		log.debug("XML Schema requested.");
    	
    	BufferedReader bis = new BufferedReader(new InputStreamReader(
    		LOADER.getResource("classpath:validator-response-2.0.xsd")
    			.getInputStream(), "UTF-8"));
    	
    	response.setContentType("application/xml");
    	
    	String line = null;
    	while((line = bis.readLine()) != null) {
    		writer.write(line + NEWLINE);
    	}
    } 
    
}