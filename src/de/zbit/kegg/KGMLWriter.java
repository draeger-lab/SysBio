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

import de.zbit.biocarta.BioCartaTools;
import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.kegg.parser.pathway.Reaction;
import de.zbit.kegg.parser.pathway.ReactionComponent;
import de.zbit.kegg.parser.pathway.Relation;
import de.zbit.kegg.parser.pathway.SubType;

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

  public static final Logger log = Logger.getLogger(BioCartaTools.class.getName());


  /**
   * creates first an {@link Document} an then writes it with the defined {@link KGMLWriter#indent}
   * 
   * @param keggPW
   * @param fileName
   * @throws FileNotFoundException
   * @throws XMLStreamException
   */
  public static void writeKGMLFile(de.zbit.kegg.parser.pathway.Pathway keggPW,
      String fileName) throws FileNotFoundException, XMLStreamException {
    Document doc = null;
    try {
      doc = createDocument(keggPW);
    } catch (ParserConfigurationException e) {
      log.log(Level.SEVERE, "Could not create a document from the KEGG pathway.", e);
    }
    writeKGMLFileFromDoc(doc, fileName);
  }
  

  /**
   * writes a {@link Document} to an xml file
   * 
   * @param doc
   * @param fileName
   * @throws FileNotFoundException
   * @throws XMLStreamException
   */
  public static void writeKGMLFileFromDoc(Document doc, String fileName) throws FileNotFoundException, XMLStreamException {
    try {    
      // write the content into xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();

      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent);
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.genome.jp/kegg/xml/KGML_v0.7.1_.dtd");
      DOMSource source = new DOMSource(doc);      
      
      StreamResult result = new StreamResult(new File(fileName));

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
   * @return
   * @throws ParserConfigurationException
   */
  private static Document createDocument(Pathway keggPW) throws ParserConfigurationException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    
    // root element pathway
    Element rootElement = doc.createElement("pathway");
    Map<String, String> attributes = keggPW.getKGMLAttributes();
    for (java.util.Map.Entry<String, String> att : attributes.entrySet()) {
         rootElement.setAttribute(att.getKey(), att.getValue());
    }
    doc.appendChild(rootElement);
    
    // kegg entries
    ArrayList<Entry>  entries = keggPW.getEntries();
    if(entries.size()>0){
      for (Entry entry : entries) {
        Element newChild = doc.createElement("entry");
        for (java.util.Map.Entry<String, String> att : (entry.getKGMLAttributes()).entrySet()) {
          newChild.setAttribute(att.getKey(), att.getValue());
        }
        
        if (entry.isSetComponent()){
          for (int component : entry.getComponents()) {
            Element newChild2 = doc.createElement("component");
            newChild2.setAttribute("id", String.valueOf(component));
            newChild.appendChild(newChild2);              
          }
        }
        
        if (entry.isSetGraphics()){
          Element newChild2 = doc.createElement("graphics");
          for (java.util.Map.Entry<String, String> graphic : entry.getGraphics().getKGMLAttributes().entrySet()) {
            newChild2.setAttribute(graphic.getKey(), graphic.getValue());
          } 
          newChild.appendChild(newChild2);
        }
        
        rootElement.appendChild(newChild);
      }
    }
    
    // kegg reactions
    ArrayList<Reaction>  reactions = keggPW.getReactions();
    if(reactions.size()>0){
      for (Reaction reaction : reactions) {
        Element newChild = doc.createElement("reaction");
        for (java.util.Map.Entry<String, String> att : (reaction.getKGMLAttributes()).entrySet()) {
          newChild.setAttribute(att.getKey(), att.getValue());
        }
        if (reaction.isSetProduct()){
          for (ReactionComponent product : reaction.getProducts()) {
            Element newChild2 = doc.createElement("product");
            for (java.util.Map.Entry<String, String> att1 : (product.getKGMLAttributes()).entrySet()) {
              newChild2.setAttribute(att1.getKey(), att1.getValue());  
              if (product.hasAlt()) {
                Element newChild3 = doc.createElement("alt");
                newChild3.setAttribute("name", product.getAlt().getName());
                newChild2.appendChild(newChild3);
              }
            }              
            newChild.appendChild(newChild2);              
          }
        }
        if (reaction.isSetSubstrate()){
          for (ReactionComponent substrate : reaction.getSubstrates()) {
            Element newChild2 = doc.createElement("substrate");
            for (java.util.Map.Entry<String, String> att1 : (substrate.getKGMLAttributes()).entrySet()) {
              newChild2.setAttribute(att1.getKey(), att1.getValue());  
              if (substrate.hasAlt()) {
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
    
    // kegg relations
    ArrayList<Relation>  relations = keggPW.getRelations();
    if(relations.size()>0){
      for (Relation relation : relations) {
        Element newChild = doc.createElement("relation");
        for (java.util.Map.Entry<String, String> att : relation.getKGMLAttributes().entrySet()) {
          newChild.setAttribute(att.getKey(), att.getValue());
          if (relation.isSetSubTypes()){
            for (SubType subtype : relation.getSubtypes()) {
              Element newChild2 = doc.createElement("subtype");
              for (java.util.Map.Entry<String, String> att1 : (subtype.getKGMLAttributes()).entrySet()) {
                newChild2.setAttribute(att1.getKey(), att1.getValue());  
              }              
              newChild.appendChild(newChild2);              
            }
          }
        }
        rootElement.appendChild(newChild);
      }
    }
    
    
    return doc;
  }

}
