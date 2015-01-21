/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */

package de.zbit.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An extension to generic maps with additional convenience functions for
 * retrieving values.
 * 
 * @author Florian Mittag
 * @version $Rev$
 */

public class ConfigurationContainer implements Map<Object, Object> {

  protected Map<Object, Object> storage;
  
  public ConfigurationContainer() {
    storage = new HashMap<Object, Object>();
  }
  
  public ConfigurationContainer(Map<Object, Object> storage) {
    this.storage = storage;
  }

  /**
   * Returns the value to which the specified key is mapped,
   * or {@code defaultValue} if this map contains no mapping for the key.
   *
   * <p>More formally, if this map contains a mapping from a key
   * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
   * key.equals(k))}, then this method returns {@code v}; otherwise
   * it returns {@code defaultValue}.  (There can be at most one such mapping.)
   *
   * <p>If this map permits null values, then a return value of
   * {@code null} does not <i>necessarily</i> indicate that the map
   * contains no mapping for the key; it's also possible that the map
   * explicitly maps the key to {@code null}, in which case {@code null} will be
   * returned and not {@code defaultValue}.  The {@link #containsKey
   * containsKey} operation may be used to distinguish these two cases.
   *
   * @param key the key whose associated value is to be returned
   * @param defaultValue the default value to be returned if this map contains
   *        no mapping for the key
   * @return the value to which the specified key is mapped, or
   *         {@code defaultValue} if this map contains no mapping for the key
   * @throws ClassCastException if the key is of an inappropriate type for
   *         this map (optional)
   * @throws NullPointerException if the specified key is null and this map
   *         does not permit null keys (optional)
   */
  public Object get(Object key, Object defaultValue) {
    if( !containsKey(key) ) {
      return defaultValue;
    }
    return get(key);
  }
  
  public Boolean getBoolean(Object key, boolean defaultValue) {
    return (Boolean)get(key, defaultValue);
  }
  
  public Boolean getBoolean(Object key) {
    return getBoolean(key, false);
  }
  
  public Double getDouble(Object key, double defaultValue) {
    return (Double)get(key, defaultValue);
  }
  
  public Double getDouble(Object key) {
    return getDouble(key, 0.0);
  }

  public Float getFloat(Object key, float defaultValue) {
    return (Float)get(key, defaultValue);
  }
  
  public Float getFloat(Object key) {
    return getFloat(key, 0.0f);
  }

  public Integer getInt(Object key, int defaultValue) {
    return (Integer)get(key, defaultValue);
  }
  
  public Integer getInt(Object key) {
    return getInt(key, 0);
  }

  public Long getLong(Object key, long defaultValue) {
    return (Long)get(key, defaultValue);
  }
  
  public Long getLong(Object key) {
    return getLong(key, 0);
  }

  public String getString(Object key, String defaultValue) {
    return (String)get(key, defaultValue);
  }
  
  public String getString(Object key) {
    return getString(key, null);
  }

  /* (non-Javadoc)
   * @see java.util.Map#size()
   */
  @Override
  public int size() {
    return storage.size();
  }

  /* (non-Javadoc)
   * @see java.util.Map#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    return storage.isEmpty();
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  @Override
  public boolean containsKey(Object key) {
    return storage.containsKey(key);
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  @Override
  public boolean containsValue(Object value) {
    return storage.containsValue(value);
  }

  /* (non-Javadoc)
   * @see java.util.Map#get(java.lang.Object)
   */
  @Override
  public Object get(Object key) {
    return storage.get(key);
  }

  /* (non-Javadoc)
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  @Override
  public Object put(Object key, Object value) {
    return storage.put(key, value);
  }

  /* (non-Javadoc)
   * @see java.util.Map#remove(java.lang.Object)
   */
  @Override
  public Object remove(Object key) {
    return storage.remove(key);
  }

  /* (non-Javadoc)
   * @see java.util.Map#putAll(java.util.Map)
   */
  @Override
  public void putAll(Map<? extends Object, ? extends Object> m) {
    storage.putAll(m);
  }

  /* (non-Javadoc)
   * @see java.util.Map#clear()
   */
  @Override
  public void clear() {
    storage.clear();
  }

  /* (non-Javadoc)
   * @see java.util.Map#keySet()
   */
  @Override
  public Set<Object> keySet() {
    return storage.keySet();
  }

  /* (non-Javadoc)
   * @see java.util.Map#values()
   */
  @Override
  public Collection<Object> values() {
    return storage.values();
  }

  /* (non-Javadoc)
   * @see java.util.Map#entrySet()
   */
  @Override
  public Set<java.util.Map.Entry<Object, Object>> entrySet() {
    return storage.entrySet();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    return storage.equals(o);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return storage.hashCode();
  }

  
}
