package org.biopax.validator.ws;

import org.biopax.validator.service.Suggester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class SuggesterController {

  @Autowired
  private Suggester service;

  @GetMapping(value = "/Xref/{db}/{id}/", produces = APPLICATION_JSON_UTF8_VALUE)
  public String xrefsDbId(@PathVariable String db, @PathVariable String id,
                          HttpServletResponse response) throws IOException {
    String uri = null;
    try {
      uri = service.xrefDbIdToUri(db, id);
    } catch (IllegalArgumentException e) {
      response.sendError(400, e.toString());
    }

    return uri;
  }

  //TODO: return {db:'ec',dbOk:true,id:'1.1.1.1',idOk:true,uri:'http://identifiers.org/ec-code/1.1.1.1',name:'Enzyme Nomenclature'}

}