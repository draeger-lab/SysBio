/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import jp.sbi.garuda.client.backend.GarudaClientBackend;
import jp.sbi.garuda.platform.commons.Gadget;
import jp.sbi.garuda.platform.commons.exception.NetworkException;
import jp.sbi.garuda.platform.commons.net.GarudaConnectionNotInitializedException;
import de.zbit.gui.GUITools;
import de.zbit.io.FileTools;
import de.zbit.util.ResourceManager;

/**
 * Requests compatible {@link Gadget} from the Garuda Core in the background
 * and opens a dialog to ask the user where to sent a file if this has been
 * successful.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev: 1158$
 * @since 1.1
 */
public class GarudaFileSender extends SwingWorker<Void, Gadget> {
  
  /**
   * Localization support.
   */
  private static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.garuda.locales.Labels");
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(GarudaFileSender.class.getName());
  
  /**
   * The GUI component acting as the parent for {@link JOptionPane}s to be
   * opened by this class.
   */
  private Component parent;
  /**
   * The back-end to communicate with the Garuda Core.
   */
  private GarudaSoftwareBackend garudaBackend;
  /**
   * The file for which compatible software is to be searched.
   */
  private File file;
  
  /**
   * The type of accepted files that the filtering of the list will be depended
   * upon.
   */
  private String fileType;
  
  /**
   * Initializes this file sender.
   * 
   * @param parent
   *        The GUI component acting as the parent for {@link JOptionPane}s to
   *        be opened by this class.
   * @param garudaBackend
   *        The backend to communicate with the Garuda Core.
   * @param file
   *        The file for which compatible software is to be searched.
   */
  public GarudaFileSender(Component parent, GarudaSoftwareBackend garudaBackend, File file, String fileType) {
    super();
    this.parent = parent;
    this.garudaBackend = garudaBackend;
    this.file = file;
    this.fileType = fileType;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Void doInBackground() throws Exception {
    garudaBackend.addPropertyChangeListener(new PropertyChangeListener() {
      
      /* (non-Javadoc)
       * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
       */
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        logger.fine(evt.toString());
        if (evt.getPropertyName().equals(GarudaClientBackend.GOT_GADGETS_PROPERTY_CHANGE_ID)) {
          // Avoid calls at later time points
          garudaBackend.removePropertyChangeListener(this);
          
          @SuppressWarnings("unchecked")
          List<Gadget> listOfCompatibleSoftare = (List<Gadget>) evt.getNewValue();
          publish(listOfCompatibleSoftare.toArray(new Gadget[] {}));
        }
      }
      
    });
    garudaBackend.requestForLoadableGadgets(file, fileType);
    return null;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#process(java.util.List)
   */
  @Override
  protected void process(List<Gadget> listOfCompatibleSoftare) {
    if (listOfCompatibleSoftare.isEmpty()) {
      JOptionPane.showMessageDialog(parent, MessageFormat.format(
        bundle.getString("NO_COMPATIBLE_SOFTWARE_FOUND"),
        FileTools.getExtension(file.getName())));
    } else {
      String softwareNames[] = new String[listOfCompatibleSoftare.size()];
      int i = 0;
      for (Gadget software : listOfCompatibleSoftare) {
        softwareNames[i++] = software.getName();
      }
      JComboBox compatibleSoftwaresComboBox = new JComboBox(softwareNames);
      compatibleSoftwaresComboBox.setPreferredSize(new Dimension(150, 20));
      compatibleSoftwaresComboBox.setToolTipText(bundle.getString("SOFTWARE_LIST"));
      
      if (JOptionPane.showConfirmDialog(parent, compatibleSoftwaresComboBox,
        bundle.getString("SELECT_SOFTWARE"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        if ((garudaBackend != null) && (compatibleSoftwaresComboBox.getSelectedIndex() != -1)) {
          try {
            garudaBackend.sentFileToSoftware(file, compatibleSoftwaresComboBox.getSelectedIndex()) ;
          } catch (IllegalStateException exc) {
            GUITools.showErrorMessage(parent, exc);
          } catch (NetworkException exc) {
            GUITools.showErrorMessage(parent, exc);
          } catch (GarudaConnectionNotInitializedException exc) {
            GUITools.showErrorMessage(parent, exc);
          }
        }
      }
    }
  }
  
}
