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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class SetSBOTerms {

	private static final transient Logger logger = Logger.getLogger(SetSBOTerms.class.getName());
	
	private SBMLDocument doc;
	private Model model;
	private Map<String, SBO.Term> sboMap = new HashMap<String, SBO.Term>();
	
	private SBO.Term sboDefaultSpecies = SBO.getTerm(SBO.getMaterialEntity());
	private SBO.Term sboDefaultReaction = SBO.getTerm(375); // process
	private SBO.Term sboDefaultModifier = SBO.getTerm(SBO.getModifier());
	private SBO.Term sboDefaultCompartment = SBO.getTerm(SBO.getCompartment());
	private SBO.Term sboDefaultModel = SBO.getTerm(SBO.getModellingFramework());
	
	
	
	/**
	 * In case if an individual SBO terms file is given.
	 * tab-separated file: compartment/species/reaction-Id \t SBO:0000001 or 0000001 or 1
	 * headline allowed 
	 * @param modelFile
	 * @param individualSBOfile
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public SetSBOTerms(File modelFile, String individualSBOfile) throws XMLStreamException, IOException {
		this(SBMLReader.read(modelFile), individualSBOfile);
	}

	/**
	 * In case if an individual SBO terms file is given.
	 * tab-separated file: compartment/species/reaction-Id \t SBO:0000001 or 0000001 or 1
	 * headline allowed
	 * @param doc
	 * @param individualSBOfile
	 * @throws IOException 
	 * @throws XMLStreamException 
	 */
	public SetSBOTerms(SBMLDocument doc, String individualSBOfile) throws IOException, XMLStreamException {
		this.doc = doc;
		this.model = doc.getModel();
		
		if (!individualSBOfile.isEmpty()) {
			createSBOMapFromFile(individualSBOfile);
		}

		createSBOMapFromAnnotation();
		
		writeSBOTerms();
	}

	/**
	 * In case if only the xml-file is given.
	 * @param modelFile
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public SetSBOTerms(File modelFile) throws XMLStreamException, IOException {
		this(SBMLReader.read(modelFile));
	}

	/**
	 * In case if only the SBMLDocument is given.
	 * @param doc
	 * @throws IOException 
	 * @throws XMLStreamException 
	 */
	public SetSBOTerms(SBMLDocument doc) throws IOException, XMLStreamException {
		this(doc,"");
	}
	
	/**
	 * 
	 * @param sboFilePath
	 * @throws IOException
	 */
	private void createSBOMapFromFile(String sboFilePath) throws IOException {
		String line;
		BufferedReader input = new BufferedReader(new FileReader(sboFilePath));
		
		while ((line = input.readLine()) != null) {
			String[] sboTerms = line.split("\t");
			if (sboTerms.length >= 2) {
				if (sboTerms[1].matches("[SBO:]*\\d{1,7}")) {
					Integer sbo = Integer.valueOf(sboTerms[1].replace("SBO:", ""));
					sboMap.put(sboTerms[0], SBO.getTerm(sbo));
				}
				else {
					logger.info(sboTerms[1] + " doesn't match the definition of a sbo term [SBO:]*\\d{1,7}\n examples: \"SBO:0000001\" or \"0000001\" or \"1\"");
				}
			}
		}
	}

	/**
	 * @throws XMLStreamException 
	 * 
	 */
	private void createSBOMapFromAnnotation() throws XMLStreamException {
		SBO.Term sbo = null;
		
		ListOf<Species> spList = this.model.getListOfSpecies();
		for (Species s : spList) {
			if (sboMap.get(s.getId()) == null) {
				sbo = null;
				if (s.getAnnotationString() != null) {
					sbo = getSBOfromAnnotation(getAnnotationBlockWithNamespace(s.getAnnotationString()));
				}
				else {
					sbo = sboDefaultSpecies;
					logger.info(s.getId() + ": no CellDesigner annotation block. SBO terms are set to default (material entity)");
				}
				sboMap.put(s.getId(), sbo);
			}
		}
		
		ListOf<Reaction> rList = this.model.getListOfReactions();
		for (Reaction r : rList) {
			if (sboMap.get(r.getId()) == null) {
				sbo = null;
				if (r.getAnnotationString() != null) {
					sbo = getSBOfromAnnotation(getAnnotationBlockWithNamespace(r.getAnnotationString()));
				}
				else {
					sbo = sboDefaultReaction;
					logger.info(r.getId() + ": no CellDesigner annotation block. SBO terms are set to default (process)");
				}
				sboMap.put(r.getId(), sbo);
			}
		}
		
	}
	
	/**
	 * 
	 * @param annotationBlock
	 * @return SBO.Term
	 * @throws XMLStreamException
	 */
	private SBO.Term getSBOfromAnnotation(String annotationBlock) throws XMLStreamException {
		return (getSBOfromAnnotation(annotationBlock, false));
	}
	
	/**
	 * 
	 * @param annotationBlock
	 * @param modifierFlag true if the modifierAnnotation should be browsed
	 * @return SBO.Term
	 * @throws XMLStreamException
	 */
	private SBO.Term getSBOfromAnnotation(String annotationBlock, boolean modifierFlag) throws XMLStreamException {
		SBO.Term sbo = null;
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new BufferedReader(new StringReader(annotationBlock)));
		
		while (streamReader.hasNext()) {
			
			if (streamReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				if (streamReader.getLocalName().equals("class")) {
					sbo = getSBOfromCDLabel(streamReader.getElementText());
					if (sbo == null) {
						sbo = sboDefaultSpecies;
					}
				}
				else if (streamReader.getLocalName().equals("reactionType")) {
					sbo = getSBOfromCDLabel(streamReader.getElementText());
					if (sbo == null) {
						sbo = sboDefaultReaction;
					}
					
				}
				else if (modifierFlag && streamReader.getLocalName().equals("modification")) {
					for (int i = 0; i < streamReader.getAttributeCount(); i++) {
						if (streamReader.getAttributeLocalName(i).equals("type")) {
							sbo = getSBOfromCDLabel(streamReader.getAttributeValue(i));
						}
					}	

					if (sbo == null) {
						sbo = sboDefaultModifier;
					}
				}
			}
			streamReader.next();
		}	
		return sbo;
	}
	
	/**
	 * 
	 * @param cellDesignerLabel
	 * @return SBO.Term
	 */
	private SBO.Term getSBOfromCDLabel(String cellDesignerLabel) {
//		System.out.println("--> " + cellDesignerLabel);
		
		// for species labels
		if (cellDesignerLabel.equals("SIMPLE_MOLECULE")){
			return (SBO.getTerm(SBO.getSimpleMolecule()));
		}
		else if (cellDesignerLabel.equals("PROTEIN")){
			return (SBO.getTerm(SBO.getProtein()));
		}
		else if (cellDesignerLabel.equals("DRUG")){
			return (SBO.getTerm(SBO.getDrug()));
		}
		else if (cellDesignerLabel.equals("ION")){
			return (SBO.getTerm(SBO.getIon()));
		}
		else if (cellDesignerLabel.equals("RNA")){ 
			return (SBO.getTerm(SBO.getRNA()));
		}
		else if (cellDesignerLabel.equals("GENE")){
			return (SBO.getTerm(SBO.getGene()));
		}
		else if (cellDesignerLabel.equals("DEGRADED")){
			return (SBO.getTerm(SBO.getEmptySet()));
		}
		else if (cellDesignerLabel.equals("UNKNOWN")){
			return (SBO.getTerm(SBO.getUnknownMolecule()));
		}
		else if (cellDesignerLabel.equals("COMPLEX")){
			return (SBO.getTerm(SBO.getComplex()));
		}
		//for reaction labels
		else if (cellDesignerLabel.equals("STATE_TRANSITION")){
			return (SBO.getTerm(SBO.getStateTransition()));
		}
		else if (cellDesignerLabel.equals("TRANSPORT")){
			return (SBO.getTerm(SBO.getTransport()));
		}
		else if (cellDesignerLabel.equals("KNOWN_TRANSITION_OMITTED")){
			return (SBO.getTerm(SBO.getTransitionOmitted()));
		}
		else if (cellDesignerLabel.equals("UNKNOWN_TRANSITION")){
			return (SBO.getTerm(SBO.getUnknownTransition()));
		}
		else if (cellDesignerLabel.equals("TRANSCRIPTION")){
			return (SBO.getTerm(SBO.getTranscription()));
		}
		else if (cellDesignerLabel.equals("TRANSLATION")){
			return (SBO.getTerm(SBO.getTranslation()));
		}
		// for modifier labels
		else if (cellDesignerLabel.equals("INHIBITION")){
			return (SBO.getTerm(SBO.getInhibitor()));
		}
		else if (cellDesignerLabel.equals("CATALYSIS")){
			return (SBO.getTerm(SBO.getCatalyticActivator()));
		}
		else if (cellDesignerLabel.equals("TRIGGER")){
			return (SBO.getTerm(SBO.getTrigger()));
		}
		else if (cellDesignerLabel.equals("MODULATION")){
			// currently, no corresponding sbo Term in JSBML found
			return null;
		}
		else if (cellDesignerLabel.equals("PHYSICAL_STIMULATION")){
			return (SBO.getTerm(SBO.getStimulator()));
		}
		else if (cellDesignerLabel.equals("UNKNOWN_CATALYSIS")){ 
			// currently, no corresponding sbo Term in JSBML found
			return null;
		}
		else if (cellDesignerLabel.equals("UNKNOWN_INHIBITION")){ 
			// currently, no corresponding sbo Term in JSBML found
			return null;
		}
		else {
			logger.info("Label: " + cellDesignerLabel + " not found");
			return null;
		}
		
	}
	
	/**
	 * @throws XMLStreamException 
	 * 
	 */
	private void writeSBOTerms() throws XMLStreamException {
		// for the modelling framework
		if (sboMap.get(this.model.getId().toString()) != null) {
			this.model.setSBOTerm(sboDefaultModel.toString());
		}
		
		// for each compartment
		ListOf<Compartment> cList = this.model.getListOfCompartments();
		for (Compartment c : cList) {
			if (sboMap.get(c.getId()) != null) {
				c.setSBOTerm(sboMap.get(c.getId()).toString());
			}
			else {
				c.setSBOTerm(sboDefaultCompartment.toString());
			}
		}
		
		//for each species
		ListOf<Species> spList = this.model.getListOfSpecies();
		for (Species s : spList) {
			if (sboMap.get(s.getId()) != null) {
				s.setSBOTerm(sboMap.get(s.getId()).toString());
			}
			else {
				s.setSBOTerm(sboDefaultSpecies.toString());
			}
		}
		
		// for each reaction
		ListOf<Reaction> rList = this.model.getListOfReactions();
		for (Reaction r : rList) {
			if (sboMap.get(r.getId()) != null) {
				r.setSBOTerm(sboMap.get(r.getId()).toString());
			}
			else {
				r.setSBOTerm(sboDefaultReaction.toString());
			}

			// for each reactant/product set SBO.getReactant/SBO.getProduct
			ListOf<SpeciesReference> srListReac = r.getListOfReactants();
			for (SpeciesReference sr : srListReac) {
				sr.setSBOTerm(SBO.getReactant());
			}
			ListOf<SpeciesReference> srListProd = r.getListOfProducts();
			for (SpeciesReference sr : srListProd) {
				sr.setSBOTerm(SBO.getProduct());
			}
			
			// in case of a modifier
			ListOf<ModifierSpeciesReference> srModifierList = r.getListOfModifiers();
			for (ModifierSpeciesReference msr : srModifierList) {
				SBO.Term sbo = null; 
				if (r.getAnnotationString() != null) {
					sbo = getSBOfromAnnotation(getAnnotationBlockWithNamespace(r.getAnnotationString()), true);
				}
				if (sbo == null) {
					sbo = sboDefaultModifier;
				}
				msr.setSBOTerm(sbo.toString());
			}
			
		}
	}
	
	/**
	 * 
	 * @param annotationBlock
	 * @return 
	 */
	private String getAnnotationBlockWithNamespace (String annotationBlock) {
		
		return ("<?xml version='1.0' encoding='UTF-8' standalone='no'?>" +
				"<annotation xmlns:celldesigner=\"http://www.sbml.org/2001/ns/celldesigner\">" +
				annotationBlock +
				"</annotation>");
	}
	
	/**
	 * 
	 * @param xmlFilePath
	 * @throws SBMLException
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private void writeDocument(String xmlFilePath) throws SBMLException, XMLStreamException, IOException{
		SBMLWriter.write(this.doc, new File(xmlFilePath), ' ', (short) 2);
		logger.info("Document written to: " + xmlFilePath);
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws XMLStreamException 
	 */
	public static void main(String[] args) throws XMLStreamException, IOException {
		logger.info(args[0]);
		
		File modelFile = new File(args[0]);
		SetSBOTerms setter;
		if (args.length >=2) {
			logger.info(args[1]);
			setter = new SetSBOTerms(modelFile, args[1]);
		}
		else {
			logger.info("no given individual SBOtermFile");
			setter = new SetSBOTerms(modelFile);
		}
		setter.writeDocument(args[0]);
//		setter.writeDocument("/home/tscherneck/Desktop/sboTest/sboOutput.xml");
	}

}
