/* $Id$
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
package de.zbit.sbml.gui;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.util.filters.OrFilter;

import de.zbit.gui.GUITools;
import de.zbit.gui.ProgressBarSwing;
import de.zbit.sbml.util.RegexpAssignmentVariableFilter;
import de.zbit.sbml.util.RegexpNameFilter;
import de.zbit.sbml.util.RegexpSpeciesReferenceFilter;
import de.zbit.util.AbstractProgressBar;

/**
 * Filters a given {@link SBMLTree} according to a given search {@link String}.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sebastian Nagel
 * @since 1.1
 * @version $Rev$
 */
public class SBMLTreeSearcher extends SwingWorker<List<TreeNode>, Void> {
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(SBMLTreeSearcher.class.getName());
	/**
	 * 
	 */
	private Component parent;
	/**
	 * 
	 */
	private AbstractProgressBar progressBar;
	/**
	 * 
	 */
	private String searchString;
	
	/**
	 * 
	 */
	private SBMLTree tree;
	
	/**
	 * 
	 * @param searchString
	 * @param node
	 */
	public SBMLTreeSearcher(Component parent, String searchString, SBMLTree tree, AbstractProgressBar progressBar) {
		super();
		this.parent = parent;
		this.searchString = searchString;
		this.tree = tree;
		this.progressBar = progressBar;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected List<TreeNode> doInBackground() throws Exception {
		tree.setAllVisible();
		
		if (searchString.equals("")) {
			SBMLNode.setShowInvisible(true);
			return null;
		} else {
			String search = ".*" + searchString + ".*";
			RegexpNameFilter nameFilter = new RegexpNameFilter(search, false);
			RegexpSpeciesReferenceFilter specFilter = new RegexpSpeciesReferenceFilter(search, false);
			RegexpAssignmentVariableFilter assFilter = new RegexpAssignmentVariableFilter(search, false);
			OrFilter filter = new OrFilter(nameFilter, specFilter, assFilter);
			tree.search(filter, progressBar);
			if ((tree.getModel() != null) && (tree.getModel().getRoot() != null)) {
				return ((SBMLNode) tree.getModel().getRoot()).getUserObject().filter(filter);
			}
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		List<TreeNode> list = null;
		try {
			list = get();
		} catch (InterruptedException exc) {
			GUITools.showErrorMessage(parent, exc);
		} catch (ExecutionException exc) {
			GUITools.showErrorMessage(parent, exc);
		}
		if (list == null) {
			SBMLNode.setShowInvisible(true);
		} else {
			logger.log(Level.INFO, "Expanding...");
			try {
				tree.expandAll(list, true, progressBar);
			} catch (Exception e) {}
			if ((progressBar != null) && (progressBar instanceof ProgressBarSwing)) {
				((ProgressBarSwing) progressBar).getProgressBar().setVisible(false);
			}
		}
		logger.log(Level.INFO, "Ready.");
		firePropertyChange("done", null, list);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSearchString() {
		return searchString;
	}
	
}
