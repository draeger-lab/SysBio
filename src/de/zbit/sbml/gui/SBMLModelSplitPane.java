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

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;
import org.sbml.jsbml.util.filters.OrFilter;

import de.zbit.gui.GUITools;
import de.zbit.gui.LayoutHelper;
import de.zbit.sbml.util.RegexpAssignmentVariableFilter;
import de.zbit.sbml.util.RegexpNameFilter;
import de.zbit.sbml.util.RegexpSpeciesReferenceFilter;

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
		TreeSelectionListener {
	
	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private final Set<ActionListener> actionListeners;

	/**
	 * @return the actionListeners
	 */
	public Set<ActionListener> getActionListeners() {
		return actionListeners;
	}

	/**
	 * 
	 */
	private static final transient Logger logger = Logger.getLogger(SBMLModelSplitPane.class.getName());
	
	/**
	 * 
	 */
	protected SBMLTree tree;
	
	/**
	 * 
	 */
	SBMLTreeSwingWorker<Void, Void> worker;
	
	/**
	 * 
	 */
	protected SBMLDocument sbmlDoc;
	
	/**
	 * 
	 */
	protected JTextField searchField;
	
	/**
	 * @return the tree
	 */
	public SBMLTree getTree() {
		return tree;
	}

	/**
	 * @param tree the tree to set
	 */
	public void setTree(SBMLTree tree) {
		this.tree = tree;
	}

	/**
	 * 
	 */
	private boolean namesIfAvailalbe;
	
	/**
	 * @param model
	 * @throws SBMLException
	 * @throws IOException
	 */
	public SBMLModelSplitPane(SBMLDocument document, boolean namesIfAvailable) throws SBMLException,
		IOException {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		this.namesIfAvailalbe = namesIfAvailable;
		actionListeners = new HashSet<ActionListener>();
		//document.addChangeListener(this);
		init(document, false);
		initTree();
	}
	
	/**
	 * @param al
	 */
	public void addActionListener(ActionListener al) {
		tree.addActionListener(al);
		actionListeners.add(al);
	}
	
	/**
	 * @param nodeInfo
	 * @return
	 * @throws SBMLException
	 * @throws IOException
	 */
	protected JPanel createLeftComponent() throws SBMLException,
		IOException {
		JPanel leftPane = new JPanel();
		LayoutHelper lh = new LayoutHelper(leftPane);
		
	  	JScrollPane treePane = new JScrollPane(tree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  	
		lh.add(treePane, 0, 0, 2, 1, 1d, 1d);
	  	
		searchField = new JTextField();
		searchField.setEditable(true);
		searchField.setMaximumSize(new Dimension(searchField.getSize().width, 10));
		searchField.addKeyListener(
				new KeyListener(){
					/* (non-Javadoc)
					 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
					 */
					public void keyPressed(KeyEvent arg0) {
					}
					/* (non-Javadoc)
					 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
					 */
					public void keyReleased(KeyEvent arg0) {

					}
					/* (non-Javadoc)
					 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
					 */
					public void keyTyped(KeyEvent arg0) {
						if (worker!=null){
							if (!worker.isDone())
								worker.cancel(true);
							
							if (!worker.getSearchString().equals(searchField.getText())){
								worker = new SBMLTreeSwingWorker<Void, Void>(searchField.getText());
								worker.execute();
							}
						}
					}
				});
		JLabel label = new JLabel(UIManager.getIcon("ICON_SEARCH_16"));
		label.setOpaque(true);
		lh.add(label, 0, 2, 1, 1, 0d, 0d);
		lh.add(searchField, 1, 2, 1, 1, 0d, 0d);
		
		return leftPane;
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
		return new JScrollPane(new ASTNodePanel(node, namesIfAvailalbe));
	}
	
	/**
	 * @param sbase
	 * @return
	 * @throws SBMLException
	 * @throws IOException
	 */
	protected JScrollPane createRightComponent(SBase sbase) throws SBMLException,
		IOException {
		SBasePanel sbPanel = new SBasePanel(sbase, namesIfAvailalbe);
		JScrollPane scroll = new JScrollPane(sbPanel);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return scroll;
	}
	
	/**
	 * 
	 * @return
	 */
	public SBMLDocument getSBMLDocument() {
		return (SBMLDocument) tree.getModel().getRoot();
	}
	
	/**
	 * @param doc
	 * @param keepDivider
	 * @throws SBMLException
	 * @throws IOException
	 */
	public void init(SBMLDocument doc, boolean keepDivider) throws SBMLException,
		IOException {
		int proportionalLocation = getDividerLocation();
		
		sbmlDoc = doc.getSBMLDocument();
		tree = new SBMLTree(sbmlDoc);
		
		setLeftComponent(createLeftComponent());
		setRightComponent(createRightComponent(doc));
		if (keepDivider) {
			setDividerLocation(proportionalLocation);
		}
		validate();
	}
	
	protected void initTree() {
		worker = new SBMLTreeSwingWorker<Void, Void>(searchField.getText());
		
		TreePath path = null;
		if (tree != null) {
			path = tree.getSelectionPath();
		}
		
		tree.setShowsRootHandles(true);
		tree.setScrollsOnExpand(true);
		for (ActionListener al : actionListeners) {
			tree.addActionListener(al);
		}
		if (path != null) {
			tree.setExpandsSelectedPaths(true);
			tree.expandPath(path);
		}
		tree.addTreeSelectionListener(this);
		tree.setSelectionRow(0);
	}
	
	/**
	 * 
	 * @author Sebastian Nagel
	 * @version $Rev$
	 * @since 1.4
	 * @param <T>
	 * @param <V>
	 */
	class SBMLTreeSwingWorker<T,V> extends SwingWorker<T,V>{
		/**
		 * 
		 */
		private String searchString;
		
		/**
		 * @param text
		 */
		public SBMLTreeSwingWorker(String searchString) {
			super();
			this.searchString = searchString;
		}

		/**
		 * 
		 * @return
		 */
		public String getSearchString(){
			return searchString;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		protected T doInBackground(){
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			
			if (!this.isCancelled()) {
				tree.setAllVisible();
				
				if (searchString.equals("")){
					SBMLNode.showInvisible = true;
				}
				else {
					String search = ".*" + searchString + ".*";
					RegexpNameFilter nameFilter = new RegexpNameFilter(search, false);
					RegexpSpeciesReferenceFilter specFilter = new RegexpSpeciesReferenceFilter(search, false);
					RegexpAssignmentVariableFilter assFilter = new RegexpAssignmentVariableFilter(search, false);
					OrFilter filter = new OrFilter(nameFilter, specFilter, assFilter);
					tree.search(filter);
				}
			}
			
			return null;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		protected void done(){
			if (!this.isCancelled()){
				if (SBMLNode.showInvisible) {
					tree.restoreSelectionPath();
				} else {
					tree.expandAll(true);
				}
			}
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
	 * .TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {
		TreeNode node = (TreeNode) tree.getLastSelectedPathComponent();
	    if (node == null) {
	      // Nothing is selected.
	      return;
	    }
	    if (node instanceof SBMLNode) {
	      TreeNodeWithChangeSupport sbmlNode = ((SBMLNode) node).getUserObject();
	      if (sbmlNode instanceof SBase) {
	        int proportionalLocation = getDividerLocation();
	        try {
	          setRightComponent(createRightComponent((SBase) sbmlNode));
	        } catch (Exception e1) {
	          GUITools.showErrorMessage(this, e1);
	        }
	        setDividerLocation(proportionalLocation);
	      } else if (sbmlNode instanceof ASTNode) {
	        int proportionalLocation = getDividerLocation();
	        try {
	          setRightComponent(createRightComponent((ASTNode) sbmlNode));
	        } catch (Exception exc) {
	          GUITools.showErrorMessage(this, exc);
	        }
	        setDividerLocation(proportionalLocation);
	      } else {
	        logger.fine(String.format("node class %s is unknown.", node.getClass()
	            .getName()));
	        // displayURL(helpURL);
	      }
	    }
	}
}
