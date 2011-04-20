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
public class Normalizer {
	private static final Log log = LogFactory.getLog(Normalizer.class);
	
	/* 
	 * URI namespace prefix for the utility class IDs 
	 * generated during data convertion, normalization, merge...
	 */
	public static final String BIOPAX_URI_PREFIX = "urn:biopax:";
	
	private SimpleIOHandler biopaxReader;
	private Validation validation;
	private ShallowCopy copier;
	private NormalizerOptions options;
	
	
	/**
	 * Constructor
	 */
	public Normalizer() {
		biopaxReader = new SimpleIOHandler(BioPAXLevel.L3);
		biopaxReader.mergeDuplicates(true);
		copier = new ShallowCopy(BioPAXLevel.L3);
		options = new NormalizerOptions(); // with default settings
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
			String name = ref.getDb();
			
			// workaround a null pointer exception
			if(name == null || "".equals(name)) {
				log.error(ref.getModelInterface().getSimpleName() 
					+ " " + ref + " - 'db' property is empty! "
						+ extraInfo());
				continue;
			}
			
			try {
				// update name to the primary one
				name = MiriamLink.getName(name);
				ref.setDb(name);
			} catch (IllegalArgumentException e) {
				log.error("Unknown or misspelled database name! " +
					e + ". " + extraInfo());
			}
			
			try {
				String rdfid; 
				
				if(ref instanceof PublicationXref) {
					rdfid = MiriamLink.getURI(name, ref.getId());
				} else {
					// consistently build a new, standard id (URI)
					String prefix = BIOPAX_URI_PREFIX + ref.getModelInterface().getSimpleName() + ":";
					String ending = (ref.getIdVersion() != null && !"".equals(ref.getIdVersion().trim()))
							? "_" + ref.getIdVersion() // add the id version/variant
									: ""; // no endings
					// add the local (last) part of the URI encoded -
					rdfid = prefix + URLEncoder.encode(name + "_" + ref.getId() + ending, "UTF-8")
						.toUpperCase();
				}
				
				// if different id, - begin updating
				if(!rdfid.equals(ref.getRDFId())) {
					updateOrRemove(model, ref, rdfid);
				}
			} catch (Exception e) {
				log.error("Failed to create RDFID from xref: " +
						ref + "! " + e + ". " + extraInfo());
			}
		}
	}	

	
	/*
	 * Replaces rdfid; removes the element from the model if (with new rdfid) it becomes duplicate
	 * Note: model loses its integrity (object properties fix is required after this)
	 */
	private void updateOrRemove(Model model, BioPAXElement bpe, String newRdfid) {
		// model has another object with the same (new) ID?
		
		if(model.containsID(newRdfid)) {
			// replace with the existing element (having new id)
			model.replace(bpe, model.getByID(newRdfid));
		} else {
			// replace with its own copy that has new id
			BioPAXElement copy = copier.copy(bpe, newRdfid);
			model.replace(bpe, copy);
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
		
		if(idExt != null && !"".equals(idExt.trim())) {
			try {
				urn += "_" + URLEncoder.encode(idExt, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("UTF-8 encoding failed for (idVersion): " +
						idExt + "! " + e + ". " + extraInfo());
			}
		}
		
		// if different id, edit the element
		if (!urn.equals(bpe.getRDFId())) {
			try {
				updateOrRemove(model, bpe, urn);
			} catch (Exception e) {
				log.error("Failed to replace ID of " + bpe + " ("
						+ bpe.getModelInterface().getSimpleName() + ") with '"
						+ urn + "'. " + e + ". " + extraInfo());
				return;
			}
		}
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
			new ClassFilterSet<UnificationXref>(referrable.getXref(), UnificationXref.class)
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
				|| uref.getDb().toLowerCase().startsWith("entrez")) {
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
		normalizeXrefs(model);
		
		// fix displayName where possible
		if(options.fixDisplayName) {
			fixDisplayName(model);
		}
				
		// copy
		Set<? extends UtilityClass> objects = 
			new HashSet<UtilityClass>(model.getObjects(UtilityClass.class));
		// process the rest of utility classes (selectively though)
		for(UtilityClass bpe : objects) 
		{
			if(bpe instanceof ControlledVocabulary || bpe instanceof BioSource) 
			{
				//note: it does not check/fix the CV term name if wrong or missing though...
				
				UnificationXref uref = getFirstUnificationXref((XReferrable) bpe);
				if (uref != null) 
					normalizeID(model, bpe, uref.getDb(), uref.getId(), null); // no idVersion for CVs!
				else 
					if(log.isInfoEnabled())
						log.info("Cannot normalize ControlledVocabulary: " +
							"no unification xrefs found in " + bpe.getRDFId()
							+ ". " + extraInfo());
			} else if(bpe instanceof EntityReference) {
				UnificationXref uref = getFirstUnificationXrefOfEr((EntityReference) bpe);
				if (uref != null) 
					normalizeID(model, bpe, uref.getDb(), uref.getId(), null); // not using idVersion!..
				else 
					if(log.isInfoEnabled())
						log.info("Cannot normalize EntityReference: " +
							"no unification xrefs found in " + bpe.getRDFId()
							+ ". " + extraInfo());
			} else if(bpe instanceof Provenance) {
				normalizeProvenance((Provenance) bpe, model);
			}
		}
		
		// update properties/find children (self-merge)
		model.repair();
		
		// auto-set dataSource property for all entities (top-down)
		ModelUtils mu = new ModelUtils(model);
		if(options.inferPropertyDataSource) {
			mu.inferPropertyFromParent("dataSource");//, Entity.class);
		}
		
		if(options.inferPropertyOrganism) {
			mu.inferPropertyFromParent("organism");//, Gene.class, SequenceEntityReference.class, Pathway.class);
		}
		
		if(options.generateRelatioshipToPathwayXrefs) {
			mu.generateEntityProcessXrefs(Pathway.class, null);
		} 
		
		if(options.generateRelatioshipToPathwayComments) {
			mu.generateEntityProcessComments(Pathway.class);
		}
			
		if(options.generateRelatioshipToInteractionXrefs) {
			mu.generateEntityProcessXrefs(Interaction.class, null);
		} 
		
		if(options.generateRelatioshipToInteractionComments) {
			mu.generateEntityProcessComments(Interaction.class);
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

	
	private void normalizeProvenance(Provenance pro, Model model) {
		autoName(pro); // throws IAE (from MiriamLink)
		// normalize rdfid
		normalizeID(model, pro, pro.getStandardName(), null, null);
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
			SortedSet<String> names = new TreeSet<String>();
			
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
				} catch (IllegalArgumentException e) {
					// ignore (then, names is still empty...)
				}
			} 
			
			// anyway, the above may fail (no match in Miriam)
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
		boolean generateRelatioshipToPathwayComments = true;
		boolean generateRelatioshipToInteractionComments = false;
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
		public boolean isGenerateRelatioshipToPathwayComments() {
			return generateRelatioshipToPathwayComments;
		}
		public void setGenerateRelatioshipToPathwayComments(
				boolean generateRelatioshipToPathwayComments) {
			this.generateRelatioshipToPathwayComments = generateRelatioshipToPathwayComments;
		}
		public boolean isGenerateRelatioshipToInteractionComments() {
			return generateRelatioshipToInteractionComments;
		}
		public void setGenerateRelatioshipToInteractionComments(
				boolean generateRelatioshipToInteractionComments) {
			this.generateRelatioshipToInteractionComments = generateRelatioshipToInteractionComments;
		}
	}
	
}
