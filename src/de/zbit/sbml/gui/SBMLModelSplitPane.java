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
package de.zbit.sbml.gui;

import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;

import de.zbit.gui.GUITools;
import de.zbit.io.OpenedFile;

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
		EquationComponent, TreeSelectionListener {
	
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
	//private SBMLTree tree;

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
		return sbmlDoc;
	}
	
	/**
	 * @return the tree
	 */
	public SBMLTree getTree() {
		if (sbmlDoc == null) {
			return null;
		}
		return ((SBMLTreeSearchComponent) getLeftComponent()).getTree();
	}
	
	/**
	 * @param doc
	 * @param keepDivider
	 */
	public void init(SBMLDocument doc, boolean keepDivider) {
		int proportionalLocation = getDividerLocation();
		
		this.sbmlDoc = doc;
		
		SBMLTreeSearchComponent treeSearchComponent = new SBMLTreeSearchComponent(doc);
		setLeftComponent(treeSearchComponent);
		treeSearchComponent.getTree().addTreeSelectionListener(this);
		setRightComponent(createRightComponent(doc));
		if (keepDivider) {
			setDividerLocation(proportionalLocation);
		}
		updateUI();
		validate();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#setRenderer(de.zbit.sbml.gui.EquationRenderer)
	 */
	public void setEquationRenderer(EquationRenderer renderer) {
		this.renderer = renderer;
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
		if (this.sbmlDoc != null) {
			SBMLTreeSearchComponent leftComponent = (SBMLTreeSearchComponent) getLeftComponent(); 
			leftComponent.updateUI();
			updateRightComponent(leftComponent.getTree().getLastSelectedPathComponent());
		}
		super.updateUI();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		updateRightComponent(((JTree) evt.getSource()).getLastSelectedPathComponent());
	}
	
}
