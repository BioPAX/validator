package org.biopax.validator.ws;

import org.biopax.validator.service.Suggester;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(SuggesterController.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class SuggesterControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  Suggester service;

  @Before
  public void before() {
    //valid synonym and valid id
    String anXrefId = "6.1.1.5";
    given(service.getPrimaryDbName("ec")).willReturn("Enzyme Nomenclature");
    given(service.getIdentifiersOrgUri("ec", anXrefId))
      .willReturn("http://identifiers.org/ec-code/" + anXrefId);

    //a test typo (fixable) name
    given(service.getPrimaryDbName("ec_code")).willReturn("Enzyme Nomenclature");
    given(service.getIdentifiersOrgUri("ec_code", anXrefId))
      .willThrow(new IllegalArgumentException("Datatype not found"));
    given(service.getIdentifiersOrgUri("Enzyme Nomenclature", anXrefId))
      .willReturn("http://identifiers.org/ec-code/" + anXrefId);

    //a bad not-fixable name
    given(service.getPrimaryDbName("foo")).willReturn(null);
    given(service.getIdentifiersOrgUri("foo", anXrefId))
      .willThrow(new IllegalArgumentException("Datatype not found"));

    given(service.getIdentifiersOrgUri("ec", "foo"))
      .willThrow(new IllegalArgumentException("does not matter"));
  }

  @Test
  public void shouldReturnUriOrErrorByXrefDbAndId() throws Exception {
    mvc.perform(get("/Xref/ec/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
      .andExpect(content().string(equalTo("http://identifiers.org/ec-code/6.1.1.5")));

    mvc.perform(get("/Xref/ec_code/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().string(equalTo("http://identifiers.org/ec-code/6.1.1.5")));

    mvc.perform(get("/Xref/foo/6.1.1.5/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError());

    //invalid id
    mvc.perform(get("/Xref/ec/foo/").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError());
  }
}