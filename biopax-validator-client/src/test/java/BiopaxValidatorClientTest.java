/*
 *
 */
import java.io.*;
import java.util.List;

import jakarta.xml.bind.JAXBException;

import org.biopax.validator.BiopaxValidatorClient;
import org.biopax.validator.BiopaxValidatorClient.RetFormat;
import org.biopax.validator.jaxb.ValidatorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/*
Uncomment when biopax.org/biopax-validator/ is available,
or run the web app locally (see readme) and this test with -Dbiopax.validator.url=http://localhost:8080/check JVM opt.
or use client.setUrl in the test case below.
*/
@Disabled
public class BiopaxValidatorClientTest {

	@Test
	public void testClientHtml() throws IOException {
		BiopaxValidatorClient client = new BiopaxValidatorClient();
		List<File> files = List.of(
				new File(getClass().getResource(File.separator + "testBiopaxElementIdRule.owl").getFile()),
				new File(getClass().getResource(File.separator + "testSyntaxErrors.owl").getFile())
		);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.validate(false, null, RetFormat.HTML, null, null, null, files, baos);
		String res = baos.toString();

		Assertions.assertAll(
				() -> Assertions.assertTrue(baos.size()>0),
				() -> Assertions.assertTrue(res.contains("testBiopaxElementIdRule.owl")),
				() -> Assertions.assertTrue(res.contains("testSyntaxErrors.owl"))
		);
  }

	@Test
	public void testClientXml() throws IOException, JAXBException {
		BiopaxValidatorClient client = new BiopaxValidatorClient();
		List<File> files = List.of(
				new File(getClass().getResource(File.separator + "testBiopaxElementIdRule.owl").getFile()),
				new File(getClass().getResource(File.separator + "testSyntaxErrors.owl").getFile())
		);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.validate(true, null, RetFormat.XML, null, null, null, files, baos);
		ValidatorResponse resp = client.unmarshal(baos.toString());

		Assertions.assertAll(
				() -> Assertions.assertTrue(baos.size()>0),
				() -> Assertions.assertNotNull(resp),
				() -> Assertions.assertEquals(2, resp.getValidation().size())
		);

//		System.out.println(resp.getValidation().get(0).getSummary()
//				+ "; cases: " + resp.getValidation().get(0).getTotalProblemsFound());
	}
	
}
