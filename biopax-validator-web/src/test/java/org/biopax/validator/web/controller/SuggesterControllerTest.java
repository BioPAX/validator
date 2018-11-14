package org.biopax.validator.web.controller;

import org.biopax.validator.web.dto.Clue;
import org.biopax.validator.web.service.Suggester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

@RunWith(SpringRunner.class)
@WebMvcTest(SuggesterController.class)
@AutoConfigureRestDocs//(outputDir = "target/snippets")
public class SuggesterControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  Suggester service;

  private static Clue aClue = new Clue("xref");

  @Before
  public void before() {
    // valid synonym and valid id
    String anXrefId = "6.1.1.5";
    given(service.xrefDbIdToUri("ec", anXrefId))
      .willReturn("http://identifiers.org/ec-code/" + anXrefId);

    // test typo (fixable) name
    given(service.xrefDbIdToUri("ec_code", anXrefId))
      .willReturn("http://identifiers.org/ec-code/" + anXrefId);
    given(service.xrefDbIdToUri("Enzyme Nomenclature", anXrefId))
      .willReturn("http://identifiers.org/ec-code/" + anXrefId);

    // bad not-fixable name
    given(service.xrefDbIdToUri("foo", anXrefId))
      .willThrow(new IllegalArgumentException("Datatype not found"));
    // bad id
    given(service.xrefDbIdToUri("ec", "foo"))
      .willThrow(new IllegalArgumentException("does not matter"));

    given(service.xref(null)).willReturn(aClue);
  }

  @Test
  public void shouldReturnUriOrErrorXrefDbIdToUri() throws Exception {
    mvc.perform(get("/xref/ec/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
      .andExpect(content().string(equalTo("http://identifiers.org/ec-code/6.1.1.5")))
      .andDo(document("one-xref"));

    mvc.perform(get("/xref/ec_code/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().string(equalTo("http://identifiers.org/ec-code/6.1.1.5")));

    mvc.perform(get("/xref/foo/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError());

    //invalid id
    mvc.perform(get("/xref/ec/foo/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError());
  }

  @Test
  public void shouldReturnClueXref() throws Exception {
    mvc.perform(post("/xref").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
      .andExpect(content().json("{'info':'xref','values':[]}"))
      .andDo(document("xref"));
  }

}