/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011-2013 by the University of Tuebingen, Germany.
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A map then can have multiple values for one key.
 * 
 * @author Florian Mittag
 * @version $Rev$
 * @since 1.0
 */

public interface MultiMap<K, V> {

  /**
   * Returns the number of values in this multimap.
   * 
   * @return the number of values in this multimap
   */
  int size();

  
  /**
   * Returns {@code true}, if the multimap is empty, {@code false}
   * otherwise
   * 
   * @return {@code true}, if the multimap is empty
   */
  boolean isEmpty();


  /**
   * Adds a mapping from the given key to the given value. If there already is
   * such a mapping in the multimap, nothing happens. The return value of this
   * method reflects whether the multimap was changed, i.e. a mapping was
   * actually added.
   * 
   * @param key the key
   * @param value the value
   * @return Returns {@code true}, if there was no previous mapping from
   *         the given key to the given value.
   */
  boolean put(K key, V value);
  
  /**
   * Removes a mapping from the given key to the given value. If there is no
   * such a mapping in the multimap, nothing happens. The return value of this
   * method reflects whether the multimap was changed, i.e. a mapping was
   * actually removed.
   * 
   * @param key the key
   * @param value the value
   * @return Returns {@code true}, if there was a mapping from the given
   *         key to the given value, {@code false} otherwise.
   */
  boolean remove(K key, V value);
  
  /**
   * Returns all mappings for this key. Classes that implement this interface
   * need to make sure that changes to the returned collection do not affect the
   * multimap itself
   * 
   * @param key the key
   * @return all mappings for this key
   */
  Collection<V> get(K key);

  
  /**
   * Adds mappings from the given key to all given value. Mappings that already 
   * exist in the multimap are ignored. The return value of this method reflects
   * whether the multimap was changed, i.e. a mapping was actually added.
   * 
   * @param key the key
   * @param values the values
   * @return Returns {@code true}, if at least one mapping was added to the
   *         multimap.
   */
  boolean putAll(K key, Iterable<? extends V> values);
  
  /**
   * Adds all mappings from the given multimap to this multimap. Mappings that
   * already exist in the multimap are ignored. The return value of this method
   * reflects whether the multimap was changed, i.e. a mapping was actually
   * added.
   * 
   * @param multimap the multimap
   * @return Returns {@code true}, if at least one mapping was added to the
   *         multimap.
   */
  boolean putAll(MultiMap<? extends K, ? extends V> multimap);
  
  /**
   * Removes all mapping for the given key. If there is no mapping for the given
   * key, nothing happens. The return value of this method reflects whether the
   * multimap was changed, i.e. a mapping was actually removed.
   * 
   * 
   * @param key the key
   * @return Returns {@code true}, if there was a mapping from the given
   *         key to the given value, {@code false} otherwise.
   */
  Collection<V> removeAll(K key);
  
  
  /**
   * Returns {@code true} if at least on mapping for the given key exists
   * in this multimap.
   * 
   * @param key the key
   * @return {@code true} if at least on mapping for the given key exists
   *         in this multimap.
   */
  boolean containsKey(K key);

  /**
   * Returns {@code true} if at least on mapping to the given value exists
   * in this multimap.
   * 
   * @param value the value
   * @return {@code true} if at least on mapping to the given value exists
   *         in this multimap.
   */
  boolean containsValue(V value);

  /**
   * Returns {@code true} if the multimap contains a mapping the from the
   * given key to the given value, {@code false} otherwise.
   * 
   * @param key the key
   * @param value the value
   * @return {@code true} if the multimap contains a mapping the from the
   *         given key to the given value, {@code false} otherwise.
   */
  boolean containsEntry(K key, V value);


  /**
   * Removes all mappings from this multimap
   */
  void clear();


  /**
   * Returns a set of all keys for which a mapping exists in this multimap.
   * 
   * @return a set of all keys for which a mapping exists in this multimap.
   */
  Set<K> keySet();

  /**
   * Returns a collection of all values to which a mapping exists in this
   * multimap. Note that if there are multiple mappings to a value, the returned
   * collection will contain this value equally often.
   * 
   * @return a collection of all values to which a mapping exists
   */
  Collection<V> values();

  /**
   * Returns a collection of all mappings in this multimap.
   * 
   * @return a collection of all mappings in this multimap
   */
  Collection<Map.Entry<K, V>> entries();

}
