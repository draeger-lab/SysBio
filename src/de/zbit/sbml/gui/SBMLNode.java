/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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

import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.jsbml.util.TreeNodeRemovedEvent;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;
import org.sbml.jsbml.util.compilers.UnitException;

import de.zbit.util.ResourceManager;

/**
 * A specialized {@link JTree} that shows the elements of a JSBML model as a
 * hierarchical structure.
 * 
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class SBMLNode extends DefaultMutableTreeNode implements TreeNodeChangeListener {
  
  /**
   * Localization for SBML element names.
   */
  private static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.sbml.locales.ElementNames");
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(SBMLNode.class.getName());
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 9057010975355065921L;
  
  /**
   * true: show invisible nodes
   */
  private static boolean showInvisible = false;
  
  /**
   * Helper method, necessary because the tree might hide some nodes... Index
   * determination of child elements is therefore a bit more complicated.
   * 
   * @param parent
   * @param child
   * @param acceptedType
   * @return
   */
  private static int indexOf(TreeNode parent, TreeNode child, Class<? extends TreeNode> acceptedType) {
    if (child == null) {
      throw new IllegalArgumentException("Argument is null.");
    }
    // linear search
    @SuppressWarnings("unchecked")
    Enumeration<TreeNode> e = parent.children();
    int index = 0;
    while (e.hasMoreElements()) {
      TreeNode elem = e.nextElement();
      if ((child == elem) || child.equals(elem)) {
        return index;
      }
      if (acceptedType.isAssignableFrom(elem.getClass())) {
        index++;
      }
    }
    // not found => node is not a child.
    return -1;
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
  
  
  private Class<? extends TreeNode> acceptedType;
  
  /**
   * 
   */
  private boolean boldFont, expanded, isVisible;
  
  /**
   * Memorizes the result of the {@link #toString()} method.
   */
  private String stringRepresentation;
  
  /**
   * 
   * @param ast
   */
  public SBMLNode(ASTNode ast) {
    this(ast, true, ASTNode.class);
  }
  
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
   * @param node
   * @param isVisible
   * @param accepted
   */
  private SBMLNode(TreeNode node, boolean isVisible, Class<? extends TreeNode> accepted) {
    super(node);
    acceptedType = accepted;
    boldFont = false;
    expanded = false;
    this.isVisible = isVisible;
    
    if (node == null) {
      return;
    }
    for (int i = 0; i < node.getChildCount(); i++) {
      TreeNode child = node.getChildAt(i);
      if (acceptedType.isAssignableFrom(child.getClass())) {
        SBMLNode newChild = new SBMLNode(child, isVisible, acceptedType);
        add(newChild);
      }
    }
    stringRepresentation = null;
    if (node instanceof TreeNodeWithChangeSupport) {
      TreeNodeWithChangeSupport n = (TreeNodeWithChangeSupport) node;
      List<TreeNodeChangeListener> listOfListeners = n.getListOfTreeNodeChangeListeners();
      if (!listOfListeners.contains(this)) {
        n.addTreeNodeChangeListener(this, false);
      }
    }
  }
  
  /**
   * 
   */
  public void collapse() {
    expanded = false;
  }
  
  /**
   * 
   * @param obj
   * @return
   */
  public boolean containsUserObject(TreeNodeWithChangeSupport obj) {
    if (getUserObject().equals(obj)) {
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
   */
  public void expand() {
    expanded = true;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.tree.DefaultMutableTreeNode#getChildAt(int)
   */
  @Override
  public TreeNode getChildAt(int index) {
    if (isShowInvisible()) {
      return super.getChildAt(index);
    }
    if (children == null) {
      throw new ArrayIndexOutOfBoundsException("node has no children");
    }
    
    int realIndex = -1;
    int visibleIndex = -1;
    Enumeration<?> e = children.elements();
    while (e.hasMoreElements()) {
      SBMLNode node = (SBMLNode) e.nextElement();
      if (node.isVisible()) {
        visibleIndex++;
      }
      realIndex++;
      if (visibleIndex == index) {
        return (TreeNode) children.elementAt(realIndex);
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
    if (children == null) {
      return 0;
    }
    int count = 0;
    Enumeration<?> e = children.elements();
    while (e.hasMoreElements()) {
      SBMLNode node = (SBMLNode) e.nextElement();
      if (node.isVisible()) {
        count++;
      }
    }
    return count;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.tree.DefaultMutableTreeNode#getUserObject()
   */
  @Override
  public TreeNodeWithChangeSupport getUserObject() {
    return (TreeNodeWithChangeSupport) super.getUserObject();
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
   * @return
   */
  public boolean isExpanded() {
    return expanded;
  }
  
  /**
   * 
   * @return
   */
  public boolean isVisible() {
    return isVisible;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
   */
  //@Override
  @Override
  public void nodeAdded(TreeNode node) {
    stringRepresentation = null;
    stringRepresentation = toString();
    
    // Add the new node to this tree
    TreeNode parent = node.getParent();
    if (parent == getUserObject()) {
      boolean hasChild = false;
      // Check if the node is already in the tree
      for (int i = 0; !hasChild && (i < getChildCount()); i++) {
        hasChild |= ((SBMLNode) getChildAt(i)).getUserObject() == node;
      }
      if (!hasChild) {
        if (node instanceof TreeNodeWithChangeSupport) {
          TreeNodeWithChangeSupport n = (TreeNodeWithChangeSupport) node;
          if (!n.isRoot() && n.getListOfTreeNodeChangeListeners().contains(this)) {
            logger.finer(MessageFormat.format("Removing parent node {0} from list of listeners in {1}.", this, n));
            n.removeTreeNodeChangeListener(this);
          }
        }
        SBMLNode newChild = new SBMLNode(node, isVisible(), acceptedType);
        add(newChild);
        // Correct index
        /* The problem is that in SBML empty lists are usually not considered
         * as children! So we cannot determine the correct index of a child
         * that is an empty list in the SBML tree structure!
         */
        if ((parent instanceof ListOf<?>) && !isRoot() && (getParent().getParent() != null) && (parent.getParent() != null)) {
          SBMLNode parentNode = (SBMLNode) getParent();
          TreeNode root = parent.getParent();
          int childIndex = indexOf(root, parent, acceptedType);
          int currIndex = parentNode.getIndex(this);
          if (childIndex != currIndex) {
            removeFromParent();
            parentNode.insert(this, childIndex);
          }
        }
        logger.finer("adding " + node);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(org.sbml.jsbml.util.TreeNodeRemovedEvent)
   */
  //@Override
  @Override
  public void nodeRemoved(TreeNodeRemovedEvent evt) {
    stringRepresentation = null;
    stringRepresentation = toString();
    
    SBMLNode parentNode = (SBMLNode) getParent();
    TreeNode parent = evt.getPreviousParent();
    if ((parent == parentNode.getUserObject()) && (evt.getSource() == getUserObject())) {
      parentNode.remove(this);
      logger.finer("removing " + evt.getSource());
    }
  }
  
  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  //@Override
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    stringRepresentation = null;
    stringRepresentation = toString();
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
   * @param visible
   */
  public void setVisible(boolean visible) {
    isVisible = visible;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.tree.DefaultMutableTreeNode#toString()
   */
  @Override
  public String toString() {
    TreeNodeWithChangeSupport node = getUserObject();
    if (node instanceof SBase) {
      if (node instanceof Unit) {
        Unit u = (Unit) node;
        if (u.isSetKind()) {
          return u.toString();
        }
      }
      if (node instanceof NamedSBase) {
        NamedSBase nsb = (NamedSBase) node;
        if (nsb instanceof UnitDefinition) {
          UnitDefinition ud = (UnitDefinition) nsb;
          if (ud.isSetName() && bundle.containsKey(ud.getName())) {
            return bundle.getString(ud.getName());
          } else if (ud.getUnitCount() > 0) {
            return ud.toString();
          }
        } else if (nsb.isSetName()) {
          return nsb.getName();
        } else if (nsb instanceof SimpleSpeciesReference) {
          SimpleSpeciesReference specRef = (SimpleSpeciesReference) node;
          if (specRef.isSetSpeciesInstance()) {
            Species s = specRef.getSpeciesInstance();
            return s.toString();
          } else if (specRef.isSetSpecies()) {
            return specRef.getSpecies();
          }
          return specRef.toString();
        }
        if (nsb.isSetId()) {
          return nsb.getId();
        }
      }
      String elementName = ((SBase) node).getElementName();
      if (bundle.containsKey(elementName)) {
        return bundle.getString(elementName);
      }
      return node.toString();
    } else if (node instanceof ASTNode) {
      if (stringRepresentation == null) {
        StringBuilder sb = new StringBuilder();
        ASTNode ast = (ASTNode) node;
        if (ast.isName()) {
          sb.append(ast.getName());
        } else if (ast.isNumber()) {
          sb.append(ast.isInteger() ? Integer.toString(ast.getInteger()) : Double.toString(ast.getReal()));
        } else {
          sb.append(ast.getType());
        }
        sb.append(' ');
        try {
          sb.append(UnitDefinition.printUnits(ast.deriveUnit(), true));
        } catch (UnitException exc) {
          sb.append("invalid");
          logger.fine(exc.getMessage());
        }
        stringRepresentation = sb.toString();
      }
      return stringRepresentation;
    }
    return super.toString();
  }
  
}
