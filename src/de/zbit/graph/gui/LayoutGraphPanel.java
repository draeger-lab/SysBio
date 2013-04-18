/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

import javax.swing.JPanel;

import org.sbml.jsbml.ext.layout.Layout;

import y.view.DefaultGraph2DRenderer;
import y.view.EditMode;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import de.zbit.graph.RestrictedEditMode;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.sbml.layout.y.ILayoutGraph;
import de.zbit.sbml.layout.y.YLayoutAlgorithm;
import de.zbit.sbml.layout.y.YLayoutBuilder;

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
		this.document = layout;
		this.editMode = editMode;
		LayoutDirector<ILayoutGraph> director = 
				new LayoutDirector<ILayoutGraph>(layout, new YLayoutBuilder(), new YLayoutAlgorithm());
		director.run();
		this.layoutGraph = director.getProduct();
		this.graph2DView = new Graph2DView(layoutGraph.getGraph2D());
		init();
	}
	
	/**
	 * 
	 */
	public void init() {
		this.add(graph2DView, BorderLayout.CENTER);
		this.graph2DView.setOpaque(false);
		((DefaultGraph2DRenderer) this.graph2DView.getGraph2DRenderer()).setDrawEdgesFirst(false);
		this.graph2DView.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
		try {
			this.graph2DView.fitContent(true);
		} catch (Throwable t) {
			// Not really a problem
		}
		RestrictedEditMode.addOverviewAndNavigation(this.graph2DView);
		this.graph2DView.setFitContentOnResize(true);
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
		return this.document;
	}

	/**
	 * @return the graph2DView
	 */
	public Graph2DView getGraph2DView() {
		return graph2DView;
	}

}
