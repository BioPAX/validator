package org.biopax.validator.web.service;

import org.biopax.paxtools.normalizer.MiriamLink;
import org.biopax.validator.web.dto.Clue;
import org.biopax.validator.web.dto.Xref;
import org.biopax.validator.CvFactory;
import org.biopax.validator.XrefUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;


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
  public Clue xref(Xref[] xrefs) {

    Clue clue = new Clue("xref");

    if(xrefs!=null && xrefs.length>0) {
      //TODO check id, suggest prefered db and uri for each xref (replace the stub below)
      Arrays.stream(xrefs).forEachOrdered(x -> clue.getValues().add(x.getDb()));
    } else {
      //TODO add info about all recommended db names, corresp. id patterns, uris, etc.
    }

    return clue;
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
          throw new IllegalArgumentException("Cannot recognize DB: " + db);
        } else {
          try { //now with valid name
            uri = MiriamLink.getIdentifiersOrgURI(pref, id);
          } catch (IllegalArgumentException ex) {//pattern failed
            throw new IllegalArgumentException(String.format(
              "DB:'%s'('%s'), ID:'%s' failed : %s", db, pref, id, ex.toString()));
          }
        }
      } else {
        //id failed, or smth. else
        throw new IllegalArgumentException(String.format(
          "DB:'%s', ID:'%s' failed : %s", db, id, e.toString()));
      }
    }

    return uri;
  }


}
