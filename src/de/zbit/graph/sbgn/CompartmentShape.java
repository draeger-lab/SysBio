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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;

/**
 * @author Andreas Dr&auml;ger
 * @date 09:02:38
 * @since 1.1
 * @version $Rev$
 */
public class CompartmentShape implements Shape, Serializable {

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
    x = x + thickness;
    y = y + thickness;
    w = w - 2d * thickness;
    h = h - 2d * thickness;
    arc = computeArc(w, h);
    inner = new RoundRectangle2D.Double(x, y, w, h, arcWfac * arc, arcHfac * arc);
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

	@Override
	public boolean contains(Point2D point) {
		return inner.contains(point);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return inner.contains(r);
	}

	@Override
	public boolean contains(double x, double y) {
		return inner.contains(x, y);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return inner.contains(x, y, w, h);
	}

	@Override
	public Rectangle getBounds() {
		return outer.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return outer.getBounds2D();
	}

	@Override
	public PathIterator getPathIterator(final AffineTransform at) {
		return new PathIterator() {
			
			private PathIterator outerIterator = outer.getPathIterator(at);
			private PathIterator innerIterator = inner.getPathIterator(at);
			
			@Override
			public void next() {
				if (!outerIterator.isDone()) {
					outerIterator.next();
				} else {
					innerIterator.next();
				}
			}
			
			@Override
			public boolean isDone() {
				return outerIterator.isDone() && innerIterator.isDone();
			}
			
			@Override
			public int getWindingRule() {
				if (!outerIterator.isDone()) {
					return outerIterator.getWindingRule();
				}
				return innerIterator.getWindingRule();
			}
			
			@Override
			public int currentSegment(double[] coords) {
				if (!outerIterator.isDone()) {
					return outerIterator.currentSegment(coords);
				}
				return innerIterator.currentSegment(coords);
			}
			
			@Override
			public int currentSegment(float[] coords) {
				if (!outerIterator.isDone()) {
					return outerIterator.currentSegment(coords);
				}
				return innerIterator.currentSegment(coords);
			}
		};
	}

	@Override
	public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
		return new PathIterator() {
			
			private PathIterator outerIterator = outer.getPathIterator(at, flatness);
			private PathIterator innerIterator = inner.getPathIterator(at, flatness);
			
			@Override
			public void next() {
				if (!outerIterator.isDone()) {
					outerIterator.next();
				} else {
					innerIterator.next();
				}
			}
			
			@Override
			public boolean isDone() {
				return outerIterator.isDone() && innerIterator.isDone();
			}
			
			@Override
			public int getWindingRule() {
				if (!outerIterator.isDone()) {
					return outerIterator.getWindingRule();
				}
				return innerIterator.getWindingRule();
			}
			
			@Override
			public int currentSegment(double[] coords) {
				if (!outerIterator.isDone()) {
					return outerIterator.currentSegment(coords);
				}
				return innerIterator.currentSegment(coords);
			}
			
			@Override
			public int currentSegment(float[] coords) {
				if (!outerIterator.isDone()) {
					return outerIterator.currentSegment(coords);
				}
				return innerIterator.currentSegment(coords);
			}
		};
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return outer.intersects(r) || inner.intersects(r);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return outer.intersects(x, y, w, h) || inner.intersects(x, y, w, h);
	}

	/**
	 * 
	 * @return
	 */
	public Shape getOuterShape() {
		return outer;
	}

	/**
	 * 
	 * @return
	 */
	public Shape getInnerShape() {
		return inner;
	}
	
}
