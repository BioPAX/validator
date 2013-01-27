package org.biopax.validator;

/*
 * #%L
 * BioPAX Validator Client
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

import javax.xml.bind.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.jaxb.Behavior;
import org.biopax.validator.jaxb.ValidatorResponse;

/**
 * Simple (example) BioPAX Validator client 
 * to upload and check BioPAX OWL files.
 * 
 * @author rodche
 *
 */
public class BiopaxValidatorClient {
	private static final Log log = LogFactory.getLog(BiopaxValidatorClient.class);
	
	/**
	 * Default BioPAX Validator's URL
	 */
	public static final String 
		DEFAULT_VALIDATOR_URL = "http://www.biopax.org/biopax-validator/check.html";
	
	public static enum RetFormat {
		HTML,// errors as HTML/Javascript 
		XML, // errors as XML
		OWL; // modified BioPAX only (when 'autofix' or 'normalize' is true)
	}
	
	private static HttpClient httpClient = new HttpClient();
	private String url;

	
    /**
     * Main Constructor
     * 
     * It configures for the validator's URL
     * (defined by DEFAULT_VALIDATOR_URL constant)
     * and result format ().
     * 
     * @param url - validator's file-upload form address
     */
    public BiopaxValidatorClient(String url) {
		this.url = (url != null) ? url : DEFAULT_VALIDATOR_URL;
	}
    
    
    /**
     * Default Constructor
     * 
     * It configures for the default validator
     * (defined by DEFAULT_VALIDATOR_URL constant)
     * to return XML result.
     */
    public BiopaxValidatorClient() {
    	this(DEFAULT_VALIDATOR_URL);
	}
       

    /**
     * Checks a BioPAX OWL file(s) or resource 
     * using the online BioPAX Validator 
     * and prints the results to the output stream.
     * 
     * @param autofix true/false (experimental)
     * @param profile validation profile name
     * @param retFormat xml, html, or owl (no errors, just modified owl, if autofix=true)
     * @param biopaxUrl check the BioPAX at the URL
     * @param biopaxFiles an array of BioPAX files to validate
     * @param out
     * @throws IOException
     */
    public void validate(boolean autofix, String profile, RetFormat retFormat, Behavior filterBy,
    		Integer maxErrs, String biopaxUrl, File[] biopaxFiles, OutputStream out) throws IOException 
    {
        Collection<Part> parts = new HashSet<Part>();
        
        if(autofix) {
        	parts.add(new StringPart("autofix", "true"));
        }
        
        //TODO add extra options (normalizer.fixDisplayName, normalizer.inferPropertyOrganism, normalizer.inferPropertyDataSource, normalizer.xmlBase)?
              
        if(profile != null && !profile.isEmpty()) {
        	parts.add(new StringPart("profile", profile));
        }
        
        // set result type
		if (retFormat != null) {
			parts.add(new StringPart("retDesired", retFormat.toString().toLowerCase()));
		}
        
		if(filterBy != null) {
			parts.add(new StringPart("filter", filterBy.toString()));
		}
		
		if(maxErrs != null && maxErrs > 0) {
			parts.add(new StringPart("maxErrors", maxErrs.toString()));
		}
		
        // add data
		if (biopaxFiles != null && biopaxFiles.length > 0) {
			for (File f : biopaxFiles) {
				parts.add(new FilePart(f.getName(), f));
			}
		} else if(biopaxUrl != null) {
        	parts.add(new StringPart("url", biopaxUrl));
        } else {
        	log.error("Nothing to do (no BioPAX data specified)!");
        	return;
        }
        
        PostMethod post = new PostMethod(url);
        post.setRequestEntity(
        		new MultipartRequestEntity(
        				parts.toArray(new Part[]{}), post.getParams()
        			)
        		);
        int status = httpClient.executeMethod(post);
		
        log.info("HTTP Status Text>>>" + HttpStatus.getStatusText(status));
		
		BufferedReader res = new BufferedReader(
				new InputStreamReader(post.getResponseBodyAsStream())
			);
		String line;
		PrintWriter writer = new PrintWriter(out);
		while((line = res.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		res.close();
		post.releaseConnection();
    }
    
    public void setUrl(String url) {
		this.url = url;
	}
    
    public String getUrl() {
		return url;
	}
    
    /**
     * 
     * 
     * @param xml
     * @return
     * @throws JAXBException
     */
    public static ValidatorResponse unmarshal(String xml) throws JAXBException {
    	JAXBContext jaxbContext = JAXBContext.newInstance("org.biopax.validator.jaxb");
		Unmarshaller un = jaxbContext.createUnmarshaller();
		Source src = new StreamSource(new StringReader(xml));
		ValidatorResponse resp = un.unmarshal(src, ValidatorResponse.class).getValue();
		return resp;
    }
    
    
    /**
     * Checks BioPAX files using the online BioPAX Validator. 
     * 
     * @see <a href="http://www.biopax.org/validator">BioPAX Validator Webservice</a>
     * 
     * @param argv
     * @throws IOException
     */
    public static void main(String[] argv) throws IOException 
    {
        if (argv.length == 0) {
            System.err.println("Available parameters: \n" + 
            	"<path> <output> [xml|html|biopax] [auto-fix] [only-errors] [maxerrors=n] [notstrict]\n" +
            	"\t- validate a BioPAX file/directory (up to ~25MB in total size, -\n" +
            	"\totherwise, please use the biopax-validator.jar instead)\n" +
            	"\tin the directory using the online BioPAX Validator service\n" +
            	"\t(generates html or xml report, or gets the processed biopax\n" +
            	"\t(cannot fix all errros though) see http://www.biopax.org/validator)");
            System.exit(-1);
        }
    	    	
    	final String input = argv[1];
        final String output = argv[2];
        
        File fileOrDir = new File(input);
        if (!fileOrDir.canRead()) {
            System.err.println("Cannot read from " + input);
            System.exit(-1);
        }        
        if (output == null || output.isEmpty()) {
            System.err.println("No output file specified (for the validation report).");
            System.exit(-1);
        }
        
        // default options
        RetFormat outf = RetFormat.HTML;
        boolean fix = false;
        Integer maxErrs = null;
        Behavior level = null; //will report both errors and warnings
        String profile = null;
        
        // match optional arguments
		for (int i = 3; i < argv.length; i++) {
			if ("html".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.HTML;
			} else if ("xml".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.XML;
			} else if ("biopax".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.OWL;
			} else if ("auto-fix".equalsIgnoreCase(argv[i])) {
				fix = true;
			} else if ("only-errors".equalsIgnoreCase(argv[i])) {
				level = Behavior.ERROR;
			} else if ((argv[i]).toLowerCase().startsWith("maxerrors=")) {
				String num = argv[i].substring(10);
				maxErrs = Integer.valueOf(num);
			} else if ("notstrict".equalsIgnoreCase(argv[i])) {
				profile = "notstrict";
			}
		}

        // collect files
        Collection<File> files = new HashSet<File>();
        
        if (fileOrDir.isDirectory()) {
            // validate all the OWL files in the folder
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".owl"));
                }
            };
            
            for (String s : fileOrDir.list(filter)) {
                files.add(new File(
                	fileOrDir.getCanonicalPath() + File.separator + s));
            }
        } else {
            files.add(fileOrDir);
        }

        // upload and validate using the default URL: http://www.biopax.org/biopax-validator/check.html        
        if (!files.isEmpty()) {
        	BiopaxValidatorClient val = new BiopaxValidatorClient();
        	val.validate(fix, profile, outf, level, maxErrs, 
        		null, files.toArray(new File[]{}), new FileOutputStream(output));
        }
    }
    
}