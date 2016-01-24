/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Clemens Wrzodek
 * ACHTUNG: Nur die Methoden, welche ich benoetige implementiert. Nur add(T), addAll, indexOf(T) und contains(T), isContained,... (s.u.).
 *
 * Haelt eine ArrayList stets sortiert. Nutzt Quicksort und BinarySearch.
 * Kann mit ArrayList<Array> (z.B. ArrayList<String[]>) umgehen und sortiert diese dann nach gegebenen index im inneren Array (default 0).
 * Beispiel: ArrayList<String[]> - Elemente: String[a,z]; String[b,a] ; String[c,h] in der Reihenfolge.
 *
 * @param <T>
 * @version $Rev$
 * @since 1.0
 */
public class SortedArrayList<T> extends java.util.ArrayList<T>{
  private static final long serialVersionUID = -5106143068070537940L;
  /**
   * Achtung: bei Not Found gibt er manchmal "0" zurueck!! Das muss gesondert gecheckt werden.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <K> int binarySearch(SortedArrayList<K> a, Object x) {
    int low = 0;
    int high = a.size() - 1;
    int mid = 1;
    
    while(low <= high) {
      mid = (low + high)/2;
      
      
      if (((Comparable)a.get(mid)).compareTo(x) < 0 ) {
        low = mid + 1;
      } else if (((Comparable)a.get(mid)).compareTo(x) > 0 ) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    
    // Hier nicht -1 statt 0 oder pauschal -1 fuer not found zurueckgeben, da dieser
    // Wer auch fuer das Insert benutzt wird.
    return -Math.abs(mid);     // NOT_FOUND = -1
  }
  /**
   * 
   * @param <K>
   * @param a
   * @param x
   * @param index
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static <K> int compare(SortedArrayList<K> a, Object x, int index) {
    if (index>=a.size())
    {
      return -1; // ...eigentlich error thowen besser.
    }
    return (((Comparable)a.get(index)).compareTo(x));
  }
  
  /**
   * TESTs
   * @param args
   */
  public static void main(String[] args) {
    SortedArrayList<String[]> sal = new SortedArrayList<String[]>();
    
    String[] a = new String[12];
    for (int i=0; i<a.length; i++) {
      a[i] = "";
    }
    sal.add(new String[]{"Z", "0"}); a[0] = "Z";
    sal.add(new String[]{"z", "1"}); a[1] = "z";
    sal.add(new String[]{"j", "2"}); a[2] = "j";
    sal.add(new String[]{"a", "3"}); a[3] = "a";
    sal.add(new String[]{"k", "4"}); a[4] = "k";
    sal.add(new String[]{"3", "5"}); a[5] = "3";
    sal.add(new String[]{"Zz", "6"}); a[6] = "Zz";
    sal.add(new String[]{"fjd", "7"}); a[7] = "fjd";
    sal.add(new String[]{"&", "8"}); a[8] = "&";
    sal.add(new String[]{"o", "9"}); a[9] = "o";
    sal.add(new String[]{"f", "10"}); a[10] = "f";
    sal.add(new String[]{"n", "11"}); a[11] = "n";
    
    sal.put(new String[]{"o", "10"});
    
    Arrays.sort(a);
    for (int i=0; i<sal.size(); i++) {
      System.out.print(sal.get(i)[1] + sal.get(i)[0] + "-" + a[i] +  "\t");
    }
    
    System.out.println();
    System.out.println(sal.indexOf(new String[]{"fjd", "7"}));
    System.out.println(sal.indexOf(new String[]{"fjd", "5"}));
    System.out.println(sal.indexOf(new String[]{"fjd"}));
    System.out.println(sal.indexOf("fjd"));
    System.out.println(sal.indexOf("&") + "" + sal.contains("&"));
    System.out.println(sal.indexOf("$") + "" + sal.contains("$"));
    System.out.println(sal.indexOf(new String[]{"&", "7"}));
    System.out.println(sal.indexOf(new String[]{"$", "5"}));
    
    System.out.println("\n---------------------------\n");
    
    SortedArrayList<String> sal2 = new SortedArrayList<String>();
    
    String[] a2 = new String[12];
    for (int i=0; i<a2.length; i++) {
      a2[i] = "";
    }
    sal2.add("Z"); a2[0] = "Z";
    sal2.add("z"); a2[1] = "z";
    sal2.add("j"); a2[2] = "j";
    sal2.add("a2"); a2[3] = "a2";
    sal2.add("k"); a2[4] = "k";
    sal2.add("3"); a2[5] = "3";
    sal2.add("Zz"); a2[6] = "Zz";
    sal2.add("fjd"); a2[7] = "fjd";
    sal2.add("&"); a2[8] = "&";
    sal2.add("o"); a2[9] = "o";
    sal2.add("f"); a2[10] = "f";
    sal2.add("n"); a2[11] = "n"; Arrays.sort(a2);
    
    System.out.println("Putting already contained element: "+sal2.put("f"));
    
    
    for (int i=0; i<sal2.size(); i++) {
      System.out.print(sal2.get(i) + "-" + a2[i] +  "\t");
    }
    
    System.out.println("\n" + sal2.indexOf("fjd") + sal2.contains("fjd"));
    System.out.println(sal2.indexOf("&") + "" + sal2.contains("&"));
    System.out.println(sal2.indexOf("$") + "" + sal2.contains("$"));
    
  }
  
