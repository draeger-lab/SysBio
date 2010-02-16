package de.zbit.kegg.parser.pathway;

import java.util.ArrayList;


import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;


public class Reaction {
  String Name;
  ReactionType type;
  
  ArrayList<ReactionComponent> substrate = new ArrayList<ReactionComponent>(); // 1..*
  ArrayList<ReactionComponent> product = new ArrayList<ReactionComponent>(); // 1..*
  
  private Reaction(String name, ReactionType type) {
    super();
    this.Name = name;
    this.type = type;
  }
  public Reaction(String name, ReactionType type, ReactionComponent substrate, ReactionComponent product) {
    this (name, type);
    addProduct(product);
    addSubstrate(substrate);
  }
  
  
  public Reaction(String name, ReactionType type, NodeList childNodes) {
    this(name, type);
    parseSubNodes(childNodes);
  }
  
  private void parseSubNodes(NodeList nl) {
    if (nl==null) return;
    
    for (int i=0; i<nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node==null) return;
      String name = node.getNodeName().trim();
      
      NamedNodeMap att = node.getAttributes();
      if (name.equalsIgnoreCase("substrate")) {
        substrate.add(new ReactionComponent(KeggParser.getNodeValue(att, "name"), node.getChildNodes()));
      } else if(name.equals("product")) {
        product.add(new ReactionComponent(KeggParser.getNodeValue(att, "name"), node.getChildNodes()));
      }
    }
  }

  public String getName() {
    return Name;
  }
  public void setName(String name) {
    Name = name;
  }
  public ReactionType getType() {
    return type;
  }
  public void setType(ReactionType type) {
    this.type = type;
  }
  
  public void addSubstrate(ReactionComponent substrate) {
    this.substrate.add(substrate);
  }
  public void addProduct(ReactionComponent product) {
    this.product.add(product);
  }


  public ArrayList<ReactionComponent> getSubstrates() {
    return substrate;
  }


  public ArrayList<ReactionComponent> getProducts() {
    return product;
  }
  
  
  
  
}
