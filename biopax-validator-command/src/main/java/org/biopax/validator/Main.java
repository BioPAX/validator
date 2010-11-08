package org.biopax.validator;

import java.io.*;
import java.util.*;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.Validator;
import org.biopax.validator.result.Validation;
import org.biopax.validator.result.ValidatorResponse;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

/**
 * PC BioPAX Validator (console), which
 * checks from the user input or a "batch" file.
 *
 * See: README.txt, context.xml, messages.properties
 *
 * @author rodche
 */
public class Main {
	static final Log log = LogFactory.getLog(Main.class);
	static ApplicationContext ctx;
	static boolean autofix = false;
	static boolean normalize = false;
	static boolean ret_biopax = false;

	public static void main(String[] args) throws Exception {				
        if(args.length == 0) {
        	String usage = 
    			"\n BioPAX Validator 2.0A\n\n" +
    		    "Parameters: <input> [<output>] [auto-fix] [normalize] [return-biopax]\n" + 
    		    "(2..5 are optional and can be in any order)\n" +
    		    "For Example:\n" +
    		    "  list:batch_file_name \n" +
    		    "  file:biopax.owl auto-fix normalize errors.xml\n" +
    		    "  http://www.some.net/data.owl output.xml\n\n" +
    		    "A Batch File Should List One Task Per Line: " +
    		    "file:/path/to/file or URL (to BioPAX data)\n" +
    		    "Use 'return-biopax' flag " +
    		    "(only works together with the 'auto-fix' or 'normalize') " +
    		    " to get the modified BioPAX OWL (no error messages are reported)";
            System.out.println(usage);
            System.exit(-1);
        }
        
		String input = args[0];
		String output = null;

		if (args.length > 1) {
			for (int i = 1; i < args.length; i++) {
				if("auto-fix".equalsIgnoreCase(args[i])) {
					autofix = true;
				} else if("normalize".equalsIgnoreCase(args[i])) {
					normalize = true;
				} else if("return-biopax".equalsIgnoreCase(args[i])) {
					ret_biopax = true;
				} else {
					output = args[i];
				}
			}
		}

		// this does 90% of the job ;)
		ctx = new ClassPathXmlApplicationContext("validator-aop-context.xml");
		// Rules are now loaded, and AOP is listening for BioPAX model method calls.
		
        // get the beans to work with
        Validator validator = (Validator) ctx.getBean("validator");
		
		// go!
		if (input != null && !"".equals(input)) {
			PrintWriter writer = (output == null) 
				? new PrintWriter(System.out)
					: new PrintWriter(output);
			ValidatorResponse validatorResponse = runBatch(validator,
					getResourcesToValidate(input));
			if (!ret_biopax) {
				Source xsltSrc = new StreamSource(ctx.getResource(
						"classpath:default-result.xsl").getInputStream());
				BiopaxValidatorUtils.write(validatorResponse, writer, xsltSrc);
			} else if (autofix || normalize) {
				for (Validation result : validatorResponse
						.getValidationResult()) {
					String owl = result.getModelSerialized();
					writer.write(owl, 0, owl.length());
					writer.write(System.getProperty ( "line.separator" ));
					writer.flush();
				}
			}
		}
	}
	
	
	protected static ValidatorResponse runBatch(Validator validator, 
			Collection<Resource> resources) throws IOException {					
		ValidatorResponse response = new ValidatorResponse();       

        // Read from the batch and validate from file, id or url, line-by-line (stops on first empty line)
        for (Resource resource: resources) {
        	Validation result = new Validation();
        	result.setFix(autofix);
        	result.setNormalize(normalize);
        	String modelName = resource.getDescription();
        	if(log.isInfoEnabled())
        		log.info("BioPAX DATA IMPORT FROM: " + modelName);
			try{
				result.setDescription(modelName);
				validator.importModel(result, resource.getInputStream());
				validator.validate(result);
				if(autofix || normalize)
					result.updateModelSerialized();
			} catch (Exception e) {
				log.error("failed", e);
				if(log.isDebugEnabled()) {
					e.printStackTrace();
				}
			}
			response.addValidationResult(result);
			validator.getResults().remove(result);
			if (log.isInfoEnabled()) 
				log.info("Done.");
		}
		
        return response;
    }
	
	
	public static Collection<Resource> getResourcesToValidate(String input) throws IOException {
			Set<Resource> setRes = new HashSet<Resource>();
			
	        File fileOrDir = new File(input);
	        if(fileOrDir.isDirectory()) {
	        	// validate all the OWL files in the folder
	        	FilenameFilter filter = new FilenameFilter() {
		            public boolean accept(File dir, String name) {
		                return (name.endsWith(".owl"));
		            }
		        };
		        for (String s : fileOrDir.list(filter)) {
		        	String uri = "file:" + fileOrDir.getCanonicalPath() + File.separator + s;
		        	setRes.add(ctx.getResource(uri));
		        }
	        } else if (input.startsWith("list:")) {
	        	// consider it's a file that contains a list of (pseudo-)URLs
	        	String batchFile = input.replaceFirst("list:", "file:");
	            Reader isr = new InputStreamReader(ctx.getResource(batchFile).getInputStream());
	            BufferedReader reader = new BufferedReader(isr);
	        	String line;
	            while ((line = reader.readLine()) != null && !"".equals(line.trim())) {
	            	// check the source URL
	            	if( !ResourceUtils.isUrl(line)) {
	            		log.error("Invalid URL: " + line + 
	            				". A resource must be either a " +
	            				"pseudo URL (classpath: or file:) or standard URL!");
	            		continue;
	            	}
	        		setRes.add(ctx.getResource(line));
	        	}
	            reader.close();
	        } else {
	        	// a single local OWL file or remote data
	        	Resource resource = null;
				if (!ResourceUtils.isUrl(input)) 
					input = "file:" + input;
				resource = ctx.getResource(input);
	        	setRes.add(resource);
	        }
	              
		return setRes;
	}
	
	private static void listAllRules(OutputStream out, Set<Rule<?>> set) {
		// show currently loaded rules:
		StringBuffer sb = new StringBuffer("Loaded rules:\n");
		for (Rule<?> r : set) {
			sb.append(r.getName()).append(" => ");
			sb.append(r.getTip()).append(" (");
			sb.append(r.getBehavior()).append(")\n");
		}
		PrintWriter w = new PrintWriter(out);
		w.write(sb.toString());
	}
}