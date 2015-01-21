/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

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
public class AddSubstanceUnits2Model {

	private static final transient Logger logger = Logger.getLogger(AddSubstanceUnits2Model.class.getName());
	
	/**
	 * 
	 * @param file
	 * @throws Exception
	 * @throws IOException
	 */
	public AddSubstanceUnits2Model(File input, String output) throws Exception, IOException{
		this(SBMLReader.read(input), output);
		
	}
	
	/**
	 * 
	 * @param doc
	 * @throws IOException 
	 * @throws XMLStreamException 
	 * @throws SBMLException 
	 */
	public AddSubstanceUnits2Model(SBMLDocument doc, String output) throws IOException, SBMLException, XMLStreamException{
		logger.info("SBML Document is read.");
		Model m = doc.getModel();
				
		if (m.isSetListOfSpecies()) {
			for (Species sp : m.getListOfSpecies()) {
				if (!sp.isSetHasOnlySubstanceUnits()) {
					sp.setHasOnlySubstanceUnits(false);
					logger.info("hasOnlySubstanceUnits wasn't set: " + sp.isHasOnlySubstanceUnits());
				}
				
				if (sp.getSubstanceUnits().isEmpty()) {
					sp.setSubstanceUnits("substance");
				}
				
				if (sp.getHasOnlySubstanceUnits()) {
					sp.setHasOnlySubstanceUnits(false);
					logger.info("hasOnlySubstanceUnits is now set to: " + sp.getHasOnlySubstanceUnits());
				}
			}
		}
		SBMLWriter w = new SBMLWriter();
		w.write(doc, output);
		logger.info("SBML Document is written to " + output);

	}
	
	/**
	 * @param args
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, Exception {
		File input = new File(args[0]);
		String outputFile = input.getPath().replaceAll(input.getName(), "") + "new_" + input.getName();
		new AddSubstanceUnits2Model(input, outputFile);

	}

}
