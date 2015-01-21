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
package de.zbit.gui.csv;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.zbit.io.PatternForColumnGuessing;
import de.zbit.io.csv.CSVReader;

/**
 * This class represents a column, that is expected as input
 * to {@link CSVImporterV2}.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ExpectedColumn implements Comparable<ExpectedColumn>, Serializable {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 110385651572193188L;

  /**
   * Name of the expected column (e.g., "Observation 1"). Might
   * get changed by user if {@link #renameAllowed} is true.
   */
  Object name;
  
  /**
   * Original, non-changeable name of the expected column (e.g., "Observation 1")
   */
  private final Object originalName;
  
  /**
   * If multiple types can be interpreted, a list of acceptable types.
   * E.g. "pValue", "Fold change",...
   */
  Object[] type = null;
  
  /**
   * Is this Column required or optional?
   */
  boolean required = true;
  
  /**
   * May multiple columns (e.g. with different types) be assigned to this column?
   * (e.g. an observation with pValue and fold change).
   */
  boolean multiSelectionAllowed = false;
  
  /**
   * Only if {@link #multiSelectionAllowed}, further restricts multiple selections to
   * selections with a different {@link #type}.
   */
  boolean multiSelectionOnlyWithDifferentType = true;
  
  /**
   * Is renaming of the {@link #name} allowed?
   */
  boolean renameAllowed = false;
  
  /**
   * Besides matching the column headers with {@link #name}, this can be
   * set to make an initial suggestion for a column.
   * (E.g., ".*_at" identifies a column with AffyMetrix probe identifiers).
   * <p>If you allow assigning {@link #type}s to columns, this may have
   * the same length as {@link #type}, to also suggest an initial type.
   */
  String[] regExPatternForInitialSuggestion = null;
  
  /**
   * A list of assigned columns. This list is only valid if the ok
   * button has been pressed on the Importer dialog!
   */
  List<Integer> assignedColumns = new ArrayList<Integer>();
  
  /**
   * A list of the {@link #type} indices that have been selected for each
   * column in the {@link #assignedColumns} (indices match!).
   */
  List<Integer> assignedTypeForEachColumn = new ArrayList<Integer>();
  
  /**
   * Create a new ExpectedColumn that expects a column
   * with the given name.
   * @param name
   */
  public ExpectedColumn(Object name) {
    super();
    this.name = name;
    this.originalName = name;
  }
  /**
   * @param name
   * @param required Is this Column required or optional?
   */
  public ExpectedColumn(Object name, boolean required) {
    this(name);
    this.required=required;
  }
  
  /**
   * @param name see {@link #name}
   * @param type see {@link #type}
   * @param required see {@link #required}
   * @param multiSelectionAllowed see {@link #multiSelectionAllowed}
   * @param multiSelectionOnlyWithDifferentType see {@link #multiSelectionOnlyWithDifferentType}
   * @param renameAllowed see {@link #renameAllowed}
   */
  public ExpectedColumn(Object name, Object[] type, boolean required,
    boolean multiSelectionAllowed, boolean multiSelectionOnlyWithDifferentType,
    boolean renameAllowed) {
    this(name,required);
    this.type = type;
    this.multiSelectionAllowed = multiSelectionAllowed;
    this.multiSelectionOnlyWithDifferentType = multiSelectionOnlyWithDifferentType;
    this.renameAllowed = renameAllowed;
  }
  
  /**
   * @param name see {@link #name}
   * @param type see {@link #type}
   * @param required see {@link #required}
   * @param multiSelectionAllowed see {@link #multiSelectionAllowed}
   * @param multiSelectionOnlyWithDifferentType see {@link #multiSelectionOnlyWithDifferentType}
   * @param renameAllowed see {@link #renameAllowed}
   * @param regExPatternForInitialSuggestion see {@link #regExPatternForInitialSuggestion}
   */
  public ExpectedColumn(Object name, Object[] type, boolean required,
    boolean multiSelectionAllowed, boolean multiSelectionOnlyWithDifferentType,
    boolean renameAllowed, String... regExPatternForInitialSuggestion) {
    this(name, type, required, multiSelectionAllowed, multiSelectionOnlyWithDifferentType, renameAllowed);
    setRegExPatternForInitialSuggestion(regExPatternForInitialSuggestion);
  }
  
  /**
   * 
   * @param name
   * @param required Is this Column required or optional?
   * @param regExPatternForInitialSuggestion
   */
  public ExpectedColumn(Object name, boolean required, String regExPatternForInitialSuggestion) {
    this (name, required);
    setRegExPatternForInitialSuggestion(regExPatternForInitialSuggestion);
  }
  /**
   * @return true if and only if {@link #type} contains more than one element. 
   */
  public boolean isSetTypeSelection() {
    return (type != null) && (type.length > 0);
  }
  
  /**
   * @return a {@link ComboBoxModel} for {@link #type}.
   */
  public ComboBoxModel getTypeComboBoxModel() {
    return new DefaultComboBoxModel(type);
  }
  
  /**
   * Suggests an initial selected column for this expected column.
   * <p>May return -1 if no column can be suggested.
   * @param r
   * @return
   * @see #getInitialSuggestions(CSVReader)
   */
  public int getInitialSuggestion(CSVReader r) {
    int sug = r.getColumn(name.toString());
    if (isSetRegExPatternForInitialSuggestion()) {
      try {
        // match agains all patterns and return column with max matches
        PatternForColumnGuessing[] pat = r.getColumnByMatchingContent(regExPatternForInitialSuggestion,0,250);
        sug = PatternForColumnGuessing.getColumnWithMaxMatches(pat);
      } catch (IOException e) {}
    }
    return sug;
  }
  
  /**
   * Suggest <b>one</b> initial column <b>for each {@link #type}</b>.
   * <p>Only if {@link #hasRegexPatternForEachType()}!<br/>
   * If you don't use {@link #type} or speciefied just one {@link #regExPatternForInitialSuggestion}
   * please use {@link #getInitialSuggestion(CSVReader)}.
   * @param r
   * @return int array matching indices to {@link #type}, containing initial
   * suggested column number. Or {@code null} if something went wrong.
   * @see #getInitialSuggestion(CSVReader)
   * @see #hasRegexPatternForEachType()
   */
  public int[] getInitialSuggestions(CSVReader r) {
    int[] ret = new int[type.length];
    for (int i=0; i<ret.length; i++) {
      ret[i] = -1;
      ret[i] = r.getColumn(type[i].toString());
    }
    
    if (isSetRegExPatternForInitialSuggestion()) {
      try {
        // match agains all patterns and return column with max matches
        PatternForColumnGuessing[] pat = r.getColumnByMatchingContent(regExPatternForInitialSuggestion,0,250);
        if (type!=null && type.length==regExPatternForInitialSuggestion.length) {
          for (int i=0;i<pat.length; i++) {
            int sug = pat[i].getColumnWithMaximumNumberOfMatches();
            if (sug>=0) {
              ret[i] = sug;
            }
          }
        }
      } catch (IOException e) {}
      return ret;
    }
    return null;
  }
  
  /**
   * @return
   */
  public boolean isSetRegExPatternForInitialSuggestion() {
    if (regExPatternForInitialSuggestion==null||regExPatternForInitialSuggestion.length<1) {
      return false;
    }
    for (String s: regExPatternForInitialSuggestion) {
      if (s!=null && s.length()>0) return true;
    }
    return false;
  }
  
  /**
   * Set a regular expression pattern for a columns content
   * to be assigned as initial suggestion.
   * @param regEx if you specify multiples, the indicies are linked
   * to the {@link #type}. If you have no different types, they are
   * all synonyms.
   */
  public void setRegExPatternForInitialSuggestion (String... regEx) {
    regExPatternForInitialSuggestion = regEx;
  }
  
  /**
   * Define, if this is an optional or required column.
   * @param required
   */
  public void setRequired(boolean required) {
    this.required = required;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name.toString() + (required? "*" : "");
  }
  /**
   * A list of assigned columns. This list is only valid if the ok
   * button has been pressed on the Importer dialog!
   * @return the assignedColumns
   */
  public List<Integer> getAssignedColumns() {
    return assignedColumns;
  }
  /**
   * A list of the {@link #type} indices that have been selected for each
   * column in the {@link #assignedColumns} (indices match!).
   * @return the assignedTypeForEachColumn
   */
  public List<Integer> getAssignedTypeForEachColumn() {
    return assignedTypeForEachColumn;
  }
  
  /**
   * @return the (maybe modified) name of this expected column
   */
  public Object getName() {
    return name;
  }
  
  /**
   * This is a convenient wrapper the returns directly the assigned
   * type.
   * 
   * <p>Note:<br/>This method does only work if {@link #multiSelectionAllowed}=false!
   * @return <pre>{@link #type}[{@link #getAssignedTypeForEachColumn()}.get(0)]</pre>
   */
  public Object getAssignedType() {
    return getAssignedType(0);
  }
  
  /**
   * 
   * @param assignedColumnIndex
   * @return
   */
  public Object getAssignedType(int assignedColumnIndex) {
    if (type!=null && assignedTypeForEachColumn!=null &&
        assignedTypeForEachColumn.size()>0) {
      int idx = assignedTypeForEachColumn.get(assignedColumnIndex);
      if (idx<type.length) return type[idx];
    }
    return null;
  }
  
  
  /**
   * This is a convenient wrapper that returns directly the
   * assigned column or -1 if none has been assigned. 
   * 
   * <p>Note:<br/>This method does only work if {@link #multiSelectionAllowed}=false!
   * @return <pre>{@link #getAssignedColumns()}.get(0)</pre>
   */
  public int getAssignedColumn() {
    if (assignedColumns!=null && assignedColumns.size()>0) {
      return assignedColumns.get(0);
    }
    return -1;
  }
  
  /**
   * For optional columns, this returns wether this {@link ExpectedColumn}
   * has assigned columns or not.
   * @return true if and only if {@link #getAssignedColumns()}.size()>0
   */
  public boolean hasAssignedColumns() {
    return assignedColumns!=null && assignedColumns.size()>0;
  }
  
  /**
   * @return true if and only if this {@link ExpectedColumn} has not been
   * renamed.
   */
  public boolean isOriginalName() {
    return originalName.equals(name);
  }
  
  /**
   * @return true if this expected column has been renamed.
   */
  public boolean isRenamed() {
    return !isOriginalName();
  }
  
  /**
   * @return the originally assigned name.
   */
  public Object getOriginalName() {
    return originalName;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(ExpectedColumn o) {
    return originalName.toString().compareTo(o.getOriginalName().toString());
  }
  /**
   * @return {@code true} if this {@link ExpectedColumn} has multiple {@link #type}s
   * and exactly the same numbers of {@link #regExPatternForInitialSuggestion}.
   */
  public boolean hasRegexPatternForEachType() {
    return isSetRegExPatternForInitialSuggestion() &&
    isSetTypeSelection() && type.length==regExPatternForInitialSuggestion.length;
  }
  
  /**
   * Copy assigned values from another instance. Just copies values, that
   * can be changed by the user. Not properties or configurations of this
   * instance.
   * <p>You MUST take care that {@code cache} refers to an {@link ExpectedColumn}
   * of the same type as this instance with same configuration
   * (especially the {@link #originalName}).
   * @param cache another instance of this class with the same configuration.
   */
  public void copyAssignedValuesFrom(ExpectedColumn cache) {
    this.name = cache.name;
    this.assignedColumns = cache.assignedColumns;
    this.assignedTypeForEachColumn = cache.assignedTypeForEachColumn;
  }
  /**
   * Checks if all {@link #assignedColumns} match the template
   * {@link #regExPatternForInitialSuggestion}.
   * @param anyContentLine any line, representing content of the CSV file.
   * The regEX will be checked agains this line.
   * @return {@code false} if any only if we have a regEx pattern,
   * we have assigned columns, and the given regEX matches the
   * assigned column (in {@code anyContentLine}).
   */
  public boolean regEXmatches(String[] anyContentLine) {
    if (!isSetRegExPatternForInitialSuggestion() ||
        !hasAssignedColumns()) {
      // a) No regex = emtpy = always matches
      // b) If we have no column assignments, we can't check this
      return true;
    }
    
    // We must perform this check for all columns
    for (int i=0; i<assignedColumns.size(); i++) {
      
      // Get regEx pattern for this column
      String regEx = regExPatternForInitialSuggestion[0];
      if (hasRegexPatternForEachType()) {
        regEx = regExPatternForInitialSuggestion[assignedTypeForEachColumn.get(i)];
      }
      
      int column = assignedColumns.get(i);
      if (column<anyContentLine.length &&
          !Pattern.matches(regEx, anyContentLine[column])) {
        return false;
      }
    }
    
    return true;
  }
  
}
