package org.biopax.validator.web.service;

import org.biopax.paxtools.normalizer.Normalizer;
import org.biopax.validator.api.beans.Behavior;
import org.biopax.validator.api.beans.Validation;
import org.springframework.core.io.Resource;

import java.io.IOException;

public interface ValidatorAdapter {
  /**
   * Parses and checks BioPAX data from the given resource
   * and returns the validation report.
   *
   * @param data input biopax model source
   * @param maxErrors optional, if greater than 0, abort once the number of critical errors exceeds the threshold
   * @param fixErrors optional, when true, some validator rules can auto-fix the model (issues still get reported)
   * @param level optional, if 'ERROR' then warnings are ignored; 'WARNING' - both errors and warnings are reported.
   * @param profile optional, validation rules' behavior settings profile, e.g., 'notstrict', 'default'
   * @param normalizer optional, pre-configured biopax normalizer
   * @return validation report
   * @throws IOException when fails to read the data
   */
  Validation validate(Resource data, int maxErrors, boolean fixErrors,
                      Behavior level, String profile,
                      Normalizer normalizer) throws IOException;

  /**
   * @return Validator results XML schema.
   */
  String getSchema();
}
