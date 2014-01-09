/*
 * $Id$
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
package de.zbit.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;

/**
 * The class which generates the 'X' icon for the tabs. The constructor accepts
 * an icon which is extra to the 'X' icon, so you can have tabs like in
 * JBuilder. This value is null if no extra icon is required.
 * 
 * @author Andreas Dr&auml;ger
 * @since originates from SBMLsqueezer 1.3
 * @version $Rev$
 */
public class CloseIcon implements Icon {
	
	private int x, y;
	private int width, height;
	private Color color;

	/**
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * 
	 */
	private boolean border;

	/**
	 * 
	 * @return
	 */
	public boolean isBorder() {
		return border;
	}

	/**
	 * 
	 * @param border
	 */
	public void setBorder(boolean border) {
		this.border = border;
	}

	/**
	 * 
	 */
	private Icon fileIcon;

	/**
	 * @return the fileIcon
	 */
	public Icon getFileIcon() {
		return fileIcon;
	}

	/**
	 * 
	 */
	public CloseIcon(boolean border) {
		this(null);
		this.border = border;
	}

	/**
	 * 
	 */
	public CloseIcon() {
		this(null);
	}

	/**
	 * 
	 * @param fileIcon
	 */
	public CloseIcon(Icon fileIcon) {
		this.fileIcon = fileIcon;
		width = 16;
		height = 16;
		border = true;
		color = Color.BLACK;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		this.x = x;
		this.y = y;

		Color col = g.getColor();

		g.setColor(color);
		int y_p = y + 2;
		if (border) {
			g.drawLine(x + 1, y_p, x + 12, y_p);
			g.drawLine(x + 1, y_p + 13, x + 12, y_p + 13);
			g.drawLine(x, y_p + 1, x, y_p + 12);
			g.drawLine(x + 13, y_p + 1, x + 13, y_p + 12);
		}
		g.drawLine(x + 3, y_p + 3, x + 10, y_p + 10);
		g.drawLine(x + 3, y_p + 4, x + 9, y_p + 10);
		g.drawLine(x + 4, y_p + 3, x + 10, y_p + 9);
		g.drawLine(x + 10, y_p + 3, x + 3, y_p + 10);
		g.drawLine(x + 10, y_p + 4, x + 4, y_p + 10);
		g.drawLine(x + 9, y_p + 3, x + 3, y_p + 9);

		g.setColor(col);
		if (fileIcon != null) {
			fileIcon.paintIcon(c, g, x + width, y_p);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#getIconWidth()
	 */
	public int getIconWidth() {
		return width + (fileIcon != null ? fileIcon.getIconWidth() : 0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#getIconHeight()
	 */
	public int getIconHeight() {
		return height;
	}

	/**
	 * 
	 * @return
	 */
	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}

}
