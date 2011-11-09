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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
   * KEGGID of this reaction (e.g. "rn:R00943")
   */
  String name;
  /**
   * See {@link ReactionType}. Currently one of reversible or irreversible.
   */
  ReactionType type;
  /**
   * Substrates
   */
  ArrayList<ReactionComponent> substrate = new ArrayList<ReactionComponent>(); // 1..*
  /**
   * Products
   */
  ArrayList<ReactionComponent> product = new ArrayList<ReactionComponent>(); // 1..*
  
  /**
   * The parent pathway object.
   */
  private Pathway parentPathway = null;
  
  /**
   * 
   * @param name
   * @param type
   */
  private Reaction(Pathway parentPathway, String name, ReactionType type) {
    super();
    this.parentPathway = parentPathway;
    this.name = name;
    this.type = type;
  }
  
  /**
   * 
   * @param name
   * @param type
   * @param childNodes
   */
  public Reaction(Pathway parentPathway, String name, ReactionType type, NodeList childNodes) {
    this(parentPathway, name, type);
    parseSubNodes(childNodes);
  }
  
  /**
   * 
   * @param name
   * @param type
   * @param substrate
   * @param product
   */
  public Reaction(Pathway parentPathway, String name, ReactionType type, ReactionComponent substrate, ReactionComponent product) {
    this (parentPathway, name, type);
    addProduct(product);
    addSubstrate(substrate);
  }

  /**
   * 
   * @param product
   */
  public void addProduct(ReactionComponent product) {
    this.product.add(product);
    parentPathway.registerReactionComponent(product, this);
  }

  /**
   * 
   * @param substrate
   */
  public void addSubstrate(ReactionComponent substrate) {
    this.substrate.add(substrate);
    parentPathway.registerReactionComponent(substrate, this);
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
  public List<ReactionComponent> getProducts() {
    return Collections.unmodifiableList(product);
  }
  
  /**
   * 
   * @return
   */
  public List<ReactionComponent> getSubstrates() {
    return Collections.unmodifiableList(substrate);
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
        addSubstrate(rc);
      } else if(name.equals("product")) {
        rc = new ReactionComponent(KeggParser.getNodeValue(att, "name"), node.getChildNodes());
        addProduct(rc);
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
    this.name = name;
  }

  /**
   * 
   * @param type
   */
  public void setType(ReactionType type) {
    this.type = type;
  }

  /**
   * @return {@link Collection} to iterate over
   * {@link #substrate}s and {@link #product}s.
   */
  public Collection<ReactionComponent> getReactants() {
    return new AbstractCollection<ReactionComponent> () {
      @Override
      public Iterator<ReactionComponent> iterator() {
        return new Iterator<ReactionComponent>() {
          int index=-1;
          @Override
          public boolean hasNext() {
            return (index+1)<size();
          }
          @Override
          public ReactionComponent next() {
            index++;
            int numSubstrates = substrate.size();
            if (index<numSubstrates) {
              return substrate.get(index);
            } else {
              try {
                return product.get(index-numSubstrates);
              } catch (IndexOutOfBoundsException e) {
                return null;
              }
            }
          }
          @Override
          public void remove() {
            System.err.println("REMOVE NOT SUPPORTED!");
          }
        };
      }
      @Override
      public int size() {
        return substrate.size() + product.size();
      }
    };
  }

  /**
   * @param reactantKeggID the name of the reactant, e.g. "cpd:C05922".
   * @return
   */
  public ReactionComponent getReactant(String reactantKeggID) {
    for (ReactionComponent rc: getReactants()) {
      if (rc.getName().equalsIgnoreCase(reactantKeggID)) return rc;
    }
    return null;
  }
  
  /**
   * WARNING: this erases all substrates and products of this
   * reaction.
   */
  public void clearReactants() {
    for (ReactionComponent rc: getReactants()) {
      parentPathway.unregisterReactionComponent(rc, this);
    }
    
    this.substrate.clear();
    this.product.clear();
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public Reaction clone() {
    try {
      Reaction other = new Reaction(this.parentPathway, name, this.type);
      for (ReactionComponent r: getSubstrates()) {
        other.addSubstrate(r.clone());
      }
      for (ReactionComponent r: getProducts()) {
        other.addProduct(r.clone());
      }
      return other;
    } catch (Exception e) {
      // Never happens, because ReactionComponent supports cloning.
      e.printStackTrace();
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (String.format("[Raction name:'%s' type:'%s' formula:'%s']", name, type, getEquation()));
  }

  /**
   * @return an equation like "C00183 + C00026 <=> C00141 + C00025"
   */
  public String getEquation() {
    StringBuilder b = new StringBuilder();
    for (int i=0; i<substrate.size(); i++) {
      if (i>0) b.append(" + ");
      b.append(substrate.get(i).getName());
    }
    if (b.length()>0) {
      b.append(' ');
      if (type.equals(ReactionType.reversible)) b.append('<');
      b.append("=> ");
    }
    for (int i=0; i<product.size(); i++) {
      if (i>0) b.append(" + ");
      b.append(product.get(i).getName());
    }
    return b.toString();
  }

}
