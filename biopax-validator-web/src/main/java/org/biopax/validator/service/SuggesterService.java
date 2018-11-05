package org.biopax.validator.service;

import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class SuggesterService implements Suggester{
  private XrefHelper xrefHelper;

  @Autowired
  public SuggesterService(XrefHelper xrefHelper) {
    this.xrefHelper = xrefHelper;
  }


}
