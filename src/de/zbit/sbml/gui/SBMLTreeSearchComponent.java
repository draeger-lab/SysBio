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
package de.zbit.sbml.gui;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.SBMLDocument;

import de.zbit.gui.layout.LayoutHelper;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class SBMLTreeSearchComponent extends JPanel implements PropertyChangeListener {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -8792142663777302836L;

	/**
	 * A {@link Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(SBMLTreeSearchComponent.class.getName());

	/**
	 * 
	 */
	private AbstractProgressBar progressBar;
	
	/**
	 * 
	 */
	protected JTextField searchField;
	/**
	 * 
	 */
	private Timer swingTimer;
	
	/**
	 * 
	 */
	protected SBMLTree tree;
	
	public SBMLTreeSearchComponent(SBMLDocument doc) {
		super();
		
		TreePath path = null;
		TreeNode[] savedState = null;
		if (tree != null) {
			path = tree.getSelectionPath();
			savedState = tree.getSavedState();
		}
		tree = new SBMLTree(doc);
		if ((path != null) && (savedState != null)) {
			// Copy the old treePath and savedState to the new tree structure
			path = tree.copyPath(path);
			savedState = tree.copyPath(savedState);
		}
		tree.setSavedState(savedState);
		tree.setShowsRootHandles(true);
		tree.setScrollsOnExpand(true);
		if (path != null) {
			tree.setExpandsSelectedPaths(true);
			tree.expandPath(path);
		} else {
			if (doc.isSetModel() && !doc.getModel().isLeaf()) {
				SBMLNode nodes[] = new SBMLNode[2];
				nodes[0] = (SBMLNode) tree.getModel().getRoot();
				nodes[1] = (SBMLNode) nodes[0].getChildAt(0);
				path = new TreePath(nodes);
				tree.expandPath(path);
			}
		}
		tree.setSelectionRow(1);
		
		JScrollPane treePane = new JScrollPane(tree);
	  	
		LayoutHelper lh = new LayoutHelper(this);
		lh.add(treePane, 0, 0, 2, 1, 1d, 1d);
	  	
		searchField = new JTextField();
		searchField.setEditable(true);
		searchField.setMaximumSize(new Dimension(searchField.getSize().width, 10));
		searchField.getDocument().addDocumentListener(EventHandler.create(DocumentListener.class, this, "changedUpdate", ""));
		JLabel label = new JLabel(UIManager.getIcon("ICON_SEARCH_16"));
		label.setOpaque(true);
		lh.add(label, 0, 2, 1, 1, 0d, 0d);
		lh.add(searchField, 1, 2, 1, 1, 1d, 0d);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (tree != null) {
			tree.updateUI();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent evt) {
		int delay = 900;
		if (swingTimer == null) {
			swingTimer = new Timer(delay, EventHandler.create(ActionListener.class, this, "searchTree"));
			swingTimer.setRepeats(false);
			swingTimer.setInitialDelay(delay);
			swingTimer.start();
		} else {
			swingTimer.restart();
		}
		if (searchField.getText().length() == 0) {
			swingTimer.stop();
			swingTimer = null;
			tree.setAllVisible();
			tree.restoreSelectionPath();
			searchField.setEnabled(true);
		}
	}

	/**
	 * 
	 * @return
	 */
	public SBMLTree getTree() {
		return tree;
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("done")) {
			searchField.setEnabled(true);
			searchField.requestFocus();
		}
	}
	
	/**
	 * Invoke search
	 */
	public void searchTree() {
		swingTimer = null;
		searchField.setEnabled(false);
		logger.fine("searching");
		SBMLTreeSearcher worker = new SBMLTreeSearcher(this, searchField.getText(),
			tree, progressBar);
		worker.addPropertyChangeListener(this);
		worker.execute();
	}
	
}
