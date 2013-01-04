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
package de.zbit.sbml.io;

import java.beans.PropertyChangeEvent;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.TreeNodeChangeEvent;
import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.jsbml.util.TreeNodeRemovedEvent;

import de.zbit.io.OpenedFile;

/**
 * A specialized listener to register changes in an {@link SBMLDocument} file.
 * 
 * @author Andreas Dr&auml;ger
 * @date 11:08:59
 * @since 1.1
 * @version $Rev$
 */
public class SBMLfileChangeListener implements TreeNodeChangeListener {
	
	/**
	 * The {@link OpenedFile} object whose change events should be forwarded.
	 */
	private OpenedFile<SBMLDocument> openedFile;

	/**
	 * Creates a new {@link TreeNodeChangeListener} to register changes in the
	 * {@link SBMLDocument} encapsulated in the given {@link OpenedFile} and adds
	 * itself to this document.
	 */
	public SBMLfileChangeListener(OpenedFile<SBMLDocument> openedFile) {
		super();
		this.openedFile = openedFile;
		SBMLDocument doc = this.openedFile.getDocument();
		if (doc != null) {
			doc.addTreeNodeChangeListener(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
	 */
	@Override
	public void nodeAdded(TreeNode node) {
		openedFile.propertyChange(new PropertyChangeEvent(node.getParent(), OpenedFile.FILE_CHANGED_EVENT, Boolean.valueOf(openedFile.isChanged()), Boolean.TRUE));
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(org.sbml.jsbml.util.TreeNodeRemovedEvent)
	 */
	@Override
	public void nodeRemoved(TreeNodeRemovedEvent evt) {
		openedFile.propertyChange(new PropertyChangeEvent(evt.getSource(), OpenedFile.FILE_CHANGED_EVENT, Boolean.valueOf(openedFile.isChanged()), Boolean.TRUE));
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!evt.getPropertyName().equals(TreeNodeChangeEvent.userObject)) {
			Object oldVal = evt.getOldValue(), newVal = evt.getNewValue();
			boolean changed = ((oldVal == null) && (newVal != null)) || ((oldVal != null) && (newVal == null)) || (oldVal != newVal) || !oldVal.equals(newVal);
			openedFile.propertyChange(new PropertyChangeEvent(evt.getSource(), OpenedFile.FILE_CHANGED_EVENT, Boolean.valueOf(openedFile.isChanged()), Boolean.valueOf(changed)));
		}
	}
	
}
