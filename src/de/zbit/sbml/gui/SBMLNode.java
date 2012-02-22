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

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;

/**
 * A specialized {@link JTree} that shows the elements of a JSBML model as a
 * hierarchical structure.
 * 
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class SBMLNode extends DefaultMutableTreeNode {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 9057010975355065921L;
	
	/**
	 * true: show invisible nodes
	 */
	private static boolean showInvisible = false;
	
	/**
	 * 
	 */
	private static int nodeCount = 0;
	
	/**
	 * 
	 */
	private boolean boldFont, expanded, isVisible;

	/**
	 * 
	 * @param sbase
	 */
	public SBMLNode(SBase sbase) {
		this(sbase, true);
	}
	
	/**
	 * 
	 * @param sbase
	 * @param isVisible
	 */
	public SBMLNode(SBase sbase, boolean isVisible) {
	    super(sbase);
	    this.boldFont = false;
	    this.expanded = false;
	    this.isVisible = isVisible;
	    nodeCount++;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#getUserObject()
	 */
	@Override
	public TreeNodeWithChangeSupport getUserObject() {
	  return (TreeNodeWithChangeSupport) super.getUserObject();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#getChildAt(int)
	 */
	public TreeNode getChildAt(int index) {
		if (isShowInvisible()) {
			return super.getChildAt(index);
		}
		if (this.children == null) {
			throw new ArrayIndexOutOfBoundsException("node has no children");
		}
		
		int realIndex = -1;
		int visibleIndex = -1;
		Enumeration<?> e = this.children.elements();
		while (e.hasMoreElements()) {
			SBMLNode node = (SBMLNode) e.nextElement();
			if (node.isVisible()) {
				visibleIndex++;
			}
			realIndex++;
			if (visibleIndex == index) {
				return (TreeNode) this.children.elementAt(realIndex);
			}
		}
		
		throw new ArrayIndexOutOfBoundsException("index unmatched");
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#getChildCount()
	 */
	public int getChildCount() {
		if (isShowInvisible()) {
			return super.getChildCount();
		}
		if (this.children == null) {
			return 0;
		}
		int count = 0;
		Enumeration<?> e = this.children.elements();
		while (e.hasMoreElements()) {
			SBMLNode node = (SBMLNode) e.nextElement();
			if (node.isVisible()) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @param boldFont
	 */
	public void setBoldFont(boolean boldFont) {
		this.boldFont = boldFont;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isBoldFont() {
		return boldFont;
	}

	/**
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return isVisible;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isExpanded() {
		return expanded;
	}
	
	/**
	 * 
	 */
	public void expand() {
		this.expanded = true;
	}
	
	/**
	 * 
	 */
	public void collapse() {
		this.expanded = false;
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isShowInvisible() {
		return showInvisible;
	}

	/**
	 * 
	 * @param showInvisible
	 */
	public static void setShowInvisible(boolean showInvisible) {
		SBMLNode.showInvisible = showInvisible;
	}

	/**
	 * 
	 * @return
	 */
	public static int getNodeCount() {
		// TODO: This will return the number of nodes FOR ALL SBMLNode instances!!!
		return nodeCount;
	}
}