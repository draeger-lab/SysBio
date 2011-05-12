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
package de.zbit.kegg.gui;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import de.zbit.gui.GUITools;
import de.zbit.gui.LayoutHelper;
import de.zbit.util.FileDownload;

/**
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class KGMLSelectAndDownload {
  
  private final static String baseKGMLurl = "ftp://ftp.genome.jp/pub/kegg/xml/kgml/";
  
  /**
   * Just for testing.
   * @param args
   */
  public static void main(String[] args) {
    GUITools.initLaF("PathwaySelector test");
    final JPanel p = new JPanel();
    
    // Create a timer, that disables the ok-button in 500ms (as soon as the JOptionPane has built the window).
    SwingWorker<Void, Void> disableOKworker = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return null;
      }
      
      @Override
      protected void done() {
        System.out.println("Disabling ok");
      }
    };
    
    try {
      PathwaySelector selector = PathwaySelector.createPathwaySelectorPanel(new LayoutHelper(p));
      
      // Show the selector.
      disableOKworker.execute();
      while (JOptionPane.showConfirmDialog(null, p, "Test",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        // TODO: Show loading bar or do something...
        
        // Try to download
        String localFile = evaluateOKButton(selector);
        if (localFile!=null) {
          break;
        }
      }
      
    } catch (Throwable exc) {
      GUITools.showErrorMessage(null, exc);
    }

    
  }
  
  public static String evaluateOKButton(final PathwaySelector selector) {
    String localFile=null;
    
    // Create pathway to orthologous or organism specific pathway.
    String org = selector.getOrganismSelector().getSelectedOrganismAbbreviation();
    if (org==null || org.equals("ko")) org="ko";
    
    // Metabolic
    String metaURL = baseKGMLurl + "metabolic/" + 
    (org.equals("ko")?"":"organisms/") +
    org + '/' + selector.getSelectedPathwayID() + ".xml";
    
    // Non-Metabolic
    String nonMetaURL = metaURL.replace("metabolic/", "non-metabolic/");
    
    // Try to download
    localFile = downloadKGML(metaURL, nonMetaURL);
    if (localFile!=null) {
      //break;
    } else {
      // Show message "Pathway not for organism".
      JOptionPane.showMessageDialog(null, "Could not download the selected pathway for the selected organism.", "Download KGML", JOptionPane.ERROR_MESSAGE);
    }
    
    return localFile;
  }
  
  
  private static String downloadKGML(String metaURL, String nonMetaURL) {
    // Check if file already exists and ask user to reuse or overwrite or cancel.
    String localFile = FileDownload.getLocalFilenameForURL(metaURL);
    if (localFile!=null && new File(localFile).exists() && new File(localFile).length()>0) {
      String[] options = new String[]{"Open already existing file", "Redownload file", "Cancel"};
      int a = JOptionPane.showOptionDialog(null, "The file '" + localFile + "' already exists. How do you want to proceed?",
        "Download KGML", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
      if (a == 0) {
        return localFile;
      } else if (a==2) {
        return null;
      }
      // Simply continue if a ==1.
    }
    
    // TODO: Put download in separate thread and check for timeouts. 
    metaURL = FileDownload.download(metaURL);
    if (new File(metaURL).exists() && new File(metaURL).length()>1) {
      return metaURL;
    } else {
      nonMetaURL = FileDownload.download(nonMetaURL);
      if (new File(nonMetaURL).exists() && new File(nonMetaURL).length()>1) {
        return nonMetaURL;
      } else {
        return null;
      }
    }
  }
  
}
