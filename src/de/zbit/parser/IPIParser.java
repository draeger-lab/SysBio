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
package de.zbit.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

import de.zbit.dbfetch.IPIFetcher;
import de.zbit.util.logging.LogUtil;

/**
 * @author Finja B&uml;chel
 * 
 * Parse information out of the text it gets from the IPIFetcher
 * @version $Rev$
 * @since 1.0
 */
public class IPIParser {

  IPIFetcher IPIManagement = null;

  public IPIParser() {
    try {
      if (new File("ipi.dat").exists())
        IPIManagement = (IPIFetcher) IPIFetcher.loadFromFilesystem("ipi.dat");
    } catch (Throwable e) {
    }
    if (IPIManagement == null)
      IPIManagement = new IPIFetcher(80000);
    
  }
  
  public IPIFetcher getIPIManagement() {
    return IPIManagement;
  }


  /**
   * @param ids
   * @return list of arrays[ipi,ac]
   */
  public ArrayList<String[]> getUniProtACs(String[] ids) {
    ArrayList<String[]> ac = new ArrayList<String[]>();
 
    String[] results = IPIManagement.getInformations(ids);
    
    for( int i = 0; i < results.length; i++) {
      if(results[i]!=null) {    
        String[] splitLines = results[i].split("\n");
        for(int j = 0; j<splitLines.length; j++) {
          String line = splitLines[j];

          if(line.startsWith("DR   UniProtKB/Swiss-Prot")) {
            
            String[] lineEntries = line.split(";");
            String[] s = new String[]{ids[i].trim(),lineEntries[1].trim()};
            if(!ac.contains(s))
              ac.add(s);
          }
        } 
      }
      else{
        String[] s = new String[]{ids[i],null};
        if(!ac.contains(s))
          ac.add(s);
      }
    }

    return ac;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    LogUtil.initializeLogging(Level.INFO);
    
    IPIParser ipiParser = new IPIParser();

    String[] ids = {"IPI00003348","IPI00003865","IPI00021435","IPI00026833","IPI00105598","IPI00114375",
                    "IPI00116074","IPI00116283.1","IPI00117264","IPI00122565","IPI00126072","IPI00128023",
                    "IPI00131695","IPI00132042","IPI00133903","IPI00221402","IPI00223875","IPI00331436",
                    "IPI00381412","IPI00407692","IPI00420349","IPI00420385","IPI00454142","IPI00462072",
                    "IPI00467833"};

    ArrayList<String[]> results = ipiParser.getUniProtACs(ids);
    for (int i = 0; i<results.size(); i++) {
      System.out.println(results.get(i)[0] + "\t" + results.get(i)[1]);
    }

}


}
