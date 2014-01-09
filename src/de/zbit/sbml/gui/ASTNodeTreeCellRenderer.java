/* $Id$
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

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.sbml.jsbml.ASTNode;

/**
 * @author Andreas Dr&auml;ger
 * @date 11:37:23
 * @since 1.1
 * @version $Rev$
 */
public class ASTNodeTreeCellRenderer extends JLabel implements TreeCellRenderer {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 4087723137131992252L;
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (value instanceof ASTNode) {
			ASTNode node = (ASTNode) value;
			if (node.isName()) {
				setText(node.getName());
			} else {
				setText(node.getType().toString().toLowerCase());
			}
		}
		return this;
	}
	
}
