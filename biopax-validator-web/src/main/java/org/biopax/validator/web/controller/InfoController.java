package org.biopax.validator.web.controller;

import java.util.*;

import org.biopax.validator.api.beans.Behavior;
import org.biopax.validator.api.beans.Category;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.Rule;
import org.biopax.validator.api.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class InfoController {

  private Validator validator;
  private ValidatorUtils utils;
  private Properties errorTypes;
  private Set extraDbSynonyms;

  @Autowired
  public InfoController(Validator validator, ValidatorUtils utils,
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

  @RequestMapping(value={"rules","rules.html"})
  public @ModelAttribute("rules") Collection<AboutRule> rules() {

    Set<AboutRule> rules = new TreeSet<>();

    for(Rule r: validator.getRules()) {
      String name = r.getClass().getName();
      rules.add(new AboutRule(name, utils.getRuleDescription(name),
        utils.getRuleBehavior(name, null), utils.getRuleBehavior(name, "notstrict")));
    }

    return rules;
  }

  /**
   * This (DTO) is used in the 'rules' JSP page/view
   */
  public static class AboutRule implements Comparable<AboutRule>{
    private String name;
    private String tip;
    private Behavior stdProfile;
    private Behavior altProfile;

    AboutRule(String name, String tip, Behavior stdProfile, Behavior altProfile) {
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
  }

  @ModelAttribute("categories")
  public Category[] errorCategories() {
    return Category.values();
  }

  @RequestMapping(value={"errorTypes","errorTypes.html"})
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

    return new TreeSet<>(map.values());
  }

  /**
   * DTO (for the JSP view)
   */
  public static class ErrorCfg implements Comparable<ErrorCfg>{
    private String code;
    private String defaultMsg;
    private String caseMsgTemplate;
    private String category;

    ErrorCfg(String code) {
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

  // JSP view knows what to do (uses the MVC model and model attributes)
  @RequestMapping({"extraCfg","extraCfg.html"})
  public void extraCfg() {}
}