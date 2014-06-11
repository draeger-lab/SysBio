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
package de.zbit.sbml.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

import com.sun.org.apache.bcel.internal.generic.NEW;

import de.zbit.sbml.io.QualModelBuilding;

/**
 * @author Stephanie Hoffmann
 * @version $Rev$
 */
public class GetSubRegulationNetwork {
  
  private int encoding = 0;
  private int regulation = 0;
  private int interaction = 0;
  private int complexing = 0;
  private int superFamily = 0;
  private int regulated = 0;
  
  private int speciesAlreadyThere = 0;
  
  /**
   * contains all outgoing transitions
   */
  private Map<String, String> species2transition = new HashMap<String, String>();
  
  /**
   * contains the corresponding count of regulating transitions
   */
  private Map<String, Integer> species2edgeWeight = new HashMap<String, Integer>(); 
  
  /**
   *  contains all regulated genes
   */
  private Set<String> regulatedGenes = new HashSet<String>();
  
  private QualModelPlugin qModel;
  private Model model;
  private QualModelPlugin subQualModel;
  private Model subModel;
  private int maximumDepth;
  
  private int transitionStatistics = 0;
  private int qualSpeciesStatistics = 0;
  
  /**
   * 
   * @param xmlFile
   * @param searchStrings
   * @param creator
   * @param organisms (comma separated)
   * @param searchDepth
   * @throws XMLStreamException
   * @throws IOException
   */
  public GetSubRegulationNetwork(String xmlFile, String searchStrings, String creator, String organisms, String outputFile, int searchDepth) throws XMLStreamException, IOException {
    this(SBMLReader.read(new File (xmlFile)), searchStrings, creator, organisms, outputFile, searchDepth);
  }
  
  /**
   * default constructor, if search depth is not specified
   * @param xmlFile
   * @param searchStrings
   * @param creator
   * @param organisms (comma separated)
   * @throws XMLStreamException
   * @throws IOException
   */
  public GetSubRegulationNetwork(String xmlFile, String searchStrings, String creator, String organisms, String outputFile) throws XMLStreamException, IOException {
    this(xmlFile, searchStrings, creator, organisms, outputFile, 3);
  }
  
  /**
   * 
   * @param doc
   * @param searchStrings
   * @param creator
   * @param organisms (comma separated)
   * @param searchDepth
   * @throws XMLStreamException
   * @throws FileNotFoundException
   * @throws SBMLException
   */
  public GetSubRegulationNetwork(SBMLDocument doc, String searchStrings, String creator, String organisms, String outputFile, int searchDepth) throws SBMLException, FileNotFoundException, XMLStreamException {
    System.out.println("Document read");
    model = doc.getModel();
    qModel = QualModelBuilding.getQualitativeModel(doc);
    System.out.println("start to create the map");
    extractSpecies2transitionMap(qModel.isSetListOfTransitions() ? qModel.getListOfTransitions() : null);
    SBMLDocument subDoc = QualModelBuilding.initializeQualDocument(model.getName(), model.getId(), creator, organisms.split(","));
    subQualModel = QualModelBuilding.qualModel;
    maximumDepth = searchDepth;
    System.out.println("start to extract sub model");
    System.out.println();
    System.out.println("level:geneName=geneId");
    extractSubRegulationNetworkFromSpecies(searchStrings);
    
    QualModelBuilding.writeSBMLDocument(subDoc, outputFile);
    
    System.out.println("\nregulatedGenes: cntRegulationTransition");
    for (String speciesId : regulatedGenes) {
    	System.out.println(subQualModel.getQualitativeSpecies(speciesId).getName() + ": " 
    			+ species2edgeWeight.get(speciesId)
    			+ " --> "
    			+ subQualModel.getQualitativeSpecies(speciesId).getCVTerms()
    			);
    }
    
    System.out.println();
    System.out.println(regulated + " regulated species");
    System.out.println(speciesAlreadyThere + " species are already there");
	System.out.println();
	System.out.println("qualSpecies: " + qualSpeciesStatistics);
	System.out.println("Transitions: " + transitionStatistics);
	System.out.println("-------------------------------");
	System.out.println("encoding: " + encoding);
	System.out.println("regulation: " + regulation);
	System.out.println("interaction: " + interaction);
	System.out.println("complexing: " + complexing);
	System.out.println("super family: " + superFamily);
	
    
    //		SBML2GraphML s2g = new SBML2GraphML(true);
    //		s2g.createGraph(doc);
    //		System.out.println("graph is created");
    //		Map<String,LinkedList<Edge>> id2edge = s2g.getId2edge();
    //
    //		LinkedList<Edge> edges = id2edge.get("tr12");
    //		for (Edge e : edges) {
    //			System.out.println(e.toString());
    //		}
  }
  
