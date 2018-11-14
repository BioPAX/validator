package org.biopax.validator.web.controller;

import org.biopax.validator.web.dto.Clue;
import org.biopax.validator.web.service.Suggester;
import org.biopax.validator.web.dto.Xref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class SuggesterController {

  @Autowired
  private Suggester service;

  @GetMapping(value = "/xref/{db}/{id}/", produces = APPLICATION_JSON_UTF8_VALUE)
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

  @PostMapping(value = "/xref", produces = APPLICATION_JSON_UTF8_VALUE)
  public Clue xref(@RequestBody(required = false) Xref[] xrefs) {
    return service.xref(xrefs);
  }

}