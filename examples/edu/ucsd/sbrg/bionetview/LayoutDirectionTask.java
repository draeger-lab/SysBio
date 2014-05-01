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

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.util.Pair;

import y.view.Graph2D;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.sbml.layout.y.ILayoutGraph;
import de.zbit.sbml.layout.y.YLayoutAlgorithm;
import de.zbit.sbml.layout.y.YLayoutBuilder;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class LayoutDirectionTask extends SwingWorker<List<Pair<String, Graph2D>>, Pair<String, Graph2D>> {
  
  /**
   * 
   */
  public static final String GRAPH_LAYOUT_DONE = LayoutDirectionTask.class.getCanonicalName() + ".graphLayoutDone";
  
  private Component parent;
  private LayoutModelPlugin layoutPlugin;
  
  /**
   * 
   * @param layoutPlugin
   * @param parent
   */
  public LayoutDirectionTask(LayoutModelPlugin layoutPlugin, Component parent) {
    this.layoutPlugin = layoutPlugin;
    this.parent = parent;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  @SuppressWarnings("unchecked")
  protected List<Pair<String, Graph2D>> doInBackground() throws Exception {
    SBMLDocument doc = layoutPlugin.getSBMLDocument();
    LayoutDirector<ILayoutGraph> director = new LayoutDirector<ILayoutGraph>(doc, new YLayoutBuilder(), new YLayoutAlgorithm());
    List<Pair<String, Graph2D>> graphList = new LinkedList<Pair<String, Graph2D>>();
    for (int layoutIndex = 0; layoutIndex < layoutPlugin.getLayoutCount(); layoutIndex++) {
      Layout layout = layoutPlugin.getLayout(layoutIndex);
      director.setLayoutIndex(layoutIndex);
      director.run();
      Graph2D product = director.getProduct().getGraph2D();
      Pair<String, Graph2D> pair = pairOf(layout.isSetName() ? layout.getName() : layout.getId(), product);
      publish(pair);
      graphList.add(pair);
    }
    return graphList;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#process(java.util.List)
   */
  @Override
  protected void process(List<Pair<String, Graph2D>> chunks) {
    firePropertyChange(GRAPH_LAYOUT_DONE, null, chunks);
  }
  
}
