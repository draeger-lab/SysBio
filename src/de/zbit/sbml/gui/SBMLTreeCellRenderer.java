/*
 * $Id:  SBMLTreeCellRenderer.java 17:49:52 snagel$
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

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;

/**
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class SBMLTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public SBMLTreeCellRenderer() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof SBMLNode) {
			SBMLNode node = (SBMLNode) value;
			if (node.isBoldFont()) {
				setFont(getFont().deriveFont(Font.BOLD));
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
			}
			if (node.getUserObject() instanceof Species) {
				if (this.getText().toLowerCase().startsWith("sa")) {
					System.out.println(this.getText()+ ": " + node.isVisible());
				}
			}
			if (node.getUserObject() instanceof SimpleSpeciesReference){
			  if (((SimpleSpeciesReference) node.getUserObject()).isSetSpeciesInstance() && 
			      ((SimpleSpeciesReference) node.getUserObject()).getSpeciesInstance().isSetName()) {
				  this.setText(((SimpleSpeciesReference) node.getUserObject()).getSpeciesInstance().getName());
			  }
			}
		}
		return this; 
	}

}
