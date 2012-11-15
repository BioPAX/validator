package org.biopax.uk.ac.ebi.ols.impl;

import org.bbop.dataadapter.DataAdapterException;
import org.biopax.uk.ac.ebi.ols.Loader;
import org.biopax.uk.ac.ebi.ols.Ontology;
import org.biopax.uk.ac.ebi.ols.Term;
import org.biopax.uk.ac.ebi.ols.TermPath;
import org.biopax.uk.ac.ebi.ols.TermRelationship;





import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;


/**
 * @author R. Cote
 * @version $Id: OntologyBean.java,v 1.6 2008/02/01 16:30:28 rglcote Exp $
 */
public class OntologyBean implements Ontology {

    private long ontologyId;

    private String shortOntologyName = null;

    private String fullOntologyName = null;

    private String definition = null;

    private String queryURL = null;

    private String sourceURL = null;

    private Collection<Term> terms = null;

    private Collection<Term> rootTerms = null;

    private Collection<TermRelationship> termRelationships = null;

    private Collection<TermPath> termPaths = null;

    private java.sql.Date loadDate = null;

    private String version = null;

    private boolean fullyLoaded = false;

    private boolean usesImports = false;

    public long getOntologyId() {
        return ontologyId;
    }

    public void setOntologyId(final long _ontologyId) {
        ontologyId = _ontologyId;
    }

    public String getShortOntologyName() {
        return shortOntologyName;
    }

    public void setShortOntologyName(final String _ontologyName) {
        shortOntologyName = _ontologyName;
    }

    public String getFullOntologyName() {
        return fullOntologyName;
    }

