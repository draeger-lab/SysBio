/*
 * $Id:  OpenedFile.java 15:43:16 Sebastian$
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
package de.zbit.sbml.io;

import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.jsbml.util.TreeNodeRemovedEvent;

/**
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class OpenedFile<V> extends de.zbit.io.OpenedFile<V> implements PropertyChangeListener, TreeNodeChangeListener {

	/**
	 * 
	 */
	public OpenedFile() {
		super();
	}
	
	/**
	 * @param file
	 * @param document
	 */
	public OpenedFile(File file, V document) {
		super(file, document);
	}
	
	/**
	 * @param document
	 */
	public OpenedFile(V document) {
		super(document);
	}
	
	/**
	 * @param file
	 */
	public OpenedFile(File file) {
		super(file);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
	 */
	public void nodeAdded(TreeNode node) {
		boolean previous = isChanged();
		setChanged(true);
		firePropertyChange("openedFileChanged", previous, true);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(javax.swing.tree.TreeNode)
	 */
	public void nodeRemoved(TreeNodeRemovedEvent evt) {
		nodeAdded(evt.getSource());
	}

}
