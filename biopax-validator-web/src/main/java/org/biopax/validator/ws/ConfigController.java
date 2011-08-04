package org.biopax.validator.ws;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.result.Behavior;
import org.biopax.validator.result.Category;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.Rule;
import org.biopax.validator.Validator;
import org.springframework.security.access.annotation.Secured;
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
public class ConfigController {
	final static Log log = LogFactory.getLog(ConfigController.class);

	private Validator validator;
	private BiopaxValidatorUtils utils;
	private Properties errorTypes;
	private Map maxDisplayNameLengths;
	private Set warnOnDataPropertyValues;
	private Map dbAllow;
	private Set extraDbSynonyms;
	private Map dbDeny;
	
	public ConfigController() {
	}

	public ConfigController(Validator validator, BiopaxValidatorUtils utils, 
			Properties errorTypes, Map maxDisplayNameLengths, 
			Set warnOnDataPropertyValues, Map dbAllow, 
			Set extraDbSynonyms, Map dbDeny) {
		this.validator = validator;
		this.utils = utils;
		this.errorTypes = errorTypes;
		this.maxDisplayNameLengths = maxDisplayNameLengths;
		this.warnOnDataPropertyValues = warnOnDataPropertyValues;
		this.dbAllow = dbAllow;
		this.extraDbSynonyms = extraDbSynonyms;
		this.dbDeny = dbDeny;
	}
	
    @ModelAttribute("behaviors")
    public Behavior[] ruleBehaviors() {
        return Behavior.values();
    }
         
	@RequestMapping(value="/rules")
    public @ModelAttribute("rules") Collection<Rule<?>> rules() {
  		return validator.getRules();
    }
	
	@Secured("ROLE_ADMIN")
    @RequestMapping(value="/rule", method=RequestMethod.POST)
    public String rule(HttpServletRequest request) {
    	Rule r = validator.findRuleByName(request.getParameter("name"));
    	r.setBehavior(Behavior.valueOf(request.getParameter("behavior")));
    	return "redirect:rules.html";
    }    

    @RequestMapping(value="/rule", method=RequestMethod.GET)
    public @ModelAttribute("rule") Rule rule(@RequestParam(required=true) String name) {
    	Rule r = validator.findRuleByName(name);
        return r;
    } 
        
   /* 
    @RequestMapping("/admin")
    public void adminPage() {}
    */
    
    @ModelAttribute("categories")
    public Category[] errorCategories() {
        return Category.values();
    }
    
    
	@RequestMapping(value="/errorTypes")
    public @ModelAttribute("errorTypes") Collection<ErrorCfg> errorTypes() {
		Map<String, ErrorCfg> map = new HashMap<String, ErrorCfg>();
		// keys there are as: CODE, CODE.default, CODE.category (CODE is a validation error id)
		// extract unique error ids -
		for(Object key : errorTypes.keySet()) {
			String code = key.toString();
			if(!code.endsWith(".default") && !code.endsWith(".category")) {
				map.put(code, new ErrorCfg(code));
			}
		}
		
		// set the rest of error props -
		for(ErrorCfg cfg : map.values()) {
			cfg.category = errorTypes.getProperty(cfg.code + ".category", "N/A (default: information)");
			cfg.defaultMsg = errorTypes.getProperty(cfg.code + ".default", "N/A");
			cfg.caseMsgTemplate = errorTypes.getProperty(cfg.code, "N/A");
		}
		
		return new TreeSet<ErrorCfg>(map.values());
    }

	
	// a bean (for the view)
	public static class ErrorCfg implements Comparable<ErrorCfg>{
		public String code;
		public String defaultMsg;
		public String caseMsgTemplate;
		public String category;
		public ErrorCfg(String code) {
			this.code = code;
		}
		
		@Override
		public int compareTo(ErrorCfg that) {
			return this.code.compareToIgnoreCase(that.code);
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDefaultMsg() {
			return defaultMsg;
		}

		public void setDefaultMsg(String defaultMsg) {
			this.defaultMsg = defaultMsg;
		}

		public String getCaseMsgTemplate() {
			return caseMsgTemplate;
		}

		public void setCaseMsgTemplate(String caseMsgTemplate) {
			this.caseMsgTemplate = caseMsgTemplate;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}
	}
	
    @ModelAttribute("maxDisplayNameLengths")
    public Map maxDisplayNameLengths() {
        return maxDisplayNameLengths;
    }
    
    @ModelAttribute("warnOnDataPropertyValues")
    public Set warnOnDataPropertyValues() {
        return warnOnDataPropertyValues;
    }
    
    @ModelAttribute("dbAllow")
    public Map dbAllow() {
        return dbAllow;
    }
    
    @ModelAttribute("extraDbSynonyms")
    public Set extraDbSynonyms() {
        return extraDbSynonyms;
    }
    
    @ModelAttribute("dbDeny")
    public Map dbDeny() {
        return dbDeny;
    }
    

    @RequestMapping("/extraCfg")
    public void extraCfg() {} // the view knows what to do
}