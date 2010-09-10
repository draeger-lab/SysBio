package de.zbit.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import de.zbit.exception.UnsuccessfulRetrieveException;

/**
 * This class is intended to reduce network traffic or hard disk load, by
 * remembering a certain number of most used elements, indestead of retrieving
 * them each time again and again.
 * 
 * A better name for this class is maybe "Cache".
 * 
 * @author wrzodek
 */
public abstract class InfoManagement<IDtype extends Comparable<?> & Serializable, INFOtype extends Serializable> implements Serializable {
  /**
   * It is recommended to generate a new ID when extending this class.
   */
  private static final long serialVersionUID = -5172273501517643495L;
  
  public static final transient Logger log = Logger.getLogger(InfoManagement.class);
  
  private SortedArrayList<Info<IDtype, INFOtype>> rememberedInfos;  
  private SortedArrayList<IDtype> unsuccessfulQueries; // Remember those separately
  private int maxListSize;
  private boolean cacheChangedSinceLastLoading=false;
  
  
  public InfoManagement() {
    this(1000);
  }
  
  /**
   * Constructor. Initialize this InfoManagement object with a maximum cache
   * size of <code>maxListSize</code> entries.
   * 
   * @param maxListSize the maximum number of cached entries
   */
  public InfoManagement(int maxListSize) {
    if (maxListSize<1) System.err.println("Initialized a InfoManagement list with size " + maxListSize);
    this.maxListSize = maxListSize;
    rememberedInfos = new SortedArrayList<Info<IDtype, INFOtype>>(this.maxListSize+1);
    unsuccessfulQueries = new SortedArrayList<IDtype>((10*this.maxListSize)+1); // so many, since usually this does not take much memory.
  }
  
  /**
   * @return The number of remembered infos and unsuccessful queries (as sum).
   */
  public int getNumberOfCachedIDs() {
    int sum = 0;
    if (rememberedInfos!=null) sum+=rememberedInfos.size();
    if (unsuccessfulQueries!=null) sum+=unsuccessfulQueries.size();
    return sum;
  }
  
  /**
   * This method clears the unsuccessfulQueries array. In addition, it removes
   * all rememberedInfos that have a query(IDtype) or content(INFOtype) that is
   * a) null, b) .toString().equals("") or c) .toString().equals("0").
   * 
   * The intention of this method is to re-fetch empty or unsuccessfulQueries
   * what may be usefull if the underlying database is updated.
   */
  public void cleanupUnsuccessfulAndEmptyInfos() {
    if (unsuccessfulQueries.size()>0) {
      cacheChangedSinceLastLoading=true;
      unsuccessfulQueries.clear();
    }
    
    for (int i=0; i<rememberedInfos.size(); i++) {
      Info<IDtype, INFOtype> in = rememberedInfos.get(i);
      if (in.getIdentifier()==null || in.getIdentifier().toString().equals("") || in.getIdentifier().toString().equals("0") ||
          in.getInformation()==null || in.getInformation().toString().equals("") || in.getInformation().toString().equals("0") ){
        rememberedInfos.remove(i);
        i--;
        cacheChangedSinceLastLoading=true;
      }
    }
  }
  
  /**
   * Clears the whole cache (rememberedInfos and unsuccessfulQueries).
   */
  public void clearCache() {
    if (unsuccessfulQueries.size()>0 || rememberedInfos.size()>0) cacheChangedSinceLastLoading=true;
    unsuccessfulQueries.clear();
    rememberedInfos.clear();
  }
  
  /**
   * Returns wether this class has been changed since it has been initiated
   * or loaded from the hard drive.
   * You should only save the cache if this is true. Else, saving won't give
   * you more information.
   * @return class has been changed since last readObject() (serializable loading)
   * or since initializing.
   */
  public boolean isCacheChangedSinceLastLoading() {
    return cacheChangedSinceLastLoading;
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
   * 
   * Use this function to also consider the time, fetching of each item took, when the class needs to
   * remove old information.
   * @param id
   * @param info
   * @param timeForRetrieve - IN SECONDS
   */
  public synchronized void addInformation(IDtype id, INFOtype info, float timeForRetrieve) {
    Info<IDtype, INFOtype> myInfo = new Info<IDtype, INFOtype>(id, info);
    myInfo.setTimeForFetchingInfo(timeForRetrieve);
    addInformation(myInfo);
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
      //int min = Math.max(0, rememberedInfos.size()-1000);
      int itemToDelete = rememberedInfos.size()-1; //int minUsage=Integer.MAX_VALUE;
      double minUsage = Double.MAX_VALUE;
      for (int i=0; i<rememberedInfos.size(); i++)  {
        double usageVal = (((double)rememberedInfos.get(i).getTimesInfoAccessed())+0.1) * rememberedInfos.get(i).getTimeForFetchingInfo();
        if(usageVal<minUsage) {
          minUsage = usageVal;
          itemToDelete = i;
          if (minUsage<1) break; // 0.1 = can't get any lower.;  <1 = low enough ;-) (iterating though all items is inefficient).
        }
      }
      rememberedInfos.remove(itemToDelete);
    }
    
    rememberedInfos.add(infoObject);
    cacheChangedSinceLastLoading=true;
  }
  
