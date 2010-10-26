package de.zbit.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

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
 */
public class SortedArrayList<T> extends java.util.ArrayList<T>{
  /**
   * 
   */
  private static final long serialVersionUID = -5106143068070537940L;
  /**
   * Achtung: bei Not Found gibt er manchmal "0" zurueck!! Das muss gesondert gecheckt werden.
   */
  @SuppressWarnings("unchecked")
  public static <K> int binarySearch(SortedArrayList<K> a, K x) {
    int low = 0;
    int high = a.size() - 1;
    int mid = 1;

    while(low <= high) {
      mid = (low + high)/2;

      
      if (((Comparable)a.get(mid)).compareTo(x) < 0 )
        low = mid + 1;
      else if (((Comparable)a.get(mid)).compareTo(x) > 0 )
        high = mid - 1;
      else
        return mid;
    }
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
  @SuppressWarnings("unchecked")
  private static <K> int compare(SortedArrayList<K> a, K x, int index) {
    if (index>=a.size()) return -1; // ...eigentlich error thowen besser.
    return (((Comparable)a.get(index)).compareTo(x));
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    SortedArrayList<String[]> sal = new SortedArrayList<String[]>();
    
    String[] a = new String[12];
    for (int i=0; i<a.length; i++)
      a[i] = "";
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
    sal.add(new String[]{"n", "11"}); a[11] = "n"; Arrays.sort(a);
    
    
    for (int i=0; i<sal.size(); i++)
      System.out.print(((String[])sal.get(i))[1] + ((String[])sal.get(i))[0] + "-" + a[i] +  "\t");

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
    for (int i=0; i<a2.length; i++)
      a2[i] = "";
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

    
    for (int i=0; i<sal2.size(); i++)
      System.out.print(sal2.get(i) + "-" + a2[i] +  "\t");
    
    System.out.println("\n" + sal2.indexOf("fjd") + sal2.contains("fjd"));
    System.out.println(sal2.indexOf("&") + "" + sal2.contains("&"));
    System.out.println(sal2.indexOf("$") + "" + sal2.contains("$"));
    
  }

  /**
   * 
   */
  private int indexToSearch = 0;
  
  /**
   * 
   */
  private int indexOfLastAddedItem = -1;
  
  /**
   * 
   */
  public SortedArrayList() {
    super();
  }
  
  /**
   * 
   * @param i
   */
  public SortedArrayList(int i) {
    super(i);
  }
  
  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#add(java.lang.Object)
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean add(T s) {
    if (this.size()==0) {
      indexOfLastAddedItem = 0;
      return super.add(s);
    }
    
    int pos = 0;
    if (s.getClass().isArray()) {
      pos = Math.abs(binarySearchTabString(this, indexToSearch, s));
      if (((Comparable)Array.get(this.get(pos), indexToSearch)).compareTo(Array.get(s, indexToSearch))>0) {
        while(pos > -1 && ((Comparable)Array.get(this.get(pos), indexToSearch)).compareTo(Array.get(s, indexToSearch))>0)
          pos--;
        if (pos<this.size()) pos = pos+1;
      } else if (((Comparable)Array.get(this.get(pos), indexToSearch)).compareTo(Array.get(s, indexToSearch))<0) {
        while(pos >=0 && pos<this.size() && ((Comparable)Array.get(this.get(pos), indexToSearch)).compareTo(Array.get(s, indexToSearch))<0)
          pos++;
      }
    } else {
      pos = Math.abs(binarySearch(this, s));
      if (((Comparable)this.get(pos)).compareTo(s)>0) {
        while(pos > -1 && ((Comparable)this.get(pos)).compareTo(s)>0)
          pos--;
        if (pos<this.size()) pos = pos+1;
      } else if (((Comparable)this.get(pos)).compareTo(s)<0) {
        while(pos >=0 && pos<this.size() && ((Comparable)this.get(pos)).compareTo(s)<0)
          pos++;
      }
    }
    
   /* for (int i=0; i<this.size(); i++)
      System.out.print(this.get(i) + "\t");
    System.out.println();*/
    
    super.add(pos, s);
    indexOfLastAddedItem = pos;
    
    /*System.out.println(s + "=>" + pos);
    
    for (int i=0; i<this.size(); i++)
      System.out.print(this.get(i) + "\t");
    System.out.println("\n---------------");*/
    
    return true;
  }
  
  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#addAll(java.util.Collection)
   */
  @Override
  public boolean addAll(Collection<? extends T> c) {
    Iterator<? extends T> it = c.iterator();
    
    while(it.hasNext())
      this.add(it.next());

    return true;
  }
  
  /**
   * 
   * @param c
   * @return
   */
  public boolean addAll(T[] c)  {
    int i=-1;
    while (true) {
      i++;
      try {
        //Object o = Array.get(c, i);
        //if (!(o instanceof T)) throw new DataFormatException();
        //try {
          //add((T)o);
        //} catch (ClassCastException e) {
        add(c[i]);
      } catch (ArrayIndexOutOfBoundsException e) {
        break;
      }
    }
    
    return true;
  }
  
  /**
   * 
   * @param <K>
   * @param a
   * @param indexToSearch
   * @param x
   * @return
   */
  @SuppressWarnings("unchecked")
  public <K> int binarySearchNonArrayItemInTabString(SortedArrayList<K> a, int indexToSearch, K x) {
    int low = 0;
    int high = a.size() - 1;
    int mid = 1;

    while(low <= high) {
      mid = (low + high)/2;

      if(((Comparable)Array.get(a.get(mid), indexToSearch)).compareTo(x) < 0 )
        low = mid + 1;
      else if(((Comparable)Array.get(a.get(mid), indexToSearch)).compareTo(x) > 0 )
        high = mid - 1;
      else
        return mid;
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
  @SuppressWarnings("unchecked")
  public <K> int binarySearchTabString(SortedArrayList<K> a, int indexToSearch, K x) {
    int low = 0;
    int high = a.size() - 1;
    int mid = 1;

    while(low <= high) {
      mid = (low + high)/2;

      if(((Comparable)Array.get(a.get(mid), indexToSearch)).compareTo(Array.get(x, indexToSearch)) < 0 )
        low = mid + 1;
      else if(((Comparable)Array.get(a.get(mid), indexToSearch)).compareTo(Array.get(x, indexToSearch)) > 0 )
        high = mid - 1;
      else
        return mid;
    }
      return -Math.abs(mid);     // NOT_FOUND = -1
  }

  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#contains(java.lang.Object)
   */
  @Override
  public boolean contains(Object o) {
    if (this.size()==0) return false;

    int pos = this.indexOf(o);
    if (pos>=0) return true; else return false;
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
  @SuppressWarnings("unchecked")
  @Override
  public int indexOf(Object o) {
    if (this.size()==0) return -1;
    T s  = get(0);
    int pos = -1;
    
    if (s.getClass().isArray()) {
      if (!o.getClass().isArray()) {
        pos = binarySearchNonArrayItemInTabString(this, indexToSearch, (T)o);
        if (pos==0 && !Array.get(this.get(0), indexToSearch).equals(o)) return -1;
      } else {
        pos = binarySearchTabString(this, indexToSearch, (T)o);
        if (pos==0 && !Array.get(this.get(0), indexToSearch).equals(Array.get(o, indexToSearch))) return -1;
      }
    } else {
      if (!o.getClass().isArray())
        pos = binarySearch(this, (T)o);
      if (pos==0 && compare(this, (T)o, 0)!=0) {
        return -1; //((T)o).equals(this.get(0)) && !((T)o).equals(this.get(0).toString())) return -1;
      }
    }
    
    
    return pos;
  }
  
  /**
   * 
   * @param s
   * @return
   */
  public boolean isContained (String s) {
    if (this.size()<=0) return false;
    
    T s2 = get(0);
    if (s2.getClass().isArray()) {
      for (int i=0; i<this.size(); i++)
        if (s.contains(Array.get(get(i),indexToSearch).toString())) return true;
    } else {
      for (int i=0; i<this.size(); i++)
        if (s.contains(get(i).toString())) return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @param indexToSearch
   */
  public void setArrayIndexToSort(int indexToSearch) {
    this.indexToSearch = indexToSearch;
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
