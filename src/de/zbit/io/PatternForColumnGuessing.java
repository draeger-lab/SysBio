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
package de.zbit.io;

import java.util.Arrays;
import java.util.regex.Pattern;

import de.zbit.io.csv.CSVReader;

/**
 * A class that counts the regex-matches for each column in a string array
 * (possible the content of a row in any table), and may return
 * the column with maximum number of matches.
 * <p>This is currently used in {@link CSVReader} to determine columns
 * by matching content.</p>
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class PatternForColumnGuessing {
  
  /**
   * Number of matches for each column
   */
  int[] matches;
  /**
   * Should be max in {@link #matches}
   */
  int matchesMax = 0;
  /**
   * Should be the column in {@link #matches} with {@link #matchesMax} matches.
   */
  int matchesMaxColumn = -1;
  
  /**
   * The pattern to match agains
   */
  Pattern pat;
  
  
  /**
   * 
   */
  public PatternForColumnGuessing(Pattern pattern, int numberOfColumns) {
    this.pat = pattern;
    matches = new int[numberOfColumns];
    Arrays.fill(matches, 0);
  }
  
  /**
   * Match the current pattern agains all columns in {@code line}
   * and increnment counters.
   * @param line
   */
  public void countMatches(String[] line) {
    if (pat==null || line==null) return;
    
    // Match against pattern
    for (int j=0; j<line.length; j++) {
      if (line[j]==null) continue;
      // Count for every column the number of matches
      
      if (pat.matcher(line[j]).matches()) {
        matches[j]++;
        if (matches[j]>matchesMax) {
          matchesMax = matches[j];
          matchesMaxColumn=j;
        }
      }
    }
  }
  
  /**
   * 
   * @return the maximum number of matches in any column.
   */
  public int getMaximumNumberOfMatchesInAnyColumn() {
    return matchesMax;
  }


  /**
   * @return the column with maximum number of matches or -1
   * if no match has been found in any column
   */
  public int getColumnWithMaximumNumberOfMatches() {
    return matchesMaxColumn;
  }
  
  /**
   * 
   * @return a copy of the number of matches for each column
   */
  public int[] getMatchesForAllColumns() {
    return Arrays.copyOf(matches, matches.length);
  }

  /**
   * @return true if at least one string in any column did match.
   */
  public boolean hasMatch() {
    return matchesMax>0;
  }

  /**
   * Parses all given {@link PatternForColumnGuessing}s and
   * returns column with absolute maximum of
   * {@link #getMaximumNumberOfMatchesInAnyColumn()}.
   * @param pat
   * @return column number with max matches or -1
   */
  public static int getColumnWithMaxMatches(PatternForColumnGuessing[] pat) {
    if (pat==null) return -1;
    int matchesMax=0, matchesMaxColNum=-1;
    for (PatternForColumnGuessing p: pat) {
      if (p.getMaximumNumberOfMatchesInAnyColumn()>matchesMax) {
        matchesMax = p.getMaximumNumberOfMatchesInAnyColumn();
        matchesMaxColNum = p.getColumnWithMaximumNumberOfMatches();
      }
    }
    return matchesMaxColNum;
  }
  
  
  
}
