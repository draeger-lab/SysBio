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
  private int maxListSize; // Unfortunately serialized in many instances. Don't rename it.
  private boolean cacheChangedSinceLastLoading=false;
  
  public InfoManagement() {
    this(100000);
  }
  
  /**
   * Constructor. Initialize this InfoManagement object with a maximum cache
   * size of <code>maxCacheSize</code> entries.
   * 
   * @param maxCacheSize the maximum number of cached entries
   */
  public InfoManagement(int maxCacheSize) {
    if (maxCacheSize<1) System.err.println("Initialized a InfoManagement cache with size " + maxCacheSize);
    this.maxListSize = maxCacheSize;
    rememberedInfos = new SortedArrayList<Info<IDtype, INFOtype>>(Math.max(this.maxListSize+1, 0));
    unsuccessfulQueries = new SortedArrayList<IDtype>((10*this.maxListSize)+1); // so many, since usually this does not take much memory.
  }
  
  /**
   * @return The number of remembered-infos and unsuccessful queries (as sum).
   */
  public int getNumberOfCachedIDs() {
    int sum = 0;
    if (rememberedInfos!=null) sum+=rememberedInfos.size();
    if (unsuccessfulQueries!=null) sum+=unsuccessfulQueries.size();
    return sum;
  }
  
  /**
   * @return Returns the number of remember-infos (no unsuccessful queries).
   */
  public int getNumberOfCachedInfos() {
    return rememberedInfos.size();
  }
  
  /**
   * @return the maximum number of elements, to store informations for.
   */
  public int getCacheSize() {
    return maxListSize;
  }

  /**
   * Set a new cache size. This is the maximum number of ids to
   * store infos for. The number of unsuccessful queries (ids
   * without infos) is not affected by the maximum cache size!
   * @param cacheSize
   */
  public synchronized void setCacheSize(int cacheSize) {
    this.maxListSize = cacheSize;
    rememberedInfos.ensureCapacity(maxListSize);
    unsuccessfulQueries.ensureCapacity(maxListSize);
  }

  /**
   * This method clears the unsuccessfulQueries array. In addition, it removes
   * all rememberedInfos that have a query(IDtype) or content(INFOtype) that is
   * a) null, b) .toString().equals("") or c) .toString().equals("0").
   * 
   * The intention of this method is to re-fetch empty or unsuccessfulQueries
   * what may be usefull if the underlying database is updated.
   */
  public synchronized void cleanupUnsuccessfulAndEmptyInfos() {
    if (unsuccessfulQueries.size()>0) {
      cacheChangedSinceLastLoading=true;
      unsuccessfulQueries.clear();
    }
    
    for (int i=0; i<rememberedInfos.size(); i++) {
      Info<IDtype, INFOtype> in = rememberedInfos.get(i);
      INFOtype info = in.getInformation(false);
      if (in.getIdentifier()==null || in.getIdentifier().toString().equals("") || in.getIdentifier().toString().equals("0") ||
          info==null || info.toString().equals("") || info.toString().equals("0") ){
        rememberedInfos.remove(i);
        i--;
        cacheChangedSinceLastLoading=true;
      }
    }
  }
  
  /**
   * This function re-sorts all internal id lists. It is usefull to call this function
   * if your compareTo (i.e. your sorting) has changed, to keep a consistent sorting of
   * your lists. It will also remove all duplicate ids. 
   */
  public synchronized void resortLists() {
    SortedArrayList<Info<IDtype, INFOtype>> tempRI = new SortedArrayList<Info<IDtype, INFOtype>>(Math.max(rememberedInfos.size(), maxListSize));
    SortedArrayList<IDtype> tempU = new SortedArrayList<IDtype>(Math.max(unsuccessfulQueries.size(), maxListSize));
    
    // Re-sort rememberedInfos
    for (Info<IDtype, INFOtype> i: rememberedInfos) {
      if (!tempRI.contains(i))
        tempRI.add(i);
    }
    
    // Re-sort unsuccessfulQueries
    for (IDtype i: unsuccessfulQueries) {
      if (!tempRI.contains(i) && !tempU.contains(i))
        tempU.add(i);
    }
    
    rememberedInfos = tempRI;
    unsuccessfulQueries = tempU;
    cacheChangedSinceLastLoading = true;
  }
  
  /**
   * Clears the whole cache (rememberedInfos and unsuccessfulQueries).
   */
  public synchronized void clearCache() {
    if (unsuccessfulQueries.size()>0 || rememberedInfos.size()>0) cacheChangedSinceLastLoading=true;
    unsuccessfulQueries.clear();
    rememberedInfos.clear();
  }
  
  /**
   * Returns whether this class has been changed since it has been initiated
   * or loaded from the hard drive or saved to hard drive.
   *
   * This does NOT reflect time stamp changes of the cached informations. Just
   * adding and removing items is reflected by this flag.
   * 
   * @return class has been changed since last readObject() (serializable loading),
   * writeObject() (serializable saving) or since initializing.
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
   * @param infoObject
   */
  public synchronized void addInformation(Info<IDtype, INFOtype> infoObject) {
    // Ensure constant max list capacity. Remove least frequently used item.
    if (isCacheFull()) {
      freeCache(Math.max(10, maxListSize/100));
    }
    
    rememberedInfos.add(infoObject);
    cacheChangedSinceLastLoading=true;
  }

  /**
   * @return true if and only if rememberedInfos.size() is at least maxListSize.
   */
  public boolean isCacheFull() {
    return (rememberedInfos.size()>=maxListSize);
  }
  
  /**
   * Removes the given number of elements from the cache.
   * Tries to remove the least used object first and to preserve
   * objects, that are recently added/used.
   * 
   * You should try to remove a bunch of elements at once,
   * because removing elements might take some time O(n).
   * 
   * @param elements - number of elements to remove.
   */
  private synchronized void freeCache(int elements) {
    // Sort the list by lastUsage and delete lowest time-stamps.
    ArrayList<Object> itemToDelete = new ArrayList<Object>(elements+1);
    SortedArrayList<Long> minDate = new SortedArrayList<Long>(elements+1);
    long minMaxDate = Long.MIN_VALUE;
    for (int i=0; i<rememberedInfos.size(); i++)  {
      long usageDate = rememberedInfos.get(i).getLastUsage();
      
      if(usageDate<=minMaxDate || itemToDelete.size() < elements) {
        minDate.add(usageDate);
        itemToDelete.add(minDate.getIndexOfLastAddedItem(), rememberedInfos.get(i));
        minMaxDate = minDate.get(0); // Math.min(elements-1, minDate.size()-1)
        // XXX: It would be good to have a break condition (i.e. good enough)
        // here, to speedup the whole process.
        //if (minMaxDate<1) break;
      }
    }
    
    // If some honks set list size to 0 or -1, itemToDelete is -1.
    if (itemToDelete.size()>=0)
      for (int i=0; i<Math.min(elements, itemToDelete.size()); i++)
        rememberedInfos.remove((Object)itemToDelete.get(i));
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
      INFOtype info = fetchInformationWrapper(id);
      if (info!=null) addInformation(id, info);
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
    ArrayList<IDtype> unknownIDs = new ArrayList<IDtype>();
    boolean touched = false; // if true, ids!=filteredIDs
    INFOtype anyCachedInfo=null;
    for (IDtype id: ids) {
      int pos = rememberedInfos.indexOf(id);
      if (pos<0 && !unsuccessfulQueries.contains(id)) { // Same if-order as below!
        unknownIDs.add(id);
      } else {
        touched = true;
        if (anyCachedInfo==null) anyCachedInfo=rememberedInfos.get(pos).getInformation(false);
      }
    }
    
    
    if (touched) {
      // Some elements are already in our cache.
      
      // Retain all elements that have to be fetched
      IDtype[] filtIDs = (IDtype[]) createNewArray(ids,unknownIDs.size());
      for (int i=0; i<unknownIDs.size(); i++)
        Array.set(filtIDs, i, unknownIDs.get(i));
      
      INFOtype[] newItems=null;
      if (unknownIDs.size()>0) {
        
        // Fetch new items
        newItems = fetchMultipleInformationWrapper(filtIDs);
        
        // Free enough cache for them
        if (isCacheFull())
          freeCache(Math.max(unknownIDs.size(), maxListSize/100));
      }
      
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
        } else if (unknownIDs.size()>0 && ids[i].equals(filtIDs[infos_i])){
          // Newly fetched infos (filteredIDs==0 if all in cache).
          if (newItems!=null && newItems.length<infos_i) {
            // should never happen. (=null => unsuccessfulQueries)
            System.err.println("Something went badly wrong. Your fetchMultipleInformations method must return an array of exactly the same size as the input id array!");
            infos[i] = null;
          } else if (newItems!=null){
            infos[i] = newItems[infos_i];
            if (newItems[infos_i]!=null) addInformation(ids[i], newItems[infos_i]);
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
      // No id is cached.
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
    ArrayList<IDtype> unknownIDs = new ArrayList<IDtype>();
    boolean touched = false; // if true, ids!=filteredIDs
    for (IDtype id: ids) {
      if (rememberedInfos.indexOf(id)<0 && !unsuccessfulQueries.contains(id)) {
        unknownIDs.add(id);
      } else {
        touched = true;
      }
    }
    if (unknownIDs.size()<1) return; // All ids are known.
    
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
      filtIDs = (IDtype[]) createNewArray(ids,unknownIDs.size());
      
      for (int i=0; i<filtIDs.length; i++)
        Array.set(filtIDs, i, unknownIDs.get(i));
    } // Else, all ids are unknown.
    
    // Query unknown ids
    infos = fetchMultipleInformationWrapper(filtIDs);
    if (infos==null) return;
    
    // Free enough cache for them
    if (isCacheFull())
      freeCache(Math.max(infos.length, maxListSize/100));
    
    // Add retrieved infos
    for (int i=0; i<infos.length; i++)
      if (infos[i]!=null && ids[i]!=null) addInformation(ids[i], infos[i]);
    
  }
  
  /**
   * Load an instance of the cache (InfoManagement) from the filesystem.
   * @param file
   * @return loaded InfoManagement instance.
   * @throws IOException 
   */
  public static synchronized InfoManagement<?, ?> loadFromFilesystem(File file) throws IOException {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObjectAutoDetectZIP(file);
    return m;
  }
  /**
   * Load an instance of the cache (InfoManagement) from the filesystem. The inputStream is not
   * buffered, so make sure to have it buffered before calling this function (for performance
   * reasons only).
   * @param in
   * @return loaded InfoManagement instance.
   * @throws IOException 
   */
  public static synchronized InfoManagement<?, ?> loadFromFilesystem(InputStream in) throws IOException {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObjectAutoDetectZIP(in);
    return m;
  }
  /**
   * Load an instance of the cache (InfoManagement) from the filesystem.
   * @param filepath
   * @return loaded InfoManagement instance.
   * @throws IOException 
   */
  public static synchronized InfoManagement<?, ?> loadFromFilesystem(String filepath) throws IOException {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObjectAutoDetectZIP(filepath);
    return m;
  }

  /**
   * Save the given instance of the cache (InfoManagement) as serialized object.
   * @param filepath
   * @param m - object to store.
   * @return true if and only if the file has been successfully saved.
   */
  public static synchronized boolean saveToFilesystem(String filepath, InfoManagement<?, ?> m) {
    return Utils.saveGZippedObject(filepath, m);
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
    cacheChangedSinceLastLoading=false;
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
