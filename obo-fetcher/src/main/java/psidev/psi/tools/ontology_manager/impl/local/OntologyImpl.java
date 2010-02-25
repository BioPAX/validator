package psidev.psi.tools.ontology_manager.impl.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.util.*;

/**
 * Holder for an Ontology and provide basic search feature.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: OntologyImpl.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>04-Jan-2006</pre>
 */
public class OntologyImpl implements Ontology {

    public static final Log log = LogFactory.getLog( OntologyImpl.class );

    ///////////////////////////////
    // Instance variables

    /**
     * Pool of all term contained in that ontology.
     */
    private Collection<OntologyTermI> ontologyTerms = new ArrayList<OntologyTermI>( 1024 );

    // TODO introduce an interface for querying/updating the relationship
    // TODO replace the hashmap by a Lucene index -> using a different interface !!

    /**
     * Represent the relationship: child -> parents.
     */
    private final Map<OntologyTermI, Set<OntologyTermI>> parents = new HashMap<OntologyTermI, Set<OntologyTermI>>();

    /**
     * Represent the relationship: parent -> children.
     */
    private final Map<OntologyTermI, Set<OntologyTermI>> children = new HashMap<OntologyTermI, Set<OntologyTermI>>();

    /**
     * Mapping of all OboTerm by their ID.
     */
    private Map<String, OntologyTermI> id2ontologyTerm = new HashMap<String, OntologyTermI>( 1024 );

    /**
     * Collection of root terms of that ontology. A root term is defined as follow: term having no parent.
     */
    private Collection<OntologyTermI> roots = null;

    /**
     * List of all obsolete term found while loading the ontology.
     */
    private Collection<OntologyTermI> obsoleteTerms = new ArrayList<OntologyTermI>();

    /////////////////////////////
    // Public methods

    /**
     * Add a new Term in the pool. It will be indexed by its ID.
     *
     * @param term the OntologyTerm to add in that Ontology.
     */
    public void addTerm( OntologyTermI term ) {

        ontologyTerms.add( term );
        String id = term.getTermAccession();
        if ( id2ontologyTerm.containsKey( id ) ) {
            OntologyTermI old = id2ontologyTerm.get( id );
            if( log.isWarnEnabled() ) {
                log.error( "WARNING: 2 Objects have the same ID (" + id + "), the old one is being replaced. old: " + old.getPreferredName() + " new: " + term.getPreferredName() );
            }
        }

        id2ontologyTerm.put( id, term );

        flushRootsCache();
    }

    /**
     * Create a relashionship parent to child between two OntologyTerm.
     *
     * @param parentId The parent term.
     * @param childId  The child term.
     */
    public void addLink( String parentId, String childId ) {

        OntologyTermI child = id2ontologyTerm.get( childId );
        OntologyTermI parent = id2ontologyTerm.get( parentId );

        if ( child == null ) {
            throw new NullPointerException( "You must give a non null child" );
        }

        if ( parent == null ) {
            throw new NullPointerException( "You must give a non null parent" );
        }

        if ( !children.containsKey( parent ) ) {
            children.put( parent, new HashSet<OntologyTermI>() );
        }

        if ( !parents.containsKey( child ) ) {
            parents.put( child, new HashSet<OntologyTermI>() );
        }

        children.get( parent ).add( child );
        parents.get( child ).add( parent );

        flushRootsCache();
    }

    /**
     * Remove the Root cache from memory.<br/> That method should be called every time the collection of OntologyTerm is
     * altered.
     */
    private void flushRootsCache() {
        if ( roots != null ) {
            // flush roots cache
            roots.clear();
            roots = null;
        }
    }

    /**
     * Answer the question: 'Has that ontology any term loaded ?'.
     *
     * @return true is there are any terms loaded, false otherwise.
     */
    public boolean hasTerms() {
        return ontologyTerms.isEmpty();
    }

    /**
     * Search for a OboTerm by its ID.
     *
     * @param id the identifier of the OntologyTerm we are looking for.
     * @return a OntologyTerm or null if not found.
     */
    public OntologyTermI search( String id ) {
        return id2ontologyTerm.get( id );
    }

    public boolean hasParent( OntologyTermI term ) {
        return parents.containsKey( term );
    }

