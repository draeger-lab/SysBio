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

package de.zbit.sbml.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;

/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class SetInitialValueFromFile {

	private static final transient Logger logger = Logger.getLogger(SetInitialValueFromFile.class.getName());
	
	/**
	 * links the species id with the concentration
	 */
	private Map<String, Double> concMap = new HashMap<String, Double>();
	
	private static boolean concentration = true;
	
	/**
	 * set the initial concentration or amount from a given value file
	 * 
	 * @param modelFile
	 * @param dataFile
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public SetInitialValueFromFile(File modelFile, String dataFile) throws XMLStreamException, IOException {
		this(SBMLReader.read(modelFile), modelFile, dataFile, concentration);
	}
	
	/**
	 * set the initial concentration or amount from a given value file
	 * 
	 * @param modelFile
	 * @param dataFile
	 * @param concentration
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public SetInitialValueFromFile(File modelFile, String dataFile, boolean concentration) throws XMLStreamException, IOException {
		this(SBMLReader.read(modelFile), modelFile, dataFile, concentration);
	}

	/**
	 * set the initial concentration or amount from a given value file
	 * 
	 * @param doc
	 * @param modelFile
	 * @param dataFile
	 * @param concentration
	 * @throws IOException
	 * @throws SBMLException
	 * @throws XMLStreamException
	 */
	public SetInitialValueFromFile(SBMLDocument doc, File modelFile, String dataFile, boolean concentration) throws IOException, SBMLException, XMLStreamException {
		writeInitialConcentration(doc, modelFile, dataFile, concentration);
	}

	/**
	 * 
	 * @param doc
	 * @param modelFile
	 * @param dataFile
	 * @param concentration
	 * @throws IOException
	 * @throws SBMLException
	 * @throws XMLStreamException
	 */
	private void writeInitialConcentration(SBMLDocument doc, File modelFile, String dataFile, boolean concentration) throws IOException, SBMLException, XMLStreamException {
		String line;
		
		BufferedReader input = new BufferedReader(new FileReader(dataFile));
		while ((line = input.readLine()) != null) {
			String[] conc = line.split("\t");
			if (conc.length >= 2) {
				if ((conc[1].trim()).matches("[-+Ee0-9.]+")) {
					Double d = Double.valueOf(conc[1].trim()).doubleValue();
//					System.out.println(conc[0] + " : " + d);
					concMap.put(conc[0], d);
				}
				else if ((conc[1].trim()).matches("[-+Ee0-9,]+")) {
					String helper = conc[1].replace(",", ".");
					Double d = Double.valueOf(helper.trim()).doubleValue();
					concMap.put(conc[0], d);
				}			
			}

		}
		Model model = doc.getModel();
		if (model.isSetListOfSpecies()) {
			for (Species species : model.getListOfSpecies()) {
				if (concentration) {
					// in case of setting initialConcentration
					species.setInitialConcentration(Double.NaN);
					species.setSubstanceUnits("substance");
					try {
						species.setInitialConcentration(concMap.get(species.getId().toString()));
					} catch (Exception e) {
						logger.info(species.getId().toString() + ": Species not found.");
					}
				}
				else {
					// in case of setting initialAmount
					species.setInitialAmount(Double.NaN);
					species.setSubstanceUnits("substance");
					try {
						species.setInitialAmount(concMap.get(species.getId().toString()));
					} catch (Exception e) {
						logger.info(species.getId().toString() + ": Species not found.");
					}
				}
			}
		}
		SBMLWriter w = new SBMLWriter();
		w.write(doc, modelFile);		
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws XMLStreamException 
	 */
	public static void main(String[] args) throws XMLStreamException, IOException {
		File modelFile = new File(args[0]);
		logger.info(args[1]);
		if (args.length >= 3 && (args[2].startsWith("Amount") || args[2].equals("false"))) {
			new SetInitialValueFromFile(modelFile, args[1], false);
		}
		else {
			new SetInitialValueFromFile(modelFile, args[1]);
		}

	}

}
