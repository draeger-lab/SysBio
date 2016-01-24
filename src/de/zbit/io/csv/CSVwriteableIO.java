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
package de.zbit.io.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.zbit.exception.CorruptInputStreamException;
import de.zbit.util.ArrayUtils;
import de.zbit.util.Reflect;
import de.zbit.util.StringUtil;

/**
 * Read and write object, using the {@link CSVwriteable} interface.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class CSVwriteableIO {
  public static final transient Logger log = Logger.getLogger(CSVwriteableIO.class.getName());
  
  /**
   * Write an {@link CSVwriteable} object to a CSV file.
   * @param object
   * @param outputfile
   * @throws IOException
   */
  public static void write(CSVwriteable object, String outputfile) throws IOException {
    // init
    File out = CSVWriter.getOrCreateFile(outputfile);
    String lineSep = StringUtil.newLine();
    Writer w = initializeWriter(out);
    
    // Preamble
    w.append("#" + object.getClass().getName()+ lineSep);
    w.append("#" + object.getCSVOutputVersionNumber()+ lineSep);
    
    // Write object
    int i=0;
    String line;
    while ((line = object.toCSV((i++)))!=null) {
      w.append(line);
      w.append(lineSep);
    }
    
    w.close();
  }
  
  /**
   * Write any {@link Iterable} object (e.g., a {@link List} or {@link Collection})
   * that contains {@link CSVwriteable} elements to a CSV file.
   * @param object
   * @param outputfile
   * @throws IOException
   */
  public static void write(Iterable<? extends CSVwriteable> object, String outputfile) throws IOException {
    // init
    File out = CSVWriter.getOrCreateFile(outputfile);
    String lineSep = StringUtil.newLine();
    Writer w = initializeWriter(out);
    
    Object exampleItem = object.iterator().hasNext()?object.iterator().next():null;
    
    // Preamble
    w.append("#" + object.getClass().getName()+"|");
    if (exampleItem!=null) {
      w.append(object.iterator().next().getClass().getName());
    } else {
      w.append(String.class.getName()); // Empty lists default to string...
    }
    w.append(lineSep);
    w.append("#" + ((CSVwriteable)exampleItem).getCSVOutputVersionNumber()+ lineSep);
    
    // Write object
    Iterator<? extends CSVwriteable> it = object.iterator();
    while ( it.hasNext() ) {
      w.append(it.next().toCSV(0));
      w.append(lineSep);
    }
    
    w.close();
  }
  
  /**
   * Write an array of {@link CSVwriteable} elements to a CSV file.
   * @param <T>
   * @param object
   * @param outputfile
   * @throws IOException
   */
  public static <T extends CSVwriteable> void write(T[] object, String outputfile) throws IOException {
    // init
    File out = CSVWriter.getOrCreateFile(outputfile);
    String lineSep = StringUtil.newLine();
    Writer w = initializeWriter(out);
    
    Object exampleItem = (object!=null && object.length>0)?object[0]:null;
    
    // Preamble
    w.append("#" + object.getClass().getName()+"|");
    if (exampleItem!=null) {
      w.append(exampleItem.getClass().getName());
    } else {
      w.append(String.class.getName()); // Empty lists default to string...
    }
    w.append(lineSep);
    w.append("#" + ((CSVwriteable)exampleItem).getCSVOutputVersionNumber()+ lineSep);
    
    // Write object
    for (int i=0; i<object.length; i++) {
      w.append(object[i].toCSV(0));
      w.append(lineSep);
    }
    
    w.close();
  }
  
  
  @SuppressWarnings("unchecked")
  /**
   * Automatically detects the input type and writes
   * an appropriate CSV file.
   */
  public static void write(Object object, String outputfile) throws IOException {
    if (object instanceof CSVwriteable || CSVwriteable.class.isAssignableFrom(object.getClass())) {
      write((CSVwriteable)object,outputfile);
    } else if (object instanceof Iterable || Iterable.class.isAssignableFrom(object.getClass())) {
      write((Iterable<? extends CSVwriteable>)object,outputfile);
    } else if (object.getClass().isArray()) {
      write((CSVwriteable[])object, outputfile);
    } else {
      log.log(Level.SEVERE,"Dont't know how to write " + object.getClass().getName() + " as CSV.");
    }
  }
  
  
  
  
  
  
  
  
  
  
  
  
  /**
   * Read an {@link CSVwriteable} object from a CSV file.
   * @param inputfile
   * @return Either a {@link CSVwriteable} or an array or collection of
   * {@link CSVwriteable}. This depends on the inputfile.
   * @throws IOException
   */
  public static Object read(String inputfile) throws IOException {
    return read(null, inputfile);
  }
  /**
   * Read an {@link CSVwriteable} object from a CSV file.
   * @param object - empty template
   * @param inputfile
   * @return Either a {@link CSVwriteable} or an array or collection of
   * {@link CSVwriteable}. This depends on the inputfile.
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public static Object read(CSVwriteable emptyObject, String inputfile) throws IOException {
    // Init the reader
    CSVReader r = new CSVReader(inputfile,false);
    //r.setSeparatorChar('\t');
    r.open();
    
    // Check class name and get version
    String[] splitt = r.getPreamble().split("\n");
    for (int i=0;i<splitt.length; i++)
      splitt[i]=splitt[i].substring(1).trim(); // Remove the #
    
    int CSVversionNumber = Integer.parseInt(splitt[1]);
    
    String superClassName = null; // java.lang.array or a list or something
    String CSVwriteableClassName = splitt[0];
    if (CSVwriteableClassName.contains("|")) {
      splitt = CSVwriteableClassName.split(Pattern.quote("|"));
      superClassName = splitt[0];
      CSVwriteableClassName = splitt[1];
    }
    
    if (emptyObject==null) {
      // Create new instance
      try {
        emptyObject = (CSVwriteable) Class.forName(CSVwriteableClassName).newInstance();
      } catch (Exception e) {
        throw new CorruptInputStreamException("Could not create source class from CSV file.", e);
      }
    } else {
      if (!emptyObject.getClass().getName().equals(CSVwriteableClassName)) {
        System.err.println("WARNING: Trying to read " + CSVwriteableClassName + " into " + emptyObject.getClass().getName());
      }
    }
    
    
    // Read the CSV content
    int i=0;
    String[] line;
    if (superClassName==null) {
     // Read the single object
      while ((line=r.getNextLine())!=null) {
        emptyObject.fromCSV(line, i++, CSVversionNumber);
      }
      return emptyObject;
      
    } else {
      // Create array, list or Iterable element and add objects.
      Class superC=null;
      Object ret=null;
      try {
        superC = Class.forName(superClassName);
      } catch (ClassNotFoundException e) {
        throw new CorruptInputStreamException("Could not create source super class from CSV file.", e);
      }
      
      // Try to rebuild source data structure (arrays are treated later)
      if (Collection.class.isAssignableFrom(superC)) {
        // Must be a list or something... Try to get an instance
        try {
          ret = superC.newInstance();
        } catch (Throwable t) {
          log.log(Level.WARNING, "Could not create instance of '"+superC.getName()+"'. Will create another collection.", t);
        }
      }
      if (ret==null) {
        ret = initDefaultCollection();
      }
      
      // Fill the list
      while ((line=r.getNextLine())!=null) {
        CSVwriteable e;
        try {
          e = createNewCSVwriteableObject(emptyObject);
        } catch (Exception e1) {
          throw new CorruptInputStreamException("Could not create new instance of emptyObject. Please implement a clone() method.", e1);
        }
        e.fromCSV(line, 0, CSVversionNumber);
        ((Collection)ret).add(e);
      }
      
      // If source was an array, try to reconstruct this
      if (superC.isArray()) {
        try {
          return ((Collection)ret).toArray(ArrayUtils.createArray(emptyObject, 0));
        } catch (Throwable e) {
          return ((Collection)ret).toArray();
        }
      } else {
        return ret;
      }
      
    }
  }

  /**
   * Tries to create a new instance of a {@link CSVwriteable} object.
   * @param emptyObject
   * @return 
   * @throws Exception - if no new instance or clone can be created. 
   */
  private static CSVwriteable createNewCSVwriteableObject(CSVwriteable emptyObject) throws Exception {
    
    // Try to clone
    Object clone = Reflect.invokeIfContains(emptyObject, "clone");
    if (clone!=null) return (CSVwriteable) clone;
    
    // Try reflection
    return emptyObject.getClass().newInstance();
  }

  /**
   * Defines the type of {@link Collection} that should
   * be returned by default, if the input CSV file
   * was a Collection/List of {@link CSVwriteable} elements
   * and the original Type could not be reconstructed.
   * @return
   */
  @SuppressWarnings("unchecked")
  private static Collection initDefaultCollection() {
    return new ArrayList();
  }
  
  /**
   * Create a writer from a file.
   * @param file
   * @return
   * @throws IOException
   */
  private static Writer initializeWriter(File file) throws IOException {
    return new BufferedWriter(new FileWriter(file));
  }
  
}
