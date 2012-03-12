/**
 ** Copyright (c) 2010 Memorial Sloan-Kettering Cancer Center (MSKCC)
 ** and University of Toronto (UofT).
 **
 ** This is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** both UofT and MSKCC have no obligations to provide maintenance, 
 ** support, updates, enhancements or modifications.  In no event shall
 ** UofT or MSKCC be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** UofT or MSKCC have been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this software; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA;
 ** or find it at http://www.fsf.org/ or http://www.gnu.org.
 **/

package org.biopax.validator.utils;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.miriam.MiriamLink;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.result.*;

/**
 * BioPAX (Level 3) Normalizer, an advanced BioPAX utility 
 * to help pathway data integrating and linking.
 * 
 * @author rodch
 *
 */
public final class Normalizer {
	private static final Log log = LogFactory.getLog(Normalizer.class);
	
	private SimpleIOHandler biopaxReader;
	private Validation validation;
	private ShallowCopy copier;
	private NormalizerOptions options;
	private final Map<BioPAXElement,BioPAXElement> subs;
	private Model subsModel;
	
	
	/**
	 * Constructor
	 */
	public Normalizer() {
		biopaxReader = new SimpleIOHandler(BioPAXLevel.L3);
		biopaxReader.mergeDuplicates(true);
		copier = new ShallowCopy(BioPAXLevel.L3);
		options = new NormalizerOptions(); // with default settings
		subs = new HashMap<BioPAXElement, BioPAXElement>();
		subsModel = biopaxReader.getFactory().createModel();
	}

	/**
	 * Constructor
	 * 
	 * @param validation existing validation errors there can be set fixed=true.
	 */
	public Normalizer(Validation validation) {
		this();
		this.validation = validation;
		if(validation.getNormalizerOptions() != null) {
			options = validation.getNormalizerOptions();
		}
	}
		
	
	/**
	 * @return the options
	 */
	public NormalizerOptions getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(NormalizerOptions options) {
		this.options = options;
	}

