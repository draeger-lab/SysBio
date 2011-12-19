/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;

/**
 * A specialized {@link JTree} that shows the elements of a JSBML model as a
 * hierarchical structure.
 * 
 * @author Simon Sch&auml;fer
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev$
 */
public class SBMLTree extends JTree implements MouseListener, ActionListener {
	
	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = -3081533906479036522L;
	
	/**
	 * 
	 */
	private SBase currSBase;
	
	/**
	 * 
	 */
	private JPopupMenu popup;
	
	/**
	 * 
	 */
	private Set<ActionListener> setOfActionListeners;
	
	/**
	 * @param sbase
	 */
	public SBMLTree(SBase sbase) {
		super(createNodes(sbase));
		init();
	}
	
	/**
	 * @param sbase
	 * @return
	 */
	private static MutableTreeNode createNodes(TreeNode sbase) {
		SBMLNode node = null;
		if ((sbase instanceof SBase)) {
			node = new SBMLNode((SBase) sbase);
			MutableTreeNode child;
			for (int i = 0; i < sbase.getChildCount(); i++) {
				child = createNodes(sbase.getChildAt(i));
				if (child != null) {
					node.add(child);
				}
			}
		} 
		return node;
	}
	
	public void addPopupMenuItem(JMenuItem item, Class<? extends SBase>... types) {
		Class<? extends SBase> type = types[0];
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(null);
		if ((types == null) || (types.length == 0)
				|| type.isAssignableFrom(treeNode.getUserObject().getClass())) {
			
		}
	}
	
	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 */
	public static class SBMLNode extends DefaultMutableTreeNode {

		/**
		 * Generated serial version identifier.
		 */
		private static final long serialVersionUID = 9057010975355065921L;
		
		
		public SBMLNode(SBase sbase) {
			super(sbase);
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.tree.DefaultMutableTreeNode#getUserObject()
		 */
		@Override
		public TreeNodeWithChangeSupport getUserObject() {
		  return (TreeNodeWithChangeSupport) super.getUserObject();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (popup != null) {
			popup.setVisible(false);
		}
		for (ActionListener al : setOfActionListeners) {
			e.setSource(currSBase);
			al.actionPerformed(e);
		}
	}
	
	/**
	 * @param al
	 */
	public void addActionListener(ActionListener al) {
		setOfActionListeners.add(al);
	}
	
	/**
	 * Initializes this object.
	 */
	private void init() {
		setOfActionListeners = new HashSet<ActionListener>();
		// popup = new JPopupMenu();
		addMouseListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if ((popup != null) && popup.isVisible()) {
			currSBase = null;
			popup.setVisible(false);
		}
		Object clickedOn = getClosestPathForLocation(e.getX(), e.getY())
				.getLastPathComponent();
		if (clickedOn instanceof ASTNode) {
			ASTNode ast = (ASTNode) clickedOn;
			System.out.println(ast.getType());
		} else if ((e.getClickCount() == 2)
				|| (e.getButton() == MouseEvent.BUTTON3)
				&& setOfActionListeners.size() > 0) {
			if (clickedOn instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) getSelectionPath()
						.getLastPathComponent();
				Object userObject = node.getUserObject();
				if ((userObject instanceof Reaction) || (userObject instanceof Model)
						|| (userObject instanceof SBMLDocument)) {
					if (userObject instanceof SBMLDocument) {
						currSBase = ((SBMLDocument) userObject).getModel();
					} else {
						currSBase = (SBase) userObject;
					}
					if (popup != null) {
						popup.setLocation(e.getX() + ((int) getLocationOnScreen().getX()),
							e.getY() + ((int) getLocationOnScreen().getY()));// e.getLocationOnScreen());
						popup.setVisible(true);
					}
				}
//				if (((DefaultMutableTreeNode) clickedOn).getUserObject() instanceof MathContainer) {
//					MathContainer mc = (MathContainer) ((DefaultMutableTreeNode) clickedOn)
//							.getUserObject();
//					JDialog dialog = new JDialog();
//					JScrollPane scroll = new JScrollPane(new SBMLTree(mc.getMath()),
//						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//					dialog.getContentPane().add(scroll);
//					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//					dialog.pack();
//					dialog.setModal(true);
//					dialog.setLocationRelativeTo(null);
//					dialog.setVisible(true);
//				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}
	
	
}
