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
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

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
	
	private static Model model;
	private static QualitativeModel qModel;
	private static int level;
	private static int version;
	private static int transition = 0;
	private static String[] modelOrganisms;
	
	private Map<String, String> organismMap = new HashMap<String, String>();
	
	public Transfac2QualModel(String modelName, String modelID, String creator, String tfOrganisms, String bindingFactors, String bindingSites, String outputFile, String organism) throws IOException, SBMLException, XMLStreamException {
		SBMLDocument doc = QualModelBuilding.initializeQualDocument(modelName, modelID, creator);
		this.modelOrganisms = organism.split(",");
		model = QualModelBuilding.model;
	    qModel = QualModelBuilding.qualModel;
	    level = model.getLevel();
	    version = model.getVersion();
		readTfOrganisms(tfOrganisms, true);
	    createBindingFactors(bindingFactors, true);
		createBindingSites(bindingSites, true);
		
		System.out.println(transition);
		
		// TODO remove non human TFs including transitions
		QualModelBuilding.writeSBMlDocument(doc, outputFile);
		
	}
	
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
					
					String tfAC = helper[0]; 	// tf ac (G000000)
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
//					organism = helper[2];				// organism
					String type = helper[3];			// type
//					String regulationId = helper[4]; 	// Regulation id (R00000)
					String encodGeneId = helper[5]; 	// encod. Gene id (G000000)
					String encodGeneName = helper[6]; 	// encod. gene name
//					String complexes = helper[7]; 		// complexes
					String interactingTFs = helper[8]; 	// interacting factors
					String complexPrecurser = helper[9];// complex precurser
					String superFamily = helper[10];	// super family
					
//					System.out.print(tfId); // TODO
					
					addTranscriptionFactor(tfId, tfName);
//					if (!regulationId.equals("none")) {
//						regulationMap.put(tfId, regulationId);
//						String[] regIds = regulationId.split(";");
//						for (int i = 0; i < regIds.length; i++) {
//							System.out.print(" " + regIds[i]);
//							if (regulationMap.get(regIds[i]) != null) {System.out.println("regId exisitert bereits");System.exit(0);}
//							regulationMap.put(regIds[i], tfId);
//						}
//					}
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
//					System.out.println(" transition:" + transition); // TODO
				}
			}
			else {header = false;}
		}
	}
	
	/**
	 * 
	 * @param tfId
	 * @param superFamilyId
	 */
	private void addSuperFamily(String tfId, String superFamilyId) {
		QualitativeSpecies qsTF = addQualitativeSpecies(tfId, "", SBO.getMacromolecule());
		QualitativeSpecies qsSF = addQualitativeSpecies(superFamilyId, "", SBO.getMacromolecule());

		if (qsSF != null) {
			Transition t = addTransition(("sf"), SBO.getStateTransition());
			t.createInput((t.getId() + superFamilyId), qsSF, InputTransitionEffect.consumption);
			t.createOutput((t.getId() + tfId), qsTF, OutputTransitionEffect.production);
		}
	}

	/**
	 * 
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
				QualitativeSpecies qsPre = addQualitativeSpecies(precurser[i], "", SBO.getMacromolecule());
				t.createInput((t.getId() + precurser[i]), qsPre, InputTransitionEffect.consumption);
			}
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
	 * tf's with name
	 * @param id
	 * @param name
	 * @param sbo
	 * @return
	 */
	private QualitativeSpecies addQualitativeSpecies(String id, String name, int sbo) {
		// check whether the species organism fit the model organism, than if species already exists and in case of existing, check for a set name
		if ((organismMap.get(id) != null && isModelOrganism(organismMap.get(id)))) { // TODO !!!
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
		else return null;
	}
	
	/**
	 * tf's w/o name
	 * @param id
	 * @param sbo
	 * @return
	 */
	private QualitativeSpecies addQualitativeSpecies(String id, int sbo) {
		// TODO !!!
		if (qModel.getQualitativeSpecies(id) == null) {
			QualitativeSpecies qs = new QualitativeSpecies(id, "", level, version);
			qModel.addSpecies(qs);
			qs.setSBOTerm(sbo);
			return qs;
		}
		else {
			if (qModel.getQualitativeSpecies(id).getName().equals("")) {
				qModel.getQualitativeSpecies(id).setName("");
			}	
			return qModel.getQualitativeSpecies(id);
		}
	}
	
	/**
	 * e.g for genes
	 * @param id
	 * @param name
	 * @param organism
	 * @param sbo
	 * @return
	 */
	private QualitativeSpecies addQualitativeSpecies(String id, String name, String organism, int sbo) {
		// TODO !!!
		if (qModel.getQualitativeSpecies(id) == null) {
			QualitativeSpecies qs = new QualitativeSpecies(id, "", level, version);
			qModel.addSpecies(qs);
			qs.setSBOTerm(sbo);
			return qs;
		}
		else {
			if (qModel.getQualitativeSpecies(id).getName().equals("")) {
				qModel.getQualitativeSpecies(id).setName("");
			}	
			return qModel.getQualitativeSpecies(id);
		}
	}
	
	
	private boolean isModelOrganism(String tfOrganism) {
		if (tfOrganism != null) {
			for (int i = 0; i < modelOrganisms.length; i++) {
				if (tfOrganism.equals(modelOrganisms[i])) {
					return true;
				}
			}
			return false;
		}
		else return false;
	}

	/**
	 * 
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

	private void addInteractingTFs(String tfId, String[] interactingIds) {
		QualitativeSpecies qsTF = addQualitativeSpecies(tfId, "", SBO.getMacromolecule());
		
		for (int i = 0; i < interactingIds.length; i++) {
			QualitativeSpecies qsInt = addQualitativeSpecies(interactingIds[i], "", SBO.getMacromolecule());
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
		}
	}

	private Transition addTransition(String pre, int sbo) {
		Transition t = qModel.createTransition(pre + transition);
		t.setName(t.getId());
		t.setSBOTerm(sbo);
		qModel.addTransition(t);
		transition++;
		return t;
	}
	
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
						Transition t = addTransition("reg", SBO.getUnknownTransition());
						t.createInput((t.getId() + regGeneId), qsGene, InputTransitionEffect.consumption);
						if (qModel.getQualitativeSpecies(bfs[i]) != null) {
							t.createOutput((t.getId() + bfs[i]), qModel.getQualitativeSpecies(bfs[i]), OutputTransitionEffect.production);
						}
						else {
							QualitativeSpecies qsBF = addQualitativeSpecies(bfs[i], "", SBO.getMacromolecule());
							t.createOutput((t.getId() + bfs[i]), qsBF, OutputTransitionEffect.production);
						}
					}
				}	
			}
			else {header = false;}
		}
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 5) {
			new Transfac2QualModel("qualitative Model of AffyData within the VLN", "VLN_AffyData_human2012", "Stephanie Tscherneck", args[0], args[1], args[2], args[3], args[4]);
		}
		else {
			throw new Exception("please give one binding factor, one regulation file and where to save the SBMLfile as arguments");
		}
	}
}
