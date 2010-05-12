package de.zbit.kegg.parser;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import de.zbit.io.OpenFile;
import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.EntryType;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.kegg.parser.pathway.Reaction;
import de.zbit.kegg.parser.pathway.ReactionType;
import de.zbit.kegg.parser.pathway.Relation;
import de.zbit.kegg.parser.pathway.RelationType;


/**
 * Parses a Kegg Pathway (in KGML (*.xml) format).
 * @author wrzodek
 */
public class KeggParser extends DefaultHandler {
  /**
   * 
   */
  public static boolean silent = true;
  /**
   * 
   */
  public static boolean offlineVersion=false;
  
  /**
   * 
   * @param filename
   * @return
   */
  public static ArrayList<Pathway> parse(String filename) {
    InputSource inS = new InputSource(OpenFile.openFile(filename));
    return parse(inS);
  }
  
  /**
   * 
   * @param inS
   * @return
   */
  public static ArrayList<Pathway> parse(InputSource inS) {
    if (inS==null) return null;
    
    if (offlineVersion) {
      // Remove System + Url in "<!DOCTYPE pathway SYSTEM "http://www.genome.jp/kegg/xml/KGML_v0.7.0_.dtd">"
      Reader s = inS.getCharacterStream();
      char c; String line="";
      StringBuffer sb = new StringBuffer();
      try {
        while (s.ready()) {
          c = (char)s.read();
          line += c;
          if (c=='\n') {
            if (line.contains("SYSTEM") && line.contains("http://") && line.contains("pathway"))
              line = line.substring(0, line.indexOf("pathway")+"pathway".length()) + line.substring(line.lastIndexOf(">"));
            sb.append(line);
            line = "";
          }
        }
        if (line.length()==0)sb.append(line);
        
        inS = new InputSource((new StringReader(sb.toString()))); // sb.toString() only klappt nicht ?!?!?
      } catch (IOException e) {e.printStackTrace();}
    }
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
    DocumentBuilder builder;
    try {
      builder = factory.newDocumentBuilder();
      Document document = builder.parse( inS );
      
      return parseKeggML(document.getChildNodes());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * 
   * @param nl
   * @return
   */
  public static ArrayList<Pathway> parseKeggML(NodeList nl) {
    ArrayList<Pathway> pathways = new ArrayList<Pathway>();
    for (int i=0; i<nl.getLength(); i++) {
      Node node = nl.item(i);
      String name = node.getNodeName().trim();
      
      NamedNodeMap att = node.getAttributes();
      if (name.equalsIgnoreCase("pathway") && node.hasChildNodes()) {
        
        if (!silent) System.out.println("Parsing pw...");
        Pathway p = new Pathway(getNodeValue(att, "name"), getNodeValue(att, "org") , getNodeValueInt(att, "number"), getNodeValue(att, "title"), getNodeValue(att, "image"), getNodeValue(att, "link"));
        parsePathway(node.getChildNodes(), p);
        pathways.add(p);
        
      }
      
    }
    return pathways;
  }
  
  /**
   * 
   * @param nl
   * @param p
   */
  private static void parsePathway(NodeList nl, Pathway p) {
    for (int i=0; i<nl.getLength(); i++) {
      Node node = nl.item(i);
      String name = node.getNodeName().trim();
      
      NamedNodeMap att = node.getAttributes();
      if (name.equalsIgnoreCase("entry")) {
        if (!silent) System.out.println("Parsing Entry " + getNodeValue(att,"name") + "...");
        Entry e = new Entry(getNodeValueInt(att, "id"), getNodeValue(att, "name"), EntryType.valueOf(getNodeValue(att,"type")), getNodeValue(att, "link"), getNodeValue(att, "reaction"), node.getChildNodes() );
        p.addEntry(e);
      } else if (name.equalsIgnoreCase("reaction")) {
        if (!silent) System.out.println("Parsing Reaction " + getNodeValue(att,"name") + "...");
        Reaction r = new Reaction(getNodeValue(att, "name"), ReactionType.valueOf(getNodeValue(att,"type")), node.getChildNodes());
        p.addReaction(r);
      } else if (name.equalsIgnoreCase("relation")) {
        if (!silent) System.out.println("Parsing Relation " + getNodeValue(att,"name") + "...");
        Relation r = new Relation(getNodeValueInt(att, "entry1"), getNodeValueInt(att, "entry2"), RelationType.valueOf(getNodeValue(att,"type")), node.getChildNodes());
        p.addRelation(r);
      }
    }
  }
  
  /**
   * 
   * @param n
   * @param attribute
   * @return
   */
  public static String getNodeValue(NamedNodeMap n, String attribute) {
    Node no = n.getNamedItem(attribute);
    String att = no==null ? "":no.getNodeValue();
    return att;
  }
  
  /**
   * 
   * @param n
   * @param attribute
   * @return
   */
  public static int getNodeValueInt(NamedNodeMap n, String attribute) {
    int number = 0;
    if (n.getNamedItem(attribute)!=null)
      try {
        number = Integer.parseInt(getNodeValue(n, attribute));
      } catch (Exception e) {e.printStackTrace();}
    return number;
  }
  
}
