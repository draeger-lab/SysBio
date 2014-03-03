/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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

import jp.sbi.garuda.client.backend.GarudaClientBackend;
import jp.sbi.garuda.client.backend.listeners.GarudaBackendPropertyChangeEvent;
import jp.sbi.garuda.platform.commons.Gadget;
import jp.sbi.garuda.platform.commons.protocol.ProtocolResults;
import jp.sbi.garuda.platform.commons.protocol.c2g.LoadDataRequest;
import jp.sbi.garuda.platform.commons.protocol.c2g.LoadGadgetRequest;
import jp.sbi.garuda.platform.commons.protocol.g2c.GetCompatibleGadgetListResponse;
import jp.sbi.garuda.platform.gadget.handler.GadgetRequestListener;
import jp.sbi.garuda.platform.gadget.handler.GadgetResponseListener;
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
public class GarudaSoftwareListener implements PropertyChangeListener, GadgetRequestListener, GadgetResponseListener {
  
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
   * @see jp.sbi.garuda.platform.gadget.handler.GadgetResponseListener#getCompatibleGadgetList(jp.sbi.garuda.platform.commons.protocol.s2c.GetCompatibleGadgetListResponse)
   */
  @Override
  public void getCompatibleGadgetList(GetCompatibleGadgetListResponse response) {
    logger.fine(MessageFormat.format(bundle.getString("RECEIVED_COMPATIBLE_SOFTWARE_LIST"), response.toString()));
    backend.firePropertyChange(GarudaClientBackend.GOT_GADGETS_PROPERTY_CHANGE_ID, response.body.gadgets);
  }
  
  /* (non-Javadoc)
   * @see jp.sbi.garuda.platform.gadget.handler.GadgetRequestListener#loadData(jp.sbi.garuda.platform.commons.protocol.c2s.LoadDataRequest)
   */
  @Override
  public String loadData(LoadDataRequest request) {
    backend.openFile(new File(request.body.data));
    return ProtocolResults.SUCCESS;
  }
  
  /* (non-Javadoc)
   * @see jp.sbi.garuda.platform.gadget.handler.GadgetRequestListener#loadGadget(jp.sbi.garuda.platform.commons.protocol.c2g.LoadGadgetRequest)
   */
  @Override
  public String loadGadget(LoadGadgetRequest request) {
    logger.fine(MessageFormat.format("RECEIVED_GADGET_REQUEST", request.toString()));
    backend.firePropertyChange(GarudaClientBackend.LOAD_GADGET_PROPERTY_CHANGE_ID, request.body.loadableGadgetName);
    return ProtocolResults.SUCCESS;
  }
  
  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String name = evt.getPropertyName();
    
    if (name.equals(GarudaClientBackend.LOAD_GADGET_PROPERTY_CHANGE_ID)) {
      // I just received a message from a gadget
      logger.info(MessageFormat.format(bundle.getString("LOADED_GADGET"), evt.getNewValue()));
    } else if (name.equals(GarudaClientBackend.CONNECTION_TERMINATED_ID)) {
      logger.info(bundle.getString("LOST_CONNECTION_TO_CORE"));
    } else if (name.equals(GarudaClientBackend.CONNECTION_NOT_INITIALIZED_ID)) {
      logger.info(bundle.getString("COULD_NOT_ESTABLISH_CONNECTION"));
    } else if (name.equals(GarudaClientBackend.GADGET_REGISTRATION_ERROR_ID)) {
      logger.info(bundle.getString("SOFTWARE_ALREADY_REGISTERED"));
    } else if (name.equals(GarudaClientBackend.GOT_GADGETS_PROPERTY_CHANGE_ID)) {
      if (backend.getCompatibleGadgetList() != null) {
        logger.info(bundle.getString("RECEIVED_NEW_SOFTWARE_LIST"));
        //        backend.removePropertyChangeListener(this);
        backend.firePropertyChange(GarudaClientBackend.GOT_GADGETS_PROPERTY_CHANGE_ID, backend.getCompatibleGadgetList());
        //        backend.addPropertyChangeListener(this);
      }
    }
    
    if (evt instanceof GarudaBackendPropertyChangeEvent) {
      
      GarudaBackendPropertyChangeEvent garudaPropertyEvt = (GarudaBackendPropertyChangeEvent) evt;
      
      if (name.equals(GarudaClientBackend.LOAD_DATA_PROPERTY_CHANGE_ID)) {
        logger.info("Loaded File \"" + garudaPropertyEvt.getSecondProperty().toString() + "\" from " + ((Gadget) garudaPropertyEvt.getFirstProperty()).getName());
        backend.openFile(new File(evt.getNewValue().toString()));
      } else if (name.equals(GarudaClientBackend.LOAD_GADGET_PROPERTY_CHANGE_ID)) {
        Gadget loadableGadget = (Gadget) garudaPropertyEvt.getFirstProperty();
        logger.info("Loaded Gadget \"" + loadableGadget.getName() + "\"");
        String launchPath = garudaPropertyEvt.getSecondProperty().toString();
        logger.info("I just received a message from a gadget:\t" + launchPath);
        //CoreClientAPI.getInstance().sentLoadGadgetResponseToCore(true, loadableGadget);
      } else if (name.equals(
        GarudaClientBackend.CONNECTION_TERMINATED_ID)) {
        logger.info("Lost Connection with Core.");
        //      backend.stopBackend();
      } else if (name.equals(
        GarudaClientBackend.CONNECTION_NOT_INITIALIZED_ID)) {
        //      JOptionPane.showMessageDialog(NewTestSoftwareGUI.this, "Could not establish connection to Core.", "No Connection.", JOptionPane.ERROR_MESSAGE);
        //      backend.stopBackend();
        //      enableGarudaUIElements (false);
        backend = null;
        logger.info("Could not establish connection to Core.");
      } else if (name.equals(
        GarudaClientBackend.GADGET_REGISTRATION_ERROR_ID)) {
        logger.info("Software already registered.");
      } else if (name.equals(
        GarudaClientBackend.GADGET_DEREGISTRATION_ERROR_ID)) {
        logger.info("Software already not registered.");
      } else if (name.equals(
        GarudaClientBackend.GADGET_CONNECTION_ESTABLISHED_ID)) {
        logger.info("Software connected.");
      } else if (name.equals(
        GarudaClientBackend.GADGET_REGISTRERED_ID)) {
        logger.info("Software Registered.");
      } else if (name.equals(
        GarudaClientBackend.GADGET_DEREGISTRERED_ID)) {
        logger.info("Software Reregistered.");
      } else if (name.equals(
        GarudaClientBackend.SENT_DATA_RECEIVED_RESPONSE)) {
        logger.info(garudaPropertyEvt.getFirstProperty().toString() + " received sent file .");
      } else if (name.equals(GarudaClientBackend.SENT_DATA_RECEIVED_RESPONSE_ERROR)) {
        logger.info(garudaPropertyEvt.getFirstProperty().toString() + " received sent file .");
      } else if (name.equals(GarudaClientBackend.GOT_GADGETS_PROPERTY_CHANGE_ID)) {
        if (backend.getCompatibleGadgetList() != null) {
          logger.info("Got software List");
        }
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
