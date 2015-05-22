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
package de.zbit.io.fileformat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import de.zbit.io.OpenFile;

/**
 * Static helper class that tries to identify the file format for a given file
 * or byte array representing the first bytes of a file. <h3>Usage</h3>
 * 
 * <pre>
 * FormatDescription desc = FormatIdentification
 *     .identify(new File(&quot;testfile.zip&quot;));
 * if (desc != null) {
 *   System.out.println(desc.getShortName());
 * }
 * </pre>
 * 
 * @author Marco Schmidt
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class FormatIdentification {
  
  /**
   * 
   */
  private static List<FormatDescription> descriptions;
  /**
   * 
   */
  private static int minBufferSize;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(FormatIdentification.class.getName());
  
  static {
    init();
  }
  
  /**
   * Private empty constructor to avoid instantiations.
   */
  private FormatIdentification() {
  }
  
  /**
   * Liest benoetigte bytes aus stream und resetted stream nach identifikation
   * @param ret
   * @return
   * @throws IOException
   */
  public static FormatDescription identify(BufferedInputStream ret) throws IOException {
    if (ret.markSupported()) {
      ret.mark(minBufferSize+2);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int s; int bytesRead = 0;
    while (((s = ret.read()) != -1) && (bytesRead < (minBufferSize+1))) {
      out.write(s);
      bytesRead++;
    }
    if (ret.markSupported()) {
      ret.reset();
    }
    return identify(out.toByteArray());
  }
  
  /**
   * Liest soviele bytes aus dem Reader, wie f&uuml;r identifikation n&ouml;tig, identifiziert und resettet den Stream wieder.
   * @param ret
   * @return
   * @throws IOException
   */
  public static FormatDescription identify(BufferedReader ret) throws IOException {
    if (ret.markSupported()) {
      ret.mark(minBufferSize+2);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int s; int bytesRead=0;
    while ((s = ret.read()) != -1 && bytesRead<(minBufferSize+1)) {
      out.write(s);
      bytesRead++;
    }
    if (ret.markSupported()) {
      ret.reset();
    }
    return identify(out.toByteArray());
  }
  
  /**
   * @param data
   * @return
   */
  public static FormatDescription identify(byte[] data) {
    if (data == null || data.length < 1) {
      //System.out.println("Too less data for identification.");
      return null;
    }
    Iterator<FormatDescription> iter = descriptions.iterator();
    while (iter.hasNext()) {
      FormatDescription desc = iter.next();
      if (desc.matches(data)) {
        return desc;
      }
    }
    return null;
  }
  
  /**
   * 
   * @param file
   * @return
   */
  public static FormatDescription identify(File file) {
    if (!file.isFile()) {
      //System.out.println("File " + file.getName() + " is no file.");
      return null;
    }
    long size = file.length();
    int numBytes;
    if (size > minBufferSize) {
      numBytes = minBufferSize;
    } else {
      numBytes = (int) size;
    }
    byte[] data = new byte[numBytes];
    RandomAccessFile in = null;
    try {
      in = new RandomAccessFile(file, "r");
      in.readFully(data);
      in.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return null;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ioe) {}
    }
    
    return identify(data);
  }
  
  /**
   * 
   * @param inn
   * @return
   */
  public static FormatDescription identify(ByteArrayInputStream inn) {
    long size = inn.available();
    int numBytes;
    if (size > minBufferSize) {
      numBytes = minBufferSize;
    } else {
      numBytes = (int) size;
    }
    byte[] data = new byte[numBytes];
    
    try {
      inn.read(data);
    } catch (IOException e) {e.printStackTrace();}
    /*
    RandomAccessFile in = null;
    try {
      in = new RandomAccessFile(file, "r");
      in.readFully(data);
      in.close();
    } catch (IOException ioe) {
      return null;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ioe) {
        //
      }
    }*/
    return identify(data);
  }
  
  /**
   * 
   */
  private static void init() {
    //String INfilename = "data/formats.txt";
    String INfilename = "de/zbit/io/formats.txt";
    descriptions = new ArrayList<FormatDescription>();
    minBufferSize = 1;
    try {
      InputStream input = null;
      if (new File(INfilename).exists()) {
        input = new FileInputStream(INfilename);
      } else if (FormatIdentification.class.getClassLoader().getResource(INfilename) != null) {
        input = FormatIdentification.class.getClassLoader().getResource(INfilename).openStream();
      } else if (FormatIdentification.class.getClassLoader().getResource(new File(INfilename).getName() ) != null) {
        input = FormatIdentification.class.getClassLoader().getResource(new File(INfilename).getName()).openStream();
      }
      
      if (input == null) {
        logger.warning("Could not load format identification magic byte file.");
        return;
      }
      FormatDescriptionReader in = new FormatDescriptionReader(
        new InputStreamReader(input));
      FormatDescription desc;
      while ((desc = in.read()) != null) {
        byte[] magic = desc.getMagicBytes();
        Integer offset = desc.getOffset();
        if (magic != null && offset != null
            && offset.intValue() + magic.length > minBufferSize) {
          minBufferSize = offset.intValue() + magic.length;
        }
        descriptions.add(desc);
      }
      input.close();
    } catch (Exception e) {
    }
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    String[] test = new String[]{"C:\\dataset_99.dat", "C:\\pareto1.dat", "C:\\Fpareto1.zip", "C:\\pareto1.txt", "C:\\GSE15239_series_matrix.txt.gz", "c:\\foo.tar.gz", "c:\\foo.tar.bz2"};
    
    for (String filename: test) {
      de.zbit.io.fileformat.FormatDescription desc = FormatIdentification.identify(new File(filename));
      if (desc==null) {
        System.out.println("Unknown");
      } else if (desc.getShortName().equalsIgnoreCase("GZ") ) {
        System.out.println("GZ");
      } else if (desc.getShortName().equalsIgnoreCase("ZIP") ) {
        System.out.println("ZIP");
      } else if (desc.getShortName().equalsIgnoreCase("BZ2") ) {
        System.out.println("BZ2");
      } else if (desc.getShortName().equalsIgnoreCase("TAR") ) {
        System.out.println("TAR");
      }
      
    }
    
    for (String filename: test) {
      System.out.println("\n"+filename+"\n===================");
      BufferedReader in = OpenFile.openFile(filename);
      try {
        int i=0;
        while (in.ready() && (i++)<10) {
          System.out.println(in.readLine());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
  }
  
  
}
