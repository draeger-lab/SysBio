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

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.sbml.jsbml.SBMLDocument;

import y.view.EditMode;
import y.view.Graph2DView;
import y.view.ViewMode;

/**
 * @author Andreas Dr&auml;ger
 * @date 16:03:47
 * @since 1.1
 * @version $Rev$
 */
public class GraphTest {
	
	/**
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame f = new JFrame("Graph Test");
		SBMLDocument doc = new SBMLDocument(3, 1);
		TranslatorSBMLgraphPanel panel = new TranslatorSBMLgraphPanel(doc, false);
		Graph2DView view = panel.getGraph2DView();
		view.removeViewMode((ViewMode) view.getViewModes().next());
    EditMode editMode = new SBGNEditMode<SBMLDocument>(panel.getConverter());
    editMode.showNodeTips(true);
    view.addViewMode(editMode);
		f.getContentPane().add(panel);
		f.setMinimumSize(new Dimension(640, 480));
		f.pack();
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
	
}
