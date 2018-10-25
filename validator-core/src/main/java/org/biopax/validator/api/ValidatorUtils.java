package org.biopax.validator.api;

import org.apache.commons.lang3.StringUtils;
import org.biopax.validator.api.beans.*;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;
import java.util.Locale;

public interface ValidatorUtils {

  /**
   * Gets the validation results (xml) marshaller.
   *
   * @return jaxb marshaller
   */
  static Marshaller getMarshaller() {
    try {
      Marshaller marshaller = BiopaxValidatorUtils.jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      return marshaller;
    } catch (JAXBException e) {
      throw new RuntimeException("Failed to create Marshaller", e);
    }
  }

  /**
   * Gets the validation results (xml) Unmarshaller.
   *
   * @return unmarshaller
   */
  static Unmarshaller getUnmarshaller() {
    try {
      return BiopaxValidatorUtils.jaxbContext.createUnmarshaller();
    } catch (JAXBException e) {
      throw new RuntimeException("Failed to create Unmarshaller", e);
    }
  }

  /**
   * Writes the multiple results report.
   * (as transformed XML).
   *
   * @param validatorResponse results
   * @param writer output
   * @param xslt source
   */
  static void write(ValidatorResponse validatorResponse, Writer writer, Source xslt) {
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
  static void write(Validation validationResult, Writer writer, Source xslt) {
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
  static Document asDocument(ValidatorResponse validatorResponse) {
    DOMResult domResult = BiopaxValidatorUtils.marshal(validatorResponse);
    Document validation = (Document) domResult.getNode();
    return validation;
  }

  void setLocale(Locale locale);

  Locale getLocale();

  /**
   * Gets current max number of errors to report.
   *
   * @return maximal no. errors to be collected (to abort and return the results)
   */
  int getMaxErrors();

  /**
   * Sets current max number of errors to report.
   *
   * @param max maximal no. errors to be collected (to abort and return the results)
   */
  void setMaxErrors(int max);

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
  ErrorType createError(String objectName, String errorCode,
                        String ruleName, String profile, boolean isFixed,
                        Object... msgArgs);

  /**
   * Gets rule's behavior (mode), to be used
   * when registering an error case reported by the rule.
   *
   * @param ruleName validation rule class name, e.g., org.biopax.validator.rules.MyRule
   * @param profile  validation profile name or null (default profile)
   * @return error level
   * @see Behavior
   */
  Behavior getRuleBehavior(String ruleName, String profile);

  /**
   * @param ruleName validation rule class name, e.g., org.biopax.validator.rules.MyRule
   * @return rule description
   */
  String getRuleDescription(String ruleName);
}
