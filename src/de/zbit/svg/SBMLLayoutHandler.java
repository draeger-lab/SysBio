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
package de.zbit.svg;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.dom.AbstractElement;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.util.ModelBuilder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGGElement;

import de.zbit.sbml.layout.Tools;
import de.zbit.sbml.util.SBMLtools;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class SBMLLayoutHandler implements SVGHandler<SBMLDocument> {
  
  /**
   * 
   */
  private double z = 0d;
  /**
   * 
   */
  private double depth = 1d;
  
  private double xOffset = 0d;
  private double yOffset = 0d;
  
  /**
   * 
   */
  private ModelBuilder builder;
  /**
   * 
   */
  private Layout layout;
  
  /**
   * 
   */
  public SBMLLayoutHandler() {
    builder = new ModelBuilder(3, 1);
    Model model = builder.buildModel("layout_model", null);
    UnitDefinition substanceUnits = UnitDefinition.substance(2, 5);
    UnitDefinition volumeUnits = UnitDefinition.volume(2, 5);
    substanceUnits = SBMLtools.setLevelAndVersion(substanceUnits, model.getLevel(), model.getVersion());
    volumeUnits = SBMLtools.setLevelAndVersion(volumeUnits, model.getLevel(), model.getVersion());
    model.setSubstanceUnits(substanceUnits);
    model.setVolumeUnits(volumeUnits);
    builder.buildCompartment("default", true, "Default compartment", 3, Double.NaN, UnitDefinition.VOLUME);
    LayoutModelPlugin layoutPlug = (LayoutModelPlugin) model.getPlugin(LayoutConstants.shortLabel);
    layout = layoutPlug.createLayout();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.svg.SVGHandler#handle(de.zbit.svg.Element, org.w3c.dom.NodeList)
   */
  @Override
  public void handle(Element element, NodeList list) {
    for (int i = 0; i < list.getLength(); i++) {
      Node n = list.item(i);
      if (element == Element.RECT) {
        NamedNodeMap attributes = n.getAttributes();
        Node nx = attributes.getNamedItem("x");
        Node ny = attributes.getNamedItem("y");
        Node nwidth = attributes.getNamedItem("width");
        Node nheight = attributes.getNamedItem("height");
        
        double x = 0d, y = 0d, width = 0d, height = 0d;
        if (nx != null) {
          x = Double.parseDouble(nx.getNodeValue());
        }
        if (ny != null) {
          y = Double.parseDouble(ny.getNodeValue());
        }
        if (nwidth != null) {
          width = Double.parseDouble(nwidth.getNodeValue());
        }
        if (nheight != null) {
          height = Double.parseDouble(nheight.getNodeValue());
        }
        
        if ((width != 9d) || (width != height)) {
          // Ugly hack to get rid of tiny squares in some SVG files.
          String id = "c", name = "unknown";
          Compartment c = builder.buildCompartment(incrementSIdSuffix(id), true, name, 3d, Double.NaN, UnitDefinition.VOLUME);
          CompartmentGlyph cg = layout.createCompartmentGlyph(incrementSIdSuffix("compGlyph"));
          cg.setCompartment(c);
          cg.createBoundingBox(width, height, depth, x - xOffset, y - yOffset, z);
        }
      } else if (element == Element.TEXT) {
        NamedNodeMap attributes = n.getAttributes();
        Node nx = attributes.getNamedItem("x");
        Node ny = attributes.getNamedItem("y");
        
        String text = n.getFirstChild().getNodeValue();
        TextGlyph tg = layout.createTextGlyph(toSId(text));
        double width = 5d, height = 5d, x = Double.parseDouble(nx.getNodeValue()), y = Double.parseDouble(ny.getNodeValue());
        tg.createBoundingBox(width, height, depth, x - xOffset, y - yOffset, z);
        tg.setText(text);
      } else if (element == Element.PATH) {
        SVGOMPathElement path = (SVGOMPathElement) n;
        if (path.getParentNode() instanceof SVGGElement) {
          SVGGElement g = (SVGGElement) path.getParentNode();
          if (g.getAttribute("style").matches(".*fill:rgb\\(.*\\);stroke.*") && (element != null)) {;
          NamedNodeMap nodeMap = path.getAttributes();
          if (nodeMap.getLength() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < nodeMap.getLength(); j++) {
              Node item = nodeMap.item(j);
              String value = item.getNodeValue();
              AbstractElement gParent = (AbstractElement) g.getParentNode();
              if (value.startsWith("M") && (gParent.getAttribute("id") != null)) {
                String attribute = ((AbstractElement) g.getParentNode()).getAttribute("id");
                if (attribute.length() > 0) {
                  String parts[] = value.split(" ");
                  String sid = SBMLtools.toSId(attribute);
                  Model model = builder.getModel();
                  Compartment compartment = model.getCompartment("default");
                  String layer = "";
                  if ((gParent.getParentNode() != null) && (((AbstractElement) gParent.getParentNode()).getAttribute("id") != null)) {
                    layer = ((AbstractElement) gParent.getParentNode()).getAttribute("id");
                  }
                  if (layer.endsWith("_5")) {
                    // Reactions; hack!
                    // We here define that reaction glyphs reside in Layer 5 only! This might be valid for one file...
                    // Second, we assume that in contrast to species, there are no cloned reactions...
                    ReactionGlyph rg;
                    if (!model.containsReaction(sid)) {
                      builder.buildReaction(sid, attribute, compartment, false, true);
                      rg = layout.createReactionGlyph(sid + "_rglyph", sid);
                    } else {
                      rg = (ReactionGlyph) model.findNamedSBase(sid + "_rglyph");
                    }
                    parsePath(parts, xOffset, yOffset, rg.isSetCurve() ? rg.getCurve() : rg.createCurve());
                  } else {
                    if (!model.containsSpecies(sid)) {
                      builder.buildSpecies(sid, attribute, compartment, true, false, false, 0d, UnitDefinition.SUBSTANCE);
                    }
                    String id = incrementSIdSuffix(sid + "_sglyph");
                    SpeciesGlyph sg = layout.createSpeciesGlyph(id, sid);
                    Path2D path2D = Tools.toGeneralPath(parsePath(parts, xOffset, yOffset, new Curve()));
                    Rectangle2D bb = path2D.getBounds2D();
                    // offset is not need here, because it is already taken into account when creating the path:
                    sg.createBoundingBox(bb.getWidth(), bb.getHeight(), 1d, bb.getX(), bb.getY(), z);
                  }
                }
              }
              sb.append("\t" + item.getNodeName() + "=" + value);
            }
            if (!sb.toString().contains("fill:none")) {
              System.out.println(sb.toString());
            }
          }
          }
        }
      }
    }
  }
  
  /**
   * 
   * @param parts
   * @param xOffset
   * @param yOffset
   * @param c
   * @return
   */
  public Curve parsePath(String[] parts, double xOffset, double yOffset, Curve c) {
    int part = 0;
    double x, y;
    Point p = null, pathStart = null;
    while (part < parts.length) {
      LineSegment currSegment = null;
      if (parts[part].charAt(0) == 'M') {
        x = Double.parseDouble(parts[part].substring(1));
        y = Double.parseDouble(parts[++part]);
        p = new Point(x - xOffset, y - yOffset, z, c.getLevel(), c.getVersion());
        pathStart = p;
      } else if (parts[part].charAt(0) == 'C') {
        currSegment = c.createCubicBezier();
        currSegment.setStart(p.clone());
        CubicBezier cb = (CubicBezier) currSegment;
        p = cb.createBasePoint1(Double.parseDouble(parts[part].substring(1)) - xOffset, Double.parseDouble(parts[++part]) - yOffset, z);
        p = cb.createBasePoint2(Double.parseDouble(parts[++part]) - xOffset, Double.parseDouble(parts[++part]) - yOffset, z);
        p = currSegment.createEnd(Double.parseDouble(parts[++part]) - xOffset, Double.parseDouble(parts[++part]) - yOffset, z);
      } else if (parts[part].charAt(0) == 'Q') {
        currSegment = c.createCubicBezier();
        currSegment.setStart(p.clone());
        CubicBezier cb = (CubicBezier) currSegment;
        p = cb.createBasePoint1(Double.parseDouble(parts[part].substring(1)) - xOffset, Double.parseDouble(parts[++part]) - yOffset, z);
        p = currSegment.createEnd(Double.parseDouble(parts[++part]) - xOffset, Double.parseDouble(parts[++part]) - yOffset, z);
      } else if (parts[part].charAt(0) == 'L') {
        int pos = 0;
        do {
          currSegment = c.createLineSegment();
          currSegment.setStart(p.clone());
          x = Double.parseDouble(pos == 0 ? parts[part].substring(1) : parts[part + pos]) - xOffset;
          y = Double.parseDouble(parts[part + pos + 1]) - yOffset;
          p = currSegment.createEnd(x, y, z);
          pos += 2;
        } while (Character.isDigit(parts[part + pos].charAt(0)));
        part += pos - 1;
      } else if (parts[part].charAt(0) == 'Z') {
        // close path with a straight line to the start of the very first line segment.
        currSegment = c.createLineSegment();
        currSegment.setStart(p.clone());
        currSegment.setEnd(pathStart);
        if (parts[part].length() > 1) {
          parts[part] = parts[part].substring(1);
          part--;
        }
      } else {
        System.out.print("Skipping unknown qualifier '" + parts[part].charAt(0) + "'");
        System.out.println();
        while (Character.isDigit(parts[part + 1].charAt(0))) {
          part++;
        }
      }
      part++;
    }
    return c;
  }
  
  /**
   * 
   * @param text
   * @return
   */
  private String toSId(String text) {
    return incrementSIdSuffix(SBMLtools.toSId(text));
  }
  
  /**
   * Appends "_Number" to a given String. Number is being set to the next
   * free number, so that this sID is unique in this {@link SBMLDocument}.
   * Should only be called from "NameToSId".
   * 
   * @return
   */
  private String incrementSIdSuffix(String prefix) {
    Model model = builder.getModel();
    if (model.findNamedSBase(prefix) == null) {
      return prefix;
    }
    int i = 1;
    String aktString = prefix + "_" + i;
    while (model.findNamedSBase(aktString) != null) {
      aktString = prefix + "_" + (++i);
    }
    return aktString;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.svg.SVGHandler#getResult()
   */
  @Override
  public SBMLDocument getResult() {
    return builder.getSBMLDocument();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.svg.SVGHandler#init(double, double, double[])
   */
  @Override
  public void init(double width, double height, double[] vBox) {
    xOffset = vBox[0];
    yOffset = vBox[1];
    layout.createDimensions(width, height, depth);
  }
  
}
