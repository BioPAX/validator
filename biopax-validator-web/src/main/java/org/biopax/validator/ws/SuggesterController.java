package org.biopax.validator.ws;

import org.biopax.paxtools.normalizer.MiriamLink;
import org.biopax.validator.service.Suggester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
public class SuggesterController {

  @Autowired
  private Suggester service;

  //TODO: return e.g. {db:'ec',dbOk:true,id:'1.1.1.1',idOk:true,uri:'http://identifiers.org/ec-code/1.1.1.1',name:'Enzyme Nomenclature'}
  @RequestMapping(value = "/Xref/{db}/{id}/", produces = APPLICATION_JSON_VALUE)
  public String xrefsDbId(@PathVariable String db, @PathVariable String id,
                          HttpServletResponse response) throws IOException {
    String uri = null;
    try {
      uri = MiriamLink.getIdentifiersOrgURI(db, id);
    } catch (IllegalArgumentException e) {
      if (e.toString().contains("Datatype")) { //honestly, a hack
        //guess, auto-correct (supports some (mis)spellings, such as 'Entrez_Gene')
        String pref = service.getPrimaryDbName(db);
        if (pref == null) {
          //'db' did not mach any data collection name in MIRIAM even despite some auto-correction
          response.sendError(400, "Cannot recognize db: " + db);
        } else {
          try { //now with valid name
            uri = MiriamLink.getIdentifiersOrgURI(pref, id);
          } catch (IllegalArgumentException ex) {//id pattern failed
            response.sendError(400, String.format(
              "Incorrect: '%s' was replaced with '%s'; then %s", db, pref, ex.toString()));
          }
        }
      } else {
        //id failed, or smth. else
        response.sendError(400, e.toString());
      }
    }
    return uri;
  }

}