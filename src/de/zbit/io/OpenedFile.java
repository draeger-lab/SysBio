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
public class OpenedFile<T> {

	/**
	 * Event name used to indicate that the document associated to the file in this object has been parsed.
	 */
	public static final String DOCUMENT_SET_EVENT = "de.zbit.io.OpenedFile.documentSetEvent";
	
	/**
	 * {@link File} change event name.
	 */
	public static final String FILE_CHANGED_EVENT = "de.zbit.io.OpenedFile.fileChangedEvent";
	
	/**
	 * {@link File} content change event name.
	 */
	public static final String FILE_CONTENT_CHANGED_EVENT = "de.zbit.io.OpenedFile.fileContentChangedEvent";

	/**
	 * Flag to indicate if the document has been changed.
	 */
	boolean changed = false;

	/**
	 * The content of the {@link File}, i.e., the result of some parsing.
	 */
	protected T document;
	
	/**
	 * The file that has been opened.
	 */
	protected File file;
	
	/**
	 * Notifies listeners about changes within the document.
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OpenedFile<?> other = (OpenedFile<?>) obj;
		if (changed != other.changed) {
			return false;
		}
		if (document == null) {
			if (other.document != null) {
				return false;
			}
		} else if (!document.equals(other.document)) {
			return false;
		}
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		} else if (!file.equals(other.file)) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return the original
	 */
	public T getDocument() {
		return document;
	}

	/**
	 * 
	 * @return the {@link File} object.
	 */
	public File getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = prime * hash + (changed ? 1231 : 1237);
		hash = prime * hash + ((document == null) ? 0 : document.hashCode());
		hash = prime * hash + ((file == null) ? 0 : file.hashCode());
		return hash;
	}
	
	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * Checks if the document is not null.
	 * @return {@code true} if the document is not null, {@code false} otherwise.
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

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		boolean previous = isChanged();
		this.changed = changed;
		propertyChangeSupport.firePropertyChange(FILE_CONTENT_CHANGED_EVENT, previous, changed);
	}
	
	/**
	 * 
	 * @param document
	 */
	public void setDocument(T document) {
		T previousDoc = getDocument();
		this.document = document;
		propertyChangeSupport.firePropertyChange(DOCUMENT_SET_EVENT, previousDoc, document);
	}

	/**
	 * set the File for saving changes
	 * @param file
	 */
	public void setFile(File file) {
		File oldFile = getFile();
		this.file = file;
		propertyChangeSupport.firePropertyChange(FILE_CHANGED_EVENT, oldFile, file);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OpenedFile [file=" + file + ", changed=" + changed + ", document=" + document + ']';
	}

	/**
	 * Removes the pointer to the underlying file. This call is equal to calling
	 * {@code setFile(null)}.
	 */
	public void unsetFile() {
		setFile(null);
	}
	
}
