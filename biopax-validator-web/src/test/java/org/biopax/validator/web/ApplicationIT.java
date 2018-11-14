package org.biopax.validator.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIT {

  @Autowired
  private TestRestTemplate template;

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

  //TODO: add tests: check a syntax rule, check a biopax model from URL or local file
}
