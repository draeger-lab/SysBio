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
package de.zbit.kegg.parser.pathway.ext;

import java.util.Map;

import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.EntryType;
import de.zbit.kegg.parser.pathway.Pathway;

/**
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class EntryExtended extends Entry {
  
  GeneType geneType = null;

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
      EntryType etype, GeneType type) {
    super(parentPathway, id, name, etype);
    setGeneType(type);
  }

  private void setGeneType(GeneType type) {
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

  
  public GeneType getGeneType(){
    return geneType;
  }
  

  private boolean isSetGeneType() {
    return geneType==null ? false : true;
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
    
    return attributes;
  }
  
  @Override
  public int hashCode() {
    int hash = super.hashCode();
    if(isSetGeneType())
      hash *= geneType.hashCode();
    
    return hash;
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
