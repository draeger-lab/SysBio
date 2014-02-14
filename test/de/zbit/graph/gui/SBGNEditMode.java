/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011-2014 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.gui;

import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.sbml.jsbml.SBO;

import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.view.Graph2D;
import de.zbit.graph.RestrictedEditMode;
import de.zbit.graph.io.SB_2GraphML;

/**
 * @author Andreas Dr&auml;ger
 * @date 16:23:33
 * @since 1.1
 * @version $Rev$
 */
public class SBGNEditMode<T> extends RestrictedEditMode implements GraphListener {
  
  private int count = 0;
  private SB_2GraphML<T> converter;
  
  /**
   * 
   */
  public SBGNEditMode(SB_2GraphML<T> converter) {
    super();
    this.converter = converter;
    allowNodeCreation(true);
    setCreateEdgeMode(new CreateReactionEdgeMode());
    allowEdgeCreation(true);
  }
  
  /* (non-Javadoc)
   * @see y.view.EditMode#mousePressedRight(double, double)
   */
  @Override
  public void mousePressedRight(double x, double y) {
    //		super.mousePressedRight(x, y);
    
    String name = JOptionPane.showInputDialog("Enter name:", "s" + ++count);
    if ((name != null) && (name.length() > 0) && !name.equalsIgnoreCase("undefined")) {
      Graph2D graph = getGraph2D();
      //			graph.addGraphListener(this);
      //			NodeRealizer nr = SBGNVisualizationProperties.getNodeRealizer(SBO.getSimpleMolecule());
      //			Node n = graph.createNode();
      //			nr = nr.createCopy();
      //			nr.setCenter(x, y);
      //			nr.setWidth(46);
      //			nr.setHeight(17);
      //			nr.setLabelText(name);
      //			nr.setFillColor(Color.GREEN);
      //			graph.setRealizer(n, nr);
      Node n = converter.createNode("s" + count, name, SBO.getSimpleMolecule(), x, y);
      
      graph.updateViews();
      if (!name.equals("s" + count)) {
        count--;
      }
    } else {
      count--;
    }
  }
  
  /**
   * @param listener
   * @param parent
   */
  public SBGNEditMode(ActionListener listener, JComponent parent) {
    super(listener, parent);
  }
  
  /* (non-Javadoc)
   * @see y.base.GraphListener#onGraphEvent(y.base.GraphEvent)
   */
  @Override
  public void onGraphEvent(GraphEvent evt) {
    if (evt.getType() == GraphEvent.NODE_CREATION) {
      System.out.println(evt.getData() + "\t" + evt.getSource() + "\t" + evt.getGraph());
    }
  }
  
}
