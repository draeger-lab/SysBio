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
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;

import de.zbit.gui.GUITools;
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.io.OpenedFile;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * A specialized {@link JSplitPane} that displays a {@link JTree} containing all
 * model elements of a JSBML model on the left hand side and an
 * {@link SBasePanel} showing details of the active element in the tree on the
 * right hand side.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sebastian Nagel
 * @since 1.0 (originates from SBMLsqueezer 1.3)
 * @version $Rev$
 */
public class SBMLModelSplitPane extends JSplitPane implements
		EquationComponent, PropertyChangeListener, TreeSelectionListener {
	
	/**
	 * 
	 */
	private static final transient Logger logger = Logger.getLogger(SBMLModelSplitPane.class.getName());

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 494330526896453639L;
	
	/**
	 * 
	 */
	private boolean namesIfAvailalbe;
	
	/**
	 * 
	 */
	private OpenedFile<SBMLDocument> openedFile;
	
	/**
	 * 
	 */
	private AbstractProgressBar progressBar;
	
	/**
	 * The renderer to be used to display equations.
	 */
	private EquationRenderer renderer;

	/**
	 * 
	 */
	protected SBMLDocument sbmlDoc;

	/**
	 * 
	 */
	protected JTextField searchField;
	
	private Timer swingTimer;
	
	/**
	 * 
	 */
	protected SBMLTree tree;
	
	/**
	 * 
	 * @param file
	 * @param namesIfAvailable
	 */
	public SBMLModelSplitPane(OpenedFile<SBMLDocument> file, boolean namesIfAvailable) {
		this(file.getDocument(), namesIfAvailable);
		this.openedFile = file;
	}

	/**
	 * 
	 * @param document
	 * @param namesIfAvailable
	 */
	public SBMLModelSplitPane(SBMLDocument document, boolean namesIfAvailable) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		this.namesIfAvailalbe = namesIfAvailable;
		init(document, false);
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
	 * @param nodeInfo
	 * @return
	 */
	protected JPanel createLeftComponent() {
		JPanel leftPane = new JPanel();
		JScrollPane treePane = new JScrollPane(tree);
	  	
		LayoutHelper lh = new LayoutHelper(leftPane);
		lh.add(treePane, 0, 0, 2, 1, 1d, 1d);
	  	
		searchField = new JTextField();
		searchField.setEditable(true);
		searchField.setMaximumSize(new Dimension(searchField.getSize().width, 10));
		searchField.getDocument().addDocumentListener(EventHandler.create(DocumentListener.class, this, "changedUpdate", ""));
		JLabel label = new JLabel(UIManager.getIcon("ICON_SEARCH_16"));
		label.setOpaque(true);
		lh.add(label, 0, 2, 1, 1, 0d, 0d);
		lh.add(searchField, 1, 2, 1, 1, 0d, 0d);
		
		return leftPane;
	}
	
	/**
	 * @param sbase
	 * @return
	 */
	protected JScrollPane createRightComponent(SBase sbase) {
		SBasePanel sbPanel = new SBasePanel(sbase, namesIfAvailalbe, renderer);
		JScrollPane scroll = new JScrollPane(sbPanel);
		return scroll;
	}
	
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#getRenderer()
	 */
	public EquationRenderer getEquationRenderer() {
		return renderer;
	}
	
	/**
	 * @return the openedFile
	 */
	public OpenedFile<SBMLDocument> getOpenedFile() {
		return openedFile;
	}
	
	/**
	 * 
	 * @return
	 */
	public SBMLDocument getSBMLDocument() {
		return (SBMLDocument) tree.getModel().getRoot();
	}
	
	/**
	 * @return the tree
	 */
	public SBMLTree getTree() {
		return tree;
	}
	
	/**
	 * @param doc
	 * @param keepDivider
	 */
	public void init(SBMLDocument doc, boolean keepDivider) {
		int proportionalLocation = getDividerLocation();
		
		this.sbmlDoc = doc;

		TreePath path = null;
		TreeNode[] savedState = null;
		if (tree != null) {
			path = tree.getSelectionPath();
			savedState = tree.getSavedState();
		}
		tree = new SBMLTree(sbmlDoc);
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
		}
		tree.addTreeSelectionListener(this);
		tree.setSelectionRow(0);
		
		setLeftComponent(createLeftComponent());
		setRightComponent(createRightComponent(doc));
		if (keepDivider) {
			setDividerLocation(proportionalLocation);
		}
		validate();
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
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#setRenderer(de.zbit.sbml.gui.EquationRenderer)
	 */
	public void setEquationRenderer(EquationRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * 
	 * @param progressBar
	 */
	public void setProgressBar(AbstractProgressBar progressBar){
		this.progressBar = progressBar;
	}
	
	/**
	 * @param tree the tree to set
	 */
	public void setTree(SBMLTree tree) {
		this.tree = tree;
	}

	/**
	 * 
	 * @param source
	 */
	private void updateRightComponent(Object source) {
		if ((source == null) || !(source instanceof TreeNode)) {
			// Nothing is selected.
			return;
		}
		TreeNode node = (TreeNode) source;
		if (node instanceof SBMLNode) {
			TreeNodeWithChangeSupport sbmlNode = ((SBMLNode) node).getUserObject();
			if (sbmlNode instanceof SBase) {
				int proportionalLocation = getDividerLocation();
				try {
					setRightComponent(createRightComponent((SBase) sbmlNode));
				} catch (Exception exc) {
					GUITools.showErrorMessage(this, exc);
				}
				setDividerLocation(proportionalLocation);
			} else {
				logger.warning(MessageFormat.format("Unknown node class {0}.", node.getClass().getSimpleName()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JSplitPane#updateUI()
	 */
	@Override
	public void updateUI() {
		if (this.tree != null) {
			this.tree.updateUI();
			updateRightComponent(this.tree.getLastSelectedPathComponent());
		}
		super.updateUI();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		updateRightComponent(this.tree.getLastSelectedPathComponent());
	}
	
}
