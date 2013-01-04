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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class CheckAtomBalance {

	private static final transient Logger logger = Logger.getLogger(SBML2GibbsPred.class.getName());
	
	/**
	 * 
	 * @param file
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public CheckAtomBalance(File file) throws XMLStreamException, IOException {
		this(SBMLReader.read(file));
	}
	
	/**
	 * 
	 * @param doc
	 * @throws IOException
	 */
	public CheckAtomBalance(SBMLDocument doc) throws IOException{
		Model m = doc.getModel();
		AnnotationCheck a = new AnnotationCheck(m);
		a.checkAtomBalance(doc.getModel(), 1);
		logger.info("checked atom balance");

	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws XMLStreamException 
	 */
	public static void main(String[] args) throws XMLStreamException, IOException {
		File file = new File(args[0]);
		new CheckAtomBalance((File) file);

	}

}
