/* $Id$
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
package de.zbit.sbml.gui;

import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.UnitDefinition;

import de.zbit.gui.GUITools;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class ASTNodeSplitPane extends JSplitPane implements EquationComponent, TreeSelectionListener {

	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(ASTNodeSplitPane.class.getName());
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -6323851778861392133L;
	
	/**
	 * 
	 */
	private EquationRenderer renderer;

	/**
	 * 
	 */
	private JTree tree;
	
	/**
	 * 
	 * @param ast
	 */
	public ASTNodeSplitPane(ASTNode ast) {
		super();
		tree = new JTree(ast);
		tree.expandPath(tree.getPathForRow(tree.getRowCount()));
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new ASTNodeTreeCellRenderer());
		setLeftComponent(new JScrollPane(tree));
		setRightComponent(new JPanel());
	}
	
	/**
	 * Function to display the properties of {@link ASTNode} objects.
	 * 
	 * @param nodeInfo
	 * @return
	 * @throws SBMLException
	 * @throws IOException
	 */
	protected JScrollPane createRightComponent(ASTNode node) throws SBMLException,
		IOException {
		return new JScrollPane(new ASTNodePanel(node, true, true, renderer));
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#getEquationRenderer()
	 */
	public EquationRenderer getEquationRenderer() {
		return renderer;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#setEquationRenderer(de.zbit.sbml.gui.EquationRenderer)
	 */
	public void setEquationRenderer(EquationRenderer renderer) {
		this.renderer = renderer;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		TreeNode node = (TreeNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			// Nothing is selected.
			return;
		}
		if (node instanceof ASTNode) {
      int proportionalLocation = getDividerLocation();
      try {
      	ASTNode ast = (ASTNode) node;
        setRightComponent(createRightComponent(ast));
        UnitDefinition ud = ast.deriveUnit();
				if (ud != null) {
					logger.info(UnitDefinition.printUnits(ud, true));
				} else {
					logger.warning("null");
				}
      } catch (Exception exc) {
        GUITools.showErrorMessage(this, exc);
      }
      setDividerLocation(proportionalLocation);
    } else {
			logger.warning(String.format("Unknown node class %s.", node.getClass().getSimpleName()));
		}
	}
	
}
