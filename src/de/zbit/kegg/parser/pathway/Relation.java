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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;

/**
 * Corresponding to the Kegg Relation class (see {@link http://www.genome.jp/kegg/xml/docs/})
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class Relation {
  /**
   * 
   */
  int entry1;
  /**
   * 
   */
  int entry2;
  /**
   * 
   */
  RelationType type;
  /**
   * 
   */
  ArrayList<SubType> subtypes = new ArrayList<SubType>();
  
  /**
   * 
   * @param keggPW 
   * @param entry1
   * @param entry2
   * @param type
   */
  public Relation(Pathway keggPW, int entry1, int entry2, RelationType type) {
    super();
    this.entry1 = entry1;
    this.entry2 = entry2;
    this.type = type;
    keggPW.addRelation(this);
  }
  
  /**
   * 
   * @param entry1
   * @param entry2
   * @param type
   * @param childNodes
   */
  public Relation(Pathway keggPW,int entry1, int entry2, RelationType type, NodeList childNodes) {
    this(keggPW, entry1, entry2, type);
    parseSubNodes(childNodes);
  }
  
  /**
   * 
   * @return
   */
  public int getEntry1() {
    return entry1;
  }
  
  /**
   * 
   * @return
   */
  public int getEntry2() {
    return entry2;
  }
  
  /**
   * 
   * @return
   */
  public List<SubType> getSubtypes() {
    return subtypes;
  }
  
  /**
   * 
   * @return
   */
  public Collection<String> getSubtypesNames() {
    Set<String> subTypeNames= new HashSet<String>();
    if (subtypes==null) return subTypeNames;
    for (SubType s : subtypes) {
      subTypeNames.add(s.getName());
    }
    return subTypeNames;
  }
  
  /**
   * 
   * @param s
   */
  public void addSubtype(SubType s) {
	  subtypes.add(s);
  }
  
  /**
   * 
   * @return
   */
  public RelationType getType() {
    return type;
  }
  
  /**
   * 
   * @param nl
   */
  private void parseSubNodes(NodeList nl) {
    if (nl==null) return;
    
    for (int i=0; i<nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node==null) return;
      String name = node.getNodeName().trim();
      
      NamedNodeMap att = node.getAttributes();
      if (name.equalsIgnoreCase("subtype")) {
        subtypes.add(new SubType(KeggParser.getNodeValue(att, "name"), KeggParser.getNodeValue(att, "value")));
      }
    }
  }
  
  /**
   * 
   * @param entry1
   */
  public void setEntry1(int entry1) {
    this.entry1 = entry1;
  }
  
  /**
   * 
   * @param entry2
   */
  public void setEntry2(int entry2) {
    this.entry2 = entry2;
  }
  
  /**
   * 
   * @param type
   */
  public void setType(RelationType type) {
    this.type = type;
  }
  
  public Map<String, String> getKGMLAttributes() {
    Map<String, String> attributes = new HashMap<String, String>();
    
    if(isSetEntry1()){
      attributes.put("entry1", String.valueOf(getEntry1()));
    }
    if(isSetEntry2()){
      attributes.put("entry2", String.valueOf(getEntry2()));
    }
    if(isSetType()){
      attributes.put("type", type.toString());
    }
    
    return attributes;
  }

  public boolean isSetSubTypes(){
    return subtypes.size()>0;
  }
  
  private boolean isSetType() {    
    return type!=null;
  }

  private boolean isSetEntry2() {
    return entry2!=-1;
  }

  private boolean isSetEntry1() {
    return entry1!=-1;
  }
  
  @Override
  public int hashCode() {
    int hash = 461;
    if(isSetEntry1())
      hash *= entry1;
    if(isSetEntry2())
      hash *= entry2;
    if(isSetType())
      hash *= type.hashCode();
    if(isSetSubTypes())
      hash *= subtypes.hashCode();
        
    return hash;
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean equals = true;
    if(obj.getClass().isAssignableFrom(Relation.class)){    
      Relation o = (Relation)obj;
      equals &= o.isSetEntry1()==this.isSetEntry1();
      if(equals && isSetEntry1()) 
        equals &= (o.getEntry1() == this.getEntry1());
      
      equals &= o.isSetEntry2()==this.isSetEntry2();
      if(equals && isSetEntry2()) 
        equals &= (o.getEntry2()==this.getEntry2());
      
      equals &= o.isSetType()==this.isSetType();
      if(equals && isSetType())
        equals &= (o.getType().equals(this.getType()));
        
      equals &= o.isSetSubTypes()==this.isSetSubTypes();
      if(equals && isSetSubTypes()) 
        equals &= (o.getSubtypes().equals(this.getSubtypes()));
     
      
    }
    return equals;
  }

}
