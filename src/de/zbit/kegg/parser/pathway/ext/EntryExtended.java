/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg.parser.pathway.ext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.EntryType;
import de.zbit.kegg.parser.pathway.Graphics;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.util.DatabaseIdentifiers;
import de.zbit.util.DatabaseIdentifiers.IdentifierDatabases;
import de.zbit.util.Utils;

/**
 * An extended Version of an {@link Entry}. This allows to store more
 * information for internal process than the original KGML
 * specification permits.
 * 
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class EntryExtended extends Entry {
  
  /**
   * This is a more fine-grained defintion of 
   * {@link EntryType} and should ONLY be set when
   * the definition of {@link EntryType} is too vague.
   */
  private EntryTypeExtended geneType = null;
  
  /**
   * This map should contain all identifiers for this element.
   * No matter if these are uniprot, entrez gene, etc.
   */
  private Map<DatabaseIdentifiers.IdentifierDatabases, Collection<String>> identifiers = 
    new HashMap<DatabaseIdentifiers.IdentifierDatabases, Collection<String>>();
  
  
  /**
   * cellular component
   */
  private String compartment = null;

  
  /**
   * @param parentPathway
   * @param id
   * @param name
   * @param type
   */
  public EntryExtended(Pathway parentPathway, int id, String name,
      EntryType type) {
    super(parentPathway, id, name, type);
  }
  
  /**
   * @param parentPathway
   * @param id
   * @param name
   * @param type
   */
  public EntryExtended(Pathway parentPathway, int id, String name,
      EntryType etype, EntryTypeExtended type) {
    super(parentPathway, id, name, etype);
    setGeneType(type);
  }
  
  public EntryExtended(Pathway parentPathway, int id, String name,
      EntryType etype, EntryTypeExtended type, Graphics graphics) {
    this(parentPathway, id, name, etype, type);    
    addGraphics(graphics);
  }

  private void setGeneType(EntryTypeExtended type) {
   this.geneType = type;
  }

  /**
   * @param parentPathway
   * @param id
   * @param name
   * @param type
   * @param link
   * @param reaction
   */
  public EntryExtended(Pathway parentPathway, int id, String name,
      EntryType type, String link, String reaction) {
    super(parentPathway, id, name, type, link, reaction);
  }

  /**
   * @param parentPathway
   * @param id
   * @param name
   * @param type
   * @param link
   * @param reaction
   * @param childNodes
   */
  public EntryExtended(Pathway parentPathway, int id, String name,
      EntryType type, String link, String reaction, NodeList childNodes) {
    super(parentPathway, id, name, type, link, reaction, childNodes);
  }

  /**
   * @param parentPathway
   * @param id
   * @param name
   */
  public EntryExtended(Pathway parentPathway, int id, String name) {
    super(parentPathway, id, name);
  }

  
  public EntryExtended(de.zbit.kegg.parser.pathway.Pathway keggPW, int keggEntryID,
      String keggname, EntryType eType, Graphics graphics) {
    super(keggPW, keggEntryID, keggname, eType, graphics);
    
  }

  /**
   * Clone constructor for Entry to EntryExtended
   * @param ke
   */
  public EntryExtended(Entry ke) {
    super(ke.getParentPathway(),
        ke.getId(),
        ke.getName());
//    if(ke.getParentPathway()!=null){
//      this.setParentNode(ke.getParentNode());
//    }
//    if(ke.isSetID()){
//      this.setId(ke.getId());
//    }
//    if(ke.isSetName()){
//      this.setName(ke.getName());
//    }
    if(ke.isSetType()){
      this.setType(ke.getType());
    }
    if(ke.isSetLink()){
      this.setLink(ke.getLink());
    }
    if(ke.isSetReaction()){
      this.setReaction(ke.getReactionString());
    }
  }

  public EntryTypeExtended getGeneType(){
    return geneType;
  }
  

  public boolean isSetGeneType() {
    return geneType==null ? false : true;
  }
  
  /**
   * 
   * @return <code>TRUE</code> if we have some {@link #identifiers}.
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
   * Remove an identifier from this {@link Entry}.
   * @param db
   * @param id
   */
  public void removeDatabaseIdentifier(IdentifierDatabases db, String id) {
    Utils.removeFromMapOfSets(identifiers, id, db);
  }
  
  /**
   * Please be careful with this, as it returns
   * internal data structures.
   * @return complemente list of {@link #identifiers}.
   */
  public Map<IdentifierDatabases, Collection<String>> getDatabaseIdentifiers() {
    return identifiers;
  }
  

  public String getCompartment() {
    return compartment;
  }

  public void setCompartment(String compartment) {
    this.compartment = compartment;
  }
  
  public boolean isSetCompartment(){
    return compartment!=null && compartment.length()>0;
  }

  
  /**
   * 
   * @return all the necessary XML attributes of this class
   */
  public Map<String, String> getKGMLAttributes() {
    Map<String, String> attributes = super.getKGMLAttributes();
    
    if(isSetGeneType()){
      attributes.put("geneType", geneType.toString());
    }    
    if(isSetCompartment()){
      attributes.put("compartment", compartment);
    }
    if(isSetDatabaseIdentifiers()){
      for (java.util.Map.Entry<IdentifierDatabases, Collection<String>> entry : identifiers.entrySet()) {
        for (String value : entry.getValue()) {
          attributes.put(entry.getKey().toString(), value);
        }
      }
    }
    
    return attributes;
  }
  
  @Override
  public int hashCode() {
    int hash = super.hashCode();
    if(isSetGeneType())
      hash *= geneType.hashCode();
    
    return hash;
  }
  
  public boolean equalsWithoutIDReactionComparison(Object obj) {
    boolean equals = super.equalsWithoutIDReactionComparison(obj);
    
    if(equals && obj.getClass().isAssignableFrom(EntryExtended.class)){    
      EntryExtended o = (EntryExtended)obj;
      equals &= (o.isSetGeneType()==this.isSetGeneType());
      if(equals && isSetGeneType()) 
        equals &= (o.getGeneType().equals(this.getGeneType()));
      
    }
    return equals;
  }
  
  public boolean equalsWithoutIDNameReactionComparison(Object obj) {
    boolean equals = super.equalsWithoutIDNameReactionComparison(obj);
    
    if (EntryExtended.class.isAssignableFrom(obj.getClass())){
      EntryExtended o = (EntryExtended)obj;          
      
      equals &= o.isSetGeneType()==this.isSetGeneType();
      if(equals && isSetGeneType()) 
        equals &= (o.getGeneType().equals(this.getGeneType()));    
      
      equals &= o.isSetCompartment()==this.isSetCompartment();
      if(equals && isSetCompartment()) 
        equals &= (o.getCompartment().equals(this.getCompartment()));
      
    }
    return equals;
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean equals = super.equals(obj);
    
    if(equals && obj.getClass().isAssignableFrom(EntryExtended.class)){    
      EntryExtended o = (EntryExtended)obj;
      equals &= (o.isSetGeneType()==this.isSetGeneType());
      if(equals && isSetGeneType()) 
        equals &= (o.getGeneType().equals(this.getGeneType()));
      
    }
    return equals;
  }


}
