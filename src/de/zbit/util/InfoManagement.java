package de.zbit.util;

import java.io.Serializable;
import java.util.concurrent.TimeoutException;

import de.zbit.exception.UnsuccessfulRetrieveException;

/**
 * This class is intended to reduce network traffic or hard disk load, by
 * remembering a certain number of most used elements, indestead of retrieving
 * them each time again and again.
 * 
 * @author wrzodek
 */
public abstract class InfoManagement<IDtype extends Comparable<?> & Serializable, INFOtype extends Serializable> implements Serializable {
  /**
   * It is recommended to generate a new ID when extending this class.
   */
  private static final long serialVersionUID = -5172273501517643495L;
  private SortedArrayList<Info<IDtype, INFOtype>> rememberedInfos;
  private SortedArrayList<IDtype> unsuccessfulQueries; // Remember those separately
  
  private int maxListSize;
  
  public static synchronized InfoManagement<?, ?> loadFromFilesystem(String filepath) {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObject(filepath);
    m.restoreUnserializableObject();
    return m;
  }
  public static synchronized void saveToFilesystem(String filepath, InfoManagement<?, ?> m) {
    m.cleanupUnserializableObject();
    Utils.saveObject(filepath, m);
  }
  
  public InfoManagement() {
    this(1000);
  }
  public InfoManagement(int maxListSize) {
    if (maxListSize<1) System.err.println("Initialized a InfoManagement list with size " + maxListSize);
    this.maxListSize = maxListSize;
    rememberedInfos = new SortedArrayList<Info<IDtype, INFOtype>>(this.maxListSize+1);
    unsuccessfulQueries = new SortedArrayList<IDtype>((10*this.maxListSize)+1); // so many, since usually this does not take much memory.
  }
  
  public synchronized INFOtype getInformation(IDtype id) {
    int pos = rememberedInfos.indexOf(id);
    if (pos>=0) return rememberedInfos.get(pos).getInformation();
    else {
      // Retrieve object and store it.
      INFOtype info = fetchInformationWrapper(id);
      addInformation(id, info);
      return info;
    }
  }
  
  /**
   * Adds the given information. It is intended, that this function does NOT check if the information
   * is already available.
   * @param id
   * @param info
   */
  public synchronized void addInformation(IDtype id, INFOtype info) {
    addInformation(new Info<IDtype, INFOtype>(id, info));
  }
  /**
   * Adds the given information. It is intended, that this function does NOT check if the information
   * is already available.
   * @param infoObject
   */
  public synchronized void addInformation(Info<IDtype, INFOtype> infoObject) {
    // Ensure constant max list capacity. Remove least frequently used item.
    while (rememberedInfos.size()>=maxListSize) {
      /* It is quite difficult to save an DataType sorted by object usage and object to quickly remove the least frequent
       * used one. That's I'm using a heuristic, just quering the bottom 1000 ones, which is in O(1). 
       
      int min = Math.max(0, rememberedInfos.size()-1000);
      int itemToDelete = rememberedInfos.size()-1; int minUsage=Integer.MAX_VALUE;
      for (int i=rememberedInfos.size()-1; i>=min; i--) {
        if(rememberedInfos.get(i).getTimesInfoAccessed()<minUsage) {
          minUsage = rememberedInfos.get(i).getTimesInfoAccessed();
          itemToDelete = i;
          if (minUsage==0) break; // can't get any lower.
        }
      }
      rememberedInfos.remove(itemToDelete);*/
      
      // Above solution is not good, since mostly the most recent added item gets deleted... Simple solution:
      int min = Math.max(0, rememberedInfos.size()-1000);
      int itemToDelete = rememberedInfos.size()-1; int minUsage=Integer.MAX_VALUE;
      for (int i=0; i<rememberedInfos.size(); i++) {
        if(rememberedInfos.get(i).getTimesInfoAccessed()<minUsage) {
          minUsage = rememberedInfos.get(i).getTimesInfoAccessed();
          itemToDelete = i;
          if (minUsage==0) break; // can't get any lower.
        }
      }
      rememberedInfos.remove(itemToDelete);
    }
    
    rememberedInfos.add(infoObject);
  }
  
  /**
   * This is a wrapper for 'fetchInformation'. It manages unsuccessfull retrievement and is able to
   * distinguish between temporary errors (Timeouts) which do not get saved as an unsuccessfull retrievement
   * and real unsuccessfulll retrievements (which are saved and returned quite quickly when the user
   * tries to retrieve them again).
   * @param id
   * @return INFOtype if info could be retrieved successfully, null instead.
   */
  private INFOtype fetchInformationWrapper(IDtype id) {    
    if (unsuccessfulQueries.contains(id)) return null; // Don't have to try it again.
    INFOtype ret=null;
    int retried=0;
    while (ret==null) {
      try {
        ret = fetchInformation(id);
        if (ret==null) unsuccessfulQueries.add(id);
        break;
      } catch (TimeoutException e) {
        retried++;
        if (retried>=3) {
          e.printStackTrace();
          break;
        }
      } catch (UnsuccessfulRetrieveException e) {
        unsuccessfulQueries.add(id);
        break;
      }
    }
    
    return ret;
  }
  
  /**
   * This function should NEVER be called from any other class. It fetches the information from an
   * online or hard disc source and does not use the remembered information in memory.
   * 
   * Implementation notes:
   * You should distinct between a temporary error (e.g. no internet connection or timeouts) and throw
   * the 'TimeoutException' if this is the case. On the other hand, please throw the 'UnsuccessfulRetrieveException'
   * OR return null.
   * The return value null is handled as an unsuccessfull retrieve (e.g. Item not in database).
   * 
   * @param id
   * @return requested Information by id
   */
  protected abstract INFOtype fetchInformation(IDtype id) throws TimeoutException, UnsuccessfulRetrieveException;
  
  /**
   * You may implement this Method to make your class serializable.
   * This function is called directly before writing the object to your hard drive.
   */
  protected abstract void cleanupUnserializableObject();
  /**
   * You may implement this Method to make your class serializable.
   * This function is called directly after loading the object from your hard drive.
   */
  protected abstract void restoreUnserializableObject();
}
