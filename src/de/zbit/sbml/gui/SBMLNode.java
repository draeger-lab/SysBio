/*
 * $Id: SBMLNode.java 739 2012-01-25 21:07:44Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/gui/SBMLNode.java $
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
	public static boolean showInvisible = false;
	
	public static int nodeCount = 0;
	
	private boolean boldFont = false;
	private boolean isVisible;
	private boolean expanded = false;

	/**
	 * 
	 * @param sbase
	 */
	public SBMLNode(SBase sbase) {
		this(sbase,true);
	}
	
	/**
	 * 
	 * @param sbase
	 * @param isVisible
	 */
	public SBMLNode(SBase sbase, boolean isVisible) {
	    super(sbase);
	    this.isVisible = isVisible;
	    nodeCount++;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#getUserObject()
	 */
	@Override
	public TreeNodeWithChangeSupport getUserObject() {
	  return (TreeNodeWithChangeSupport) super.getUserObject();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#getChildAt(int)
	 */
	public TreeNode getChildAt(int index) {
		if (showInvisible) {
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

	public int getChildCount() {
		if (showInvisible) {
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
	
	public void setBoldFont(boolean boldFont) {
		this.boldFont = boldFont;
	}

	public boolean isBoldFont() {
		return boldFont;
	}

	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}

	public boolean isVisible() {
		return isVisible;
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	
	public void expand() {
		this.expanded = true;
	}
	
	public void collapse() {
		this.expanded = false;
	}
}