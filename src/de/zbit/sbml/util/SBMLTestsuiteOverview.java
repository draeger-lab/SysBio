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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.text.HTMLFormatter;
import de.zbit.util.logging.LogUtil;

/**
 * Creates an overview for all models of the SBML test suite by checking its content.
 * 
 * @author Andreas Dr&auml;ger
 * @date 14:36:37
 * @since 1.1
 * @version $Rev$
 */
public class SBMLTestsuiteOverview {
	
	private static Logger logger = Logger.getLogger(SBMLTestsuiteOverview.class.getName());
	
	private SortedSet<File> withFuncDef, withUnitDef, withCompType, withSpecType,
			withCompart, withSpecies, withParameters, withLocalParam,
			withInitAssignments, withRules, withConstraints, withReactions,
			withEvents, withAlgebraicRules, withAssignmentRules, withRateRules;
	
	public SBMLTestsuiteOverview(File inputFile, File outputFile, HTMLFormatter formatter) throws XMLStreamException, IOException {
		super();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		withFuncDef = new TreeSet<File>();
		withUnitDef = new TreeSet<File>();
		withCompType = new TreeSet<File>();
		withSpecType =  new TreeSet<File>();
		withCompart = new TreeSet<File>();
		withSpecies = new TreeSet<File>();
		withParameters = new TreeSet<File>();
		withLocalParam = new TreeSet<File>();
		withInitAssignments = new TreeSet<File>();
		withRules = new TreeSet<File>();
		withConstraints = new TreeSet<File>();
		withReactions = new TreeSet<File>();
		withEvents = new TreeSet<File>();
		withAlgebraicRules = new TreeSet<File>();
		withAssignmentRules = new TreeSet<File>();
		withRateRules = new TreeSet<File>();
		
		check(inputFile);
		formatter.init(bw, "Test Suite Overview");
		formatter.createTable(bw);
		formatter.createTableHead(bw, "Feature", "Files");
		formatter.createTableRow(bw, "Function definitions", createLinks(formatter, withFuncDef));
		formatter.createTableRow(bw, "Unit definitions", createLinks(formatter, withUnitDef));
		formatter.createTableRow(bw, "Compartment types", createLinks(formatter, withCompType));
		formatter.createTableRow(bw, "Species types", createLinks(formatter, withSpecType));
		formatter.createTableRow(bw, "Compartments", createLinks(formatter, withCompart));
		formatter.createTableRow(bw, "Species", createLinks(formatter, withSpecies));
		formatter.createTableRow(bw, "Parameters", createLinks(formatter, withParameters));
		formatter.createTableRow(bw, "Local parameters", createLinks(formatter, withLocalParam));
		formatter.createTableRow(bw, "Initial Assignments", createLinks(formatter, withInitAssignments));
		formatter.createTableRow(bw, "Rules", createLinks(formatter, withRules));
		formatter.createTableRow(bw, "Rate rules", createLinks(formatter, withRateRules));
		formatter.createTableRow(bw, "Assignment rules", createLinks(formatter, withAssignmentRules));
		formatter.createTableRow(bw, "Algebraic rules", createLinks(formatter, withAlgebraicRules));
		formatter.createTableRow(bw, "Constraints", createLinks(formatter, withConstraints));
		formatter.createTableRow(bw, "Reactions", createLinks(formatter, withReactions));
		formatter.createTableRow(bw, "Events", createLinks(formatter, withEvents));
		formatter.closeTable(bw);
		formatter.close(bw);
		bw.close();
	}

	private String createLinks(HTMLFormatter formatter, Set<File> fileList) throws IOException {
		if (fileList.isEmpty()) {
			return " ";
		}
		StringWriter sw = new StringWriter();
		BufferedWriter bw = new BufferedWriter(sw);
		bw.newLine();
		bw.append("          ");
		File file = null;
		int i = 0;
		for (File f : fileList) {
			file = f;
			String name = file.getName();
			formatter.createLink(bw, file.getAbsolutePath(), name);
			if (i < fileList.size() - 1) {
				bw.append(',');
				bw.newLine();
				bw.append("          ");
			}
			i++;
		}
		bw.newLine();
		bw.append("        ");
		bw.close();
		return sw.toString();
	}

	@SuppressWarnings("deprecation")
	private void check(File file) throws XMLStreamException, IOException {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				check(child);
			}
		} else if (file.isFile() && file.canRead() && SBFileFilter.isSBMLFile(file)) {
			logger.info("reading " + file.getName());
			SBMLDocument doc = SBMLReader.read(file);
			if (doc.isSetModel()) {
				Model m = doc.getModel();
				if (m.getFunctionDefinitionCount() > 0) {
					withFuncDef.add(file.getParentFile());
				}
				if (m.getUnitDefinitionCount() > 0) {
					withUnitDef.add(file.getParentFile());
				}
				if (m.getCompartmentTypeCount() > 0) {
					withCompType.add(file.getParentFile());
				}
				if (m.getSpeciesTypeCount() > 0) {
					withSpecType.add(file.getParentFile());
				}
				if (m.getCompartmentCount() > 0) {
					withCompart.add(file.getParentFile());
				}
				if (m.getSpeciesCount() > 0) {
					withSpecies.add(file.getParentFile());
				}
				if (m.getParameterCount() > 0) {
					withParameters.add(file.getParentFile());
				}
				if (m.getLocalParameterCount() > 0) {
					withLocalParam.add(file.getParentFile());
				}
				if (m.getInitialAssignmentCount() > 0) {
					withInitAssignments.add(file.getParentFile());
				}
				if (m.getRuleCount() > 0) {
					for (Rule r : m.getListOfRules()) {
						if (r.isAlgebraic()) {
							withAlgebraicRules.add(file.getParentFile());
						} else if (r.isAssignment()) {
							withAssignmentRules.add(file.getParentFile());
						} else if (r.isRate()) {
							withRateRules.add(file.getParentFile());
						}
					}
					withRules.add(file.getParentFile());
				}
				if (m.getConstraintCount() > 0) {
					withConstraints.add(file.getParentFile());
				}
				if (m.getReactionCount() > 0) {
					withReactions.add(file.getParentFile());
				}
				if (m.getEventCount() > 0) {
					withEvents.add(file.getParentFile());
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws XMLStreamException 
	 */
	public static void main(String[] args) throws XMLStreamException, IOException {
		LogUtil.initializeLogging("de.zbit.sbml");
		new SBMLTestsuiteOverview(new File(args[0]), new File(args[1]), new HTMLFormatter());
	}
	
}
