/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;

/**
 * Corresponding to the Kegg ReactionComponent class (see {@link http://www.genome.jp/kegg/xml/docs/})
 * Includes the Kegg "alt" class.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class ReactionComponent implements Cloneable {
  
  /**
   * 
   */
  String name;
  /**
   * 
   */
  ReactionComponent alt = null;
  
  /**
   * A rather new attribute.
   */
  Integer id=null;
  
  /**
   * Stoichiometry (count) of this reaction component in this reaction 
   */
  Integer stoichiometry=null;
  
  /**
   * 
   * @param name
   */
  public ReactionComponent(String name) {
    super();
    this.name = name;
  }
  
  /**
   * 
   * @param name
   * @param nl
   */
  public ReactionComponent(String name, NodeList nl) {
    this(name);
    if (nl==null) return;
    
    // Parse child ("Alt's") from nodeList
    for (int i=0; i<nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node==null) continue;
      
      NamedNodeMap att = node.getAttributes();
      if (node.getNodeName().trim().equalsIgnoreCase("alt"))
        alt = new ReactionComponent(KeggParser.getNodeValue(att, "name"), node.getChildNodes());
    }
  }

  /**
   * 
   * @return
   */
  public ReactionComponent getAlt() {
    return alt;
  }
  
  /**
   * 
   * @return
   */
  public String getName() {
    return name;
  }
  
  /**
   * 
   * @return
   */
  public boolean hasAlt() {
    return (alt!=null);
  }

  /**
   * 
   * @param alt
   */
  public void setAlt(ReactionComponent alt) {
    this.alt = alt;
  }
  
  /**
   * 
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param id
   */
  public void setId(Integer id) {
    this.id = id;
  }
  
  /**
   * @return id
   */
  public Integer getId() {
    return this.id;
  }

  /**
   * @return
   */
  public boolean hasId() {
    return id!=null && id.intValue()>0;
  }

  /**
   * @return
   */
  public boolean hasName() {
    return (this.name != null && this.name.length()>0);
  }
  
  /**
   * @return the stoichiometry (defaults to <code>NULL</code>, if not set manually by calling {@link #setStoichiometry(Integer)})
   */
  public Integer getStoichiometry() {
    return stoichiometry;
  }

  /**
   * @param stoichiometry the stoichiometry to set
   */
  public void setStoichiometry(Integer stoichiometry) {
    this.stoichiometry = stoichiometry;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected ReactionComponent clone() throws CloneNotSupportedException {
    ReactionComponent other = new ReactionComponent(name);
    if (id==null) other.setId(null);
    else other.setId(new Integer(id.intValue()));
    if (alt!=this) {
      other.setAlt(alt.clone());
    }
    return alt;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (String.format("[RC name:'%s' id:'%s'%s]", name, id==null?"none":id, alt!=null?" alternative available":""));
  }
  
}
