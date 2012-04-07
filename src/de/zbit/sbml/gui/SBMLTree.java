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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

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
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.filters.Filter;

import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.gui.ProgressBarSwing;

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
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(SBMLTree.class.getName());
	
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
		if (count1 <= count2) {
			return false;
		}
		while(count1 != count2) {
			path1 = path1.getParentPath();
			count1--;
		}
		return path1.equals(path2);
	}
	
	/**
	 * 
	 */
	private SBase currSBase;
	
	/**
	 * 
	 */
	private List<ActionListener> listOfActionListeners;
	
	/**
	 * 
	 */
	private JPopupMenu popup;
	
	/**
	 * 
	 */
	private Map<String, List<Class<? extends SBase>>> popUpMap;
	
	/**
	 * 
	 */
	private TreeNode[] savedState;
	
	/**
	 * @param savedState the savedState to set
	 */
	public void setSavedState(TreeNode[] savedState) {
		this.savedState = savedState;
	}

	/**
	 * @return the savedState
	 */
	public TreeNode[] getSavedState() {
		return savedState;
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
	 */
	public SBMLTree(SBase sbase) {
		super(createNodes(sbase));
		init();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (popup != null) {
			popup.setVisible(false);
		}
		e.setSource(currSBase);
		for (ActionListener listener : listOfActionListeners) {
			listener.actionPerformed(e);
		}
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void addActionListener(ActionListener listener) {
		listOfActionListeners.add(listener);
	}
	
	/**
	 * 
	 * @param item
	 * @param types
	 */
	public void addPopupMenuItem(JMenuItem item, Class<? extends SBase>... types) {
		if ((item == null) || (item.getName() == null) || (item.getActionCommand() == null)) {
			throw new NullPointerException("The given JMenuItem must not be null and its name or actionCommand must not be undefined!");
		}
		String key = item.getName() == null ? item.getActionCommand() : item.getName();
		if (popup == null) {
			popup = new JPopupMenu();
		}
		if (popUpMap.containsKey(key)) {
			List<Class<? extends SBase>> list = popUpMap.get(item);
			list.addAll(Arrays.asList(types));
			popUpMap.put(key, list);
		} else {
			popUpMap.put(key, Arrays.asList(types));
			popup.add(item);
		}
	}
	
	/**
	 * 
	 * @param expand
	 */
	public void expandAll(boolean expand) {
		expandAll(null, expand, null);
	}

	/**
	 * 
	 * @param nodesOfInterest
	 * @param expand
	 */
	public void expandAll(List<TreeNode> nodesOfInterest, boolean expand) {
		expandAll(nodesOfInterest, expand, null);
	}
	
	/**
	 * 
	 * @param nodesOfInterest
	 * @param expand
	 * @param progressBar
	 */
	public void expandAll(List<TreeNode> nodesOfInterest, boolean expand, AbstractProgressBar progressBar) {
		expandAll(nodesOfInterest, expand, progressBar, true, true, null);
	}
	
	/**
	 * 
	 * @param nodesOfInterest
	 * @param expand
	 * @param progressBar
	 * @param markHits
	 * @param setUninterestingNodesInvisible
	 */
	public void expandAll(List<TreeNode> nodesOfInterest, boolean expand,
		AbstractProgressBar progressBar, boolean markHits,
		boolean setUninterestingNodesInvisible, List<TreeNode> selectedNodes) {
		if (getModel().getRoot() != null) {
			TreePath path = expandAll(nodesOfInterest, expand, new TreePath(
				((TreeNode) this.getModel().getRoot())), progressBar, markHits,
				setUninterestingNodesInvisible);
			if (expand) {
				expandPath(path);
			} else {
				collapsePath(path);
			}
		}
		if ((selectedNodes != null) && (selectedNodes.size() > 0)) {
			setSelectionPath(new TreePath(toTreePath((TreeNode) getModel().getRoot(),
				selectedNodes).toArray(new TreeNode[] {})));
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param selectedNodes
	 * @return
	 */
	public List<TreeNode> toTreePath(TreeNode node, List<TreeNode> selectedNodes) {
		List<TreeNode> list = new LinkedList<TreeNode>();
		boolean found = (selectedNodes == null) || (selectedNodes.size() == 0);
		if (!found && (node instanceof SBMLNode)) {
			SBMLNode sbmlNode = (SBMLNode) node;
			SBase sbase = ((SBase) sbmlNode.getUserObject());
			if (selectedNodes.remove(sbase)) {
				found = true;
				list.add(sbmlNode);
			}
		}
		if (found && !selectedNodes.isEmpty() && !node.isLeaf()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				list.addAll(toTreePath(node.getChildAt(i), selectedNodes));
			}
		}
		return list;
	}

	/**
	 * 
	 * @param nodesOfInterest
	 * @param expand
	 * @param parent
	 * @param progressBar
	 * @param markHits
	 * @param setUninterestingNodesInvisible
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private TreePath expandAll(List<TreeNode> nodesOfInterest, boolean expand,
		TreePath parent, AbstractProgressBar progressBar, boolean markHits,
		boolean setUninterestingNodesInvisible) {
		if (progressBar != null) {
			progressBar.DisplayBar();
		}
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		boolean found = nodesOfInterest == null;
		if (!found && (node instanceof SBMLNode)) {
			SBMLNode sbmlNode = (SBMLNode) node;
			SBase sbase = ((SBase) sbmlNode.getUserObject());
			found = nodesOfInterest.remove(sbase);
			if (markHits) {
				sbmlNode.setBoldFont(found);
			}
		}
		Enumeration<TreeNode> e = node.children();
		Queue<TreeNode> nodeQueue = new LinkedList<TreeNode>();
		while (e.hasMoreElements()) {
			TreeNode child = e.nextElement();
			expandAll(nodesOfInterest, expand, parent.pathByAddingChild(child),
				progressBar, markHits, setUninterestingNodesInvisible);
			if (child instanceof SBMLNode) {
				SBMLNode n = (SBMLNode) child;
				if (n.isVisible()) {
					found = true;
					nodeQueue.add(child);
				} else if (!setUninterestingNodesInvisible) {
					n.setVisible(true);
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
			for (int i = 0; i < path.length; i++) {
				((SBMLNode) path[i]).setVisible(true);
				TreePath treePath = new TreePath(((DefaultTreeModel) this.getModel()).getPathToRoot(path[i]));
				this.expandPath(treePath);
				this.setSelectionPath(treePath);
				this.scrollPathToVisible(treePath);
			}
		}
	}
	
	/**
	 * 
	 * @param nodesOfInterest
	 * @param expand
	 * @param selected
	 * @param bold
	 * @param hideOthers
	 */
	public void properties(List<TreeNode> nodesOfInterest, boolean expand, boolean selected, boolean bold, boolean hideOthers) {
		List<TreePath> paths = new ArrayList<TreePath>();
		
		setAllVisible();

		SBMLNode.setShowInvisible(true);
		properties((SBMLNode) getModel().getRoot(), nodesOfInterest, paths, expand, selected, bold, hideOthers);
		SBMLNode.setShowInvisible(false);
		
		if (selected && paths.size() > 0) {
			setSelectionPaths((TreePath[]) paths.toArray());
			scrollPathToVisible(paths.get(0));
		}
		
		//validate();
		updateUI();
		//reload();
	}
	
	/**
	 * 
	 * @param currentNode
	 * @param nodesOfInterest
	 * @param paths
	 * @param expand
	 * @param selected
	 * @param bold
	 * @param hideOthers
	 */
	private void properties(SBMLNode currentNode, List<TreeNode> nodesOfInterest, List<TreePath> paths, 
			boolean expand, boolean selected, boolean bold, boolean hideOthers) {
		
		SBase currentSbase = ((SBase) currentNode.getUserObject());
		
		currentNode.setVisible(!hideOthers);
		currentNode.setBoldFont(false);
		
		for (TreeNode node : nodesOfInterest) {
			boolean isChild = false;
			boolean isCurrentNode = false;
			
			if (node instanceof SBase) {
				SBase sbase = (SBase) node;
				
				isChild = currentNode.containsUserObject(sbase);
				isCurrentNode = currentSbase.equals(sbase);
			}
			if (node instanceof SBMLNode) {
				SBMLNode sbmlNode = (SBMLNode) node;
				
				isChild = currentNode.isNodeChild(sbmlNode);
				isCurrentNode = currentNode.equals(sbmlNode);
			}
			
			currentNode.setVisible(currentNode.isVisible() || !(!isChild && !isCurrentNode && hideOthers));
			
			currentNode.setBoldFont(currentNode.isBoldFont() || (isCurrentNode && bold));
			
			if (isCurrentNode) {
				SBMLNode.setShowInvisible(true);
				if (expand || selected) {
					expandNode(currentNode);
				}
				if (selected) {
					paths.add(new TreePath(((DefaultTreeModel)this.getModel()).getPathToRoot(currentNode)));
				}
				SBMLNode.setShowInvisible(true);
			}
		}
		
		for (int i = 0; i < currentNode.getChildCount(); i++) {
			properties((SBMLNode) currentNode.getChildAt(i), nodesOfInterest, paths, expand, selected, bold, hideOthers);
		}
	}
	
	/**
	 * Initializes this object.
	 */
	private void init() {
		listOfActionListeners = new LinkedList<ActionListener>();
		popUpMap = new HashMap<String, List<Class<? extends SBase>>>();
		setCellRenderer(new SBMLTreeCellRenderer());
		addMouseListener(EventHandler.create(MouseListener.class, this, "processMouseClick", "", "mouseClicked"));
	}
	
	/**
	 * 
	 * @param e
	 */
	public void processMouseClick(MouseEvent e) {
		if (e.getClickCount() < 1) {
			return;
		}
		Object clickedOn = getClosestPathForLocation(e.getX(), e.getY()).getLastPathComponent();
		
		if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON1)) {
			if ((popup != null) && popup.isVisible()) {
				popup.setVisible(false);
			} else if (clickedOn instanceof DefaultMutableTreeNode) {
					saveSelectionPath();
			}
			if (clickedOn instanceof ASTNode) {
				ASTNode ast = (ASTNode) clickedOn;
				logger.fine(UnitDefinition.printUnits(ast.deriveUnit(), true) + " : " + ast.toFormula());
			}
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
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
					int enabledCount = 0;
					if (popUpMap.size() > 0) {
						for (int i = 0; i < popup.getComponentCount(); i++) {
							Component item = popup.getComponent(i);
							String key = item.getName();
							if ((key == null) && (item instanceof JMenuItem)) {
								key = ((JMenuItem) item).getActionCommand();
							}
							List<Class<? extends SBase>> classes = popUpMap.get(key);
							boolean enabled = false;
							for (int j = 0; (j < classes.size()) && !enabled; j++) {
								if (classes.get(j).isInstance(userObject)) {
									enabled = true;
									enabledCount++;
								}
							}
							popup.getComponent(i).setEnabled(enabled);
						}
					}
					if (0 < enabledCount) {
						popup.setLocation(e.getX() + ((int) getLocationOnScreen().getX()),
							e.getY() + ((int) getLocationOnScreen().getY()));// e.getLocationOnScreen());
						popup.setVisible(true);
					}
				}
				if (((DefaultMutableTreeNode) clickedOn).getUserObject() instanceof MathContainer) {
					MathContainer mc = (MathContainer) ((DefaultMutableTreeNode) clickedOn).getUserObject();
					JDialog dialog = new JDialog();
					JScrollPane scroll = new JScrollPane(new SBMLTree(mc.getMath()));
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
	
	/**
	 * reload the tree model
	 */
	public void reload() {
		((DefaultTreeModel)this.getModel()).reload();
	}
	
	/**
	 * restore selection path
	 */
	public void restoreSelectionPath() {
		expandPath(savedState);
	}
	
	/**
	 * save current selection Path
	 */
	public void saveSelectionPath() {
		if (this.getSelectionPath() != null) {
			savedState = ((DefaultTreeModel) this.getModel()).getPathToRoot((TreeNode) this.getSelectionPath().getLastPathComponent());
		}
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
    
}
