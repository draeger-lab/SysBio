/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBML Editor.
 *
 * Copyright (C) 2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sbml.layout.y;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.JFrame;

import y.view.Graph2D;
import y.view.Graph2DView;
import de.zbit.sbml.layout.LayoutDirector;

/**
 * Simple GUI to display a Graph2DView.
 * 
 * It renders a SBML document (from command line arg0, or falling back to a
 * default) with LayoutDirector using YLayoutBuilder and the TikZLayoutAlgorithm
 * (YLayoutAlgorithm is not yet functional).
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YGraphView {

	/**
	 * The main graph view component.
	 */
	private Graph2DView view;

	/**
	 * Frame to hold graphical elements.
	 */
	private JFrame frame;

	/**
	 * @param args
	 * @throws Throwable 
	 */
	public YGraphView(String[] args) throws Throwable {
		File in;
		if (args.length == 1) {
			in = new File(args[0]);
		}
		else {
			in = new File("test/de/zbit/sbml/layout/y/sbml/reaction.sbml");
		}

		LayoutDirector<Graph2D> director =
			new LayoutDirector<Graph2D>(in,
					new YLayoutBuilder(),
					new YLayoutAlgorithm());
		director.run();

		Graph2D product = director.getBuilder().getProduct();
		view = new Graph2DView(product);
		Rectangle box = view.getGraph2D().getBoundingBox();  
		Dimension dim = box.getSize();  
		view.setSize(dim);  
		view.zoomToArea(box.getX() - 10, box.getY() - 10,   
				box.getWidth() + 20, box.getHeight() + 20);  

		frame = new JFrame(YGraphView.class.getSimpleName());
		frame.add(view);
		frame.setMinimumSize(new Dimension(800, 800));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new YGraphView(args);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
}
