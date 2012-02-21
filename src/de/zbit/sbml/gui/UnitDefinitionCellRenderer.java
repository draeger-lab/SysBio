/* $Id$
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
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.compilers.HTMLFormula;

import de.zbit.gui.ColorPalette;
import de.zbit.util.StringUtil;

/**
 * Displays a {@link UnitDefinition} in HTML format in a {@link JTree} or a
 * {@link JTable}
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class UnitDefinitionCellRenderer extends JLabel implements
		TableCellRenderer, TreeCellRenderer {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(UnitDefinitionCellRenderer.class.getName());

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 3139960961236803377L;
	
	/**
	 * 
	 */
	public UnitDefinitionCellRenderer() {
		super();
		setOpaque(true);
	}
	
	/**
	 * 
	 * @param value
	 * @param selected
	 * @param foreground
	 * @param background
	 */
	private void init(Object value, boolean selected, Color foreground, Color background) {
		setForeground(foreground);
		setBackground(background);
		try {
			UnitDefinition ud = (UnitDefinition) value;
			setText(StringUtil.toHTML(HTMLFormula.toHTML(ud)));
			setToolTipText(ud.isSetName() ? ud.getName() : null);
		} catch (Exception exc) {
			setText("N/A");
			setToolTipText(null);
			if (value != null) {
				logger.warning(exc.getLocalizedMessage());
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public JLabel getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
		Color foreground;
		Color background;
		if (isSelected) {
      foreground = table.getSelectionForeground();
      background = table.getSelectionBackground();
    } else {
      foreground = table.getForeground();
      background = table.getBackground();
    }
		foreground = Color.BLACK;
		if (row % 2 == 0) {
			background = ColorPalette.lightBlue;
		} else if (!isSelected) {
  		background = Color.WHITE;
  	}
		init(value, isSelected, foreground, background);
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	public JLabel getTreeCellRendererComponent(JTree tree, Object value,
		boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Color foreground = tree.getForeground();
		Color background = tree.getBackground();
		init(value, selected, foreground, background);
		return this;
	}
	
}
