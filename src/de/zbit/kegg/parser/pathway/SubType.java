/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg.parser.pathway;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import de.zbit.util.Reflect;

/**
 * Corresponding to the Kegg SubType class (see
 * {@link http://www.genome.jp/kegg/xml/docs/})
 * 
 * @author Clemens Wrzodek
 * @author Florian Mittag
 * @author Finja B&uuml;chel
 * @version $Rev$
 * @since 1.0
 */
public class SubType implements Cloneable {
	// SubType is for RELATION
  
  public static final String COMPOUND = "compound";
  public static final String HIDDEN_COMPOUND = "hidden compound";
  
  public static final String ACTIVATION = "activation";
  public static final String INHIBITION = "inhibition";
  public static final String EXPRESSION = "expression";
  public static final String REPRESSION = "repression";
  public static final String INDIRECT_EFFECT = "indirect effect";
  public static final String STATE_CHANGE = "state change";
  public static final String BINDING_ASSOCIATION = "binding/association";
  public static final String BINDING = "binding";
  public static final String ASSOCIATION = "association";
  public static final String DISSOCIATION = "dissociation";
  public static final String MISSING_INTERACTION = "missing interaction";
  public static final String PHOSPHORYLATION = "phosphorylation";
  public static final String DEPHOSPHORYLATION = "dephosphorylation";
  public static final String GLYCOSYLATION = "glycosylation";
  /**
   * OFTEN ERRORNOUSLY given as "ubiquination" in KGML documents!!!
   */
  public static final String UBIQUITINATION = "ubiquitination";
  public static final String METHYLATION = "methylation";
  
  /**
   * Returns an enumeration, that contains all values of all
   * declared static final variables in this class.
   * <p>You can iterate this enum by using, e.g.
   * <pre>Collections.list(SubType.asEnum());</pre>
   * @return
   */
  public static Enumeration<String> asEnum() {
    return Reflect.getStaticFinalVariablesAsEnumeration(SubType.class);
  }

	/**
	 * One of the following:
	 * <ul>
	 *   <li>compound</li>
	 *   <li>hidden compound</li>
	 *   <li>activation</li>
	 *   <li>inhibition</li>
	 *   <li>expression</li>
	 *   <li>repression</li>
	 *   <li>indirect effect</li>
	 *   <li>state change</li>
	 *   <li>binding/association</li>
	 *   <li>dissociation</li>
	 *   <li>missing interaction</li>
	 *   <li>phosphorylation</li>
	 *   <li>dephosphorylation</li>
	 *   <li>glycosylation</li>
	 *   <li>ubiquitination</li>
	 *   <li>methylation</li>
	 * </ul>
	 */
	String name;
	
	/**
	 * Interaction/relation property value.
	 * See <a href="http://www.genome.jp/kegg/xml/docs/#label:34">official
	 * documentation</a>.
	 * 
	 */
	String value;
	
	
	/**
	 * This is not part of the official kgml specification,
	 * but it is needed in parts of own code. This allows to
	 * set an edge color, that should be handled by
	 * visualizers and converters.
	 * Set to null, to disable and take default color.
	 * Color must be encoded as HTML color. E.g. "#FF0000".
	 */
	String edgeColor = null;

	/**
	 * 
	 * @param name
	 */
	public SubType(String name) {
		super();
		this.name = name.trim();

		// Values according to http://www.genome.jp/kegg/xml/docs/
		// compound and hidden compound default to
		// "Entry element id attribute for (hidden) compound".
		if (name.equalsIgnoreCase(ACTIVATION)) {
			value = "-->";
		} else if (name.equalsIgnoreCase(INHIBITION)) {
			value = "--|";
		} else if (name.equalsIgnoreCase(EXPRESSION)) {
			value = "-->";
		} else if (name.equalsIgnoreCase(REPRESSION)) {
			value = "--|";
		} else if (name.equalsIgnoreCase(INDIRECT_EFFECT)) {
			value = "..>";
		} else if (name.equalsIgnoreCase(STATE_CHANGE)) {
			value = "...";
		} else if (name.equalsIgnoreCase(BINDING_ASSOCIATION)) {
			value = "---";
		} else if (name.equalsIgnoreCase(BINDING)) {
			value = "---";
		} else if (name.equalsIgnoreCase(ASSOCIATION)) {
			value = "---";
		} else if (name.equalsIgnoreCase(DISSOCIATION)) {
			value = "-+-";
		} else if (name.equalsIgnoreCase(MISSING_INTERACTION)) {
			value = "-/-";
		} else if (name.equalsIgnoreCase(PHOSPHORYLATION)) {
			value = "+p";
		} else if (name.equalsIgnoreCase(DEPHOSPHORYLATION)) {
			value = "-p";
		} else if (name.equalsIgnoreCase(GLYCOSYLATION)) {
			value = "+g";
		} else if (name.equalsIgnoreCase(UBIQUITINATION)) {
			value = "+u";
		} else if (name.equalsIgnoreCase(METHYLATION)) {
			value = "+m";
		}
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public SubType(String name, String value) {
		this(name);
		if (value != null && value.length() != 0)
			setValue(value);
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return (name != null) ? name : "";
	}

	/**
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * See {@link #edgeColor}.
	 * @return
	 */
	public String getEdgeColor() {
		return edgeColor;
	}
	
	/**
	 * See {@link #edgeColor}.
	 * Color must be encoded as HTML color. E.g. "#FF0000".
	 * @param edgeColor
	 */
	public void setEdgeColor(String edgeColor) {
		this.edgeColor = edgeColor;
	}

	/**
	 * Be careful, value is set with value.replace("&gt;", ">").
	 * Because when parsing html, "-->" becomes "--&gt;".
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value.replace("&gt;", ">").trim(); // + HTML Code Korrekturen
	}
	
	@Override
	public String toString() {
	  return getName() + "(" + getValue() + ")";
	}

  public Map<String, String> getKGMLAttributes() {
    Map<String, String> attributes = new HashMap<String, String>();
    
    if(isSetName()) {
      attributes.put("name", name);
    }
    if(isSetValue()) {
      attributes.put("value", value);
    }
    
    return attributes;
  }

  private boolean isSetValue() {   
    return value!=null;
  }

  private boolean isSetName() {
    return name!=null;
  }
  
  @Override
  public int hashCode() {
    int hash = 683;
    if(isSetName())
      hash *= name.hashCode();
    if(isSetValue())
      hash *= value.hashCode();
        
    return hash;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public SubType clone() {
    SubType st = new SubType(this.name, this.value);
    st.setEdgeColor(this.edgeColor);
    return st;
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean equals = SubType.class.isAssignableFrom(obj.getClass());
    if(equals) {
      SubType o = (SubType)obj;
      equals &= o.isSetName()==this.isSetName();
      if(equals && isSetName()) 
        equals &= (o.getName().equals(this.getName()));
      
      equals &= o.isSetValue()==this.isSetValue();
      if(equals && isSetValue()) 
        equals &= (o.getValue().equals(this.getValue()));      
      
    }
    return equals;
  }

}
