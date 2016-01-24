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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;
import de.zbit.util.DatabaseIdentifiers;
import de.zbit.util.Utils;
import de.zbit.util.DatabaseIdentifiers.IdentifierDatabases;

/**
 * Corresponding to the Kegg Reaction class (see {@link http://www.genome.jp/kegg/xml/docs/})
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
 * @version $Rev$
 * @since 1.0
 */
public class Reaction {
  /**
   * 
   */
  Integer id;
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
  
  // the following objects are no KGML objects
  
  /**
   * This map should contain all identifiers for this element.
   * No matter if these are uniprot, entrez gene, etc.
   */
  private Map<DatabaseIdentifiers.IdentifierDatabases, Collection<String>> identifiers = 
    new HashMap<DatabaseIdentifiers.IdentifierDatabases, Collection<String>>();
  
  /**
   * The parent pathway object.
   */
  private Pathway parentPathway = null;
  
  /**
   * 
   * <p>Note: This does not (and should not) add this reaction to the
   * parent pathway. You need to call addReaction() on the parent pathway
   * after creating the reaction.</p>
   * @param name
   * @param type
   */
  public Reaction(Pathway parentPathway, String name, ReactionType type) {
    super();
    this.parentPathway = parentPathway;
    this.name = name;
    this.type = type;
  }
  
  /**
   * 
   * <p>Note: This does not (and should not) add this reaction to the
   * parent pathway. You need to call addReaction() on the parent pathway
   * after creating the reaction.</p>
   * @param id
   * @param parentPathway
   * @param name
   * @param type
   */
  public Reaction(Pathway parentPathway, Integer id, String name, ReactionType type) {
    this(parentPathway, name, type);
    this.id = id;
  }
  
  /**
   * 
   * <p>Note: This does not (and should not) add this reaction to the
   * parent pathway. You need to call addReaction() on the parent pathway
   * after creating the reaction.</p>
   * @param name
   * @param type
   * @param childNodes
   */
  public Reaction(Pathway parentPathway, Integer id, String name, ReactionType type, NodeList childNodes) {
    this(parentPathway, id, name, type);
    parseSubNodes(childNodes);
  }
  
  /**
   * 
   * <p>Note: This does not (and should not) add this reaction to the
   * parent pathway. You need to call addReaction() on the parent pathway
   * after creating the reaction.</p>
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
  
  public void addProducts(List<ReactionComponent> products) {
   for (ReactionComponent component : products) {
     addProduct(component);
   } 
  }

  /**
   * 
   * @param substrate
   */
  public void addSubstrate(ReactionComponent substrate) {
    this.substrate.add(substrate);
    parentPathway.registerReactionComponent(substrate, this);
  }
 
