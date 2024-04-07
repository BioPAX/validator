package org.biopax.validator.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.biopax.validator.web.dto.Clue;
import org.biopax.validator.web.service.Suggester;
import org.biopax.validator.web.dto.Xref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.MediaType.*;

@RestController
public class SuggesterController {

  @Autowired
  private Suggester service;

  @GetMapping(value = "/xref/{db}/{id}/", produces = TEXT_PLAIN_VALUE)
  public String xrefDbId(@PathVariable String db, @PathVariable String id,
                          HttpServletResponse response) throws IOException {
    String uri = null;
    try {
      uri = service.xrefDbIdToUri(db, id);
    } catch (IllegalArgumentException e) {
      response.sendError(400, e.toString());
    }
    return uri;
  }

  @PostMapping(value = "/xref", produces = APPLICATION_JSON_VALUE)
  public Clue xref(@RequestBody Xref[] xrefs, HttpServletResponse response)
    throws IOException {

    try {
      return service.xref(xrefs);
    } catch (IllegalArgumentException e) {
      response.sendError(400, e.toString());
    }

    return null;
  }

}