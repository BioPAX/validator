/*
 *
 */
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
		BiopaxValidatorClient client = new BiopaxValidatorClient();
		
		File[] files = new File[] {
				new File(getClass().getResource(
						File.separator + "testBiopaxElementIdRule.owl").getFile())
		};
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.validate(false, null, RetFormat.HTML, null, null, null, files, baos);
		
		System.out.println(baos.toString());
    }
	
	@Test
	public void testClientXml() throws IOException, JAXBException {
		BiopaxValidatorClient client = new BiopaxValidatorClient();
		
		File[] files = new File[] {
				new File(getClass().getResource(
					File.separator + "testBiopaxElementIdRule.owl").getFile())
		};
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.validate(true, null, RetFormat.XML, null, null, null, files, baos);
		
		//System.out.println(baos.toString());
		
		Assert.assertTrue(baos.size()>0);
		
		ValidatorResponse resp = client.unmarshal(baos.toString());
		
		Assert.assertNotNull(resp);
		Assert.assertFalse(resp.getValidation().isEmpty());
		
		System.out.println(resp.getValidation().get(0).getSummary()
				+ "; cases: " + resp.getValidation().get(0).getTotalProblemsFound());
    }
	
}
