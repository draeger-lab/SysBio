/*
 * $Id$
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
package de.zbit.graph.sbgn;

import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;

/**
 * @author Andreas Dr&auml;ger
 * @date 09:02:38
 * @since 1.1
 * @version $Rev$
 */
public class CompartmentShape extends Area implements Serializable {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -8601663070857468444L;
	
	/**
	 * 
	 */
	private RoundRectangle2D.Double outer, inner;

	/**
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public CompartmentShape(double x, double y, double w, double h) {
		super();
		double minThickness = 6d;
		double thickness = Math.min(minThickness, ((x + w) * (y + h)) / minThickness);
		double arcWfac = 1.5d, arcHfac = 4.5d;
		int arc = computeArc(w, h);
    outer = new RoundRectangle2D.Double(x, y, w, h, arcWfac * arc, arcHfac * arc);
    add(new Area(outer));
    x = x + thickness;
    y = y + thickness;
    w = w - 2d * thickness;
    h = h - 2d * thickness;
    arc = computeArc(w, h);
    inner = new RoundRectangle2D.Double(x, y, w, h, arcWfac * arc, arcHfac * arc);
    subtract(new Area(inner));
	}
	
	/**
	 * 
	 * @param w
	 * @param h
	 * @return
	 */
	public int computeArc(double w, double h) {
		return (int) (Math.min(w, h) / 5d);
	}
	
	/**
	 * 
	 * @return
	 */
	public RoundRectangle2D getInnerArea() {
		return inner;
	}

}
