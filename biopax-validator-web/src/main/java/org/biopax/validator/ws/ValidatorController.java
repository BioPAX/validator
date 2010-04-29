package org.biopax.validator.ws;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.dom.DOMSource;

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
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
* TODO e.g., add user sessions, re-use the result if a browser
 * submits exactly the same data again or user simply wants a different view...
 * 
 * @author rodch
 *
 */

@Controller
@RequestMapping("/validator/*")
public class ValidatorController {
	final static Log log = LogFactory.getLog(ValidatorController.class);

	private static final String HOME = "redirect:index.html";
	
	private Validator validator;
	
	public ValidatorController() {
	}

	public ValidatorController(Validator validator) {
		this.validator = validator;
	}	
      
    @RequestMapping(value="checkUrl", method=RequestMethod.GET)
    public void checkUrl() {};
    
    @RequestMapping(value="checkUrl", method=RequestMethod.POST)
    public ModelAndView checkUrl(@RequestParam String url, @RequestParam String retDesired) throws IOException  {
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
  		return getResponseView(retDesired, validatorResponse);
    }
	

    @RequestMapping(value="checkFile", method=RequestMethod.GET)
    public void checkFile() {	
    } // show form
   

    /*
	 * validates several BioPAX files
	 */
    @RequestMapping(value="checkFile", method = RequestMethod.POST)
	public ModelAndView checkFile(HttpServletRequest request)
			throws IOException {
				
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
		
		// consider the report parameters
		String retDesired = request.getParameter("retDesired");
		ModelAndView mView = getResponseView(retDesired, validatorResponse);
		return mView;
	}
    
    
    private ModelAndView getResponseView(String type, ValidatorResponse validatorResponse) {
    	ModelAndView mView = new ModelAndView(HOME); // fall-back
    	ModelMap model = new ModelMap();
		if(type.equalsIgnoreCase("xml")) {
			model.addAttribute("response", new DOMSource(BiopaxValidatorUtils.asDocument(validatorResponse)));
			mView = new ModelAndView("xmlresponse", model);
		} else if(type.equalsIgnoreCase("html")) {
			model.addAttribute("response", validatorResponse);
			mView = new ModelAndView("groupByCodeResponse", model);
		}
		return mView;
    } 
    
}