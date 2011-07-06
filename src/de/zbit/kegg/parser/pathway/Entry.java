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
 * Corresponding to the Kegg Entry class (see 
 * <a href="http://www.genome.jp/kegg/xml/docs/">KeggAPI</a>)
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class Entry {
  /**
   * id.type    the ID of this entry in the pathway map
   */
  private int id = 0;
  /**
   * keggid.type    the KEGGID of this entry
   */
  private String name = "";
  /**
   * entry_type.type    the type of this entry
   */
  private EntryType type;
  /**
   * url.type   the resource location of the information about this entry
   */
  private String link = "";
  /**
   * keggid.type    the KEGGID of corresponding reaction
   */
  private String reaction = "";
  /**
   * For Customize purposes (e.g. saving a corresponding node reference)
   */
  private Object custom=null;
  /**
   * Contains the Graphics information from the KGML
   */
  private Graphics graph=null;
  /**
   * If it is a group node, this list contains the ids of all children.
   */
  private ArrayList<Integer> components = null;
  /**
   * The reverse-argument to the components argument.
   * I.e. if this is contained in a components list, this is
   * the reference back to the node, that contains this node as component.
   */
  private Entry parent = null;
  
  /**
   * The parent pathway object.
   */
  private Pathway parentPathway = null;
  
  /**
   * A node which is removed, but may still be linked to other nodes
   * gets this name assignes.
   */
  protected final static String removedNodeName = "REMOVEDNODE";
  
  /**
   * 
   * @param id
   * @param name
   * @param type
   */
  public Entry(Pathway parentPathway, int id, String name, EntryType type) {
    super();
    this.parentPathway = parentPathway;
    this.id = id;
    this.type = type;
    setName(name);
    //if (type==EntryType.gene) graph = new Graphics(true); else graph = new Graphics(false);
  }
  
  /**
   * 
   * @param id
   * @param name
   * @param type
   * @param link
   * @param reaction
   */
  public Entry(Pathway parentPathway, int id, String name, EntryType type, String link, String reaction) {
    this(parentPathway, id, name, type);
    this.link = link;
    this.reaction = reaction;
  }
  
  /**
   * 
   * @param id
   * @param name
   * @param type
   * @param link
   * @param reaction
   * @param childNodes
   */
  public Entry(Pathway parentPathway, int id, String name, EntryType type, String link, String reaction, NodeList childNodes) {
    this(parentPathway, id,name,type,link,reaction);
    parseSubNodes(childNodes);
  }
  
  /**
   * @param newEntrysId
   * @param substrate
   */
  public Entry(Pathway parentPathway, int id, String name) {
    this(parentPathway, id,name,Entry.getEntryTypeForKeggId(name));
  }

  /**
   * 
   * @return
   */
  public ArrayList<Integer> getComponents() {
    return components==null?new ArrayList<Integer>():components;
  }
  
  /**
   * @return true if and only if the node has children (i.e. if it is a group node).
   */
  public boolean hasComponents() {
    return (components!=null&&components.size()>0);
  }
  
  /**
   * 
   * @return
   */
  public Object getCustom() {
    return custom;
  }
  
  /**
   * 
   * @return the pathway in which this Entry occurs.
   */
  public Pathway getParentPathway() {
    return parentPathway;
  }
  
  /**
   * 
   * @return
   */
  public Graphics getGraphics() {
    return graph;
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
  public String getLink() {
    return link;
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
  public String getReaction() {
    return reaction;
  }

  /**
   * 
   * @return
   */
  public EntryType getType() {
    return type;
  }

  /**
   * 
   * @return
   */
  public boolean hasGraphics() {
    return (graph!=null);
  }
  
  /**
   * @return true if this is a modifier for a certain reaction and the reaction
   * is set.
   */
  public boolean hasReaction() {
    return (getReaction() != null && getReaction().trim().length() != 0);
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
      if (name.equalsIgnoreCase("component")) { // 0 .. *
        if (components==null) components = new ArrayList<Integer>();
        components.add(KeggParser.getNodeValueInt(att, "id"));
      } else if(name.equals("graphics")) { // 0 .. 1
        graph = new Graphics(KeggParser.getNodeValue(att, "name"), KeggParser.getNodeValueInt(att, "x"), KeggParser.getNodeValueInt(att, "y"), GraphicsType.valueOf(KeggParser.getNodeValue(att, "type")), KeggParser.getNodeValueInt(att, "width"), KeggParser.getNodeValueInt(att, "height"), KeggParser.getNodeValue(att, "fgcolor"), KeggParser.getNodeValue(att, "bgcolor"), (type==EntryType.gene) || (type==EntryType.genes));
      }
    }
  }

  /**
   * 
   * @param custom
   */
  public void setCustom(Object custom) {
    this.custom = custom;
  }

  /**
   * 
   * @param id
   */
  public void setId(int id) {
    parentPathway.idChange(this, id);
    this.id = id;
  }

  /**
   * 
   * @param link
   */
  public void setLink(String link) {
    this.link = link;
  }

  /**
   * 
   * @param name
   */
  public void setName(String name) {
    // Convenient helper for compounds (add "cpd:" to "C00186")
    if (name!=null && (name.length()>2) && !name.contains(":")
        && name.charAt(0)=='C' && Character.isDigit(name.charAt(1))) {
      name = "cpd:"+name;
    }
    parentPathway.nameChange(this, name);
    this.name = name;
  }

  /**
   * 
   * @param reaction
   */
  public void setReaction(String reaction) {
    parentPathway.reactionChange(this, name);
    this.reaction = reaction;
  }

  /**
   * 
   * @param type
   */
  public void setType(EntryType type) {
    this.type = type;
  }

  /**
   * If there exists a group node, with this node/entry as component,
   * then this function is the reference, back to the group node.
   * @return
   */
  public Entry getParentNode() {
    if (parent!=null && parent.getName().equals(removedNodeName)) {
      parent = null;
    }
    return parent;
  }

  /**
   * See {@link #getParentNode()}
   */
  public void setParentNode(Entry parent) {
    this.parent = parent;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    sb.append(getClass().getName());
    sb.append(String.format(" Id: %s, Name: '%s', Type: %s", id, name, type.toString()));
    if (reaction!=null && reaction.length()>0) {
      sb.append(String.format(", Reaction: %s", reaction));
    }
    sb.append(String.format(", HasComponents: %s, HasGraphics: %s", hasComponents(), hasGraphics()));
    sb.append(']');
    
    // Missing: link
    
    return sb.toString();
  }

  /**
   * Tryis to infere the EntryType from the id's prefix.
   * @param kgId
   * @return EntryType
   */
  public static EntryType getEntryTypeForKeggId(String kgId) {
    kgId = kgId.toLowerCase().trim();
    if (kgId.startsWith("cpd:")) {
      return EntryType.compound;
    } else if (kgId.startsWith("glycan:") || kgId.startsWith("gl:")) {
      return EntryType.compound;
    } else if (kgId.startsWith("ec:")) {
      return EntryType.enzyme;
    } else if (kgId.startsWith("group:")) {
      return EntryType.group;
    } else if (kgId.startsWith("path:")) { // Link to another pathway
      return EntryType.map;
    } else if (kgId.startsWith("ko:")) {
      return EntryType.ortholog;
    } else if (kgId.contains(":")) {// z.B. hsa:00123, ko:00123
      return EntryType.gene;
    } else {
      return EntryType.other;
    }
  }

}
