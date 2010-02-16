package de.zbit.kegg.parser.pathway;

import java.util.ArrayList;


import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;

public class Relation {
  int entry1;
  int entry2;
  RelationType type;
  
  ArrayList<SubType> subtypes = new ArrayList<SubType>();
  
  public Relation(int entry1, int entry2, RelationType type) {
    super();
    this.entry1 = entry1;
    this.entry2 = entry2;
    this.type = type;
  }
  public Relation(int entry1, int entry2, RelationType type, NodeList childNodes) {
    this(entry1, entry2, type);
    parseSubNodes(childNodes);
  }
  
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
  
  public int getEntry1() {
    return entry1;
  }
  public void setEntry1(int entry1) {
    this.entry1 = entry1;
  }
  public int getEntry2() {
    return entry2;
  }
  public void setEntry2(int entry2) {
    this.entry2 = entry2;
  }
  public RelationType getType() {
    return type;
  }
  public void setType(RelationType type) {
    this.type = type;
  }
  public ArrayList<SubType> getSubtypes() {
    return subtypes;
  }
  
  
  
}
