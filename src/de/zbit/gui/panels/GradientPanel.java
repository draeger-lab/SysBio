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
package de.zbit.gui.panels;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Paints a new Gradient Panel (a simple JPanel with
 * a gradient background).
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class GradientPanel extends JPanel {
  private static final long serialVersionUID = -3813858288052450021L;
  private Color color1 = getBackground( );
  private Color color2 = color1.darker( );
  
  public GradientPanel() {
    super();
    initStdColors();
  }
  public GradientPanel(Color color1, Color color2) {
    super();
    initColors(color1, color2);
  }
  
  public GradientPanel(LayoutManager l) {
    super(l);
    initStdColors();
  }
  public GradientPanel(LayoutManager l, Color color1, Color color2) {
    super(l);
    initColors(color1, color2);
  }
  
  private void initStdColors() {
    initColors(null, null);
  }
  private void initColors(Color c1, Color c2) {
    if (color1==null) {
      color1 = getBackground( );
    } else color1 = c1;
    if (color2==null) {
      color2 = color1.darker( );
    } else color2 = c2;
  }
  
  
  protected void paintComponent( Graphics g )  {
    Graphics2D g2d = (Graphics2D)g;
    
    int w = getWidth( );
    int h = getHeight( );
    color1 = getBackground( );
    color2 = color1.darker( );
    // Paint a gradient from top to bottom
    GradientPaint gp = new GradientPaint(
        0, 0, color1,
        0, h, color2 );
    g2d.setPaint( gp );
    g2d.fillRect( 0, 0, w, h );
  }
  
}
