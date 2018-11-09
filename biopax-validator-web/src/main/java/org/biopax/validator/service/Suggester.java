package org.biopax.validator.service;

public interface Suggester {

  /**
   * Gets the preferred name of the MIRIAM collection.
   * @param xrefDb
   * @return primary name if exists
   */
  String getPrimaryDbName(String xrefDb);

  /**
   * Gets Identifiers.org URI (URL) for a bio entity record defined by db:id.
   * @param xrefDb bio entity db/collection name
   * @param xrefId bio entity identifier
   * @return Identifiers.org standard URI for the bio resource
   */
  String getIdentifiersOrgUri(String xrefDb, String xrefId);

}
