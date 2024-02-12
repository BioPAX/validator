package org.biopax.validator;

import java.util.List;

public interface XrefUtils {
  /**
   * Removes tail spaces and converts to upper case
   *
   * @param name original name
   * @return normalized name
   */
  String dbName(String name);

  /**
   * Gets database name and its variants.
   * The first in the list is the recommended one.
   *
   * @param name case insensitive
   * @return set of equivalent database names
   */
  List<String> getSynonymsForDbName(String name);

  /**
   * Gets the primary name for the DB.
   * It returns NULL for "unknown" database name.
   *
   * @param name case-insensitive name (of a bio ID type/resource) name
   * @return preferred name (upper case)
   */
  String getPrimaryDbName(String name);

  /**
   * Gets the "prefix" (curated short name) for the DB (collection of IDs).
   * It returns NULL for "unknown" database name.
   *
   * @param name case-insensitive name (of a bio ID type/resource) name
   * @return prefix (lower case)
   */
  String getPrefix(String name);

  /**
   * Checks whether the ID format is valid for the database.
   * Always use {@link #canCheckIdFormatIn(String)} before this method,
   * because it may throw an exception if you do not.
   *
   * @param db case insensitive
   * @param id
   * @return 'false' if matcher fails, 'true' otherwise
   * @throws NullPointerException when no pattern available
   */
  boolean checkIdFormat(String db, String id);

  /**
   * @param name a database name (used in xrefs), case-insensitive
   * @return true if it's possible to check the format.
   */
  boolean canCheckIdFormatIn(String name);

  /**
   * Gets the regular expression corresponding
   * to the database.
   *
   * @param db a database name, case insensitive
   * @return regular expression to check its ID
   */
  String getRegexpString(String db);

  /**
   * Checks whether the db name is known (configured)
   * misspellings or unofficial name,
   * which the Validator can recognize, report
   * warning, and replace with official names; and which
   * otherwise, using MI or Miriam, cannot be resolved.
   *
   * @param db case insensitive
   * @return true or false
   */
  boolean isUnofficialOrMisspelledDbName(String db);

}
