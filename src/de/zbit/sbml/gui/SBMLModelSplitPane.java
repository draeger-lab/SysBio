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
import java.io.IOException;
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
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;

import de.zbit.gui.GUITools;
import de.zbit.gui.layout.LayoutHelper;
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
	 * @param document
	 * @param namesIfAvailable
	 * @throws SBMLException
	 * @throws IOException
	 */
	public SBMLModelSplitPane(SBMLDocument document, boolean namesIfAvailable) throws SBMLException, IOException {
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
	 * @throws SBMLException
	 * @throws IOException
	 */
	protected JPanel createLeftComponent() throws SBMLException,
		IOException {
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
	 * @throws SBMLException
	 * @throws IOException
	 */
	protected JScrollPane createRightComponent(SBase sbase) throws SBMLException,
		IOException {
		SBasePanel sbPanel = new SBasePanel(sbase, namesIfAvailalbe, renderer);
		JScrollPane scroll = new JScrollPane(sbPanel);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return scroll;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#getRenderer()
	 */
	public EquationRenderer getEquationRenderer() {
		return renderer;
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
	 * @throws SBMLException
	 * @throws IOException
	 */
	public void init(SBMLDocument doc, boolean keepDivider) throws SBMLException,
		IOException {
		int proportionalLocation = getDividerLocation();
		
		sbmlDoc = doc.getSBMLDocument();
		////////////////////////////////////////////////////
		// Copied from previously separate method initTree:
		TreePath path = null;
		if (tree != null) {
			path = tree.getSelectionPath();
		}
		tree = new SBMLTree(sbmlDoc);
		
		tree.setShowsRootHandles(true);
		tree.setScrollsOnExpand(true);
		if (path != null) {
			tree.setExpandsSelectedPaths(true);
			tree.expandPath(path);
		}
		tree.addTreeSelectionListener(this);
		tree.setSelectionRow(0);                           //
		/////////////////////////////////////////////////////
		
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
		logger.info("searching");
		SBMLTreeSearcher worker = new SBMLTreeSearcher(this, searchField.getText(),
			tree, progressBar);
		worker.addPropertyChangeListener(this);
		worker.execute();
	}
	
	/**
	 * 
	 * @param progressBar
	 */
	public void setProgressBar(AbstractProgressBar progressBar){
		this.progressBar = progressBar;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#setRenderer(de.zbit.sbml.gui.EquationRenderer)
	 */
	public void setEquationRenderer(EquationRenderer renderer) {
		this.renderer = renderer;
	}
	
	/**
	 * @param tree the tree to set
	 */
	public void setTree(SBMLTree tree) {
		this.tree = tree;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {
		TreeNode node = (TreeNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			// Nothing is selected.
			return;
		}
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
				logger.warning(String.format("Unknown node class %s.", node.getClass().getSimpleName()));
			}
		}
	}
}
