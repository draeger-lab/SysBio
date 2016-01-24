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
package de.zbit.dbfetch;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Pattern;

import de.zbit.util.StringUtil;
import de.zbit.util.logging.LogUtil;

/**
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class RefSeqFetcher extends DBFetcher {
  private static final long serialVersionUID = -2537846063039628280L;
  
  private String format;
  /**
   * WSDBfetch has two RefSeq adapters:
   * One for nucleotides ("refseqn") and
   * one for proteins ("refseqp").
   */
  private boolean protein;
  
  /**
   * Cretes a new RefSeq WSDBFetch adapter for nucleotides
   * with maximal capacity of 10000.
   */
  public RefSeqFetcher() {
    this(10000, false);
  }
  
  /**
   * 
   * @param cacheSize - maximum capacity.
   * @param protein - if trua, a RefSeq Protein adapter will be
   * created. Else, a RefSeq Nucleotide (e.g., NM_*) adapter
   * will be created.
   */
  public RefSeqFetcher(int cacheSize, boolean protein) {
    super(cacheSize);
    setFormat("default");
    setStyle(Style.RAW);
    this.protein = protein;
  }
  
  @Override
  public String getCheckStrFromInfo(String info) {
    /* Get the following part
     * ACCESSION   NM_001173173
     * VERSION     NM_001173173.1  GI:290563407
     */
    int start = info.indexOf("ACCESSION");
    if (start<0) return info;
    int pos = info.indexOf("VERSION", start);
    int end = info.indexOf('\n', pos>=0?pos:start);
    if (end<0) end = info.length();
    info = info.substring(start, end).toUpperCase();
    
    return info;
  }
  
  @Override
  public String getEntrySeparator() {
    return Pattern.quote("\nLOCUS");
  }
  @Override
  public String getAppendAtEnd() {
    return "\n";
  }
  @Override
  public String getAppendAtStart() {
    return "LOCUS";
  }

  @Override
  public String getDbName() {
    return protein?"refseqp":"refseqn";
  }

  @Override
  public String getFormat() {
    return format;
  }

  @Override
  public void setFormat(String format) {
    this.format = format;
  }

  @Override
  public boolean matchIDtoInfo(String id, String toCheck) {
    
    // Search for exact id match
    id = id.toUpperCase().trim();
    boolean matches = StringUtil.isWord(toCheck, id);
    
    // Trim version number from id and search again
    // (adapter may return a newer version of the same accession).
    if (!matches) {
      int index = id.indexOf('.');
      if( index > 0 ) {
        id = id.substring(0, index);
        matches = StringUtil.isWord(toCheck, id);
      }
    }
    
    return matches;
  }
  
  
  public static void main(String[] args) {
    LogUtil.initializeLogging(Level.ALL);
    RefSeqFetcher fetch = new RefSeqFetcher();
    
    //String result = fetch.getInformation("NM_173173");
    String[] results = fetch.getInformations(new String[]{"NM_001173173.1", "NW_001731730.1"});
    
    System.out.println(Arrays.toString(results));
    
  }
}
