package org.biopax.validator.web;

import org.biopax.validator.BiopaxIdentifier;
import org.biopax.validator.api.Validator;
import org.biopax.validator.api.beans.Validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIT {

  @Autowired
  private TestRestTemplate template;

  @Autowired
  private Validator biopaxValidator;

  @Test
  public void getSchema() {
    String result = template.getForObject("/schema", String.class);
    assertNotNull(result);
    assertTrue(result.contains("element name=\"validatorResponse\""));
  }

  //test that LTW is enabled, and it can catch and report unknown.property syntax error
  @Test
  public void unknownProperty() throws IOException {
    Validation validation = new Validation(new BiopaxIdentifier());
    biopaxValidator.importModel(validation, new DefaultResourceLoader()
      .getResource("classpath:testSyntaxErrors.xml").getInputStream());
    biopaxValidator.getResults().clear(); // clean after itself
    assertEquals(1, validation.countErrors(null, null, "unknown.property",
      null, false, false));
  }

  @Test
  public void xrefHttpGet() {
    String result = template.getForObject(
        "/xref/{db}/{id}/", String.class, "enzyme nomenclature", "6.1.1.5");
    assertNotNull(result);
    assertEquals("http://bioregistry.io/eccode:6.1.1.5", result);
  }

  @Test
  public void xrefHttpPost() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> requestEntity = new HttpEntity<>(headers); //empty body
    ResponseEntity<String> responseEntity = template.exchange("/xref", HttpMethod.POST, requestEntity, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    HttpEntity<String> httpEntity = new HttpEntity<>(
        //1st is wrong id; 2nd is ok; "ec" or "ec-code" must be identified as synonyms for "eccode"
        "[{\"db\":\"ec\",\"id\":\"foo\"}, {\"db\":\"ec-code\",\"id\":\"1.1.1.1\"}]", headers);
    String result = template.postForObject("/xref", httpEntity, String.class);

    assertAll(
        () -> assertNotNull(result),
        () -> assertTrue(result.startsWith("{\"info\":\"Checked")),
        () -> assertTrue(result.contains("eccode") && result.contains("enzyme commission code"))
    );
  }

}
