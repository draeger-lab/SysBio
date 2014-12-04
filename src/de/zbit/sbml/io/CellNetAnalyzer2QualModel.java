/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.text.parser.ParseException;

import de.zbit.io.csv.CSVReader;

/**
 * @author Roland Keller
 * @version $Rev$
 */

public class CellNetAnalyzer2QualModel {
  
  /**
   * 
   * @param modelName
   * @param speciesFile
   * @param reactionFile
   * @return
   * @throws IOException
   * @throws ParseException
   * @throws XMLStreamException
   */
  public static SBMLDocument createQualitativeModel(String modelName, String modelID, String creator, String speciesFile, String reactionFile, String taxon, String organism) throws IOException, ParseException, XMLStreamException {
    String[] org = organism.split(",");
    SBMLDocument sbmlDoc = QualModelBuilding.initializeQualDocument(modelName, modelID, creator, org);
    
    Model model = QualModelBuilding.model;
    QualModelPlugin qModel = QualModelBuilding.qualModel;
    Layout layout = QualModelBuilding.layout;
    
    //add species
    CSVReader reader = new CSVReader(speciesFile);
    
    String[] line = reader.getNextLine();
    while (line != null) {
      String speciesId = line[0];
      QualitativeSpecies q = new QualitativeSpecies(speciesId);
      qModel.addSpecies(q);
      if(speciesId.contains("_gene")) {
        q.setSBOTerm(354);
      }
      
      if (line.length >= 6) {
        GraphicalObject go = null;
        SpeciesGlyph sg = new SpeciesGlyph(q.getId() + "_l",
          model.getLevel(), model.getVersion());
        sg.setSpecies(q.getId());
        layout.addSpeciesGlyph(sg);
        go = sg;
        if (go != null) {
          // TODO
          int width = 70;
          int height = 25;
          double x = Double.valueOf(line[4]);
          double y = Double.valueOf(line[5]);
          BoundingBox bb = go.createBoundingBox(width, height, 0);
          bb.setPosition(new Point(x, y, 0));
        }
      }
      line = reader.getNextLine();
    }
    
    //add reactions
    reader = new CSVReader(reactionFile);
    int num=0;
    for(String math : reader.getColumn(0)) {
      String outputId = math.split("=")[1];
      String formula = math.split("=")[0];
      Transition t = qModel.createTransition("tr"+num);
      t.createOutput(t.getId() + "_out_" + outputId, outputId, OutputTransitionEffect.production);
      ASTNode node = ASTNode.parseFormula(formula);
      t.createFunctionTerm(node);
      determineInputs(qModel, node, t);
      num++;
      
    }
    return sbmlDoc;
  }
  
  /**
   * 
   * @param node
   * @param current
   * @return
   */
  public static void determineInputs(QualModelPlugin model, ASTNode node, Transition t) {
    if(node.getType().equals(ASTNode.Type.PLUS)) {
      node.setType(ASTNode.Type.LOGICAL_AND);
    }
    
    if (node.isName()) {
      QualitativeSpecies sp = model.getQualitativeSpecies(node.getName());
      if (sp != null) {
        Input i = t.createInput(t.getId() + "_in_" + sp.getId(), sp, InputTransitionEffect.none);
        ASTNode parent = (ASTNode) node.getParent();
        if((parent != null) && (parent.getType().equals(ASTNode.Type.LOGICAL_NOT))) {
          i.setSign(Sign.negative);
        }
        else {
          i.setSign(Sign.positive);
        }
      }
    }
    
    else {
      for (ASTNode child : node.getChildren()) {
        determineInputs(model, child, t);
      }
    }
    
    
  }
  
  public static void main(String[] args) throws IOException, ParseException, SBMLException, XMLStreamException {
    SBMLDocument sbmlDoc = createQualitativeModel("IL_6", "IL_6", "Roland Keller", args[0], args[1], args[2], args[3]); // e.g.: .. , .. ,9606, human
    SBMLWriter.write(sbmlDoc, args[2], "BuildToyModelTest", "1");
  }
  
  
}
