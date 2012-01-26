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
import java.beans.EventHandler;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.filters.Filter;

import de.zbit.gui.ProgressBarSwing;
import de.zbit.util.AbstractProgressBar;

/**
 * A specialized {@link JTree} that shows the elements of a JSBML model as a
 * hierarchical structure.
 * 
 * @author Simon Sch&auml;fer
 * @author Andreas Dr&auml;ger
 * @author Sebastian Nagel
 * @since 1.0
 * @version $Rev$
 */
public class SBMLTree extends JTree implements ActionListener {
	
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
	 * 
	 */
	private TreeNode[] savedState;
	
	/**
	 * 
	 */
	private Map<JMenuItem, List<Class<? extends SBase>>> popUpMap;
	
	/**
	 * @param sbase
	 */
	public SBMLTree(SBase sbase) {
		super(createNodes(sbase));
		init();
	}
	
	/**
	 * Generate a tree that displays an ASTNode tree.
	 * 
	 * @param math
	 */
	public SBMLTree(ASTNode math) {
		super(math);
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
	
	public void search(Filter filter) {
		search(filter, null);
	}
	
	/**
	 * 
	 * @param filter
	 * @param progressBar 
	 */
	public void search(Filter filter, AbstractProgressBar progressBar) {
		SBMLNode.setShowInvisible(true);
		SBMLNode root = (SBMLNode)this.getModel().getRoot();
		List<TreeNode> list = ((SBase)root.getUserObject()).filter(filter);
		if (progressBar != null) {
			progressBar.reset();
			progressBar.setNumberOfTotalCalls(2 * SBMLNode.getNodeCount());
			if (progressBar instanceof ProgressBarSwing){
				((ProgressBarSwing) progressBar).getProgressBar().setVisible(true);
			}
		}
		search(root, filter, list, progressBar);
		reload();
		SBMLNode.setShowInvisible(false);
	}
	
	/**
	 * 
	 * @param node
	 * @param filter
	 * @param list
	 * @param progressBar
	 * @param callNr
	 */
	private void search(SBMLNode node,Filter filter,List<TreeNode> list, final AbstractProgressBar progressBar) {
		SBase sbase = ((SBase)node.getUserObject());
		if (sbase.filter(filter).size() > 0) {
			node.setVisible(true);
			node.setBoldFont(false);
		} else if (list.contains(sbase)) {
			node.setBoldFont(true);
			node.setVisible(true);
		} else {
			node.setBoldFont(false);
			node.setVisible(false);
		}
		for (int i = 0; i < node.getChildCount(); ++i) {
			SBMLNode child = (SBMLNode) node.getChildAt(i);
			search(child, filter, list, progressBar);
		}
		if (progressBar != null) {
			progressBar.DisplayBar();
		}
	}
	
	public void expandAll(boolean expand) {
		expandAll(null, expand, null);
	}
	
	/**
	 * 
	 * @param expand
	 * @param progressBarSwing 
	 */
	public void expandAll(List<TreeNode> nodesOfInterest, boolean expand, AbstractProgressBar progressBar) {
		if (getModel().getRoot() != null) {
			TreePath path = expandAll(nodesOfInterest, expand, new TreePath(((TreeNode) this.getModel().getRoot())), progressBar);
			if (expand) {
				expandPath(path);
			} else {
				collapsePath(path);
			}
		}
	}

	/**
	 * 
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	private TreePath expandAll(List<TreeNode> nodesOfInterest, boolean expand, TreePath parent, AbstractProgressBar progressBar) {
		if (progressBar != null) {
			progressBar.DisplayBar();
		}
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		boolean found = nodesOfInterest == null;
		if ((!found) && (node instanceof SBMLNode)) {
			SBMLNode sbmlNode = (SBMLNode) node;
			SBase sbase = ((SBase) sbmlNode.getUserObject());
			found = nodesOfInterest.remove(sbase);
			sbmlNode.setBoldFont(found);
		}
		Enumeration<TreeNode> e = node.children();
		Queue<TreeNode> nodeQueue = new LinkedList<TreeNode>();
		while (e.hasMoreElements()) {
			TreeNode child = e.nextElement();
			expandAll(nodesOfInterest, expand, parent.pathByAddingChild(child), progressBar);
			if (child instanceof SBMLNode) {
				SBMLNode n = (SBMLNode) child;
				if (n.isVisible()) {
					found = true;
					nodeQueue.add(child);
				}
			}
		}
		if (node instanceof SBMLNode) { 
			SBMLNode sbmlNode = (SBMLNode) node;
			sbmlNode.setVisible(found);
		}
		while (!nodeQueue.isEmpty()) {
			if (expand) {
				expandPath(parent.pathByAddingChild(nodeQueue.poll()));
			} else {
				collapsePath(parent.pathByAddingChild(nodeQueue.poll()));
			}
		}
		return parent;
	}
	
	/**
	 * 
	 * @param item
	 * @param types
	 */
	public void addPopupMenuItem(JMenuItem item, Class<? extends SBase>... types) {
		if (popUpMap.containsKey(item)) {
			List<Class<? extends SBase>> list = popUpMap.get(item);
			list.addAll(Arrays.asList(types));
			popUpMap.put(item, list);
		} else {
			popUpMap.put(item, Arrays.asList(types));
		}
		// TODO: Why this? We don't know if this item is appropriate in the current situation! 
		popup.add(item);
	}
	
	/**
	 * 
	 */
	public void setAllVisible() {
		SBMLNode.setShowInvisible(true);
		setAllVisible((SBMLNode) this.getModel().getRoot());
		reload();
		SBMLNode.setShowInvisible(false);
	}
	
	/**
	 * 
	 * @param node
	 */
	private void setAllVisible(SBMLNode node) {
		node.setVisible(true);
		node.setBoldFont(false);
		for (int i = 0; i<node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			if (child instanceof SBMLNode) {
				setAllVisible((SBMLNode) child);
			}
		}
	}
	
	/**
	 * reload the tree model
	 */
	public void reload() {
		((DefaultTreeModel)this.getModel()).reload();
	}
	
	/**
	 * is path1 descendant of path2
	 * 
	 * @param path1
	 * @param path2
	 * @return
	 */
    public static boolean isDescendant(TreePath path1, TreePath path2) {
        int count1 = path1.getPathCount();
        int count2 = path2.getPathCount();
        if(count1<=count2)
            return false;
        while(count1!=count2) {
            path1 = path1.getParentPath();
            count1--;
        }
        return path1.equals(path2);
    }
 
    /**
     * save current selection Path
     */
    public void saveSelectionPath() {
    	if (this.getSelectionPath() != null) {
    		savedState = ((DefaultTreeModel)this.getModel()).getPathToRoot((TreeNode) this.getSelectionPath().getLastPathComponent());
    	}
    }
    
    /**
     * restore selection path
     */
    public void restoreSelectionPath() {
    	expandPath(savedState);
    }
    
    /**
     * expand a path from root to node
     * @param node
     */
    public void expandNode(TreeNode node) {
    	if (node != null) {
    		expandPath(((DefaultTreeModel)this.getModel()).getPathToRoot(node));
    	}
    }
    
    /**
     * expand a path
     * @param path
     */
    public void expandPath(TreeNode[] path) {
    	if (path != null) {
    		for (int i=0; i<path.length; i++) {
    			TreePath treePath = new TreePath(((DefaultTreeModel)this.getModel()).getPathToRoot(path[i]));
    			this.expandPath(treePath);
    			this.setSelectionPath(treePath);
    			this.scrollPathToVisible(treePath);
    		}
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
		e.setSource(currSBase);
		for (ActionListener al : setOfActionListeners) {
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
		popup = new JPopupMenu();
		popUpMap = new HashMap<JMenuItem, List<Class<? extends SBase>>>();
		this.setCellRenderer(new SBMLTreeCellRenderer());
		addMouseListener(EventHandler.create(MouseListener.class, this, "mouseClicked", ""));
	}
	
	/**
	 * 
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		Object clickedOn = getClosestPathForLocation(e.getX(), e.getY())
				.getLastPathComponent();
		
		if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON1)) {
			if ((popup != null) && popup.isVisible()) {
				currSBase = null;
				popup.setVisible(false);
			} else if (clickedOn instanceof DefaultMutableTreeNode) {
				this.saveSelectionPath();
			}
		}
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
				if (userObject instanceof SBMLDocument) {
					currSBase = ((SBMLDocument) userObject).getModel();
				} else {
					currSBase = (SBase) userObject;
				}
				if (popup != null) {
					if (popUpMap != null && popUpMap.size() > 0) {
						for (int i=0; i<popup.getComponentCount(); i++) {
							List<Class<? extends SBase>> classes = popUpMap.get(popup.getComponent(i));
							boolean enabled = false;
							for (int j=0; j<classes.size(); j++) {
								if (classes.get(j).isInstance(userObject)) {
									enabled = true;
									break;
								}
							}
							popup.getComponent(i).setEnabled(enabled);
						}
					}
					popup.setLocation(e.getX() + ((int) getLocationOnScreen().getX()),
							e.getY() + ((int) getLocationOnScreen().getY()));// e.getLocationOnScreen());
					popup.setVisible(true);
				}
				if (((DefaultMutableTreeNode) clickedOn).getUserObject() instanceof MathContainer) {
					MathContainer mc = (MathContainer) ((DefaultMutableTreeNode) clickedOn)
							.getUserObject();
					JDialog dialog = new JDialog();
					JScrollPane scroll = new JScrollPane(new SBMLTree(mc.getMath()),
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					dialog.getContentPane().add(scroll);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.pack();
					dialog.setModal(true);
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
				}
			}
		}
	}
	
	
}
