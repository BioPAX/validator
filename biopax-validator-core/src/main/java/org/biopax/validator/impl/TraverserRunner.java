package org.biopax.validator.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.utils.BiopaxValidatorUtils;

/**
 * This is a utility class that helps 
 * walking the BioPAX properties
 * to find/check various things there, 
 * while keeping track of it and not
 * going into previously visited elements. 
 * 
 * @author rodch
 *
 */
public abstract class TraverserRunner implements Visitor {
	protected static Log log;
	protected BioPAXElement start;
	protected String path = "";
	protected final Set<BioPAXElement> visited;
	private final EditorMap editorMap;
		
	public TraverserRunner(EditorMap editorMap) {
		log = LogFactory.getLog(this.getClass());
		this.editorMap = editorMap;
		visited = new HashSet<BioPAXElement>();
	}

	/**
	 * This is to implement a real action here, 
	 * e.g., report errors and/or continue
	 * going into property values.
	 * 
	 * @param value
	 * @param editor
	 */
	protected abstract void visitObjectValue(BioPAXElement value, Model model, PropertyEditor editor);
	
	/**
	 * This is to implement an action 
	 * within Data Property
	 * 
	 * @param value primitive type
	 * @param editor
	 * 
	 * TODO override, if required, in a subclass
	 */
	protected void visitDataValue(Object value, BioPAXElement parent, Model model, PropertyEditor editor) {
	}
		
	public void visit(BioPAXElement bpe, Model model, PropertyEditor editor) {
		if (bpe != null && !visited.contains(bpe)) {
			visited.add(bpe); // must be before the following visit(..) call
			String oldPath = path; // save the current path
			path += "." + editor.getProperty() + "=" 
				+ BiopaxValidatorUtils.getLocalId(bpe);
			visitObjectValue(bpe, model, editor); // does the job!
			path = oldPath; // reset the previous path
		}
	}
		
	/**
	 * Starts traversing and visiting
	 * 
	 * @param model
	 * @return can be overridden or ignored
	 */
	public boolean run(BioPAXElement start, Model model) {
		this.start = start;
		visited.clear();
		path = "";
		if(start != null) {
			traverse(start, model);
		} else if (model != null){
			for(BioPAXElement e : model.getObjects()) {
				traverse(e, model);
			}
		}
		return true;
	}
	
    /**
	 * Provides {@link Visitor} functionallity regarding the editors'
	 * cardinality features. While using all the editors whose domain contains
	 * the BioPAX <em>element</em>.
	 * 
	 * @param element BioPAX element to be traversed
	 * @param model model into which <em>element</em> will be traversed
	 */
	protected void traverse(BioPAXElement element, Model model) {
		if (element == null) {
			return;
		}
		try {
			Set<PropertyEditor> editors = editorMap.getEditorsOf(element);
			if(editors == null) {
				log.warn("No editors for : " + element.getModelInterface());
				return;
			}
			
			for (PropertyEditor editor : editors) {
				if (editor.isMultipleCardinality()) {
					for (Object value : (Collection) editor
							.getValueFromBean(element)) {
						if (value instanceof BioPAXElement) {
							visit((BioPAXElement) value, model, editor);
						} else {
							visitDataValue(value, element, model, editor);
						}
					}
				} else {
					Object value = editor.getValueFromBean(element);
					if (value instanceof BioPAXElement) {
						visit((BioPAXElement) value, model, editor);
					} else {
						visitDataValue(value, element, model, editor);
					}
				}
			}
		} catch (NullPointerException e) {
			if (log.isWarnEnabled()) {
				log.warn("no editors? ", e);
			}
			visited.add(element);
		}
	}

}