  /**
   * 
   * @param searchStrings
   * @param depth
   */
  private void extractSubRegulationNetworkFromSpecies(String searchStrings) {
    String[] search = searchStrings.split(",");
    transferTransitions(search,1);
    //			String[] ids = getSpeciesIdsFromName(search[i]);
    
  }
  
  /**
   * extract the corresponding transitions of the qual species to the submodel
   * @param String[] qualSpeciesIDs
   */
  private void transferTransitions(String[] qualSpeciesIDs, int depth) {
    if (depth <= maximumDepth) {
      for (int i = 0; i < qualSpeciesIDs.length; i++) {
        
        if (species2transition.get(qualSpeciesIDs[i]) != null) {
          String[] trIds = species2transition.get(qualSpeciesIDs[i]).split(";");
          
          //			if (trIds != null && trIds.length > 0) {
          for (int j = 0; j < trIds.length; j++) {
        	  String currentId = trIds[j];
//            System.out.print(trIds[j] + " ");
//        	  System.out.print(depthIsImportant(trIds[j]));
            if (depthIsImportant(currentId)) {depth++;} 
//      	  System.out.println(" " + depth);
            String[] qsList = addTransitionToSubmodel(qModel.getTransition(trIds[j]), (depth-1));
            if (qsList != null) {
            	// recursive call of the method
              transferTransitions(qsList, depth);
              if (depthIsImportant(trIds[j])) {depth--;} 
            }
          }
        }
      }
      //			}
    } else {
//    	System.out.println("depth > max");
    }
    
  }
  
  /*
   * which kind of transitions should be penalized
   */
  private boolean depthIsImportant(String transitionID) {
	  return (transitionID.startsWith("reg")); // || transitionID.startsWith("cx"));
  }

/**
   * add the transition and corresponding Species to the submodel as long as they are not already included
   * @param transition
   */
  private String[] addTransitionToSubmodel(Transition transition, int currentDepth) {
	  if (subQualModel.getTransition(transition.getId()) == null) {
		  String[] qsList = new String[transition.getOutputCount()];
		  int cnt = 0;
		  if (transition.isSetListOfInputs()) {
			  for (Input i : transition.getListOfInputs()) {
				  if (subQualModel.getQualitativeSpecies(i.getQualitativeSpecies()) == null) {
					  QualitativeSpecies qs = qModel.getQualitativeSpecies(i.getQualitativeSpecies()).clone();
					  //					qs.unregister(model);
					  subQualModel.addQualitativeSpecies(qs);
					  qualSpeciesStatistics++;
				  }
			  }
		  }
		  if (transition.isSetListOfOutputs()) {
			  for (Output o : transition.getListOfOutputs()) {
				  if (subQualModel.getQualitativeSpecies(o.getQualitativeSpecies()) == null) {
					  if (transition.getId().startsWith("reg")) {
						  System.out.println(currentDepth + ":" + qModel.getQualitativeSpecies(o.getQualitativeSpecies()).getName() + "=" + o.getQualitativeSpecies());
						  regulatedGenes.add(o.getQualitativeSpecies());
						  regulated++;
						  species2edgeWeight.put(o.getQualitativeSpecies(), 1);
					  }
					  subQualModel.addQualitativeSpecies(qModel.getQualitativeSpecies(o.getQualitativeSpecies()).clone());
					  qualSpeciesStatistics++;
					  qsList[cnt] = o.getQualitativeSpecies();
					  cnt++;
				  }
				  else {
					  if (transition.getId().startsWith("reg")) {
//						  System.out.println(currentDepth + ":" + qModel.getQualitativeSpecies(o.getQualitativeSpecies()).getName() + "=" + o.getQualitativeSpecies());
						  species2edgeWeight.put(o.getQualitativeSpecies(), (species2edgeWeight.get(o.getQualitativeSpecies()) + 1));
					  }
					speciesAlreadyThere++;
				}
			  }
		  }
		  if (subQualModel.getTransition(transition.getId()) == null) {
			  Transition t = transition.clone();
			  subQualModel.addTransition(t);
			  transitionStatistics++;
			  makeStatistics(t);
			  return qsList;
		  }
		  else return null;
	  }
	  else return null;
  }
  
