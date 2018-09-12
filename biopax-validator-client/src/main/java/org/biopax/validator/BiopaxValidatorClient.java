package org.biopax.validator;

/*
 *
 */

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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
		DEFAULT_VALIDATOR_URL = "http://www.biopax.org/validator/check.html";
	
	/**
	 * The Java Option to set a BioPAX Validator URL 
	 * (if set, it overrides the default as well as a value set via the Constructor)
	 */
	public static final String JVM_PROPERTY_URL = "biopax.validator.url";
	
	public static enum RetFormat {
		HTML,// errors as HTML/Javascript 
		XML, // errors as XML
		OWL; // modified BioPAX only (when 'autofix' or 'normalize' is true)
	}
	
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
		// 1) use the arg (if not empty/null) or the default URL
    	this.url = (url == null || url.isEmpty())
			? DEFAULT_VALIDATOR_URL : url;
    	
    	// 2) override if the JVM option is set to another value
    	this.url = System.getProperty(JVM_PROPERTY_URL, this.url);
		
    	// 3) get actual location (force through redirects, if any)
		try {
			this.url = location(this.url);
		} catch (IOException e) {
			log.warn("Failed to resolve to actual web service " +
				"URL using: " + url + " (if there is a 301/302/307 HTTP redirect, " +
					"then validation requests (using HTTP POST method) will probably fail...)", e);
		}
	}
    
    
    /**
     * Default Constructor
     * 
     * It configures for the default validator URL.
     */
    public BiopaxValidatorClient() {
    	this(null);
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
    	MultipartEntityBuilder meb = MultipartEntityBuilder.create();
    	meb.setCharset(Charset.forName("UTF-8"));
    	
    	if(autofix)
    		meb.addTextBody("autofix", "true");

		//TODO add extra options (normalizer.fixDisplayName, normalizer.xmlBase)?

		if(profile != null && !profile.isEmpty())
    		meb.addTextBody("profile", profile);
    	if(retFormat != null)
    		meb.addTextBody("retDesired", retFormat.toString().toLowerCase());
    	if(filterBy != null)
    		meb.addTextBody("filter", filterBy.toString());
    	if(maxErrs != null && maxErrs > 0)
    		meb.addTextBody("maxErrors", maxErrs.toString());
    	if(biopaxFiles != null && biopaxFiles.length > 0)
    		for (File f : biopaxFiles) //important: use MULTIPART_FORM_DATA content-type
    			meb.addBinaryBody("file", f, ContentType.MULTIPART_FORM_DATA, f.getName());
    	else if(biopaxUrl != null) {
    		meb.addTextBody("url", biopaxUrl);
    	} else {
    		log.error("Nothing to do (no BioPAX data specified)!");
        	return;
    	}
    	
    	HttpEntity httpEntity = meb.build();
//    	if(log.isDebugEnabled()) httpEntity.writeTo(System.err);
    	String content = Executor.newInstance()
    			.execute(Request.Post(url).body(httpEntity))
    				.returnContent().asString();  	

    	//save: append to the output stream (file)
		BufferedReader res = new BufferedReader(new StringReader(content));
		String line;
		PrintWriter writer = new PrintWriter(out);
		while((line = res.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		res.close();
    }
    
    public void setUrl(String url) {
		this.url = url;
	}
    
    public String getUrl() {
		return url;
	}
    
    /**
     * Converts a biopax-validator XML response to the java object.
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
    	    	
    	final String input = argv[0];
        final String output = argv[1];
        
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
		for (int i = 2; i < argv.length; i++) {
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
    
    private String location(final String url) throws IOException {
        String location = url; //initially the same
    	// discover actual location, avoid going in circles:
        int i=0;
        for(String loc = url; loc != null && i<5; i++ ) 
        { 	
        	//do POST for location (Location header present if there's a 301/302/307 redirect on the way)
        	loc = Request.Post(loc).execute()
        			.handleResponse(new ResponseHandler<String>() {
						public String handleResponse(HttpResponse httpResponse)
								throws ClientProtocolException, IOException {
							Header header = httpResponse.getLastHeader("Location");
//							System.out.println("header=" + header);
							return (header != null) ? header.getValue().trim() : null;
						}
			});
        	 
        	if(loc != null) {
        		location = loc;   	
        		log.info("BioPAX Validator location: " + loc);
        	}
    	}
        
        return location;
    }
    
}