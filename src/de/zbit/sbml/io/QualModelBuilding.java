/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2012-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sbml.io;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.qual.QualConstants;
import org.sbml.jsbml.ext.qual.QualModelPlugin;

import de.zbit.util.DatabaseIdentifierTools;
import de.zbit.util.DatabaseIdentifiers.IdentifierDatabases;
import de.zbit.util.EscapeChars;

/**
 * @author Roland Keller, Stephanie Hoffmann
 * @version $Rev$
 */
public abstract class QualModelBuilding {
  
  public static QualModelPlugin qualModel;
  public static Layout layout;
  public static Model model;
  
  protected final static String notesStartString = "<notes><body xmlns=\"http://www.w3.org/1999/xhtml\">";
  protected final static String notesEndString = "</body></notes>";
  
  /**
   * contains the NCBI taxonomy identifier for organisms
   */
  protected static Map<String, String> ncbiTaxonomyMap = new HashMap<String, String>();
  
  /**
   * 
   * @param modelName
   * @param modelID
   * @param creator
   * @param taxon
   * @param organism
   * @return
   * @throws XMLStreamException
   */
  public static SBMLDocument initializeQualDocument(String modelName, String modelID, String creator, String[] organisms) throws XMLStreamException {
    initTaxonomyMap();
    
    SBMLDocument doc = new SBMLDocument(3, 1);
    doc.addDeclaredNamespace("xmlns" + ':' + QualConstants.shortLabel, QualConstants.namespaceURI);
    
    model = doc.createModel(modelID);
    model.setMetaId("meta_" + modelID);
    for (int i = 0; i < organisms.length; i++) {
      CVTerm term = DatabaseIdentifierTools.getCVTerm(IdentifierDatabases.NCBI_Taxonomy, null, ncbiTaxonomyMap.get(organisms[i]));
      model.addCVTerm(term);
    }
    
    StringBuffer notes = new StringBuffer(notesStartString);
    notes.append(String.format("<h1>%s</h1>\n", formatTextForHTMLnotes(modelName)));
    model.setName(String.format("%s", modelName));
    
    notes.append(notesEndString);
    model.setNotes(notes.toString());
    if (!model.getHistory().isSetCreatedDate()) {
      model.getHistory().setCreatedDate(Calendar.getInstance().getTime());
    }
    else {
      model.getHistory().addModifiedDate(Calendar.getInstance().getTime());
    }
    
    qualModel = new QualModelPlugin(model);
    model.addExtension(QualConstants.namespaceURI, QualModelBuilding.qualModel);
    
    LayoutModelPlugin layoutExt = new LayoutModelPlugin(model);
    model.addExtension(LayoutConstants.namespaceURI, layoutExt);
    layout = layoutExt.createLayout();
    
    return doc;
  }
  
  /**
   * Escapes all HTML-tags in the given string and
   * replaces new lines with a space.
   * @param text
   * @return
   */
  public static String formatTextForHTMLnotes(String text) {
    if (text==null) {
      return "";
    }
    return EscapeChars.forHTML(text.replace('\n', ' '));
  }
  
  /**
   * initializes the taxonomy identifier
   */
  private static void initTaxonomyMap() {
    ncbiTaxonomyMap.put("human", "9606");
    ncbiTaxonomyMap.put("mouse", "10090");
    ncbiTaxonomyMap.put("rat", "10114");
    ncbiTaxonomyMap.put("yeast", "4932");
    ncbiTaxonomyMap.put("fruit fly", "7227");
    ncbiTaxonomyMap.put("clawed frog", "8355");
    ncbiTaxonomyMap.put("monkey", "9533");
    ncbiTaxonomyMap.put("chick", "9031");
    ncbiTaxonomyMap.put("Mammalia", "40674");
  }
  
  /**
   * 
   * @param {@link SBMLDocument}
   * @return {@link QualModelPlugin} if existing
   */
  public static QualModelPlugin getQualitativeModel(SBMLDocument doc) {
    if (doc != null) {
      Model m = doc.getModel();
      if (m != null) {
        QualModelPlugin qModel = (QualModelPlugin)m.getExtension(QualConstants.namespaceURI);
        if (qModel != null) {
          return qModel;
        }
      }
    }
    return null;
  }
  
  /**
   * 
   * @param doc
   * @param outputFile
   * @throws SBMLException
   * @throws FileNotFoundException
   * @throws XMLStreamException
   */
  public static void writeSBMLDocument(SBMLDocument doc, String outputFile) throws SBMLException, FileNotFoundException, XMLStreamException {
    SBMLWriter.write(doc, outputFile, "Sysbio-Project", "1");
  }
  
}
