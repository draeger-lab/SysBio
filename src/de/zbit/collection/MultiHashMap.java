/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */

package de.zbit.collection;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Florian Mittag
 * @version $Rev$
 */

public class MultiHashMap<K, V> implements MultiMap<K, V> {

  protected Map<K, Collection<V>> map;
  
  public MultiHashMap() {
    map = new HashMap<K, Collection<V>>();
  }
  
  protected Collection<V> newCollection() {
    return new HashSet<V>();
  }
  
  
  
  public int size() {
    int size = 0;
    for( K key : map.keySet() ) {
      size += map.get(key).size();
    }
    
    return size;
  }

  
  
  public boolean isEmpty() {
    return size() == 0;
  }

  
  
  public boolean put(K key, V value) {
    Collection<V> coll = null;
    if( !map.containsKey(key) ) {
      map.put(key, newCollection());
    }
    
    coll = map.get(key);
    
    if( coll.contains(value) ) {
      return false;
    }

    coll.add(value);
    return true;
  }

  
  
  public boolean remove(K key, V value) {
    Collection<V> coll = null;
    if( !map.containsKey(key) ) {
      return false;
    }
    
    coll = map.get(key);
    if( coll.contains(value) ) {
      coll.remove(value);
      if( coll.isEmpty() ) {
        map.remove(key);
      }
      return true;
    }

    return false;
  }

  
  
  public Collection<V> get(K key) {
    if( !map.containsKey(key) ) {
      return null;
    }
    
    Collection<V> coll = newCollection();
    coll.addAll(map.get(key));
    
    return coll;
  }

  
  
  public boolean putAll(K key, Iterable<? extends V> values) {
    Collection<V> coll = null;
    if( !map.containsKey(key) ) {
      map.put(key, newCollection());
    }
    
    coll = map.get(key);
    boolean changed = false;
    for( V v : values ) {
      if( !coll.contains(v) ) {
        changed = true;
        coll.add(v);
      }
    }

    return changed;
  }

  
  
  public boolean putAll(MultiMap<? extends K, ? extends V> multimap) {
    boolean changed = false;
    for( Entry<? extends K, ? extends V> entry : multimap.entries() ) {
      changed |= put(entry.getKey(), entry.getValue());
    }
    return changed;
  }

  
  
  public Collection<V> removeAll(K key) {
    if( map.containsKey(key) ) {
      return map.remove(key);
    }
    
    return Collections.emptySet();
  }

  
  
  public boolean containsKey(K key) {
    return map.containsKey(key);
  }

  
  
  public boolean containsValue(V value) {
    return values().contains(value);
  }

  
  
  public boolean containsEntry(K key, V value) {
    if( map.containsKey(key) ) {
      return get(key).contains(value);
    }
    return false;
  }

  
  
  public void clear() {
    map.clear();
  }

  
  
  public Set<K> keySet() {
    return map.keySet();
  }

  
  
  public Collection<V> values() {
    List<V> coll = new ArrayList<V>();
    for( K key : map.keySet() ) {
      coll.addAll(map.get(key));
    }
    return coll;
  }

  
  
  public Collection<Entry<K, V>> entries() {
    Collection<Entry<K, V>> coll = new HashSet<Entry<K,V>>();
    for( K key : map.keySet() ) {
      for( V value : map.get(key) ) {
        coll.add(new AbstractMap.SimpleEntry<K, V>(key, value));
      }
    }
    return coll;
  }

}
