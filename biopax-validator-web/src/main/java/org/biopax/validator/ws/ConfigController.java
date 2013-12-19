package org.biopax.validator.ws;

/*
 * #%L
 * BioPAX Validator Web Application
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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.Rule;
import org.biopax.validator.api.Validator;
import org.biopax.validator.api.beans.Behavior;
import org.biopax.validator.api.beans.Category;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author rodch
 *
 */

@Controller
public class ConfigController {
	final static Log log = LogFactory.getLog(ConfigController.class);

	private Validator validator;
	private ValidatorUtils utils;
	private Properties errorTypes;
	private Set extraDbSynonyms;
	
	public ConfigController() {
	}

	public ConfigController(Validator validator, ValidatorUtils utils, 
			Properties errorTypes, Set extraDbSynonyms) {
		this.validator = validator;
		this.utils = utils;
		this.errorTypes = errorTypes;
		this.extraDbSynonyms = extraDbSynonyms;
	}
	
    @ModelAttribute("behaviors")
    public Behavior[] ruleBehaviors() {
        return Behavior.values();
    }
         
	@RequestMapping(value="/rules")
    public @ModelAttribute("rules") Collection<AboutRule> rules() {
		
		Set<AboutRule> rules = new TreeSet<AboutRule>();
		
  		for(Rule r: validator.getRules()) {
  			String name = r.getClass().getName();
  			rules.add(new AboutRule(name, utils.getRuleDescription(name), 
  				utils.getRuleBehavior(name, null), utils.getRuleBehavior(name, "notstrict")));
  		}
  		
  		return rules;
    }
	
	
	public final static class AboutRule implements Comparable<AboutRule>{
		String name;
		String tip;
		Behavior stdProfile;
		Behavior altProfile;
			
		public AboutRule(String name, String tip, Behavior stdProfile,
				Behavior altProfile) {
			this.name = name;
			this.tip = tip;
			this.stdProfile = stdProfile;
			this.altProfile = altProfile;
		}


		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public String getTip() {
			return tip;
		}
		public void setTip(String tip) {
			this.tip = tip;
		}

		public Behavior getStdProfile() {
			return stdProfile;
		}
		public void setStdProfile(Behavior stdProfile) {
			this.stdProfile = stdProfile;
		}

		public Behavior getAltProfile() {
			return altProfile;
		}
		public void setAltProfile(Behavior altProfile) {
			this.altProfile = altProfile;
		}

		public int compareTo(AboutRule r) {return name.compareTo(r.name);};
	};
	
    
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
    
    @ModelAttribute("extraDbSynonyms")
    public Set extraDbSynonyms() {
        return extraDbSynonyms;
    }

    @RequestMapping("/extraCfg")
    public void extraCfg() {} // the view knows what to do (using here defined @ModelAttribute methods)
}