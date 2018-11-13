package org.biopax.validator.service;

import org.biopax.paxtools.model.level3.Xref;
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

  @Override
  public String getPrimaryDbName(String xrefDb) {
    String name = xrefUtils.getPrimaryDbName(xrefDb);
    return (name != null) ? name.toLowerCase() : null;
  }

  @Override
  public Clue xref(Xref... x) {
    throw new UnsupportedOperationException("Not implemented.");  //TODO implement
  }

  @Override
  public String xrefDbIdToUri(String db, String id) {
    String uri;

    try {
      uri = MiriamLink.getIdentifiersOrgURI(db, id);
    } catch (IllegalArgumentException e) {
      if (e.toString().contains("Datatype")) { //honestly, a hack
        //guess, auto-correct (supports some (mis)spellings, such as 'Entrez_Gene')
        String pref = getPrimaryDbName(db);
        if (pref == null) {
          //'db' did not mach any data collection name in MIRIAM even despite some auto-correction
          throw new IllegalArgumentException("Cannot recognize (or auto-fix) db: " + db);
        } else {
          try { //now with valid name
            uri = MiriamLink.getIdentifiersOrgURI(pref, id);
          } catch (IllegalArgumentException ex) {//pattern failed
            throw new IllegalArgumentException(String.format(
              "Although '%s' was recognized as '%s', it failed with: %s", db, pref, ex.toString()));
          }
        }
      } else {
        //id failed, or smth. else
        throw new IllegalArgumentException(e);
      }
    }

    return uri;
  }


}
