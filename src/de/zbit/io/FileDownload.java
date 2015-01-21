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
package de.zbit.io;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import de.zbit.util.progressbar.AbstractProgressBar;


/**
 * Download files from a url (http/ftp).
 * <p><i>(heavily modified by a template from Marco Schmidt).</i></p>
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class FileDownload {
  public static final transient Logger log = Logger.getLogger(FileDownload.class.getName());
  public static Object ProgressBar;
  public static Object StatusLabel;
  
  /**
   * Returns the last HTTP Status code (200 = OK, 
   * values &gt;=400 are usually errors).
   */
  public static int status=-1;

  static {
    // Use the system proxy.
    System.setProperty("java.net.useSystemProxies", "true");
  }

  
  /**
   * Downloads the given {@code address} and <b>stores the 
   * results in a file on hard disk.</b>
   *  
   * @param address
   * @return the path and filename of the downloaded file,
   * relative to the current folder. NOT the file content. 
   */
  public static String download(String address) {
    String localFile = getLocalFilenameForURL(address);
    
    if (localFile==null) {
      localFile = "temp.tmp";
      System.err.println("Could not figure out local file name for " + address + ". Calling it '" + localFile + "'.");
    }
    
    localFile = download(address, localFile);
    return localFile;
  }
  
  /**
   * Extracts a valid FileName from an URL. E.g. if the Url is 
   * "http://www.cgi.de/content.txt" the return value is "content.txt".
   * If the URL contains variable characters like "?" or "=", or no
   * valid filename could be extracted, NULL is returned.
   * @param address
   * @return FileName, denoted by this url, or null if it is not valid. 
   */
  public static String getLocalFilenameForURL(String address) {
    int lastSlashIndex = address.lastIndexOf('/');
    String localFile=null;
    if (lastSlashIndex >= 0 &&
        lastSlashIndex < address.length() - 1) {
      localFile = address.substring(lastSlashIndex + 1);
      if (localFile.contains("?") || localFile.contains("=")) localFile = null;
    }
    return localFile;
  }
  
  /**
   * 
   * @param address
   * @param out
   * @throws IOException 
   */
  public static void download(String address, OutputStream out) throws IOException {
    download(address, out, true);
  }
  
  /**
   * 
   * @param address
   * @param out
   * @param verbose if {@code true}, this method will issue log messages and use the progress bar.
   * @throws IOException
   */
  public static void download(String address, OutputStream out, boolean verbose) throws IOException {
    if (out==null) return; // we have no place to write the results
    URLConnection conn = null;
    InputStream  in = null;
    
    AbstractProgressBar progress = null;
    if (verbose) {
      String statusLabelText = "Downloading '" + address + "' ";
      log.info(statusLabelText);
      
      if (ProgressBar!=null && ProgressBar instanceof AbstractProgressBar) {
        progress = (AbstractProgressBar) ProgressBar;
      }
    }
    
    try {
      URL url = new URL(address);
      int status = 0;
      if (address.toLowerCase().startsWith("http:")) {
        status = ((HttpURLConnection) url.openConnection()).getResponseCode();
      } else { //if (address.toLowerCase().startsWith("ftp:"))
        url.openConnection(); //status = ((URLConnection) url.openConnection()).getResponseCode();
      }
      
      if (status>=400) {
        //log.warning("Failed: HTTP error (code " + status + ").");
        System.err.println("Failed: HTTP error (code " + status + ").");
        // This should not logged to warning. Errors include not-existing KEGG IDs and such.
        
        return; //404 und sowas ... >400 nur error codes. Normal:200 =>OK
      }
      FileDownload.status = status;
      
      conn = url.openConnection();
      
      in = conn.getInputStream();
      byte[] buffer = new byte[1024];
      int numRead;
      long numWritten = 0;
      
      final int reportEveryXKB = 50; // Set progressbar every X kb. = StepSize
      //guiOperations.SetProgressBarMAXThreadlike(Math.max(conn.getContentLength(), in.available()), ProgressBar);
      if (verbose && progress!=null) progress.setNumberOfTotalCalls((long)((double)Math.max(conn.getContentLength(), in.available())/(double)(buffer.length*reportEveryXKB)));
      
      int calls = 0;
      while ((numRead = in.read(buffer)) != -1) {
        if (verbose && progress!=null && (calls%reportEveryXKB) == 0) {
          if (Thread.currentThread().isInterrupted()) break;
          double mb = (Math.round(numWritten/1024.0/1024.0*10.0)/10.0);
          //if (ProgressBar!=null) guiOperations.SetProgressBarVALUEThreadlike((int)numWritten, true, ProgressBar);
          //if (StatusLabel!=null) guiOperations.SetStatusLabelThreadlike(statusLabelText + "(" + mb + " MB)", StatusLabel);
          //System.out.println(statusLabelText + "(" + mb + " MB)");
          //System.out.println((Math.round(numWritten/1024/1024)));
          
          progress.DisplayBar(String.format("(%s MB)", mb));
        }
        
        out.write(buffer, 0, numRead);
        numWritten += numRead;
        calls++;
      }
      //System.out.println(address + " \t " + calls  + " \t " + (numWritten/1024.0/1024.0));
      
    } finally {
      try {
        if (in != null) in.close();
        if (out != null) out.close();
        if (conn != null) conn = null;
      } catch (IOException ioe) {}
    }
    return;
  }
  
  /**
   * 
   * @param address
   * @param localFileName
   * @return localFileName File might get renamed and moved to the
   * systems temp directory if no write access to specified folder is
   * granted. In these cases, the return value is the new file path.
   */
  public static String download(String address, String localFileName) {
    OutputStream out = null;
    Integer targetFileSize = null;
    
    // If file already exists, look if it is complete and skip re-downloading it.
    try {
      if (new File(localFileName).exists()) {
        URL url = new URL(address);
        if (targetFileSize==null) targetFileSize = url.openConnection().getContentLength();
        // REMARK: targetFileSize is often -1 if server can not give this info
        if (targetFileSize!=null && targetFileSize.longValue() ==new File(localFileName).length()) {
          System.out.println("File already exists and file length matches. Not downloading it again.");
          return localFileName;
        }
      }
    } catch (Throwable t2) {
      // Doesn't matter.
    }
    
    // Try to open stream as given filename
    try {
      out = new BufferedOutputStream(new FileOutputStream(localFileName));
    } catch (Throwable t) {
      // Try to open stream in official system tempDir.
      final String tempDir = System.getProperty("java.io.tmpdir");
      localFileName = tempDir + (tempDir.endsWith(File.separator)?"":File.separator) + new File(localFileName).getName();
      
      // Check if we before performed this fallback to the temp dir
      // and downloaded the file already
      // If file already exists, look if it is complete and skip re-downloading it.
      try {
        if (new File(localFileName).exists()) {
          URL url = new URL(address);
          if (targetFileSize==null) targetFileSize = url.openConnection().getContentLength();
          // REMARK: targetFileSize is often -1 if server can not give this info
          if (targetFileSize!=null && targetFileSize.longValue() ==new File(localFileName).length()) {
            System.out.println("File already exists and file length matches. Not downloading it again.");
            return localFileName;
          }
        }
      } catch (Throwable t2) {
        // Doesn't matter.
      }
      
      try {
        out = new BufferedOutputStream(new FileOutputStream(localFileName));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    
    downloadWithoutErrors(address, out);
    
    return localFileName;
  }
  
  private static void downloadWithoutErrors(String address, OutputStream out) {
    int retry = 0;
    while (retry<3) {
      try {
        
        download(address, out);
        break;
        
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        break;
      } catch (Exception exception) {
        exception.printStackTrace();
        if (retry >=2) {
          exception.printStackTrace();
          break;
        }
        retry++;
      }
    }
  }
  
  /**
   * 
   * @param address
   * @return
   */
  public static boolean isHTMLcontent(String address) {
    URLConnection conn = null;
    InputStream  in = null;
    
    int retry = 0;
    boolean ret = false;
    while (true) {
      if (ret) break;
      try {
        URL url = new URL(address);
        int status = 0;
        if (address.toLowerCase().startsWith("http:"))
          status = ((HttpURLConnection) url.openConnection()).getResponseCode();
        else //if (address.toLowerCase().startsWith("ftp:"))
          url.openConnection(); //status = ((URLConnection) url.openConnection()).getResponseCode();
        
        if (status>=400) {
          System.out.println("Failed: HTTP error (code " + status + ").");          
          break; //404 und sowas ... >400 nur error codes. Normal:200 =>OK
        }
        FileDownload.status = status;
        conn = url.openConnection();
        
        in = conn.getInputStream();
        
        byte[] buffer = new byte[1024];
        int reads = 0;
        while ((in.read(buffer)) != -1) {
          String read = new String(buffer).trim();
          //if (read.contains("HTML") && read.contains(">") && read.contains("<")) return true;
          if (read.toUpperCase().replace(" ", "").contains("<HTML>")) {ret=true; break;}
          if ((reads++) >3 ) break; // Nur "peeken" nicht komplette datei lesen.
        }
        
        break;
      } catch (Exception exception) {
        if (retry >=2) {
          exception.printStackTrace();
          break;
        }
        retry++;
        
      } finally {
        try {
          if (in != null) in.close();
          if (conn != null) conn = null;
        } catch (IOException ioe) {}
        if (retry >=2 || ret) break;
      }
    }
    
    return ret;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(isHTMLcontent("http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=GSE9786"));
    System.out.println(isHTMLcontent("http://rsat.ulb.ac.be/rsat/data/genomes/Arabidopsis_thaliana/oligo-frequencies/5nt_upstream-noorf_Arabidopsis_thaliana-ovlp-2str.freq.gz"));
//    if (true) return;
//    
//    
//    download("http://rsat.ulb.ac.be/rsat/data/genomes/Arabidopsis_thaliana/oligo-frequencies/5nt_upstream-noorf_Arabidopsis_thaliana-ovlp-2str.freq.gz");
//    
//    for (int i = 0; i < args.length; i++) {
//      download(args[i]);
//    }
  }
  
  /**
   * 
   * @param o
   */
  public static void setProgressBar(Object o) {
    ProgressBar = o;
  }
  
  /**
   * 
   * @param o
   */
  public static void setStatusLabel(Object o) {
    StatusLabel = o;
  }
  
}

