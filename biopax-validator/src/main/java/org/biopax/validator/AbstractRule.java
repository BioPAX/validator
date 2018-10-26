package org.biopax.validator;

import org.biopax.validator.api.BaseRule;
import org.biopax.validator.api.ValidatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * A BioPAX validation rule.
 *
 * @param <T> a type the rule can validate.
 */
@Configurable
public abstract class AbstractRule<T> extends BaseRule<T> {

  @Autowired
  public void setUtils(ValidatorUtils utils) {
    this.utils = utils;
  }

}