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

/**
 * Manage the changes for opened document
 * 
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev: 808 $
 */
public class OpenedFile<T> extends Component implements PropertyChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	protected File file;
	
	/**
	 * 
	 */
	protected T document;
	
	/**
	 * 
	 */
	boolean changed = false;
	


	/**
	 * @return
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * set the File for saving changes
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the original
	 */
	public T getDocument() {
		return document;
	}
	
	/**
	 * 
	 * @param document
	 */
	public void setDocument(T document) {
		this.document = document;
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
	public OpenedFile(T document) {
		this(null, document);
	}
	
	/**
	 * 
	 * @param original
	 */
	public OpenedFile(File file) {
		this(file, null);
	}
	
	/**
	 * 
	 * @param file
	 * @param original
	 */
	public OpenedFile(File file, T document) {
		this.file = file;
		this.document = document;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	//@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("fileChanged")){
			boolean previous = isChanged();
			setChanged(true);
			firePropertyChange("openedFileChanged", previous, true);
		}
	}

}
