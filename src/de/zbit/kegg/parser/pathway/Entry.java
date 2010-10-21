package de.zbit.kegg.parser.pathway;


import java.util.ArrayList;


import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.zbit.kegg.parser.KeggParser;

/**
 * Corresponding to the Kegg Entry class (see 
 * <a href="http://www.genome.jp/kegg/xml/docs/">KeggAPI</a>)
 * @author wrzodek
 */
public class Entry {
  /**
   * id.type    the ID of this entry in the pathway map
   */
  int id = 0;
  /**
   * keggid.type    the KEGGID of this entry
   */
  String name = "";
  /**
   * entry_type.type    the type of this entry
   */
  EntryType type;
  /**
   * url.type   the resource location of the information about this entry
   */
  String link = "";
  /**
   * keggid.type    the KEGGID of corresponding reaction
   */
  String reaction = "";
  /**
   * For Customize purposes (e.g. saving a corresponding node reference)
   */
  Object custom=null;
  /**
   * 
   */
  Graphics graph=null;
  /**
   * If it is a group node, this list contains the ids of all children.
   */
  ArrayList<Integer> components = null;
  /**
   * The reverse-argument to the components argument.
   * I.e. if this is contained in a components list, this is
   * the reference back to the node, that contains this node as component.
   */
  private Entry parent = null;
  
  /**
   * 
   * @param id
   * @param name
   * @param type
   */
  public Entry(int id, String name, EntryType type) {
    super();
    this.id = id;
    this.name = name;
    this.type = type;
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
  public Entry(int id, String name, EntryType type, String link, String reaction) {
    this(id, name, type);
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
  public Entry(int id, String name, EntryType type, String link, String reaction, NodeList childNodes) {
    this(id,name,type,link,reaction);
    parseSubNodes(childNodes);
  }
  
  /**
   * 
   * @return
   */
  public ArrayList<Integer> getComponents() {
    return components==null?new ArrayList<Integer>():components;
  }
  
  /**
   * 
   * @return
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
        graph = new Graphics(KeggParser.getNodeValue(att, "name"), KeggParser.getNodeValueInt(att, "x"), KeggParser.getNodeValueInt(att, "y"), GraphicsType.valueOf(KeggParser.getNodeValue(att, "type")), KeggParser.getNodeValueInt(att, "width"), KeggParser.getNodeValueInt(att, "height"), KeggParser.getNodeValue(att, "fgcolor"), KeggParser.getNodeValue(att, "bgcolor"), (type==EntryType.gene));
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
    this.name = name;
  }

  /**
   * 
   * @param reaction
   */
  public void setReaction(String reaction) {
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
    return parent;
  }

  /**
   * See {@link #getParentNode()}
   */
  public void setParentNode(Entry parent) {
    this.parent = parent;
  }

}
