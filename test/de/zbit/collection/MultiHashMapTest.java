/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011-2014 by the University of Tuebingen, Germany.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Mittag
 * @version $Rev$
 */

public class MultiHashMapTest {

  MultiHashMap<Integer, String> map;
  

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    map = new MultiHashMap<Integer, String>();
  }


  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#size()}.
   */
  @Test
  public void testSize() {
    assertEquals("New multimap should be empty", 0, map.size());
    
    map.put(1, "A");
    assertEquals(1, map.size());

    map.put(1, "B");
    map.put(1, "B");
    assertEquals(2, map.size());

    map.put(2, "A");
    assertEquals(3, map.size());
    
    map.remove(2, "A");
    assertEquals(2, map.size());
    
    map.removeAll(1);
    assertEquals(0, map.size());
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#isEmpty()}.
   */
  @Test
  public void testIsEmpty() {
    assertTrue("New multimap should be empty", map.isEmpty());
    
    map.put(1, "A");
    assertFalse(map.isEmpty());

    map.put(1, "B");
    map.put(1, "B");
    assertFalse(map.isEmpty());

    map.put(2, "A");
    assertFalse(map.isEmpty());
    
    map.remove(2, "A");
    assertFalse(map.isEmpty());
    
    map.removeAll(1);
    assertTrue(map.isEmpty());
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#put(java.lang.Object, java.lang.Object)}.
   */
  @Test
  public void testPut() {
    assertTrue( map.put(1, "A") );
    assertTrue( map.containsEntry(1, "A") );

    assertTrue( map.put(1, "B") );
    assertTrue( map.containsEntry(1, "B") );
    assertFalse( map.put(1, "B") );
    assertTrue( map.containsEntry(1, "B") );

    assertTrue( map.put(2, "A") );
    assertTrue( map.containsEntry(2, "A") );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#remove(java.lang.Object, java.lang.Object)}.
   */
  @Test
  public void testRemove() {
    assertFalse( map.remove(1, "A") );
    
    map.put(1, "A");
    assertTrue( map.remove(1, "A") );
    assertFalse( map.containsEntry(1, "A") );

    map.put(1, "B");
    map.put(1, "B");
    assertTrue( map.remove(1, "B") );
    assertFalse( map.containsEntry(1, "A") );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#get(java.lang.Object)}.
   */
  @Test
  public void testGet() {
    map.put(1, "A");
    Collection<String> result1 = map.get(1);
    assertTrue( result1.contains("A") );
    assertFalse( result1.contains("B") );

    map.put(1, "B");
    result1 = map.get(1);
    assertTrue( result1.contains("A") );
    assertTrue( result1.contains("B") );

    map.put(1, "B");
    result1 = map.get(1);
    assertTrue( result1.contains("A") );
    assertTrue( result1.contains("B") );

    map.remove(1, "B");
    result1 = map.get(1);
    assertTrue( result1.contains("A") );
    assertFalse( result1.contains("B") );
    
    map.put(2, "A");
    Collection<String> result2 = map.get(2);
    assertTrue( result2.contains("A") );
    assertFalse( result2.contains("B") );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#putAll(java.lang.Object, java.lang.Iterable)}.
   */
  @Test
  public void testPutAllKIterableOfQextendsV() {
    List<String> iter = new ArrayList<String>();
    iter.add("A");
    iter.add("C");
    iter.add("B");
    
    assertTrue( map.putAll(3, iter) );
    assertEquals( 3, map.size() );
    
    Collection<String> result = map.get(3);
    assertEquals( 3, result.size() );
    assertTrue( result.contains("A") );
    assertTrue( result.contains("B") );
    assertTrue( result.contains("C") );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#putAll(de.zbit.collection.MultiMap)}.
   */
  @Test
  public void testPutAllMultiMapOfQextendsKQextendsV() {
    MultiMap<Integer, String> other = new MultiHashMap<Integer, String>();
    other.put(1, "A");
    other.put(2, "A");
    other.put(2, "B");
    other.put(3, "C");
    
    assertTrue( map.putAll(other) );

    assertEquals( 4, map.size() );
    assertTrue( map.containsEntry(1, "A") );
    assertTrue( map.containsEntry(2, "A") );
    assertTrue( map.containsEntry(2, "B") );
    assertTrue( map.containsEntry(3, "C") );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#removeAll(java.lang.Object)}.
   */
  @Test
  public void testRemoveAll() {
    map.put(1, "A");
    map.put(2, "A");
    map.put(2, "B");
    map.put(3, "C");
    
    Collection<String> result2 = map.removeAll(2);
    assertEquals( 2, map.size() );
    assertEquals( 2, result2.size() );
    assertTrue( result2.contains("A") );
    assertTrue( result2.contains("B") );
    
    Collection<String> emptyResult = map.removeAll(2);
    assertEquals( 2, map.size() );
    assertEquals( 0, emptyResult.size() );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#containsKey(java.lang.Object)}.
   */
  @Test
  public void testContainsKey() {
    map.put(1, "A");
    map.put(-2, "A");
    
    assertTrue( map.containsKey(-2) );
    assertTrue( map.containsKey(1) );
    assertFalse( map.containsKey(0) );
    assertFalse( map.containsKey(2) );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#containsValue(java.lang.Object)}.
   */
  @Test
  public void testContainsValue() {
    map.put(1, "A");
    map.put(2, "A");
    map.put(2, "B");
    map.put(3, "C");
    
    assertTrue( map.containsValue("A") );
    assertTrue( map.containsValue("B") );
    assertTrue( map.containsValue("C") );

    map.removeAll(2);
    assertTrue( map.containsValue("A") );
    assertFalse( map.containsValue("B") );
    assertTrue( map.containsValue("C") );
    
    map.remove(3, "A");
    assertTrue( map.containsValue("A") );
    assertFalse( map.containsValue("B") );
    assertTrue( map.containsValue("C") );
    
    map.removeAll(1);
    assertFalse( map.containsValue("A") );
    assertFalse( map.containsValue("B") );
    assertTrue( map.containsValue("C") );

    map.clear();
    assertFalse( map.containsValue("A") );
    assertFalse( map.containsValue("B") );
    assertFalse( map.containsValue("C") );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#containsEntry(java.lang.Object, java.lang.Object)}.
   */
  @Test
  public void testContainsEntry() {
    map.put(1, "A");
    map.put(2, "A");
    map.put(2, "B");
    map.put(3, "C");
    
    assertTrue( map.containsEntry(1, "A") );
    assertTrue( map.containsEntry(2, "A") );
    assertTrue( map.containsEntry(2, "B") );
    assertTrue( map.containsEntry(3, "C") );
    assertFalse( map.containsEntry(1, "B") );

    map.removeAll(2);
    assertTrue( map.containsEntry(1, "A") );
    assertFalse( map.containsEntry(2, "A") );
    assertFalse( map.containsEntry(2, "B") );
    assertTrue( map.containsEntry(3, "C") );
    assertFalse( map.containsEntry(1, "B") );
    
    map.remove(3, "A");
    assertTrue( map.containsEntry(1, "A") );
    assertFalse( map.containsEntry(2, "A") );
    assertFalse( map.containsEntry(2, "B") );
    assertTrue( map.containsEntry(3, "C") );
    assertFalse( map.containsEntry(1, "B") );

    map.remove(3, "C");
    assertTrue( map.containsEntry(1, "A") );
    assertFalse( map.containsEntry(2, "A") );
    assertFalse( map.containsEntry(2, "B") );
    assertFalse( map.containsEntry(3, "C") );
    assertFalse( map.containsEntry(1, "B") );

    map.removeAll(1);
    assertFalse( map.containsEntry(1, "A") );
    assertFalse( map.containsEntry(2, "A") );
    assertFalse( map.containsEntry(2, "B") );
    assertFalse( map.containsEntry(3, "C") );
    assertFalse( map.containsEntry(1, "B") );

    map.clear();
    assertFalse( map.containsEntry(1, "A") );
    assertFalse( map.containsEntry(2, "A") );
    assertFalse( map.containsEntry(2, "B") );
    assertFalse( map.containsEntry(3, "C") );
    assertFalse( map.containsEntry(1, "B") );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#clear()}.
   */
  @Test
  public void testClear() {
    map.put(1, "A");
    map.put(2, "A");
    map.put(2, "B");
    map.put(3, "C");
    assertEquals( 4, map.size() );

    map.clear();
    assertEquals( 0, map.size() );
    assertFalse( map.containsEntry(1, "A") );
    assertFalse( map.containsEntry(2, "A") );
    assertFalse( map.containsEntry(2, "B") );
    assertFalse( map.containsEntry(3, "C") );
    assertFalse( map.containsEntry(1, "B") );    
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#keySet()}.
   */
  @Test
  public void testKeySet() {
    
    Set<Integer> emptySet = map.keySet();
    assertTrue( emptySet.isEmpty() );
    
    map.put(1, "A");
    map.put(2, "A");
    Set<Integer> keySet = map.keySet();
    assertEquals( 2, keySet.size() );
    assertTrue( keySet.contains(1) );
    assertTrue( keySet.contains(2) );
    
    map.put(2, "B");
    keySet = map.keySet();
    assertEquals( 2, keySet.size() );
    assertTrue( keySet.contains(1) );
    assertTrue( keySet.contains(2) );
    
    map.put(3, "C");
    keySet = map.keySet();
    assertEquals( 3, keySet.size() );
    assertTrue( keySet.contains(1) );
    assertTrue( keySet.contains(2) );
    assertTrue( keySet.contains(3) );
    
    map.remove(1, "B");
    keySet = map.keySet();
    assertEquals( 3, keySet.size() );
    assertTrue( keySet.contains(1) );
    assertTrue( keySet.contains(2) );
    assertTrue( keySet.contains(3) );
    
    map.remove(1, "A");
    keySet = map.keySet();
    assertEquals( 2, keySet.size() );
    assertFalse( keySet.contains(1) );
    assertTrue( keySet.contains(2) );
    assertTrue( keySet.contains(3) );
  }
  
  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#values()}.
   */
  @Test
  public void testValues() {
    map.put(1, "A");
    map.put(2, "A");
    map.put(2, "B");
    map.put(3, "C");

    Collection<String> result = map.values();
    assertEquals(4, result.size() );
    assertTrue( result.contains("A") );
    assertTrue( result.contains("B") );
    assertTrue( result.contains("C") );
    
    int count = 0;
    for( String s : result ) {
      if( "A".equals(s) ) {
        count++;
      }
    }
    assertEquals( 2, count );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#entries()}.
   */
  @Test
  public void testEntries() {
    map.put(1, "A");
    map.put(2, "A");
    map.put(2, "B");
    map.put(3, "C");
    
    Collection<Entry<Integer, String>> entries = map.entries();
    assertEquals( 4, entries.size() );
    boolean[] seen = new boolean[]{ false, false, false, false };
    boolean other = false;
    for( Entry<Integer, String> e : entries ) {
      if( e.getKey() == 1 && e.getValue().equals("A") ) {
        seen[0] = true;
      } else if( e.getKey() == 2 && e.getValue().equals("A") ) {
        seen[1] = true;
      } else if( e.getKey() == 2 && e.getValue().equals("B") ) {
        seen[2] = true;
      } else if( e.getKey() == 3 && e.getValue().equals("C") ) {
        seen[3] = true;
      } else {
        other = true;
      }
    }
    
    for( int i = 0; i < 4; i++ ) {
      assertTrue( seen[i] );
    }
    assertFalse( other );
  }

  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#equals(java.lang.Object)}.
   */
  @Test
  public void testEquals() {
    fail("Not yet implemented");
  }
  
  /**
   * Test method for {@link de.zbit.collection.MultiHashMap#hashCode()}.
   */
  @Test
  public void testHashCode() {
    fail("Not yet implemented");
  }
}