  public void addSubstrates(List<ReactionComponent> products) {
    for (ReactionComponent component : products) {
      addSubstrate(component);
    } 
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
  public int getId() {
    return id;
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
        rc = new ReactionComponent(KeggParser.getNodeValueInt(att, "id"), KeggParser.getNodeValue(att, "name"), node.getChildNodes());
        addSubstrate(rc);
      } else if(name.equals("product")) {
        rc = new ReactionComponent(KeggParser.getNodeValueInt(att, "id"), KeggParser.getNodeValue(att, "name"), node.getChildNodes());
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
          /*
           * (non-Javadoc)
           * @see java.util.Iterator#hasNext()
           */
          public boolean hasNext() {
            return (index+1)<size();
          }
          /*
           * (non-Javadoc)
           * @see java.util.Iterator#next()
           */
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
          /*
           * (non-Javadoc)
           * @see java.util.Iterator#remove()
           */
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
   * Returns the reactant, but only if it is a substrate or product!
   * @param reactantKeggID the name of the reactant, e.g. "cpd:C05922".
   * @param isSubstrate {@code true} to only return the
   * reactant if it occurs as substrate. {@code false} to only return the
   * reactant if it occurs as product.  
   * @return
   * @see #getReactant(String) the same method without boolean attribute
   * to return a reactant, regardless if it occurs as substrate or product.
   */
  public ReactionComponent getReactant(String reactantKeggID, boolean isSubstrate) {
    if (isSubstrate) {
      for (ReactionComponent rc: getSubstrates()) {
        if (rc.getName().equalsIgnoreCase(reactantKeggID)) return rc;
      }
    } else { // isProduct
      for (ReactionComponent rc: getProducts()) {
        if (rc.getName().equalsIgnoreCase(reactantKeggID)) return rc;
      }
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
    return (String.format("[Reaction name:'%s' type:'%s' formula:'%s']", name, type, getEquation()));
    //return String.format("[Reaction name:'%s' type:'%s' formula:'%s' substrates:'%s' products:'%s']", name, type, getEquation(), getSubstrates(), getProducts() );
  }

  /**
   * @return an equation like "C00183 + C00026 <=> C00141 + C00025"
   * <br/>or "16 C00002 + 16 C00001 + 8 C00138 <=> 8 C05359 + 16 C00009 + 16 C00008 + 8 C00139"
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
  
  /**
   * Please be careful with this, as it returns
   * internal data structures.
   * @return complemente list of {@link #identifiers}.
   */
  public Map<IdentifierDatabases, Collection<String>> getDatabaseIdentifiers() {
    return identifiers;
  }
  
  /**
   * 
   * @return {@code true} if we have some {@link #identifiers}.
   */
  public boolean isSetDatabaseIdentifiers() {
    return identifiers!=null && identifiers.size()>0;
  }
  
  /**
   * Add an identifier to this {@link Entry}.
   * @param db
   * @param id
   */
  public void addDatabaseIdentifier(IdentifierDatabases db, String id) {
    Utils.addToMapOfSets(identifiers, db, id);
  }
  
  /**
   * Add all identifier of the map to this {@link Entry}.
   * @param db
   * @param id
   */
  public void addDatabaseIdentifiers( Map<IdentifierDatabases, Collection<String>> map) {
    for (java.util.Map.Entry<IdentifierDatabases, Collection<String>> entry : map.entrySet()) {
      for (String value : entry.getValue()) {
        Utils.addToMapOfSets(identifiers, entry.getKey(), value);
      }
    }    
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
    if(isSetType()) {
      attributes.put("type", type.toString());
    }
       
    
    return attributes;
  }

  public boolean isSetType() {
    return type!=null;
  }

  private boolean isSetName() {    
    return name!=null;
  }

  private boolean isSetID() {   
    return id!=null;
  }

  public boolean isSetProduct() {
    return product.size()>0 ? true : false;
  }

  public boolean isSetSubstrate() {
    return substrate.size()>0 ? true : false;
  }  

  @Override
  public int hashCode() {
    int hash = 727;
    if(isSetID())
      hash *= id;
    if(isSetName())
      hash *= name.hashCode();
    if(isSetType())
      hash *= type.hashCode();
    if(isSetSubstrate())
      hash *= substrate.hashCode();
    if(isSetProduct())
      hash *= product.hashCode();
    
    
    return hash;
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean equals = Reaction.class.isAssignableFrom(obj.getClass());
    if(equals) {
      Reaction o = (Reaction)obj;
      equals &= o.isSetID()==this.isSetID();
      if(equals && isSetID()) 
        equals &= (o.getId() == this.getId());
      
      equals &= o.isSetName()==this.isSetName();
      if(equals && isSetName()) 
        equals &= (o.getName().equals(this.getName()));
      
      equals &= o.isSetType()==this.isSetType();
      if(equals && isSetType()) 
        equals &= (o.getType().equals(this.getType()));
      
      equals &= o.isSetProduct()==this.isSetProduct();
      if(equals && isSetProduct()) 
        equals &= (o.getProducts().equals(this.getProducts()));
      
      equals &= o.isSetSubstrate()==this.isSetSubstrate();
      if(equals && isSetSubstrate()) 
        equals &= (o.getSubstrates().equals(this.getSubstrates()));
      
    }
    return equals;
  }
}
