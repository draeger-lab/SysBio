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


import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;

/**
 * Corresponding to the Kegg Reaction class (see {@link http://www.genome.jp/kegg/xml/docs/})
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class Reaction {
  /**
   * 
   */
  String Name;
  /**
   * 
   */
  ReactionType type;
  /**
   * 
   */
  ArrayList<ReactionComponent> substrate = new ArrayList<ReactionComponent>(); // 1..*
  /**
   * 
   */
  ArrayList<ReactionComponent> product = new ArrayList<ReactionComponent>(); // 1..*
  
  /**
   * 
   * @param name
   * @param type
   */
  private Reaction(String name, ReactionType type) {
    super();
    this.Name = name;
    this.type = type;
  }
  
  /**
   * 
   * @param name
   * @param type
   * @param childNodes
   */
  public Reaction(String name, ReactionType type, NodeList childNodes) {
    this(name, type);
    parseSubNodes(childNodes);
  }
  
  /**
   * 
   * @param name
   * @param type
   * @param substrate
   * @param product
   */
  public Reaction(String name, ReactionType type, ReactionComponent substrate, ReactionComponent product) {
    this (name, type);
    addProduct(product);
    addSubstrate(substrate);
  }
  
  /**
   * 
   * @param product
   */
  public void addProduct(ReactionComponent product) {
    this.product.add(product);
  }

  /**
   * 
   * @param substrate
   */
  public void addSubstrate(ReactionComponent substrate) {
    this.substrate.add(substrate);
  }
  
  /**
   * 
   * @return
   */
  public String getName() {
    return Name;
  }
  
  /**
   * 
   * @return
   */
  public ArrayList<ReactionComponent> getProducts() {
    return product;
  }
  
  /**
   * 
   * @return
   */
  public ArrayList<ReactionComponent> getSubstrates() {
    return substrate;
  }
  
  /**
   * 
   * @return
   */
  public ReactionType getType() {
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
      ReactionComponent rc = null;
      if (name.equalsIgnoreCase("substrate")) {
        rc = new ReactionComponent(KeggParser.getNodeValue(att, "name"), node.getChildNodes());
        substrate.add(rc);
      } else if(name.equals("product")) {
        rc = new ReactionComponent(KeggParser.getNodeValue(att, "name"), node.getChildNodes());
        product.add(rc);
      }
      
      // Attribute id is since 7.1
      try {
        String id = KeggParser.getNodeValue(att, "id");
        rc.setId(Integer.parseInt(id));
      } catch (Exception e) {
        /* Possible conflicts:
         * - id attribute might not be set (old KGML document)
         * - Integer.parseInt might fail
         * - rc may be null.
         */
      }
    }
  }

  /**
   * 
   * @param name
   */
  public void setName(String name) {
    Name = name;
  }

  /**
   * 
   * @param type
   */
  public void setType(ReactionType type) {
    this.type = type;
  }

}
