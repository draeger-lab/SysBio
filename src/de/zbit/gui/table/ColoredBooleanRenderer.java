/* $Id$
 * $URL$
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
package de.zbit.gui.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import de.zbit.gui.ColorPalette;

/**
 * This class renders {@link Boolean} entries within a {@link JTable} and sets
 * the background color of every second row to a default (light blue) or given
 * {@link Color}s. It is also possible to give multiple colors in order to change
 * the background {@link Color} for several rows, or to give just one {@link Color},
 * i.e., to have an identical background for all rows.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class ColoredBooleanRenderer extends JCheckBox implements
		TableCellRenderer {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 2699486332938892133L;
	
	private Color bg[];
	
	/**
	 * 
	 */
	public ColoredBooleanRenderer() {
		this(ColorPalette.lightBlue, Color.WHITE);
	}

	/**
	 * A collection of background colors (at least two {@link Color} objects).
	 * 
	 * @param bgColors
	 */
	public ColoredBooleanRenderer(Color... bgColors) {
		super();
		setHorizontalAlignment(JCheckBox.CENTER);
		bg = bgColors;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
      setForeground(table.getSelectionForeground());
		} else {
			setForeground(table.getForeground());
		}
		if ((bg != null) && (bg.length > 0)) {
			setBackground(bg[row % bg.length]);
		}
		setSelected(((value != null) && ((Boolean) value).booleanValue()));
		setOpaque(true);
		return this;
	}
	
}
