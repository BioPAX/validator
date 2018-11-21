package org.biopax.validator.web;

import org.biopax.validator.BiopaxIdentifier;
import org.biopax.validator.api.Validator;
import org.biopax.validator.api.beans.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIT {

  @Autowired
  private TestRestTemplate template;

  @Autowired
  private Validator biopaxValidator;

  @Test
  public void testGetTypes() {
    String result = template.getForObject(
      "/xref/{db}/{id}/", String.class, "enzyme nomenclature", "6.1.1.5");
    assertNotNull(result);
    assertEquals("http://identifiers.org/ec-code/6.1.1.5", result);
  }

  @Test
  public void testGetSchema() {
    String result = template.getForObject("/schema", String.class);
    assertNotNull(result);
    assertTrue(result.contains("element name=\"validatorResponse\""));
  }

  //test that LTW is enabled and it can catch and report unknown.property syntax error
  @Test
  public void testUnknownProperty() throws IOException {
    Validation validation = new Validation(new BiopaxIdentifier());
    biopaxValidator.importModel(validation, new DefaultResourceLoader()
      .getResource("classpath:testSyntaxErrors.xml").getInputStream());
    biopaxValidator.getResults().clear(); // clean after itself
    assertEquals(1, validation.countErrors(null, null, "unknown.property",
      null, false, false));
  }

}
