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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

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

}
