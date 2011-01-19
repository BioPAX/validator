import java.io.*;

import javax.xml.bind.JAXBException;

import org.biopax.validator.BiopaxValidatorClient;
import org.biopax.validator.BiopaxValidatorClient.RetFormat;
import org.biopax.validator.jaxb.ValidatorResponse;
import org.junit.*;

// remove @Ignore when biopax.org/biopax-validator/  is available
@Ignore
public class BiopaxValidatorClientTest {

	@Test
	public void testClientHtml() throws IOException {
		//BiopaxValidatorClient client = new BiopaxValidatorClient();
		
		BiopaxValidatorClient client = new BiopaxValidatorClient("http://localhost:8080/biopax-validator/check.html");
		
		File[] files = new File[] {
				new File(getClass().getResource(
						File.separator + "testBiopaxElementIdRule.owl").getFile())
		};
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.validate(false, false, RetFormat.HTML, null, null, files, baos);
		
		System.out.println(baos.toString());
    }
	
	@Test
	public void testClientXml() throws IOException, JAXBException {
		//BiopaxValidatorClient client = new BiopaxValidatorClient();
		
		BiopaxValidatorClient client = new BiopaxValidatorClient("http://localhost:8080/biopax-validator/check.html");
		
		File[] files = new File[] {
				new File(getClass().getResource(
					File.separator + "testBiopaxElementIdRule.owl").getFile())
		};
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.validate(true, true, RetFormat.XML, null, null, files, baos);
		
		//System.out.println(baos.toString());
		
		Assert.assertTrue(baos.size()>0);
		
		ValidatorResponse resp = client.unmarshal(baos.toString());
		
		Assert.assertNotNull(resp);
		Assert.assertFalse(resp.getValidationResult().isEmpty());
		
		System.out.println(resp.getValidationResult().get(0).getSummary()
				+ "; cases: " + resp.getValidationResult().get(0).getTotalProblemsFound());
    }
	
}
