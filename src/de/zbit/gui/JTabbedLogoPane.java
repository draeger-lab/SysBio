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
package de.zbit.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

/**
 * This is a {@link JTabbedPaneDraggableAndCloseable} with the single extension
 * that the TabbedPane without any tabs contains a centered logo image.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class JTabbedLogoPane extends JTabbedPane { //JTabbedPaneDraggableAndCloseable {
  private static final long serialVersionUID = 6977541013827456374L;
  
  private ImageIcon img=null;
  
  /**
   * Construct a new {@link #JTabbedLogoPane(ImageIcon)} that displays
   * the given image if no tab is currently on the
   * {@link javax.swing.JTabbedPane}.
   * @param img
   */
  public JTabbedLogoPane(ImageIcon img) {
    super();
    this.img = img;
  }
  
  
  
  /**
   * @return the img
   */
  public ImageIcon getLogoImage() {
    return img;
  }



  /**
   * @param img the img to set
   */
  public void setLogoImage(ImageIcon img) {
    this.img = img;
    this.repaint();
  }



  @Override
  public void paintComponent(Graphics g) {
    // TODO: Please catch the "java.lang.OutOfMemoryError: Java heap space" Exception when calling 'super' and show an apporpriate message.
    super.paintComponent(g);
    if (g==null || img==null) return;
    
    // Antialiasing ON
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    
    // Paint logo
    if (getSelectedIndex()<0) {
      Dimension d = getSize();
      // XXX: On might add an option to choose one of the three posibilities. Default
      // should be drawing the image centered.
      
      // Draw image in upper left corner
      //g2d.drawImage(img.getImage(), 0, 0, d.width, d.height, null);
      
      // Center image
      g2d.drawImage(img.getImage(), d.width/2-img.getIconWidth()/2, d.height/2-img.getIconHeight()/2, this);
      
      // Draw image full-screen
      //g2d.drawImage(img.getImage(),0,0,this);
    }
  }
  
}
