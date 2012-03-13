/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.util.SortedArrayList;
import de.zbit.util.Utils;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This class is intended to reduce network traffic or hard disk load, by
 * remembering a certain number of most used elements, instead of retrieving
 * them each time again and again.
 * 
 * A better name for this class is maybe "Cache".
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public abstract class InfoManagement<IDtype extends Comparable<?> & Serializable, INFOtype extends Serializable> implements Serializable {
  /**
   * It is recommended to generate a new ID when extending this class.
   */
  private static final long serialVersionUID = -5172273501517643495L;
  /**
   * Initialize the logger for this class
   */
  public static final transient Logger log = Logger.getLogger(InfoManagement.class.getName());
  
  /**
   * Actual cache content
   */
  private Map<IDtype, ObjectAndTimestamp<INFOtype>> rememberedInfos;
  /**
   * Additional cache to remember unsuccessful queries
   */
  private Set<IDtype> unsuccessfulQueries; // Remember those separately
  
  /**
   * Cache size limit
   */
  private int maxListSize; // Unfortunately serialized in many instances. Don't rename it.
  
  /**
   * If true, the cache has changed since last reading/writing
   * and should be saved to disk upon exit.
   */
  private transient boolean cacheChangedSinceLastLoading=false;
  
  /**
   * Version number of this java class.
   */
  private final static int latestVersion=2;
  
  /**
   * Allows to change older caches (when reading serialized files)
   * if the version number changed.
   */
  private int version=latestVersion;
  
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
    if (maxCacheSize<1) {
      log.warning("Initialized a InfoManagement cache with size of " + maxCacheSize);
    }
    this.maxListSize = maxCacheSize;
    rememberedInfos = new HashMap<IDtype, ObjectAndTimestamp<INFOtype>>(Math.max(this.maxListSize+1, 1));
    unsuccessfulQueries = new HashSet<IDtype>((10*this.maxListSize)+1); // so many, since usually this does not take much memory.
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
    return rememberedInfos==null?0:rememberedInfos.size();
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
  public void setCacheSize(int cacheSize) {
    this.maxListSize = cacheSize;
  }
  
  /**
   * Clears the whole cache (rememberedInfos and unsuccessfulQueries).
   */
  public void clearCache() {
    cacheChangedSinceLastLoading |= (unsuccessfulQueries.size()>0 || rememberedInfos.size()>0);
    synchronized (rememberedInfos) {
      rememberedInfos.clear();
    }
    synchronized (unsuccessfulQueries) {
      unsuccessfulQueries.clear();
    }
  }
  
  /**
   * Clears the unsuccessfulQueries (Queries with no, or no valid, response).
   */
  public void clearFailCache() {
    cacheChangedSinceLastLoading |= (unsuccessfulQueries.size()>0);
    synchronized (unsuccessfulQueries) {
      unsuccessfulQueries.clear();
    }
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
  public void addInformation(IDtype id, INFOtype info) {
    addInformation(id, new ObjectAndTimestamp<INFOtype>(info));
  }
  
  /**
   * Adds the given information. It is intended, that this function does NOT check if the information
   * is already available.
   * @param infoObject
   */
  private void addInformation(IDtype id, ObjectAndTimestamp<INFOtype> info) {
    // Ensure constant max list capacity. Remove least frequently used item.
    if (isCacheFull()) {
      freeCache(Math.max(10, (int)(((double)maxListSize)*0.1)));
    }
    synchronized (rememberedInfos) {
      rememberedInfos.put(id, info);
    }
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
   * <p>You should try to remove a bunch of elements at once,
   * because removing elements might take some time O(n).
   * 
   * @param elements number of elements to remove.
   */
  private void freeCache(int elements) {
    // Sort the list by lastUsage and delete lowest time-stamps.
    List<IDtype> keysToDelete = new ArrayList<IDtype>(elements+1);
    SortedArrayList<Long> minDate = new SortedArrayList<Long>(elements+1);
    long maxMinDate = Long.MAX_VALUE;
    
    int removedElements =0;
    synchronized (rememberedInfos) {
      Iterator<Entry<IDtype, ObjectAndTimestamp<INFOtype>>> it = rememberedInfos.entrySet().iterator();
      while (it.hasNext()) {
        Entry<IDtype, ObjectAndTimestamp<INFOtype>> entry = it.next();
        long usageDate = entry.getValue().getLastUsage();
        
        if(usageDate<maxMinDate || keysToDelete.size() < elements) {
          minDate.add(usageDate);
          keysToDelete.add(minDate.getIndexOfLastAddedItem(), entry.getKey());
          maxMinDate = minDate.get(minDate.size()-1);
          // XXX: It would be good to have a break condition (i.e. good enough)
          // here, to speedup the whole process.
          //if (minMaxDate<1) break;
        }
      }
      
      // If some honks set list size to 0 or -1, itemToDelete is -1.
      if (keysToDelete.size()>=0) {
        for (int i=0; i<Math.min(elements, keysToDelete.size()); i++) {
          if (rememberedInfos.remove(keysToDelete.get(i))!=null) {
            removedElements++;
          }
        }
      }
    }
    
    log.fine(String.format("Removed %s elements from %s-Cache.", removedElements, getClass().getName()));
  }
  
  /**
   * Removes one element from the cache. 
   * @param id
   * @return true, if the element has been found and removed. False instead.
   */
  public boolean removeInformation(IDtype id) {
    boolean found = false;
    synchronized (rememberedInfos) {
      found = rememberedInfos.remove(id)!=null;
    }
    
    if (!found) {
      synchronized (unsuccessfulQueries) {
        found = unsuccessfulQueries.remove(id);
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
          synchronized (unsuccessfulQueries) {
            unsuccessfulQueries.add(id);
          }
          cacheChangedSinceLastLoading=true;
        }
        break;
      } catch (TimeoutException e) {
        retried++;
        if (retried>=3) {
          log.info("3 attempts failed with a TimeoutException");
          e.printStackTrace();
          break;
        }
      } catch (UnsuccessfulRetrieveException e) {
        log.log(Level.FINE, "Unsuccessful retrieval, marking " + 
          (id ==null?"null": id.toString()) + " as unretrievable", e);
        synchronized (unsuccessfulQueries) {
          unsuccessfulQueries.add(id);
        }
        cacheChangedSinceLastLoading=true;
        break;
      } catch (Throwable t) {
        // do NOT retry and do NOT save anything... simply return the null
        // This may happen e.g. if this class is used to manage db queries
        // and the user or database is offline.
        log.log(Level.WARNING, "Catched an unknown exception while fetching informations", t);
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
   * Overwrite this method if you want to use a {@link AbstractProgressBar}.
   * @param ids
   * @param progress
   * @return
   * @throws TimeoutException
   * @throws UnsuccessfulRetrieveException
   * @see #fetchMultipleInformations(Comparable[])
   */
  protected INFOtype[] fetchMultipleInformations(IDtype[] ids, AbstractProgressBar progress) throws TimeoutException, UnsuccessfulRetrieveException {
    return fetchMultipleInformations(ids);
  }
  
  
  /**
   * This will fetch all given ids (NOT using cache) and handle the UnsuccessfulRetrieveException
   * and TimeoutException exceptions. It will update the unsuccessfulQueries() collection and
   * return the resulting infos.
   * @param ids
   * @param progress 
   * @return
   */
  private INFOtype[] fetchMultipleInformationWrapper(IDtype[] ids, AbstractProgressBar progress) {
    // you should already have checked for "unsuccessfulQueries" when using this function.
    INFOtype[] ret = null;
    if (ids==null) return ret;
    
    int retried=0;
    while (ret==null) {
      try {
        ret = fetchMultipleInformations(ids, progress);
        
        // Cache the unsuccessfulQueries
        if (ret==null) {
          synchronized (unsuccessfulQueries) {
            unsuccessfulQueries.addAll(Arrays.asList(ids));
          }
          cacheChangedSinceLastLoading=true;
        } else {
          for (int i=0; i<ids.length; i++) {
            if (ret[i]==null && ids[i]!=null && !ids[i].equals("")) {
              //log.fine(String.format("No info for id '%s'.", ids[i]));
              synchronized (unsuccessfulQueries) {
                unsuccessfulQueries.add(ids[i]);
              }
              cacheChangedSinceLastLoading=true;
            }
          }
        }
        break;
      } catch (TimeoutException e) {
        retried++;
        if (retried>=3) {
          log.log(Level.INFO, "3 attempts failed for all with a TimeoutException", e);
          e.printStackTrace();
          break;
        }
      } catch (UnsuccessfulRetrieveException e) {
        String example = null;
        if (ids!=null && ids.length>0) {
          example = (ids[0]==null?"null":ids[0].toString());
        }
        log.log(Level.FINE, "Unsuccessful retrieval, marking ALL IDs as unretrievable"+
          (example!=null?" (e.g., '" +example+"')": ""), e);
        synchronized (unsuccessfulQueries) {
          unsuccessfulQueries.addAll(Arrays.asList(ids));
        }
        cacheChangedSinceLastLoading=true;
        break;
      } catch (Throwable t) {
        // do NOT retry and do NOT save anything... simply return the null
        // This may happen e.g. if this class is used to manage db queries
        // and the user or database is offline.
        log.log(Level.WARNING, "Catched an unknown exception while fetching multiple informations", t);
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
  public INFOtype getInformation(IDtype id) {
    ObjectAndTimestamp<INFOtype> o = rememberedInfos.get(id);
    if (o!=null) return o.getInformation();
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
  public INFOtype[] getInformations(IDtype[] ids) {
    return getInformations(ids, null);
  }
  
  /**
   * Retrieve multiple informations. This will used the cached information, if available. Else,
   * it will call the fetchMultipleInformation method and build a cache on the answer. The
   * returned results use the same indices as the given ids. 
   * @param ids ids to query.
   * @param progress optional aditional progress bar (might be null)
   * @return INFOtype array of same size, with same ordering as ids.
   */
  @SuppressWarnings("unchecked")
  public INFOtype[] getInformations(IDtype[] ids, AbstractProgressBar progress) {
    if (ids==null) return null;
    List<IDtype> unknownIDs = new ArrayList<IDtype>();
    
    // Look if at least one of the ids is in the cache
    INFOtype anyCachedInfo=null;
    for (IDtype id: ids) {
      if (id==null) continue;
      ObjectAndTimestamp<INFOtype> o = rememberedInfos.get(id);
      if (o==null && !unsuccessfulQueries.contains(id)) { // Same if-order as below!
        unknownIDs.add(id);
      } else {
        if (anyCachedInfo==null) anyCachedInfo=o.getInformation(false);
      }
    }
    
    if (anyCachedInfo!=null) {
      // Some elements are already in our cache.
      
      // Retain all elements that have to be fetched
      IDtype[] filtIDs = (IDtype[]) createNewArray(ids,unknownIDs.size());
      for (int i=0; i<unknownIDs.size(); i++)
        Array.set(filtIDs, i, unknownIDs.get(i));
      
      INFOtype[] newItems=null;
      if (unknownIDs.size()>0) {
        
        // Fetch new items
        newItems = fetchMultipleInformationWrapper(filtIDs, progress);
        
        // Free enough cache for them
        if (isCacheFull()) {
          freeCache(Math.max(Math.max(10, unknownIDs.size()), (int)(((double)maxListSize)*0.1)));
        }
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
        ObjectAndTimestamp<INFOtype> o = rememberedInfos.get(ids[i]);
        if (o!=null) { // Same if-order as above!
          infos[i] = o.getInformation();
        } else if (unknownIDs.size()>0 && ids[i].equals(filtIDs[infos_i])){
          // Newly fetched infos (filteredIDs==0 if all in cache).
          if (newItems!=null && newItems.length<infos_i) {
            // should never happen. (=null => unsuccessfulQueries)
            log.warning("Something went badly wrong. Your fetchMultipleInformations method must return an array of exactly the same size as the input id array!");
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
      INFOtype[] infos = fetchMultipleInformationWrapper(ids, progress);
      if (infos==null) return null;
      for (int i=0; i<infos.length; i++)
        if (infos[i]!=null && ids[i]!=null) addInformation(ids[i], infos[i]);
      return infos;
    }
  }
  
  /**
   * Precache ids, so they are available as soon as you query them.
   * This is useful if you know that you are going to query multiple ids with
   * a single query. Than you can precache them here, which results in on fetchMulti
   * query, and later on retrieve them with getInformation().
   * This will significantly increase performance, since you queried all with one query.
   * @param ids
   */
  public void precacheIDs(IDtype[] ids) {
    precacheIDs(ids,null);
  }
  
  /**
   * Precache ids, so they are available as soon as you query them.
   * This is useful if you know that you are going to query multiple ids with
   * a single query. Than you can precache them here, which results in on fetchMulti
   * query, and later on retrieve them with getInformation().
   * This will significantly increase performance, since you queried all with one query.
   * @param ids
   * @param progress optional additional progress bar for this operation.
   */
  @SuppressWarnings("unchecked")
  public void precacheIDs(IDtype[] ids, AbstractProgressBar progress) {
    if (ids==null || ids.length<1) return;
    List<IDtype> unknownIDs = new ArrayList<IDtype>();
    
    // Look if at least one of the ids is in the cache
    boolean containsAtLeastOneID=false;
    for (IDtype id: ids) {
      if (id==null) continue;
      ObjectAndTimestamp<INFOtype> o = rememberedInfos.get(id);
      if (o==null && !unsuccessfulQueries.contains(id)) { // Same if-order as below!
        unknownIDs.add(id);
      } else {
        containsAtLeastOneID = true;
      }
    }
    if (unknownIDs.size()<1) return; // All ids are known.
    
    INFOtype[] infos;
    IDtype[] filtIDs = ids;
    if (containsAtLeastOneID) {
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
    if (filtIDs.length<1) return;
    
    // Query unknown ids
    infos = fetchMultipleInformationWrapper(filtIDs, progress);
    if (infos==null) return;
    
    // Free enough cache for them
    if (isCacheFull()) {
      freeCache(Math.max(Math.max(10, infos.length), (int)(((double)maxListSize)*0.1)));
    }
    
    // Add retrieved infos
    for (int i=0; i<infos.length; i++) {
      if (infos[i]!=null && filtIDs[i]!=null) addInformation(filtIDs[i], infos[i]);
    }
  }
  
  /**
   * Load an instance of the cache (InfoManagement) from the filesystem.
   * @param file
   * @return loaded InfoManagement instance.
   * @throws IOException 
   */
  public static InfoManagement<?, ?> loadFromFilesystem(File file) throws IOException {
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
  public static InfoManagement<?, ?> loadFromFilesystem(InputStream in) throws IOException {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObjectAutoDetectZIP(in);
    return m;
  }
  /**
   * Load an instance of the cache (InfoManagement) from the filesystem.
   * @param filepath
   * @return loaded InfoManagement instance.
   * @throws IOException 
   */
  public static InfoManagement<?, ?> loadFromFilesystem(String filepath) throws IOException {
    InfoManagement<?, ?> m = (InfoManagement<?, ?>)Utils.loadObjectAutoDetectZIP(filepath);
    return m;
  }

  /**
   * Save the given instance of the cache (InfoManagement) as serialized object.
   * @param filepath
   * @param m object to store.
   * @return true if and only if the file has been successfully saved.
   */
  public static boolean saveToFilesystem(String filepath, InfoManagement<?, ?> m) {
    boolean ret = Utils.saveGZippedObject(filepath, m);
    if (ret) {
      // reset cache changed flag
      m.cacheChangedSinceLastLoading = false;
    }
    return ret;
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
  @SuppressWarnings("rawtypes")
  private static Object createNewArray(Object type, int size) {
    Class elementType = null;
    if (type instanceof Class) {
      elementType = (Class) type;
    } else if (type.getClass().isArray()) {
      elementType = type.getClass().getComponentType();
    }
    
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
    synchronized (rememberedInfos) {
      synchronized (unsuccessfulQueries) {
        cleanupUnserializableObject();
        
        out.defaultWriteObject();
      }
    }
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
    try {
      in.defaultReadObject();
    } catch (Exception exc) {
      throw new IOException("Could not read cache from disk.", exc);
    }
    
    restoreUnserializableObject();
    cacheChangedSinceLastLoading=false;
    
    // Eventually change old file for compatibility with latest release
    if (version != latestVersion) {
      if (version < 1) {
        clearCache();
      }
      
      version = latestVersion;
    }
  }
  
}
