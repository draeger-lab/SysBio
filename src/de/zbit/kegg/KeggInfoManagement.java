package de.zbit.kegg;

import java.io.Serializable;
import java.util.concurrent.TimeoutException;

import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.util.InfoManagement;
import de.zbit.util.Utils;

public class KeggInfoManagement extends InfoManagement<String, String> implements Serializable {
  private static final long serialVersionUID = -2621701345149317801L;
  private KeggAdaptor adap=null;
  
  /**
   * If this flag ist set to true, this class does NOT retrieve any Information, but uses stored information.
   */
  public static boolean offlineMode = false;
  
  public KeggInfoManagement (int maxListSize, KeggAdaptor adap) {
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
  protected String fetchInformation(String id) throws TimeoutException, UnsuccessfulRetrieveException {
    if (offlineMode) throw new TimeoutException();
    
    if (adap==null) adap = getKeggAdaptor(); // create new one
    String ret = adap.getWithReturnInformation(id);
    if (ret==null || ret.trim().isEmpty()) throw new UnsuccessfulRetrieveException(); // Will cause the InfoManagement class to remember this one.
    
    return ret; // Successfull and "with data" ;-) 
  }
  

  
}
