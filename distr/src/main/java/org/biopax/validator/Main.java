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
	static int maxErrors = 0;
	static final String EXT = ".modified.owl";

	public static void main(String[] args) throws Exception {				
        if(args.length < 2) {
        	String usage = 
    			"\n The BioPAX Validator v2.2, Console Java Application\n\n" +
    		    "Parameters: <input> <output[.xml|.html]> [--auto-fix] [--normalize] [--max-errors=<n>]\n" + 
    		    "(the second and next arguments are optional and can go in any order).\n" +
    		    "For example:\n" +
    		    "  path/dir errors.xml\n" +
    		    "  list:batch_file.txt errors.xml\n" +
    		    "  file:biopax.owl errors.xml --auto-fix --normalize\n" +
    		    "  http://www.some.net/data.owl errors.html\n\n" +
    		    "A batch file should list one task (resource) per line, i.e., " +
    		    "file:path/file or URL (to BioPAX data)\n" +
    		    "If '--auto-fix' or '--normalize' options were used, it " +
    		    "also creates a new BioPAX file for each input file " +
    		    "in the current working directory (using '.modified.owl' exention). " +
    		    "If the output file extension is '.html', the XML result will " +
    		    "be auto-transformed to a stand-alone HTML/javascript page, " +
    		    "which is very similar to what the online version returns.";
            System.out.println(usage);
            System.exit(-1);
        }
        
		String input = args[0];
		String output = args[1];

		if (args.length > 2) {
			for (int i = 1; i < args.length; i++) {
				if("--auto-fix".equalsIgnoreCase(args[i])) {
					autofix = true;
				} else if("--normalize".equalsIgnoreCase(args[i])) {
					normalize = true;
				} else if(args[i].startsWith("--max-errors=")) {
					String n = args[i].substring(13);
					maxErrors = Integer.parseInt(n);
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
			// validate all
			ValidatorResponse validatorResponse = runBatch(validator,
					getResourcesToValidate(input));
			
			// save modified BioPAX data
			if (autofix || normalize) {
				for (Validation result : validatorResponse.getValidationResult()) 
				{
					String out = result.getDescription();
					// if was URL, create a shorter name;
					out = out.replaceAll("\\[|\\]","").replaceFirst("/&", ""); // remove ']', '[', and ending '/', if any
					int idx = out.lastIndexOf('/');
					if(idx >= 0) {
						if(idx < out.length() - 1)
							out = out.substring(idx+1);
					}
					out += EXT; // add the file extension
					PrintWriter bpWriter = new PrintWriter(out);
					String owl = result.getModelSerialized();
					bpWriter.write(owl, 0, owl.length());
					bpWriter.write(System.getProperty ( "line.separator" ));
					bpWriter.flush();
					// remove now saved BioPAX model from the xml result
					result.setModel(null);
					result.setModelSerialized(null);
				}
			}
			
			// init errors writer
			PrintWriter errWriter = (output == null) 
				? new PrintWriter(System.out)
					: new PrintWriter(output);
				
			// save the validation result either as XML or HTML
			Source xsltSrc = (output != null && output.endsWith(".html"))
				? new StreamSource(ctx.getResource("classpath:html-result.xsl").getInputStream())
				: null;
			BiopaxValidatorUtils.write(validatorResponse, errWriter, xsltSrc);
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
        	result.setMaxErrors(maxErrors);
        	result.setDescription(resource.getDescription());
        	if(log.isInfoEnabled())
        		log.info("BioPAX DATA IMPORT FROM: " 
        			+ result.getDescription());
			try{
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
}