  /**
   * When arrays are being put into this list, this is the index
   * in the array that is being used to sort this list.
   */
  private int indexToSearch = 0;
  
  /**
   * The index of the last added item.
   */
  private int indexOfLastAddedItem = -1;
  
  /**
   * 
   */
  public SortedArrayList() {
    super();
  }
  
  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#ArrayList(int)
   */
  public SortedArrayList(int initialCapacity) {
    super(initialCapacity);
  }
  
  /**
   * <B>WARNING: </B>This contructor does ONLY WORK FOR
   * SORTED LISTS! Ensure to have called {@link Collections#sort(List)}
   * BEFORE calling this constructor.
   * @param c
   */
  public SortedArrayList(List<? extends T> c) {
    super(c);
    
  }
  
  /**
   * Returns the position where {@code s} should
   * be inserted.
   * @param s
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private int getInsertPosition(T s) {
    if (size()<=0) {
      return 0;
    }
    int pos = 0;
    if (s.getClass().isArray()) {
      pos = Math.abs(binarySearchTabString(this, indexToSearch, s));
      if (((Comparable)Array.get(get(pos), indexToSearch)).compareTo(Array.get(s, indexToSearch))>0) {
        while(pos > -1 && ((Comparable)Array.get(get(pos), indexToSearch)).compareTo(Array.get(s, indexToSearch))>0) {
          pos--;
        }
        if (pos<size()) {
          pos = pos+1;
        }
      } else if (((Comparable)Array.get(get(pos), indexToSearch)).compareTo(Array.get(s, indexToSearch))<0) {
        while(pos >=0 && pos<size() && ((Comparable)Array.get(get(pos), indexToSearch)).compareTo(Array.get(s, indexToSearch))<0) {
          pos++;
        }
      }
    } else {
      pos = Math.abs(binarySearch(this, s));
      if (((Comparable)get(pos)).compareTo(s)>0) {
        while(pos > -1 && ((Comparable)get(pos)).compareTo(s)>0) {
          pos--;
        }
        if (pos<size()) {
          pos = pos+1;
        }
      } else if (((Comparable)get(pos)).compareTo(s)<0) {
        while(pos >=0 && pos<size() && ((Comparable)get(pos)).compareTo(s)<0) {
          pos++;
        }
      }
    }
    return pos;
  }
  
  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#add(java.lang.Object)
   */
  @Override
  public boolean add(T s) {
    int pos = getInsertPosition(s);
    
    super.add(pos, s);
    indexOfLastAddedItem = pos;
    
    return true;
  }
  
  /**
   * Adds {@code s} to this list if and only if it is not already
   * in the list. This is a more efficient method than writing
   * <pre>if (!contains(s)) add(s)</pre>
   * <p>Compares the elements based on the compareTo method, and uses
   * only the {@link #indexToSearch} column in arrays (as always in
   * this implementation).
   * @see #add(Object)
   * @param s
   * @return true if and only if this item has been added to this list.
   */
  public boolean put(T s) {
    int pos = getInsertPosition(s);
    
    // Check if not already contained
    if (pos<size() && equals(get(pos), s)) {
      return false;
    }
    
    super.add(pos, s);
    indexOfLastAddedItem = pos;
    
    return true;
  }
  
  /**
   * Performs a {@code compareTo==0} check with the implemented special treatment
   * of arrays and using the compareTo, not the equals method!
   * @param element1
   * @param element2
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private boolean equals(T element1, T element2) {
    if (element2.getClass().isArray()) {
      if (((Comparable)Array.get(element1, indexToSearch)).compareTo(Array.get(element2, indexToSearch))==0) {
        return true;
      }
    } else {
      if (((Comparable)element1).compareTo(element2)==0) {
        return true;
      }
    }
    
    return false;
  }
  
  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#addAll(java.util.Collection)
   */
  @Override
  public boolean addAll(Collection<? extends T> c) {
    Iterator<? extends T> it = c.iterator();
    return addAll(it);
  }
  
  /**
   * See {@link #addAll(Collection)}
   * @param it
   * @return
   */
  public boolean addAll(Iterator<? extends T> it) {
    boolean hasChanged=false;
    while(it.hasNext()) {
      hasChanged = this.add(it.next());
    }
    
    return hasChanged;
  }
  
