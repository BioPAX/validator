package org.biopax.validator;

import java.io.*;
import java.util.*;

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

	public static void main(String[] args) throws Exception {				
		// this does 90% of the job ;)
		ctx = new ClassPathXmlApplicationContext("validator-context.xml");
		// Rules are now loaded, and AOP is listening for BioPAX model method calls.
		
        // get the beans to work with
        Validator validator = (Validator) ctx.getBean("validator");
        BiopaxValidatorUtils utils = (BiopaxValidatorUtils) ctx.getBean("utils");
        
        if(args.length < 1) {
        	String usage = 
    			"\n" +
    			" Pathway Commons BioPAX Validator v1.0b, 07/2009\n\n" +
    		    "It Takes One Argument or Two (the Second Is the Output File Name)\n\n" +
    		    "For Example:\n" +
    		    "  pc:1234567\n" +
    		    "  list:batch_file_name\n" +
    		    "  file:biopax.owl\n" +
    		    "  http://www.some.net/data.owl\n\n" +
    		    "The Batch File Can List the Following (One Task Per Line): \n" +
    		    "a PC Pathway ID (e.g., pc:11111), file:/path/to/file, or URL (to BioPAX data)\n";
        	
        	System.out.println(usage);
        	listAllRules(System.out, validator.getRules());
        }
		
		boolean isQuit = false; // user interaction loop breaking condition
		String input = null;
		String output = null;
		if(args.length > 0) {
			isQuit = true; // quite the shell after job is done
			input = args[0];
		}
		if(args.length > 1) {
			output = args[1];
		}
		
		Scanner sc = new Scanner(System.in);
		PrintWriter writer = (output==null) ? new PrintWriter(System.out) : new PrintWriter(output);
		
        do {
			if(!isQuit) {
				input = null;
				System.out.println("Please Specify a Directory, OWL File "
								+ "(as 'file:filename.owl'), Batch ('list:filename.txt'), " +
								"or URL");
				System.out.println("INPUT: ");
				input = sc.nextLine();
				System.out.println("Where do you want to write the results? \n" +
						"(Default Is Console Output)");
				System.out.println("OUTPUT: ");
				output = sc.nextLine();
				if (output == null || "".equals(output.trim()) ) {
		        	writer = new PrintWriter(System.out);
				} else {
					writer = new PrintWriter(output);
				}
			}
	
			if(input != null && !"".equals(input)) {
				// go!
				ValidatorResponse validatorResponse = runBatch(validator, getResourcesToValidate(input));
				utils.write(validatorResponse, writer);
			}
			if(!isQuit) {
				System.out.println("\nAre You Going to Continue? (NO/yes) "); // default is to quit
				if (!"yes".equalsIgnoreCase(sc.nextLine().trim())) {
					isQuit = true;
				}
			}	
			
		} while (!isQuit);
        
        System.out.println("Thank you.");
    }
	
	
	protected static ValidatorResponse runBatch(Validator validator, 
			Collection<Resource> resources) throws IOException {					
		ValidatorResponse response = new ValidatorResponse();       

        // Read from the batch and validate from file, id or url, line-by-line (stops on first empty line)
        for (Resource resource: resources) {
        	Validation result = new Validation();
        	String modelName = resource.getDescription();
        	if(log.isInfoEnabled())
        		log.info("BioPAX DATA IMPORT FROM: " + modelName);
			try{
				result.setDescription(modelName);
				validator.importModel(result, resource.getInputStream());
				validator.validate(result);
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
	            	if( !(ResourceUtils.isUrl(line) || line.startsWith("pc:")) ) {
	            		log.error("Invalid URL: " + line + 
	            				". A resource must be either a " +
	            				"pseudo URL (classpath: or file:) or standard URL!");
	            		continue;
	            	}
	        		setRes.add(ctx.getResource(line));
	        	}
	            reader.close();
	        } else {
	        	// a single local OWL file, link to remote data, or pc:ID
	        	Resource resource = null;
				if (input.startsWith("pc:")) {
					// get data from a pathwaycommons.org WS, using pathway ID:
					resource = BiopaxValidatorUtils.getResourceByPcId(input.substring(3));	
				} else {
					// or - from local or remote (must be OWL) file:
					if (!ResourceUtils.isUrl(input)) 
						input = "file:" + input;
					resource = ctx.getResource(input);
				}
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