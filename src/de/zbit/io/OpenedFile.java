/*
 * $Id: OpenedFile.java 931 2012-05-30 10:53:59Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/io/OpenedFile.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OpenedFile [file=" + file + ", changed=" + changed + ", document=" + document
				+ ']';
	}

	/**
	 * 
	 */
	boolean changed = false;
	
	/**
	 * 
	 */
	protected T document;
	
	/**
	 * 
	 */
	protected File file;
	
	/**
	 * 
	 */
	private PropertyChangeSupport propertyChangeSupport;
	


	/**
	 * Bean constructor.
	 */
	public OpenedFile() {
		super();
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
		this();
		this.file = file;
		this.document = document;
		this.propertyChangeSupport = new PropertyChangeSupport(this);
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
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OpenedFile<?>) {
			OpenedFile<?> other = (OpenedFile<?>) obj;
			return this.isSetFile() && other.isSetFile() &&
					this.file.equals(other.file);
		}
		return false;
	}

	/**
	 * 
	 */
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	/**
	 * @return the original
	 */
	public T getDocument() {
		return document;
	}
	
	/**
	 * @return
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSetDocument() {
		return document != null;
	}

	/**
	 * checks if file path is set
	 */
	public boolean isSetFile() {
		return this.file != null;
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
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	/**
	 * 
	 * @param document
	 */
	public void setDocument(T document) {
		this.document = document;
	}

	/**
	 * set the File for saving changes
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	
}
