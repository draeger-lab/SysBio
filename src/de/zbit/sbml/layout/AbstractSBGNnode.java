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
package de.zbit.sbml.layout;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @param <T> @see {@link SBGNNode}
 */
public abstract class AbstractSBGNnode<T> implements SBGNNode<T> {
  
  /**
   * Line width in px.
   */
  private double lineWidth = 1d;
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNNode#getLineWidth()
   */
  @Override
  public double getLineWidth() {
    return lineWidth;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNNode#setLineWidth(double)
   */
  @Override
  public void setLineWidth(double lineWidth) {
    this.lineWidth = lineWidth;
  }
  
}