    /**
     * Get the Root terms of the ontology. The way to get it is as follow: pick a term at random, and go to his highest
     * parent.
     *
     * @return a collection of Root term.
     */
    public Collection<OntologyTermI> getRoots() {

        if ( roots != null ) {
            return roots;
        }

        // it wasn't precalculated, then do it here...
        roots = new HashSet<OntologyTermI>();

        for ( Iterator iterator = ontologyTerms.iterator(); iterator.hasNext(); ) {
            OntologyTermI ontologyTerm = ( OntologyTermI ) iterator.next();

            if ( !hasParent( ontologyTerm ) ) {
                roots.add( ontologyTerm );
            }
        }

        if ( roots.isEmpty() ) {
            return Collections.EMPTY_LIST;
        }

        return roots;
    }

    /**
     * Get all OboTerm.
     *
     * @return all Ontology term found in the Ontology.
     */
    public Collection<OntologyTermI> getOntologyTerms() {
        return Collections.unmodifiableCollection( ontologyTerms );
    }

    public void addObsoleteTerm( OntologyTermI term ) {
        if ( term == null ) {
            throw new IllegalArgumentException( "You must give a non null term" );
        }
        if ( log.isDebugEnabled() ) {
            log.debug( "Adding obsolete term: " + term.getTermAccession() + " " + term.getPreferredName() );
        }
        obsoleteTerms.add( term );
    }

    public boolean isObsoleteTerm( OntologyTermI term ) {
        return obsoleteTerms.contains( term );
    }

    /**
     * Go through the list of all CV Term and select those that are obsolete.
     *
     * @return a non null Collection of obsolete term.
     */
    public Collection<OntologyTermI> getObsoleteTerms() {
        return Collections.unmodifiableCollection( obsoleteTerms );
    }

    public Set<OntologyTermI> getDirectParents( OntologyTermI term ) {
        final Set<OntologyTermI> directParents = parents.get( term );
        if ( directParents == null ) {
            return Collections.EMPTY_SET;
        } else {
            return directParents;
        }
    }

    public Set<OntologyTermI> getDirectChildren( OntologyTermI term ) {
        final Set<OntologyTermI> directChildren = children.get( term );
        if ( directChildren == null ) {
            return Collections.EMPTY_SET;
        } else {
            return directChildren;
        }
    }

    public Set<OntologyTermI> getAllParents( OntologyTermI term ) {
        Set<OntologyTermI> parents = new HashSet<OntologyTermI>();
        getAllParents( term, parents );
        return parents;
    }

    private void getAllParents( OntologyTermI term, Set<OntologyTermI> parents ) {
        final Collection<OntologyTermI> directParents = getDirectParents( term );
        parents.addAll( directParents );
        for ( OntologyTermI parent : directParents ) {
            getAllParents( parent, parents );
        }
    }

    public Set<OntologyTermI> getAllChildren( OntologyTermI term ) {
        Set<OntologyTermI> children = new HashSet<OntologyTermI>();
        getAllChildren( term, children );
        return children;
    }

    private void getAllChildren( OntologyTermI term, Set<OntologyTermI> children ) {
         getAllChildren( "", term, children, new HashSet(512) );
    }

    private void getAllChildren( String prefix, OntologyTermI term, Set<OntologyTermI> children, Set<String> traversedChildren ) {
        if( traversedChildren.contains( term.getTermAccession() ) ) {
//            System.out.println( prefix.replaceAll( " ", "#" )+" > "+ term.getTermAccession() +" / "+ term.getPreferredName() +" )" );
            return;
        } else {
//            System.out.println( prefix +" > "+ term.getTermAccession() +" / "+ term.getPreferredName() +" )" );
        }
        final Collection<OntologyTermI> directChildren = getDirectChildren( term );
        traversedChildren.add( term.getTermAccession() );
        children.addAll( directChildren );
        for ( OntologyTermI child : directChildren ) {
            getAllChildren( prefix+"     ", child, children, traversedChildren );
        }
    }

    /////////////////////////////////
    // Utility - Display methods

    public void print() {
        log.info( ontologyTerms.size() + " terms to display." );
        final Collection<OntologyTermI> roots = getRoots();
        if ( log.isDebugEnabled() ) {
            log.info( this.roots.size() + " root(s) found." );
        }
        for ( OntologyTermI root : roots ) {
            print( root );
        }
    }

    private void print( OntologyTermI term, String indent ) {

        log.info( indent + term.getTermAccession() + "   " + term.getPreferredName() );
        for ( OntologyTermI child : getDirectChildren( term ) ) {
            print( child, indent + "  " );
        }
    }

    public void print( OntologyTermI term ) {
        print( term, "" );
    }
}