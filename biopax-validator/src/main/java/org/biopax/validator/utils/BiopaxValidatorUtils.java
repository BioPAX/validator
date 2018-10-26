package org.biopax.validator.utils;


import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.beans.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

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


  public BiopaxValidatorUtils() {
    this.locale = Locale.getDefault();
  }


  @Autowired
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }
  public MessageSource getMessageSource() {
    return messageSource;
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


  @Override
  public ErrorType createError(String objectName, String errorCode,
                               String ruleName, String profile, boolean isFixed, Object... msgArgs)
  {
    if (objectName == null) {
      objectName = "null";
      logger.warn("Creating an error " + errorCode + " for Null object!");
    }

    //get/use current behavior
    Behavior behavior = getRuleBehavior(ruleName, profile);
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

  @Override
  public Behavior getRuleBehavior(String ruleName, String profile) {
    if (messageSource == null) return Behavior.ERROR;

    // get the default behavior value first
    String value = messageSource.getMessage(ruleName + ".behavior", null,
      "ERROR", Locale.getDefault());

    // override from the profile
    if (profile != null && !profile.isEmpty())
      value = messageSource.getMessage(ruleName + ".behavior." + profile,
        null, value, Locale.getDefault());

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