  /*
   * counts the type of transitions for statistics
   */
  private void makeStatistics(Transition t) {
	  if (t.getId().startsWith("tr")) {
		  encoding++; 
//		  System.out.println("tr");
	  } else if (t.getId().startsWith("reg")) {
		  regulation++; 
//		  System.out.println("reg");
	  } else if (t.getId().startsWith("int")) {
		  interaction++; 
//		  System.out.println("int");
	  } else if (t.getId().startsWith("cx")) {
		  complexing++; 
//		  System.out.println("cx");
	  } else if (t.getId().startsWith("sf")) {
		  superFamily++; 
//		  System.out.println("sf");
	  } else {
		  System.out.println("unknown start of transition id" + t.getId());
	  }
	  

  }

private String[] getSpeciesIdsFromName(String string) {
    return null;
    // TODO über equal transitions den rest
  }
  
  /**
   * 
   * @param transitions
   * @return true if species are mapped to the transitions, in which they participate
   */
  private boolean extractSpecies2transitionMap(List<Transition> transitions) {
    if ((transitions != null) && (transitions.size() > 0)) {
      for (Transition t : transitions) {
        if (t.isSetListOfInputs()) {
          for (Input i : t.getListOfInputs()) {
            if (i.getQualitativeSpecies() != null) {
              String helper;
              //						ListOf<Transition> trList;
              //TODO test extraction
              if (species2transition.get(i.getQualitativeSpecies()) != null) {
                //							trList = species2transition.get(i.getQualitativeSpecies());
                helper = species2transition.get(i.getQualitativeSpecies());
              }
              else {
                //							trList = new ListOf<Transition>(model.getLevel(), model.getVersion());
                helper = "";
              }
              
              String trList = helper + t.getId() + ";";
              //						Transition tClone = t.clone();
              //						tClone.unregister(transitions);
              //						trList.add(tClone);
              //						species2transition.put(i.getQualitativeSpecies(), trList);
              species2transition.put(i.getQualitativeSpecies(), trList);
            }
          }
        }
      }
      return true;
    }
    else {
      return false;
    }
  }
  
  
  /**
   * @param args
   * @throws XMLStreamException
   * @throws IOException
   */
  public static void main(String[] args) throws XMLStreamException, IOException {
    if (args.length == 6) {
      new GetSubRegulationNetwork(args[0], args[1], args[2], args[3], args[4], (Integer.parseInt(args[5])));
    }
    else if (args.length == 5) {
      new GetSubRegulationNetwork(args[0], args[1], args[2], args[3], args[4]);
    }
    else {
      System.out.println("usage: " +
      	  "[Model.xml] " +
          "[search strings comma separated] " +
          "[creator] " +
          "[commaseparated allowed organisms (e.g., human,Mammalia)] " +
          "[outputfile] " +
          "[search depth] \n" +
          "or usage: [Model.xml] " +
          "[search string comma separated] " +
          "[creator] " +
          "[commaseparated allowed organisms (e.g., human,Mammalia)] " +
          "[outputfile] " +
          "(default search depth of 3 is assumed)");
    }
    
  }
  
}
