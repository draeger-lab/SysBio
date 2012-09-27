/*
 * $Id$
 * $URL$
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
package de.zbit.garuda;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import jp.sbi.garuda.platform.commons.protocol.ProtocolResults;
import jp.sbi.garuda.platform.commons.protocol.c2s.LoadFileRequest;
import jp.sbi.garuda.platform.commons.protocol.c2s.LoadGadgetRequest;
import jp.sbi.garuda.platform.commons.protocol.s2c.GetLoadableSoftwaresResponse;
import jp.sbi.garuda.platform.software.handler.SoftwareRequestListener;
import jp.sbi.garuda.platform.software.handler.SoftwareResponseListener;
import de.zbit.UserInterface;
import de.zbit.gui.GUITools;
import de.zbit.util.ResourceManager;

/**
 * Listens to software events from the Garuda platform.
 * 
 * @author Andreas Dr&auml;ger
 * @date 17:28:33
 * @since 1.1
 * @version $Rev$
 */
public class GarudaSoftwareListener implements PropertyChangeListener, SoftwareRequestListener,
		SoftwareResponseListener {
	
	/**
	 * Localization support.
	 */
	private static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.garuda.locales.Labels");
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(GarudaSoftwareListener.class.getName());
	
	/**
	 * The backend to which this listener is assigned.
	 */
	private GarudaSoftwareBackend backend;

	/**
	 * 
	 * @param backend
	 */
	public GarudaSoftwareListener(GarudaSoftwareBackend backend) {
		this.backend = backend;
	}
	
	/* (non-Javadoc)
	 * @see jp.sbi.garuda.platform.software.handler.SoftwareResponseListener#getLoadableSoftwares(jp.sbi.garuda.platform.commons.protocol.s2c.GetLoadableSoftwaresResponse)
	 */
	@Override
	public void getLoadableSoftwares(GetLoadableSoftwaresResponse response) {
		logger.fine(MessageFormat.format(bundle.getString("RECEIVED_COMPATIBLE_SOFTWARE_LIST"), response.toString()));
		backend.setListOfCompatibleSoftware(response.getSoftwareList());
	}
	
	/* (non-Javadoc)
	 * @see jp.sbi.garuda.platform.software.handler.SoftwareRequestListener#loadFile(jp.sbi.garuda.platform.commons.protocol.c2s.LoadFileRequest)
	 */
	@Override
	public String loadFile(LoadFileRequest request) {
		backend.openFile(new File(request.body.filePath));
		return ProtocolResults.SUCCESS;
	}
	
	/* (non-Javadoc)
	 * @see jp.sbi.garuda.platform.software.handler.SoftwareRequestListener#loadGadget(jp.sbi.garuda.platform.commons.protocol.c2s.LoadGadgetRequest)
	 */
	@Override
	public String loadGadget(LoadGadgetRequest request) {
		logger.fine(MessageFormat.format("RECEIVED_GADGET_REQUEST", request.toString()));
		backend.firePropertyChange(GarudaSoftwareBackend.LOAD_GADGET_PROPERTY_CHANGE_ID, request.body.filePath);
		return ProtocolResults.SUCCESS ;
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		String name = evt.getPropertyName();
		
		if (name.equals(GarudaSoftwareBackend.LOAD_FILE_PROPERTY_CHANGE_ID)) {
			logger.info(MessageFormat.format(bundle.getString("LOADED_FILE"), evt.getNewValue()));
		} else if (name.equals(GarudaSoftwareBackend.LOAD_GADGET_PROPERTY_CHANGE_ID)) {
			// I just received a message from a gadget
			logger.info(MessageFormat.format(bundle.getString("LOADED_GADGET"), evt.getNewValue()));
		} else if (name.equals(GarudaSoftwareBackend.CONNECTION_TERMINATED_ID)) {
			logger.info(bundle.getString("LOST_CONNECTION_TO_CORE"));
		} else if (name.equals(GarudaSoftwareBackend.CONNECTION_NOT_INITIALIZED_ID)) {
			logger.info(bundle.getString("COULD_NOT_ESTABLISH_CONNECTION"));
		} else if (name.equals(GarudaSoftwareBackend.SOFTWARE_REGISTRATION_ERROR_ID)) {
			logger.info(bundle.getString("SOFTWARE_ALREADY_REGISTERED"));
		} else if (name.equals(GarudaSoftwareBackend.SOFTWARE_DEREGISTRATION_ERROR_ID)) {
			logger.info(bundle.getString("SOFTWARE_STILL_NOT_REGISTERED"));
		} else if (name.equals(GarudaSoftwareBackend.GOT_SOFTWARES_PROPERTY_CHANGE_ID)) {
			if (backend.getListOfCompatibleSoftware() != null) {
				logger.info(bundle.getString("RECEIVED_NEW_SOFTWARE_LIST"));
			}
		}

	}
	
	/* (non-Javadoc)
	 * @see jp.sbi.garuda.platform.commons.protocol.ProtocolListener#protocolError(java.lang.String)
	 */
	@Override
	public void protocolError(String message) {
		UserInterface parent = backend.getParent();
		if (parent instanceof Component) {
			GUITools.showErrorMessage((Component) parent, message);
		} else {
			logger.warning(message);
		}
	}

}
