package de.zbit.dbfetch;

import java.util.concurrent.TimeoutException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import uk.ac.ebi.webservices.WSDbfetchClient;
import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.util.InfoManagement;
import de.zbit.util.ProgressBar;

/**
 * @author Finja Buechel: finja.buechel@uni-tuebingen.de
 * @author Clemens Wrzodek: clemens.wrzodek@uni-tuebingen.de
 * @author Florian Mittag: florian.mittag@uni-tuebingen.de
 * 
 */
public abstract class DBFetcher extends InfoManagement<String, String> {

  public static Logger log = Logger.getLogger(DBFetcher.class);
  
  private static final long serialVersionUID = -1996313057043843757L;
  private WSDbfetchClient dbfetch = new WSDbfetchClient();

  // TODO: get rid of global variables, this is unclean code
  public static boolean showProgress = false;
  public static boolean fetchNonMappableIDs = false;
  
  
  private Style style;
  
  public DBFetcher(int i) {
    super(i);
  }

  public DBFetcher() {
    super();
  }

  /**
   * Returns the DB name to be used for the dbfetch queries
   * (e.g., uniprot, ipi, ...) 
   * 
   * @return the DB name to be used for dbfetch queries
   */
  public abstract String getDbName();
  
  
  public abstract String getFormat();
  
  public abstract void setFormat(String format);
  
  
  public void setStyle(Style style) {
    this.style = style;
  }
  
  public Style getStyle() {
    return style;
  }
  
  public String getStyleString() {
    return style.toString();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see de.zbit.util.InfoManagement#fetchInformation(java.lang.Comparable)
   */
  @Override
  protected String fetchInformation(String id) throws TimeoutException,
            UnsuccessfulRetrieveException {
    if (id == null || id.isEmpty())
      throw new UnsuccessfulRetrieveException();

    // fetch proteins
    String entriesStr = "";
    int retried = 0;
    while (retried < 3 && (entriesStr == null || entriesStr.isEmpty())) {
      try {
        entriesStr = dbfetch.fetchData(getDbName() + ":" + id.toUpperCase(),
            getFormat(), getStyleString());
        break;
      } catch (Exception e) {
        retried++;
        log.debug("Attempt " + retried + " to fetch data failed", e);
      }
    }

    if (retried >= 3 && (entriesStr == null || entriesStr.isEmpty()))
      throw new TimeoutException();
    if (entriesStr.trim().isEmpty())
      throw new UnsuccessfulRetrieveException();

    return entriesStr;
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

    if (!queryString.isEmpty()) {
      queryString = queryString.substring(0, queryString.length() - 1); // Remove last comma.
      fetchMultipleChecked(ids, ret, queryString, startID, ids.length - 1);
    }

    if (fetchNonMappableIDs) {
      if (showProgress) {
        int c = 0;
        for (int i = 0; i < ids.length; i++)
          if (ret[i] == null || ret[i].isEmpty())
            c++;
        log.info("Fixing single not mappable or not found IDs (" + c
                  + ")...");
        prog = new ProgressBar(c);
      }

      // IDs that couldn't be mapped will be requested separately
      for (int i = 0; i < ids.length; i++) {
        if (ret[i] == null || ret[i].isEmpty()) {
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
    while (retried < 3 && (entriesStr == null || entriesStr.isEmpty())) {
      try {
        entriesStr = dbfetch.fetchBatch(getDbName(), queryString, getFormat(),
                  getStyleString());
      } catch (Exception e) {
        retried++;
        log.debug("Attempt " + retried + " to fetch data failed", e);        
      }
    }
    if (retried >= 3 && (entriesStr == null || entriesStr.isEmpty())) {
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
          
          for (int index = startID; index <= endID; index++) {
            if (ret[index] != null && !ret[index].isEmpty())
              continue;

            if (matchIDtoInfo(ids[index], toCheck)) {
              ret[index] = info;
              break;
            }
          }
        }
      }
    }
  }

  /**
   * @param info
   * @return
   */
  public abstract String getCheckStrFromInfo(String info);
  
  /**
   * Returns whether the given ID matches to the String to check. The default
   * case implemented here performs a simple <code>contains</code> check.
   * Overwrite this method, if you want to have a different behavior.
   * 
   * @param id
   * @param toCheck
   * @return
   */
  public boolean matchIDtoInfo(String id, String toCheck) {
    return toCheck.contains(id);
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
   * @see de.zbit.util.InfoManagement#restoreUnserializableObject()
   */
  @Override
  protected void restoreUnserializableObject() {
    dbfetch = new WSDbfetchClient();
  }

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
}
