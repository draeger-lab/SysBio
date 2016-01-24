/*
 * $Id$
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
package de.zbit.sbml.layout.y;

import java.util.Collections;
import java.util.List;

import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;

import y.view.Arrow;
import y.view.EdgeRealizer;
import de.zbit.sbml.layout.Production;

/**
 * yFiles implementation of arc type {@link Production}.
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YProduction extends YAbstractSBGNArc implements Production<EdgeRealizer> {
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.Curve)
   */
  @Override
  public EdgeRealizer draw(Curve curve) {
    // Reverse order of curve segments and of start and end points because
    // curves are always specified in the direction of the reaction
    // (from substrate process node, from process node to product).
    if ((curve != null) && curve.isSetListOfCurveSegments()) {
      List<CurveSegment> listOfCurveSegments = curve.getListOfCurveSegments();
      Collections.reverse(listOfCurveSegments);
      for (CurveSegment curveSegment : listOfCurveSegments) {
        LineSegment ls = (LineSegment) curveSegment;
        if (ls instanceof CubicBezier) {
          CubicBezier bezier = (CubicBezier) ls;
          if (bezier.isSetBasePoint1() && bezier.isSetBasePoint2()) {
            Point point = bezier.removeBasePoint1();
            bezier.setBasePoint1(bezier.removeBasePoint2());
            bezier.setBasePoint2(point);
          }
        }
        Point end = ls.removeEnd();
        ls.setEnd(ls.removeStart());
        ls.setStart(end);
      }
    }
    
    EdgeRealizer edgeRealizer = YLayoutBuilder.createEdgeRealizerFromCurve(curve);
    edgeRealizer.setTargetArrow(Arrow.DELTA);
    return edgeRealizer;
  }
  
}
