package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.paxtools.model.level3.ExperimentalForm;
import org.biopax.paxtools.model.level3.Score;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Evidence properties and cardinality constraints.
 * 
 * This is not a trivial "cardinality and range rule" that
 * cannot simply extend the CardinalityAndRangeRule class.
 * 
 * @author rodche
 */
@Component
public class EvidencePropertiesUsageRule extends AbstractRule<Evidence> {

    public void check(final Validation validation, Evidence ev) {

		// check it has AT LEAST one of the things
		if ((ev.getEvidenceCode() == null || ev.getEvidenceCode().isEmpty())
				&& (ev.getExperimentalForm() == null || ev.getExperimentalForm().isEmpty())
				&& (ev.getConfidence() == null || ev.getConfidence().isEmpty())) {
			error(validation, ev,
					"min.cardinality.violated", false, "'evidenceCode' or 'confidence' or 'experimantalForm'", 1);
		} else {

			// evidenceCode range
			if (ev.getEvidenceCode() != null) {
				for (Object cv : ev.getEvidenceCode()) {
					if (!EvidenceCodeVocabulary.class.isInstance(cv)) {
						error(validation, ev, "range.violated", 
								false, "evidenceCode",
								cv, cv.getClass().getSimpleName(), "EvidenceCodeVocabulary");
					}
				}
			}

			// experimentalForm
			if (ev.getExperimentalForm() != null) {
				for (Object cv : ev.getExperimentalForm()) {
					if (!ExperimentalForm.class.isInstance(cv)) {
						error(validation, ev, "range.violated", 
								false, "experimentalForm", cv, cv.getClass().getSimpleName(), "ExperimentalForm");
					}
				}
			}

			// confidence
			if (ev.getConfidence() != null) {
				for (Object cv : ev.getConfidence()) {
					if (!Score.class.isInstance(cv)) {
						error(validation, ev, "range.violated", 
								false, "confidence", cv, cv.getClass().getSimpleName(), "Score");
					}
				}
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Evidence;
	}

}
