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
package de.zbit.sbml.layout.y;

import java.awt.Graphics2D;

import org.sbml.jsbml.util.StringTools;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * A {@link NodeRealizer} to display independent text (i.e. text which is not a
 * label for a graphical object).
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class IndependentTextRealizer extends ShapeNodeRealizer {
	
	private String text;

	/**
	 * copy constructor
	 */
	public IndependentTextRealizer(NodeRealizer nr) {
		super(nr);
		if (nr instanceof IndependentTextRealizer) {
			IndependentTextRealizer textNr = (IndependentTextRealizer) nr;
			text = textNr.text;
		}
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param text
	 */
	public IndependentTextRealizer(double x, double y, double width,
			double height, String text) {
		this.text = text;
		setLabelText(text);
		setSize(width, height);
		setLocation(x, y);
	}

	/* (non-Javadoc)
	 * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
	 */
	@Override
	protected void paintFilledShape(Graphics2D gfx) {
		// Do not fill it.
	}

	/* (non-Javadoc)
	 * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
	 */
	@Override
	protected void paintShapeBorder(Graphics2D gfx) {
		// Do not paint a border.
	}
	
	/* (non-Javadoc)
	 * @see y.view.NodeRealizer#paintSloppy(java.awt.Graphics2D)
	 */
	@Override
	public void paintSloppy(Graphics2D gfx) {
		// No sloppy representation.
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return StringTools.concat(getClass().getSimpleName(),
				" [x=", x,
				", y=", y,
				", width=", width,
				", height=", height,
				", text=", text,
		"]").toString();
	}
}
