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
 * Class for downloading KEGG Pathways in XML format (KGML-files).
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class KGMLSelectAndDownload {
  
  
  /**
   * The base path to locate KGML pathways. As of 2011-07-01 this is no more publicly
   * available on the internet.
   */
  //private final static String baseKGMLurl = "ftp://ftp.genome.jp/pub/kegg/xml/kgml/";
  
  
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
  
  /**
   * Can be used in combination with the {@link PathwaySelector} to
   * evaluate the dialog and download the selected pathway.
   * @param selector
   * @return local file name of the downloaded pathway.
   * @throws Exception if the pathway could not be downloaded.
   */
  public static String evaluateOKButton(final PathwaySelector selector) throws Exception {
    // Create pathway to orthologous or organism specific pathway.
    String org = selector.getOrganismSelector().getSelectedOrganismAbbreviation();
    if (org==null || org.equals("map")) org="ko";
    
    return downloadPathway(selector.getSelectedPathwayID(), true);
  }

  /**
   * Download a pathway from KEGGs public FTP server.
   * @param pwID pathway identifier (e.g., "mmu00010")
   * @param askUserBeforeUsingCache if true (default), the user will be asked if a 
   * file with the same name has already been downloaded. If false, this method will
   * simply return the path to the already existing file.
   * @return the local file path of the downloaded pathway.
   * @throws Exception if the pathway could not be downloaded.
   */
  public static String downloadPathway(String pwID, boolean askUserBeforeUsingCache) throws Exception {
    return downloadPathway(pwID, null, askUserBeforeUsingCache);
  }

  /**
   * Download a pathway from KEGGs public FTP server.
   * @param pwID pathway identifier (e.g., "mmu00010")
   * @param localFile specify the local file where the xml should be downloaded to.
   * @param askUserBeforeUsingCache if true (default), the user will be asked if a 
   * file with the same name has already been downloaded. If false, this method will
   * simply return the path to the already existing file.
   * @return the local file path of the downloaded pathway.
   * @throws Exception if the pathway could not be downloaded.
   */
  public static String downloadPathway(String pwID, String localFile, boolean askUserBeforeUsingCache) throws Exception {
    
    // OLD FTP-Based procedure before 2011-07-01
    
    // Metabolic
    /*String metaURL = baseKGMLurl + "metabolic/" + 
    (org.equals("ko")?"":"organisms/") +
    org + '/' + pwID + ".xml";
    
    
    // Non-Metabolic
    String nonMetaURL = metaURL.replace("metabolic/", "non-metabolic/");
    
    // Try to download
    localFile = downloadKGML(metaURL, nonMetaURL, askUserBeforeUsingCache);
    if (localFile!=null) {
      //break;
    } else {
      // Show message "Pathway not for organism".
      JOptionPane.showMessageDialog(null, "Could not download the selected pathway for the selected organism.", "Download KGML", JOptionPane.ERROR_MESSAGE);
    }*/
    
    // ----
    
    // Generic maps must be fetched for the general orthologous organism (ko).
    // It is not possible to download "map" pathways.
    if (pwID.startsWith("map")) pwID="ko"+pwID.substring(3);
    
    // Try to download with new url
    localFile = downloadKGML(pwID, localFile, askUserBeforeUsingCache);
    if (localFile==null) {
//      GUITools.showErrorMessage(null, String.format("Could not download the selected pathway for the selected organism (%s).", pwID));
      throw new Exception(String.format("Could not download the selected pathway for the selected organism (%s).", pwID));
    }
    
    return localFile;
  }
  
  /**
   * For KGML downloads BEFORE 2011-07-01.
   * 
   * @param metaURL url for metabolic pathway
   * @param nonMetaURL url for non-metabolic pathway
   * @param askUserBeforeUsingCache if true (default), the user will be asked if a 
   * file with the same name has already been downloaded. If false, this method will
   * simply return the path to the already existing file.
   * @return
   */
//  @SuppressWarnings("unused")
//  @Deprecated
//  private static String downloadKGML(String metaURL, String nonMetaURL, boolean askUserBeforeUsingCache) {
//    // Check if file already exists and ask user to reuse or overwrite or cancel.
//    String localFile = FileDownload.getLocalFilenameForURL(metaURL);
//    if (localFile!=null && new File(localFile).exists() && new File(localFile).length()>1) {
//      int a=0;
//      if (askUserBeforeUsingCache) {
//        String[] options = new String[]{"Open already existing file", "Redownload file", "Cancel"};
//        a = JOptionPane.showOptionDialog(null, "The file '" + localFile + "' already exists. How do you want to proceed?",
//        "Download KGML", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//      }
//      if (a == 0) {
//        return localFile;
//      } else if (a==2) {
//        return null;
//      }
//      // Simply continue if a ==1.
//    }
//    
//    // ToDo: Put download in separate thread and check for timeouts. 
//    if (metaURL!=null) {
//      metaURL = FileDownload.download(metaURL);
//    }
//    
//    if (metaURL!=null && new File(metaURL).exists() && new File(metaURL).length()>1) {
//      return metaURL;
//    } else if (nonMetaURL!=null){
//      nonMetaURL = FileDownload.download(nonMetaURL);
//      if (new File(nonMetaURL).exists() && new File(nonMetaURL).length()>1) {
//        return nonMetaURL;
//      } else {
//        return null;
//      }
//    } else {
//      return null;
//    }
//  }
  
  /**
   * Download KGML file for a KEGG pathway ID.
   * @param pwID pathway id for pathway to download. E.g.: "rno00010".
   * @param askUserBeforeUsingCache if true (default), the user will be asked if a 
   * file with the same name has already been downloaded. If false, this method will
   * simply return the path to the already existing file.
   * @return path and filename of the downloaded KGML-KEGG Pathway.
   */
  @SuppressWarnings("unused")
  private static String downloadKGML(String pwID, boolean askUserBeforeUsingCache) {
    return downloadKGML(pwID, null, askUserBeforeUsingCache);
  }
  
  /**
   * Download KGML file for a KEGG pathway ID.
   * @param pwID pathway id for pathway to download. E.g.: "rno00010".
   * @param localFile OPTIONAL: specify the local file where the xml should be downloaded to. If null,
   * will be infered automatically.
   * @param askUserBeforeUsingCache if true (default), the user will be asked if a 
   * file with the same name has already been downloaded. If false, this method will
   * simply return the path to the already existing file.
   * @return path and filename of the downloaded KGML-KEGG Pathway.
   */
  private static String downloadKGML(String pwID, String localFile, boolean askUserBeforeUsingCache) {
    if (localFile==null || localFile.length()<1) {
      localFile = pwID + ".xml";
    }
    
    // Check if file already exists and ask user to reuse or overwrite or cancel.
    if (localFile!=null && new File(localFile).exists() && new File(localFile).length()>1) {
      int a=0;
      if (askUserBeforeUsingCache) {
        String[] options = new String[]{"Open already existing file", "Redownload file", "Cancel"};
        a = JOptionPane.showOptionDialog(null, "The file '" + localFile + "' already exists. How do you want to proceed?",
          "Download KGML", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
      }
      if (a == 0) {
        return localFile;
      } else if (a==2) {
        return null;
      }
      // Simply continue if a ==1.
    }
    
    // TODO: Put download in separate thread and check for timeouts.
    String newUrl = String.format("http://www.genome.jp/kegg-bin/download?entry=%s&format=kgml", pwID);
    localFile = FileDownload.download(newUrl, localFile);
    
    if (localFile!=null && new File(localFile).exists() && new File(localFile).length()>1) {
      return localFile;
    } else {
      return null;
    }
  }
  
}
