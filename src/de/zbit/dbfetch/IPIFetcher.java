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
package de.zbit.dbfetch;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import de.zbit.util.LogUtil;

/**
 * 
 * @author Florian Mittag
 * @version $Rev$
 */
public class IPIFetcher extends DBFetcher {

  private static final long serialVersionUID = -1921530776666710988L;

  private String format;
  
  public IPIFetcher() {
    this(1000);
  }
  
  public IPIFetcher(int cacheSize) {
    super(cacheSize);
    setFormat("default");
    setStyle(Style.RAW);
  }
  
  @Override
  public String getCheckStrFromInfo(String info) {
    return info;
  }

  @Override
  public String getDbName() {
    return "ipi";
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
    int index = id.indexOf('.');
    if( id.toUpperCase().contains("IPI") && index >= 0 ) {
      String ac = id.toUpperCase().substring(0, index);
      return toCheck.toUpperCase().contains(ac);
    }
    return super.matchIDtoInfo(id, toCheck);
  }
  
  @Override
  public String[] getInformations(String[] ids) {
    String ret[] = super.getInformations(ids);
    
    if( ret != null ) {
      // collect all "null" results
      Map<String, Integer> mapping = new HashMap<String, Integer>();
      for (int i = 0; i < ret.length; i++) {
        if( ret[i] == null ) {
          mapping.put(ids[i], i);
        }
      }
      
      // create an array with the ACs (IDs without the ".X" at the end)
      String[] ids2 = mapping.keySet().toArray(new String[0]);
      String[] acs = new String[mapping.size()];
      for (int i = 0; i < acs.length; i++) {
        int index = ids2[i].indexOf('.');
        if( index >= 0 ) {
          acs[i] = ids2[i].substring(0, index);
        } else {
          acs[i] = ids2[i];
        }
      }
      
      String[] ret2 = super.getInformations(acs);
      
      for (int i = 0; i < ids2.length; i++) {
        if( ret2[i] != null ) {
          int index = mapping.get(ids2[i]);
          ret[index] = ret2[i];
        }
      }
    }
    
    return ret;
  }
  
  
  public static void main(String[] args) {
    LogUtil.initializeLogging(Level.ALL);
    
    IPIFetcher ipifetch = new IPIFetcher();
    
    // TODO: IPI seems to be case sensitive
    /*String[] ids = {"IPI00003348","IPI00003865","IPI00021435","IPI00026833","IPI00105598","IPI00114375",
              "IPI00116074","IPI00116283.1","IPI00117264","IPI00122565","IPI00126072","IPI00128023",
              "IPI00131695","IPI00132042","IPI00133903","IPI00221402","IPI00223875","IPI00331436",
              "IPI00381412","IPI00407692","IPI00420349","IPI00420385","IPI00454142","IPI00462072",
              "IPI00467833"};*/

    
    
    /*
    String[] ids = new String[] { "IPI00910870.1", "IPI00182126.3",
        "IPI00655812.1", "IPI00916535.1", "IPI00025252.1", "IPI00329331.6",
        "IPI00894365.2", "IPI00853290.1", "IPI00915768.1", "IPI00218918.5",
        "IPI00748256.2", "IPI00872531.1", "IPI00295741.4", "IPI00411704.9",
        "IPI00880148.1", "IPI00479997.4", "IPI00219219.3", "IPI00374179.1" };*/
        
    //String[] ids = new String[]{"ENSP00000383556", "LGALS1"};
    
    String[] ids = new String[]{"IPI00114375", "IPI00420349"};
    
    String[] results = ipifetch.getInformations(ids);
    for( int i = 0; i < ids.length; i++) {
      System.out.println("=== ID: " + ids[i] + " ===");
      System.out.println(results[i]);
      System.out.println();
    }
    
    // ENSP doesn't seem to work
    // String id = "ENSP00000383556";
    //String id = "ENSP00000332449";
    //System.out.println(ipifetch.getInformation(id));
    
    //String id = "IPI00420349";
    //System.out.println(ipifetch.getInformation(id));
  }

}
