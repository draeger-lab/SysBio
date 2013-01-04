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
	 * 
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
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!openedFile.isChanged()) {
			openedFile.propertyChange(evt);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
	 */
	public void nodeAdded(TreeNode node) {
		if (!openedFile.isChanged()) {
			propertyChange(new PropertyChangeEvent(node, OpenedFile.FILE_CHANGED_EVENT, Boolean.FALSE, Boolean.TRUE));
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(javax.swing.tree.TreeNode)
	 */
	public void nodeRemoved(TreeNodeRemovedEvent evt) {
		if (!openedFile.isChanged()) {
			propertyChange(new PropertyChangeEvent(evt, OpenedFile.FILE_CHANGED_EVENT, Boolean.FALSE, Boolean.TRUE));
		}
	}
	
}
