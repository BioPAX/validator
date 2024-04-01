package org.biopax.validator.web.service;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.normalizer.Resolver;
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
  private CvFactory cvFactory; //TODO: use this factory to implement controlled vocabulary related suggestions...

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
    Clue clue;

    if(xrefs!=null && xrefs.length>0) {
      clue = new Clue("Checked xrefs; suggested standard name and URI for valid ones.");
      Arrays.stream(xrefs).forEach(xref -> clue.addValue(suggest(xref.getDb(), xref.getId())));
    } else {
      throw new IllegalArgumentException("Input array is empty or null.");
    }

    return clue;
  }

  @Override
  public String xrefDbIdToUri(String db, String id) {
    Xref checked = suggest(db, id);
    if (!checked.isDbOk()) {
      throw new IllegalArgumentException("Cannot recognize DB: " + db);
    } else if(!checked.isIdOk()) {
      throw new IllegalArgumentException(String.format(
        "DB:'%s'('%s'), ID:'%s' failed", db, checked.getPreferredDb(), id));
    }

    return checked.getUri();
  }

  private Xref suggest(String db, String id) {
    Xref x = new Xref();
    x.setDb(db);
    x.setId(id);
    //check (can autocorrect some misspellings, such as 'Entrez_Gene')
    String name = getPrimaryDbName(db);
    if (name == null) {
      x.setDbOk(false);
    } else {
      x.setDbOk(true);
      x.setPreferredDb(name);
      x.setNamespace(xrefUtils.getPrefix(name));
      //try to resolve it now with valid name
      String curie = Resolver.getCURIE(name, id);
      if(StringUtils.isNotBlank(curie)) {
        String uri = "http://bioregistry.io/" + curie;
        //TODO: it's not a good xref URI except for PublicationXref (but PC app-ui webapp depends on this...)
        //Use CURIE for a UnificationXref and e.g. RX_<rel_type>_<curie> - for RelationshipXref... to avoid semantic mess/miss...
        //Such absolute standard URI better suits EntityReference or CV.
        x.setUri(uri);
        x.setIdOk(true);
      } else { //regex failed id
        x.setIdOk(false);
      }
    }
    return x;
  }
}
