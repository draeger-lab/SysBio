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
package de.zbit.dbfetch;


/**
 * 
 * @author unknown
 * 
 * Fetches uniprot data, but doesn't save it like the UniProtWrapper!
 * @version $Rev$
 * @since 1.0
 */
public class UniProtFetcher extends DBFetcher {
  
  /**
   * 
   */
  private static final long serialVersionUID = -2640407060927083468L;
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    UniProtFetcher upf = new UniProtFetcher();
    
    // System.out.println(uw.getInformation("ALBU_HUMAN"));
    
    String[] anfrage = new String[] { "ZCH18_HUMAN", "1433B_HUMAN",
        "QUATSCH", "ZCH14_HUMAN" };
    String[] ret = upf.getInformations(anfrage);
    
    System.out.println("Rueckgabe:");
    for (int i = 0; i < ret.length; i++) {
      System.out.println(anfrage[i] + ":\n" + ret[i]
          + "\n==============================");
    }
  }
  
  /**
   * 
   */
  public UniProtFetcher() {
    this(1000);
  }
  
  /**
   * 
   * @param cacheSize
   */
  public UniProtFetcher(int cacheSize) {
    super(cacheSize);
    setStyle(Style.RAW);
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.dbfetch.DBFetcher#getCheckStrFromInfo(java.lang.String)
   */
  @Override
  public String getCheckStrFromInfo(String info) {
    // check every line until AC or everything if no AC line is there
    int endPos = info.indexOf("\n", info.indexOf("\nAC")+1);
    String toCheck = (endPos > 0) ? info.substring(0, endPos) : info;
    return toCheck;
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.dbfetch.DBFetcher#getDbName()
   */
  @Override
  public String getDbName() {
    return "uniprot";
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.dbfetch.DBFetcher#getFormat()
   */
  @Override
  public String getFormat() {
    return "uniprot";
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.dbfetch.DBFetcher#setFormat(java.lang.String)
   */
  @Override
  public void setFormat(String format) {
    throw new UnsupportedOperationException();
  }
  
}
