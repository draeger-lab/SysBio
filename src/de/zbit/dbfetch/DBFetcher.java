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

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.axis.AxisFault;

import uk.ac.ebi.webservices.axis1.WSDbfetchClient;
import uk.ac.ebi.webservices.axis1.stubs.wsdbfetch.DbfNoEntryFoundException;
import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.util.InfoManagement;
import de.zbit.util.ProgressBar;
import de.zbit.util.Utils;

/**
 * Abstract implementation of the WSDBfetch client, using a cache and
 * more convenient methods.
 * {@link http://www.ebi.ac.uk/Tools/webservices/services/dbfetch}.
 * 
 * <p>For further infos on WSDBFetch, see:</p>
 * <ul>
 * <li><a href="http://www.ebi.ac.uk/Tools/webservices/services/dbfetch">http://www.ebi.ac.uk/Tools/webservices/services/dbfetch</a></li>
 * <li><a href="http://www.ebi.ac.uk/Tools/webservices/tutorials/06_programming/java">http://www.ebi.ac.uk/Tools/webservices/tutorials/06_programming/java</a></li>
 * <li><a href="http://ws.apache.org/axis/">http://ws.apache.org/axis/</a></li>
 * </ul>
 *
 * @author Finja B&uuml;chel
 * @author Clemens Wrzodek
 * @author Florian Mittag
 * @version $Rev$
 * @since 1.0
 */
public abstract class DBFetcher extends InfoManagement<String, String> {
  
  /**
   * Enumeration to describe the requested style of a dbfetch result.
   * 
   * @author Florian Mittag
   */
  public static enum Style {
    DEFAULT ("default"),
    HTML ("html"),
    RAW ("raw");
    
    private String name;
    
    Style(String name) {
      this.name = name;
    }
    
    public String toString() {
      return name;
    }
  }

  public static Logger log = Logger.getLogger(DBFetcher.class.getName());
  private static final long serialVersionUID = -1996313057043843757L;

  /**
   * The actual WSDBFetch client
   */
  private transient WSDbfetchClient dbfetch = new WSDbfetchClient();

  /**
   * Displayes a ProgressBar when fetching multiple identifier.
   */
  public static boolean showProgress = false;
  /**
   * If true, IDs that could not be found by mass query (if fetching multiple
   * identifier) will be retried one after one, by fetching only a single id
   * at a time.
   * Background: Uniprot sometimes gives the new protein for old accessions and
   * you can't map the old id to the new entry, because it does not occur in the
   * entry block.
   */
  public static boolean fetchNonMappableIDs = false;
  
  /**
   * The style to fetch. Usually "Style.RAW".
   * @see Style
   */
  private Style style;

  /**
   * 
   */
  public DBFetcher() {
    super();
  }

