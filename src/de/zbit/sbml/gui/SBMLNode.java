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
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;
import org.sbml.jsbml.util.compilers.UnitException;
import org.sbml.jsbml.util.compilers.UnitsCompiler;

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
	 * A {@link Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(SBMLNode.class.getName());
	
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
		this(sbase, isVisible, SBase.class);
	}
	
	/**
	 * 
	 * @param ast
	 */
	public SBMLNode(ASTNode ast) {
		this(ast, true, ASTNode.class);
	}
	
	/**
	 * 
	 * @param node
	 * @param isVisible
	 * @param accepted
	 */
	private SBMLNode(TreeNode node, boolean isVisible, Class<? extends TreeNode> accepted) {
		super(node);
		this.boldFont = false;
		this.expanded = false;
		this.isVisible = isVisible;
		nodeCount++;
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			if (accepted.isAssignableFrom(child.getClass())) {
				add(new SBMLNode(child, isVisible, accepted));
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString() {
		TreeNodeWithChangeSupport node = getUserObject();
		if (node instanceof SBase) {
			return node.toString();
		} else if (node instanceof ASTNode) {
			StringBuilder sb = new StringBuilder();
			ASTNode ast = (ASTNode) node;
			int level = -1, version = -1;
			MathContainer mc = ast.getParentSBMLObject();
			if (mc != null) {
				level = mc.getLevel();
				version = mc.getVersion();
			}
			if (ast.isName()) {
				sb.append(ast.getName());
			} else if (ast.isNumber()) {
				sb.append(ast.isInteger() ? Integer.toString(ast.getInteger()) : Double.toString(ast.getReal()));
			} else {
				sb.append(ast.getType());
			}
			sb.append(' ');
			try {
				sb.append(UnitDefinition.printUnits(ast.compile(new UnitsCompiler(level, version)).getUnits(), true));
			} catch (UnitException exc) {
				sb.append("invalid");
				logger.fine(exc.getMessage());
			}
			return sb.toString();
		}
		return super.toString();
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
	@Override
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
	@Override
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
	 * @param obj
	 * @return
	 */
	public boolean containsUserObject(TreeNodeWithChangeSupport obj) {
		if (getUserObject().equals(obj)){
			return true;
		} else if (isLeaf()) {
			return false;
		} else {
			boolean result = false;
			for (int i = 1; i < getChildCount(); i++) {
				result = result || ((SBMLNode) getChildAt(i)).containsUserObject(obj);
			}
			return result;
		}
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
