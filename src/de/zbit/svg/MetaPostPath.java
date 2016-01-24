/*
 * $Id$ $URL$
 * --------------------------------------------------------------------- This
 * file is part of the SysBio API library.
 * 
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.svg;

import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGItem;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGPathSegList;

/**
 * Responsible for converting an SVG path element to MetaPost. This will convert
 * just the bezier curve portion of the path element, not its style. Typically
 * the SVG path data is provided from the "d" attribute of an SVG path node.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class MetaPostPath {
  private SVGOMPathElement pathElement;
  
  /**
   * Use to create an instance of a class that can parse an SVG path element to
   * produce MetaPost code.
   *
   * @param pathNode
   *        The path node containing a "d" attribute (output as MetaPost code).
   */
  public MetaPostPath(Node pathNode) {
    setPathNode(pathNode);
  }
  
  /**
   * Converts this object's SVG path to a MetaPost draw statement.
   * 
   * @return A string that represents the MetaPost code for a path element.
   */
  public String toCode() {
    StringBuilder sb = new StringBuilder(16384);
    SVGOMPathElement pathElement = getPathElement();
    SVGPathSegList pathList = pathElement.getNormalizedPathSegList();
    
    int pathObjects = pathList.getNumberOfItems();
    
    sb.append((new MetaPostComment(getId())).toString());
    
    for (int i = 0; i < pathObjects; i++) {
      SVGItem item = (SVGItem) pathList.getItem(i);
      sb.append(String.format("%s%n", item.getValueAsString()));
    }
    
    return sb.toString();
  }
  
  /**
   * Returns the value for the id attribute of the path element. If the id isn't
   * present, this will probably throw a NullPointerException.
   * 
   * @return A non-null, but possibly empty String.
   */
  private String getId() {
    SVGOMPathElement element = getPathElement();
    if (element != null) {
      NamedNodeMap nodeMap = element.getAttributes();
      if (nodeMap != null) {
        Node node = nodeMap.getNamedItem("id");
        if (node != null) {
          return node.getNodeValue();
        }
      }
    }
    return null;
  }
  
  /**
   * Typecasts the given pathNode to an SVGOMPathElement for later analysis.
   * 
   * @param pathNode
   *        The path element that contains curves, lines, and other SVG
   *        instructions.
   */
  private void setPathNode(Node pathNode) {
    pathElement = (SVGOMPathElement) pathNode;
  }
  
  /**
   * Returns an SVG document element that contains path instructions (usually
   * for drawing on a canvas).
   * 
   * @return An object that contains a list of items representing pen movements.
   */
  private SVGOMPathElement getPathElement() {
    return pathElement;
  }
}
