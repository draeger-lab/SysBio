/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models
 * of biochemical processes encoded in the modeling language SBML.
 *
 * Copyright (C) 2007-2012 by the University of Tuebingen, Germany.
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
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import de.zbit.gui.ColorPalette;
import de.zbit.util.StringUtil;

/**
 * A table renderer for decimal numbers. See also {@link ScientificNumberRenderer}!
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-09-21
 * @version $Rev$
 * @since 1.0
 */
public class DecimalCellRenderer extends DefaultTableCellRenderer {
	
  /**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 7169267933533860622L;
	
	/**
	 * alignment (LEFT, CENTER, RIGHT)
	 */
	private int align;
	
	/**
	 * Switch to decide whether or not numbers should be formatted all with the
	 * identical number of fraction digits. Default is false.
	 */
	private boolean allSame;
	
	/**
	 * An array that allows to store the background {@link Color}s of rows. The
	 * first row will have the first background {@link Color}, the second row the
	 * second {@link Color} etc. At the end of the array's length, it will repeat
	 * again.
	 */
	private Color bg[];
	/**
	 * 
	 */
	private NumberFormat formatter;
	
	/**
	 * Creates a new renderer with the default two background {@link Color}s
	 * {@link ColorPalette#lightBlue} and {@link Color#WHITE}.
	 */
	public DecimalCellRenderer() {
		this.formatter = new DecimalFormat(StringUtil.DECIMAL_FORMAT);
		this.align = SwingConstants.RIGHT;
		this.allSame = true;
		this.bg = new Color[] {ColorPalette.lightBlue, Color.WHITE};
	}
	
	/**
	 * @param integer
	 *        maximum integer digits
	 * @param fraction
	 *        exact number of fraction digits if exact is true
	 * @param align
	 *        alignment (LEFT, CENTER, RIGHT)
	 * @param exact
	 *        whether or not fraction should be the exact number of fraction
	 *        digits.
	 * @param bgColors
	 *        an array of background colors for the rows that will repeatedly be
	 *        used to highlight a row. {@code null} values are allowed.
	 */
	public DecimalCellRenderer(int integer, int fraction, int align, boolean exact, Color... bgColors) {
		this();
		this.formatter = NumberFormat.getInstance();
		this.formatter.setMaximumIntegerDigits(integer);
		this.formatter.setMaximumFractionDigits(fraction);
		this.formatter.setMinimumFractionDigits(allSame ? fraction : 0);
		this.align = align;
		this.bg = bgColors;
	}
	
	/**
	 * Creates a new renderer with the default two background {@link Color}s
	 * {@link ColorPalette#lightBlue} and {@link Color#WHITE}.
	 * 
	 * @param integer
	 * @param fraction
	 * @param align
	 * @param exact
	 */
	public DecimalCellRenderer(int integer, int fraction, int align, boolean exact) {
		this(integer, fraction, align, exact, new Color[] {ColorPalette.lightBlue, Color.WHITE});
	}
	
	/**
	 * @param integer
	 *        maximum integer digits
	 * @param fraction
	 *        number of fraction digits
	 * @param align
	 *        alignment (LEFT, CENTER, RIGHT)
	 * @param bgColors
	 *        an array of background colors for the rows that will repeatedly be used
	 *        to highlight a row. {@code null} values are allowed.
	 */
	public DecimalCellRenderer(int integer, int fraction, int align,
		Color... bgColors) {
		this(integer, fraction, align, false, bgColors);
	}
	
	/**
	 * Creates a new renderer with the default two background {@link Color}s
	 * {@link ColorPalette#lightBlue} and {@link Color#WHITE}.
	 * 
	 * @param integer
	 * @param fraction
	 * @param align
	 */
	public DecimalCellRenderer(int integer, int fraction, int align) {
		this(integer, fraction, align, new Color[] {ColorPalette.lightBlue, Color.WHITE});
	}

	/**
	 * @return the background colors.
	 */
	public Color[] getBackgroundColors() {
		return bg;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (isSelected) {
      component.setForeground(table.getSelectionForeground());
		} else {
			component.setForeground(table.getForeground());
		}
		if ((bg != null) && (bg.length > 0)) {
			component.setBackground(bg[row % bg.length]);
		}
		return component;
	}
	
	/**
	 * @param bg the background colors to set
	 */
	public void setBackgroundColors(Color... bg) {
		this.bg = bg;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
	 */
	@Override
	protected void setValue(final Object value) {
		if ((value != null) && (value instanceof Number)) {
			double v = ((Number) value).doubleValue();
			setText(StringUtil.toString(v));
		} else {
			super.setValue(value);
		}
		setHorizontalAlignment(align);
	}

}
