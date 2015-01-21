/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

/**
 * This class schedules given instances of {@link SwingWorker} for a serial
 * execution, i.e., it maintains a {@link List} of open tasks (one for each
 * worker) and executes the next worker after the previous one has been
 * completed. While doing that it forwards all {@link PropertyChangeEvent}s to
 * listeners that are registered in this object. It is still possible to
 * register a listener directly at some {@link SwingWorker} in this
 * {@link SerialWorker}. Tasks that have been completed, are directly removed
 * from this {@link SerialWorker}, but it is possible to add more tasks at
 * running time. To this end, you can use the {@link #add(SwingWorker)} method.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class SerialWorker implements PropertyChangeListener {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(SerialWorker.class.getName());
	/**
	 * Listeners that respond to changes triggered by the individual workers.
	 */
	private List<PropertyChangeListener> listOfPropertyChangeListeners;
	/**
	 * The {@link SwingWorker}s that can be executed.
	 */
	private LinkedList<SwingWorker<?, ?>> workers;
	
	/**
	 * Creates a new {@link SerialWorker} that doesn't have any tasks.
	 */
	public SerialWorker() {
		super();
		this.listOfPropertyChangeListeners = new LinkedList<PropertyChangeListener>();
		workers = new LinkedList<SwingWorker<?, ?>>();
	}
	
	/**
	 * 
	 * @param worker
	 */
	public SerialWorker(SwingWorker<?, ?>... worker) {
		this();
		if (worker != null) {
			for (SwingWorker<?, ?> w : worker) {
				add(w);
			}
		}
	}
	
	/**
	 * 
	 * @param worker
	 */
	public void add(SwingWorker<?, ?> worker) {
		if (worker != null) {
			workers.add(worker);
		} else {
			logger.fine("ignoring null value for worker");
		}
	}

	/**
	 * 
	 * @param listener
	 * @return
	 */
	public boolean addPropertyChangeListener(PropertyChangeListener listener) {
		return listOfPropertyChangeListeners.add(listener);
	}

	/**
	 * 
	 */
	public void execute() {
		if (!workers.isEmpty()) {
			SwingWorker<?, ?> worker = workers.removeFirst();
			worker.addPropertyChangeListener(this);
			worker.execute();
		} else {
			firePropertyChange("done", null, null);
		}
	}
	
	/**
	 * 
	 * @param propertyName
	 * @param oldValue
	 * @param newValue
	 */
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		for (PropertyChangeListener listener : listOfPropertyChangeListeners) {
			listener.propertyChange(evt);
		}
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		for (PropertyChangeListener listener : listOfPropertyChangeListeners) {
			listener.propertyChange(evt);
		}
		if ((evt.getSource() instanceof SwingWorker) && ((SwingWorker<?, ?>) evt.getSource()).isDone()) {
			if (evt.getSource() instanceof SwingWorker<?, ?>) {
				((SwingWorker<?, ?>) evt.getSource()).removePropertyChangeListener(this);
			}
			execute();
		}
	}
	
}
