/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg.api.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.zbit.cache.InfoManagement;
import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.kegg.api.KeggAdaptor;
import de.zbit.kegg.api.KeggInfos;
import de.zbit.util.StringUtil;
import de.zbit.util.ThreadManager;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Retrieve and manage Kegg infos. Once retrieved, they are cached and don't have to
 * be retrieved again. Also a "precache" statement is possible, to quickly cache and download
 * many kegg IDs simultaneously.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class KeggInfoManagement extends InfoManagement<String, KeggInfos> implements Serializable {
  private static final long serialVersionUID = -2621701345149317801L;
  /**
   * True if this class contents have changed since
   * the last reading/writing of this instance.
   */
  private transient boolean hasChanged=false;
  /**
   * The adapter to communicate with the KEGG API
   */
  private transient KeggAdaptor adap=null;
  
  
  /**
   * If this flag ist set to true, this class does NOT retrieve any Information, but uses stored information.
   */
  public static boolean offlineMode = false;
      
  /**
   * 
   */
  public KeggInfoManagement () {
    super();
    this.adap = new KeggAdaptor();
  }
  
  public KeggInfoManagement (int maxListSize) {
    this (maxListSize, new KeggAdaptor());
  }
    
  /**
   * 
   * @param maxListSize
   * @param adap
   */
  public KeggInfoManagement (int maxListSize, KeggAdaptor adap) {
    super(maxListSize); // Remember maxListSize queries at max.
    this.adap = adap;
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.util.InfoManagement#cleanupUnserializableObject()
   */
  @Override  
  protected void cleanupUnserializableObject() {
    adap = null;
  }

  /*
   * (non-Javadoc)
   * @see de.zbit.util.InfoManagement#fetchInformation(java.lang.Comparable)
   */
  @Override
  protected KeggInfos fetchInformation(String id) throws TimeoutException, UnsuccessfulRetrieveException {
    if (offlineMode) throw new TimeoutException(); // do not cache as "Unsuccessful" and retry next time.
    if (id.toLowerCase().startsWith("unknown")) return null;
    hasChanged=true;
    
    if (adap==null) adap = getKeggAdaptor(); // create new one
    String ret = adap.getWithReturnInformation(id);
    if (ret==null || ret.trim().length()==0) throw new UnsuccessfulRetrieveException(); // Will cause the InfoManagement class to remember this one.
    
    if (id.startsWith("br:")) {
      // KEGG Brite unfortunately returns HTML-code
      ret = transformBRITEOutputToNormal(ret);
    }
    
    ret = removeUnnecessaryInfos(ret);
    KeggInfos realRet = new KeggInfos(id, ret);
    
    
    return  realRet;// Successfull and "with data" ;-) 
  }
  
  /**
   * KEGG Brite gives HTML-code, but luckily the old-text format
   * as HTML-comment => parse the comment.
   * @param ret
   * @return
   */
  private String transformBRITEOutputToNormal(String ret) {
    int ePos = ret.indexOf("ENTRY");
    if (ePos<0) return ret;
    int start = ret.lastIndexOf("<!---",ePos);
    int end = ret.indexOf("--->",ePos);
    if (start>=0 && end>start) {
      ret = ret.substring(start+5, end);
      ret = ret.replace("\n#", "\n");
    }
    return ret.trim();
  }

  /*
   * (non-Javadoc)
   * @see de.zbit.util.InfoManagement#fetchMultipleInformations(IDtype[])
   */
  /**
   * Wrapper for {@link fetchMultipleInformationsUpTo100AtATime} because Kegg only supports 100 at a time :)
   */
  @Override
  protected KeggInfos[] fetchMultipleInformations(final String[] ids) throws TimeoutException, UnsuccessfulRetrieveException {
    return fetchMultipleInformations(ids,null);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.util.InfoManagement#fetchMultipleInformations(IDtype[], de.zbit.util.AbstractProgressBar)
   */
  /**
   * Wrapper for {@link fetchMultipleInformationsUpTo100AtATime} because Kegg only supports 100 at a time :)
   */
  @Override
  protected KeggInfos[] fetchMultipleInformations(String[] ids,
    AbstractProgressBar progress) throws TimeoutException, UnsuccessfulRetrieveException {
    final int atATime = 10; // Since 2013-01-01, KEGG limited the amount of ids that can be retrieved simultaneously to 10!
    
    int fetchInArun = Math.min(atATime, ids.length);
    int fetchRuns = (int) Math.ceil(((double)ids.length)/((double)atATime));
    
    String[] APIinfos;
    final KeggInfos[] realRet = new KeggInfos[ids.length];
    if (progress!=null) {
      progress.setNumberOfTotalCalls((fetchInArun*fetchRuns) + ids.length);
      log.finer(String.format("Querying %s KEGG ids in %s runs.", ids.length, fetchRuns ));
    }
    
    // If we parse to many string in parallel, we get
    // out of memory errors! => Limit to maximal 50!
    ThreadManager APIstringParser = new ThreadManager();
    if (ids.length<=atATime) {
      try {
        APIinfos = fetchMultipleInformationsUpTo100AtATime(ids);
      } catch (UnsuccessfulRetrieveException e) {
        // Do NOT pipie it through! else, everything is marked as unretrievable
        APIinfos=null;
      }
      
      // Jump progress bar to 50%
      if (progress!=null) {
        synchronized (progress) {
          progress.setCallNr(progress.getCallNumber()+fetchInArun);
        }
      }
      
      // Multi-threaded string parsing
      APIinfos = removeUnnecessaryInfos(APIinfos);
      parseAPI(ids, APIinfos, realRet, APIstringParser,0, progress);
      // ---
    } else {
      //APIinfos = new String[ids.length];

      // Instead of requesting all objects at once, splitts Queries to 100 max and concatenates the results... that's it.
      int j=0;
      while (j<ids.length) {
        String[] subArr = new String[Math.min(atATime, ids.length-j)];
        System.arraycopy(ids, j, subArr, 0, subArr.length);

        String[] ret;
        try {
          ret = fetchMultipleInformationsUpTo100AtATime(subArr);
        } catch (UnsuccessfulRetrieveException e) {
          // Do NOT pipie it through! else, everything is marked as unretrievable
          ret=null;
        }
        
        if (progress!=null) {
          synchronized (progress) {
            progress.setCallNr(progress.getCallNumber()+fetchInArun);
          }
        }
        ret = removeUnnecessaryInfos(ret);
        //System.arraycopy(ret, 0, APIinfos, j, ret.length);

        // Multi-threaded string parsing
        parseAPI(subArr, ret, realRet, APIstringParser, j, progress);
        // ---        

        j+=subArr.length;
      }
    }
    
    APIstringParser.awaitTermination();
    if (progress!=null) progress.finished();
    // For Debugging
    //for (int i=0; i<ids.length; i++) {
    //  System.out.println(ids[i] + ": '" + realRet[i].substring(0, 50).replace("\n", "|").replaceAll(" +", " ")+"'");
    //}
    
    return realRet;
  }

  /**
   * Parse the return string from the KEGG API to the internal {@link KeggInfos}
   * data structure.
   * @param ids queried identifiers
   * @param APIinfos returned infos from the KEGG API
   * @param realRet target array to write the {@link KeggInfos}
   * @param APIstringParser {@link ThreadManager} to handle the threads
   * @param realRetOffset optional (set to 0 by default) offset between
   * {@code ids} or {@code APIinfos} and  {@code realRet}.
   */
  private void parseAPI(final String[] ids, String[] APIinfos,
    final KeggInfos[] realRet, ThreadManager APIstringParser, final int realRetOffset, final AbstractProgressBar progress) {
    if (APIinfos==null) {
      // None was succesfull!
      for (int i=0; i<ids.length; i++) {
        realRet[i+realRetOffset] = null;
      }
      if (progress!=null) {
        synchronized (progress) {
          progress.incrementCallNumber(ids.length);
        }
      }
    } else {
      for (int i=0; i<APIinfos.length; i++) {
        final int final_i = i;
        final String apiInfos = APIinfos[final_i];
        Runnable parser = new Runnable() {
          /*
           * (non-Javadoc)
           * @see java.lang.Runnable#run()
           */
          public void run() {
            if (apiInfos==null || apiInfos.length()<1) {
              realRet[final_i+realRetOffset] = null;
            } else {
              realRet[final_i+realRetOffset] = new KeggInfos(ids[final_i], apiInfos);
            }
            if (progress!=null) {
              synchronized (progress) {
                progress.DisplayBar();
              }
            }
          }
        };
        APIstringParser.addToPool(parser);
      }
    }
  }

  /**
   * Do not call this class by yourself.
   * It's just a helper method for {@link fetchMultipleInformations}
   * @param ids
   * @return
   * @throws TimeoutException
   * @throws UnsuccessfulRetrieveException
   */
  private String[] fetchMultipleInformationsUpTo100AtATime(String[] ids) throws TimeoutException, UnsuccessfulRetrieveException {
    if (offlineMode) throw new TimeoutException(); // do not cache as "Unsuccessful" and retry next time.
    if (ids == null) return null;
    if (ids.length<1) return new String[0];
    hasChanged=true;
    
    // Check if we have at least one valid kegg id!
    boolean allUnknown = true;
    for (String id: ids) {
      if (!id.toLowerCase().startsWith("unknown")) {
        allUnknown=false;
        break;
      }
    }
    if (allUnknown) throw new UnsuccessfulRetrieveException(); // Will cause the InfoManagement class to remember all.
    
    if (adap==null) adap = getKeggAdaptor(); // create new one
    String q = adap.getWithReturnInformation(concatenateKeggIDs(ids));
    if (q==null || q.trim().length()==0) throw new UnsuccessfulRetrieveException(); // Will cause the InfoManagement class to remember all.
    
    String[] splitt = q.split("///");
    
    String[] ret = new String[ids.length];
    Arrays.fill(ret, null); // Initialize all non-successful ids.
    
    int numMissing = 0;
    boolean errors = false;
    for (int i=0; i<splitt.length; i++) {
      // Trim and check trivial cases
      if (splitt[i]==null) continue;
      splitt[i] = splitt[i].trim();
      if  (splitt[i].length()<=0) {splitt[i]=null; continue;}
      
      // Extract Entry id of current dataset
      String aktEntryID = KeggAdaptor.extractInfo(splitt[i], "ENTRY", "  ");
      if (aktEntryID==null || aktEntryID.length()==0) {
        // Fallback on HTML-processing, somited bGet (e.g. for BRITE) returns HTML-code
        // and the actual API info in commented-brackets.
        splitt[i] = transformBRITEOutputToNormal(splitt[i]);
        // ... and retry
        aktEntryID = KeggAdaptor.extractInfo(splitt[i], "ENTRY", "  ");
        // Last fallback on regular expression
        if (aktEntryID==null || aktEntryID.length()==0) {
          Matcher m = Pattern.compile("\\s?ENTRY\\s+(\\w+)").matcher(splitt[i]);
          if (m.find()) {
            aktEntryID = (m.group(1));
          }
        }
      }
      if (aktEntryID==null || aktEntryID.length()<=0) {
        // Should NEVER happen, because KEGG does always send ENTRY-entries.
        System.err.println(String.format("No Entry id found in:\n%s\n[...]\n------------",
          splitt[i].substring(0, Math.min(150, splitt[i].length()))));
        continue;
      } else {
        aktEntryID = aktEntryID.trim();
      }
      
      
      // Look if maybe the indices of entry and return value are the same. This is the case, until the first invalid (or "not found") kegg id.
      /*
       * Folgendes Prinzip: Kegg schickt immer Ergebnisse in der Reihenfolge, in der auch die IDs geschickt wurden. Ist mal kein
       * Ergebnis vorhanden, shiften sich die ids um 1 mehr. ... Rest, siehe implementierung ;-)
       */
      int idIndex = i+numMissing;
      boolean found = false;
      int minNumMissing = numMissing; boolean takeNotSoSureHits=false;
      String aktQueryID;
      while (idIndex<ids.length) {
        idIndex = i+numMissing;
        if (idIndex>= ids.length) {
          // z.B. Query (gn:)"HSA" liefert eine Entry ID "T01001" zur�ck. Das findet man nicht so einfach. Deshalb komplett durchlaufen lassen
          // und spaeter noch mal unschaerfer suchen.
          numMissing = minNumMissing;
          idIndex = i+numMissing;
          if (takeNotSoSureHits) break; // ... should never happen.
          takeNotSoSureHits = true;
        }
        aktQueryID = (ids[idIndex].contains(":")? ids[idIndex].substring(ids[idIndex].indexOf(':')+1):ids[idIndex]).trim().toUpperCase();
        if (aktQueryID.equalsIgnoreCase(aktEntryID) 
            || ("EC " + aktQueryID).equalsIgnoreCase(aktEntryID) // Enzyme werden ohne "EC " gequeried, kommen aber MIT zurueck... 
            || (takeNotSoSureHits && StringUtil.isWord(splitt[i].toUpperCase(), aktQueryID))) { // Siehe obiges Beispiel.
          ret[idIndex] = splitt[i]; // Aufpassen. Hier nur i, da index von splitt und id2 hier gleich!
          found = true;
          break;
        }
        numMissing++;
      }
      
      if (!found) {
        System.err.println("No id found for result '"+aktEntryID+"':\n" + splitt[i].substring(0, Math.min(150, splitt[i].length())) + "...\n-----------------\nThis should not happen!");
        errors = true;
      }
      
    }
    
    // Output missing ids (helps debugging...)
    if (errors) {
      StringBuilder sb = new StringBuilder();
      for (int i=0; i<ret.length; i++) {
        if (ret[i] ==null){
          if (sb.length()>0) sb.append(", ");
          sb.append('"');
          sb.append(ids[i]==null?null:ids[i]);
          sb.append('"');
        }
      }
      System.err.println("The following ids could not get fetched from KEGG: " + sb.toString());
    }
    
    return ret; // Successfull and "with data" ;-) 
  }

  /**
   * 
   * @return
   */
  public KeggAdaptor getKeggAdaptor() {
    if (adap==null) adap = new KeggAdaptor();
    return adap;
  }
  
  /**
   * This function allows you to extend this class and overwrite this function.
   * Then you can remove all information from the KeggString which you don't need.
   * This may save you a lot of RAM. Please keep this Class as generic as possible.
   * So don't implement this function here directly!
   * 
   * This class must be public, so the user can override it. Please never call this
   * function from outside this class.
   * @return
   */
  public String removeUnnecessaryInfos(String ret) {
    /* Example for content of ret:
ENTRY       8491              CDS       H.sapiens
NAME        MAP4K3
DEFINITION  mitogen-activated protein kinase kinase kinase kinase 3
            (EC:2.7.11.1)
ORTHOLOGY   K04406  mitogen-activated protein kinase kinase kinase kinase 3
                    [EC:2.7.11.1]
PATHWAY     hsa04010  MAPK signaling pathway
CLASS       Metabolism; [...]
     */
    return ret;
  }

  /**
   * This function allows you to extend this class and overwrite this function.
   * Then you can remove all information from the KeggString which you don't need.
   * This may save you a lot of RAM. Please keep this Class as generic as possible.
   * So don't implement this function here directly!
   * @return
   */
  private String[] removeUnnecessaryInfos(String[] realRet) {
    if (realRet==null) return realRet;
    for (int i=0; i<realRet.length; i++)
      realRet[i] = removeUnnecessaryInfos(realRet[i]);
    return realRet;
  }

  /*
   * (non-Javadoc)
   * @see de.zbit.util.InfoManagement#restoreUnserializableObject()
   */
  @Override
  protected void restoreUnserializableObject () {
    adap = getKeggAdaptor();
    hasChanged=false;
  }
  /**
   * 
   * @param adap
   */
  public void setKeggAdaptor(KeggAdaptor adap) {
    this.adap = adap;
  }
  
  /**
   * @return has the content of this class changed, since initilization/ Loading?
   */
  public boolean hasChanged() {
    return hasChanged || isCacheChangedSinceLastLoading();
  }
  
  /**
   * Requiered for queries to the KeggDB.
   * @param ids
   * @return every id in the array in one string, separated by a whitespace.
   */
  private static String concatenateKeggIDs(String[] ids) {
    StringBuilder ret = new StringBuilder();
    for (String s: ids) {
      ret.append(s.replace(" ", ""));
      ret.append(' ');
    }
    return ret.toString().trim();
  }
  
  /**
   * Save the given instance of {@link #KeggInfoManagement()}.
   * @param filepath
   * @param m current instance
   * @return true if and only if the file has been successfully saved.
   */
  public static boolean saveToFilesystem(String filepath, KeggInfoManagement m) {
    boolean ret = InfoManagement.saveToFilesystem(filepath, m);
    if (ret) {
      m.hasChanged = false;
    }
    return ret;
  }
}
