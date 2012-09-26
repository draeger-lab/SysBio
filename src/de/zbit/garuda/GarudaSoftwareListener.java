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
import java.io.File;
import java.util.logging.Logger;

import jp.sbi.garuda.platform.commons.protocol.ProtocolResults;
import jp.sbi.garuda.platform.commons.protocol.c2s.LoadFileRequest;
import jp.sbi.garuda.platform.commons.protocol.c2s.LoadGadgetRequest;
import jp.sbi.garuda.platform.commons.protocol.s2c.GetLoadableSoftwaresResponse;
import jp.sbi.garuda.platform.software.handler.SoftwareRequestListener;
import jp.sbi.garuda.platform.software.handler.SoftwareResponseListener;
import de.zbit.UserInterface;
import de.zbit.gui.GUITools;

/**
 * @author Andreas Dr&auml;ger
 * @date 17:28:33
 * @since 1.1
 * @version $Rev$
 */
public class GarudaSoftwareListener implements SoftwareRequestListener,
		SoftwareResponseListener {
	
	/**
	 * 
	 */
	private GarudaSoftwareBackend backend;
	/**
	 * A {@link Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(GarudaSoftwareListener.class.getName());

	/**
	 * 
	 * @param backend
	 */
	public GarudaSoftwareListener(GarudaSoftwareBackend backend) {
		this.backend = backend;
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
	
	/* (non-Javadoc)
	 * @see jp.sbi.garuda.platform.software.handler.SoftwareResponseListener#getLoadableSoftwares(jp.sbi.garuda.platform.commons.protocol.s2c.GetLoadableSoftwaresResponse)
	 */
	@Override
	public void getLoadableSoftwares(GetLoadableSoftwaresResponse response) {
		logger.fine("Got compatible software List. " + response.toString());
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
		logger.fine("Got load gadget request: "  + request.toString() ) ;
		backend.firePropertyChange(GarudaSoftwareBackend.LOAD_GADGET_PROPERTY_CHANGE_ID, request.body.filePath);
		return ProtocolResults.SUCCESS ;
	}

}
