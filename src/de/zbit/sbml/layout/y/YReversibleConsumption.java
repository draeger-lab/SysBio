/*
 * $Id: YReversibleConsumption.java 1400 2016-11-29 15:51:11Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/layout/y/YReversibleConsumption.java $
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

import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;

import de.zbit.sbml.layout.Geometry;
import de.zbit.sbml.layout.ReversibleConsumption;
import y.view.Arrow;
import y.view.EdgeRealizer;

/**
 * yFiles implementation of arc type {@link ReversibleConsumption}.
 * 
 * Consumptions in reversible reactions have to display an arrow. This class
 * represents a consumption in a reversible reaction.
 * 
 * Note: This is not a dedicated SBGN arc by specification.
 * 
 * @author Jakob Matthes
 * @version $Rev: 1400 $
 */
public class YReversibleConsumption extends YAbstractSBGNArc implements ReversibleConsumption<EdgeRealizer> {
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.Curve)
   */
  @Override
  public EdgeRealizer draw(Curve curve) {
    // We need to check what really needs to be reversed...
    boolean forward = false;
    if (curve.isSetParent()) {
      CurveSegment firstSegment = curve.getCurveSegment(0);
      CurveSegment lastSegment = curve.getCurveSegment(curve.getCurveSegmentCount() - 1);
      SBase sbase = curve.getParent();
      if (sbase instanceof ReactionGlyph) {
        ReactionGlyph rg = (ReactionGlyph) sbase;
        Point center = Geometry.center(rg.getBoundingBox());
        forward = Geometry.euclideanDistance(firstSegment.getStart(), center) < Geometry.euclideanDistance(lastSegment.getEnd(), center);
      }
    }
    EdgeRealizer edgeRealizer = YLayoutBuilder.createEdgeRealizerFromCurve(curve, forward);
    edgeRealizer.setTargetArrow(Arrow.DELTA);
    return edgeRealizer;
  }
  
}
