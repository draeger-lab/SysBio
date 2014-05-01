/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package edu.ucsd.sbrg.bionetview;

import static org.sbml.jsbml.util.Pair.pairOf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.sbml.jsbml.util.Pair;

import y.base.Edge;
import y.base.Node;
import y.view.DefaultGraph2DRenderer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import de.zbit.graph.RestrictedEditMode;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class BioNetViewPanel extends JPanel implements ItemListener {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 2630138750911663597L;
  
  private Graph2DView activeView;
  private JComboBox layoutSelect;
  /**
   * Keeps all graphs together with a title for each graph in a list.
   */
  private List<Pair<String, Graph2DView>> listOfGraphs;
  
  /**
   * 
   * @param layouts
   */
  public BioNetViewPanel(Pair<String, Graph2D>... layouts) {
    super(new BorderLayout());
    listOfGraphs = new LinkedList<Pair<String,Graph2DView>>();
    activeView = null;
    layoutSelect = null;
    if (layouts != null) {
      for (Pair<String, Graph2D> layout : layouts) {
        add(layout);
      }
    }
  }
  
  /**
   * 
   * @param layout
   */
  public void add(Pair<String, Graph2D> layout) {
    if (activeView != null) {
      remove(activeView);
    }
    
    int WINDOW_HEIGHT = 720;
    
    activeView = new Graph2DView(layout.getValue());
    DefaultGraph2DRenderer dgr = new DefaultGraph2DRenderer();
    dgr.setDrawEdgesFirst(true);
    activeView.setGraph2DRenderer(dgr);
    Rectangle box = activeView.getGraph2D().getBoundingBox();
    Dimension dim = box.getSize();
    activeView.setSize(dim);
    // activeView.zoomToArea(box.getX() - 10, box.getY() - 10, box.getWidth() + 20, box.getHeight() + 20);
    Dimension minimumSize = new Dimension(
      (int) Math.max(activeView.getMinimumSize().getWidth(), 100),
      (int) Math.max(activeView.getMinimumSize().getHeight(), WINDOW_HEIGHT/2d));
    activeView.setMinimumSize(minimumSize);
    activeView.setPreferredSize(new Dimension(100, (int) Math.max(WINDOW_HEIGHT * 0.6d, 50d)));
    activeView.setOpaque(false);
    
    activeView.setGraph2DRenderer(new DefaultGraph2DRenderer() {
      /* (non-Javadoc)
       * @see y.view.DefaultGraph2DRenderer#getLayer(y.view.Graph2D, y.base.Edge)
       */
      @Override
      protected int getLayer(Graph2D graph, Edge edge) {
        return 1;
      }
      /* (non-Javadoc)
       * @see y.view.DefaultGraph2DRenderer#getLayer(y.view.Graph2D, y.base.Node)
       */
      @Override
      protected int getLayer(Graph2D graph, Node node) {
        return 0;
      }
    });
    
    ((DefaultGraph2DRenderer) activeView.getGraph2DRenderer()).setLayeredPainting(true);
    activeView.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    try {
      activeView.fitContent(true);
    } catch (Throwable t) {
      // Not really a problem
    }
    RestrictedEditMode.addOverviewAndNavigation(activeView);
    activeView.addViewMode(new RestrictedEditMode());
    activeView.setFitContentOnResize(true);
    
    listOfGraphs.add(pairOf(layout.getKey(), activeView));
    add(activeView, BorderLayout.CENTER);
    
    if (listOfGraphs.size() > 1) {
      if (layoutSelect == null) {
        layoutSelect = new JComboBox();
        JPanel northPanel = new JPanel();
        northPanel.setOpaque(false);
        northPanel.add(layoutSelect);
        add(northPanel, BorderLayout.NORTH);
        layoutSelect.addItem(listOfGraphs.get(0).getKey());
        layoutSelect.addItemListener(this);
      }
      layoutSelect.addItem(layout.getKey());
      layoutSelect.setSelectedIndex(listOfGraphs.size() - 1);
    }
  }
  
  /* (non-Javadoc)
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  @Override
  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() == layoutSelect) {
      if (evt.getStateChange() == ItemEvent.SELECTED) {
        activeView = listOfGraphs.get(layoutSelect.getSelectedIndex()).getValue();
        add(activeView, BorderLayout.CENTER);
        validate();
        activeView.updateView();
      } else if (activeView != null) {
        remove(activeView);
        validate();
      }
    }
  }
  
}
