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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.zbit.exception.CorruptInputStreamException;
import de.zbit.io.csv.CSVwriteable;
import de.zbit.io.csv.CSVwriteableIO;

/**
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class CSVwriteableTest1 implements CSVwriteable {  
  String element1;
  int element2;
  
  public CSVwriteableTest1() {super();}
  public CSVwriteableTest1(String e, int e2) {
    this();
    this.element1=e;
    this.element2=e2;
  }
  
  public String toString() {
    return "["+element1 + ", " + element2+"]";
  }
  
  public void fromCSV(String[] elements, int elementNumber, int CSVversionNumber) throws CorruptInputStreamException {
    // do the reverse of toCSV()
    element1 = elements[0];
    element2 = Integer.parseInt(elements[1]);
  }
  
  public int getCSVOutputVersionNumber() {
    // Any version number to be compatible with previous releases
    return 0;
  }
  
  public String toCSV(int elementNumber) {
    // Create a tab separated string of all elements
    return element1+"\t"+element2;
  }
  
  
  
  /**
   * Demonstrates usage of {@link CSVwriteable}.
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    
    // Create an example list of a CSVwriteable object
    List<CSVwriteableTest1> l = createExampleList();
    
    // Write list as CSV
    CSVwriteableIO.write(l, "test1.txt");
    //XXX: Now go ahead and take a look at this file.
    
    // Read the CSV directly into the old data structure
    List<CSVwriteableTest1> l2 = (List<CSVwriteableTest1>) CSVwriteableIO.read("test1.txt");
    // Hey, it's a LinkedList again! (=> See and change createExampleList())
    System.out.println(l2.getClass().getName());
    for (CSVwriteableTest1 t: l2)
      System.out.println(t);
    
    
    
    // Now, let's retry with an array
    CSVwriteableTest1[] a = createExampleArray();
    
    // Write list as CSV
    CSVwriteableIO.write(a, "test2.txt");
    //XXX: Now go ahead and take a look at this file.
    
    // Read the CSV directly into the old data structure
    CSVwriteableTest1[] a2 =  (CSVwriteableTest1[]) CSVwriteableIO.read("test2.txt");
    // Hey, it's an array of CSVwriteableTest1 again!
    System.out.println(a2.getClass().getName());
    for (CSVwriteableTest1 t: a2)
      System.out.println(t);
    
  }

  /**
   * @return an example list
   */
  public static List<CSVwriteableTest1> createExampleList() {
    List<CSVwriteableTest1> ret = new LinkedList<CSVwriteableTest1>();
    for (int i=0; i<25; i++) {
      ret.add(new CSVwriteableTest1("test"+i,i));
    }
    return ret;
  }

  /**
   * @return an example array
   */
  public static CSVwriteableTest1[] createExampleArray() {
    CSVwriteableTest1[] ret = new CSVwriteableTest1[25];
    for (int i=0; i<25; i++) {
      ret[i] = new CSVwriteableTest1("test"+i,i);
    }
    return ret;
  }

  
}
