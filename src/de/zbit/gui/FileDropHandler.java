/**
 *
 * @author wrzodek
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
 * @author wrzodek
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
   * @param l
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
