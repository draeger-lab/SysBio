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
package de.zbit.io;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Various tools for the {@link Serializable} interface.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class SerializableTools {

  /**
   * Load a serializable object.
   * @param file
   * @return the loaded object or null if it failed.
   */
  public static Object loadObject(File file) {
    try {
      FileInputStream fileIn = new FileInputStream(file);
      BufferedInputStream bIn = new BufferedInputStream(fileIn);
      Object o = loadObject(bIn);
      fileIn.close();
      return o;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Load a serializable object.
   * Does NOT buffer the stream. Make sure to pipe your
   * stream though a BufferedStream for more performance
   * (e.g. BufferedInputStream). 
   * @param inn
   * @return the loaded object or null if it failed.
   */
  public static Object loadObject(InputStream inn) {
    try {
      ObjectInputStream in = new ObjectInputStream(inn);
      Object ret = in.readObject();
      in.close();
      inn.close();
      return ret;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Load a serializable object.
   * Does NOT buffer the stream. Make sure to pipe your
   * stream though a BufferedStream for more performance
   * (e.g. BufferedInputStream). 
   * @param inn
   * @return the loaded object or null if it failed.
   * @throws IOException 
   * @throws ClassNotFoundException 
   */
  public static Object loadObjectAndThrowExceptions(InputStream inn) throws IOException, ClassNotFoundException {
    ObjectInputStream in = new ObjectInputStream(inn);
    Object ret = in.readObject();
    in.close();
    inn.close();
    return ret;
  }

  /**
   * Load a serializable object.
   * @param filename
   * @return the loaded object or null if it failed.
   */
  public static Object loadObject(String filename) {
    return loadObject(new File(filename));
  }

  /**
   * Load a serializable object from a gzipped file.
   * @param file (String, File, FileDescriptor or InputStream).
   * @return the loaded object or null if it failed.
   */
  public static Object loadGZippedObject(Object infile) {
    Object ret = null;
    if (infile==null) return ret;
    
    try {
      InputStream fileIn;
      if (infile instanceof String) {
        fileIn = new FileInputStream((String)infile);
      } else if (infile instanceof File) {
        fileIn = new FileInputStream((File)infile);
      } else if (infile instanceof FileDescriptor) {
        fileIn = new FileInputStream((FileDescriptor)infile);
      } else if (infile instanceof InputStream) {
        fileIn = ((InputStream)infile);
      } else {
        throw new IOException("Unsupported input file object: "+infile.getClass().getName());
      }
      BufferedInputStream bIn = new BufferedInputStream(fileIn);
      
      GZIPInputStream gzIn = new GZIPInputStream(bIn);
      
      ret = loadObject((InputStream)gzIn);
      
      bIn.close();
      if (fileIn instanceof Closeable) ((Closeable)fileIn).close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return ret;
  }

  /**
   * Load a serializable object from a gzipped or normal file
   * (is infered automatically).
   * @param file (String, File, FileDescriptor or InputStream).
   * @return the loaded object or null if it failed.
   * @throws IOException 
   */
  public static Object loadObjectAutoDetectZIP(Object serializedFile) throws IOException {
    
    Object ret = null;
    if (serializedFile==null) return ret;
    
    // Create input stream
    InputStream fileIn;
    if (serializedFile instanceof String) {
      fileIn = OpenFile.searchFileAndGetInputStream((String)serializedFile);//new FileInputStream((String)serializedFile);
    } else if (serializedFile instanceof File) {
      fileIn = new FileInputStream((File)serializedFile);
    } else if (serializedFile instanceof FileDescriptor) {
      fileIn = new FileInputStream((FileDescriptor)serializedFile);
    } else if (serializedFile instanceof InputStream) {
      fileIn = ((InputStream)serializedFile);
    } else {
      throw new IOException("Unsupported input file object: "+serializedFile.getClass().getName());
    }
    
    // Create buffered Stream to be able to read and reset the magic bytes
    BufferedInputStream bin = new BufferedInputStream(fileIn);
    bin.mark(20); // Set mark to be able to reset if magic bytes don't match.
    
    // Look if it is an GZipped Object
    InputStream in=null;
    try {
      in = new GZIPInputStream(bin);
    } catch (IOException e) {
      // java.io.IOException: Not in GZIP format
      in=null;
      bin.reset();
    }
    
    // Take directly the source input stream, if it is no GZstream
    if (in==null) in = bin;
    
    try {
      ret = loadObjectAndThrowExceptions(in);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      ret=null;
    }
    
    bin.close();
    fileIn.close();
    
    return ret;
  }

  /**
   * Saves and GZipps a serializable object.
   * @param filename - the file to save the object to.
   * Please make sure by yourself, that this filename ends with ".gz".
   * @param obj - the object to save.
   * @return true if and only if saving was succesfull.
   */
  public static boolean saveGZippedObject(String filename, Object obj) {
    try {
      if (filename.contains("/")) {
        new File(filename.substring(0, filename.lastIndexOf('/'))).mkdirs();
      }
    } catch (Throwable t) {};
    try {
      FileOutputStream fileOut = new FileOutputStream(filename);
      GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
      ObjectOutputStream out = new ObjectOutputStream(gzOut);
      out.writeObject(obj);
      out.close();
      gzOut.close();
      fileOut.close();
      return true;
    } catch(FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * The same as {@link #saveGZippedObject(String, Object)} but does not catch
   * any errors.
   * @param filename
   * @param obj
   * @throws IOException 
   */
  public static void saveGZippedObjectAndThrowErrors(String filename, Object obj) throws IOException {
    FileOutputStream fileOut = new FileOutputStream(filename);
    GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
    ObjectOutputStream out = new ObjectOutputStream(gzOut);
    out.writeObject(obj);
    out.close();
    gzOut.close();
    fileOut.close();
  }

  /**
   * Saves a serializable object.
   * @param filename - the file to save the object to.
   * @param obj - the object to save.
   * @return true if and only if saving was succesfull.
   */
  public static boolean saveObject(String filename, Object obj) {
    try {
      FileOutputStream fileOut = new FileOutputStream(filename);
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(obj);
      out.close();
      fileOut.close();
      return true;
    } catch(FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
  
}
