/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.text;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class TableColumn {
	
	public enum Align {
		left,
		center,
		right;
	}
	
	public enum VAlign {
		top,
		center,
		bottom;
	}
	
	private Align align;
	private VAlign valign;
	private int width;
	private int height;
	private int rowspan;
	private int colspan;
	
	public TableColumn(Align align) {
		this.align = align;
	}
	
	public TableColumn(Align align, int width) {
		this(align);
		setWidth(width);
	}
	
	/**
	 * @return the align
	 */
	public Align getAlign() {
		return align;
	}
	/**
	 * @param align the align to set
	 */
	public void setAlign(Align align) {
		this.align = align;
	}
	/**
	 * @return the valign
	 */
	public VAlign getValign() {
		return valign;
	}
	/**
	 * @param valign the valign to set
	 */
	public void setValign(VAlign valign) {
		this.valign = valign;
	}
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * @return the rowspan
	 */
	public int getRowspan() {
		return rowspan;
	}
	/**
	 * @param rowspan the rowspan to set
	 */
	public void setRowspan(int rowspan) {
		this.rowspan = rowspan;
	}
	/**
	 * @return the colspan
	 */
	public int getColspan() {
		return colspan;
	}
	/**
	 * @param colspan the colspan to set
	 */
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}
	
}
