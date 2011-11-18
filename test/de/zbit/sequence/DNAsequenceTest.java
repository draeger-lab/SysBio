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
package de.zbit.sequence;

import java.io.IOException;

import de.zbit.util.Timer;

/**
 * Function and performance tests of {@link DNAsequence} class.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class DNAsequenceTest {
  
  public static void main(String[] args) throws IOException, InterruptedException {
    int testCount = 30000000; // number of dna characters to add
    
    
    System.out.println("Comparing String storage of DNA sequences with DNAsequence-class storage of DNA sequences:");
    System.out.println("Evaluating with DNA sequence of " + testCount + " base pairs.");
    
    
    System.out.println("\nEvaluating DNAsequence class:");
    Timer t = new Timer();
    DNAsequence seq = new DNAsequence(testCount);
    for (int i=0;i<testCount;i++) {
      seq.append('a');
    }
    System.out.println("Time: " + t.getNiceAndReset());
    System.out.println("Memory usage: " + Math.round(((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024.0/1024.0*100.0))/100.0  + " MB");
    System.out.println("In-Memory object is stable. Now trying toString() Method:");
    seq.toString();
    System.out.println("To-String time: " + t.getNiceAndReset());
    System.gc();
    Thread.currentThread().sleep(1000);
    
    
    seq = null;
    System.gc();
    Thread.currentThread().sleep(1000);
    System.out.println("\nEvaluating StringBuilder class:");
    t = new Timer();
    StringBuilder seq2 = new StringBuilder();
    for (int i=0;i<testCount;i++) {
      seq2.append('A');
    }
    System.out.println("Time: " + t.getNiceAndReset());
    System.out.println("Memory usage: " + Math.round(((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024.0/1024.0*100.0))/100.0  + " MB");
    System.out.println("In-Memory object is stable. Now trying toString() Method:");
    seq2.toString();
    System.out.println("To-String time: " + t.getNiceAndReset());
    System.gc();
    Thread.currentThread().sleep(1000);
    
  }
  
}
