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
package de.zbit.util;

import java.io.Serializable;

import javax.swing.ListModel;

import de.zbit.gui.ActionCommand;

/**
 * This class is a very generic override of the {@link #toString()} method. With
 * it, you can return any {@link String} and associate this string with an
 * {@link Object}.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class LabeledObject<T> implements ActionCommand, Cloneable,
    Comparable<LabeledObject<?>>, Serializable {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 2091985992659785789L;

  /**
   * Returns the index of <code>object</code> inside
   * {@link #getObject()} in <code>arr</code>.
   * @param arr array of {@link LabeledObject}s
   * @param object to search for (ignoring the label)
   * @return index of <code>object</code> in <code>arr</code>.
   */
  public static <T> int getIndexOfObject(LabeledObject<T>[] arr,
      T object) {
    for (int i=0; i<arr.length; i++) {
      if (arr[i].getObject().equals(object)) return i;
    }
    return -1;
  }
  
  /**
   * The same as {@link #getIndexOfObject(Object[], Object)}, but with
   * a list model.
   * @param model
   * @param object
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> int getIndexOfObject(ListModel model,T object) {
    for (int i=0; i<model.getSize(); i++) {
      Object o = model.getElementAt(i);
      if ((o instanceof LabeledObject) &&
         (((LabeledObject<T>)o).getObject().equals(object))) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * Does the same as {@link #getIndexOfObject(LabeledObject[], NameAndSignalsTab)}
   * but casts each element of <code>arr</code> to
   * {@link LabeledObject}.
   * @param arr
   * @param object
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> int getIndexOfObject(Object[] arr,
      T object) {
    for (int i=0; i<arr.length; i++) {
      if ((arr[i] instanceof LabeledObject) &&
         (((LabeledObject<T>)arr[i]).getObject().equals(object))) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * The label to display in the {@link #toString()} method.
   */
  private String label;
  
  /**
   * The object represented by the label.
   */
  private T object;
  
  /**
   * A tooltip to display additional information about the encapsulated
   * {@link Object}.
   */
  private String tooltip;

  /**
   * 
   * @param label
   * @param object
   * @param tooltip
   */
  public LabeledObject(String label, String tooltip, T object) {
    super();
    this.label = label;
    this.object = object;
    this.tooltip = tooltip;
  }

  /**
   * 
   * @param label
   * @param object
   */
  public LabeledObject(String label, T object) {
    this(label, null, object);
  }

  /**
   * 
   * @param object
   */
  public LabeledObject(T object) {
    this(object.toString(), object);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected LabeledObject<T> clone() {
    return new LabeledObject<T>(getLabel(), getToolTip(), getObject());
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(LabeledObject<?> o) {
    return label.compareTo(o.getLabel());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (obj.getClass().equals(getClass())) {
      LabeledObject lo = (LabeledObject) obj;
      boolean equal = lo.getObject().equals(getObject());
      equal &= isSetLabel() == lo.isSetLabel();
      if (equal && isSetLabel()) {
        equal &= lo.getLabel().equals(getLabel());
      }
      equal &= isSetToolTip() == lo.isSetToolTip();
      if (equal && isSetToolTip()) {
        equal &= lo.getToolTip().equals(getToolTip());
      }
      return equal;
    }
    return false;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getName()
   */
  public String getName() {
    return getLabel();
  }

  /**
   * @return the object
   */
  public T getObject() {
    return object;
  }

  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getToolTip()
   */
  public String getToolTip() {
    return tooltip;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 619;
    int hashCode = getClass().getName().hashCode();
    hashCode += prime * object.hashCode();
    if (isSetLabel()) {
      hashCode += prime * getLabel().hashCode();
    }
    if (isSetToolTip()) {
      hashCode += prime * getToolTip().hashCode();
    }
    return hashCode;
  }

  /**
   * 
   * @return
   */
  public boolean isSetLabel() {
    return label != null;
  }

  /**
   * 
   * @return
   */
  public boolean isSetToolTip() {
    return tooltip != null;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @param object the object to set
   */
  public void setObject(T object) {
    this.object = object;
  }
  
  /**
   * @param tooltip the tooltip to set
   */
  public void setToolTip(String tooltip) {
    this.tooltip = tooltip;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return label;
  }
  
}
