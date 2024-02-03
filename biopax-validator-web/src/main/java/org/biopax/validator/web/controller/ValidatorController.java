package org.biopax.validator.web.controller;


import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.normalizer.Normalizer;
import org.biopax.validator.api.beans.Behavior;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.web.service.ValidatorAdapter;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.beans.ValidatorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@Controller
public class ValidatorController {

  private ValidatorAdapter service;

  private final static Log log = LogFactory.getLog(ValidatorController.class);
  private final static String NEWLINE = System.getProperty ( "line.separator" );

  @Autowired
  public ValidatorController(ValidatorAdapter service) {
    this.service = service;
  }

  @RequestMapping(value = {"schema","schema.html"}, method = RequestMethod.GET, produces = APPLICATION_XML_VALUE)
  @ResponseBody  public String getSchema() {
    return service.getSchema();
  }

  //Views (pages)

  @RequestMapping({"/", "home", "home.html"})
  public String home() {
    return "home";
  }

  @RequestMapping({"ws","ws.html"})
  public String ws() {
    return "ws";
  }

  @RequestMapping(value={"check","check.html"}, method=RequestMethod.GET)
  public void check(Model model) {
    model.addAttribute("normalizer", new Normalizer());
    //user can edit some of normalizer's options in the 'check' view
  }

  /**
   * JSP pages and RESTful web services controller, the main one that checks BioPAX data.
   * All parameter names are important, i.e., these are part of public API (for clients)
   *
   * System parameters:
   * @param request the web request object (may contain multi-part data, i.e., multiple files uploaded)
   * @param response
   * @param mvcModel Spring MVC Model
   * @param writer HTTP response writer
   *
   * Validator parameters:
   * @param url
   * @param retDesired
   * @param autofix
   * @param filter
   * @param maxErrors
   * @param profile
   *
   * Normalizer parameters:
   * @param normalizer binds to view options: normalizer.fixDisplayName,
   *                   normalizer.inferPropertyOrganism, normalizer.inferPropertyDataSource
   * @return results view name (or null if XML or normalized RDF/XML were requested)
   * @throws IOException when data cannot be read from the files or URL, etc.
   */
  @RequestMapping(value={"check","check.html"}, method=RequestMethod.POST)
  public String check(HttpServletRequest request, HttpServletResponse response,
                      Model mvcModel, Writer writer,
    @RequestParam(required=false) String url,
    @RequestParam(required=false) String retDesired,
    @RequestParam(required=false) Boolean autofix,
    @RequestParam(required=false) Behavior filter,
    @RequestParam(required=false) Integer maxErrors,
    @RequestParam(required=false) String profile,
    //normalizer!=null when called from the JSP;
    //but it's usually null when from the validator-client or a web script
    @ModelAttribute("normalizer") Normalizer normalizer) throws IOException
  {
    Resource resource; //to validate
    final int lim = (maxErrors != null)? maxErrors.intValue() : 0; //0->no error limit
    final boolean fix = Boolean.TRUE.equals(autofix);

    // create the response container
    ValidatorResponse validatorResponse = new ValidatorResponse();

    if(url != null && url.length()>0) {
      log.info("url : " + url);

      try {
        resource = new UrlResource(url);
      } catch (MalformedURLException e) {
        return errorView(mvcModel, e.toString());
      }

      try {
        Validation v = service.validate(resource, lim, fix, filter, profile, normalizer);
        validatorResponse.addValidationResult(v);
      } catch (Exception e) {
        return errorView(mvcModel, e.toString());
      }

    } else if (request instanceof MultipartHttpServletRequest) {
      MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
      Map<String,MultipartFile> files = multiRequest.getFileMap();
      Assert.state(!files.isEmpty(), "No files to validate");
      for (MultipartFile file : files.values()) {
        String filename = file.getOriginalFilename();
        // a workaround (for some reason there is always a no-name-file;
        // this might be a javascript isue)
        if(file.getBytes().length==0 || filename==null || "".equals(filename))
          continue;

        log.info("check : " + filename);
        resource = new ByteArrayResource(file.getBytes(), filename);
        try {
          Validation v = service.validate(resource, lim, fix, filter, profile, normalizer);
          validatorResponse.addValidationResult(v);
        } catch (Exception e) {
          return errorView(mvcModel, e.toString());
        }
      }
    } else {
      return errorView(mvcModel, "No BioPAX input source provided!");
    }

    if("xml".equalsIgnoreCase(retDesired)) {
      response.setContentType("application/xml");
      ValidatorUtils.write(validatorResponse, writer, null);
    } else if("html".equalsIgnoreCase(retDesired)) {
    		/* could also use ValidatorUtils.write with a xml-to-html xslt source
    		 but using JSP here makes it easier to keep the same style, header, footer*/
      mvcModel.addAttribute("response", validatorResponse);
      return "groupByCodeResponse";
    } else { //the fixed/normalized OWL (BioPAX RDF/XML) was requested
      response.setContentType("text/plain");
      // write all the models one after another (RDF spec. allows that,
      // despite Paxtools might not be able to parse it)
      for(Validation result : validatorResponse.getValidationResult()) {
        if(result.getModelData() != null)
          writer.write(result.getModelData() + NEWLINE);
        else // write "empty" rdf
          writer.write("<rdf:RDF></rdf:RDF>" + NEWLINE);
      }
    }
    //no view as result/error was written to the response stream
    return null;
  }

  private String errorView(Model model, String msg) {
    model.addAttribute("error", msg);
    return null;
  }

}