  /**
   * 
   * @param i
   */
  public DBFetcher(int i) {
    super(i);
  }
  
  
  /*
   * (non-Javadoc)
   * 
   * @see de.zbit.util.InfoManagement#cleanupUnserializableObject()
   */
  @Override
  protected void cleanupUnserializableObject() {
    dbfetch = null;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see de.zbit.util.InfoManagement#fetchInformation(java.lang.Comparable)
   */
  @Override
  protected String fetchInformation(String id) throws TimeoutException,
            UnsuccessfulRetrieveException {
    if (id == null || id.length() == 0)
      throw new UnsuccessfulRetrieveException();

    // fetch proteins
    String entriesStr = "";
    int retried = 0;
    final int retryLimit=3;
    while (retried < retryLimit && (entriesStr == null || entriesStr.length() == 0)) {
      try {
        if (dbfetch==null) restoreUnserializableObject();
        entriesStr = dbfetch.fetchData(getDbName() + ":" + id.toUpperCase(),
            getFormat(), getStyleString());
        break;
      } catch (AxisFault e) {
        if (e instanceof DbfNoEntryFoundException || e.toString()!=null && e.toString().equalsIgnoreCase("No result found"))
          throw new UnsuccessfulRetrieveException( e );
        else {
          retried++;
          if (retryLimit==retryLimit) e.printStackTrace();
          log.log(Level.FINE, "Attempt " + retried + " to fetch data failed", e);
        }
      } catch (Exception e) {
        retried++;
        if (retryLimit==retryLimit) e.printStackTrace();
        log.log(Level.FINE, "Attempt " + retried + " to fetch data failed", e);
      }
    }

    if (retried >= retryLimit && (entriesStr == null || entriesStr.length() == 0))
      throw new TimeoutException();
    if (entriesStr.trim().length() == 0)
      throw new UnsuccessfulRetrieveException();

    return entriesStr;
  }
  
  
  /**
   * @param ids
   * @param ret
   * @param queryString
   * @param startID
   * @param endID
   */
  private void fetchMultipleChecked(String[] ids, String[] ret,
            String queryString, int startID, int endID) {
    String entriesStr = "";
    final int retryLimit=3;
    int retried = 0;
    while (retried < retryLimit && (entriesStr == null || entriesStr.length() == 0)) {
      try {
        // I've no idea why, but sometimes the initial restoring call didn't work.
        if (dbfetch==null) restoreUnserializableObject();
        entriesStr = dbfetch.fetchBatch(getDbName(), queryString, getFormat(),getStyleString());
      } catch (AxisFault e) {
        if (e instanceof DbfNoEntryFoundException || e.toString()!=null && e.toString().equalsIgnoreCase("No result found")) {
          // Propagate to caller.
          for (int index = startID; index <= endID; index++)
            ret[index] = null;
          return;
        } else {
          retried++;
          if (retried==retryLimit) {
            e.printStackTrace();
            log.log(Level.FINE, "Attempt " + retried + " to fetch data failed", e);
          } else {
            log.log(Level.FINE, "Attempt " + retried + " to fetch data failed");
          }
        }
      } catch (Exception e) {
        // One of DbfParamsException, DbfConnException, DbfException, 
        // InputException, RemoteException, ServiceException
        retried++;
        if (retried==retryLimit) {
          e.printStackTrace();
          log.log(Level.FINE, "Attempt " + retried + " to fetch data failed", e);
        } else {
          log.log(Level.FINE, "Attempt " + retried + " to fetch data failed");
        }
      }
    }
    if (retried >= retryLimit && (entriesStr == null || entriesStr.length() == 0)) {
      // Try it protein by protein
      String[] splitt = queryString.split(",");
      for (int j = 0; j < splitt.length; j++) {
        try {
          ret[startID + j] = fetchInformation(splitt[j]);
        } catch (Exception e) {
          ret[startID + j] = null;
        }
      }
    } else {
      // divided by "//" (in Uniprot, but may be different in other dbs!)
      String[] splitt = entriesStr.split(getEntrySeparator());

      // optimal case: as many answers as requests
      if ((splitt.length - 1) == queryString.split(",").length) { // -1 due to last "\n"
        int j = 0;
        for (int index = startID; index <= endID; index++) {
          if (!splitt[j].endsWith(getAppendAtEnd()))
            splitt[j]+=getAppendAtEnd();//"\n//\n"
          if (!splitt[j].startsWith(getAppendAtStart()))
            splitt[j]=getAppendAtStart()+splitt[j];//""
          
          ret[index] = splitt[j];
          j++;
        }
      } else {

        // Some requests had no results => Mapping.
        for (String info : splitt) {
          if (info.length() <= 1)
            continue; // last split = "\n"
          
          String toCheck = getCheckStrFromInfo(info);
          
          // Iterate over every ID and try to map the block to the id
          for (int index = startID; index <= endID; index++) {
            // Has the id already an associated block?
            if (ret[index] != null && (ret[index].length() > 0))
              continue;

            // Does the ID or AC line contain this idText?
            if (matchIDtoInfo(ids[index], toCheck)) {
              if (!info.endsWith(getAppendAtEnd()))
                info+=getAppendAtEnd();
              if (!info.startsWith(getAppendAtStart()))
                info=getAppendAtStart()+info;
              
              ret[index] = info;
              
              // Don't break here. 1:n mapping possible. With break,
              // we would make an 1:1 mapping.
              // E.g. when fetching "ENOA_MOUSE" and "P17182" the result
              // is the same data-block.
              // break;
            }
          }
        }
      }
    }
  }
  
  /**
   * @return the string to append to each entry to fix missing
   * parts, due to the {@link #getEntrySeparator()}.
   */
  public String getAppendAtEnd() {
    return "\n//\n";
  }
  
  /**
   * @return the string to append to each entry to fix missing
   * parts, due to the {@link #getEntrySeparator()}.
   */
  public String getAppendAtStart() {
    return "";
  }

  /**
   * @return the separator that is used by the DBFetch Database
   * to separate multiple fetched entries.
   * Usually this is a "//", but for some databases (e.g., RefSeq)
   * another separator is used.
   */
  public String getEntrySeparator() {
    return Pattern.quote("\n//\n");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.zbit.util.InfoManagement#fetchMultipleInformations(IDtype[])
   */
  @Override
  protected String[] fetchMultipleInformations(String[] ids)
            throws TimeoutException, UnsuccessfulRetrieveException {
    String[] ret = new String[ids.length];
    String queryString = "";
    int protInQS = 0;
    int startID = 0;
    ProgressBar prog = null;
    

    if (showProgress) {
      log.info("Trying mass retrieve...");
      prog = new ProgressBar(ids.length);
    }

    for (int i = 0; i < ids.length; i++) {
      queryString += ids[i] + ",";
      protInQS++;
      if (queryString.length() > 500 || protInQS == 99) {
        queryString = queryString.substring(0, queryString.length() - 1); // Remove last comma.
        fetchMultipleChecked(ids, ret, queryString, startID, i);

        queryString = "";
        protInQS = 0;
        startID = i + 1;
      }
      if (showProgress)
        prog.DisplayBar();
    }

    if (queryString.length() > 0) {
      queryString = queryString.substring(0, queryString.length() - 1); // Remove last comma.
      fetchMultipleChecked(ids, ret, queryString, startID, ids.length - 1);
    }

    if (fetchNonMappableIDs) {
      if (showProgress) {
        int c = 0;
        for (int i = 0; i < ids.length; i++)
          if (ret[i] == null || ret[i].length() == 0)
            c++;
        log.info("Fixing single not mappable or not found IDs (" + c + ")...");
        prog = new ProgressBar(c);
      }

      // IDs that couldn't be mapped will be requested separately
      for (int i = 0; i < ids.length; i++) {
        if (ret[i] == null || ret[i].length() == 0) {
          try {
            ret[i] = fetchInformation(ids[i]);
          } catch (Exception e) {
            ret[i] = null;
          }
          if (showProgress)
            prog.DisplayBar();
        }
      }
    }
    

    if (showProgress)
      log.info("Done.");

    return ret;
  }
  
  /**
   * Returns a substring of the given info string that will be used for matching
   * IDs to info strings.
   * 
   * @see #matchIDtoInfo(String, String)
   * @param info the info string from which to generate the substring
   * @return the string that will be used for matching IDs to info strings
   */
  public abstract String getCheckStrFromInfo(String info);
  
  /**
   * Returns the DB name to be used for the dbfetch queries
   * (e.g., uniprot, ipi, ...) 
   * 
   * <p>You can find a list of available databases
   * <a href="http://www.ebi.ac.uk/Tools/dbfetch/dbfetch/dbfetch.databases">
   * here</a>.</p>
   * 
   * @return the DB name to be used for dbfetch queries
   */
  public abstract String getDbName();

  /**
   * Returns the format string that is used for queries.
   * 
   * @return the format string that is used for queries
   */
  public abstract String getFormat();

  /**
   * Returns the style that is used for queries.
   * 
   * @return the style that is used for queries
   */
  public Style getStyle() {
    return style;
  }

  /**
   * Returns the style that is used for queries as a string.
   * 
   * @return the style that is used for queries as a string
   */
  public String getStyleString() {
    return style.toString();
  }
  
  /**
   * Returns whether the given ID matches to the String to check. The default
   * case implemented here performs a simple <code>contains</code> check.
   * Overwrite this method, if you want to have a different behavior.
   * 
   * @param id the ID to check
   * @param toCheck the string to check for occurence of the ID
   * @return <code>true</code>, if the ID can be matched to the string,
   *         otherwise <code>false</code>
   */
  public boolean matchIDtoInfo(String id, String toCheck) {
    /*
     *  Contains might lead to wrong results. Perform an
     *  Case insensitive word search (e.g. query: NOA_MOUSE and
     *  result contains "ENOA_MOUSE" => result is a wrong mapping.
     */
    //return toCheck.contains(id);
    return Utils.isWord(toCheck, id);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see de.zbit.util.InfoManagement#restoreUnserializableObject()
   */
  @Override
  protected void restoreUnserializableObject() {
    dbfetch = new WSDbfetchClient();
  }

  /**
   * Sets the format string that is used for queries. Implementations that do
   * not allow to change the format string should throw a
   * {@link UnsupportedOperationException} here.
   * 
   * @param format the format string that should be used for queries
   */
  public abstract void setFormat(String format);

  /**
   * Sets the style that is used for queries.
   * 
   * @param style the style that is used for queries
   */
  public void setStyle(Style style) {
    this.style = style;
  }
}
