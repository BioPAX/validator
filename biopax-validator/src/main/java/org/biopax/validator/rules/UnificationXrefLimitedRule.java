package org.biopax.validator.rules;


import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.XrefUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UnificationXref applicability rule
 * (BioPAX class - allowed/denied unification xref db names)
 *
 * @author rodche
 */
@Component
public class UnificationXrefLimitedRule extends AbstractRule<UnificationXref> {

  private static final Map<Class<? extends BioPAXElement>, Set<String>> allow =
    new ConcurrentHashMap<Class<? extends BioPAXElement>, Set<String>>();
  private static final Map<Class<? extends BioPAXElement>, Set<String>> deny =
    new ConcurrentHashMap<Class<? extends BioPAXElement>, Set<String>>();

  static {
    // allow ONLY listed db names (synonyms will be considered too) for member UnificationXrefs of:
    allow.put(BioSource.class, new HashSet<>(Arrays.asList("taxonomy")));
    allow.put(Provenance.class, new HashSet<>(Arrays.asList("miriam")));
    allow.put(CellVocabulary.class, new HashSet<>(Arrays.asList("cl")));
    allow.put(TissueVocabulary.class, new HashSet<>(Arrays.asList("bto")));
    allow.put(CellularLocationVocabulary.class, new HashSet<>(Arrays.asList("go")));
    allow.put(EvidenceCodeVocabulary.class, new HashSet<>(Arrays.asList("mi")));
    allow.put(ExperimentalFormVocabulary.class, new HashSet<>(Arrays.asList("mi")));
    allow.put(InteractionVocabulary.class, new HashSet<>(Arrays.asList("mi")));
    allow.put(SequenceModificationVocabulary.class, new HashSet<>(Arrays.asList("so", "mod")));
    allow.put(PhenotypeVocabulary.class, new HashSet<>(Arrays.asList("pato")));
    allow.put(RelationshipTypeVocabulary.class, new HashSet<>(Arrays.asList("mi")));
    allow.put(SequenceRegionVocabulary.class, new HashSet<>(Arrays.asList("so")));

    // not recommended xref.db names (and all synonyms) for UnificationXrefs of
    deny.put(Dna.class, new HashSet<>(Arrays.asList("uniprot", "pubmed")));
    deny.put(Rna.class, new HashSet<>(Arrays.asList("uniprot")));
    deny.put(DnaReference.class, new HashSet<>(Arrays.asList("uniprot", "pubmed")));
    deny.put(RnaReference.class, new HashSet<>(Arrays.asList("uniprot")));
    deny.put(SmallMoleculeReference.class, new HashSet<>(Arrays.asList("uniprot")));
    deny.put(SmallMolecule.class, new HashSet<>(Arrays.asList("uniprot")));
    deny.put(PhysicalEntity.class, new HashSet<>(Arrays.asList("go")));
    deny.put(ProteinReference.class, new HashSet<>(Arrays.asList("OMIM", "Entrez Gene")));
    deny.put(Interaction.class, new HashSet<>(Arrays.asList("mi")));
  }

  private XrefUtils helper;

  private boolean ready = false;

  // to init on the first check(..) call
  void initInternalMaps() {
    if (!ready) {
      addDbSynonymsTo(allow);
      addDbSynonymsTo(deny);
      ready = true;
    }
  }

  private void addDbSynonymsTo(Map<Class<? extends BioPAXElement>,Set<String>> map) {
    for (Set<String> val : map.values()) {
      for (String db : new HashSet<>(val)) {
        val.addAll(helper.getSynonymsForDbName(db));
      }
    }
  }

  /*
   * Constructor requires the two sets to be defined in
   * the Spring application context.
   *
   * @param xrefHelper utils
   */
  @Autowired
  public UnificationXrefLimitedRule(XrefUtils xrefUtils) {
    helper = xrefUtils;
  }


  public boolean canCheck(Object thing) {
    return thing instanceof UnificationXref;
  }

  public void check(final Validation validation, UnificationXref x) {
    if (!ready)
      initInternalMaps();

    if (x.getDb() == null
      || helper.getPrefix(x.getDb()) == null) {
      // ignore for unknown databases (another rule checks)
      return;
    }

    // fix case sensitivity
    final String xdb = helper.dbName(x.getDb());

    // check constrains for each element containing this unification xref
    for (XReferrable bpe : x.getXrefOf()) {
      for (Class<? extends BioPAXElement> c : allow.keySet()) {
        if (c.isInstance(bpe)) {
          if (!allow.get(c).contains(xdb)) {
            error(validation, x, "not.allowed.xref", false, x.getDb(),
              bpe, c.getSimpleName(), allow.get(c).toString());
          }
        }
      }
      for (Class<? extends BioPAXElement> c : deny.keySet()) {
        if (c.isInstance(bpe)) {
          if (deny.get(c).contains(xdb)) {
            error(validation, x, "denied.xref", false, x.getDb(),
              bpe, c.getSimpleName(), deny.get(c).toString());
          }
        }
      }
    }
  }

}
