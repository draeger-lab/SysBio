/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.table.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
public class ColoredBooleanRenderer implements TableCellRenderer {
	
	/**
	 * An array that allows to store the background {@link Color}s of rows. The
	 * first row will have the first background {@link Color}, the second row the
	 * second {@link Color} etc. At the end of the array's length, it will repeat
	 * again.
	 */
	private Color bg[];
	
	/**
	 * Creates a new renderer with the two default background {@link Color}
	 * {@link ColorPalette#lightBlue} and {@link Color#WHITE}.
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
		bg = bgColors;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
		JComponent component;
		if (value instanceof Boolean) {
			component = new JCheckBox();
			JCheckBox check = (JCheckBox) component;
			check.setHorizontalAlignment(JCheckBox.CENTER);
			check.setSelected(((value != null) && ((Boolean) value).booleanValue()));
		} else {
			component = new JLabel(value != null ? value.toString() : "");
		}
		if (isSelected) {
      component.setForeground(table.getSelectionForeground());
      component.setBackground(table.getSelectionBackground());
		} else {
			component.setForeground(table.getForeground());
			if ((bg != null) && (bg.length > 0)) {
				component.setBackground(bg[row % bg.length]);
			} else {
				component.setBackground(table.getBackground());
			}
		}
		component.setOpaque(true);
		return component;
	}
	
}
