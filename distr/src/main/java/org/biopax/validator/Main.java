package org.biopax.validator;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.normalizer.Normalizer;
import org.biopax.validator.api.Validator;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.api.beans.ValidatorResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * PC BioPAX Validator (console), which
 * checks from the user input or a "batch" file.
 * <p>
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
  static String outFormat = "html";
  static String output = null;

  public static void main(String[] args) throws Exception {

    if (args == null || args.length == 0) {
      log.warn("At least input file/dir must be specified.");
      printHelpAndQuit();
    }

    String input = args[0];

    if (input == null || input.isEmpty() || input.startsWith("--")) {
      log.warn("Input (file, url, or directory) is probably missing");
      printHelpAndQuit();
    }

    // match optional parameters
    for (int i = 1; i < args.length; i++) {
      if ("--auto-fix".equalsIgnoreCase(args[i])) {
        autofix = true;
      } else if (args[i].startsWith("--max-errors=")) {
        String n = args[i].substring(13);
        maxErrors = Integer.parseInt(n);
      } else if (args[i].startsWith("--profile=")) {
        profile = args[i].substring(10);
      } else if (args[i].startsWith("--xmlBase=")) {
        xmlBase = args[i].substring(10);
      } else if (args[i].startsWith("--output=")) {
        output = args[i].substring(9);
      } else if (args[i].startsWith("--out-format=")) {
        outFormat = args[i].substring(13);
        if (outFormat.isEmpty())
          outFormat = "html";
      }
    }

    // this does 90% of the job ;)
    ctx = new ClassPathXmlApplicationContext(
      new String[]{"META-INF/spring/appContext-loadTimeWeaving.xml",
        "META-INF/spring/appContext-validator.xml"});
    // Rules are now loaded, and AOP is listening for BioPAX model method calls.

    // get the beans to work with
    Validator validator = (Validator) ctx.getBean("validator");

    // go validate all
    runBatch(validator, getResourcesToValidate(input));
  }


  private static void printHelpAndQuit() {
    final String usage =
      "\nThe BioPAX Validator v3\n\n" +
        "Usage (arguments):\n <input> [--output=<filename>] [--out-format=xml|html] [--auto-fix] " +
        "[--xmlBase=<base>] [--max-errors=<n>] [--profile=notstrict]\n\n" +
        "Given --output=<filename>, a one-file validation report will be \n" +
        "generated (HTML or XML) instead of default report file(s) in the \n" +
        "current directory. Optional arguments can go in any order.\n" +
        "For example:\n" +
        "  path/dir --out-format=xml\n" +
        "  list:batch_file.txt --output=reports.html\n" +
        "  file:biopax.owl --out-format=xml --auto-fix\n" +
        "  http://www.some.net/data.owl\n\n" +
        "A batch file should list one task (resource) per line, i.e.,\n" +
        "file:path/file or URL (to BioPAX data)\n" +
        "If '--auto-fix' option was used, it also creates a new BioPAX file \n" +
        "for each input file in the current working directory \n" +
        "(adding '.modified.owl' exention). If the outFormat file extension \n" +
        "is '.html', the XML result will be auto-transformed to a stand-alone \n" +
        "HTML/javascript page, which is very similar to what the online version returns.";
    System.out.println(usage);
    System.exit(-1);
  }


  protected static void runBatch(Validator validator,
                                 Collection<Resource> resources) throws IOException {

    //collect all reports in this object (only if --output option was used)
    final ValidatorResponse consolidatedReport = new ValidatorResponse();

    // Read from the batch and validate from file, id or url, line-by-line (stops on first empty line)
    for (Resource resource : resources) {
      Validation result = new Validation(new BiopaxIdentifier(), resource.getDescription(),
        autofix, null, maxErrors, profile);
      result.setDescription(resource.getDescription());
      log.info("BioPAX DATA IMPORT FROM: " + result.getDescription());
      try {
        validator.importModel(result, resource.getInputStream());
        validator.validate(result);

        //if autofix is enabled, then do normalize too
        if (autofix) {
          Model model = (Model) result.getModel();
          Normalizer normalizer = new Normalizer();
          normalizer.setXmlBase(xmlBase); //if xmlBase is null, the model's one is used
          normalizer.normalize(model);
        }

        if (output != null)
          consolidatedReport.addValidationResult(result);

      } catch (Exception e) {
        log.error("failed", e);
      }

      final String filename = outFileName(result);
      PrintWriter writer;

      // save modified (normalized) biopax if the option was used
      if (autofix) {
        Model model = (Model) result.getModel();
        (new SimpleIOHandler()).convertToOWL(model, new FileOutputStream(filename + EXT));
      }

      // remove the BioPAX data before writing report
      result.setModel(null);
      result.setModelData(null);

      // save the individual validation results
      //unless the user specified the output file explicitly
      if (output == null || output.isEmpty()) {
        writer = new PrintWriter(filename + ".validation." + outFormat);
        Source xsltSrc = (outFormat.equalsIgnoreCase("html"))
          ? new StreamSource(ctx.getResource("classpath:html-result.xsl").getInputStream())
          : null;
        ValidatorUtils.write(result, writer, xsltSrc);
        writer.close();
      }

      validator.getResults().remove(result);
      log.info("Done with " + filename);
    }

    // save if the user specified the output file explicitly
    if (output != null) {
      Writer writer = new PrintWriter(output);
      Source xsltSrc = (outFormat.equalsIgnoreCase("html"))
        ? new StreamSource(ctx.getResource("classpath:html-result.xsl").getInputStream())
        : null;
      ValidatorUtils.write(consolidatedReport, writer, xsltSrc);
      writer.close();
    }
  }


  private static String outFileName(Validation result) {
    String filename = result.getDescription();
    // if was URL, create a shorter name;
    // remove ']', '[', and ending '/', if any
    filename = filename.replaceAll("\\[|\\]", "").replaceFirst("/&", "");
    int idx = filename.lastIndexOf('/');
    if (idx >= 0) {
      if (idx < filename.length() - 1)
        filename = filename.substring(idx + 1);
    }

    return filename;
  }


  public static Collection<Resource> getResourcesToValidate(String input) throws IOException {
    Set<Resource> setRes = new HashSet<Resource>();

    File fileOrDir = new File(input);
    if (fileOrDir.isDirectory()) {
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
        if (!ResourceUtils.isUrl(line)) {
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
