
import java.io.FileOutputStream;
import java.io.IOException;

import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;

abstract class TestBasic {
    
	final static String TEST_DATA_DIR = "testfiles/data/";
	
	SimpleExporter exporter = 
		new SimpleExporter(BioPAXLevel.L3);
	
	void writeExample(String file, Model model) {
    	try {
			exporter.convertToOWL(model, 
					new FileOutputStream(TEST_DATA_DIR + file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
}
