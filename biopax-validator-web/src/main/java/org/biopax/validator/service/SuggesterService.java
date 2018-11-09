package org.biopax.validator.service;

import org.biopax.paxtools.normalizer.MiriamLink;
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

  /**
   * Gets the preferred name for a synonym, non-standard or misspelled one.
   * @return preferred name or null if none found
   */
  @Override
  public String getPrimaryDbName(String xrefDb) {
    return xrefUtils.getPrimaryDbName(xrefDb);
  }

  /**
   * @throws IllegalArgumentException when xrefDb is invalid or xrefId does not match the pattern.
   */
  @Override
  public String getIdentifiersOrgUri(String xrefDb, String xrefId) {
    return MiriamLink.getIdentifiersOrgURI(xrefDb, xrefId);
  }

}
