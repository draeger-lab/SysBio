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
package de.zbit.io;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.zbit.exception.CorruptInputStreamException;
import de.zbit.util.SortedArrayList;

/**
 * This is an example to help you using the {@link CSVwriteable} interface.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class CSVwriteableTest2 {
  
  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    CSVwriteableTest2 test = new CSVwriteableTest2();
    test.CSVwritableTest();
    test.CSVreadableTest();
  }
  
  /**
   * Creates an example class and writes the content as CSV
   * @throws IOException
   */
  public void CSVwritableTest() throws IOException {
    
    // Create new example class with 10 miRNAs and 10 targets each.
    miRNAtargets t_all = new miRNAtargets();
    for (int i=0; i<100; i++) {
      t_all.addTarget(Integer.toString(i%10), new miRNAtarget(i,false,"Source-"+i,i));
    }
    System.out.println("Initial size: " + t_all.size());
    
    // Write to hard disk as CSV
    CSVwriteableIO.write(t_all, "test.txt");
    
    // XXX: Now go ahead and open the file in a text editor!
  }
  
  /**
   * Reads an object from a CSV file, created with {@link #CSVwritableTest()}
   * @throws IOException
   */
  public void CSVreadableTest() throws IOException {
    // Read class from CSV
    miRNAtargets t_all = new miRNAtargets();
    t_all = (miRNAtargets) CSVwriteableIO.read(t_all, "test.txt");
    
    // XXX: REMARK: If miRNAtargets would not be a nested class and
    // contains an ampty constructor, the following is more
    // convenient:
    // miRNAtargets t_all = (miRNAtargets) CSVReader.read("test.txt");
    
    System.out.println("Size after reading from CSV: " + t_all.size());
  }
  
  
  
  
  
  
  
  /*
   * XXX: The following are classes, used in the Example. The only intresting
   * methods should be the *toCSV* and *fromCSV* methods!!!
   */
  
  
  
  
  /**
   * An EXAMPLE PARTIAL class to hold miRNA and collections of targets.
   * @author Clemens Wrzodek
   */
  public class miRNAtargets implements CSVwriteable {
    
    /**
     * A mapping from miRNAs (official identifiers as string) to
     * a list of targets.
     */
    private Map<String, Collection<miRNAtarget>> targets;
    
    /** Intermediate variable that is required by {@link #toCSV(int)}. */
    private Iterator<String> currentCSVElement = null;
    
    public miRNAtargets() {}

    /**
     * Adds a target to the miRNA.  
     * @param miRNA - official name of the miRNA.
     * @param target - NCBI Gene ID (Entrez).
     */
    public void addTarget(String miRNA, miRNAtarget target) {
      if (targets==null) initializeTargets();
      
      Collection<miRNAtarget> ctargets = targets.get(miRNA);
      if (ctargets==null) {
        ctargets = initializeTargetCollection();
        targets.put(miRNA, ctargets);
      }
      
      if (!ctargets.contains(target)) {
        ctargets.add(target);
      }
    }
    
    /**
     * @return a Collection<miRNAtarget>.
     */
    private Collection<miRNAtarget> initializeTargetCollection() {
      Collection<miRNAtarget> ret = new SortedArrayList<miRNAtarget>();
      return ret;
    }

    /**
     * Inizializes {@link #targets}, if it is null.
     */
    private void initializeTargets() {
      if (targets==null) targets = new HashMap<String, Collection<miRNAtarget>>();
    }

    /**
     * @return Returns the number of miRNAs for which targets are available.
     */
    public int size() {
      return targets==null?0:targets.size();
    }


    /* (non-Javadoc)
     * @see de.zbit.io.CSVwriteable#fromCSV(java.lang.String[], int, int)
     */
    public void fromCSV(String[] elements, int elementNumber, int CSVversionNumber)
      throws CorruptInputStreamException {
      // Core for CSVversionNumber 0
      
      // Build array for miRNAtarget
      String[] elementsNew = new String[elements.length-2];
      System.arraycopy(elements, 2, elementsNew, 0, elementsNew.length);
      
      // Read version number of miRNAtarget
      int miRNAtargetCSVversionNumber = Integer.parseInt(elements[1]);
      
      // Build miRNAtarget
      miRNAtarget t = new miRNAtarget(0);
      t.fromCSV(elementsNew, 0, miRNAtargetCSVversionNumber);
      
      // Add to list
      addTarget(elements[0], t);
    }


    /* (non-Javadoc)
     * @see de.zbit.io.CSVwriteable#getCSVOutputVersionNumber()
     */
    public int getCSVOutputVersionNumber() {
      return 0;
    }

    
    /* (non-Javadoc)
     * @see de.zbit.io.CSVwriteable#toCSV(int)
     */
    public String toCSV(int elementNumber) {
      if (targets==null) return null;
      
      // Init output Iterator
      if (currentCSVElement==null) {
        currentCSVElement = targets.keySet().iterator();
      }
      // Terminate when iterator is through
      if (!currentCSVElement.hasNext()) {
        currentCSVElement=null;
        return null;
      }
      
      // Return next element
      StringBuffer ret = new StringBuffer();
      String miRNA = currentCSVElement.next();
      Collection<miRNAtarget> col = targets.get(miRNA);
      int i=0;
      for (miRNAtarget t: col) {
        if (i>0) ret.append("\n");
        ret.append(miRNA + "\t" + t.getCSVOutputVersionNumber() + "\t" + t.toCSV((i++)));
      }
      
      return ret.toString();
    }
    
  }
  
  /**
   * Holder EXAMPLE class to hold targets for an miRNA.
   * @author Clemens Wrzodek
   */
  public class miRNAtarget implements Comparable<miRNAtarget>, CSVwriteable {

    /** Target gene id */
    private int target;
    /** Experimental verified or predicted target */
    private boolean experimental;
    /** Source of evidence */
    private String source=null;
    /** A pValue or Score for this target. */
    private float pValue=Float.NaN;
    
    /**
     * Adds a new EXPERIMENTAL VERIFIED target.
     * @param target - GeneID of the target.
     */
    public miRNAtarget(int target) {
      super();
      this.target = target;
      experimental=true;
    }
    
    /**
     * Adds a new EXPERIMENTAL VERIFIED target.
     * @param target - GeneID of the target.
     * @param source - e.g. PubMed ID as evidence.
     */
    public miRNAtarget(int target, String source) {
      this(target);
      this.source=source;
    }
    
    /**
     * Adds a new target.
     * @param target - GeneID of the target.
     * @param experimental - false if it is a predicted target. Else: true.
     * @param source - e.g. PubMed ID or Name of prediction algorithm.
     * @param pValue - a pValue or score for this target. Please set to Float.NaN if not available.
     */
    public miRNAtarget(int target, boolean experimental, String source, float pValue) {
      this(target,source);
      this.experimental=experimental;
      this.pValue = pValue;
    }

    /**
     * @return the target
     * @see #target
     */
    public int getTarget() {
      return target;
    }

    /**
     * @return false if it is a predicted target. Else: true.
     * @see #experimental
     */
    public boolean isExperimental() {
      return experimental;
    }

    /**
     * @return the source
     * @see #source
     */
    public String getSource() {
      return source;
    }

    /**
     * @return the pValue
     * @see #pValue
     */
    public float getPValue() {
      return pValue;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(miRNAtarget o) {
      // Compare by target, isExperimental, Source and pValue.
      if (o instanceof miRNAtarget) {
        miRNAtarget t = (miRNAtarget)o;
        int r = target-t.getTarget();
        if (r==0) {
          if (isExperimental()&&!t.isExperimental()) return -1;
          if (!isExperimental()&&t.isExperimental()) return 1;
          
          r = source.compareTo(t.getSource());
          if (r==0 && !isExperimental()) {
            r = Float.compare(this.pValue, t.getPValue());
          }
        }
        
        return r;
      }
      return -1;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "[miRNAtarget " + target + (experimental?" experimental":" predicted") + 
      (source!=null?" source:\"" + source+"\"":"") + (Float.isNaN(pValue)?"":" pValueOrScore:" + pValue) + "]";
    }

    /* (non-Javadoc)
     * @see de.zbit.io.CSVwriteable#fromCSV(java.lang.String[], int, int)
     */
    public void fromCSV(String[] elements, int elementNumber, int CSVversionNumber)
      throws CorruptInputStreamException {
      // For Version 0:
      target = Integer.parseInt(elements[0]);
      experimental = Boolean.parseBoolean(elements[1]);
      source = elements[2];
      pValue = Float.parseFloat(elements[3]);
    }

    /* (non-Javadoc)
     * @see de.zbit.io.CSVwriteable#getCSVOutputVersionNumber()
     */
    public int getCSVOutputVersionNumber() {
      return 0;
    }

    /* (non-Javadoc)
     * @see de.zbit.io.CSVwriteable#toCSV(int)
     */
    public String toCSV(int elementNumber) {
      return target+"\t"+experimental+"\t"+source.replace("\t", " ")+"\t"+pValue;
    }
    
  }

  
}
