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

/**
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class OpenedFile<V> extends de.zbit.io.OpenedFile<Object> implements PropertyChangeListener, TreeNodeChangeListener{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4542668375887299995L;
	
	/**
	 * 
	 */
	V workingCopy;

	
	/**
	 * @return the workingCopy
	 */
	public V getWorkingCopy() {
		return workingCopy;
	}

	/**
	 * @param workingCopy the workingCopy to set
	 */
	public void setWorkingCopy(V workingCopy) {
		this.workingCopy = workingCopy;
	}
	
	/**
	 * 
	 * @param original
	 * @param workingCopy
	 */
	public OpenedFile(Object original, V workingCopy) {
		this(null,original,workingCopy);
	}
	
	/**
	 * 
	 * @param file
	 * @param original
	 * @param workingCopy
	 */
	public OpenedFile(File file, Object original, V workingCopy) {
		super(file,original);
		this.workingCopy = workingCopy;
	}


	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
	 */
	@Override
	public void nodeAdded(TreeNode node) {
		boolean previous = isChanged();
		setChanged(true);
		firePropertyChange("openedFileChanged", previous, true);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(javax.swing.tree.TreeNode)
	 */
	@Override
	public void nodeRemoved(TreeNode node) {
		nodeAdded(node);
	}
}
