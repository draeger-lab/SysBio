/**
 * 
 */
package de.zbit.uniprot;

import java.util.concurrent.TimeoutException;

import uk.ac.ebi.webservices.WSDbfetchClient;
import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.util.InfoManagement;
import de.zbit.util.ProgressBar;

/**
 * @author Finja Buechel: finja.buechel@uni-tuebingen.de
 * 
 */
public class UniProtWrapper extends InfoManagement<String, String> {
  private static final long serialVersionUID = -1996313057043843757L;
  private WSDbfetchClient dbfetch = new WSDbfetchClient();

  public static boolean showProgress = false;
  public static boolean fetchRenamedProteins = false;

  /**
   * @param listLength
   */
  public UniProtWrapper(int i) {
    super(i);
  }

  /**
   * 
   */
  public UniProtWrapper() {
    super();
  }

  public static void main(String[] args) {
    UniProtWrapper uw = new UniProtWrapper();

    System.out.println(uw.getInformation("ALBU_HUMAN"));

    // String[] anfrage = new String[]{"ZCH18_HUMAN", "1433B_HUMAN", "QUATSCH", "ZCH14_HUMAN"};
    // String[] ret = uw.getInformations(anfrage);
    //    
    // System.out.println("Rückgabe:");
    // for (int i=0; i<ret.length; i++) {
    // System.out.println(anfrage[i] + ":\n" + ret[i] + "\n==============================");
    // }
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
    // protNames must be comma separated uniprot ids/acs
    if (id == null || id.isEmpty())
      throw new UnsuccessfulRetrieveException();

    // fetch proteins
    String entriesStr = "";
    int retried = 0;
    while (retried < 3 && (entriesStr == null || entriesStr.isEmpty())) {
      try {
        entriesStr = dbfetch.fetchData("uniprot:" + id.toUpperCase(),
                  "uniprot", "raw");
        // entriesStr = dbfetch.fetchBatch("uniprot", id, "uniprot", "raw");
        break;
      } catch (Exception e) {
        retried++;
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
      System.out.println("Trying mass retrieve...");
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

    if (fetchRenamedProteins) {
      if (showProgress) {
        int c = 0;
        for (int i = 0; i < ids.length; i++)
          if (ret[i] == null || ret[i].isEmpty())
            c++;
        System.out.println("Fixing single not mappable or not found IDs (" + c
                  + ")...");
        prog = new ProgressBar(c);
      }

      // Proteine, die nicht zugeordnet werden konnten, gesondert anfordern
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
      System.out.println("Done.");

    return ret;
  }

  /**
   * @param ids
   * @param ret
   * @param queryString
   * @param startID
   * @param i
   */
  private void fetchMultipleChecked(String[] ids, String[] ret,
            String queryString, int startID, int i) {
    // fetch proteins
    String entriesStr = "";
    int retried = 0;
    while (retried < 3 && (entriesStr == null || entriesStr.isEmpty())) {
      try {
        entriesStr = dbfetch.fetchBatch("uniprot", queryString, "uniprot",
                  "raw");
      } catch (Exception e) {
        retried++;
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
    }
    else {
      // by // divided
      String[] splitt = entriesStr.split("\n//");

      // Idealfall: Soviele Antworten wie Anfragen
      if ((splitt.length - 1) == queryString.split(",").length) { // -1 wei letztes = "\n"
        int j = 0;
        for (int index = startID; index <= i; index++) {
          ret[index] = splitt[j];
          j++;
        }
      }
      else {

        // Ein paar Anfragen lieferten keine Antworten => Mapping.
        for (String info : splitt) {
          if (info.length() <= 1)
            continue; // letzter split = "\n"
          int endPos = info.indexOf("\n", info.indexOf("\nAC"));
          String toCheck = info;
          if (endPos > 0)
            toCheck = info.substring(0, endPos);
          for (int index = startID; index <= i; index++) {
            if (ret[index] != null && !ret[index].isEmpty())
              continue;

            if (toCheck.contains(ids[index])) {
              ret[index] = info;
              break;
            }
          }
        }
      }
    }
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
}
