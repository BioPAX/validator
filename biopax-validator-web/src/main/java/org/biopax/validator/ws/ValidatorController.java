package org.biopax.validator.ws;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

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
    public String checkUrl(@RequestParam String url, 
    		@RequestParam(required=false) String retDesired,
    		Model model) throws IOException  {
    	if(log.isInfoEnabled()) log.info("checkUrl : " + url);
    	ValidatorResponse validatorResponse = new ValidatorResponse();
    	Resource in = new UrlResource(url);
		String modelName = in.getDescription();
		Validation result = new Validation();
		result.setDescription(modelName);
		validator.importModel(result, in.getInputStream());
		validator.validate(result);
    	validatorResponse.addValidationResult(result);
    	validator.getResults().remove(result);
		
    	if("xml".equalsIgnoreCase(retDesired)) {
			return "redirect:printXmlResult";
		} else {
			model.addAttribute("response", validatorResponse);
			return "groupByCodeResponse";
		}
    }
	

    @RequestMapping(value="/checkFile", method=RequestMethod.GET)
    public void checkFile() {	
    } // show form
   

    /*
	 * validates several BioPAX files
	 */
    @RequestMapping(value="/checkFile", method = RequestMethod.POST)
	public String checkFile(HttpServletRequest request,
			@RequestParam(value="retDesired", required=false) String retDesired,
			Writer writer, Model model) throws IOException 
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
				if(file.getBytes().length==0 || filename==null || "".equals(filename)) {
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
		
		model.addAttribute("response", validatorResponse);
		if("xml".equalsIgnoreCase(retDesired)) {
			return "redirect:xmlResult.html";
		} else {
			return "groupByCodeResponse";
		}
		
	}
    
    
    @RequestMapping("/xmlResult")
    public void getResultsAsXml(Model model, Writer writer) throws IOException {
    	if(!model.containsAttribute("response")) {
    		ValidatorResponse response = (ValidatorResponse) model.asMap().get("response");
    		BiopaxValidatorUtils.write(response, writer, null);
    	} else {
    		writer.write("Empty Result or Error.");
    	}
    }
     
    
    
    @RequestMapping("/home")
    public void homePage() {}
 
    @RequestMapping("/ws")
    public void wsDescriptionPage() {}
    
}