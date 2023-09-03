package org.biopax.psidev.ontology_manager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyTermI;

import java.io.Serializable;
import java.util.*;

/**
 * Access to a local ontology in the form of an OBO file.
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baderlab.org) - re-factored/simplified for the BioPAX validator
 * @since 2.0.0
 */
public class OntologyAccessImpl implements OntologyAccess, Serializable {

    private static final long serialVersionUID = 1L;

    public static final Log log = LogFactory.getLog( OntologyAccessImpl.class );

    private String name;

    public OntologyAccessImpl() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<OntologyTermI> getValidTerms( String accession, boolean allowChildren, boolean useTerm ) {
        Set<OntologyTermI> collectedTerms = new HashSet<>();

        final OntologyTermI term = getTermForAccession( accession );
        if ( term != null ) {
            if ( useTerm ) {
                collectedTerms.add( term );
            }

            if ( allowChildren ) {
                collectedTerms.addAll( getAllChildren( term ) );
            }
        }

        return collectedTerms;
    }


    /**
     * Pool of all term contained in that ontology.
     */
    private Collection<OntologyTermI> ontologyTerms = new ArrayList<>( 1024 );

    /**
     * Represent the relationship: child -> parents.
     */
    private final Map<OntologyTermI, Set<OntologyTermI>> parents = new HashMap<>();

    /**
     * Represent the relationship: parent -> children.
     */
    private final Map<OntologyTermI, Set<OntologyTermI>> children = new HashMap<>();

    /**
     * Mapping of all OboTerm by their ID.
     */
    private Map<String, OntologyTermI> id2ontologyTerm = new HashMap<>( 1024 );

    /**
     * Collection of root terms of that ontology. A root term is a term having no parent.
     */
    private Collection<OntologyTermI> roots = null;

    /**
     * List of all obsolete term found while loading the ontology.
     */
    private Collection<OntologyTermI> obsoleteTerms = new ArrayList<>();

    /**
     * Add a new Term in the pool. It will be indexed by its ID.
     *
     * @param term the OntologyTerm to add in that OntologyAccess.
     */
    public void addTerm( OntologyTermI term ) {
        ontologyTerms.add( term );
        String id = term.getTermAccession();
        if ( id2ontologyTerm.containsKey( id ) ) {
            OntologyTermI old = id2ontologyTerm.get( id );
            log.error( "WARNING: 2 Objects have the same ID (" + id
              + "), the old one is being replaced. old: "
              + old.getPreferredName() + " new: " + term.getPreferredName() );
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
    public void addLink(String parentId, String childId) {
        OntologyTermI child = id2ontologyTerm.get(childId);
        OntologyTermI parent = id2ontologyTerm.get(parentId);
        if (child == null || parent == null) {
            throw new NullPointerException("You must give a non null " +
              "child/parent for addLink method!");
        } else {
            if (!children.containsKey(parent)) {
                children.put(parent, new HashSet<>());
            }
            if (!parents.containsKey(child)) {
                parents.put(child, new HashSet<>());
            }
            children.get(parent).add(child);
            parents.get(child).add(parent);
            flushRootsCache();
        }
    }

    /**
     * Remove the Root cache from memory.<br/> That method should be called
     * every time the collection of OntologyTerm is altered.
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
        roots = new HashSet<>();

        for ( Iterator iterator = ontologyTerms.iterator(); iterator.hasNext(); ) {
            OntologyTermI ontologyTerm = ( OntologyTermI ) iterator.next();
            if ( !hasParent( ontologyTerm ) ) {
                roots.add( ontologyTerm );
            }
        }

        if ( roots.isEmpty() ) {
            return Collections.emptyList();
        }

        return roots;
    }

    /**
     * Get all OboTerm.
     *
     * @return all OntologyAccess term found in the OntologyAccess.
     */
    public Collection<OntologyTermI> getOntologyTerms() {
        return Collections.unmodifiableCollection( ontologyTerms );
    }

    public void addObsoleteTerm( OntologyTermI term ) {
        if ( term == null ) {
            throw new IllegalArgumentException( "You must give a non null term" );
        }

        log.debug( "Adding obsolete term: " + term.getTermAccession() + " " + term.getPreferredName() );
        obsoleteTerms.add( term );
    }

    public boolean isObsolete( OntologyTermI term ) {
        return obsoleteTerms.contains( term );
    }

    /**
     * Go through the list of all CV Term and select those that are obsolete.
     *
     * @return a non-null Collection of obsolete term.
     */
    public Collection<OntologyTermI> getObsoleteTerms() {
        return Collections.unmodifiableCollection( obsoleteTerms );
    }

    public Set<OntologyTermI> getDirectParents( OntologyTermI term ) {
        final Set<OntologyTermI> directParents = parents.get( term );
        if ( directParents == null ) {
            return Collections.emptySet();
        } else {
            return directParents;
        }
    }

    public Set<OntologyTermI> getDirectChildren( OntologyTermI term ) {
        final Set<OntologyTermI> directChildren = children.get( term );
        if ( directChildren == null ) {
            return Collections.emptySet();
        } else {
            return directChildren;
        }
    }

    public Set<OntologyTermI> getAllParents( OntologyTermI term ) {
        Set<OntologyTermI> parents = new HashSet<>();
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
        Set<OntologyTermI> children = new HashSet<>();
        getAllChildren( term, children );
        return children;
    }

    private void getAllChildren( OntologyTermI term, Set<OntologyTermI> children ) {
        getAllChildren( "", term, children, new HashSet(512) );
    }

    private void getAllChildren( String prefix, OntologyTermI term, Set<OntologyTermI> children,
                                 Set<String> traversedChildren )
    {
        if( traversedChildren.contains( term.getTermAccession() ) ) {
            return;
        }
        final Collection<OntologyTermI> directChildren = getDirectChildren( term );
        traversedChildren.add( term.getTermAccession() );
        children.addAll( directChildren );
        for ( OntologyTermI child : directChildren ) {
            getAllChildren( prefix+"     ", child, children, traversedChildren );
        }
    }

    public OntologyTermI getTermForAccession(String accession) {
        return search(accession);
    }
}