	/**
	 * Normalizes BioPAX OWL data and returns
	 * the result as BioPAX OWL (string).
	 * 
	 * This public method is actually intended to use 
	 * outside the BioPAX Validator framework.
	 * 
	 * @param biopaxOwlData
	 * @return
	 */
	public String normalize(String biopaxOwlData) {
		
		if(biopaxOwlData == null || biopaxOwlData.length() == 0) 
			throw new IllegalArgumentException("no data. " + extraInfo());
		
		// quick-fix for older BioPAX L3 version (v0.9x) property 'taxonXref' (range: BioSource)
		biopaxOwlData = biopaxOwlData.replaceAll("taxonXref","xref");
		
		// if required, upgrade to L3
		biopaxOwlData = convertToLevel3(biopaxOwlData);
		
		// build the model
		Model model = null;
		try {
			model = biopaxReader.convertFromOWL(
				new ByteArrayInputStream(biopaxOwlData.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Failed! " + extraInfo(), e);
		}
		
		if(model == null || model.getLevel() != BioPAXLevel.L3) {
			throw new IllegalArgumentException("Failed to create Model! " 
					+ extraInfo());
		}
		
		normalize(model); // L3 only!
		
		// return as BioPAX OWL
		return convertToOWL(model);
	}
	

	private void normalizeXrefs(Model model) {
		// use a copy of the xrefs set (to avoid concurrent modif. exception)
		Set<? extends Xref> xrefs = new HashSet<Xref>(model.getObjects(Xref.class));
		for(Xref ref : xrefs) {
			// get database official urn
			String db = ref.getDb();
			String id = ref.getId();
			// workaround a null pointer exception
			if(db == null || "".equals(db)) {
				log.error(ref.getModelInterface().getSimpleName() 
					+ " " + ref.getRDFId() + " - 'db' property is empty! "
					+ extraInfo());
				continue; // skip it
			}
			if(id == null || "".equals(id)) {
				log.error(ref.getModelInterface().getSimpleName() 
					+ " - 'id' property is empty! "
					+ extraInfo());
				continue; // skip it
			}
			
			try {// to update name to the primary one
				db = MiriamLink.getName(db);
				ref.setDb(db);
			} catch (IllegalArgumentException e) {
				if(log.isWarnEnabled())
					log.warn("Unknown db: " + db +
						". Cannot replace " + ref.getDb() +
						" with a standard name! " +
						ref.getRDFId() + "; " + e + "; " + extraInfo());
			}	
			
			String rdfid = generateURIForXref(db, id, ref.getIdVersion(),
					(Class<? extends Xref>) ref.getModelInterface());	
			
			if(rdfid != null)
				addToReplacementMap(model, ref, rdfid);
		}
		
		// update/replace xrefs now
		doSubs(model);
	}	

	
	/**
	 * Consistently builds a "normalized"
	 * Xref URI from given parameters. 
	 * Miriam resource is used to get a standard db name, 
	 * and if it fails, the initial value is still used. 
	 * 
	 * @param db
	 * @param id
	 * @param ver
	 * @param type
	 * @return new ID (URI); not null (unless it's a bug :))
	 * 
	 */
	public static String generateURIForXref(String db, String id, String ver, Class<? extends Xref> type) 
	{
		if(id == null && "".equals(id.trim())) return null;
		if(db == null && "".equals(db.trim())) return null;
		
		String rdfid = null;
		
		// try to use primary standard name if exists -
		try {
			db = MiriamLink.getName(db);
			if(type.equals(PublicationXref.class)
				&& id != null && !"".equals(id.trim())) 
			{
				return MiriamLink.getURI(db, id);
			}
		} catch (IllegalArgumentException e) {
			if(log.isDebugEnabled())
				log.debug("Unknown database name: " + db + ". "  + e);
			
		}
		
		// consistently build a new, standard id (URI)
		String prefix = ModelUtils.uriPrefixForGeneratedXref(type);
		String ending = (ver != null && !"".equals(ver.trim()))
			? "_" + ver.trim() // add the id version/variant
			: ""; // no endings
			
		// add the local part of the URI encoded -
		try {
			rdfid = prefix + URLEncoder
				.encode(db.trim() + "_" + id.trim() + ending, "UTF-8")
					.toUpperCase();
		} catch (UnsupportedEncodingException e) {
			if(log.isWarnEnabled())
				log.warn("ID UTF-8 encoding failed! " +
					"Using the platform default (deprecated method).", e);
			rdfid = prefix + URLEncoder
				.encode(db.trim() + "_" + id.trim() + ending).toUpperCase();
		}

		return rdfid;
	}
	
	
	
	/*
	 * Replaces rdfid; removes the element from the model if (with new rdfid) it becomes duplicate
	 * Note: model loses its integrity (object properties fix is required after this)
	 */
	private void addToReplacementMap(Model model, BioPAXElement bpe, String newRdfid) 
	{
		// model has another object with the same (new) ID?
		if(model.containsID(newRdfid)) {
			// replace with the existing element (having new id)
			subs.put(bpe, model.getByID(newRdfid));
		} else if(subsModel.containsID(newRdfid)) {
			subs.put(bpe, subsModel.getByID(newRdfid));
		} else {
			// replace with its own copy that has new id
			BioPAXElement copy = copier.copy(bpe, newRdfid);
			subs.put(bpe, copy);
			subsModel.add(copy);
		}
	}

	
	private String extraInfo() {
		return (validation != null) ? validation.getDescription() : "";
	}
	
	
	/**
	 * Sets Miriam standard URI (if possible) for a utility object 
	 * (but not for *Xref!); also removes duplicates...
	 * 
	 * @param model the BioPAX model
	 * @param bpe a utility class element, except for xref, to normalize
	 * @param db official database name or synonym (that of bpe's unification xref)
	 * @param id identifier (if null, new ID will be that of the Miriam Data Type; this is mainly for Provenance)
	 * @param idExt id suffix
	 */
	private void normalizeID(Model model, UtilityClass bpe, String db, String id, String idExt) 
	{	
		if(bpe instanceof Xref) {
			log.error("normalizeID is not supposed to " +
				"be called for Xrefs (hey, this is a bug!). "
				+ extraInfo());
			return;
		}
		
		// get the standard ID
		String urn = null;
		try {
			// make a new ID for the element
			if(id != null)
				urn = MiriamLink.getURI(db, id);
			else 
				urn = MiriamLink.getDataTypeURI(db);
		} catch (Exception e) {
			log.error("Cannot get a Miriam standard ID for " + bpe 
				+ " (" + bpe.getModelInterface().getSimpleName()
				+ ") " + ", using " + db + ":" + id 
				+ ". " + e + ". " + extraInfo());
			return;
		}
		
		if (bpe instanceof SmallMoleculeReference
				&& db.trim().equalsIgnoreCase("chebi")) 
		{
			// A special case, shortcut, for the ChEBI SMR case
			urn = "urn:miriam:";
			// correction for missing 'chebi:' prefix
			// (if it's not already fixed by the validator...)
			String suf = id.trim().toLowerCase();
			urn += ((suf.startsWith("chebi:")) ? suf : "chebi:" + suf);
		} else {
			if (idExt != null && !"".equals(idExt.trim())) {
				try {
					urn += "_" + URLEncoder.encode(idExt, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					log.error("UTF-8 encoding failed for (idVersion): " + idExt
							+ "! " + e + ". " + extraInfo());
				}
			}
		}
		
		// if different id, edit the element
		if(urn != null)
			addToReplacementMap(model, bpe, urn);
	}
	
	
	private void fixDisplayName(Model model) {
		if (log.isInfoEnabled())
			log.info("Trying to auto-fix 'null' displayName...");
		// where it's null, set to the shortest name if possible
		for (Named e : model.getObjects(Named.class)) {
			if (e.getDisplayName() == null) {
				if (e.getStandardName() != null) {
					e.setDisplayName(e.getStandardName());
					if (log.isInfoEnabled())
						log.info(e + " displayName auto-fix: "
								+ e.getDisplayName() + ". " + extraInfo());
					Validation.setFixed(validation, BiopaxValidatorUtils.getId(e), 
						"displayNameRule", "no.display.name", null);
				} else if (!e.getName().isEmpty()) {
					String dsp = e.getName().iterator().next();
					for (String name : e.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					e.setDisplayName(dsp);
					if (log.isInfoEnabled())
						log.info(e + " displayName auto-fix: " + dsp
							+ ". " + extraInfo());
					Validation.setFixed(validation, BiopaxValidatorUtils.getId(e), 
						"displayNameRule", "no.display.name", null);
				}
			}
		}
		// if required, set PE name to (already fixed) ER's name...
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
				if(spe.getDisplayName() == null || spe.getDisplayName().trim().length() == 0) {
					if(er.getDisplayName() != null && er.getDisplayName().trim().length() > 0) {
						spe.setDisplayName(er.getDisplayName());
						Validation.setFixed(validation, BiopaxValidatorUtils.getId(spe), 
							"displayNameRule", "no.display.name", null);
					}
				}
			}
		}
	}


	private String convertToOWL(Model model) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		(new SimpleIOHandler(model.getLevel())).convertToOWL(model, out);
		return out.toString();
	}


	private List<UnificationXref> getUnificationXrefsSorted(XReferrable referrable) {
		List<UnificationXref> urefs = new ArrayList<UnificationXref>(
			new ClassFilterSet<Xref,UnificationXref>(
				referrable.getXref(), UnificationXref.class)
		);	
		
		Comparator<UnificationXref> comparator = new Comparator<UnificationXref>() {
			@Override
			public int compare(UnificationXref o1, UnificationXref o2) {
				String s1 = o1.getDb() + o1.getId();
				String s2 = o2.getDb() + o2.getId();
				return s1.compareTo(s2);
			}
		};
		
		Collections.sort(urefs, comparator);
		
		return urefs;
	}

	
	/*
	 * Gets the first one, the set is not empty, or null.
	 */
	private UnificationXref getFirstUnificationXref(XReferrable xr) 
	{
		List<UnificationXref> urefs = getUnificationXrefsSorted(xr);
		UnificationXref toReturn = null;
		for(UnificationXref uref : urefs) 
		{
			if(uref.getDb() == null || uref.getId() == null) {
				// report error, skip
				log.error("UnificationXref's properties 'db' or 'id' " +
					"cannot be null: " + uref + ", " + uref.getRDFId()
					+ ". " + extraInfo());
			} else {
				toReturn = uref;
				break;
			}
		}
		return toReturn;
	}

	
	/*
	 * The first uniprot or enterz gene xref, if exists, will be returned;
	 * otherwise, the first one of any kind is the answer.
	 */
	private UnificationXref getFirstUnificationXrefOfEr(EntityReference er) 
	{
		UnificationXref toReturn = null;
		
		for(UnificationXref uref : getUnificationXrefsSorted(er)) 
		{
			if(uref.getDb() == null || uref.getId() == null) {
				// report error, skip
				log.error("UnificationXref's properties 'db' or 'id' " +
					"cannot be null: " + uref + ", " + uref.getRDFId()
					+ ". " + extraInfo());
			} 
			else if(uref.getDb().toLowerCase().startsWith("uniprot") 
				|| uref.getDb().toLowerCase().startsWith("entrez")
				|| uref.getDb().toLowerCase().startsWith("chebi")) {
				toReturn = uref;
				break;
			} else if(toReturn == null) {
				// if not already done, pick up the first one for now...
				toReturn = uref; 
				// - may be re-assigned later if there are uniprot/entrez ones
			}
		}

		return toReturn;
	}


	/**
	 * BioPAX normalization 
	 * (modifies the original Model!)
	 * 
	 * @param model
	 */
	public void normalize(Model model) {
		/* save curr. state;
		 * disable error reporting: it generates artifact errors (via AOP)
		 * during the normalization, because the model is being edited!
		 */
		Behavior threshold = null;
		if(validation != null) {
			threshold = validation.getThreshold();
			validation.setThreshold(Behavior.IGNORE);
		}
		
		// clean/normalize xrefs first, because they gets used next!
		if(log.isInfoEnabled())
			log.info("Normalizing xrefs..." + extraInfo());
		normalizeXrefs(model);
		
		// fix displayName where possible
		if(options.fixDisplayName) {
			if(log.isInfoEnabled())
				log.info("Normalizing display names..." + extraInfo());
			fixDisplayName(model);
		}
			
		if(log.isInfoEnabled())
			log.info("Normalizing CVs and organisms..." + extraInfo());
		normalizeCVsAndBioSource(model);
		if(log.isInfoEnabled())
			log.info("Normalizing data sources (Provenance)..." + extraInfo());
		normalizeProvenance(model);
		if(log.isInfoEnabled())
			log.info("Normalizing entity references..." + extraInfo());
		normalizeERs(model);
		
		// find/add lost (in replace) children
		if(log.isInfoEnabled())
			log.info("Repairing..." + extraInfo());
		model.repair(); // it does not remove dangling utility class objects (can be done separately, later, if needed)
		
		if(log.isInfoEnabled())
			log.info("Optional tasks (reasoning)..." + extraInfo());
		
		ModelUtils mu = new ModelUtils(model);
		
		// auto-set dataSource property for all entities (top-down)
		if(options.inferPropertyDataSource) {
			mu.inferPropertyFromParent("dataSource");//, Entity.class);
		}
		
		if(options.inferPropertyOrganism) {
			mu.inferPropertyFromParent("organism");//, Gene.class, SequenceEntityReference.class, Pathway.class);
		}
		
		if(options.generateRelatioshipToPathwayXrefs) {
			mu.generateEntityProcessXrefs(Pathway.class);
		} 
			
		if(options.generateRelatioshipToInteractionXrefs) {
			mu.generateEntityProcessXrefs(Interaction.class);
		} 
		
		// the following two tasks better do AFTER inferPropertyOrganism (if enabled)
		if(options.generateRelatioshipToOrganismXrefs) {
			mu.generateEntityOrganismXrefs();
		} 
		
		/* 
		 * We could also "fix" organism property, where it's null,
		 * a swell (e.g., using the value from the pathway);
		 * also - check those values in protein references actually
		 * correspond to what can be found in the UniProt by using
		 * unification xrefs's 'id'... But this, fortunately, 
		 * happens in the CPathMerger (a ProteinReference 
		 * comes from the Warehouse with organism property already set!)
		 */
		
		// restore validation state, if any -
		if(validation != null) {
			validation.setThreshold(threshold);
		}
	}

	
	private void normalizeCVsAndBioSource(Model model) {
		// process the rest of utility classes (selectively though)
		for(UtilityClass bpe : model.getObjects(UtilityClass.class)) 
		{
			if(bpe instanceof ControlledVocabulary || bpe instanceof BioSource) 
			{
				//note: it does not check/fix the CV term name if wrong or missing though...
				UnificationXref uref = getFirstUnificationXref((XReferrable) bpe);
				if (uref != null) 
					normalizeID(model, bpe, uref.getDb(), uref.getId(), null); // no idVersion for a CV or BS!
				else 
					if(log.isInfoEnabled())
						log.info("Cannot normalize " + bpe.getModelInterface().getSimpleName() 
							+ " : no unification xrefs found in " + bpe.getRDFId()
							+ ". " + extraInfo());
			} 
		}
		
		// replace/update elements in the model
		doSubs(model);
	}
	
	private void normalizeERs(Model model) {
		// process the rest of utility classes (selectively though)
		for (EntityReference bpe : model.getObjects(EntityReference.class)) {
			UnificationXref uref = getFirstUnificationXrefOfEr((EntityReference) bpe);
			if (uref != null) // not using idVersion!..
				normalizeID(model, bpe, uref.getDb(), uref.getId(), null); 
			else if (log.isInfoEnabled())
				log.info("Cannot normalize EntityReference: "
						+ "no unification xrefs found in " + bpe.getRDFId()
						+ ". " + extraInfo());
		}
		
		// replace/update elements in the model
		doSubs(model);
	}
	
	private void normalizeProvenance(Model model) {
		// process the rest of utility classes (selectively though)
		for(Provenance pro : model.getObjects(Provenance.class)) 
		{
			autoName(pro); // throws IAE (from MiriamLink)
			normalizeID(model, pro, pro.getStandardName(), null, null);
		}
		
		// replace/update elements in the model
		doSubs(model);
	}

	/**
	 * Executes the batch replace/update 
	 * to the normalized equivalent objects.
	 * 
	 * @param model
	 */
	private void doSubs(Model model) {
		ModelUtils mu = new ModelUtils(model);
		for(BioPAXElement e : subs.keySet()) {
			model.remove(e);
		}

		try {
			mu.replace(subs);
		} catch (Exception e) {
			log.error("Failed to replace IDs. " + extraInfo(), e);
			return;
		}
		
		for(BioPAXElement e : subs.values()) {
			if(!model.contains(e))
				model.add(e);
		}
		subs.clear();
		subsModel = biopaxReader.getFactory().createModel();
	}

	
	/**
	 * Auto-generates standard and other names for the datasource
	 * from either its ID (if URN) or one of its existing names (preferably - standard name)
	 * 
	 * @param pro
	 */
	public static void autoName(Provenance pro) {
		if(!pro.getRDFId().startsWith("urn:miriam:") && pro.getName().isEmpty()) {
			if(log.isInfoEnabled())
				log.info("Cannot generate names from ID/name of Provenance: " + pro.getRDFId());
			
		}
		else { // i.e., 'name' is not empty or ID is the URN
			final SortedSet<String> names = new TreeSet<String>();
			
			String key = null;
			if(pro.getRDFId().startsWith("urn:miriam:")) {
				key = pro.getRDFId();
			} else if (pro.getStandardName() != null) {
				key = pro.getStandardName();
			} else {
				key = pro.getDisplayName(); // can be null
			}
			
			if (key != null) {
				try {
					names.addAll(Arrays.asList(MiriamLink.getNames(key)));
					pro.setStandardName(MiriamLink.getName(key));
					// get the datasource description
					String description = MiriamLink.getDataTypeDef(pro.getStandardName());
					pro.addComment(description);
				} catch (IllegalArgumentException e) {
					// ignore (then, names is still empty...)
				}
			} 
			
			// when the above failed (no match in Miriam), or key was null -
			if(names.isEmpty()) {
				// finally, trying to find all valid names for each existing one
					for (String name : pro.getName()) {
						try {
							names.addAll(Arrays.asList(MiriamLink.getNames(name)));
						} catch (IllegalArgumentException e) {
							// ignore
						}
					}
					// pick up the first name, get the standard name
					if(!names.isEmpty())
						pro.setStandardName(MiriamLink
							.getName(names.iterator().next()));
			}
			
			// and add all the synonyms if any
			for(String name : names)
				pro.addName(name);
			
			//set display name if not set (standard name is set already)
			if(pro.getDisplayName() == null)
				pro.setDisplayName(pro.getStandardName());			
		}
	}
	
	/**
	 * Converts biopax l2 string to biopax l3 if it's required
	 *
	 * @param biopaxData String
	 * @return
	 */
	private String convertToLevel3(final String biopaxData) {
		String toReturn = "";
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream is = new ByteArrayInputStream(biopaxData.getBytes());
			SimpleIOHandler io = new SimpleIOHandler();
			io.mergeDuplicates(true);
			Model model = io.convertFromOWL(is);
			if (model.getLevel() != BioPAXLevel.L3) {
				if (log.isInfoEnabled())
					log.info("Converting to BioPAX Level3... " + extraInfo());
				model = (new OneTwoThree()).filter(model);
				if (model != null) {
					io.setFactory(model.getLevel().getDefaultFactory());
					io.convertToOWL(model, os);
					toReturn = os.toString();
				}
			} else {
				toReturn = biopaxData;
			}
		} catch(Exception e) {
			throw new RuntimeException(
				"Failed to read data or convert to L3! "
					+ extraInfo(), e);
		}

		return toReturn;
	}
	
	
	/**
	 * This nested class allows to set extra 
	 * normalization options
	 * (it follows builder design pattern).
	 *
	 */
	public static class NormalizerOptions{
		boolean fixDisplayName = true;
		boolean inferPropertyOrganism = true;
		boolean inferPropertyDataSource = true;
		boolean generateRelatioshipToPathwayXrefs = false;
		boolean generateRelatioshipToInteractionXrefs = false;
		boolean generateRelatioshipToOrganismXrefs = false;
		//TODO add a "remove utility class duplicates/clones" option
		
