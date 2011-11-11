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

import keggapi.Subtype;


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
   * @param entry1
   * @param entry2
   * @param type
   */
  public Relation(int entry1, int entry2, RelationType type) {
    super();
    this.entry1 = entry1;
    this.entry2 = entry2;
    this.type = type;
  }
  
  /**
   * 
   * @param entry1
   * @param entry2
   * @param type
   * @param childNodes
   */
  public Relation(int entry1, int entry2, RelationType type, NodeList childNodes) {
    this(entry1, entry2, type);
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
  public ArrayList<SubType> getSubtypes() {
    return subtypes;
  }
  
  /**
   * 
   * @return
   */
  public ArrayList<String> getSubtypesNames() {
    ArrayList<String> subTypeNames= new ArrayList<String>();
    for (SubType s : subtypes) {
      if(!subTypeNames.contains(s.getName()))
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

}
