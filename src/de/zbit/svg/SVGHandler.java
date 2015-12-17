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

import org.w3c.dom.NodeList;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @param <T>
 */
public interface SVGHandler<T> {
  
  /**
   * 
   * @param element
   * @param elements
   */
  void handle(Element element, NodeList elements);
  
  /**
   * 
   * @return
   */
  T getResult();
  
  /**
   * 
   * @param width
   * @param height
   * @param vBox
   */
  void init(double width, double height, double[] vBox);
  
}
