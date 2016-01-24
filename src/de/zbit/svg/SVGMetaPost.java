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

import org.w3c.dom.NodeList;


/**
 * Responsible for converting all SVG path elements into MetaPost curves.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class SVGMetaPost implements SVGHandler<String> {
  
  /**
   * 
   */
  private StringBuilder sb;
  
  /**
   * 
   */
  public SVGMetaPost() {
    sb = new StringBuilder();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.svg.SVGHandler#handle(de.zbit.svg.Element, org.w3c.dom.NodeList)
   */
  @Override
  public void handle(Element element, NodeList nodes) {
    if (element == Element.PATH) {
      /*
       * Finds all the path nodes and converts them to MetaPost code.
       */
      int pathNodeCount = nodes.getLength();
      
      for (int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++) {
        MetaPostPath mpp = new MetaPostPath(nodes.item(iPathNode));
        sb.append(mpp.toCode());
      }
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.svg.SVGHandler#getResult()
   */
  @Override
  public String getResult() {
    return sb.toString();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.svg.SVGHandler#init(double, double, double[])
   */
  @Override
  public void init(double width, double height, double[] vBox) {
    // TODO Auto-generated method stub
    
  }
  
}
