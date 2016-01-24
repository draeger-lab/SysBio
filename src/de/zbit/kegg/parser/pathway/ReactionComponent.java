/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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

import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;

/**
 * Corresponding to the Kegg ReactionComponent class (see {@link http://www.genome.jp/kegg/xml/docs/})
 * Includes the Kegg "alt", "substrate" and "product" classes.
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
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
   * <p><i>This attribute is NOT part of the original KGML specification!</i> 
   */
  Integer stoichiometry=null;
  
  /**
   * The entry {@link #name} or {@link #id} is referring to.
   * <b>This attribute is OPTIONAL and an unset attribute means just,
   * that it is not set, NOT that the entry does not exist!</b>
   * <p><i>This attribute is NOT part of the original KGML specification!</i> 
   */
  Entry correspondingEntry=null;
  
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
   */
  public ReactionComponent(Integer id, String name) {
    this(name);
    if (id.intValue()!=0) {
      // Id defaults to 0 (unfornatunately) even if it was not set
      // Thus, never store an int value of 0 here.
      this.id = id;
    }
  }
  
  
  /**
   * 
   * @param name
   * @param nl
   */
  public ReactionComponent(Integer id, String name, NodeList nl) {
    this(id, name);
    if (nl==null) return;
    
    // Parse child ("Alt's") from nodeList
    for (int i=0; i<nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node==null) continue;
      
      NamedNodeMap att = node.getAttributes();
      if (node.getNodeName().trim().equalsIgnoreCase("alt"))
        alt = new ReactionComponent(KeggParser.getNodeValueInt(att, "id"), KeggParser.getNodeValue(att, "name"), node.getChildNodes());
    }
  }

  /**
   * @param child
   */
  public ReactionComponent(Entry child) {
    this (child.getId(), child.getName());
    setCorrespondingEntry(child);
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
   * {@code true} if an alternative {@link ReactionComponent}
   * is available.
   * @return
   */
  public boolean isSetAlt() {
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
  public boolean isSetID() {
    return id!=null && id.intValue()>0;
  }

  /**
   * @return
   */
  public boolean isSetName() {
    return (this.name != null && this.name.length()>0);
  }
  
  /**
   * @return the stoichiometry (defaults to {@code null}, if not set manually by calling {@link #setStoichiometry(Integer)})
   */
  public Integer getStoichiometry() {
    return stoichiometry;
  }
  
  
  /**
   * Returns the corresponding entry, IF IT IS SET! This is
   * a non-required attribute, thus, if it is not set, you
   * must use another method (e.g.
   * {@link Pathway#getEntryForReactionComponent(ReactionComponent)}). 
   * @return the correspondingEntry
   */
  public Entry getCorrespondingEntry() {
    return correspondingEntry;
  }

  /**
   * @param correspondingEntry the correspondingEntry to set
   */
  public void setCorrespondingEntry(Entry correspondingEntry) {
    this.correspondingEntry = correspondingEntry;
  }
  
  public boolean isSetCorrespondingEntry() {
    return correspondingEntry!=null;
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
    if (alt!=null && alt!=this) {
      other.setAlt(alt.clone());
    }
    return other;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (String.format("[RC name:'%s' id:'%s'%s]", name, id==null?"none":id, alt!=null?" alternative available":""));
  }
  
  /**
   * 
   * @return all the necessary XML attributes of this class
   */
  public Map<String, String> getKGMLAttributes() {
    Map<String, String> attributes = new TreeMap<String, String>();
    
    if(isSetID()) {
      attributes.put("id", id.toString());
    }
    if(isSetName()) {
      attributes.put("name", name);
    }           
    
    return attributes;
  }
  
  @Override
  public int hashCode() {
    int hash = 467;
    if(isSetID())
      hash *= id;
    if(isSetName())
      hash *= name.hashCode();
    if(isSetAlt())
      hash *= alt.hashCode();
    return hash;
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean equals = ReactionComponent.class.isAssignableFrom(obj.getClass());
    if(equals) {
      ReactionComponent o = (ReactionComponent)obj;
      equals &= o.isSetID()==this.isSetID();
      if(equals && isSetID()) 
        equals &= (o.getId() == this.getId());
      
      equals &= o.isSetName()==this.isSetName();
      if(equals && isSetName()) 
        equals &= (o.getName().equals(this.getName()));
      
      equals &= o.isSetAlt()==this.isSetAlt();
      if(equals && isSetAlt()) 
        equals &= (o.getAlt().equals(this.getAlt()));
      
    }
    return equals;
  }

  /**
   * @return
   */
  public boolean isSetStoichiometry() {
    return stoichiometry!=null;
  }
  
}
