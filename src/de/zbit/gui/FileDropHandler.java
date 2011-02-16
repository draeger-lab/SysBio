/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler;

/**
 * This class allows the user to drag and drop files to a target.
 * 
 * <p>Usage Example on a JFrame:
 * <pre>
    // Make this panel responsive to drag'n drop events.
    FileDropHandler dragNdrop = new FileDropHandler(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          File file = ((File) event.getSource());
          // Do something (e.g. openFile(file)).
        }
      }
    );
    
    this.setTransferHandler(dragNdrop);
    </pre>
    </p>
 * @author wrzodek
 * @version $Rev$
 */
public class FileDropHandler extends TransferHandler {
  private static final long serialVersionUID = -2874307479020044075L;
  
  /**
   * Event for the listener
   */
  public final static int FILE_DROPPED = 0;
  
  /**
   * This listener will be fired on a drop.
   */
  private ActionListener listener;
  
  /**
   * Creates a new FileDropHandler, that fires the given listener on drop.
   * @param l - an ActionListener which will recive the following event(s):
   * <code>new ActionEvent(file, FILE_DROPPED, "FILE_DROPPED")</code>.
   */
  public FileDropHandler(ActionListener l) {
    listener = l;
  }
  
  public boolean canImport(TransferSupport supp) {
    // Also clipboard paste's possible.
    //if (!supp.isDrop()) {
    //return false;
    //}
    
    /* return true if and only if the drop contains a list of files */
    return supp.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
  }
  
  @SuppressWarnings("unchecked")
  public boolean importData(TransferSupport supp) {
    if (!canImport(supp)) {
      return false;
    }
    
    /* fetch the Transferable */
    Transferable t = supp.getTransferable();
    
    try {
      /* fetch the data from the Transferable */
      Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
      
      /* data of type javaFileListFlavor is a list of files */
      List<File> fileList = (List<File>)data;
      
      /* loop through the files in the file list */
      for (File file : fileList) {
        ActionEvent e = new ActionEvent(file, FILE_DROPPED, "FILE_DROPPED");
        listener.actionPerformed(e);
      }
    } catch (UnsupportedFlavorException e) {
      return false;
    } catch (IOException e) {
      return false;
    }
    
    return true;
  }
  
}
