/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.EntryType;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.kegg.parser.pathway.Reaction;
import de.zbit.kegg.parser.pathway.ReactionComponent;
import de.zbit.kegg.parser.pathway.Relation;
import de.zbit.kegg.parser.pathway.SubType;
import de.zbit.kegg.parser.pathway.ext.EntryExtended;
import de.zbit.util.StringUtil;

/**
 * This class writes an kgml file out of a pathway element
 * 
 * BE CAREFULL UP TO NOW THERE IS NO CHECK IF THE DOCUMENT IS CORRECT
 * 
 * 
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class KGMLWriter {

  /**
   * indent for the xml entries
   */
  static String indent = "4";

  public static final Logger log = Logger.getLogger(KGMLWriter.class.getName());


  /**
   * If the fileName is not set it will be set automatically to the pathway
   * name. The file will be saved in the current folder.
   * 
   * @param keggPW
   * @param writeEntryExtended if is set true the extended KGML is written for {@link EntryExtended}, 
   * otherwise the basic KGML is written with normal {@link Entry}
   * @throws XMLStreamException 
   * @throws FileNotFoundException 
   */
  public static void writeKGML(de.zbit.kegg.parser.pathway.Pathway keggPW, boolean writeEntryExtended)  {
    if (keggPW!=null && keggPW.getEntries().size() > 0) {
      String fileName = createFileName(keggPW);

      writeKGML(keggPW, fileName, writeEntryExtended);
    }
  }

  /**
   * Uses the keggPW title to create a fileName
   * 
   * @param keggPW
   * @return
   */
  public static String createFileName(de.zbit.kegg.parser.pathway.Pathway keggPW) {
    String fileName = keggPW.getTitle() + ".xml";
    fileName = fileName.replace(" ", "_");

    if (fileName == null) {
      fileName = keggPW.getName();
      if (fileName == null) {
        fileName = Integer.toString(keggPW.hashCode());
      }
    }
    if (!fileName.toLowerCase().endsWith(".xml")) {
      fileName += ".xml";
    }

    fileName = StringUtil.removeAllNonFileSystemCharacters(fileName);
    return fileName;
  }
  
  /**
   * if the fileName is not set it will be set automatically to the pathway
   * name. The file will be saved in the current folder
   * 
   * @param keggPW 
   * @param writeEntryExtended if is set true the extended KGML is written for {@link EntryExtended}, 
   * otherwise the basic KGML is written with normal {@link Entry}
   * @param fileName
   */
  public static void writeKGML(de.zbit.kegg.parser.pathway.Pathway keggPW, String fileName, 
      boolean writeEntryExtended) {
    ArrayList<de.zbit.kegg.parser.pathway.Entry> entries = keggPW.getEntries();

    if (entries.size() > 0) {
      int counter = 0;
      int counterMacro = 0;
      int counterMaps = 0;

      for (de.zbit.kegg.parser.pathway.Entry entry : entries) {
        if (entry.getType().equals(EntryType.group))
          counter++;
        if (entry.getType().equals(EntryType.map))
          counterMaps++;
        if (entry.getType().equals(EntryType.compound))
          counterMacro++;
      }
      log.info("Entries.size(): " + entries.size() + " - " + counter + " are groups, " + " "
          + counterMacro + " are marcromolecules, " + " " + counterMaps + " are maps.");
      log.info("Reactions.size(): " + keggPW.getReactions().size());
      log.info("Relations.size(): " + keggPW.getRelations().size());

      
      Document doc = null;
      try {
        doc = createDocument(keggPW, writeEntryExtended);
      } catch (ParserConfigurationException e) {
        log.log(Level.SEVERE, "Could not create a document from the KEGG pathway.", e);
      }
      writeKGMLFileFromDoc(doc, fileName);
    }
  }
  

  /**
   * writes a {@link Document} to an xml file
   * 
   * @param doc
   * @param fileName
   * @throws FileNotFoundException
   * @throws XMLStreamException
   */
  public static void writeKGMLFileFromDoc(Document doc, String fileName) {
    try {    
      // write the content into xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();

      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent);
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.genome.jp/kegg/xml/KGML_v0.7.1_.dtd");
      DOMSource source = new DOMSource(doc);      
      
      File outFile = new File(fileName);
      try {
        new File(outFile.getParent()).mkdirs();
      } catch (Throwable t) {
        // Just ensure that the directory is available.
      }
      StreamResult result = new StreamResult(outFile);

      // Output to console for testing
      // StreamResult result = new StreamResult(System.out);
      transformer.transform(source, result);

      log.info("File '" + fileName + "' saved!");

    } catch (TransformerException tfe) {
      tfe.printStackTrace();
    }
  }

  /**
   * 
   * @param keggPW
   * @param writeEntryExtended if is set true the extended KGML is written for {@link EntryExtended}, 
   * otherwise the basic KGML is written with normal {@link Entry}
   * @return
   * @throws ParserConfigurationException
   */
  private static Document createDocument(Pathway keggPW, boolean writeEntryExtended) throws ParserConfigurationException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    
    // root element pathway
    Element rootElement = doc.createElement("pathway");
    Map<String, String> attributes = keggPW.getKGMLAttributes();
    for (java.util.Map.Entry<String, String> att : attributes.entrySet()) {
         rootElement.setAttribute(att.getKey(), att.getValue());
    }
    if(writeEntryExtended && keggPW.isSetAdditionalText()) {
      rootElement.setAttribute("additionalText", keggPW.getAdditionalText());
    }
    doc.appendChild(rootElement);
    
    // kegg entries
    ArrayList<Entry>  entries = keggPW.getEntries();
    if(entries.size()>0) {
      for (Entry entry : entries) {
        Element newChild = doc.createElement("entry");
        Map<String, String> entryMap = null;
        if(writeEntryExtended) {
          try{
          entryMap = ((EntryExtended)entry).getKGMLAttributes();
          } catch (ClassCastException e) {
            entryMap = entry.getKGMLAttributes();  
          }
        } else {
          if (entry instanceof EntryExtended) {
            entryMap = ((EntryExtended) entry).getKGMLAttributes(false);
          } else {
            entryMap = ((Entry)entry).getKGMLAttributes();
          }
        }
        for (java.util.Map.Entry<String, String> att : entryMap.entrySet()) {
          newChild.setAttribute(att.getKey(), att.getValue());
        }
        
        if (entry.isSetComponent()) {
          for (int component : entry.getComponents()) {
            Element newChild2 = doc.createElement("component");
            newChild2.setAttribute("id", String.valueOf(component));
            newChild.appendChild(newChild2);              
          }
        }
        
        if (entry.isSetGraphics()) {
          Element newChild2 = doc.createElement("graphics");
          for (java.util.Map.Entry<String, String> graphic : entry.getGraphics().getKGMLAttributes().entrySet()) {
            newChild2.setAttribute(graphic.getKey(), graphic.getValue());
          } 
          newChild.appendChild(newChild2);
        }
        
        rootElement.appendChild(newChild);
      }
    }    
    
    // kegg relations
    ArrayList<Relation>  relations = keggPW.getRelations();
    if(relations!=null && relations.size()>0) {
      for (Relation relation : relations) {
        Element newChild = doc.createElement("relation");
        for (java.util.Map.Entry<String, String> att : relation.getKGMLAttributes().entrySet()) {
          newChild.setAttribute(att.getKey(), att.getValue());
        }
        if (relation.isSetSubTypes()) {
          for (SubType subtype : relation.getSubtypes()) {
            Element newChild2 = doc.createElement("subtype");
            for (java.util.Map.Entry<String, String> att1 : (subtype.getKGMLAttributes()).entrySet()) {
              newChild2.setAttribute(att1.getKey(), att1.getValue());  
            }              
            newChild.appendChild(newChild2);              
          }
        }

        rootElement.appendChild(newChild);
      }
    }
    
 // kegg reactions
    ArrayList<Reaction>  reactions = keggPW.getReactions();
    if(reactions.size()>0) {
      for (Reaction reaction : reactions) {
        Element newChild = doc.createElement("reaction");
        for (java.util.Map.Entry<String, String> att : (reaction.getKGMLAttributes()).entrySet()) {
          newChild.setAttribute(att.getKey(), att.getValue());
        }
        if (reaction.isSetProduct()) {
          for (ReactionComponent product : reaction.getProducts()) {
            Element newChild2 = doc.createElement("product");
            for (java.util.Map.Entry<String, String> att1 : (product.getKGMLAttributes()).entrySet()) {
              newChild2.setAttribute(att1.getKey(), att1.getValue());  
              if (product.isSetAlt()) {
                Element newChild3 = doc.createElement("alt");
                newChild3.setAttribute("name", product.getAlt().getName());
                newChild2.appendChild(newChild3);
              }
            }              
            newChild.appendChild(newChild2);              
          }
        }
        if (reaction.isSetSubstrate()) {
          for (ReactionComponent substrate : reaction.getSubstrates()) {
            Element newChild2 = doc.createElement("substrate");
            for (java.util.Map.Entry<String, String> att1 : (substrate.getKGMLAttributes()).entrySet()) {
              newChild2.setAttribute(att1.getKey(), att1.getValue());  
              if (substrate.isSetAlt()) {
                Element newChild3 = doc.createElement("alt");
                newChild3.setAttribute("name", substrate.getAlt().getName());
                newChild2.appendChild(newChild3);
              }
            }              
            newChild.appendChild(newChild2);              
          }
        }
        rootElement.appendChild(newChild);
      }
    }
    
    
    return doc;
  }

}
