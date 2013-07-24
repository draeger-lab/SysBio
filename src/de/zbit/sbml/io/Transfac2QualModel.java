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


package de.zbit.sbml.io;

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
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml.jsbml.ext.qual.QualitativeModel;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

import com.sun.org.apache.bcel.internal.generic.NEW;

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
	 * counter for qualitative Species
	 */
	private static int qualSpeciesCnt = 0;
	
	/**
	 * index for qualitative Species
	 */
	private static int qualSpeciesIndex = 0;
	
	/**
	 * contains the given model organisms
	 */
	private static String[] modelOrganisms;
	
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
	 * maps the original gene name to the {@link QualitativeSpecies}
	 */
	private Map<String, QualitativeSpecies> oriGeneName2qualSpecies = new HashMap<String, QualitativeSpecies>();

	/**
	 * contains the corresponding accession numbers to the TF name + type, 
	 * important for finding of equal species later on 
	 */
	private Map<String, String> nameType2qsId = new HashMap<String, String>();
	
	/**
	 * maps the original accession number of the transcription factor to the new qual species ID 
	 */
	private Map<String, String> oriTfAc2qsId = new HashMap<String, String>();

	/**
	 * maps the original accession number of the transcription factor to the new qual species ID 
	 */
	private Map<String, QualitativeSpecies> oriGeneAc2qs = new HashMap<String, QualitativeSpecies>();

	/**
	 * maps the combination of gene and transcription factor to the transition 
	 */
	private Map<String, Transition> geneTf2transition = new HashMap<String, Transition>();
	
	/**
	 * in case the transcription factors are complexes, this map contains their precursers, for a later addition
	 */
	private Map<String, String[]> tfAC2complexPrecurser = new HashMap<String, String[]>();
	
	/**
	 * maps transcription factor accession number to their interacting transcription factors
	 */
	private Map<String, String[]> tfAC2interactingTfs = new HashMap<String, String[]>();
	
	/**
	 * maps transcription factor accession number to their super family transcription factors
	 */
	private Map<String, String> tfAC2superFamily = new HashMap<String, String>();

	/**
	 * maps the combination of transcription factor and the super family species to their transition 
	 */
	private Map<String, Transition> tfIdSfId2transition = new HashMap<String, Transition>();

	/**
	 * maps transcription factor accession number to the organism
	 */
	private Map<String, String> tfAC2organism = new HashMap<String, String>();

	/**
	 * contains all allowed organisms in a string, needed for a faster search
	 */
	private String allOrganisms = "";

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
			String bindingSites, String genesAnnotation, String outputFile, String organisms) 
			throws IOException, SBMLException, XMLStreamException {
		setModelOrganisms(organisms.split(","));
		allOrganisms = "," + organisms + ",";
		SBMLDocument doc = QualModelBuilding.initializeQualDocument(modelName, modelID, creator, modelOrganisms); 
		
	    qModel = QualModelBuilding.qualModel;
	    level = QualModelBuilding.model.getLevel();
	    version = QualModelBuilding.model.getVersion();
	    taxonomyMap = QualModelBuilding.ncbiTaxonomyMap;
	    
	    System.out.println("read genes annotation file");
		readGenesFile(genesAnnotation, true);

		System.out.println("read binding factors file");
	    readFactorsFile(bindingFactors, true);
	    
	    System.out.println("add Complexes");
	    addComplexes();
	    
	    System.out.println("add Interactions");
	    addInteractingTFs();

	    System.out.println("add Super Families");
	    addSuperFamily();
	    
	    System.out.println("read binding sites file");
		readBindingSites(bindingSites, true);
	    
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
		
		QualModelBuilding.writeSBMlDocument(doc, outputFile);
		
	}
	
	/**
	 * adds the annotations for the genes
	 * @param genesAnnotation
	 * @param header
	 * @throws IOException 
	 */
	private void readGenesFile(String genesAnnotation, boolean header) throws IOException {
		String line;

		BufferedReader input = new BufferedReader(new FileReader(genesAnnotation));
		while ((line = input.readLine()) != null) {
			if (!header) {
				String[] helper = line.split("\t");
				if (helper.length !=4) {
					System.out.println("genes annotation file does not match the expected number of columns");
					System.exit(0);
				}
				else {
					
					String geneAc = helper[0];
					String geneName = helper[1].toLowerCase();		//gene name
					String organism = helper[2]; 					// organism
					String dbIdentifier = helper[3]; 				// annotation [db: id;]+

					if (isModelOrganism(organism)) {
						QualitativeSpecies qs = null;
						if (oriGeneName2qualSpecies.get(geneName) == null) {
							String qsId = "qs" + qualSpeciesIndex;
							qualSpeciesIndex++;
							// create a new Qualspecies
							qs = createQualitativeSpecies(qsId, geneName, SBO.getGene());
							// map gene name to qual species
							oriGeneName2qualSpecies.put(geneName, qs);
						}
						else {
							// get Species
							qs = oriGeneName2qualSpecies.get(geneName);
						}
						
						oriGeneAc2qs.put(geneAc, qs);
						if (!dbIdentifier.equals("none")) {
							addAnnotation(qs, dbIdentifier.split(";"));
						}
						addTaxonomy(qs, organism);
//						System.out.println(geneAc + " " + qs.getId());

					}
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
	private void readFactorsFile(String bindingFactors, boolean header) throws IOException {
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

					String tfAC = helper[0]; 				// TF ac (T00000)
					String tfName = helper[1]; 				// TF name
					String organism = helper[2];			// organism
					String type = helper[3];				// type, e.g., basic, complex
					String encodGeneAc = helper[5]; 		// encod. Gene id (G000000)
					String encodGeneName = helper[6]; 		// encod. gene name
					String interactingTFs = helper[8]; 		// interacting factors (T00000)
					String complexPrecurser = helper[9];	// complex precurser (T00000)
					String superFamily = helper[10];		// super family (T00000)
					String dbIdentifier = helper[11];		// annotation [db: id;]+

					if (isModelOrganism(organism) && (!tfAC.equals("none"))) {
						QualitativeSpecies qs = null;
						String qsId = "qs" + qualSpeciesIndex;
						if (!tfName.equals("none")) {
							if (nameType2qsId.get((tfName + type)) == null) {
								// create new qual species
								if (type.equalsIgnoreCase("complex")) {
									qs = createQualitativeSpecies(qsId, tfName, SBO.getComplex());
									tfAC2complexPrecurser.put(tfAC, complexPrecurser.split(";")); // safe for later
								}
								else {
									qs = createQualitativeSpecies(qsId, tfName, SBO.getMacromolecule());
								}
								qualSpeciesIndex++;
								// add to qual model
								qModel.addQualitativeSpecies(qs);
								// map ori... put (tfAc,qsId)
								oriTfAc2qsId.put(tfAC, qsId);
								// add to map
								nameType2qsId.put((tfName + type), qsId);
							}
							else { // species does exist
								// get species
								qs = qModel.getQualitativeSpecies(nameType2qsId.get((tfName + type)));
								// map ori... put (tfAc,qsId) 	
								oriTfAc2qsId.put(tfAC, qs.getId());
							}
//							System.out.println(tfAC + " " + qsId);
						}
						else { // for transcription factors without a name 
							// create new qual species
							qs = createQualitativeSpecies(qsId, tfName, SBO.getMacromolecule());
							oriTfAc2qsId.put(tfAC, qsId);
							qualSpeciesIndex++;
							// add to qual model
							qModel.addQualitativeSpecies(qs);
							logger.info("no name given for transcription factor: " + tfAC + "new Id: " + qsId);
						}

						// annotation of qual species
						if(!dbIdentifier.equalsIgnoreCase("none")) {
							addAnnotation(qs, dbIdentifier.split(";"));
						}
						// remember taxonomy
						tfAC2organism.put(tfAC, organism);

						// taxonomy of qual species
						addTaxonomy(qs, organism);

						// encoding transition 
						if (!encodGeneAc.equalsIgnoreCase("none")) {
							addEncodingGene(qs, encodGeneAc, organism);
						}

						// save the rest for a later use
						if (!interactingTFs.equalsIgnoreCase("none")) {
							tfAC2interactingTfs.put(tfAC, interactingTFs.split(";"));
						}
						if (!superFamily.equalsIgnoreCase("none")) {
							tfAC2superFamily.put(tfAC, superFamily);
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
	private void readBindingSites(String bindingSites, boolean header) throws IOException {
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
					String bsac = helper[0];
					String organism = helper[2]; // organism for bindingsite
					String regGeneId = helper[3]; 	// reg. Gene id (G000000)
					String regGeneName = helper[4].toLowerCase(); 	// reg. gene name
					String bindingFactors = helper[5]; 		// binding factors (T00000)
					String annotation = helper[6];		// EMBL annotation "AC(first site pos: last site pos)"+
					if (isModelOrganism(organism)) {
						QualitativeSpecies qsGene = oriGeneAc2qs.get(regGeneId);
//						if (qsGene == null) {
//							qsGene = oriGeneName2qualSpecies.get(regGeneName);
//						}
						if (qsGene != null) {
//						QualitativeSpecies qsGene = addQualitativeSpecies(regGeneId, regGeneName, SBO.getGene(), organism);
						String[] bfs = bindingFactors.split(";");
						for (int i = 0; i < bfs.length; i++) {
							if (!(bfs[i].equals("none"))) {
								QualitativeSpecies qsTF = qModel.getQualitativeSpecies(oriTfAc2qsId.get(bfs[i]));
								if (qsTF != null) { // organism have already been checked 
									if (qModel.getQualitativeSpecies(qsGene.getId()) == null) {
										qModel.addQualitativeSpecies(qsGene);
										qualSpeciesCnt++;
									}
									Transition t = createTransition(("reg" + transitionCnt), SBO.getUnknownTransition());
									t.createInput((t.getId() + qsTF.getId() + "input"), qsTF, InputTransitionEffect.none);
									t.createOutput((t.getId() + qsGene.getId() + "output"), qsGene, OutputTransitionEffect.assignmentLevel);

									qModel.addTransition(t);
									transitionCnt++;
									regulationTransitions++;
									addTaxonomy(t, organism);
									String organism2 = tfAC2organism.get(bfs[i]);
									addTaxonomy(t, organism2);
									String notes = "tf: " + qsTF.getId() + "(" + organism2 + ") regulates gene: " + qsGene.getId() + "(" + organism + ")<br/>";
									if (!annotation.equalsIgnoreCase("none")) {
										String[] a = annotation.split(";");
										for (int j = 0; j < a.length; j++) {
											String[] ac = a[j].split(" ");
											addAnnotation(t, ac[0]);
											notes = concat(notes, ("position of bindingsite " + ac[1] + "<br/>"));
										}
									}
									t.setNotes(concat(t.getNotesString(), notes));

								}
							}
						}
						}
						else {
							System.out.println("qsgene null" + bsac);
						}
					}
				}	
			}
			else {header = false;}
		}
	}
	
	private String concat(String s1, String s2) {
		return (s1 + s2);
	}

	
	/**
	 * 
	 * @param transition
	 * @param annotation
	 */
	private void addAnnotation(Transition transition, String annotation) {
		String[] helper = annotation.split(": ");
		IdentifierDatabases idDB = getIdentifierDatabase(helper[0]);
		if (idDB != null) {
			CVTerm term = DatabaseIdentifierTools.getCVTerm(idDB, null, helper[1]);
			if (!existsCVTerm(transition.getCVTerms(), term)) {
				transition.addCVTerm(term);
			}
		}
	}

	/**
	 * 
	 * @param qualitativeSpecies
	 * @param annotation e.g. "EMBL: ABC123"
	 */
	private void addAnnotation(QualitativeSpecies qualitativeSpecies, String[] annotation) {
		for (int i = 0; i < annotation.length; i++) {
			String[] helper = annotation[i].split(": ");
			IdentifierDatabases idDB = getIdentifierDatabase(helper[0]);
			if (idDB != null) {
				CVTerm term = DatabaseIdentifierTools.getCVTerm(idDB, null, helper[1]);
				if (!existsCVTerm(qualitativeSpecies.getCVTerms(), term)) {
					qualitativeSpecies.addCVTerm(term);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param qs
	 * @param organism
	 */
	private void addTaxonomy(QualitativeSpecies qs, String organism) {
		CVTerm term = DatabaseIdentifierTools.getCVTerm(IdentifierDatabases.NCBI_Taxonomy, null, taxonomyMap.get(organism));
		if (!existsCVTerm(qs.getCVTerms(), term)) {
			qs.addCVTerm(term);
		}
	}
	
	/**
	 * 
	 * @param t
	 * @param organism
	 */
	private void addTaxonomy(Transition t, String organism) {
		CVTerm term = DatabaseIdentifierTools.getCVTerm(IdentifierDatabases.NCBI_Taxonomy, null, taxonomyMap.get(organism));
		if (!existsCVTerm(t.getCVTerms(), term)) {
			t.addCVTerm(term);
		}
	}

	/**
	 * 
	 * @param cvTerms
	 * @param cv
	 * @return
	 */
	private boolean existsCVTerm(List<CVTerm> cvTerms, CVTerm cv) {
		for (CVTerm cvt : cvTerms) {
		 if (cvt.equals(cv)) {return true;}	
		}
		return false;
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
	 * adds the encoding gene and the corresponding transition
	 * @param tfId
	 * @param encodGeneId
	 * @param encodGeneAc
	 */
	private void addEncodingGene(QualitativeSpecies qsTF, String encodGeneAc, String organism) {
		QualitativeSpecies qsGene = oriGeneAc2qs.get(encodGeneAc);
		if (qModel.getQualitativeSpecies(qsGene.getId()) == null) {
			qModel.addSpecies(qsGene);
			qualSpeciesCnt ++;
		}

		// as no transition does exist between, create a new transition
		if (geneTf2transition.get(qsGene.getId() + qsTF.getId()) == null) {
			// create new transition
			Transition t = createTransition(("tr" + transitionCnt), SBO.getTranscription());
			t.createInput((t.getId() + qsGene.getId()), qsGene, InputTransitionEffect.consumption);
			t.createOutput((t.getId() + qsTF.getId()), qsTF, OutputTransitionEffect.production);
			qModel.addTransition(t);
			transitionCnt++;
			encodingTransitions++;
			// add taxonomy
			addTaxonomy(t, organism);
			// add to map
			geneTf2transition.put((qsGene.getId() + qsTF.getId()), t);
		}
		else { // as  a transition already exists
			// get transition
			Transition t = geneTf2transition.get(qsGene.getId() + qsTF.getId());
			// add taxonomy
			addTaxonomy(t, organism);
		}
	}

	/**
	 * adds the complex and the corresponding transition
	 * @param complexId
	 * @param complexName
	 * @param precurser
	 */
	private void addComplexes() {
		for (Map.Entry<String, String[]> entry : tfAC2complexPrecurser.entrySet()) {
			String complexId = entry.getKey();
			String[] precurser = entry.getValue();

			Transition t = createTransition(("cx" + transitionCnt), SBO.getStateTransition());
			String qsId = oriTfAc2qsId.get(complexId);
			t.createOutput((t.getId() + qsId + "output"), qModel.getQualitativeSpecies(qsId), OutputTransitionEffect.production);
			ListOf<QualitativeSpecies> qsList = new ListOf<QualitativeSpecies>(level, version);
			boolean list = false;
			for (int i = 0; i < precurser.length; i++) {
				QualitativeSpecies qsPre = qModel.getQualitativeSpecies(oriTfAc2qsId.get(precurser[i]));
				if (qsPre != null) {
					qsList.add(qsPre.clone());
					addTaxonomy(t, tfAC2organism.get(precurser[i]));
					list = true;
				}
				else { // if at least one precurser species is not of model organism
					list = false; // transition should not included in the model
					break; // do not delete this break !!! for-loop must stop !!!
				}
			}
			if (list) {
				String oldId = "";
				int index = 0;
				for (QualitativeSpecies qs : qsList) {
					if (!qs.getId().equals(oldId)) {
						t.createInput((t.getId() + qs.getId() + "input"), qs, InputTransitionEffect.consumption);
					}
					else {
						t.createInput((t.getId() + qs.getId() + "input" + index), qs, InputTransitionEffect.consumption);
						index++;
					}
					oldId = qs.getId();
				}
				qModel.addTransition(t);
				transitionCnt++;
				complexingTransitions++;
			}
			else {
				qModel.removeTransition(t);
			}
		}	
	}

//	/**
//	 * 
//	 * @param precurser[]
//	 * @param listOfInputs
//	 * @return
//	 */
//	private boolean samePrecursers(String[] precurser, ListOf<Input> listOfInputs) {
//		for (int i = 0; i < listOfInputs.size(); i++) {
//			boolean found = false;
//			for (int j = 0; j < precurser.length; j++) {
//				if (listOfInputs.get(i).getQualitativeSpecies() == oriTfAc2qsId.get(precurser[i])) {
//					found = true;
//					break; // TODO check break
//				}
//			}
//			if (!found) {
//				return false;
//			}
//		}
//		return true;
//	}

	/**
	 * adds the interacting transcription factors and the corresponding transition
	 * @param tfId
	 * @param interactingIds
	 */
	private void addInteractingTFs() {
		for (Map.Entry<String, String[]> entry : tfAC2interactingTfs.entrySet()) {
			String oriTF = entry.getKey();
			String[] interactingTfs = entry.getValue();

			String tfId = oriTfAc2qsId.get(oriTF);
			QualitativeSpecies qsTF = qModel.getQualitativeSpecies(tfId);
			int tf = Integer.parseInt(oriTF.replace("T", ""));
			for (int i = 0; i < interactingTfs.length; i++) {
				String intId = oriTfAc2qsId.get(interactingTfs[i]);
				QualitativeSpecies qsInt = qModel.getQualitativeSpecies(intId);
				if (qsInt != null) { // do not create a new transition if the interacting tf is not of the model organism
					int ia = Integer.parseInt(interactingTfs[i].replace("T", ""));
					if (tf != ia) {
						if (tf < ia) { // only the first occurrence as interacting species
							if (tfId.equals(intId)) {

								Transition t = createTransition(("int" + transitionCnt), SBO.getUnknownTransition());
								t.createInput((t.getId() + qsTF.getId() + "input_" + tfAC2organism.get(oriTF)), qsTF, InputTransitionEffect.consumption);
								t.createInput((t.getId() + qsInt.getId() + "input_" + tfAC2organism.get(interactingTfs[i])), qsInt, InputTransitionEffect.consumption);
								t.createOutput((t.getId() + qsTF.getId() + "output_" + tfAC2organism.get(oriTF)), qsTF, OutputTransitionEffect.production);
								t.createOutput((t.getId() + qsInt.getId() + "output_" + tfAC2organism.get(interactingTfs[i])), qsInt, OutputTransitionEffect.production);
								qModel.addTransition(t);
								transitionCnt++;
								interactionTransitions++;
								addTaxonomy(t, tfAC2organism.get(oriTF));
								addTaxonomy(t, tfAC2organism.get(interactingTfs[i]));
							}
							else {

								Transition t = createTransition(("int" + transitionCnt), SBO.getUnknownTransition());
								t.createInput((t.getId() + qsTF.getId() + "input"), qsTF, InputTransitionEffect.consumption);
								t.createInput((t.getId() + qsInt.getId() + "input"), qsInt, InputTransitionEffect.consumption);
								t.createOutput((t.getId() + qsTF.getId() + "output"), qsTF, OutputTransitionEffect.production);
								t.createOutput((t.getId() + qsInt.getId() + "output"), qsInt, OutputTransitionEffect.production);
								qModel.addTransition(t);
								transitionCnt++;
								interactionTransitions++;
								addTaxonomy(t, tfAC2organism.get(oriTF));
								addTaxonomy(t, tfAC2organism.get(interactingTfs[i]));

							}
						}
					}
					else {
						// self interaction
						Transition t = createTransition(("int" + transitionCnt), SBO.getUnknownTransition());
						t.createInput((t.getId() + qsTF.getId() + "input"), qsTF, InputTransitionEffect.consumption);
						t.createOutput((t.getId() + qsInt.getId() + "output"), qsInt, OutputTransitionEffect.production);
						qModel.addTransition(t);
						transitionCnt++;
						selfInteractionTransitions++;
						addTaxonomy(t, tfAC2organism.get(oriTF));
					}
				}
			}
		}	
	}

	/**
	 * adds the super family molecule and the corresponding transition
	 * @param tfId
	 * @param superFamilyId
	 */
	private void addSuperFamily() {
		for (Map.Entry<String, String> entry : tfAC2superFamily.entrySet()) {
			String oriTF = entry.getKey();
			String sfTF = entry.getValue();

			QualitativeSpecies qsTF = qModel.getQualitativeSpecies(oriTfAc2qsId.get(oriTF));
			QualitativeSpecies qsSF = qModel.getQualitativeSpecies(oriTfAc2qsId.get(sfTF));

			if (qsSF != null && qsTF != null && (qsSF.getId() != qsTF.getId())) {
				Transition t = tfIdSfId2transition.get(qsTF.getId() + qsSF.getId());
				if (t == null) {
					t = createTransition(("sf" + transitionCnt), SBO.getStateTransition());
					t.createInput((t.getId() + sfTF), qsSF, InputTransitionEffect.none);
					t.createOutput((t.getId() + oriTF), qsTF, OutputTransitionEffect.assignmentLevel);
					qModel.addTransition(t); 
					transitionCnt++;
					superFamilyTransitions++;
					tfIdSfId2transition.put((qsTF.getId() + qsSF.getId()),t);
				}
				// add taxonomy also if transition already exists (double occurrence of CVterms is already checked in the method addTaxonomy())
				addTaxonomy(t, tfAC2organism.get(oriTF));
				addTaxonomy(t, tfAC2organism.get(sfTF));
			}
		}
	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param sbo
	 * @return
	 */
	private QualitativeSpecies createQualitativeSpecies(String id, String name, int sbo) {
		QualitativeSpecies qs = null;
		
		qs = new QualitativeSpecies(id, name, level, version);
		qs.setMetaId("meta_" + id);
		qs.setSBOTerm(sbo);
		
		return qs;
	}
	
//	/**
//	 * checks for existing and adds a new {@link QualitativeSpecies}
//	 * @param id
//	 * @param name
//	 * @param sbo
//	 * @return QualitativeSpecies
//	 */
//	private QualitativeSpecies addQualitativeSpecies(String id, String name, int sbo, String organism) {
//		QualitativeSpecies qs;	
//		// no need for checking the organism, this is done earlier
//		// check if species already exists and in case of existing, check for a set name
//		if (qModel.getQualitativeSpecies(id) == null) {
//			qs = new QualitativeSpecies(id, name, level, version);
//			qModel.addSpecies(qs);
//			qs.setMetaId("meta_" + id);
//			qs.setSBOTerm(sbo);
//
//			// fill equal species map
//			if (SBO.isGene(sbo)) {
//				StringBuffer b = equalGenesMap.get(name); // TODO plus type
//				if (b == null) {b = new StringBuffer(""); }
//				equalGenesMap.put(name, (b.append(id + ",")));
//			}
//			else { // Macromolecule or complex
//				StringBuffer b = equalSpeciesMap.get(name); // TODO plus type
//				if (b == null) {b = new StringBuffer(""); }
//				equalSpeciesMap.put(name, (b.append(id + ",")));
//			}
//
//			qualSpeciesCnt++;
//		}
//		else {
//			if (qModel.getQualitativeSpecies(id).getName().equals("")) {
//				qModel.getQualitativeSpecies(id).setName(name);
//			}	
//			qs = qModel.getQualitativeSpecies(id);
//		}
//		// add taxonomy if not already set
//		boolean noTaxonomy = true;
//		if (qs.getCVTermCount() != 0) {
//			for (CVTerm cvTerm : qs.getCVTerms()) {
//				if (cvTerm.toString().contains("urn:miriam:taxonomy:")) {
//					noTaxonomy = false;
//					break;
//				}
//			}
//		}
//		if (noTaxonomy) {
//			CVTerm term = DatabaseIdentifierTools.getCVTerm(IdentifierDatabases.NCBI_Taxonomy, null, taxonomyMap.get(organism));
//			qs.addCVTerm(term);
//		}
//		return qs;
//	}
	
	
	/**
	 * 
	 * @param organism
	 * @return true if given Organism is part of the model organisms list
	 */
	private boolean isModelOrganism(String organism) {
		if (organism != null) {
			if ((allOrganisms.replace(organism, "").length()) < (allOrganisms.length())) {
				return true;
			}
			else {
				return false;
			}
		}
		else return false;
	}

	/**
	 * adds a new Transition to the qualitative model
	 * @param id
	 * @param sbo
	 * @return Transition
	 */
	private Transition createTransition(String id, int sbo) {
		Transition t =  qModel.createTransition(id);
		t.setName(t.getId());
		t.setSBOTerm(sbo);
		t.setMetaId("meta_" + t.getId());
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
		if (args.length == 5) {
			new Transfac2QualModel("qualitative Model of the Transfac database", "Transfac_human2013", "Stephanie Tscherneck", args[0], args[1], args[2], args[3], args[4]);
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
