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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

/**
 * Manage the changes for opened document in a {@link File}.
 * 
 * @author Sebastian Nagel
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @version $Rev: 808 $
 */
public class OpenedFile<T> implements PropertyChangeListener {

	/**
	 * File change event name.
	 */
	public static final String FILE_CHANGED_EVENT = "de.zbit.io.OpenedFile.fileChangedEvent";

	/**
	 * 
	 */
	private PropertyChangeSupport propertyChangeSupport;
	
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
	 * checks if Filepath is set
	 */
	public boolean isSetFile() {
		return this.file != null;
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
	 * Bean constructor.
	 */
	public OpenedFile() {
		super();
	}
	
	/**
	 * 
	 * @param file
	 * @param original
	 */
	public OpenedFile(File file, T document) {
		this();
		this.file = file;
		this.document = document;
		this.propertyChangeSupport = new PropertyChangeSupport(this);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	//@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(FILE_CHANGED_EVENT)) {
			boolean previous = isChanged();
			setChanged(true);
			firePropertyChange(FILE_CHANGED_EVENT, previous, true);
		}
	}

	/**
	 * 
	 */
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	/**
	 * 
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}
}
