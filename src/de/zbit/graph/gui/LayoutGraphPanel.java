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
package de.zbit.graph.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.sbml.jsbml.ext.layout.Layout;

import de.zbit.graph.RestrictedEditMode;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.sbml.layout.y.ILayoutGraph;
import de.zbit.sbml.layout.y.YLayoutAlgorithm;
import de.zbit.sbml.layout.y.YLayoutBuilder;
import de.zbit.util.Utils;
import y.view.DefaultGraph2DRenderer;
import y.view.EditMode;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;

/**
 * @author Jan Rudolph
 * @version $Rev$
 */
public class LayoutGraphPanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = -2958151300254693100L;
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(LayoutGraphPanel.class.getName());
  
  /**
   * contains graphical information
   */
  protected ILayoutGraph layoutGraph;
  /**
   * enables lasting graph manipulation
   */
  protected EditMode editMode;
  /**
   * contains all layout information
   */
  protected Layout document;
  /**
   * the view of the current graph
   */
  protected Graph2DView graph2DView;
  
  
  /**
   * @param layout the layout to be displayed by this panel
   * @param editMode the editMode to enable lasting graph manipulation
   */
  public LayoutGraphPanel(Layout layout, EditMode editMode) {
    super(new BorderLayout());
    document = layout;
    this.editMode = editMode;
    // TODO: This needs improvement!
    SwingWorker<ILayoutGraph, Void> layoutWorker = new SwingWorker<ILayoutGraph, Void>() {
      /* (non-Javadoc)
       * @see javax.swing.SwingWorker#doInBackground()
       */
      @Override
      protected ILayoutGraph doInBackground() throws Exception {
        LayoutDirector<ILayoutGraph> director =
            new LayoutDirector<ILayoutGraph>(document, new YLayoutBuilder(), new YLayoutAlgorithm());
        director.run();
        return director.getProduct();
      }
      
      /* (non-Javadoc)
       * @see javax.swing.SwingWorker#process(java.util.List)
       */
      @Override
      protected void process(List<Void> chunks) {
        // TODO Auto-generated method stub
        super.process(chunks);
      }
      
      /* (non-Javadoc)
       * @see javax.swing.SwingWorker#done()
       */
      @Override
      protected void done() {
        try {
          layoutGraph = get();
          graph2DView = new Graph2DView(layoutGraph.getGraph2D());
          DefaultGraph2DRenderer dgr = new DefaultGraph2DRenderer();
          // Drawing edges first implies that nodes are drawn last, i.e., on top of the edges.
          dgr.setDrawEdgesFirst(false);
          
          // Register the freshly configured DefaultGraph2DRenderer with the given view.
          graph2DView.setGraph2DRenderer(dgr);
        } catch (InterruptedException exc) {
          logger.log(Level.WARNING, Utils.getMessage(exc), exc);
        } catch (ExecutionException exc) {
          logger.log(Level.WARNING, Utils.getMessage(exc), exc);
        }
        init();
        // Redraw the graphical component!
        validate();
      }
      
    };
    layoutWorker.execute();
  }
  
  /**
   * 
   */
  public void init() {
    this.add(graph2DView, BorderLayout.CENTER);
    graph2DView.setOpaque(false);
    ((DefaultGraph2DRenderer) graph2DView.getGraph2DRenderer()).setDrawEdgesFirst(false);
    graph2DView.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    try {
      graph2DView.fitContent(true);
    } catch (Throwable t) {
      // Not really a problem
    }
    RestrictedEditMode.addOverviewAndNavigation(graph2DView);
    graph2DView.setFitContentOnResize(true);
  }
  
  /**
   * @return the layoutGraph
   */
  public ILayoutGraph getLayoutGraph() {
    return layoutGraph;
  }
  
  /**
   * @return the editMode
   */
  public EditMode getEditMode() {
    return editMode;
  }
  
  /**
   * @return the Document
   */
  public Layout getDocument() {
    return document;
  }
  
  /**
   * @return the graph2DView
   */
  public Graph2DView getGraph2DView() {
    return graph2DView;
  }

}
