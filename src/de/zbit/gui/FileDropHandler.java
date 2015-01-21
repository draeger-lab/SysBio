/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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

import static de.zbit.util.Utils.getMessage;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

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
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class FileDropHandler extends TransferHandler {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -2874307479020044075L;
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(FileDropHandler.class.getName());
  
  /**
   * Event ID for a single file, fired on the listener.
   * The source object will be a {@link File}.
   */
  public final static int FILE_DROPPED = 1;
  
  /**
   * Event ID for a multiple files, fired on the listener.
   * The source object will be a {@link List} of {@link File}s.
   */
  public final static int FILES_DROPPED = 2;
  
  /**
   * This listener will be fired on a drop.
   */
  private ActionListener listener;
  
  /**
   * Creates a new FileDropHandler, that fires the given listener on drop.
   * @param l - an ActionListener which will recive the following event(s):
   * {@code new ActionEvent(file, FILE_DROPPED, "FILE_DROPPED")}.
   */
  public FileDropHandler(ActionListener l) {
    listener = l;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
   */
  @Override
  public boolean canImport(TransferSupport supp) {
    boolean canImport = supp.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    // Also clipboard paste's possible.
    //if (!supp.isDrop()) {
    //return false;
    //}
    if (!canImport && supp.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      canImport = true;
    }
    
    /* return true if and only if the drop contains a list of files */
    return canImport;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean importData(TransferSupport supp) {
    if (!canImport(supp)) {
      return false;
    }
    
    /* fetch the Transferable */
    Transferable t = supp.getTransferable();
    
    try {
      /* fetch the data from the Transferable */
      
      Object data;
      List<File> fileList;
      
      if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        data = t.getTransferData(DataFlavor.stringFlavor);
        List<String> stringList = Arrays.asList(data.toString().split("\n"));
        fileList = new LinkedList<File>();
        for (int i = 0; i < stringList.size(); i++) {
          try {
            URL url = new URL(stringList.get(i));
            fileList.add(new File(url.getFile()));
          } catch (MalformedURLException exc) {
            logger.fine(getMessage(exc));
          }
        }
        if ((fileList.size() == 0) && (stringList.size() > 0)) {
          return false;
        }
      } else {
        data = t.getTransferData(DataFlavor.javaFileListFlavor);
        /* data of type javaFileListFlavor is a list of files */
        fileList = (List<File>) data;
      }
      
      // Fire single action per drag and drop.
      if (fileList.size() > 1) {
        ActionEvent e = new ActionEvent(fileList, FILES_DROPPED, "FILES_DROPPED");
        listener.actionPerformed(e);
        
      } else {
        
        /* loop through the files in the file list */
        for (File file : fileList) {
          ActionEvent e = new ActionEvent(file, FILE_DROPPED, "FILE_DROPPED");
          listener.actionPerformed(e);
        }
      }
    } catch (UnsupportedFlavorException e) {
      return false;
    } catch (IOException e) {
      return false;
    }
    
    return true;
  }
  
}
