package de.zbit.dbfetch;

import java.util.concurrent.TimeoutException;

import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;

import uk.ac.ebi.webservices.WSDbfetchClient;
import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.util.InfoManagement;
import de.zbit.util.ProgressBar;
import de.zbit.util.Utils;

/**
 * Abstract implementation of the WSDBfetch client:
 * {@link http://www.ebi.ac.uk/Tools/webservices/services/dbfetch}.
 * 
 * 
 * @author Finja B&uuml;chel: finja.buechel@uni-tuebingen.de
 * @author Clemens Wrzodek: clemens.wrzodek@uni-tuebingen.de
 * @author Florian Mittag: florian.mittag@uni-tuebingen.de
 * 
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
  /**
   * 
   */
  public static Logger log = Logger.getLogger(DBFetcher.class);
  /**
   * 
   */
  private static final long serialVersionUID = -1996313057043843757L;

  /**
   * 
   */
  private WSDbfetchClient dbfetch = new WSDbfetchClient();
  // TODO: get rid of global variables, this is unclean code
  /**
   * 
   */
  public static boolean showProgress = false;
  /**
   * 
   */
  public static boolean fetchNonMappableIDs = false;
  
  /**
   *   
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
    while (retried < 3 && (entriesStr == null || entriesStr.length() == 0)) {
      try {
        entriesStr = dbfetch.fetchData(getDbName() + ":" + id.toUpperCase(),
            getFormat(), getStyleString());
        break;
      } catch (AxisFault e ) {
        throw new UnsuccessfulRetrieveException( e );
      } catch (Exception e) {
        retried++;
        log.debug("Attempt " + retried + " to fetch data failed", e);
      }
    }

    if (retried >= 3 && (entriesStr == null || entriesStr.length() == 0))
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
    int retried = 0;
    while (retried < 3 && (entriesStr == null || entriesStr.length() == 0)) {
      try {
        entriesStr = dbfetch.fetchBatch(getDbName(), queryString, getFormat(),
                  getStyleString());
      } catch (Exception e) {
        retried++;
        log.debug("Attempt " + retried + " to fetch data failed", e);        
      }
    }
    if (retried >= 3 && (entriesStr == null || entriesStr.length() == 0)) {
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
      // divided by "//"
      String[] splitt = entriesStr.split("\n//");

      // optimal case: as many answers as requests
      if ((splitt.length - 1) == queryString.split(",").length) { // -1 due to last "\n"
        int j = 0;
        for (int index = startID; index <= endID; index++) {
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