  /**
   * Removes one element from the cache. 
   * @param id
   * @return true, if the element has been found and removed. False instead.
   */
  public synchronized boolean removeInformation(IDtype id) {
    boolean found = false;
    for (int i=0; i<rememberedInfos.size(); i++) {
      Info<IDtype, INFOtype> in = rememberedInfos.get(i);
      if (in.getIdentifier().equals(id)) {
        rememberedInfos.remove(i);
        found = true;
        break;
      }
    }
    
    if (!found) {
      for (int i=0; i<unsuccessfulQueries.size(); i++) {
        if (unsuccessfulQueries.get(i).equals(id)) {
          unsuccessfulQueries.remove(i);
          found = true;
          break;
        }
      }
    }
    
    if (found) cacheChangedSinceLastLoading=true;
    
    return found;
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
        if (ret==null) {
          unsuccessfulQueries.add(id);
          cacheChangedSinceLastLoading=true;
        }
        break;
      } catch (TimeoutException e) {
        retried++;
        if (retried>=3) {
          log.debug("3 attempts failed with a TimeoutException");
          e.printStackTrace();
          break;
        }
      } catch (UnsuccessfulRetrieveException e) {
        log.debug("Unsuccessful retrieval, marking this ID as unretrievable", e);
        unsuccessfulQueries.add(id);
        cacheChangedSinceLastLoading=true;
        break;
      } catch (Throwable t) {
        // do NOT retry and do NOT save anything... simply return the null
        // This may happen e.g. if this class is used to manage db queries
        // and the user or database is offline.
        log.debug("Catched an unknown exception while fetching informations", t);
        t.printStackTrace();
        ret=null;
        break;
      }
    }
    
    return ret;
  }
  
  
  /**
   * Please SEE {@link fetchInformation} for more annotations!
   * Return INFOtype for each id in ids. Use exactly the same index in both arrays.
   * If you return null, all queries for all ids do not return results (unsuccessfull retrieve).
   * If you set certain elements in the array to null, only the id at this index was not successfully queried. 
   * @param ids - MAY CONTAIN null or empty IDS, may also be a list of size 0 !!!!!
   * @return
   * @throws TimeoutException - if and only if the timeout is for ALL object.
   * @throws UnsuccessfulRetrieveException - if and only if none of all ids could be retrieved.
   */
  protected abstract INFOtype[] fetchMultipleInformations(IDtype[] ids) throws TimeoutException, UnsuccessfulRetrieveException;
  
  /**
   * This will fetch all given ids (NOT using cache) and handle the UnsuccessfulRetrieveException
   * and TimeoutException exceptions. It will update the unsuccessfulQueries() collection and
   * return the resulting infos.
   * @param ids
   * @return
   */
  private INFOtype[] fetchMultipleInformationWrapper(IDtype[] ids) {
    // you should already have checked for "unsuccessfulQueries" when using this function.
    INFOtype[] ret = null;
    if (ids==null) return ret;
    
    int retried=0;
    while (ret==null) {
      try {
        ret = fetchMultipleInformations(ids);
        
        // Cache the unsuccessfulQueries
        if (ret==null) {
          unsuccessfulQueries.addAll(ids);
          cacheChangedSinceLastLoading=true;
        } else {
          for (int i=0; i<ids.length; i++) {
            if (ids[i]!=null && !ids[i].equals("") && ret[i]==null) {
              unsuccessfulQueries.add(ids[i]);
              cacheChangedSinceLastLoading=true;
            }
          }
        }
        break;
      } catch (TimeoutException e) {
        retried++;
        if (retried>=3) {
          log.debug("3 attempts failed for all with a TimeoutException");
          e.printStackTrace();
          break;
        }
      } catch (UnsuccessfulRetrieveException e) {
        log.debug("Unsuccessful retrieval, marking ALL IDs as unretrievable", e);
        unsuccessfulQueries.addAll(ids);
        cacheChangedSinceLastLoading=true;
        break;
      } catch (Throwable t) {
        // do NOT retry and do NOT save anything... simply return the null
        // This may happen e.g. if this class is used to manage db queries
        // and the user or database is offline.
        log.debug("Catched an unknown exception while fetching multiple informations", t);
        t.printStackTrace();
        ret=null;
        break;
      }
    }
    
    return ret;
  }
  
  /**
   * Retrieve a single information. This will use the cached information, if available. Else,
   * it will call the fetchInformation method and cache the answer.
   * @param id - id to query.
   * @return INFOtype - the answer.
   */
  public synchronized INFOtype getInformation(IDtype id) {
    int pos = rememberedInfos.indexOf(id);
    if (pos>=0) return rememberedInfos.get(pos).getInformation();
    else {
      // Retrieve object and store it.
      long timer = System.currentTimeMillis();
      INFOtype info = fetchInformationWrapper(id);
      timer = System.currentTimeMillis()-timer;
      if (info!=null) addInformation(id, info, (((float)timer)/1000) );
      return info;
    }
  }
  
  
  /**
   * Retrieve multiple informations. This will used the cached information, if available. Else,
   * it will call the fetchMultipleInformation method and build a cache on the answer. The
   * returned results use the same indices as the given ids. 
   * @param ids - ids to query.
   * @return INFOtype - array of same size, with same ordering as ids.
   */
  @SuppressWarnings("unchecked")
  public synchronized INFOtype[] getInformations(IDtype[] ids) {
    if (ids==null) return null;
    ArrayList<IDtype> filteredIDs = new ArrayList<IDtype>();
    boolean touched = false; // if true, ids!=filteredIDs
    INFOtype anyCachedInfo=null;
    for (IDtype id: ids) {
      int pos = rememberedInfos.indexOf(id);
      if (pos<0 && !unsuccessfulQueries.contains(id)) { // Same if-order as below!
        filteredIDs.add(id);
      } else {
        touched = true;
        if (anyCachedInfo==null) anyCachedInfo=rememberedInfos.get(pos).getInformation();
      }
    }
    
    
    
    if (touched) {
      // Some elements are already in our cache.
    
      IDtype[] filtIDs = (IDtype[]) createNewArray(ids,filteredIDs.size());
      for (int i=0; i<filtIDs.length; i++)
        Array.set(filtIDs, i, filteredIDs.get(i));
      
      INFOtype[] ret = fetchMultipleInformationWrapper(filtIDs);
      
      // Big Problem: Java does not permit creating an generic array
      //INFOtype[] infos = new INFOtype[ids.length]; // Not permitted... workaround: 
      //INFOtype[] infosTemp = (INFOtype[]) Array.newInstance(ret.getClass(), ids.length);
      //INFOtype[] infosTemp = Arrays.copyOf(ret, ids.length); // Create a new Reference to an existing array, WITH NEW SIZE
      //INFOtype[] infos = infosTemp.clone(); // After creating new reference with correct size, create new array.
      INFOtype[] infos = (INFOtype[]) createNewArray(anyCachedInfo,ids.length);
      
      // Iterate in parallel through ids, infos and filteredIDs
      int infos_i=0;
      for (int i=0; i<ids.length; i++) {
        int pos = rememberedInfos.indexOf(ids[i]);
        if (pos>=0) { // Same if-order as above!
          infos[i] = rememberedInfos.get(pos).getInformation();
        } else if (ids[i].equals(filtIDs[infos_i])){
          // Newly fetched infos
          if (ret!=null && ret.length<infos_i) {
            // should never happen. (=null => unsuccessfulQueries)
            System.err.println("Something went badly wrong. Your fetchMultipleInformations method must return an array of exactly the same size as the input id array!");
            infos[i] = null;
          } else if (ret!=null){
            infos[i] = ret[infos_i];
            if (ret[infos_i]!=null) addInformation(ids[i], ret[infos_i]);
          }else{
            infos[i] = null;
          }
          infos_i++;
        } else if (unsuccessfulQueries.contains(ids[i])) {
          // Must be below "Newly fetched infos" because it is modified in fetchMultipleInformationWrapper.
          infos[i] = null;
        }
      }
      return infos;
    } else {
      INFOtype[] infos = fetchMultipleInformationWrapper(ids);
      if (infos==null) return null;
      for (int i=0; i<infos.length; i++)
        if (infos[i]!=null && ids[i]!=null) addInformation(ids[i], infos[i]);
      return infos;
    }
  }
  
  /**
   * Precache ids, so they are available as soon as you query them.
   * This is usefull if you know that you are going to query mulitple ids with
   * a single query. Than you can precache them here, which results in on fetchMulti
   * query, and later on retrieve them with getInformation().
   * This will significantly increas performance, since you queried all with one query.
   * @param ids
   */
  @SuppressWarnings("unchecked")
  public synchronized void precacheIDs(IDtype[] ids) {
    if (ids==null || ids.length<1) return;
    ArrayList<IDtype> filteredIDs = new ArrayList<IDtype>();
    boolean touched = false; // if true, ids!=filteredIDs
    for (IDtype id: ids) {
      if (rememberedInfos.indexOf(id)<0 && !unsuccessfulQueries.contains(id)) {
        filteredIDs.add(id);
      } else {
        touched = true;
      }
    }
    
    INFOtype[] infos;
    IDtype[] filtIDs = ids;
    if (touched) {
      // Some elements are already in our cache.
      
      // Big Problem: Java does not permit creating an generic array
      // IDtype[] filtIDs = new IDtype[filteredIDs.size()]; // Not permitted... workaround:
      //IDtype[] filtIDs = (IDtype[]) Array.newInstance(ids.getClass(), filteredIDs.size()); // <= funzt auch nicht.
      // Funzt nur in Java 1.6 (nächste zwei zeilen):
      //IDtype[] temp = Arrays.copyOf(ids, filteredIDs.size()); // Create a new Reference to an existing array, WITH NEW SIZE
      //IDtype[] filtIDs = temp.clone(); // After creating new reference with correct size, create new array.
      filtIDs = (IDtype[]) createNewArray(ids,filteredIDs.size());
      
      for (int i=0; i<filtIDs.length; i++)
        Array.set(filtIDs, i, filteredIDs.get(i));
    }
    
    // Query unknown ids    
    infos = fetchMultipleInformationWrapper(ids);
    if (infos==null) return;
    
    // Add retrieved infos
    for (int i=0; i<infos.length; i++)
      if (infos[i]!=null && ids[i]!=null) addInformation(ids[i], infos[i]);
    
  }
  
  /**
   * 
   * @param file
   * @return
   */
  public static synchronized InfoManagement<?, ?> loadFromFilesystem(File file) {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObject(file);
    return m;
  }
  /**
   * 
   * @param in
   * @return
   */
  public static synchronized InfoManagement<?, ?> loadFromFilesystem(InputStream in) {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObject(in);
    return m;
  }
  /**
   * 
   * @param filepath
   * @return
   */
  public static synchronized InfoManagement<?, ?> loadFromFilesystem(String filepath) {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObject(filepath);
    return m;
  }

  /**
   * 
   * @param filepath
   * @param m
   */
  public static synchronized void saveToFilesystem(String filepath, InfoManagement<?, ?> m) {
    Utils.saveObject(filepath, m);
  }
  
  /**
   * Creates a new array, of the given size, that contains all items
   * from the old one and has the new size.
   * Uses reflection methods, so this method is save to use with generics.
   * @param oldArray
   * @param newSize
   * @return resized array (copy of old one, including elements of old one).
   */
  @SuppressWarnings("unused")
  private static Object resizeArray(Object oldArray, int newSize) {
    int oldSize = 0;
    if (oldArray.getClass().isArray())
      oldSize = java.lang.reflect.Array.getLength(oldArray);
    
    Object newArray = createNewArray(oldArray, newSize);
    
    int preserveLength = Math.min(oldSize, newSize);
    if (preserveLength > 0)
      System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
    return newArray;
  }
  
  /**
   * Creates a new array, uses reflection methods, so this method is save
   * to use with generics.
   * @param type - the method will infere the class of the object from this
   * given sample. Just give any sample of the class you want to create a new
   * array from. The sample won't be touched.
   * @param size
   * @return new array of the class of the given type, with the given size.
   */
  @SuppressWarnings("unchecked")
  private static Object createNewArray(Object type, int size) {
    Class elementType=null;
    if (type instanceof Class)
      elementType = (Class) type;
    else if (type.getClass().isArray())
      elementType = type.getClass().getComponentType();
    
    // If oldArray was in fact no array, then elementType==null here.
    if (elementType==null) elementType = type.getClass();
    Object newArray = java.lang.reflect.Array.newInstance(elementType, size);
    
    return newArray;
  }

  
  /**
   * You may implement this Method to make your class serializable.
   * This function is called directly after loading the object from your hard drive.
   */
  protected abstract void restoreUnserializableObject();
  
  /**
   * You may implement this Method to make your class serializable.
   * This function is called directly before writing the object to your hard drive.
   */
  protected abstract void cleanupUnserializableObject();
  
  
  /**
   * This overrides the Method from java.io.Serializable. It calls the super method
   * and automatically calls cleanupUnserializableObject() each time the object is saved
   * through the serializable API, before the actual super method is called.
   * @param out
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    cleanupUnserializableObject();
    
    out.defaultWriteObject();
  }
  
  /**
   * This overrides the Method from java.io.Serializable. It calls the super method
   * and automatically calls restoreUnserializableObject() each time the object is loaded
   * through the serializable API.
   * @param in
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    
    restoreUnserializableObject();
    cacheChangedSinceLastLoading=false;
  }
  
}
