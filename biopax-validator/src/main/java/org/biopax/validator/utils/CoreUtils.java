package org.biopax.validator.utils;


import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.beans.*;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * Validation rules and errors utilities.
 *
 * This is injected into other beans, keeps several global settings and objects,
 * e.g., marshaller, and also provides static service methods to register,
 * merge, do OXM, and resolve validation errors to human-readable verbose messages.
 *
 * @author rodche
 */
@Configurable
public class CoreUtils implements ValidatorUtils {
  private static final Logger logger = LoggerFactory.getLogger(CoreUtils.class);
  public static final int DEFAULT_MAX_ERRORS = 10000;

  private Locale locale;
  private MessageSource messageSource;
  private int maxErrors;

  public CoreUtils() {
    this.maxErrors = DEFAULT_MAX_ERRORS;
    this.locale = LocaleContextHolder.getLocale();
  }

  public void setMessageSource(MessageSource rulesMessageSource) {
    this.messageSource = rulesMessageSource;
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

  public int getMaxErrors() {
    return maxErrors;
  }
  public void setMaxErrors(int max) {
    maxErrors = max;
  }

  public ErrorType createError(String objectName, String errorCode,
                               String ruleName, String profile,
                               boolean isFixed, Object... msgArgs)
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

    String msg =
      (messageSource != null)
        ? messageSource.getMessage(errorCode, msgArgs, StringUtils.join(msgArgs, "; "), locale)
            .replaceAll("\r|\n+", " ")
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

  public Behavior getRuleBehavior(String ruleName, String profile) {
    if (messageSource == null) return Behavior.ERROR;

    //using the default locale for values below (those ain't info messages)
    final Locale defaultLocale = Locale.getDefault();

    // get the default behavior value first
    String value = messageSource.getMessage(ruleName + ".behavior", null,
      "ERROR", defaultLocale);

    // override from the profile
    if (profile != null && !profile.isEmpty())
      value = messageSource.getMessage(ruleName + ".behavior." + profile,
        null, value,  defaultLocale);

    return Behavior.valueOf(value.toUpperCase());
  }

  public String getRuleDescription(String ruleName) {
    //the default locale is used to get the ruleName intentionally
    String tip = messageSource.getMessage(ruleName, null, "", Locale.getDefault());
    if (tip == null || "".equals(tip)) {
      tip = "description is not found in the messages.properties file";
    } else {
      tip = StringEscapeUtils.escapeHtml4(tip);
    }

    return tip;
  }

}
