package org.biopax.validator.ws;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.Behavior;
import org.biopax.validator.Rule;
import org.biopax.validator.Validator;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.security.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * @author rodch
 *
 */

@Controller
@RequestMapping("/config/*")
public class ConfigController {
	final static Log log = LogFactory.getLog(ConfigController.class);

	private static final String ADMIN = "redirect:admin.html";
	
	private Validator validator;
	private BiopaxValidatorUtils utils;
	
	public ConfigController() {
	}

	public ConfigController(Validator validator, BiopaxValidatorUtils utils) {
		this.validator = validator;
		this.utils = utils;
	}
	
    @ModelAttribute("behaviors")
    public Behavior[] newRequest() {
        return Behavior.values();
    }
         
	@RequestMapping(value="rules", method=RequestMethod.GET)
    public @ModelAttribute("rules") Collection<Rule<?>> rules() {
  		return validator.getRules();
    }
	
	@Secured("ROLE_ADMIN")
    @RequestMapping(value="rule", method=RequestMethod.POST)
    public String rule(HttpServletRequest request) {
    	Rule r = validator.findRuleByName(request.getParameter("name"));
    	r.setBehavior(Behavior.valueOf(request.getParameter("behavior")));
    	return "redirect:rules.html";
    }    

    @RequestMapping(value="rule.html", method=RequestMethod.GET)
    public @ModelAttribute("rule") Rule rule(@RequestParam(required=true) String name) {
    	Rule r = validator.findRuleByName(name);
        return r;
    } 
        

    @Secured("ROLE_ADMIN")
    @RequestMapping(value="errors", method=RequestMethod.POST)
    public String errors(@RequestParam("maxErrors") String maxErrors) {
    	utils.setMaxErrors(Integer.parseInt(maxErrors));
		return "redirect:errors";
    }    

    @RequestMapping(value="errors", method=RequestMethod.GET)
    public @ModelAttribute("utils") BiopaxValidatorUtils errors() {
    	return this.utils;
    }
}