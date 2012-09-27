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
package de.zbit.kegg.parser.pathway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;
import de.zbit.util.DatabaseIdentifiers;
import de.zbit.util.DatabaseIdentifiers.IdentifierDatabases;
import de.zbit.util.Utils;

/**
 * Corresponding to the Kegg Relation class (see {@link http://www.genome.jp/kegg/xml/docs/})
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
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
  List<SubType> subtypes = new ArrayList<SubType>();
  

  // the following object is no KGML objects
  
  /**
   * This map should contain all identifiers for this element.
   * No matter if these are uniprot, entrez gene, etc.
   */
  private Map<DatabaseIdentifiers.IdentifierDatabases, Collection<String>> identifiers = 
    new HashMap<DatabaseIdentifiers.IdentifierDatabases, Collection<String>>();

  /**
   * The source of this relation. If null, it is considered to be from KEGG.
   * Else, please specify the source (e.g., "Biocarta") here.
   */
  private String source;
  
  /**
   * 
   * <p>Note: This does not (and should not) add this relation to the
   * parent pathway. You need to call addRelation() on the parent pathway
   * after creating the relation.</p>
   * @param keggPW 
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
   * <p>Note: This does not (and should not) add this relation to the
   * parent pathway. You need to call addRelation() on the parent pathway
   * after creating the relation.</p>
   * @param entry1
   * @param entry2
   * @param type
   * @param childNodes
   */
  public Relation(int entry1, int entry2, RelationType type, NodeList childNodes) {
    this(entry1, entry2, type);
    parseSubNodes(childNodes);
  }
  
  public Relation(int entry1, int entry2, RelationType type, SubType subType) {
    this(entry1, entry2, type);
    addSubtype(subType);
  }

  /**
   * 
   * @return id of entry1
   */
  public int getEntry1() {
    return entry1;
  }
  
  /**
   * 
   * @return id of entry2
   */
  public int getEntry2() {
    return entry2;
  }
  
  /**
   * @see #source
   * @return the source. If null, it is considered to be KEGG.
   */
  public String getSource() {
    return source;
  }

  /**
   * @param source the source of this relation. Only set if it is NOT from KEGG.
   * @see #source
   */
  public void setSource(String source) {
    this.source = source;
  }
  
  /**
   * @see #source
   * @return
   */
  public boolean isSetSource() {
    return source!=null && source.length()>0;
  }
  
  /**
   * 
   * @return
   */
  public List<SubType> getSubtypes() {
    return Collections.unmodifiableList(subtypes);
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
  public boolean addSubtype(SubType s) {
    if(s!=null && (!subtypes.contains(s))){
      subtypes.add(s);
      return true;
    }
    return false;
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
    Map<String, String> attributes = new TreeMap<String, String>();
    
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
    return subtypes!=null && subtypes.size()>0;
  }
  
  public boolean isSetType() {    
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
    boolean equals = Relation.class.isAssignableFrom(obj.getClass());
    if(equals){
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Relation [entry1=" + entry1 + ", entry2=" + entry2 + ", type="
        + type + ", subtypes=" + subtypes + "]";
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

}
