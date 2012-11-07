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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml.jsbml.ext.qual.QualitativeModel;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

import de.zbit.util.DatabaseIdentifierTools;
import de.zbit.util.DatabaseIdentifiers.IdentifierDatabases;

/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */

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
	private static int transitionCnt = 0;

	/**
	 * counter for encodingTransitions
	 */
	private static int encodingTransitions = 0;

	/**
	 * counter for regulationTransitions
	 */
	private static int regulationTransitions = 0;

	/**
	 * counter for interactionTransitions
	 */
	private static int interactionTransitions = 0;

	/**
	 * counter for selfInteractionTransitions
	 */
	private static int selfInteractionTransitions = 0;

	/**
	 * counter for complexingTransitions
	 */
	private static int complexingTransitions = 0;

	/**
	 * counter for superFamilyTransitions
	 */
	private static int superFamilyTransitions = 0;
	
	/**
	 * counter for equalTransitions
	 */
	private static int equalTransitions = 0;
	
	/**
	 * counter for qualitative Species
	 */
	private static int qualSpeciesCnt = 0;
	
	/**
	 * contains the given model organisms
	 */
	private static String[] modelOrganisms;
	
	/**
	 * contains the corresponding organism to the transcription factor
	 */
	private Map<String, String> organismMap = new HashMap<String, String>();
	
	/**
	 * contains the NCBI taxonomy identifier for organisms
	 */
	private Map<String, String> taxonomyMap;
	
	/**
	 * contains the equal species (e.g. PXR of human, rat, mouse)
	 * key is the name (e.g. PXR), value is a comma separated string with the species identifiers
	 */
	private Map<String, StringBuffer> equalSpeciesMap = new HashMap<String, StringBuffer>();
	
	/**
	 * contains the equal genes (e.g. g6pc of human, rat, mouse)
	 * key is the name (e.g. g6pc), value is a comma separated string with the species identifiers
	 */
	private Map<String, StringBuffer> equalGenesMap = new HashMap<String, StringBuffer>();
	
	/**
	 * 
	 * @param modelName
	 * @param modelID
	 * @param creator
	 * @param tfOrganisms
	 * @param bindingFactors
	 * @param bindingSites
	 * @param genesAnnotation
	 * @param outputFile
	 * @param organism
	 * @throws IOException
	 * @throws SBMLException
	 * @throws XMLStreamException
	 */
	public Transfac2QualModel(String modelName, String modelID, String creator, String bindingFactors, 
			String tfOrganisms, String bindingSites, String genesAnnotation, String outputFile, String organisms) throws IOException, SBMLException, XMLStreamException {
		setModelOrganisms(organisms.split(","));
		SBMLDocument doc = QualModelBuilding.initializeQualDocument(modelName, modelID, creator, modelOrganisms); 
		
	    qModel = QualModelBuilding.qualModel;
	    level = QualModelBuilding.model.getLevel();
	    version = QualModelBuilding.model.getVersion();
	    taxonomyMap = QualModelBuilding.ncbiTaxonomyMap;
	    
	    System.out.println("read organisms file");
		readTfOrganisms(tfOrganisms, true);
	    System.out.println("read binding factors file");
	    createBindingFactors(bindingFactors, true);
	    System.out.println("read binding sites file");
		createBindingSites(bindingSites, true);
	    System.out.println("read genes annotation file");
		addGenesDBIdentifier(genesAnnotation, true);
	    System.out.println("create equal species transition");
		createEqualSpeciesTransition();

		System.out.println();
		System.out.println("qualSpecies: " + qualSpeciesCnt);
		System.out.println("Transitions: " + transitionCnt);
		System.out.println("-------------------------------");
		System.out.println("encoding: " + encodingTransitions);
		System.out.println("regulation: " + regulationTransitions);
		System.out.println("interaction: " + interactionTransitions);
		System.out.println("self interaction: " + selfInteractionTransitions);
		System.out.println("complexing: " + complexingTransitions);
		System.out.println("super family: " + superFamilyTransitions);
		System.out.println("equally: " + equalTransitions);
		
		QualModelBuilding.writeSBMlDocument(doc, outputFile);
		
	}
	
	/**
	 * adds the annotations for the genes
	 * @param genesAnnotation
	 * @param header
	 * @throws IOException 
	 */
	private void addGenesDBIdentifier(String genesAnnotation, boolean header) throws IOException {
		String line;
		String geneAC = "G000000";
		
		BufferedReader input = new BufferedReader(new FileReader(genesAnnotation));
		while ((line = input.readLine()) != null) {
			if (!header) {
				String[] helper = line.split("\t");
				if (helper.length !=4) {
					System.out.println("genes annotation file does not match the expected number of columns");
					System.exit(0);
				}
				else {
					
					geneAC = helper[0]; 		// gene ac (G000000)
					String geneName = helper[1];	//gene name
					String organism = helper[2]; 	// organism
					String dbIdentifier = helper[3]; 	// annotation [db: id;]+
					QualitativeSpecies qs = qModel.getQualitativeSpecies(geneAC);
					if ((qs != null) && (!dbIdentifier.equalsIgnoreCase("none"))) {
						addQualitativeSpecies(geneAC, geneName, SBO.getGene(), organism);
						linkSpeciesWithDatabaseIdentifier(qs, dbIdentifier.split(";"));
					}
					
				}
			}
			else {header = false;}
		}
		
	}

	/**
	 * creates transitions between equal species of different organisms (e.g. PXR(h) and PXR(m))
	 */
	private void createEqualSpeciesTransition() {
		// TODO also if superfamily relations is already included ?????
		for (Map.Entry<String, StringBuffer> entry : equalGenesMap.entrySet()) { 
//			System.out.println("gene: " + entry.getKey() + " " + entry.getValue());
			String[] eq = entry.getValue().toString().split(",");
			if (eq.length >= 2){
				for (int i = 0; i < (eq.length -1); i++) {
					for (int j = (i + 1); j < eq.length; j++) {
						// check if empty
						if (!eq[j].equals("")) {
							addEqualSpeciesTransition(eq[i], eq[j]);
						}
						else {
							System.out.println("String leer");
						}
					}
				}
			}
		}
		
		for (Map.Entry<String, StringBuffer> entry : equalSpeciesMap.entrySet()) { 
//			System.out.println("other: " + entry.getKey() + " " + entry.getValue());
			String[] eq = entry.getValue().toString().split(",");
			if (eq.length >= 2){
				for (int i = 0; i < (eq.length -1); i++) {
					for (int j = (i + 1); j < eq.length; j++) {
						// check if empty
//						System.out.println(eq[i] + " " + eq[j]);
						if (!eq[j].equals("")) {
							addEqualSpeciesTransition(eq[i], eq[j]);
						}
						else {
							System.out.println("String leer");
						}
					}
				}
			}
		}
		
	}

	/**
	 * 
	 * @param tfOrganisms
	 * @param header
	 * @throws IOException
	 */
	private void readTfOrganisms(String tfOrganisms, boolean header) throws IOException {
		String line;
		String tfAC = "T00000";
		
		BufferedReader input = new BufferedReader(new FileReader(tfOrganisms));
		while ((line = input.readLine()) != null) {
			if (!header) {
				String[] helper = line.split("\t");
				if (helper.length !=3) {
					System.out.println("organisms file does not match the expected number of columns");
					System.exit(0);
				}
				else {
					
					tfAC = helper[0]; 		// tf ac (T00000)
					String tfOrganism = helper[2]; 	// organism
					
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
			if (!header) {
				String[] helper = line.split("\t");
				if (helper.length !=12) {
					System.out.println("binding factor file does not match the expected number of columns");
					System.exit(0);
				}
				else {
					
					String tfId = helper[0]; 				// TF id (T00000)
					String tfName = helper[1]; 				// TF name
					String organism = helper[2];			// organism
					String type = helper[3];				// type, e.g., basic, complex
					String encodGeneId = helper[5]; 		// encod. Gene id (G000000)
					String encodGeneName = helper[6]; 		// encod. gene name
					String interactingTFs = helper[8]; 		// interacting factors (T00000)
					String complexPrecurser = helper[9];	// complex precurser (T00000)
					String superFamily = helper[10];		// super family (T00000)
					String dbIdentifier = helper[11];		// annotation [db: id;]+
					if (isModelOrganism(organism)) {
						QualitativeSpecies qs = addTranscriptionFactor(tfId, tfName, organism);
						if(!dbIdentifier.equalsIgnoreCase("none")) {
							linkSpeciesWithDatabaseIdentifier(qs, dbIdentifier.split(";"));
						}
						if (!encodGeneId.equalsIgnoreCase("none")) {
							addEncodingGene(tfId, encodGeneId, encodGeneName, organism);
						}
						if (type.equalsIgnoreCase("complex")) {
							String[] precurser = complexPrecurser.split(";");
							addComplexes(tfId, tfName, precurser, organism);
						}
						if (!interactingTFs.equalsIgnoreCase("none")) {
							String[] interactingTfIds = interactingTFs.split(";");
							addInteractingTFs(tfId, interactingTfIds, organism);
						}
						if (!superFamily.equalsIgnoreCase("none")) {
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
				if (helper.length != 7) {
					System.out.println("binding sites file does not match the expected number of columns");
					System.exit(0);
				}
				else {
					String organism = helper[2];
					String regGeneId = helper[3]; 	// reg. Gene id (G000000)
					String regGeneName = helper[4]; 	// reg. gene name
					String bindingFactors = helper[5]; 		// binding factors (T00000)
					if (isModelOrganism(organism)){
						QualitativeSpecies qsGene = addQualitativeSpecies(regGeneId, regGeneName, SBO.getGene(), organism);
						String[] bfs = bindingFactors.split(";");
						for (int i = 0; i < bfs.length; i++) {
							if (!(bfs[i].equals("none")) && qModel.getQualitativeSpecies(bfs[i]) != null) {
								Transition t = addTransition("reg", SBO.getUnknownTransition());
								t.createInput((t.getId() + regGeneId), qsGene, InputTransitionEffect.consumption);
								t.createOutput((t.getId() + bfs[i]), qModel.getQualitativeSpecies(bfs[i]), OutputTransitionEffect.production);
								regulationTransitions++;
							}
//							else {
//								logger.info("one regulation skipped due to non matching organism TF");
//							}
						}
					}
				}	
			}
			else {header = false;}
		}
	}
	
	/**
	 * 
	 * @param qs
	 * @param dbIdentifier
	 */
	private void linkSpeciesWithDatabaseIdentifier(QualitativeSpecies qs, String[] dbIdentifier) {
		for (int i = 0; i < dbIdentifier.length; i++) {
			String[] helper = dbIdentifier[i].split(": ");
			IdentifierDatabases idDB = getIdentifierDatabase(helper[0]);
			if (idDB != null) {
				CVTerm term = DatabaseIdentifierTools.getCVTerm(idDB, null, helper[1]);
				qs.addCVTerm(term);
			}
		}
		
	}
	
	/**
	 * 
	 * @param database
	 * @return
	 */
	private IdentifierDatabases getIdentifierDatabase(String database) {
		if (database.equalsIgnoreCase("swissprot")) {
			return IdentifierDatabases.UniProt_AC;
		}
		else if (database.equalsIgnoreCase("embl")) {
			return IdentifierDatabases.ENA;
		} 
		else if (database.equalsIgnoreCase("pdb")) {
			return IdentifierDatabases.PDB;
		} 
		else if (database.equalsIgnoreCase("mirbase")) {
			return IdentifierDatabases.miRBase;
		} 
		else if (database.equalsIgnoreCase("pir")) {
			return IdentifierDatabases.NCBI_Protein;
		} 
		else if (database.equalsIgnoreCase("flybase")) {
			return IdentifierDatabases.FlyBase;
		} 
		else if (database.equalsIgnoreCase("ensembl")) {
			return IdentifierDatabases.Ensembl;
		} 
		else if (database.equalsIgnoreCase("entrezgene")) {
			return IdentifierDatabases.EntrezGene;
		} 
		else if (database.equalsIgnoreCase("refseq")) {
			return IdentifierDatabases.RefSeq;
		} 
		else if (database.equalsIgnoreCase("omim")) {
			return IdentifierDatabases.OMIM;
		} 
		else if (database.equalsIgnoreCase("unigene")) {
			return IdentifierDatabases.UniGene;
		} 
		else return null;
	}
	
	/**
	 * 
	 * @param tfId
	 * @param tfName
	 */
	private QualitativeSpecies addTranscriptionFactor(String tfId, String tfName, String organism) {
		return (addQualitativeSpecies(tfId, tfName, SBO.getMacromolecule(), organism));
	}

	/**
	 * 
	 * @param sp1Id
	 * @param sp2Id
	 */
	private void addEqualSpeciesTransition(String sp1Id, String sp2Id) {
		if ((qModel.getQualitativeSpecies(sp1Id) != null) && (qModel.getQualitativeSpecies(sp2Id) != null)){
//			System.out.println(sp1Id + " " + sp2Id);
			Transition t = addTransition("equal", SBO.getUnknownTransition());
			t.createInput((t.getId() + sp1Id + "input"), qModel.getQualitativeSpecies(sp1Id), InputTransitionEffect.consumption);
			t.createInput((t.getId() + sp2Id + "input"), qModel.getQualitativeSpecies(sp2Id), InputTransitionEffect.consumption);
			t.createOutput((t.getId() + sp1Id + "output"), qModel.getQualitativeSpecies(sp1Id), OutputTransitionEffect.production);
			t.createOutput((t.getId() + sp2Id + "output"), qModel.getQualitativeSpecies(sp2Id), OutputTransitionEffect.production);
			equalTransitions++;
		}
	}
	
	/**
	 * adds the encoding gene incl. the corresponding transition
	 * @param tfId
	 * @param encodGeneId
	 * @param encodGeneName
	 */
	private void addEncodingGene(String tfId, String encodGeneId,
			String encodGeneName, String organism) {
		QualitativeSpecies qsGene = addQualitativeSpecies(encodGeneId, encodGeneName, SBO.getGene(), organism);
		QualitativeSpecies qsTF = addQualitativeSpecies(tfId, "", SBO.getMacromolecule(), organism);
	
		Transition t = addTransition(("tr"), SBO.getTranscription());
		t.createInput((t.getId() + encodGeneId), qsGene, InputTransitionEffect.consumption);
		t.createOutput((t.getId() + tfId), qsTF, OutputTransitionEffect.production);
		encodingTransitions++;
	}

	/**
	 * adds the complex incl. the corresponding transition
	 * @param complexId
	 * @param complexName
	 * @param precurser
	 */
	private void addComplexes(String complexId, String complexName, String[] precurser, String organism) {
		QualitativeSpecies qsComplex = addQualitativeSpecies(complexId, complexName, SBO.getComplex(), organism);
		
		if (qsComplex != null) { // complex organism corresponds to the precurser organism 
			Transition t = addTransition(("cx"), SBO.getStateTransition());
			t.createOutput((t.getId() + complexId), qsComplex, OutputTransitionEffect.production);
			complexingTransitions++;
			
			for (int i = 0; i < precurser.length; i++) {
				QualitativeSpecies qsPre = addQualitativeSpecies(precurser[i], SBO.getMacromolecule());
				if (qsPre != null) {
				t.createInput((t.getId() + precurser[i]), qsPre, InputTransitionEffect.consumption);
				}
				else { // if at least one precurser species is not of model organism, remove the transition and the complex species
//					logger.info("complex transition removed due to non matching organism");
					qModel.removeTransition(t);
					qModel.removeQualitativeSpecies(qsComplex);
					complexingTransitions--;
					transitionCnt--;
					break;
				}
			}
		}
	}

	/**
	 * adds the interacting transcription factors incl. the corresponding transition
	 * @param tfId
	 * @param interactingIds
	 */
	private void addInteractingTFs(String tfId, String[] interactingIds, String organism) {
		QualitativeSpecies qsTF = addQualitativeSpecies(tfId, "", SBO.getMacromolecule(), organism);
		for (int i = 0; i < interactingIds.length; i++) {
			QualitativeSpecies qsInt = addQualitativeSpecies(interactingIds[i], SBO.getMacromolecule());
			if (qsInt != null) { // do not create a new transition if the interacting tf is not of the model organism
				Integer tf = Integer.valueOf(tfId.replace("T", ""));
				Integer ia = Integer.valueOf(interactingIds[i].replace("T", ""));
				if (tf != ia) {
					if (tf<ia){ // only the first occurrence as interacting species
						Transition t = addTransition("int", SBO.getUnknownTransition());
						t.createInput((t.getId() + tfId + "input"), qsTF, InputTransitionEffect.consumption);
						t.createInput((t.getId() + interactingIds[i] + "input"), qsInt, InputTransitionEffect.consumption);
						t.createOutput((t.getId() + tfId + "output"), qsTF, OutputTransitionEffect.production);
						t.createOutput((t.getId() + interactingIds[i] + "output"), qsInt, OutputTransitionEffect.production);
						interactionTransitions++;
					}
				}
				else {
					Transition t = addTransition("int", SBO.getUnknownTransition());
					t.createInput((t.getId() + tfId + "input"), qsTF, InputTransitionEffect.consumption);
					t.createOutput((t.getId() + interactingIds[i] + "output"), qsInt, OutputTransitionEffect.production);
					selfInteractionTransitions++;
				}
			}
//			else {
//				logger.info("one species skipped due to non matching organism");
//			}
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
			superFamilyTransitions++;
		}
//		else {
//			logger.info("one species skipped due to non matching organism");
//		}
	}

	/**
	 * checks for existing and adds a new {@link QualitativeSpecies}
	 * @param id
	 * @param name
	 * @param sbo
	 * @return QualitativeSpecies
	 */
	private QualitativeSpecies addQualitativeSpecies(String id, String name, int sbo, String organism) {
		QualitativeSpecies qs;	
		// no need for checking the organism, this is done earlier
		// check if species already exists and in case of existing, check for a set name
		if (qModel.getQualitativeSpecies(id) == null) {
			qs = new QualitativeSpecies(id, name, level, version);
			qModel.addSpecies(qs);
			qs.setMetaId("meta_" + id);
			qs.setSBOTerm(sbo);
			
			// fill equal species map
			if (SBO.isGene(sbo)){
				StringBuffer b = equalGenesMap.get(name);
				if (b == null) {b = new StringBuffer(""); }
				equalGenesMap.put(name, (b.append(id + ",")));
			}
			else { // Macromolecule or complex
				StringBuffer b = equalSpeciesMap.get(name);
				if (b == null) {b = new StringBuffer(""); }
				equalSpeciesMap.put(name, (b.append(id + ",")));
			}
			
			qualSpeciesCnt++;
		}
		else {
			if (qModel.getQualitativeSpecies(id).getName().equals("")) {
				qModel.getQualitativeSpecies(id).setName(name);
			}	
			qs = qModel.getQualitativeSpecies(id);
		}
		// add taxonomy if not already set
		boolean noTaxonomy = true;
		if (qs.getCVTermCount() != 0) {
			for (CVTerm cvTerm : qs.getCVTerms()) {
				if (cvTerm.toString().contains("urn:miriam:taxonomy:")) {
					noTaxonomy = false;
					break;
				}
			}
		}
		if (noTaxonomy) {
			CVTerm term = DatabaseIdentifierTools.getCVTerm(IdentifierDatabases.NCBI_Taxonomy, null, taxonomyMap.get(organism));
			qs.addCVTerm(term);
		}
//		just for checking purposes
//		else {
//			System.out.print(taxonomyMap.get(organism) + ": ");
//			for (CVTerm cv : qs.getCVTerms()) {
//				System.out.print(cv.toString() + " ");
//			}
//			System.out.println();
//		}
		return qs;
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
				qs.setMetaId("meta_" + id);
				qs.setSBOTerm(sbo);
				qualSpeciesCnt++;
				return qs;
			}
			else { // in case of deviating organism
				return null;
			}
		}
		else {
			return qModel.getQualitativeSpecies(id);
		}
	}
	
	/**
	 * 
	 * @param tfOrganism
	 * @return true if given Organism is contained in the model organisms list
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
		Transition t = qModel.createTransition(prefix + transitionCnt);
		t.setName(t.getId());
		t.setSBOTerm(sbo);
		qModel.addTransition(t);
		t.setMetaId("meta_" + t.getId());
		transitionCnt++;
		return t;
	}
	
	/**
	 * 
	 * @return the allowed organisms
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
		if (args.length == 6) {
			new Transfac2QualModel("qualitative Model of the Transfac database", "Transfac_human2012", "Stephanie Tscherneck", args[0], args[1], args[2], args[3], args[4], args[5]);
		}
		else {
			throw new Exception("\n arguments: " +
					"[bindingFactor-file] [organism-file] " +
					"[bindingSites-file] [genesAnnotation-file] [sbml-file] " +
					"[commaseparated allowed organisms (e.g., human,Mammalia)]\n " +
					"The first four files can be generated using the preparation scripts, " +
					"which can be found in the folder files/transfacExtractionScripts/ " +
					"(call perl *.pl each for usage information)");
		}
	}
}
