package org.biopax.validator.web.service;

import org.biopax.validator.web.dto.Clue;
import org.biopax.validator.web.dto.Xref;

public interface Suggester {

  /**
   * Gets the preferred name for a synonym, non-standard or misspelled one.
   * @param xrefDb bio data/identifiers collection name or synonym
   * @return preferred name or null if none found
   */
  String getPrimaryDbName(String xrefDb);

  /**
   * Checks whether xrefs make sense and suggests preferred name, uri, namespace, etc.
   * @param xrefs (optional) xref elements to check and suggest values; if empty,
   * @return recommendation/validation result.
   */
  Clue xref(Xref[] xrefs);

  /**
   * Gets Identifiers.org URI (URL) for a bio entity record defined by db:id.
   * @param db xref.db value (bio data/identifiers collection name)
   * @param id xref.id value, an identifier, e.g., MI:0444, BMP2
   * @return bio entity URI
   * @throws IllegalArgumentException when db or id is invalid
   */
  String xrefDbIdToUri(String db, String id);

}
