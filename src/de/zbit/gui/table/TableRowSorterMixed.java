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
package de.zbit.gui.table;

import java.text.Collator;
import java.util.Comparator;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * This is an extension to the {@link TableRowSorter} that catches exceptions
 * during comparisons. This makes columns with mixed content still partially
 * sortable.
 * <p>E.g., if you have a column p-value that also contains strings
 * "n/a", you normally would get an exception and can't sort this column.
 * Using this class, you still can sort the p-values.
 * 
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
@SuppressWarnings("rawtypes")
public class TableRowSorterMixed<M extends TableModel> extends TableRowSorter<M> {
  
  /**
   * Comparator that uses compareTo on the contents.
   */
  private static final Comparator CHECKED_COMPARABLE_COMPARATOR =
          new CheckedComparableComparator();
  
  /**
   * Creates a {@code TableRowSorterMixed} with an empty model.
   */
  public TableRowSorterMixed() {
      super();
  }

  /**
   * Creates a {@code TableRowSorterMixed} using {@code model}
   * as the underlying {@code TableModel}.
   *
   * @param model the underlying {@code TableModel} to use,
   *        {@code null} is treated as an empty model
   */
  public TableRowSorterMixed(M model) {
    super(model);
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableRowSorter#getComparator(int)
   */
  @Override
  public Comparator<?> getComparator(int column) {
    Class columnClass = getModel().getColumnClass(column);
    if (columnClass == String.class) {
        return Collator.getInstance();
    } else if (Comparable.class.isAssignableFrom(columnClass)) {
      return CHECKED_COMPARABLE_COMPARATOR;
    } else {
      return super.getComparator(column);
    }
  }
  
  private static class CheckedComparableComparator implements Comparator {
  	/* (non-Javadoc)
  	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
  	 */
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2) {
      try {
        return ((Comparable)o1).compareTo(o2);
      } catch (Exception e) {
        // Any constant...
        try {
          return o1.toString().compareTo(o2.toString());
        } catch (Exception e2) { // Nullpointer
          return Integer.MIN_VALUE;
        }
      }
    }
  }
  
}
