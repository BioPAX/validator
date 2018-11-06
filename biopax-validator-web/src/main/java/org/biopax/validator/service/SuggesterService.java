package org.biopax.validator.service;

import org.biopax.validator.CvFactory;
import org.biopax.validator.XrefUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuggesterService implements Suggester {
  private XrefUtils xrefUtils;
  private CvFactory cvFactory; //TODO: add methods that call CvFactory

  @Autowired
  public SuggesterService(XrefUtils xrefUtils, CvFactory cvFactory) {
    this.xrefUtils = xrefUtils;
    this.cvFactory = cvFactory;
  }

  @Override
  public String getPrimaryDbName(String xrefDb) {
    return xrefUtils.getPrimaryDbName(xrefDb);
  }

}
