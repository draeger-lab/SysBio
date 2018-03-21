/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2017 by the University of Tuebingen, Germany.
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

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

import de.zbit.sbml.layout.Geometry;
import de.zbit.sbml.layout.LayoutDirector;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class SBMLLayoutArcCorrector {
  
  /**
   * @param args
   * @throws XMLStreamException
   * @throws IOException
   * @throws SBMLException
   */
  public static void main(String[] args) throws XMLStreamException, SBMLException, IOException {
    new SBMLLayoutArcCorrector(SBMLReader.read(new File(args[0])), new File(args[1]));
  }
  
  /**
   * 
   * @param doc
   * @param out
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   */
  public SBMLLayoutArcCorrector(SBMLDocument doc, File out) throws SBMLException, XMLStreamException, IOException {
    if (doc.isSetModel()) {
      Model m = doc.getModel();
      LayoutModelPlugin lmp = (LayoutModelPlugin) m.getExtension(LayoutConstants.shortLabel);
      if ((lmp != null) && lmp.isSetListOfLayouts()) {
        for (Layout l : lmp.getListOfLayouts()) {
          if (l.isSetListOfReactionGlyphs()) {
            for (ReactionGlyph rg : l.getListOfReactionGlyphs()) {
              correctArcs(rg);
            }
          }
        }
      }
    }
    TidySBMLWriter.write(doc, out, ' ', (short) 2);
  }
  
  /**
   * 
   * @param rg
   */
  private void correctArcs(ReactionGlyph rg) {
    if (rg.isSetListOfSpeciesReferenceGlyphs()) {
      for (SpeciesReferenceGlyph srg : rg.getListOfSpeciesReferenceGlyphs()) {
        correctArcs(srg);
      }
    }
  }
  
  /**
   * 
   * @param srg
   */
  private void correctArcs(SpeciesReferenceGlyph srg) {
    if (srg.isSetCurve()) {
      Curve curve = srg.getCurve();
      if (curve.isSetListOfCurveSegments()) {
        boolean isModifier = LayoutDirector.isModifier(srg);
        
        CurveSegment firstSegment = curve.getCurveSegment(0);
        CurveSegment lastSegment = curve.getCurveSegment(curve.getCurveSegmentCount() - 1);
        ReactionGlyph rg = curve.getParentReactionGlyph();
        Point center = Geometry.center(rg.getBoundingBox());
        double distFromStartToCenter = Geometry.euclideanDistance(firstSegment.getStart(), center);
        double distFromEndToCenter = Geometry.euclideanDistance(lastSegment.getEnd(), center);
        boolean forward = isModifier ? distFromStartToCenter > distFromEndToCenter : distFromStartToCenter < distFromEndToCenter;
        
        if (!forward) {
          ListOf<CurveSegment> listOfCurveSegments = curve.getListOfCurveSegments().clone();
          curve.getListOfCurveSegments().clear();
          for (int i = listOfCurveSegments.size() - 1; i >= 0; i--) {
            curve.addCurveSegment(reverse(listOfCurveSegments.remove(i)));
          }
        }
      }
    }
  }
  
  /**
   * 
   * @param cs
   * @return
   */
  private CurveSegment reverse(CurveSegment cs) {
    if (cs instanceof CubicBezier) {
      CubicBezier cb = (CubicBezier) cs;
      Point bp2 = cb.isSetBasePoint2() ? cb.removeBasePoint2() : null;
      if (cb.isSetBasePoint1()) {
        cb.setBasePoint2(cb.removeBasePoint1());
      }
      cb.setBasePoint1(bp2);
    }
    Point end = cs.isSetEnd() ? cs.removeEnd() : null;
    if (cs.isSetStart()) {
      cs.setEnd(cs.removeStart());
    }
    cs.setStart(end);
    return cs;
  }
  
}