  /**
   * See {@link #addAll(Collection)}
   * @param c
   * @return
   */
  public boolean addAll(T[] c)  {
    boolean hasChanged=false;
    int i=-1;
    while (true) {
      i++;
      try {
        //Object o = Array.get(c, i);
        //if (!(o instanceof T)) throw new DataFormatException();
        //try {
        //add((T)o);
        //} catch (ClassCastException e) {
        hasChanged = add(c[i]);
      } catch (ArrayIndexOutOfBoundsException e) {
        break;
      }
    }
    
    return hasChanged;
  }
  
  /**
   * 
   * @param <K>
   * @param a
   * @param indexToSearch
   * @param x
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <K> int binarySearchNonArrayItemInTabString(SortedArrayList<K> a, int indexToSearch, Object x) {
    int low = 0;
    int high = a.size() - 1;
    int mid = 1;
    
    while(low <= high) {
      mid = (low + high)/2;
      
      if(((Comparable)Array.get(a.get(mid), indexToSearch)).compareTo(x) < 0 ) {
        low = mid + 1;
      } else if(((Comparable)Array.get(a.get(mid), indexToSearch)).compareTo(x) > 0 ) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -Math.abs(mid);     // NOT_FOUND = -1
  }
  
  
  /**
   * 
   * @param <K>
   * @param a
   * @param indexToSearch
   * @param x
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <K> int binarySearchTabString(SortedArrayList<K> a, int indexToSearch, Object x) {
    int low = 0;
    int high = a.size() - 1;
    int mid = 1;
    
    while(low <= high) {
      mid = (low + high)/2;
      
      if(((Comparable)Array.get(a.get(mid), indexToSearch)).compareTo(Array.get(x, indexToSearch)) < 0 ) {
        low = mid + 1;
      } else if(((Comparable)Array.get(a.get(mid), indexToSearch)).compareTo(Array.get(x, indexToSearch)) > 0 ) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -Math.abs(mid);     // NOT_FOUND = -1
  }
  
  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#contains(java.lang.Object)
   */
  @Override
  public boolean contains(Object o) {
    if (size()==0) {
      return false;
    }
    
    int pos = this.indexOf(o);
    if (pos>=0) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * 
   * @return
   */
  public int getIndexOfLastAddedItem() {
    return indexOfLastAddedItem;
  }
  
  
  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Override
  public int indexOf(Object o) {
    // TODO: Return FIRST, not any indexOf item to be
    // Conform with super.indexOf().
    if (size()==0) {
      return -1;
    }
    T s  = get(0);
    int pos = -1;
    
    if (s.getClass().isArray()) {
      if (!o.getClass().isArray()) {
        pos = binarySearchNonArrayItemInTabString(this, indexToSearch, o);
        if (pos==0 && !Array.get(get(0), indexToSearch).equals(o)) {
          return -1;
        }
      } else {
        pos = binarySearchTabString(this, indexToSearch, o);
        if (pos==0 && !Array.get(get(0), indexToSearch).equals(Array.get(o, indexToSearch))) {
          return -1;
        }
      }
    } else {
      if (!o.getClass().isArray()) {
        pos = binarySearch(this, o);
      }
      if (pos==0 && compare(this, o, 0)!=0) {
        return -1; //((T)o).equals(this.get(0)) && !((T)o).equals(this.get(0).toString())) return -1;
      }
    }
    
    
    return pos;
  }
  
  @Override
  public int lastIndexOf(Object o) {
    // TODO: Return LAST, not any indexOf item to be
    // Conform with super.indexOf().
    return indexOf(o);
  }
  
  /**
   * Returns true if and only if there is an element in this list, such that
   * the String representation of this element is contained in s.
   * I.e. "s.contains.( get(i).toString)".
   * @param s
   * @return
   */
  public boolean isContained (String s) {
    if (size()<=0) {
      return false;
    }
    
    T s2 = get(0);
    if (s2.getClass().isArray()) {
      for (int i=0; i<size(); i++) {
        if (s.contains(Array.get(get(i),indexToSearch).toString())) {
          return true;
        }
      }
    } else {
      for (int i=0; i<size(); i++) {
        if (s.contains(get(i).toString())) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  
  /**
   * 
   * @param indexToSearch
   * @throws Exception
   */
  public void setArrayIndexToSort(int indexToSearch) throws Exception {
    if (size()>1) {
      throw new Exception("Can only change array index to sort in empty list!");
    }
    this.indexToSearch = indexToSearch;
  }
  
  @Override
  public void add(int index, T element) {
    System.err.println("Add with specific index not allowed in " + getClass().getSimpleName());
    add(element);
  };
  
  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    System.err.println("Add with specific index not allowed in " + getClass().getSimpleName());
    return addAll(c);
  }
  
  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#toString()
   */
  @Override
  public String toString() {
    return super.toString();
  }
}
