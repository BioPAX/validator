package org.biopax.validator.web.controller;

import org.biopax.validator.web.dto.Clue;
import org.biopax.validator.web.dto.Xref;
import org.biopax.validator.web.service.Suggester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
//import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {SuggesterController.class})
@AutoConfigureRestDocs
public class SuggesterControllerTest {

  @TestConfiguration
  static class ResultHandlerConfiguration {
    @Bean
    public RestDocumentationResultHandler restDocumentation() {
      return MockMvcRestDocumentation.document("{method-name}");
    }
  }

  @Autowired
  private MockMvc mvc;

  @MockBean
  private Suggester service;

  @Autowired
  private ObjectMapper mapper;

  private static Clue aClue = new Clue("some info");

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
  }

  @Test
  public void shouldReturnUri() throws Exception {
    mvc.perform(get("/xref/ec/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
      .andExpect(content().string(equalTo("http://identifiers.org/ec-code/6.1.1.5")));

    mvc.perform(get("/xref/ec_code/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().string(equalTo("http://identifiers.org/ec-code/6.1.1.5")));
  }

  @Test
  public void shouldReturnDbError4xx() throws Exception {
    mvc.perform(get("/xref/foo/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().is4xxClientError());
  }

  @Test
  public void shouldReturnIdError4xx() throws Exception {
    mvc.perform(get("/xref/ec/foo/").accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().is4xxClientError());
  }

  @Test
  public void shouldReturnInfoWhenEmptyBody() throws Exception {
    given(service.xref(null)).willThrow(new IllegalArgumentException()); //no body
    mvc.perform(post("/xref").accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().is4xxClientError());

    given(service.xref(new Xref[]{})).willThrow(new IllegalArgumentException()); //no body
    mvc.perform(post("/xref").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError());
  }

  @Test
  public void shouldReturnCheckedXrefs() throws Exception {
    Xref x = new Xref();
    x.setDb("ec"); //ok
    x.setId("1.1.1.1"); //ok
    Xref y = new Xref();
    y.setDb("ec"); // ok
    y.setId("foo"); //invalid
    Xref xx = new Xref();
    xx.setDb("ec"); //ok
    xx.setNamespace("ec-code");
    xx.setDbOk(true);
    xx.setId("1.1.1.1"); //ok
    xx.setIdOk(true);
    Xref yy = new Xref();
    yy.setDb("ec"); // ok
    yy.setDbOk(true);
    yy.setNamespace("ec-code");
    yy.setId("foo"); //invalid
    Clue xyClue = new Clue("some info");
    xyClue.addValue(xx);
    xyClue.addValue(yy);

    given(service.xref(ArgumentMatchers.any(Xref[].class))).willReturn(xyClue);

    String inpContent = mapper.writeValueAsString(new Xref[]{x,y});
    String outContent = mapper.writeValueAsString(xyClue);

    String ret = mvc.perform(post("/xref")
          .accept(MediaType.APPLICATION_JSON)
          .contentType(MediaType.APPLICATION_JSON)
          .content(inpContent))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().json(outContent))
      .andReturn().getResponse().getContentAsString();
  }

}