		public boolean isFixDisplayName() {
			return fixDisplayName;
		}
		public void setFixDisplayName(boolean fixDisplayName) {
			this.fixDisplayName = fixDisplayName;
		}
		public boolean isInferPropertyOrganism() {
			return inferPropertyOrganism;
		}
		public void setInferPropertyOrganism(boolean inferPropertyOrganism) {
			this.inferPropertyOrganism = inferPropertyOrganism;
		}
		public boolean isInferPropertyDataSource() {
			return inferPropertyDataSource;
		}
		public void setInferPropertyDataSource(boolean inferPropertyDataSource) {
			this.inferPropertyDataSource = inferPropertyDataSource;
		}
		public boolean isGenerateRelatioshipToPathwayXrefs() {
			return generateRelatioshipToPathwayXrefs;
		}
		public void setGenerateRelatioshipToPathwayXrefs(
				boolean generateRelatioshipToPathwayXrefs) {
			this.generateRelatioshipToPathwayXrefs = generateRelatioshipToPathwayXrefs;
		}
		public boolean isGenerateRelatioshipToInteractionXrefs() {
			return generateRelatioshipToInteractionXrefs;
		}
		public void setGenerateRelatioshipToInteractionXrefs(
				boolean generateRelatioshipToInteractionXrefs) {
			this.generateRelatioshipToInteractionXrefs = generateRelatioshipToInteractionXrefs;
		}
		public boolean isGenerateRelatioshipToOrganismXrefs() {
			return generateRelatioshipToOrganismXrefs;
		}
		public void setGenerateRelatioshipToOrganismXrefs(
				boolean generateRelatioshipToOrganismXrefs) {
			this.generateRelatioshipToOrganismXrefs = generateRelatioshipToOrganismXrefs;
		}
	}
	
}
