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
 * @author Finja B&uml;chel
 * 
 * 
 * Manages the retrival of UniProt information
 * 
 * 
 */
public class UniProtWrapper extends InfoManagement<String, String> {
  /**
   * 
   */
  private static final long serialVersionUID = -1996313057043843757L;

  /**
   * 
   */
  private WSDbfetchClient dbfetch = new WSDbfetchClient();
  /**
   * 
   */
  public static boolean showProgress = false;

  /**
   * 
   */
  public static boolean fetchRenamedProteins = false;

  /**
   * 
   */
  public UniProtWrapper() {
    super();
  }

  /**
   * @param listLength
   */
  public UniProtWrapper(int i) {
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
    // protNames must be comma separated uniprot ids/acs
    if (id == null || id.length() == 0)
      throw new UnsuccessfulRetrieveException();

    // fetch proteins
    String entriesStr = "";
    int retried = 0;
    while (retried < 3 && (entriesStr == null || entriesStr.length() == 0)) {
      try {
        entriesStr = dbfetch.fetchData("uniprot:" + id.toUpperCase(),
                  "uniprot", "raw");
        // entriesStr = dbfetch.fetchBatch("uniprot", id, "uniprot", "raw");
        break;
      } catch (Exception e) {
        retried++;
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
   * @param i
   */
  private void fetchMultipleChecked(String[] ids, String[] ret,
            String queryString, int startID, int i) {
    // fetch proteins
    String entriesStr = "";
    int retried = 0;
    while (retried < 3 && (entriesStr == null || entriesStr.length() == 0)) {
      try {
        entriesStr = dbfetch.fetchBatch("uniprot", queryString, "uniprot",
                  "raw");
      } catch (Exception e) {
        retried++;
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
        // Iterate over every uniprot block
        for (String info : splitt) {
          if (info.length() <= 1)
            continue; // letzter split = "\n"
          // Extract first two lines with ID and AC.
          int endPos = info.indexOf("\n", info.indexOf("\nAC")+1);
          String toCheck = info;
          if (endPos > 0)
            toCheck = info.substring(0, endPos);
          
          // Iterate over every ID and try to map the block to the id
          for (int index = startID; index <= i; index++) {
            // Has the id already an associated block?
            if ((ret[index] != null) && (ret[index].length() > 0))
              continue;
            
            // Does the ID or AC line contain this idText?
            /*
             * TODO: contains might lead to wring results. Perform an
             *  Case insensitive word search (e.g. query: NOA_MOUSE and
             *  result contains "ENOA_MOUSE" => result is a wrong mapping.
             */
            if (toCheck.contains(ids[index])) {
              ret[index] = info;
              // Don't break here. 1:n mapping possible. With break,
              // we would make an 1:1 mapping.
              //break;
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

    if (queryString.length() > 0) {
      queryString = queryString.substring(0, queryString.length() - 1); // Remove last comma.
      fetchMultipleChecked(ids, ret, queryString, startID, ids.length - 1);
    }

    if (fetchRenamedProteins) {
      if (showProgress) {
        int c = 0;
        for (int i = 0; i < ids.length; i++)
          if (ret[i] == null || ret[i].length() == 0)
            c++;
        System.out.println("Fixing single not mappable or not found IDs (" + c
                  + ")...");
        prog = new ProgressBar(c);
      }

      // Proteine, die nicht zugeordnet werden konnten, gesondert anfordern
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
      System.out.println("Done.");

    return ret;
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
   * 
   * @param args
   */
  public static void main(String[] args) {
    UniProtWrapper uw = new UniProtWrapper();

//    // System.out.println(uw.getInformation("ALBU_HUMAN"));
//
//    WSDbfetchClient wfetch = new WSDbfetchClient();
//    try {
//      System.out.println(wfetch.fetchData("uniprot:P17182",
//                "uniprot", "raw"));
//    } catch (RemoteException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (ServiceException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//    if(true)return;
    String[] anfrage = new String[]{//"P62258", "Q91G88"//, "1433B_HUMAN", "QUATSCH", "ZCH14_HUMAN"
//              "ENOA_MOUSE",
//              "P62879",
//              "P11142",
//              "P35998",
//              "P30520",
//              "O00231",
//              "O08553",
//              "Q99KI0",
//              "P80318",
//              "Q99LX0",
//              "Q61598",
//              "Q62465",
//              "Q91WD5",
//              "P07724",
//              "Q9D051",
//              "P38647",
//              "P05064",
//              "Q8C0M9",
//              "Q9CPY7",
//              "Q8CAQ8",
//              "P50516",
//              "Q8C1B7",
//              "Q8C1B7",
              "P17182",
              "P17751"
    };
    String[] ret = uw.getInformations(anfrage);
       
    System.out.println("Rückgabe:");
    for (int i=0; i<ret.length; i++) {
      System.out.println(anfrage[i] + ":\n" + ret[i] + "\n==============================");
    }
  }
}
