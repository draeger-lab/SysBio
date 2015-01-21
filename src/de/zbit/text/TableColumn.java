/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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
		center,
		left,
		right;
	}
	
	public enum VAlign {
		bottom,
		center,
		top;
	}
	
	private Align align;
	private int colspan;
	private int height;
	private String heightUnit;
	private int rowspan;
	private VAlign valign;
	private int width;
	private String widthUnit;
	
	public TableColumn(Align align) {
		this.align = align;
	}

	public TableColumn(Align align, int width) {
		this(align);
		setWidth(width);
	}
	
	public TableColumn(Align align, int width, String widthUnit) {
		this(align, width);
		setWidthUnit(widthUnit);
	}

	/**
	 * @return the align
	 */
	public Align getAlign() {
		return align;
	}

	/**
	 * @return the colspan
	 */
	public int getColspan() {
		return colspan;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * @return the heightUnit
	 */
	public String getHeightUnit() {
		return heightUnit;
	}
	
	/**
	 * @return the rowspan
	 */
	public int getRowspan() {
		return rowspan;
	}
	/**
	 * @return the valign
	 */
	public VAlign getValign() {
		return valign;
	}
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * @return the withUnit
	 */
	public String getWidthUnit() {
		return widthUnit;
	}
	/**
	 * @param align the align to set
	 */
	public void setAlign(Align align) {
		this.align = align;
	}
	/**
	 * @param colspan the colspan to set
	 */
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}
	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * @param heightUnit the heightUnit to set
	 */
	public void setHeightUnit(String heightUnit) {
		this.heightUnit = heightUnit;
	}
	/**
	 * @param rowspan the rowspan to set
	 */
	public void setRowspan(int rowspan) {
		this.rowspan = rowspan;
	}
	/**
	 * @param valign the valign to set
	 */
	public void setValign(VAlign valign) {
		this.valign = valign;
	}
	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/**
	 * @param widthUnit the widthUnit to set
	 */
	public void setWidthUnit(String widthUnit) {
		this.widthUnit = widthUnit;
	}
	
}
