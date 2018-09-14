package org.biopax.validator.api;


import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.api.beans.*;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * Validator Utility Class (not BioPAX specific).
 * <p>
 * This is injected into other beans, keeps several global settings and objects,
 * e.g., marshaller, and also provides static service methods to register,
 * merge, do OXM, and resolve validation errors to human-readable verbose messages.
 *
 * @author rodche
 */
@Service
public class ValidatorUtils {
  private static final Log logger = LogFactory.getLog(ValidatorUtils.class);

  private Locale locale;
  private MessageSource messageSource;
  //private static Jaxb2Marshaller resultsMarshaller;
  private static JAXBContext jaxbContext;
  private int maxErrors = Integer.MAX_VALUE;

  static {
    try {
      jaxbContext = JAXBContext.newInstance(
        ValidatorResponse.class, Validation.class,
        ErrorCaseType.class, ErrorType.class,
        Behavior.class, Category.class);
    } catch (JAXBException e) {
      throw new RuntimeException("Failed to initialize the " +
        "org.biopax.validator.result JAXB context!", e);
    }
  }

  public ValidatorUtils() {
    this.locale = Locale.getDefault();
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }

  /**
   * Gets the results Marshaller (for validation results.)
   *
   * @return
   */
  public static Marshaller getMarshaller() {
    //return resultsMarshaller;
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      return marshaller;
    } catch (JAXBException e) {
      throw new RuntimeException("Failed to create Marshaller", e);
    }
  }

  /**
   * Gets the results Unmarshaller (for validation results.)
   *
   * @return unmarshaller
   */
  public static Unmarshaller getUnmarshaller() {
    //return resultsMarshaller;
    try {
      return jaxbContext.createUnmarshaller();
    } catch (JAXBException e) {
      throw new RuntimeException("Failed to create Unmarshaller", e);
    }
  }


  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public MessageSource getMessageSource() {
    return messageSource;
  }

  /**
   * Gets current max number of errors to report.
   *
   * @return maximal no. errors to be collected (to abort and return the results)
   */
  public int getMaxErrors() {
    return maxErrors;
  }

  /**
   * Sets current max number of errors to report.
   *
   * @param max maximal no. errors to be collected (to abort and return the results)
   */
  public void setMaxErrors(int max) {
    maxErrors = max;
  }


  /**
   * Writes the multiple results report.
   * (as transformed XML).
   *
   * @param validatorResponse results
   * @param writer output
   * @param xslt source
   */
  public static void write(ValidatorResponse validatorResponse, Writer writer, Source xslt) {
    try {
      if (xslt != null) {
        Document doc = asDocument(validatorResponse);
        Source xmlSource = new DOMSource(doc);
        Result result = new StreamResult(writer);
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xslt);
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        trans.transform(xmlSource, result);
      } else {
        // write without any xslt
        getMarshaller().marshal(validatorResponse, writer);
      }

    } catch (Exception e) {
      throw new RuntimeException("Cannot transform/serialize/write: "
        + validatorResponse, e);
    }
  }

  /**
   * Writes the single validation report.
   * (as transformed XML).
   *
   * @param validationResult results
   * @param writer XML validation result output
   * @param xslt source
   */
  public static void write(Validation validationResult, Writer writer, Source xslt) {
    ValidatorResponse resp = new ValidatorResponse();
    resp.addValidationResult(validationResult);
    write(resp, writer, xslt);
  }

  /**
   * Converts a validator response (contains one or
   * several validation results) into the document.
   *
   * @param validatorResponse results
   * @return results as XML DOM
   */
  public static Document asDocument(ValidatorResponse validatorResponse) {
    DOMResult domResult = marshal(validatorResponse);
    Document validation = (Document) domResult.getNode();
    return validation;
  }

  /**
   * Converts one validation result
   * into the DOM element.
   *
   * @param validationResult results
   * @return element (DOM)
   */
  public static Element asElement(Validation validationResult) {
    DOMResult domResult = marshal(validationResult);
    Element validation = (Element) domResult.getNode().getFirstChild();
    return validation;
  }


  protected static DOMResult marshal(Object obj) {
    DOMResult domResult = new DOMResult();
    try {
      getMarshaller().marshal(obj, domResult);
    } catch (Exception e) {
      throw new RuntimeException("Cannot serialize object: " + obj, e);
    }
    return domResult;
  }


  /**
   * Creates an error type with single error case.
   *
   * @param objectName name of the problematic model object
   * @param errorCode validator error code
   * @param ruleName validation rule name
   * @param profile validation profile
   * @param isFixed true/false - whether it's auto-fixed or not
   * @param msgArgs error message details
   * @return error type obj.
   */
  public ErrorType createError(String objectName, String errorCode,
                               String ruleName, String profile, boolean isFixed,
                               Object... msgArgs) {
    return createError(messageSource, locale, objectName, errorCode, ruleName, profile, isFixed, msgArgs);
  }

  /**
   * Creates an error type with single error case.
   * If the message source is null (e.g., in tests, when a rule
   * is checked outside the validator framework),
   *
   * @param messageSource error message source
   * @param locale locale
   * @param objectName name of the problematic model object
   * @param errorCode validator error code
   * @param ruleName validation rule name
   * @param profile validation profile
   * @param isFixed true/false - whether it's auto-fixed or not
   * @param msgArgs error message details
   * @return error type
   */
  public static ErrorType createError(
    MessageSource messageSource, Locale locale,
    String objectName, String errorCode,
    String ruleName, String profile, boolean isFixed,
    Object... msgArgs) {

    if (objectName == null) {
      objectName = "null";
      logger.warn("Creating an error " + errorCode + " for Null object!");
    }

    //get/use current behavior
    Behavior behavior = getRuleBehavior(ruleName, profile, messageSource);
    // new error object
    ErrorType error = new ErrorType(errorCode, behavior);

    // build human-friendly messages using default locale and property files (msg sources)
    String commonMsg = (messageSource != null)
      ? messageSource.getMessage(errorCode + ".default", new Object[]{}, "No description.", locale)
      : "No description.";
    error.setMessage(commonMsg);

    String msg = (messageSource != null)
      ? messageSource.getMessage(errorCode, msgArgs, StringUtils.join(msgArgs, "; "), locale).replaceAll("\r|\n+", " ")
      : StringUtils.join(msgArgs, "; ");

    // resolve/set BioPAX problem category
    String category = (messageSource != null)
      ? messageSource.getMessage(errorCode + ".category", null, Category.INFORMATION.name(), locale)
      : null;
    if (category != null)
      error.setCategory(Category.valueOf(category.trim().toUpperCase()));

    // add one error case
    ErrorCaseType errorCase = new ErrorCaseType(ruleName, objectName, msg);
    errorCase.setFixed(isFixed); //
    error.addErrorCase(errorCase);

    //done.
    return error;
  }


  /**
   * Gets the validator's home directory path.
   *
   * @return directory path
   * @throws IOException when it fails to get the filesystem path
   */
  public static String getHomeDir() throws IOException {
    Resource r = new FileSystemResource(ResourceUtils.getFile("classpath:"));
    return r.createRelative("..").getFile().getCanonicalPath();
  }


  /**
   * Gets rule's behavior (mode), to be used
   * when registering an error case reported by the rule.
   *
   * @param ruleName validation rule class name, e.g., org.biopax.validator.rules.MyRule
   * @param profile  validation profile name or null (default profile)
   * @return error level/mode
   * @see Behavior
   */
  public Behavior getRuleBehavior(String ruleName, String profile) {
    return getRuleBehavior(ruleName, profile, messageSource);
  }


  /**
   * Gets rule's behavior (mode) during unit testing
   * when messageSource can be null.
   *
   * @param ruleName      validation rule class name, e.g., org.biopax.validator.rules.MyRule
   * @param profile       validation profile name or null (default profile)
   * @param messageSource error message source (properties)
   * @return error level/mode
   * @see ValidatorUtils#getRuleBehavior(String, String)
   * @see ValidatorUtils#createError(MessageSource, Locale, String, String, String, String, boolean, Object...)
   */
  private static Behavior getRuleBehavior(String ruleName, String profile, MessageSource messageSource) {
    if (messageSource == null) return Behavior.ERROR;

    // get the default behavior value first
    String value = messageSource.getMessage(ruleName + ".behavior", null, "ERROR", Locale.getDefault());

    // - override from the profile, if set/available
    if (profile != null && !profile.isEmpty())
      value = messageSource.getMessage(ruleName + ".behavior." + profile, null, value, Locale.getDefault());

    return Behavior.valueOf(value.toUpperCase());
  }


  /**
   * @param ruleName validation rule class name, e.g., org.biopax.validator.rules.MyRule
   * @return rule description
   */
  public String getRuleDescription(String ruleName) {

    String tip = messageSource.getMessage(ruleName, null, "",
      Locale.getDefault());
    if (tip == null || "".equals(tip)) {
      tip = "description is not found in the messages.properties file";
    } else {
      tip = StringEscapeUtils.escapeHtml4(tip);
    }

    return tip;
  }

}
