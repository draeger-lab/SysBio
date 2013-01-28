/*
 * $Id:  GetNeighboringNodes.java 10:56:18 tscherneck $
 * $URL: GetNeighboringNodes.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml.jsbml.ext.qual.QualConstant;
import org.sbml.jsbml.ext.qual.QualitativeModel;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

import y.base.Edge;
import de.zbit.graph.io.SBML2GraphML;

/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class GetSubRegulationNetwork {

	private ListOf<Transition> encoding;
	private ListOf<Transition> regulation;
	private ListOf<Transition> interaction;
	private ListOf<Transition> selfInteraction;
	private ListOf<Transition> complexing;
	private ListOf<Transition> superFamily;
	private ListOf<Transition> equally;
	
	/**
	 * contains all outgoing transitions
	 */
	private Map<String, String> species2transition = new HashMap<String, String>(); 
	
	private QualitativeModel qModel;
	private Model model;
	private QualitativeModel subQualModel;
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
		extractSpecies2transitionMap(qModel.getListOfTransitions());
		SBMLDocument subDoc = QualModelBuilding.initializeQualDocument(model.getName(), model.getId(), creator, organisms.split(","));
		subQualModel = QualModelBuilding.qualModel;
		maximumDepth = searchDepth;
		System.out.println("start to extract sub model");
		extractSubRegulationNetworkFromSpecies(searchStrings);
		
		QualModelBuilding.writeSBMlDocument(subDoc, outputFile);
		
		System.out.println("statistics:\n transitions: " + transitionStatistics + "\n species: " + qualSpeciesStatistics);
		
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
//			String[] ids = getSpeciesIdsFromName(search[i]);
			transferTransitions(search,0);
	}

	/**
	 * extract the corresponding transitions of the qual species to the submodel
	 * @param String[] qualSpeciesIDs
	 */
	private void transferTransitions(String[] qualSpeciesID, int depth) {
		if (depth <= maximumDepth) {
			for (int i = 0; i < qualSpeciesID.length; i++) {
				
			if (species2transition.get(qualSpeciesID[i]) != null) {
			String[] trIds = species2transition.get(qualSpeciesID[i]).split(","); 
			
//			if (trIds != null && trIds.length > 0) {
				for (int j = 0; j < trIds.length; i++) {
					System.out.println(trIds[j]);
					if (trIds[j].startsWith("reg")) {
						depth++;
					}
					String[] qsList = addTransitionToSubmodel(qModel.getTransition(trIds[j]));
					if (qsList != null) {
						transferTransitions(qsList, depth);
					}
				}
			}
			}
//			}
		}
	}

	/**
	 * add the transition and corresponding Species to the submodel as long as they are not already included
	 * @param transition
	 */
	private String[] addTransitionToSubmodel(Transition transition) {
		if (subQualModel.getTransition(transition.getId()) == null) { 
			String[] qsList = new String[transition.getListOfOutputs().size()];
			int cnt = 0;
			for (Input i : transition.getListOfInputs()) {
				if (subQualModel.getQualitativeSpecies(i.getQualitativeSpecies()) == null) {
					QualitativeSpecies qs = qModel.getQualitativeSpecies(i.getQualitativeSpecies()).clone();
//					qs.unregister(model);
					subQualModel.addQualitativeSpecies(qs);
					qualSpeciesStatistics++;
				}
			}
			for (Output o : transition.getListOfOutputs()) {
				System.out.println("output: " + o.getQualitativeSpecies());
				if (subQualModel.getQualitativeSpecies(o.getQualitativeSpecies()) == null) {
					subQualModel.addQualitativeSpecies(qModel.getQualitativeSpecies(o.getQualitativeSpecies()).clone());
					qualSpeciesStatistics++;
				}
				qsList[cnt] = o.getQualitativeSpecies();
				cnt++;
			}
			Transition t = transition.clone();
			t.unregister(model);
			subQualModel.addTransition(t);
			transitionStatistics++;
			return qsList;
		}
		else {
			return null;
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
	private boolean extractSpecies2transitionMap(ListOf<Transition> transitions) {
		if ((transitions != null) && (transitions.size() > 0)){
			for (Transition t : transitions) {
				for (Input i : (t.getListOfInputs())) {
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
						
						String trList = helper + t.getId() + ",";
//						Transition tClone = t.clone();
//						tClone.unregister(transitions);
//						trList.add(tClone);
//						species2transition.put(i.getQualitativeSpecies(), trList);
						species2transition.put(i.getQualitativeSpecies(), trList);
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
			System.out.println("usage: [Model.xml] " +
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
