/*
 * $Id$
 * $URL$
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
package de.zbit.gui;

import java.util.EventListener;

/**
 * Allows reacting to closing events in {@link JTabbedPaneDraggableAndCloseable}.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public interface TabClosingListener extends EventListener {

	/**
	 * Allows implementers to perform some required action before a tab can be
	 * closed, or even to prevent the tab from being closed.
	 * 
	 * @param index
	 *        the index of the tab that is now about to be closed.
	 * @return if {@code true} this means that closing the tab has been accepted
	 *         by this listener, a value of {@code false} means a veto.
	 */
	public boolean tabClosing(int index);

}