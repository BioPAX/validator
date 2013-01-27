package org.biopax.validator;

/*
 * #%L
 * BioPAX Validator Assembly
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.*;
import java.util.*;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.Validator;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.api.beans.ValidatorResponse;
import org.biopax.validator.impl.IdentifierImpl;
import org.biopax.validator.utils.Normalizer;
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
	static int maxErrors = 0;
	static final String EXT = ".modified.owl";
	static String profile = null;
	static String xmlBase = null;

	public static void main(String[] args) throws Exception {
		
        if(args == null || args.length < 2) {
        	log.warn("At least input and output parameters must be specified.");
        	printHelpAndQuit();
        }

		String input = args[0];
		String output = args[1];
		if(input == null || input.isEmpty() || output == null || output.isEmpty()) {
			log.warn("At least input and output parameters must be specified.");
			printHelpAndQuit();
		}
        
		// match optional parameters
		if (args.length > 2) {
			for (int i = 1; i < args.length; i++) {
				if("--auto-fix".equalsIgnoreCase(args[i])) {
					autofix = true;
				} else if(args[i].startsWith("--max-errors=")) {
					String n = args[i].substring(13);
					maxErrors = Integer.parseInt(n);
				} else if(args[i].startsWith("--profile=")) {
					profile = args[i].substring(10);
				} else if(args[i].startsWith("--xmlBase=")) {
					xmlBase = args[i].substring(10);
				}
			}
		}

		// this does 90% of the job ;)
		ctx = new ClassPathXmlApplicationContext(
			new String[] {"META-INF/spring/appContext-validator.xml", "META-INF/spring/appContext-loadTimeWeaving.xml"});
		// Rules are now loaded, and AOP is listening for BioPAX model method calls.
		
        // get the beans to work with
        Validator validator = (Validator) ctx.getBean("validator");
		
		// go validate all
		ValidatorResponse validatorResponse = runBatch(validator,
				getResourcesToValidate(input));
		
		// save modified BioPAX data
		
		for (Validation result : validatorResponse.getValidationResult()) 
		{
			if (autofix) {
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
				String owl = result.getModelData();
				bpWriter.write(owl, 0, owl.length());
				bpWriter.write(System.getProperty ( "line.separator" ));
				bpWriter.flush();
			}
			
			// remove the BioPAX model data before printing results
			result.setModel(null);
			result.setModelData(null);
		}

			
		// save the validation result either as XML or HTML
		PrintWriter errWriter = new PrintWriter(output);
		Source xsltSrc = (output.endsWith(".html"))
			? new StreamSource(ctx.getResource("classpath:html-result.xsl").getInputStream())
				: null;
		ValidatorUtils.write(validatorResponse, errWriter, xsltSrc);
	}
	
	
	private static void printHelpAndQuit() {
    	final String usage = 
			"\n The BioPAX Validator v3, Console Java Application\n\n" +
		    "Parameters: <input> <output[.xml|.html]> [--auto-fix] [--xmlBase=<base>] [--max-errors=<n>] [--profile=notstrict]\n" + 
		    "(the second and next arguments are optional and can go in any order).\n" +
		    "For example:\n" +
		    "  path/dir errors.xml\n" +
		    "  list:batch_file.txt errors.xml\n" +
		    "  file:biopax.owl errors.xml --auto-fix\n" +
		    "  http://www.some.net/data.owl errors.html\n\n" +
		    "A batch file should list one task (resource) per line, i.e., " +
		    "file:path/file or URL (to BioPAX data)\n" +
		    "If '--auto-fix' option was used, it " +
		    "also creates a new BioPAX file for each input file " +
		    "in the current working directory (using '.modified.owl' exention). " +
		    "If the output file extension is '.html', the XML result will " +
		    "be auto-transformed to a stand-alone HTML/javascript page, " +
		    "which is very similar to what the online version returns.";
        System.out.println(usage);
        System.exit(-1);
	}


	protected static ValidatorResponse runBatch(Validator validator, 
			Collection<Resource> resources) throws IOException {					
		ValidatorResponse response = new ValidatorResponse();       

        // Read from the batch and validate from file, id or url, line-by-line (stops on first empty line)
        for (Resource resource: resources) {
        	Validation result = new Validation(new IdentifierImpl(), resource.getDescription(), autofix, null, maxErrors, profile);
        	result.setDescription(resource.getDescription()); 
       		log.info("BioPAX DATA IMPORT FROM: " + result.getDescription());
			try{				
				validator.importModel(result, resource.getInputStream());
				validator.validate(result);
				
				//if autofix is enabled, then do normalize too
				if(autofix) {
					Model model = (Model) result.getModel();
					Normalizer normalizer = new Normalizer();
					normalizer.setXmlBase(xmlBase); //if xmlBase is null, the model's one is used
					normalizer.normalize(model);
					result.setModelData(SimpleIOHandler.convertToOwl(model));
				}
				
			} catch (Exception e) {
				log.error("failed", e);
			}
			
			response.addValidationResult(result);
			validator.getResults().remove(result);
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
