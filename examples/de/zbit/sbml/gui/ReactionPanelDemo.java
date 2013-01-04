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
package de.zbit.sbml.gui;

import javax.swing.JOptionPane;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class ReactionPanelDemo {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	  SBMLDocument doc = new SBMLDocument(2, 4);
	  Model model = doc.createModel("test");
	  Species s1 = model.createSpecies("s1", model.createCompartment("c1"));
	  Species s2 = model.createSpecies("s2", s1.getCompartmentInstance());
	  Reaction r1 = model.createReaction("r1");
	  r1.createReactant(s1);
	  r1.createProduct(s2);
	  r1.setReversible(false);
	  JOptionPane.showMessageDialog(null, new ReactionPanel(r1, true));
	}
	
}
