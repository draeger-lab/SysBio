package de.zbit.kegg.parser;
import java.io.BufferedReader;
import java.io.File;
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
   * if silent=false, a few debugging outputs will occur during parsing.
   */
  public static boolean silent = true;
  /**
   * If true, the doctype reference will be removed from your document.
   * E.g. "<!DOCTYPE pathway SYSTEM "http://www.genome.jp/kegg/xml/KGML_v0.7.0_.dtd">"
   * will be changed to "<!DOCTYPE pathway>". This will prevent the java xml parser from
   * going online and downloading the given namespace file.
   */
  public static boolean offlineVersion=false;
  
  /**
   * Parse kgml files.
   * @param filename
   * @return all pathways in the given kgml file.
   * Usually, there is only one pathway per file, so the size of the
   * collection should always be 1.
   */
  public static ArrayList<Pathway> parse(String filename) {
    InputSource inS = new InputSource(new BufferedReader(OpenFile.openFile(filename)));
    return parse(inS);
  }
  
  /**
   * Parse kgml files.
   * @param inS
   * @return all pathways in the given kgml file.
   */
  public static ArrayList<Pathway> parse(InputSource inS) {
    if (inS==null) return null;
    
    double kgmlVersion = getKGMLVersion(inS.getCharacterStream());
    if (offlineVersion || kgmlVersion<=0.5) {
      // Remove System + Url in "<!DOCTYPE pathway SYSTEM "http://www.genome.jp/kegg/xml/KGML_v0.7.0_.dtd">"
      Reader s = inS.getCharacterStream();
      char c; String line="";
      StringBuffer sb = new StringBuffer();
      try {
        while (s.ready()) {
          c = (char)s.read();
          line += c;
          if (c=='\n') {
            if (offlineVersion && line.contains("SYSTEM") && line.contains("http://") && line.contains("pathway")) {
              kgmlVersion = getKGMLVersion(line); // Get KGML version
              // Remove the URL
              line = line.substring(0, line.indexOf("pathway")+"pathway".length()) + line.substring(line.lastIndexOf(">"));
            }
            
            // Remove invalid XML code in older KGML versions
            if (kgmlVersion<=0.5 && line.contains("&keywords="))
              line = line.replace("&keywords=", "");
            
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
      
      // Give a warning if version does not match.
      try {
        if (kgmlVersion==0)
          kgmlVersion = getKGMLVersion(document);
        if (kgmlVersion>0 && kgmlVersion<0.7) {
          System.out.println("WARNING: Your kgml document is rather old.\n"+
            "It is written in kgml version " + kgmlVersion + ". This parser is for version 0.7 / 0.71.\n"+
            "Trying to read your document in compatibility mode.");
        } else if (kgmlVersion>0 && kgmlVersion>=0.8) {
          System.out.println("WARNING: Your kgml document is rather new.\n"+
              "It is written in kgml version " + kgmlVersion + ". This parser is for version 0.7 / 0.71.\n"+
              "Trying to read your document anyways.");
        }
      } catch (Exception e) {} // doesn't matter.
      
      return parseKeggML(document);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
	/**
	 * Returns the KGML Version, parsed from the SystemID URL String. Returns 0
	 * if an error occurs / no version could be parsed.
	 * 
	 * @param document
	 * @return KGML version number
	 */
	private static double getKGMLVersion(Document document) {
		double ret = 0;
		// e.g. http://www.genome.jp/kegg/xml/KGML_v0.6.1_.dtd
		String s = document.getDoctype().getSystemId();
		if (s != null) {
			ret = parseNextDouble(s, s.lastIndexOf('v'), true);
		}
		return ret;
	}
  
  /**
   * Returns the version information from a string like
   * <!DOCTYPE pathway SYSTEM "http://www.genome.jp/kegg/xml/KGML_v0.6.1_.dtd">
   * @param SYSTEMline
   * @return KGML version number
   */
  private static double getKGMLVersion(String SYSTEMline) {
    double ret = 0;
    try {
      // e.g. http://www.genome.jp/kegg/xml/KGML_v0.6.1_.dtd
      if (SYSTEMline!=null)
        ret = parseNextDouble(SYSTEMline, SYSTEMline.lastIndexOf('v'),true);
    } catch (Exception e){
      e.printStackTrace();
      if (SYSTEMline!=null) {
        System.err.println("Could not parse Pathway version from '" + SYSTEMline + "'.");
      }
    }
    
    return ret;
  }
  
  /**
   * Fetches the head of the reader (and resets the stream afterwards)
   * and returns the KGML version.
   * Parsing of the version is done as in {@link #getKGMLVersion(String)}.
   * @param in
   * @return KGML version number
   */
  private static double getKGMLVersion(Reader in) {
    // Parse the head
    double ret = 0;
    String s = null;
    try {
      // Set a mark
      Reader bin;
      if (in.markSupported()) {
        bin = in;
      } else {
        bin = new BufferedReader(in);
      }
      bin.mark(200);
      // Read the head
      char[] head = new char[200];
      bin.read(head);
      // Reset stream and parse the version
      bin.reset();
      
      s = new String(head);
      if (s!=null)
        ret = parseNextDouble(s, s.lastIndexOf('v'),true);
    } catch (Exception e){
      e.printStackTrace();
      if (s!=null) {
        System.err.println("Could not parse Pathway version from '" + s + "'.");
      }
    }
    
    return ret;
  }
  
  /**
   * Returns the comment of the document, if available. Usually
   * e.g. "Creation date: Mar 16 2007 16:05:56 +0900 (JST)".
   * @param document
   * @return
   */
  private static String getKGMLComment(Document document) {
    String comment = "";
    
    if (document.getDoctype().getNextSibling()!=null) {
      comment = document.getDoctype().getNextSibling().getNodeValue();
    }
    
    return comment.trim();
  }
  
  /**
   * Parses the next double from a string, starting at the
   * given position. Parses "0.6" but not ".6" (number must start
   * with a digit).
   * @param string
   * @param position
   * @param allowMultipledots - if true, e.g., will return 0.61 for 0.6.1
   *                            if false: 0.6 will be returned.
   * @return
   */
  public static double parseNextDouble(String string, int position, boolean allowMultipledots) {
    if (position<0) return 0;
    char[] ca = string.substring(position).toCharArray();
    boolean parsedDot = false;
    boolean foundDigit = false;
    String myDouble="";
    for (char c:ca) {
      if (!Character.isDigit(c) && !foundDigit) continue; // Search for first digit.
      if (Character.isDigit(c)) {
        foundDigit = true;
        myDouble+=c;
      } else if (c=='.' && !parsedDot) {
        parsedDot = true;
        myDouble+=c;
      } else if (c=='.' && parsedDot && allowMultipledots) {
        // Do nothing. Simply skip this dot.
      } else {
        break;
      }
    }
    return Double.parseDouble(myDouble);
  }
  
  /**
   * Parses a KGML XML document, returning all contained pathways.
   * Usually this is in 99.9999% just one ;-)
   * @param doc
   * @return
   */
  public static ArrayList<Pathway> parseKeggML(Document doc) {
    ArrayList<Pathway> ret = parseKeggML(doc.getChildNodes());
    double version = getKGMLVersion(doc);
    String comment = getKGMLComment(doc);
    if (comment.trim().length()<1) comment = null;
    if (ret!=null) {
      for (Pathway p : ret) {
        p.setVersion(version);
        p.setComment(comment);
      }
    }
    return ret;
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
        
        // Creates back references of group nodes
        createGroupNodeBackReferences(p);
      }
      
    }
    return pathways;
  }
  
  /**
   * Creates back-references for childs of group nodes, to the
   * group node.
   * @param p
   */
  private static void createGroupNodeBackReferences(Pathway p) {
    for (Entry e:p.getEntries()) {
      if (e.hasComponents()) {
        for (Integer c: e.getComponents()) {
          Entry e2 = p.getEntryForId(c);
          if (e2!=null) e2.setParentNode(e);
        }
      }
    }
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
    boolean error = false;
    Exception ex = null;
    if (n.getNamedItem(attribute)!=null)
      try {
        number = Integer.parseInt(getNodeValue(n, attribute));
        error = false; // Parsing was succesfull.
      } catch (Exception e) {
        error = true;
        ex=e;
        System.err.println("Error while parsing int '" + attribute + "' => " + getNodeValue(n, attribute));
      }
      if (error) {
        // In old kegg definitions, number is often e.g. "04010hsa" instead of "04010".
        // Removing the "hsa" fixes the problem in a compatibility-mode-way.
        // I think it's still better than throwing errors.
        String s = getNodeValue(n, attribute);
        for (int i=0; i<s.length(); i++) {
          if (!Character.isDigit(s.charAt(i))) {
            s = s.replace(Character.toString(s.charAt(i)), "");
            i--;
          }
        }
        if (s.length()>0) {
          number = Integer.parseInt(s);
          System.err.println("Going into compatibility mode and returning '" + number + "'.");
        } else {
          if (ex!=null) {
            ex.printStackTrace();
          }
        }
      }
    return number;
  }
  
  /**
   * Just for testing this parser.
   * @param args
   */
  public static void main(String[] args) {
    // For simple testing
    if (args==null || args.length<1)
      args = new String[]{"files/kgmlSample.xml"};
    
    if (args!=null && args.length >0) {
      File f = new File(args[0]);
      if (f.isDirectory()) 
        System.err.println("Can't parse directories.");
      else {
        // Parse the pathway.
        Pathway p = KeggParser.parse(args[0]).get(0);
        
        System.out.println(p);
      }
      return;
    }
  }
  
}
