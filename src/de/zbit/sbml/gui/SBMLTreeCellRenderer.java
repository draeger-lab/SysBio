/*
 * $Id:  SBMLTreeCellRenderer.java 17:49:52 snagel$
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
package de.zbit.sbml.gui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.sbml.jsbml.SimpleSpeciesReference;

/**
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class SBMLTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -2809335076156082540L;

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
			if (node.getUserObject() instanceof SimpleSpeciesReference) {
			  if (((SimpleSpeciesReference) node.getUserObject()).isSetSpeciesInstance() && 
			      ((SimpleSpeciesReference) node.getUserObject()).getSpeciesInstance().isSetName()) {
				  this.setText(((SimpleSpeciesReference) node.getUserObject()).getSpeciesInstance().getName());
			  }
			}
			if (node.isBoldFont()) {
				setText("<html><b>" + getText() + "</b></html>");
			}
		}
		return this; 
	}

}
