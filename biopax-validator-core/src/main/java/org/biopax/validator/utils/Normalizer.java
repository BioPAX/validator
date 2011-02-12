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
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.result.*;

/**
 * BioPAX Normalizer.
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
	
	private SimpleReader biopaxReader;
	
	private Validation validation;
	
	/**
	 * Constructor
	 */
	public Normalizer() {
		biopaxReader = new SimpleReader(BioPAXLevel.L3);
		biopaxReader.mergeDuplicates(true);
	}

	/**
	 * Constructor
	 * 
	 * @param validation existing validation errors there can be set fixed=true.
	 */
	public Normalizer(Validation validation) {
		this();
		this.validation = validation;
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
		
		// if required, upgrade to L3
		biopaxOwlData = convertToLevel3(biopaxOwlData);
		
		// fix BioPAX L3 pre-release property name 'taxonXref' (BioSource)
		biopaxOwlData = biopaxOwlData.replaceAll("taxonXref","xref");
		
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
	

	public void normalizeXrefs(Model model) {
		// normalize xrefs first: set db name as in Miriam and rdfid as db_id
		
		// make a copy (to safely remove duplicates)
		Set<? extends Xref> xrefs = new HashSet<Xref>(model.getObjects(Xref.class));
		for(Xref ref : xrefs) {
			// get database official urn
			String name = ref.getDb();
			
			// workaround a nullpoinerexception
			if(name == null || "".equals(name)) {
				log.error(ref.getModelInterface().getSimpleName() 
					+ " " + ref + " - 'db' property is empty! "
						+ extraInfo());
				continue;
			}
			
			try {
				String urn = MiriamLink.getDataTypeURI(name);
				// update name to the primary one
				name = MiriamLink.getName(urn);
				ref.setDb(name);
			} catch (IllegalArgumentException e) {
				log.error("Unknown or misspelled database name! " +
					e + ". " + extraInfo());
			}
			
			// build new standard rdfid
			// note: due to 
			String rdfid = null;
			try {
				String prefix = BIOPAX_URI_PREFIX + ref.getModelInterface().getSimpleName() + ":";
				
				String ending = 
					(ref.getIdVersion() != null && !"".equals(ref.getIdVersion().trim()))
						? "_" + ref.getIdVersion() // add the id version/variant
						: ""; // no endings
				
				// add the local (last) part of the URI encoded -
				rdfid = prefix + URLEncoder.encode(name + "_" + ref.getId() + ending, "UTF-8").toUpperCase();
				
				// replace xref or update ID
				if(!rdfid.equals(ref.getRDFId()))
					updateID(model, ref, rdfid);
				
			} catch (UnsupportedEncodingException e) {
				log.error("Failed to create RDFID from xref: " +
						ref + "! " + e + ". " + extraInfo());
			}
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
	 * @param bpe element to normalize
	 * @param db official database name or synonym (that of bpe's unification xref)
	 * @param id identifier (if null, new ID will be that of the Miriam Data Type; this is mainly for Provenance)
	 * @param idExt TODO
	 */
	private void normalizeID(Model model, UtilityClass bpe, String db, String id, String idExt) 
	{	
		if(bpe instanceof Xref) {
			log.error("normalizeID called for Xref (hey, this is a bug!). "
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
		
		// update element and model (if required)
		if (!urn.equals(bpe.getRDFId())) {
			try {
				updateID(model, bpe, urn);
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
		try {
			(new SimpleExporter(model.getLevel())).convertToOWL(model, out);
		} catch (IOException e) {
			throw new RuntimeException("Conversion to OWL failed. " 
				+ extraInfo(), e);
		}
		return out.toString();
	}


	/**
	 * Updates ID and/or removes duplicate
	 * 
	 * @param model
	 * @param ref
	 * @param rdfid
	 * @return
	 */
	private UtilityClass updateID(final Model model, final UtilityClass u, final String rdfid) 
	{	
		// is there an object with the same (new) id?
		final UtilityClass v = (UtilityClass) model.getByID(rdfid);
		if(v != null) {
			// rather than update id, use the existing element instead -
			if(log.isInfoEnabled())
				log.info("Removing duplicate, updating links" +
					" (object properties) using existing " 
					 + rdfid + " element instead of " + u.getRDFId()
					 + ". " + extraInfo());
			
			if(!v.isEquivalent(u)) {
				String msg = "Replacing existing BioPAX element: " +
				u + " (" + u.getRDFId() + ", " + u.getModelInterface().getSimpleName()
				+ ") with what MIGHT be a DIFFERENT (type/semantics) one: " + 
				v + " (" + v.getRDFId() + ", " + v.getModelInterface().getSimpleName()
				+ ")!";
				// are they at least of the same type?
				if(v.getModelInterface().equals(u.getModelInterface())) {
					log.error(msg); // can live with it
					/* TODO what about things like uniprot isoforms (e.g., Q9BVL2-2)?
					 * For now, will merge them, copying names, xrefs, comments...
					 */
					if(v instanceof XReferrable 
						&& u instanceof XReferrable) { // the second is for sure ;)
						// copy at least something...
						for(Xref x : ((XReferrable)u).getXref()) {
							((XReferrable) v).addXref(x);
						}
						v.getComment().addAll(u.getComment());
						if(v instanceof Named) {
							((Named)v).getName()
								.addAll(((Named)u).getName());
						}
					}
				} else {
					throw new BiopaxValidatorException(u, msg); // too bad!
				}
			}
			
			AbstractTraverser traverser = new AbstractTraverser(new SimpleEditorMap(BioPAXLevel.L3)) {
				@Override
				protected void visit(Object range, BioPAXElement domain, Model model,
						PropertyEditor editor) {
					if(editor instanceof ObjectPropertyEditor && u.equals(range)) {
						// replace value
						if(editor.isMultipleCardinality())
							editor.removeValueFromBean(u, domain);
						editor.setValueToBean(v, domain);
						if(log.isDebugEnabled()) {
							log.debug("Replaced " + u.getRDFId() + 
								" with " + v.getRDFId() +
								"; " + editor.toString() + 
								"; (domain) bean: " + domain);
						}
					}
				}
			};
			
			// look inside every object -
			for(BioPAXElement element : model.getObjects()) {
				traverser.traverse(element, model);
			}
			// remove now dangling object
			model.remove(u);
			
			// smoke test...
			if(u instanceof Xref)
				assert(((Xref)u).getXrefOf().isEmpty());
			else if(u instanceof EntityReference)
				assert(((EntityReference)u).getEntityReferenceOf().isEmpty());
			
			return v;
		} else {
			model.updateID(u.getRDFId(), rdfid);
			return u;
		}
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
		
		// clean/normalize xrefs first (they are used next)!
		normalizeXrefs(model);
		
		// fix displayName where possible
		fixDisplayName(model);
		
		//for CVs: adding missing xrefs to CVs or, vice versa, infer terms from the existing uni.xrefs
		fixCVs(model);
		
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
				Provenance pro = (Provenance) bpe;
				String name = pro.getStandardName();
				if(name == null) 
					name = pro.getDisplayName();
				if (name != null) 
					normalizeID(model, pro, name, null, null);
				else 
					if(log.isInfoEnabled())
						log.info("Cannot normalize Provenance: " +
								"no standard names found in " + bpe.getRDFId()
								+ ". " + extraInfo());
			} 
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
		
		// restore state
		if(validation != null) {
			validation.setThreshold(threshold);
		}
	}

	
	/**
	 * Adding missing xrefs to CVs or, vice versa, 
	 * infer terms from the existing uni.xrefs
	 * 
	 * @param model
	 */
	private void fixCVs(Model model) {
		// TODO for each CV, add u.xrefs where absent but (only one) can be found by the term name; find the preferred term by xref.id where no terms,etc..
		
		
		
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
			SimpleReader reader = new SimpleReader();
			reader.mergeDuplicates(true);
			Model model = reader.convertFromOWL(is);
			if (model.getLevel() != BioPAXLevel.L3) {
				if (log.isInfoEnabled())
					log.info("Converting to BioPAX Level3... " + extraInfo());
				model = (new OneTwoThree()).filter(model);
				if (model != null) {
					SimpleExporter exporter = new SimpleExporter(model.getLevel());
					exporter.convertToOWL(model, os);
					toReturn = os.toString();
				}
			} else {
				toReturn = biopaxData;
			}
		} catch(Exception e) {
			throw new RuntimeException(
				"Failed to reading data or convert to L3! "
					+ extraInfo(), e);
		}

		// outta here
		return toReturn;
	}
}
