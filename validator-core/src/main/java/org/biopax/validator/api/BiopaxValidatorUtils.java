package org.biopax.validator.api;


import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.api.beans.*;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.dom.DOMResult;
import java.util.Locale;

/**
 * Validator utilities.
 *
 * This is injected into other beans, keeps several global settings and objects,
 * e.g., marshaller, and also provides static service methods to register,
 * merge, do OXM, and resolve validation errors to human-readable verbose messages.
 *
 * @author rodche
 */
@Configurable
@Service
public class BiopaxValidatorUtils implements ValidatorUtils {
  private static final Log logger = LogFactory.getLog(ValidatorUtils.class);

  private Locale locale;
  private MessageSource messageSource;
  private int maxErrors = Integer.MAX_VALUE;

  static JAXBContext jaxbContext;

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



  public BiopaxValidatorUtils() {
    this.locale = Locale.getDefault();
  }

  public BiopaxValidatorUtils(MessageSource messageSource) {
    super();
    this.messageSource = messageSource;
  }

  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }

  @Override
  public int getMaxErrors() {
    return maxErrors;
  }

  @Override
  public void setMaxErrors(int max) {
    maxErrors = max;
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

  @Override
  public ErrorType createError(String objectName, String errorCode,
                               String ruleName, String profile, boolean isFixed, Object... msgArgs)
  {
    if (objectName == null) {
      objectName = "null";
      BiopaxValidatorUtils.logger.warn("Creating an error " + errorCode + " for Null object!");
    }

    //get/use current behavior
    Behavior behavior = BiopaxValidatorUtils.getRuleBehavior(ruleName, profile, messageSource);
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

    return error;
  }


  /**
   * Creates an error type with single error case.
   * If the message source is null (e.g., in tests, when a rule
   * is checked outside the validator framework),
   *
   * @param objectName name of the problematic model object
   * @param errorCode validator error code
   * @param ruleName validation rule name
   * @param profile validation profile
   * @param isFixed true/false - whether it's auto-fixed or not
   * @param msgArgs error message details
   * @return error type
   */
  public static ErrorType createError(String objectName, String errorCode,
                        String ruleName, String profile, boolean isFixed, Object... msgArgs)
  {
    if (objectName == null) {
      objectName = "null";
      logger.warn("Creating an error " + errorCode + " for Null object!");
    }
    Behavior behavior = BiopaxValidatorUtils.getRuleBehavior(ruleName, profile, null);
    ErrorType error = new ErrorType(errorCode, behavior);
    error.setMessage("No description.");
    String msg = StringUtils.join(msgArgs, "; ");
    ErrorCaseType errorCase = new ErrorCaseType(ruleName, objectName, msg);
    errorCase.setFixed(isFixed);
    error.addErrorCase(errorCase);
    return error;
  }


  @Override
  public Behavior getRuleBehavior(String ruleName, String profile) {
    return getRuleBehavior(ruleName, profile, messageSource);
  }


  /**
   * Gets rule's behavior (mode) during unit testing when messageSource can be null.
   *
   * @param ruleName      validation rule class name, e.g., org.biopax.validator.rules.MyRule
   * @param profile       validation profile name or null (default profile)
   * @param messageSource error message source (properties)
   * @return error level
   * @see BiopaxValidatorUtils#getRuleBehavior(String, String)
   * @see BiopaxValidatorUtils#createError(String, String, String, String, boolean, Object...)
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


  @Override
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
