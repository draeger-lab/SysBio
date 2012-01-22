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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;
import org.sbml.jsbml.util.filters.Filter;

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
	private String savedState;
	
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
	 * @param sbase
	 * @param filter
	 */
	public SBMLTree(SBase sbase, Filter filter) {
		super(searchTree(sbase, filter, true));
		this.setCellRenderer(new SBMLTreeCellRenderer());
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
			if (sbase instanceof SimpleSpeciesReference){
				SimpleSpeciesReference specRef = (SimpleSpeciesReference) sbase;
				specRef.setName(specRef.getSpeciesInstance().getName());
			}
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
	 * @param sbase
	 * @param filter
	 * @param showChildren
	 * @return
	 */
	private static MutableTreeNode searchTree(TreeNode sbase, Filter filter, boolean showChildren) {
		SBMLNode node = null;
		if ((sbase instanceof SBase)) {
			List<TreeNode> list = ((SBase) sbase).filter(filter);
			if (list.size() > 0) {
				if (sbase instanceof SimpleSpeciesReference){
					SimpleSpeciesReference specRef = (SimpleSpeciesReference) sbase;
					specRef.setName(specRef.getSpeciesInstance().getName());
				}
				node = new SBMLNode((SBase) sbase);
				MutableTreeNode child = null;
				for (int i = 0; i < sbase.getChildCount(); i++) {
					child = searchTree(sbase.getChildAt(i), filter, showChildren);
					if (child != null) {
						node.add(child);
					}
				}
				if (node.getChildCount() == 0) {
					for (int j=0; j<list.size(); j++){
						if (showChildren) {
							child = createNodes((SBase) list.get(j));
						} else {
							child = new SBMLNode((SBase) list.get(j));
						}
						if (child != null) {
							((SBMLNode) child).boldFont = true;
							node.add(child);
						}
					}
				}
			}
		}
		return node;
	}
	
	public void expandAll(boolean expand){
		if (this.getModel().getRoot() != null){
			this.expandAll(null, expand);
		}
	}
	
	private void expandAll(TreePath parent, boolean expand) {
		if (parent == null){
			parent = new TreePath(((TreeNode) this.getModel().getRoot()));
		}
	    TreeNode node = (TreeNode) parent.getLastPathComponent();
	    if (node.getChildCount() >= 0) {
	        for (Enumeration e=node.children(); e.hasMoreElements(); ) {
	            TreeNode n = (TreeNode)e.nextElement();
	            TreePath path = parent.pathByAddingChild(n);
	            expandAll(path, expand);
	        }
	    }

	    if (expand) {
	        this.expandPath(parent);
	    } else {
	        this.collapsePath(parent);
	    }
	}
	
	public void addPopupMenuItem(JMenuItem item, Class<? extends SBase>... types) {
		popUpMap.put(item, Arrays.asList(types));
		popup.add(item);
	}
	
	// is path1 descendant of path2
    public static boolean isDescendant(TreePath path1, TreePath path2){
        int count1 = path1.getPathCount();
        int count2 = path2.getPathCount();
        if(count1<=count2)
            return false;
        while(count1!=count2){
            path1 = path1.getParentPath();
            count1--;
        }
        return path1.equals(path2);
    }
 
    public void saveExpansionState(){
    	if (savedState == null) {
	    	int row = 0;
	        TreePath rowPath = this.getPathForRow(row);
	        StringBuffer buf = new StringBuffer();
	        int rowCount = this.getRowCount();
	        for(int i=row; i<rowCount; i++){
	            TreePath path = this.getPathForRow(i);
	            if(i==row || isDescendant(path, rowPath)){
	                if(this.isExpanded(path))
	                    buf.append(","+String.valueOf(i-row));
	            } else {
	            	break;
	            }
	        }
	        savedState = buf.toString();
    	}
    }
    
    public void restoreExpanstionState(){
    	if (savedState != null) {
	    	int row = 0;
	        StringTokenizer stok = new StringTokenizer(savedState, ",");
	        while(stok.hasMoreTokens()){
	            int token = row + Integer.parseInt(stok.nextToken());
	            this.expandRow(token);
	        }
	        savedState = null;
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
		
		public boolean boldFont = false;

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
		addMouseListener(EventHandler.create(MouseListener.class, this, "mouseClicked", ""));
	}
	
	/**
	 * 
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON1)
				&& (popup != null) && popup.isVisible()) {
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
				if (userObject instanceof SBMLDocument) {
					currSBase = ((SBMLDocument) userObject).getModel();
				} else {
					currSBase = (SBase) userObject;
				}
				if (popup != null) {
					if (popUpMap != null && popUpMap.size() > 0) {
						for (int i=0; i<popup.getComponentCount(); i++){
							List<Class<? extends SBase>> classes = popUpMap.get(popup.getComponent(i));
							boolean enabled = false;
							for (int j=0; j<classes.size(); j++){
								if (classes.get(j).isInstance(userObject)){
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
