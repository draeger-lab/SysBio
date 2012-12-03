/*
 * $Id: YGraphView.java 1064 2012-10-29 15:46:01Z jmatthes $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/test/de/zbit/sbml/layout/y/YGraphView.java $
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFrame;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.LayoutConstants;

import y.view.DefaultGraph2DRenderer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import de.zbit.graph.RestrictedEditMode;
import de.zbit.io.OpenedFile;
import de.zbit.sbml.gui.SBMLReadingTask;
import de.zbit.sbml.layout.GlyphCreator;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.util.logging.LogUtil;

/**
 * Simple GUI to display a Graph2DView.
 * 
 * It renders an {@link SBMLDocument} (from command line arg0, or falling back to a
 * default) with {@link LayoutDirector} using {@link YLayoutBuilder} and the TikZLayoutAlgorithm
 * (YLayoutAlgorithm is not yet functional).
 * 
 * @author Jakob Matthes
 * @author Andreas Dr&auml;ger
 * @version $Rev: 1064 $
 */
public class YGraphView implements PropertyChangeListener {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		LogUtil.initializeLogging(YGraphView.class.getPackage().toString());
		final File in = new File(args[0]);
//		javax.swing.SwingUtilities.invokeLater(new Runnable() {
//			/* (non-Javadoc)
//			 * @see java.lang.Runnable#run()
//			 */
//			@Override
//			public void run() {
				try {
					new YGraphView(in);
				} catch (Throwable e) {
					e.printStackTrace();
				}
//			}
//		});
	}
	
	/**
	 * 
	 */
	public YGraphView() {
	}
	
	/**
	 * @param inputFile
	 * @throws Throwable 
	 */
	public YGraphView(File inputFile) throws Throwable {
		this();
		SBMLReadingTask readingTask = new SBMLReadingTask(inputFile, null, this);
		readingTask.execute();
	}

	/**
	 * 
	 * @param doc
	 */
	public YGraphView(SBMLDocument doc) {
		this();
		setSBMLDocument(doc);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SBMLReadingTask.SBML_READING_SUCCESSFULLY_DONE)) {
			@SuppressWarnings("unchecked")
		  OpenedFile<SBMLDocument> openedFile = (OpenedFile<SBMLDocument>) evt.getNewValue();
			SBMLDocument doc = openedFile.getDocument();
			setSBMLDocument(doc);
		}
	}

	/**
	 * 
	 * @param doc
	 */
	private void setSBMLDocument(SBMLDocument doc) {
		Model model = doc.getModel();
		ExtendedLayoutModel ext = (ExtendedLayoutModel) model.getExtension(LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion()));
		if (ext == null) {
			new GlyphCreator(model).create();
			ext = (ExtendedLayoutModel) model.getExtension(LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion()));

		}
		LayoutDirector<ILayoutGraph> director =
				new LayoutDirector<ILayoutGraph>(doc,
						new YLayoutBuilder(),
						new YLayoutAlgorithm());
			director.run();
			Graph2D product = director.getBuilder().getProduct().getGraph2D();
			
			int width = 960, height = 720;
			
			Graph2DView view = new Graph2DView(product);
			Rectangle box = view.getGraph2D().getBoundingBox();  
			Dimension dim = box.getSize();  
			view.setSize(dim);  
	    // view.zoomToArea(box.getX() - 10, box.getY() - 10, box.getWidth() + 20, box.getHeight() + 20);
			Dimension minimumSize = new Dimension( (int)Math.max(view.getMinimumSize().getWidth(), 100), (int) Math.max(view.getMinimumSize().getHeight(), height/2) );
	    view.setMinimumSize(minimumSize);
	    view.setPreferredSize(new Dimension(100, (int) Math.max(height * 0.6, 50)));
			view.setOpaque(false);
			((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setDrawEdgesFirst(false);
	    view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
	    try {
	      view.fitContent(true);
	    } catch (Throwable t) {
	    	// Not really a problem
	    }
			RestrictedEditMode.addOverviewAndNavigation(view);
	    view.setFitContentOnResize(true);

	    JFrame frame = new JFrame();
      frame.setTitle(YGraphView.class.getSimpleName());
			frame.add(view);
			frame.setMinimumSize(new Dimension(width, height));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
	}

}
