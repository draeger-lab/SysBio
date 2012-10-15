/*
 * $Id$
 * $URL$
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml.jsbml.ext.qual.QualitativeModel;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

public class Transfac2QualModel {
	
	private static final transient Logger logger = Logger.getLogger(Transfac2QualModel.class.getName());
	
	/**
	 * contains the qualitative model
	 */
	private static QualitativeModel qModel;
	
	/**
	 * contains the level of the model
	 */
	private static int level;
	
	/**
	 * contains the version of the model
	 */
	private static int version;
	
	/**
	 * counter for transitions
	 */
	private static int transition = 0;
	
	/**
	 * contains the given model organisms
	 */
	private static String[] modelOrganisms;
	
	/**
	 * contains the corresponding organism to the transcription factor
	 */
	private Map<String, String> organismMap = new HashMap<String, String>();
	
	/**
	 * 
	 * @param modelName
	 * @param modelID
	 * @param creator
	 * @param tfOrganisms
	 * @param bindingFactors
	 * @param bindingSites
	 * @param outputFile
	 * @param organism
	 * @throws IOException
	 * @throws SBMLException
	 * @throws XMLStreamException
	 */
	public Transfac2QualModel(String modelName, String modelID, String creator, String tfOrganisms, String bindingFactors, String bindingSites, String outputFile, String organism) throws IOException, SBMLException, XMLStreamException {
		SBMLDocument doc = QualModelBuilding.initializeQualDocument(modelName, modelID, creator);
		
		setModelOrganisms(organism.split(","));
		
	    qModel = QualModelBuilding.qualModel;
	    level = QualModelBuilding.model.getLevel();
	    version = QualModelBuilding.model.getVersion();
	    
		readTfOrganisms(tfOrganisms, true);
	    createBindingFactors(bindingFactors, true);
		createBindingSites(bindingSites, true);
		
		System.out.println(transition);
		
		QualModelBuilding.writeSBMlDocument(doc, outputFile);
		
	}
	
	/**
	 * 
	 * @param tfOrganisms
	 * @param header
	 * @throws IOException
	 */
	private void readTfOrganisms(String tfOrganisms, boolean header) throws IOException {
		String line;
		
		BufferedReader input = new BufferedReader(new FileReader(tfOrganisms));
		while ((line = input.readLine()) != null) {
			if (!header) {
				String[] helper = line.split("\t");
				if (helper.length !=2) {
					System.out.println(helper.length);
					System.out.println(line);
					System.exit(0);
				}
				else {
					
					String tfAC = helper[0]; 		// tf ac (T00000)
					String tfOrganism = helper[1]; 	// organism
					
					organismMap.put(tfAC, tfOrganism);	
				}
			}
			else {header = false;}
		}
		
	}

	/**
	 * 
	 * @param bindingFactors
	 * @param header
	 * @throws IOException
	 */
	private void createBindingFactors(String bindingFactors, boolean header) throws IOException {
		String line;
		
		BufferedReader input = new BufferedReader(new FileReader(bindingFactors));
		while ((line = input.readLine()) != null) {
//			System.out.println(line);
			if (!header) {
				String[] helper = line.split("\t");
				if (helper.length !=11) {
					System.out.println(helper.length);
				}
				else {
					
					String tfId = helper[0]; 			// TF id (T00000)
					String tfName = helper[1]; 			// TF name
					String organism = helper[2];		// organism
					String type = helper[3];			// type, e.g., basic, complex
					String encodGeneId = helper[5]; 	// encod. Gene id (G000000)
					String encodGeneName = helper[6]; 	// encod. gene name
					String interactingTFs = helper[8]; 	// interacting factors (T00000)
					String complexPrecurser = helper[9];// complex precurser (T00000)
					String superFamily = helper[10];	// super family (T00000)
					
//					System.out.print(tfId); // TODO
					
					if (isModelOrganism(organism)) {
						addTranscriptionFactor(tfId, tfName);
						if (!encodGeneId.equals("none")) {
							addEncodingGene(tfId, encodGeneId, encodGeneName);
						}
						if (type.equals("complex")) {
							String[] precurser = complexPrecurser.split(";");
							addComplexes(tfId, tfName, precurser);
						}
						if (!interactingTFs.equals("none")) {
							String[] interactingTfIds = interactingTFs.split(";");
							addInteractingTFs(tfId, interactingTfIds);
						}
						if (!superFamily.equals("none")) {
							addSuperFamily(tfId, superFamily);
						}
					}
				}
			}
			else {header = false;}
		}
	}
	
	/**
	 * 
	 * @param bindingSites
	 * @param header
	 * @throws IOException
	 */
	private void createBindingSites(String bindingSites, boolean header) throws IOException {
		String line;
		
		BufferedReader input = new BufferedReader(new FileReader(bindingSites));
		while ((line = input.readLine()) != null) {
			if (!header) {
				String[] helper = line.split("\t");
				if (helper.length !=6) {
					System.out.println(helper.length);
				}
				else {
					
					String regGeneId = helper[3]; 	// reg. Gene id (G000000)
					String regGeneName = helper[4]; 	// reg. gene name
					String bindingFactors = helper[5]; 		// binding factors (T00000)
					
					QualitativeSpecies qsGene = addQualitativeSpecies(regGeneId, regGeneName, SBO.getGene());
					String[] bfs = bindingFactors.split(";");
					for (int i = 0; i < bfs.length; i++) {
						
						if (qModel.getQualitativeSpecies(bfs[i]) != null) {
							Transition t = addTransition("reg", SBO.getUnknownTransition());
							t.createInput((t.getId() + regGeneId), qsGene, InputTransitionEffect.consumption);
							t.createOutput((t.getId() + bfs[i]), qModel.getQualitativeSpecies(bfs[i]), OutputTransitionEffect.production);
						}
						else {
							logger.info("one regulation skipped due to non matching organism TF");
						}
					}
				}	
			}
			else {header = false;}
		}
	}

	/**
	 * 
	 * @param tfId
	 * @param tfName
	 */
	private void addTranscriptionFactor(String tfId, String tfName) {
		addQualitativeSpecies(tfId, tfName, SBO.getMacromolecule());
	}

	/**
	 * adds the encoding gene incl. the corresponding transition
	 * @param tfId
	 * @param encodGeneId
	 * @param encodGeneName
	 */
	private void addEncodingGene(String tfId, String encodGeneId,
			String encodGeneName) {
		QualitativeSpecies qsGene = addQualitativeSpecies(encodGeneId, encodGeneName, SBO.getGene());
		QualitativeSpecies qsTF = addQualitativeSpecies(tfId, "", SBO.getMacromolecule());
	
		Transition t = addTransition(("tr"), SBO.getTranscription());
		t.createInput((t.getId() + encodGeneId), qsGene, InputTransitionEffect.consumption);
		t.createOutput((t.getId() + tfId), qsTF, OutputTransitionEffect.production);
	}

	/**
	 * adds the complex incl. the corresponding transition
	 * @param complexId
	 * @param complexName
	 * @param precurser
	 */
	private void addComplexes(String complexId, String complexName, String[] precurser) {
		QualitativeSpecies qsComplex = addQualitativeSpecies(complexId, complexName, SBO.getComplex());
		
		if (qsComplex != null) { // complex organism corresponds to the precurser organism 
			Transition t = addTransition(("cx"), SBO.getStateTransition());
			t.createOutput((t.getId() + complexId), qsComplex, OutputTransitionEffect.production);
			
			for (int i = 0; i < precurser.length; i++) {
				QualitativeSpecies qsPre = addQualitativeSpecies(precurser[i], SBO.getMacromolecule());
				if (qsPre != null) {
				t.createInput((t.getId() + precurser[i]), qsPre, InputTransitionEffect.consumption);
				}
				else { // if at least one precurser species is not of model organism, unregister the transition and the complex
					logger.info("complex transition unregistered due to non matching organism");
					t.unregister(t);
					qsComplex.unregister(qsComplex);
					// TODO check unregistering
				}
			}
		}
	}

	/**
	 * adds the interacting transcriptoin factors incl. the corresponding transition
	 * @param tfId
	 * @param interactingIds
	 */
	private void addInteractingTFs(String tfId, String[] interactingIds) {
		QualitativeSpecies qsTF = addQualitativeSpecies(tfId, "", SBO.getMacromolecule());
		
		for (int i = 0; i < interactingIds.length; i++) {
			QualitativeSpecies qsInt = addQualitativeSpecies(interactingIds[i], SBO.getMacromolecule());
			if (qsInt != null) { // do not create a new transition if the interacting tf is not of the model organism
				if (!tfId.equals(interactingIds[i])) {
					Transition t = addTransition("int", SBO.getUnknownTransition());
					t.createInput((t.getId() + tfId + "input"), qsTF, InputTransitionEffect.consumption);
					t.createInput((t.getId() + interactingIds[i] + "input"), qsInt, InputTransitionEffect.consumption);
					t.createOutput((t.getId() + tfId + "output"), qsTF, OutputTransitionEffect.production);
					t.createOutput((t.getId() + interactingIds[i] + "output"), qsInt, OutputTransitionEffect.production);
				}
				else {
					Transition t = addTransition("int", SBO.getUnknownTransition());
					t.createInput((t.getId() + tfId + "input"), qsTF, InputTransitionEffect.consumption);
					t.createOutput((t.getId() + interactingIds[i] + "output"), qsInt, OutputTransitionEffect.production);
				}
			}
			else {
				logger.info("one species skipped due to non matching organism");
			}
		}
	}

	/**
	 * adds the super family molecule incl. the corresponding transition
	 * @param tfId
	 * @param superFamilyId
	 */
	private void addSuperFamily(String tfId, String superFamilyId) {
		QualitativeSpecies qsTF = addQualitativeSpecies(tfId, SBO.getMacromolecule());
		QualitativeSpecies qsSF = addQualitativeSpecies(superFamilyId, SBO.getMacromolecule());

		if (qsSF != null && qsTF != null) {
			Transition t = addTransition(("sf"), SBO.getStateTransition());
			t.createInput((t.getId() + superFamilyId), qsSF, InputTransitionEffect.consumption);
			t.createOutput((t.getId() + tfId), qsTF, OutputTransitionEffect.production);
		}
		else {
			logger.info("one species skipped due to non matching organism");
		}
	}

	/**
	 * checks for existing and adds a new {@link QualitativeSpecies}
	 * @param id
	 * @param name
	 * @param sbo
	 * @return QualitativeSpecies
	 */
	private QualitativeSpecies addQualitativeSpecies(String id, String name, int sbo) {
		// check whether the species organism fit the model organism, than if species already exists and in case of existing, check for a set name
			if (qModel.getQualitativeSpecies(id) == null) {
				QualitativeSpecies qs = new QualitativeSpecies(id, name, level, version);
				qModel.addSpecies(qs);
				qs.setSBOTerm(sbo);
				return qs;
			}
			else {
				if (qModel.getQualitativeSpecies(id).getName().equals("")) {
					qModel.getQualitativeSpecies(id).setName(name);
				}	
				return qModel.getQualitativeSpecies(id);
			}
	}
	
	/**
	 * checks for existing and adds a new {@link QualitativeSpecies}
	 * @param id
	 * @param sbo
	 * @return QualitativeSpecies or null, if deviating organism
	 */
	private QualitativeSpecies addQualitativeSpecies(String id, int sbo) {
		if (qModel.getQualitativeSpecies(id) == null) {
			if (organismMap.get(id) != null && isModelOrganism(organismMap.get(id))){
				QualitativeSpecies qs = new QualitativeSpecies(id, "", level, version);
				qModel.addSpecies(qs);
				qs.setSBOTerm(sbo);
				return qs;
			}
			else { // if deviating organism
				return null;
			}
		}
		else {
			return qModel.getQualitativeSpecies(id);
		}
	}
	
	/**
	 * true if given Organism is contained in the qual model organisms list
	 * @param tfOrganism
	 * @return
	 */
	private boolean isModelOrganism(String tfOrganism) {
		if (tfOrganism != null) {
			for (int i = 0; i < getModelOrganisms().length; i++) {
				if (tfOrganism.equalsIgnoreCase(getModelOrganisms()[i])) {
					return true;
				}
			}
			return false;
		}
		else return false;
	}

	/**
	 * adds a new Transition to the qualitative model
	 * @param prefix
	 * @param sbo
	 * @return Transition
	 */
	private Transition addTransition(String prefix, int sbo) {
		Transition t = qModel.createTransition(prefix + transition);
		t.setName(t.getId());
		t.setSBOTerm(sbo);
		qModel.addTransition(t);
		transition++;
		return t;
	}
	
	/**
	 * 
	 * @return the model organisms
	 */
	public static String[] getModelOrganisms() {
		return modelOrganisms;
	}

	/**
	 * 
	 * @param organisms
	 */
	public static void setModelOrganisms(String[] organisms) {
		Transfac2QualModel.modelOrganisms = organisms;
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 5) {
			new Transfac2QualModel("qualitative Model of the Transfac database", "Transfac_human2012", "Stephanie Tscherneck", args[0], args[1], args[2], args[3], args[4]);
		}
		else {
			throw new Exception("arguments: (space separated) organism-file bindingFactor-file bindingSites-file sbml-file and organisms (commaseparated)");
		}
	}
}