    public void setFullOntologyName(final String _ontologyName) {
        fullOntologyName = _ontologyName;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(final String _definition) {
        definition = _definition;
    }

    public Collection<Term> getTerms() {
        return terms;
    }

    public void setTerms(final Collection<Term> _terms) {
        terms = _terms;
    }

    public Collection<Term> getRootTerms() {
        if (rootTerms == null) {
            rootTerms = new ArrayList<Term>();
            if (terms != null) {
                for (Term term : terms) {
                    if (term.isRootTerm()) {
                        rootTerms.add(term);
                    }
                }
            }
        }
        return rootTerms;
    }

    public void setRootTerms(Collection<Term> rootTerms) {
        this.rootTerms = rootTerms;
    }

    public Collection<TermRelationship> getTermRelationships() {
        return termRelationships;
    }

    public void setTermRelationships(final Collection<TermRelationship> _termRelationships) {
        termRelationships = _termRelationships;
    }

    public Collection<TermPath> getTermPaths() {
        return termPaths;
    }

    public void setTermPaths(final Collection<TermPath> _termPaths) {
        termPaths = _termPaths;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String _version) {
        version = _version;
    }

    public java.util.Date getLoadDate() {
        return loadDate;
    }

    public void setLoadDate(final java.sql.Date _loadDate) {
        loadDate = _loadDate;
    }

    public void setLoadData(final java.util.Date _loadDate) {
        loadDate = new java.sql.Date(_loadDate.getTime());
    }

    public boolean getFullyLoaded() {
        return fullyLoaded;
    }

    public void setFullyLoaded(boolean fullyLoaded) {
        this.fullyLoaded = fullyLoaded;
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public String getQueryURL() {
        return queryURL;
    }

    public void setQueryURL(String queryURL) {
        this.queryURL = queryURL;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public boolean isUsesImports() {
        return usesImports;
    }

    public void setUsesImports(boolean usesImports) {
        this.usesImports = usesImports;
    }

    public String toString() {
        return new StringBuilder().append("OntologyBean{").append("ontologyId=").append(ontologyId).append(", shortOntologyName='").append(shortOntologyName).append('\'').append(", fullOntologyName='").append(fullOntologyName).append('\'').append(", definition='").append(definition).append('\'').append(", queryURL='").append(queryURL).append('\'').append(", sourceURL='").append(sourceURL).append('\'').append(", terms=").append(terms).append(", rootTerms=").append(rootTerms).append(", termRelationships=").append(termRelationships).append(", termPaths=").append(termPaths).append(", loadDate=").append(loadDate).append(", version='").append(version).append('\'').append(", fullyLoaded=").append(fullyLoaded).append(", usesImports=").append(usesImports).append('}').toString();
    }

    /**
     * Facade method to simplify most of the Loader package code for user who simply want to obtain
     * a fully-constructed ontology object from a single OBO source file. This method will produce an
     * object that is still valid for persistence however, so mandatory fields need to be provided.
     *
     * @param oboFilePath - The fully qualified path to the file on disk to be parsed [Mandatory]
     * @param shortName   - The short label of the ontology (eg "GO", "MI", etc...) [Mandatory]
     * @param fullName    - The full name of the ontology (eg Gene Ontology)  [Optional]
     * @param definition  - The definition of the ontology [Optional]
     * @return a fully build Ontology object.
     * @throws IllegalArgumentException if the shortName is null or an empty string
     * @throws IOException              on parse errors
     */
    public static Ontology loadFromOBOFile(
            String oboFilePath, String shortName, String fullName, String definition
    ) throws IOException {

        //test parameters - file path
        File oboFile = new File(oboFilePath);
        if (!oboFile.exists()) {
            throw new IOException("The file path to be parsed does not exist");
        }

        return loadFromOBOFile(oboFile, shortName, fullName, definition);

    }


    /**
     * Facade method to simplify most of the Loader package code for user who simply want to obtain
     * a fully-constructed ontology object from a single OBO source file that is accessed via an URL.
     * The URL can be local or remote. If the URL is remote, a temporary copy will be obtained and
     * parsed. This method will produce an object that is still valid for persistence however, so
     * mandatory fields need to be provided.
     *
     * @param oboFileURL - The URL of the OBO file to be read (remote or local) [Mandatory]
     * @param shortName  - The short label of the ontology (eg "GO", "MI", etc...) [Mandatory]
     * @param fullName   - The full name of the ontology (eg Gene Ontology)  [Optional]
     * @param definition - The definition of the ontology [Optional]
     * @return a fully build Ontology object.
     * @throws IllegalArgumentException if the shortName is null or an empty string
     * @throws IOException              if the URL cannot be read or on parse errors
     */
    public static Ontology loadFromOBOFile(
            final URL oboFileURL, final String shortName, final String fullName, final String definition
    ) throws IOException {

        //test parameters - URL
        if (oboFileURL == null) {
            throw new IOException("The URL to be parsed must not be null.");
        }

        //logging to System.out
        System.out.println("OntologyBean.loadFromOBOFile will read from: " + oboFileURL.toString());

        URLConnection conn = null;
        BufferedReader in = null;
        PrintWriter out = null;
        File tempFile = null;

        try {

            boolean useNumericalProgressDisplay = false;

            // Read URL content
            conn = oboFileURL.openConnection();
            int contentLength = conn.getContentLength();
            if (contentLength > 0) {
                useNumericalProgressDisplay = true;
            }

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()), 4096);

            // Create a temp file and write URL content in it.
            File tempDirectory = new File(System.getProperty("java.io.tmpdir", "tmp"));
            if (!tempDirectory.exists()) {
                if (!tempDirectory.mkdirs()) {
                    throw new IOException("Cannot create temp directory: " + tempDirectory.getAbsolutePath());
                }
            }
            tempFile = File.createTempFile("ontologyBean.", ".obo", tempDirectory);
            tempFile.deleteOnExit();

            out = new PrintWriter(new FileWriter(tempFile));

            //for progress reporting
            int currentSize = 0;
            int currentPct = 0;
            int lineCount = 0;
            int reportInc = 10;
            int lastPct = -1;

            String line;
            while ((line = in.readLine()) != null) {

                //write to file
                out.println(line);

                //if we have content length, display progress as percent of download
                if (useNumericalProgressDisplay) {
                    currentSize += line.length();
                    currentPct = currentSize * 100 / contentLength;

                    if (currentPct % reportInc == 0 && currentPct != lastPct) {
                        System.out.print(currentPct + "% ");
                        System.out.flush();
                        lastPct = currentPct;
                    }
                    //otherwise, display progress as dots on the console
                } else {
                    lineCount++;
                    // display progress bar on STDOUT
                    if ((lineCount % 20) == 0) {
                        System.out.print(".");
                        System.out.flush();
                        if ((lineCount % 500) == 0) {
                            System.out.println("   " + lineCount);
                        }
                    }
                }
            }
            System.out.println();

            //logging to System.out
            System.out.println("OntologyBean.loadFromOBOFile is processing file.");


        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }

        return loadFromOBOFile(tempFile, shortName, fullName, definition);

    }

    /**
     * Facade method to simplify most of the Loader package code for user who simply want to obtain
     * a fully-constructed ontology object from a single OBO source file. This method will produce an
     * object that is still valid for persistence however, so mandatory fields need to be provided.
     *
     * @param oboFile    - The File object to be parsed [Mandatory]
     * @param shortName  - The short label of the ontology (eg "GO", "MI", etc...) [Mandatory]
     * @param fullName   - The full name of the ontology (eg Gene Ontology)  [Optional]
     * @param definition - The definition of the ontology [Optional]
     * @return a fully build Ontology object.
     * @throws IllegalArgumentException if the shortName is null or an empty string
     * @throws IOException              on parse errors
     */
    public static Ontology loadFromOBOFile(final File oboFile, final String shortName, final String fullName, final String definition) throws IOException {

        //test parameters - shortName
        if (shortName == null || (shortName != null && "".equals(shortName.trim()))) {
            throw new IllegalArgumentException("The ShortName of an ontology cannot be null");
        }

        //test parameters - file path
        if (!oboFile.exists()) {
            throw new IOException("The file path to be parsed does not exist");
        }

        Loader loader = new BaseOBO2AbstractLoader() {

            protected void printUsage() { /* ignore for now */ }

            protected void configure() throws DataAdapterException {
                parser = new OBO2FormatParser(oboFile.getAbsolutePath());
//                setUseGreedy(true);
                setUseGreedy(false);
                ONTOLOGY_DEFINITION = definition;
                FULL_NAME = fullName;
                SHORT_NAME = shortName;
            }

            public Ontology getOntology() throws IOException {
                try {
                    configure();
                    //create the object model
                    process();
                    //get the ontology
                    return super.getOntology();
                } catch (IllegalStateException ise) {
                    throw new IOException("There were errors while loading the ontology: " + ise.getMessage());
                } catch (DataAdapterException ioe) {
                    throw new IOException("There were errors while loading the ontology: " + ioe.getMessage());
                }
            }
        };

        return loader.getOntology();

    }

    public static void main(String[] args) {

        try {
            Ontology ont = OntologyBean.loadFromOBOFile("/home/rcote/dev/cvs/ols/ols-core/src/test/resources/obo/phenotype/human_disease.obo", "dfg", "dfg", "sdf");
            Collection<Term> trm = ont.getRootTerms();
            for (Term t : trm) {
                //navigate terms
                System.out.println(t.getIdentifier());
            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}

