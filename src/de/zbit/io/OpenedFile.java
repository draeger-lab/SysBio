/*
 * $Id: OpenedFile.java 931 2012-05-30 10:53:59Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/io/OpenedFile.java $
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
package de.zbit.io;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.util.TreeNodeChangeListener;


/**
 * Manage the changes for opened document
 * 
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev: 808 $
 */
public class OpenedFile<T,V> extends Component implements PropertyChangeListener, TreeNodeChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File file;
	
	private T original;
	private V workingCopy;
	
	/**
	 * 
	 */
	boolean changed = false;
	
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
	 * @return the original
	 */
	public T getOriginal() {
		return original;
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	/**
	 * 
	 * @param original
	 */
	public OpenedFile(T original) {
		this(original, null);
	}
	
	/**
	 * 
	 * @param original
	 * @param workingCopy
	 */
	public OpenedFile(T original, V workingCopy) {
		this(null,original,workingCopy);
	}
	
	/**
	 * 
	 * @param file
	 * @param original
	 * @param workingCopy
	 */
	public OpenedFile(File file, T original, V workingCopy) {
		this.file = file;
		this.original = original;
		this.workingCopy = workingCopy;
	}

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("fileChanged")){
			setChanged(true);
			firePropertyChange("openedFileChanged", false, true);
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
	 */
	@Override
	public void nodeAdded(TreeNode node) {
		setChanged(true);
		firePropertyChange("openedFileChanged", false, true);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(javax.swing.tree.TreeNode)
	 */
	@Override
	public void nodeRemoved(TreeNode node) {
		setChanged(true);
		firePropertyChange("openedFileChanged", false, true);
	}

	/**
	 * @return
	 */
	public File getFile() {
		return file;
	}
}
