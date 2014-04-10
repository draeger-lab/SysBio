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
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import jp.sbi.garuda.client.backend.GarudaClientBackend;
import jp.sbi.garuda.client.backend.listeners.GarudaBackendPropertyChangeEvent;
import jp.sbi.garuda.client.backend.ui.GarudaGlassPanel;
import jp.sbi.garuda.platform.commons.Gadget;
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
public class GarudaSoftwareListener implements PropertyChangeListener {
  
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
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String name = evt.getPropertyName();
    
    if (evt instanceof GarudaBackendPropertyChangeEvent) {
      
      GarudaBackendPropertyChangeEvent garudaPropertyEvt = (GarudaBackendPropertyChangeEvent) evt;
      
      if (name.equals(GarudaClientBackend.GOT_ERRORS_PROPERTY_CHANGE_ID)) {
        UserInterface parent = backend.getParent();
        String message = toMessage(garudaPropertyEvt);
        if (parent instanceof Component) {
          GUITools.showErrorMessage((Component) parent, message);
        } else {
          logger.warning(message);
        }
        logger.warning("An error occured");
      } else if (name.equals(GarudaClientBackend.LOAD_DATA_PROPERTY_CHANGE_ID)) {
        final String filePath =  garudaPropertyEvt.getSecondProperty().toString();
        
        logger.info(MessageFormat.format(bundle.getString("LOADED_FILE"), garudaPropertyEvt.getSecondProperty().toString()));
        logger.fine(((Gadget) garudaPropertyEvt.getFirstProperty()).getName());
        SwingUtilities.invokeLater(new Runnable() {
          /* (non-Javadoc)
           * @see java.lang.Runnable#run()
           */
          @Override
          public void run() {
            backend.openFile(new File(filePath));
          }
        });
      } else if (name.equals(GarudaClientBackend.LOAD_GADGET_PROPERTY_CHANGE_ID)) {
        // I just received a message from a gadget
        Gadget loadableGadget = (Gadget) garudaPropertyEvt.getFirstProperty();
        logger.info(MessageFormat.format(bundle.getString("LOADED_GADGET"), loadableGadget.getName()));
        String launchPath = garudaPropertyEvt.getSecondProperty().toString();
        logger.fine(MessageFormat.format(bundle.getString("PATH_FOR_SOFTWARE"), launchPath));
        firePropertyChange(GarudaClientBackend.LOAD_GADGET_PROPERTY_CHANGE_ID, loadableGadget.getName());
      } else if (name.equals(GarudaClientBackend.CONNECTION_TERMINATED_ID)) {
        SwingUtilities.invokeLater(new Runnable() {
          /* (non-Javadoc)
           * @see java.lang.Runnable#run()
           */
          @Override
          public void run() {
            Object obj = backend.getGlassPane();
            if ((obj != null) && (obj instanceof GarudaGlassPanel)) {
              ((GarudaGlassPanel) obj).showPanel(null);
            }
          }
        });
        logger.info(bundle.getString("LOST_CONNECTION_TO_CORE"));
      } else if (name.equals(GarudaClientBackend.CONNECTION_NOT_INITIALIZED_ID)) {
        logger.info(bundle.getString("COULD_NOT_ESTABLISH_CONNECTION"));
      } else if (name.equals(GarudaClientBackend.GADGET_REGISTRATION_ERROR_ID)) {
        logger.info(bundle.getString("SOFTWARE_ALREADY_REGISTERED"));
      } else if (name.equals(GarudaClientBackend.GADGET_CONNECTION_ESTABLISHED_ID)) {
        logger.info(bundle.getString("GADGET_CONNECTION_ESTABLISHED_ID"));
        firePropertyChange(GarudaSoftwareBackend.GARUDA_ACTIVATED, backend);
      } else if (name.equals(GarudaClientBackend.GADGET_REGISTRERED_ID)) {
        logger.info(bundle.getString("GADGET_REGISTRERED_ID"));
      } else if (name.equals(GarudaClientBackend.SENT_DATA_RECEIVED_RESPONSE)) {
        logger.info(MessageFormat.format(bundle.getString("SENT_DATA_RECEIVED_RESPONSE"), garudaPropertyEvt.getFirstProperty().toString()));
      } else if (name.equals(GarudaClientBackend.SENT_DATA_RECEIVED_RESPONSE_ERROR)) {
        logger.info(MessageFormat.format(bundle.getString("SENT_DATA_RECEIVED_RESPONSE_ERROR"), garudaPropertyEvt.getFirstProperty().toString()));
      } else if (name.equals(GarudaClientBackend.GOT_GADGETS_PROPERTY_CHANGE_ID)) {
        logger.info(bundle.getString("RECEIVED_NEW_SOFTWARE_LIST"));
        if (backend.getCompatibleGadgetList() != null) {
          final List<Gadget> listOfGadgets = backend.getCompatibleGadgetList();
          logger.fine(MessageFormat.format(bundle.getString("RECEIVED_COMPATIBLE_SOFTWARE_LIST"), createGadgetListString(listOfGadgets)));
          SwingUtilities.invokeLater(new Runnable() {
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
              if (!backend.isSetGlassPane()) {
                backend.setGlassPane();
              }
              Object glassPane = backend.getGlassPane();
              if ((glassPane != null) && (glassPane instanceof GarudaGlassPanel)) {
                ((GarudaGlassPanel) glassPane).showPanel(listOfGadgets);
              }
            }
          });
          firePropertyChange(GarudaClientBackend.GOT_GADGETS_PROPERTY_CHANGE_ID, listOfGadgets);
        }
      }
    }
    
  }
  
  /**
   * 
   * @param propertyName
   */
  private void firePropertyChange(final String propertyName, final Object newValue) {
    SwingUtilities.invokeLater(new Runnable() {
      /* (non-Javadoc)
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run() {
        backend.firePropertyChange(propertyName, newValue);
      }
    });
  }
  
  /**
   * 
   * @param listOfGadgets
   * @return
   */
  private String createGadgetListString(List<Gadget> listOfGadgets) {
    StringBuilder list = new StringBuilder();
    list.append('[');
    if (listOfGadgets != null) {
      for (int i = 0; i < listOfGadgets.size(); i++) {
        Gadget g = listOfGadgets.get(i);
        list.append(g.getName());
        if (i < listOfGadgets.size() - 1) {
          list.append(", ");
        }
      }
    }
    list.append(']');
    return list.toString();
  }
  
  /**
   * 
   * @param garudaPropertyEvt
   * @return
   */
  private String toMessage(GarudaBackendPropertyChangeEvent garudaPropertyEvt) {
    String message = "";
    if (garudaPropertyEvt.getFirstProperty() != null) {
      message = garudaPropertyEvt.getFirstProperty().toString();
    } else if (garudaPropertyEvt.getSecondProperty() != null) {
      message = garudaPropertyEvt.getSecondProperty().toString();
    }
    return message;
  }
  
}
