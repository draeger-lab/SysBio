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
package de.zbit.sbml.layout.y;

import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;

import y.view.Arrow;
import y.view.EdgeRealizer;
import de.zbit.sbml.layout.NecessaryStimulation;

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YNecessaryStimulation implements
		NecessaryStimulation<EdgeRealizer> {

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.CurveSegment, double)
	 */
	@Override
	public EdgeRealizer draw(CurveSegment curveSegment, double width) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.Curve)
	 */
	@Override
	public EdgeRealizer draw(Curve curve) {
		EdgeRealizer edgeRealizer = YLayoutBuilder.createEdgeRealizerFromCurve(curve);
		// TODO wrong arrow
		edgeRealizer.setSourceArrow(Arrow.PLAIN);
		return edgeRealizer;
	}

	@Override
	public EdgeRealizer draw(Curve curve, double lineWidth) {
		// TODO Auto-generated method stub
		return null;
	}

}