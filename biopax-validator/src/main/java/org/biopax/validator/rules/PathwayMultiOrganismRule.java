package org.biopax.validator.rules;

import org.biopax.paxtools.controller.Fetcher;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.util.Filter;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Warn if a pathway and its component have different (not null) 'organism' values.
 * What to do? (ignore, delete this value, or override nested organism properties with pathway's value)
 *
 * @author rodche
 */
@Component
public class PathwayMultiOrganismRule extends AbstractRule<Pathway> {
  private final static Filter<PropertyEditor> filter = new Filter<PropertyEditor>() {

    public boolean filter(PropertyEditor editor) {
      //skip for: nextStep (those can be reached via pathwayOrder or pathwayComponent) and evidence (multi-org. is normal there)
      return !("nextStep".equals(editor.getProperty()) || "evidence".equals(editor.getProperty()));
    }
  };

  public void check(final Validation validation, final Pathway pathway) {
    Fetcher fetcher = new Fetcher(SimpleEditorMap.L3, Fetcher.evidenceFilter, Fetcher.nextStepFilter);
    fetcher.setSkipSubPathways(true);
    final Set<BioSource> organisms = fetcher.fetch(pathway, BioSource.class);
    //collect taxonomy IDs (from the BioSource objects that have valid xrefs)
    PathAccessor accessor = new PathAccessor("BioSource/xref:UnificationXref/id");
    Set<String> taxIds = accessor.getValueFromBeans(organisms);
    if (taxIds.size() > 1) {
      accessor = new PathAccessor("BioSource/name");
      Set<String> names = new HashSet<String>();
      for(Object name  : accessor.getValueFromBeans(organisms))
       names.add(String.valueOf(name).toLowerCase());

      error(validation, pathway, "multi.organism.pathway", false, taxIds, names);
    }
  }

  public boolean canCheck(Object thing) {
    return thing instanceof Pathway;
  }

}
