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
package de.zbit.sbml.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;

import de.zbit.gui.ColorPalette;

/**
 * A {@link ListCellRenderer} for SBML {@link ListOf}-Objects.
 * 
 * @author Andreas Dr&auml;ger
 * @date 12:07:48
 * @since 1.1
 * @version $Rev$
 */
public class SBMLlistCellRenderer extends DefaultListCellRenderer {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 77458008315378864L;

	/**
	 * 
	 */
	public SBMLlistCellRenderer() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		boolean textSet = false;
		if (value instanceof SimpleSpeciesReference) {
			SimpleSpeciesReference specRef = (SimpleSpeciesReference) value;
      if (specRef.isSetSpeciesInstance()) {
      	Species species = specRef.getSpeciesInstance();
      	if (species.isSetName()) {
      		setText(species.getName());
      		textSet = true;
      	} else if (species.isSetId()) {
      		setText(species.getId());
      		textSet = true;
      	}
      }
		}
		if (!textSet && (value instanceof NamedSBase)) {
			NamedSBase nsb = (NamedSBase) value;
			setText(nsb.isSetName() ? nsb.getName() : nsb.getId());
		}
		Color bg[] = new Color[] {ColorPalette.lightBlue, Color.WHITE}; 
		setBackground(bg[index % bg.length]);
		return this;
	}
	
}
