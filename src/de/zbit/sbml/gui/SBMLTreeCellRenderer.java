/*
 * $Id:  SBMLTreeCellRenderer.java 17:49:52 snagel$
 * $URL: SBMLTreeCellRenderer.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.zbit.sbml.gui.SBMLTree.SBMLNode;

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
	
	/** 
	 * @param tree tree 
	 * @param value cell value 
	 * @param selected closed tree icon 
	 * @param expanded opened tree icon 
	 * @param leaf end point icon 
	 * @param row number of row 
	 * @param hasFocus the focus 
	 */ 
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof SBMLNode) {
			if (((SBMLNode) value).boldFont){
				setFont(getFont().deriveFont(Font.BOLD));
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
			}
		}
		
	    return this; 
	}

}
