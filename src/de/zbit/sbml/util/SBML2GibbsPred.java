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
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class SBML2GibbsPred {

	private static final transient Logger logger = Logger.getLogger(SBML2GibbsPred.class.getName());
	
	/**
	 * 
	 * @param file
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public SBML2GibbsPred(File file, String output) throws XMLStreamException, IOException {
		this(SBMLReader.read(file), output);
	}

	/**
	 * 
	 * @param doc
	 * @throws IOException 
	 */
	public SBML2GibbsPred(SBMLDocument doc, String output) throws IOException {
		logger.info("reading completed");
		Model m = doc.getModel();
		if (m.isSetListOfReactions()) {
			this.writeReactionsInGibbsPredFormat(m.getListOfReactions(), output);
		}
	}
	

	/**
	 * 
	 * @param rlist
	 * @param outputfile
	 * @throws IOException
	 */
	public void writeReactionsInGibbsPredFormat(List<Reaction> rlist, String outputfile) throws IOException {
		FileWriter output = new FileWriter(outputfile ,false);
		for (Reaction r : rlist) {
			output.write(r + "\t");
			if (r.isSetListOfReactants()) {
				writeReferences(r.getListOfReactants(), output);
			}
			output.write("=");
			if (r.isSetListOfProducts()) {
				writeReferences(r.getListOfProducts(), output);
			}
			output.write("\n");

		}
		
		output.close();
	}

	/**
	 * 
	 * @param rlist
	 * @param keggReactionId if true, the Output changed to Kegg reaction ID instead of the SBML reaction ID
	 * @param outputfile
	 * @throws IOException 
	 */
	public void writeReactionsInGibbsPredFormat(List<Reaction> rlist, boolean keggReactionId, String outputfile) throws IOException {
		FileWriter output = new FileWriter(outputfile ,false);

		for (Reaction r : rlist) {
			List<CVTerm> cvTermslist = r.getCVTerms();
			if (!cvTermslist.isEmpty()) {
				for (int j = 0; j < cvTermslist.size(); j++) {
					String s = cvTermslist.get(j).toString().replace(
							"biological entity is urn:miriam:kegg.reaction:",
							"");
					if (!s.isEmpty() && s.length() == 6) {
						output.write(s + "\t");
					}
				}
			}
			else {
				output.write(r + "\t");
			}
			if (r.isSetListOfReactants()) {
				writeReferences(r.getListOfReactants(), output);
			}
			output.write("=");
			if (r.isSetListOfProducts()) {
				writeReferences(r.getListOfProducts(), output);
			}
			output.write("\n");

		}
		output.close();
	}

	
	/**
	 * 
	 * @param listOfReferences
	 * @throws IOException 
	 */
	private void writeReferences(ListOf<SpeciesReference> listOfReferences, FileWriter output) throws IOException {
		boolean first = true;
		for (SpeciesReference specRef : listOfReferences) {
			if (!first) {
				output.write("+");
			} else {
				first = false;
			}
			output.write((int) specRef.getStoichiometry() + " ");
			Species species = specRef.getSpeciesInstance();
			if (species != null) {
				List<String> list = species.filterCVTerms(Qualifier.BQB_IS,
						"kegg");
				if (list.size() > 0) {
					output.write(list.get(0).substring(
							"urn:miriam:kegg.compound:".length()));
				} else {
					logger.info("no CV-Term found: " + species.getName());
				}
			} else {
				logger.info("species = null");
			}
		}
	}

	/**
	 * @param args
	 * @throws XMLStreamException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws XMLStreamException, IOException {		
		File file = new File(args[0]);
		String outputFile = file.getPath().replaceAll(file.getName(), "") + "reactions_" + file.getName() + ".txt";
//		File output = new File(outputFile);
//		new SBML2GibbsPred((File) file, outputFile);
		new SBML2GibbsPred((File) file, args[1]);
		
	}

}
