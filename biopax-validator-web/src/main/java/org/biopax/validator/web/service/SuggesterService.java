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
    Clue clue;

    if(xrefs!=null && xrefs.length>0) {
      clue = new Clue("Checked xrefs; suggested standard name and URI for valid ones.");
      Arrays.stream(xrefs).forEach(xref -> clue.addValue(suggest(xref.getDb(), xref.getId())));
    } else {
      clue = new Clue("A list of recommended data collection names and ID " +
        "patterns for BioPAX Xref.db and Xref.id.");
      Arrays.stream(MiriamLink.getDataTypesId())
        .map(MiriamLink::getDatatype).forEach(datatype -> {
          Xref x = new Xref();
          x.setPreferredDb(datatype.getName());
          x.setNamespace(datatype.getNamespace());
          x.setId(datatype.getPattern());
          x.setUri(String.format("http://identifiers.org/%s/", x.getNamespace()));
          clue.addValue(x);
      });
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
    //check (can auto-correct some misspellings, such as 'Entrez_Gene')
    String name = getPrimaryDbName(db);
    if (name == null) {
      x.setDbOk(false);
    } else {
      x.setDbOk(true);
      x.setPreferredDb(name);
      x.setNamespace(MiriamLink.getDatatype(name).getNamespace());
      try { //now with valid name
        String uri = MiriamLink.getIdentifiersOrgURI(name, id);
        x.setUri(uri);
        x.setIdOk(true);
      } catch (IllegalArgumentException ex) {//pattern failed
        x.setIdOk(false);
      }
    }

    return x;
  }
}
