package de.zbit.kegg;

import java.util.concurrent.TimeoutException;

import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.util.CustomObject;
import de.zbit.util.InfoManagement;

/**
 * Uses Kegg functions (like retrieve all PWs for an organism) instead of just retrieving information
 * for a Kegg ID (what KeggInfoManagement does).
 * Think of this class as a cache.
 * @author wrzodek
 */
public class KeggFunctionManagement extends InfoManagement<KeggQuery, CustomObject>{
  private static final long serialVersionUID = -4559358395869823899L;
  private KeggAdaptor adap=null;
  
  /**
   * If this flag ist set to true, this class does NOT retrieve any Information, but uses stored information.
   */
  public static boolean offlineMode = false;
  
  public KeggFunctionManagement () {
    super();
    this.adap = new KeggAdaptor();
  }
  
  public KeggFunctionManagement (int maxListSize, KeggAdaptor adap) {
    super(maxListSize); // Remember maxListSize queries at max.
    this.adap = adap;
  }
  
  public KeggAdaptor getKeggAdaptor() {
    if (adap==null) adap = new KeggAdaptor();
    return adap;
  }
  
  @Override  
  protected void cleanupUnserializableObject() {
    adap = null;
  }
  @Override
  protected void restoreUnserializableObject () {
    adap = getKeggAdaptor();
  }
  
  @Override
  protected CustomObject fetchInformation(KeggQuery id) throws TimeoutException, UnsuccessfulRetrieveException {
    if (offlineMode) throw new TimeoutException();
    
    int j = id.getJobToDo();
    Object answer=null;
    if (adap==null) adap = getKeggAdaptor(); // create new one
    
    
    //try {
      if (j==KeggQuery.getGenesByPathway) {
        answer = adap.getGenesByPathwayWithTimeout(id.getQuery());
      } else if (j==KeggQuery.getIdentifier) {
        answer = adap.getIdentifierWithTimeout(id.getQuery());
      } else if (j==KeggQuery.getPathways) {
        answer = adap.getPathwayListWithTimeout(id.getQuery());
      } else {
        System.err.println("Unknown job '" + j + "'.");
        throw new UnsuccessfulRetrieveException(); // don't retry.
      }
      /*
      } catch (AdaptorException e) {
        throw new TimeoutException(); // retry...
      } catch (ParseException e) {
        e.printStackTrace();
        throw new UnsuccessfulRetrieveException(); // don't retry.
      }
      */    
    if (answer==null || (answer instanceof String &&  ((String)answer).trim().length()==0)) throw new UnsuccessfulRetrieveException(); // Will cause the InfoManagement class to remember this one.
    
    return new CustomObject(answer); // Successfull and "with data" ;-)
  }
  
  @Override
  protected CustomObject[] fetchMultipleInformations(KeggQuery[] ids) throws TimeoutException, UnsuccessfulRetrieveException {
    System.err.println("Fetching multiple infos not supported. Please use KeggInfoManagement, if possible.");
    return null;
  }
  
  
}
