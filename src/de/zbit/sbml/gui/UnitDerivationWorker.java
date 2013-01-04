/* $Id$
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
package de.zbit.sbml.gui;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.UnitDefinition;

import de.zbit.sbml.util.ListOfCallableSBases;

/**
 * Derives all units within a given {@link Model} (in background). The result is
 * an array of HTML-formatted human-readable units.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class UnitDerivationWorker extends SwingWorker<UnitDefinition[], Void> {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(UnitDerivationWorker.class.getName());
	
	/**
	 * 
	 */
	private Model model;

	/**
	 * 
	 */
	public UnitDerivationWorker(Model model) {
		this.model = model;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected UnitDefinition[] doInBackground() throws Exception {
		List<CallableSBase> listOfCallableSBases = new ListOfCallableSBases(model);
		UnitDefinition result[] = new UnitDefinition[listOfCallableSBases.size()];
		for (int i = 0; i < result.length; i++) {
			try {
				result[i] = listOfCallableSBases.get(i).getDerivedUnitDefinition();
			} catch (Exception exc) {
				result[i] = null;
				logger.warning(exc.getLocalizedMessage());
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		try {
			firePropertyChange("done", null, get());
		} catch (InterruptedException exc) {
			logger.warning(exc.getLocalizedMessage());
		} catch (ExecutionException exc) {
			logger.warning(exc.getLocalizedMessage());
		}
	}
	
}
