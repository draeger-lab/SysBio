/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.zbit.util.objectwrapper.ValuePair;

/**
 * Class containing frequently used utility functions for String processing.
 * 
 * @author Florian Mittag
 * @author Clemens Wrzodek
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.0
 */
public class StringUtil {
	
	/**
	 * 
	 */
	public static final String DECIMAL_FORMAT = "###0.######";
	
	/**
	 * 
	 */
	public static final String REAL_FORMAT = "########.###############";
	/**
	 * 
	 */
	public static final String SCIENTIFIC_FORMAT = "###0.######E0";
	
	
  /**
	 * The file separator of the operating system.
	 */
	private static final char fileSeparator = System.getProperty("file.separator").charAt(0);
	
	/**
	 * New line separator of this operating system
	 */
  private static final String newLine = System.getProperty("line.separator");
	
	/**
   * The location for texts of labels. 
   */
  public static final String RESOURCE_LOCATION_FOR_LABELS = "de.zbit.locales.Labels";

  /**
   * The location for warning message texts.
   */
	public static final String RESOURCE_LOCATION_FOR_WARNINGS = "de.zbit.locales.Warnings";
  

	/**
   * The number of symbols per line in tool tip texts.
   */
  public static int TOOLTIP_LINE_LENGTH = 60;
  
	/**
	 * Adds a prefix or/and suffix to each element.
	 * @param list
	 * @param prefix - may be null.
	 * @param suffix - may be null.
	 * @return List<String> - the list.
	 */
	public static List<String> addPrefixAndSuffix(Iterable<?> list, String prefix, String suffix) {
	  List<String> ret = new LinkedList<String>();
	  // Simply define an empty string is faster than checking in the loop each time if it's null.
	  if (prefix == null) prefix = "";
	  if (suffix == null) suffix = "";
	  for (Object string : list) {
	    ret.add(prefix + string.toString() + suffix);
	  }
	  return ret;
	}
	
	/**
	 * 
	 * @param sb
	 * @param whatToAppend
	 * @return
	 */
	public static StringBuilder append(StringBuilder sb, Object... whatToAppend) {
		for (Object o : whatToAppend) {
			sb.append(o.toString());
		}
		return sb;
	}

	/**
	 * Returns a {@link String} whose first letter has been changed depending on
	 * the second argument to an upper or lower case character. Depending on the
	 * third argument, the rest of the given name {@link String} is turned into a
	 * lower-case {@link String} or kept the same.
	 * 
	 * @param name
	 * @param upperCase
	 * @param othersToLowerCase
	 * @return
	 */
	public static String changeFirstLetterCase(String name, boolean upperCase,
		boolean othersToLowerCase) {
	  if (name.length()<1) return name;
		char c = name.charAt(0);
		if (Character.isLetter(c)) {
			c = upperCase ? Character.toUpperCase(c) : Character.toLowerCase(c);
		}
		if (name.length() > 1) {
			name = Character.toString(c)
					+ (othersToLowerCase ? name.substring(1).toLowerCase() : name
							.substring(1));
		} else {
			return Character.toString(c);
		}
		return name;
	}

	/**
	 * Returns a new {@link StringBuilder} object containing the given objects in
	 * a concatenated form.
	 * 
	 * @param objects the objects to concatenate
	 * @return the passed objects concatenated in a new {@link StringBuilder}
	 */
	public static StringBuilder concat(Object... objects) {
		return append(new StringBuilder(), objects);
	}

	
	/**
   * Checks if {@code parentString} contains any of the strings
   * in {@code strings}.
   * @param strings to search for
   * @param parentString to search in
   * @return index of (first) string of {@code strings}
   * that is contained in {@code parentString}, or -1. 
   */
  public static int containsAny(String[] strings, String parentString) {
      if (strings==null) return -1;
      for (int i=0; i<strings.length; i++) {
        if (parentString==null) {
          // Also detect indexOf null
          if (strings[i]==null) return i;
          else continue;
        }
        if (strings[i]==null) continue;
        if (parentString.contains(strings[i])) return i;
      }
      return -1;
  }
	
	/**
   * @param   source      the string to search.
   * @param   str         the substring for which to search.
   * @return
   */
  public static boolean containsIgnoreCase(String source, String str) {
    return indexOfIgnoreCase(source, str)>=0;
  }
	
	/**
	 * This method introduces left and right quotation marks where we normally
	 * have straight quotation marks.
	 * 
	 * @param text
	 * @param leftQuotationMark
	 * @param rightQuotationMark
	 * @return
	 */
	public static String correctQuotationMarks(String text,
		String leftQuotationMark, String rightQuotationMark) {
		boolean opening = true;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '"') {
				if (opening) {
					text = text.substring(0, Math.max(0, i - 1)) + leftQuotationMark
							+ text.substring(i + 1);
					opening = false;
				} else {
					text = text.substring(0, Math.max(0, i - 1)) + rightQuotationMark
							+ text.substring(i + 1);
					opening = true;
				}
			}
		}
		return text;
	}
	
	/**
   * @param string
   * @param toCount
   * @return number of occurrences of {@code toCount} in {@code string}.
   */
  public static int countChar(String string, char toCount) {
    int counter = 0;
    if (string!=null) {
      char[] arr = string.toCharArray();
      for (char c2: arr) {
        if (c2==toCount) counter++;
      }
    }
    return counter;
  }
	
	/**
   * @param string
   * @param toCount
   * @return number of occurrences of {@code toCount} in {@code string}.
   */
  public static int countString(String string, String toCount) {
    int counter = 0;
    int pos = 0;
    if (string!=null) {
      while (pos>=0) {
        pos = string.indexOf(toCount, pos);
        if (pos>=0) {
          counter++;
          pos++;
        }
      }
    }
    return counter;
  }
	
	/**
   * Will change a regular variable name such as 'cmdOptions' to 'CMD_OPTIONS'.
   * 
   * @param name
   * @return
   */
  public static String createKeyFromField(String name) {
    StringBuilder sb = new StringBuilder();
    char ch;
    for (int i=0; i<name.length(); i++) {
      ch = name.charAt(i);
      if (Character.isLowerCase(ch)) {
        sb.append(Character.toUpperCase(ch));
      } else if (Character.isUpperCase(ch)) {
        if (i < name.length()) {
          sb.append('_');
        }
        sb.append(ch);
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }
	
//	/**
//	 * Returns the name of a given month.
//	 * 
//	 * @param month
//	 * @return
//	 */
//	public static String getMonthName(short month) {
//		switch (month) {
//			case 1:
//				return "January";
//			case 2:
//				return "February";
//			case 3:
//				return "March";
//			case 4:
//				return "April";
//			case 5:
//				return "May";
//			case 6:
//				return "June";
//			case 7:
//				return "July";
//			case 8:
//				return "August";
//			case 9:
//				return "September";
//			case 10:
//				return "October";
//			case 11:
//				return "November";
//			case 12:
//				return "December";
//			default:
//				return "invalid month " + month;
//		}
//	}
	
	
	/**
	 * Escape all HTML Characters in {@code string}.
	 * @param string
	 * @return string with escaped characters.
	 */
	public static String escapeHTMLchars(String string) {
	  return EscapeChars.forHTML(string);
	}
	
	/**
	 * 
	 * @return
	 */
	public static char fileSeparator() {
		return fileSeparator;
	}
	
	/**
	 * Return the given string filled up to the given length with the given
	 * character. If the length of the input string is equal or greater than the
	 * target length, it will be return unchanged, i.e., it will NOT be shortened.
	 * The {@code prepend} parameter defines whether the fill character
	 * should be prepended (or appended).
	 * 
	 * @param input
	 *        the input string to fill up
	 * @param len
	 *        the target length of the filled up string
	 * @param fill
	 *        the fill character
	 * @param prepend
	 *        whether the filling characters should be prepended
	 * @return the filled up string
	 */
	public static String fill(String input, int len, char fill, boolean prepend) {
		if (input == null) {
			input = "";
		}
		// if the input string already has target size or is longer, do nothing
		if (len <= input.length()) { return input; }
		
		char[] cs = new char[len - input.length()];
		Arrays.fill(cs, fill);
		
		return prepend ? (new String(cs) + input) : input + (new String(cs));
	}
	
	/**
	 * Returns a lower-case {@link String} who's first letter is now in lower
	 * case.
	 * 
	 * @param name
	 * @return
	 */
	public static String firstLetterLowerCase(String name) {
		return changeFirstLetterCase(name, false, true);
	}
	
	/**
	 * Returns a lower-case {@link String} who's first letter is now in upper
	 * case.
	 * 
	 * @param name
	 * @return
	 */
	public static String firstLetterUpperCase(String name) {
		return changeFirstLetterCase(name, true, true);
	}
	
	/**
	 * Changes an optionName by replacing all underscores with a space, setting
	 * the whole string to lower case and changing the first letter to upper case.
	 * E.g., "REMOVE_ORPHANS" => "Remove orphans"
	 * 
	 * @param optionName
	 * @return reformatted option string
	 */
	public static String formatOptionName(String optionName) {
		String ret = optionName;
		ret = ret.replace("_", " ");
		ret = ret.toLowerCase().trim();
		ret = Character.toUpperCase(ret.charAt(0)) + ret.substring(1);
		return ret;
	}
	
	/**
	 * Returns the given column (O(n) implementation).
	 * 
	 * @param data
	 *        [rows][cols]
	 * @param col
	 *        - col number to return
	 * @return
	 */
	public static String[] getColumn(String[][] data, int col) {
		String[] ret = new String[data.length];
		for (int i = 0; i < data.length; i++)
			if (data[i] == null || data[i].length <= col)
				ret[i] = null;
			else ret[i] = data[i][col];
		return ret;
	}
	
	/**
	 * Returns the longest common length and the number of it's occurrences. If
	 * multiple lengths occur equally often, prefers the longer length. Null
	 * strings are treated as length=0.
	 * 
	 * @param b
	 * @return an integer array of size 2. [0]=The longest common String length
	 *         [1]=The number of strings in b with that length.
	 */
	public static int[] getLongestCommonLength(String... b) {
		/*
		 * Can be implemented in o(n*n) runtime and o(1) memory usage OR o(n)
		 * runtime and o(n) memory usage. This is the latter implementation.
		 */
		SortedArrayList<int[]> LengthAndCounts = new SortedArrayList<int[]>();
		int maxLength = 0;
		int maxLengthOcc = 0;
		for (int l = 0; l < b.length; l++) {
			int length = 0;
			if (b[l] != null) {
				length = b[l].length();
			}
			
			// Create new element or increment by one
			int pos = LengthAndCounts.indexOf(length);
			int newCounter = 1;
			if (pos < 0)
				LengthAndCounts.add(new int[] { length, newCounter });
			else {
				newCounter = (LengthAndCounts.get(pos)[1] + 1);
				LengthAndCounts.set(pos, new int[] { length, newCounter });
			}
			
			// Remember maximum occuring length
			if (newCounter > maxLengthOcc || newCounter == maxLengthOcc
					&& length > maxLength) {
				maxLengthOcc = newCounter;
				maxLength = length;
			}
			
		}
		
		return new int[] { maxLength, maxLengthOcc };
	}
	
	/**
	 * Returns the longest common prefix of both strings. This method is
	 * case-sensitive.
	 * 
	 * @param a
	 *        string a
	 * @param b
	 *        string b
	 * @return the longest common prefix
	 */
	public static String getLongestCommonPrefix(String a, String b) {
		int i;
		for (i = 0; i < Math.min(a.length(), b.length()); i++) {
			if (a.charAt(i) != b.charAt(i)) break;
		}
		return a.substring(0, i);
	}
	
	public static String getLongestCommonPrefix(String a, String[] b,
		boolean ignoreEmptyStrings) {
		int i;
		
		// Get minimum String length in b
		int minLength = a.length();
		minLength = Math.min(getMinimumLength(b, ignoreEmptyStrings), minLength);
		
		// Iterate through all positions, until it does not match.
		boolean breakIt = false;
		for (i = 0; i < minLength; i++) {
			char c = a.charAt(i);
			for (int l = 0; l < b.length; l++) {
				// !ignoreEmptyStrings already handled.
				if (b[l] == null || b[l].length() < 1) continue;
				
				if (c != b[l].charAt(i)) {
					breakIt = true;
					break;
				}
			}
			if (breakIt) break;
		}
		
		return a.substring(0, i);
	}
	
	/**
	 * Get the longest common prefix.
	 * <p>Note: the LCP of ALG13 and ALG14 is ALG1... just
	 * for your consideration.</p>
	 * @param b
	 * @param ignoreEmptyStrings
	 * @return the longest common prefix of all elements in b.
	 */
	public static String getLongestCommonPrefix(String[] b, boolean ignoreEmptyStrings) {
		int i;
		
		// Get minimum String length in b
		int minLength = getMinimumLength(b, ignoreEmptyStrings);
		
		// Iterate through all positions, until it does not match.
		int nonEmptyId = -1;
		boolean breakIt = false;
		for (i = 0; i < minLength; i++) {
			char c = '\u0000';
			for (int l = 0; l < b.length; l++) {
				// !ignoreEmptyStrings already handled.
				if (b[l] == null || b[l].length() < 1) continue;
				
				// Set current char
				if (c == '\u0000') {
					nonEmptyId = l;
					c = b[l].charAt(i);
					continue;
				}
				
				if (c != b[l].charAt(i)) {
				  breakIt=true;
				  break;
				}
			}
			if (breakIt) break;
		}
		
		return nonEmptyId < 0 ? "" : b[nonEmptyId].substring(0, i);
	}
	
	/**
	 * Returns the longest common suffix of both strings. This method is
	 * case-sensitive.
	 * 
	 * @param a
	 *        string a
	 * @param b
	 *        string b
	 * @return the longest common suffix
	 */
	public static String getLongestCommonSuffix(String a, String b) {
		int i;
		for (i = 1; i <= Math.min(a.length(), b.length()); i++) {
			if (a.charAt(a.length() - i) != b.charAt(b.length() - i)) break;
		}
		return a.substring(a.length() - i + 1, a.length());
	}
	
	public static String getLongestCommonSuffix(String a, String[] b,
		boolean ignoreEmptyStrings) {
		int i;
		
		// Get minimum String length in b
		int minLength = a.length();
		minLength = Math.min(getMinimumLength(b, ignoreEmptyStrings), minLength);
		
		// Iterate through all positions, until it does not match.
		boolean breakIt = false;
		for (i = 1; i <= minLength; i++) {
			char c = a.charAt(a.length() - i);
			for (int l = 0; l < b.length; l++) {
				// !ignoreEmptyStrings already handled.
				if (b[l] == null || b[l].length() < 1) continue;
				
				if (c != b[l].charAt(b[l].length() - i)) {
					breakIt = true;
					break;
				}
			}
			if (breakIt) break;
		}
		
		return a.substring(a.length() - i + 1, a.length());
	}
	
	public static String getLongestCommonSuffix(String[] b, boolean ignoreEmptyStrings) {
		int i;
		
		// Get minimum String length in b
		int minLength = getMinimumLength(b, ignoreEmptyStrings);
		
		// Iterate through all positions, until it does not match.
		int nonEmptyId = -1;
		boolean breakIt = false;
		for (i = 1; i <= minLength; i++) {
			char c = '\u0000';
			for (int l = 0; l < b.length; l++) {
				// !ignoreEmptyStrings already handled.
				if (b[l] == null || b[l].length() < 1) continue;
				
				// Set current char
				if (c == '\u0000') {
					nonEmptyId = l;
					c = b[l].charAt(b[l].length() - i);
					continue;
				}
				
				if (c != b[l].charAt(b[l].length() - i)) {
					breakIt = true;
					break;
				}
			}
			if (breakIt) break;
		}
		
		return nonEmptyId < 0 ? "" : b[nonEmptyId].substring(b[nonEmptyId].length()
				- i + 1, b[nonEmptyId].length());
	}
	
	/**
	 * 
	 * @param b
	 * @param ignoreEmptyStrings
	 *        - Ignore empty or null elements.
	 * @return Minimum String length in b
	 */
	public static int getMinimumLength(String[] b, boolean ignoreEmptyStrings) {
		int minLength = Integer.MAX_VALUE;
		boolean atLeastOneNonNull = false;
		for (int l = 0; l < b.length; l++) {
			if (b[l] == null || b[l].length() < 1) {
				if (ignoreEmptyStrings)
					continue;
				else return 0;
			} else {
				minLength = Math.min(minLength, b[l].length());
				atLeastOneNonNull = true;
			}
		}
		
		if (b.length == 0 || !atLeastOneNonNull) return 0;
		return minLength;
	}
	
	/**
	 * Returns the most similar String from a list of given Strings
	 * 
	 * @param s
	 * @param synonyms
	 * @return
	 */
	public static String getMostSimilarString(String s, String... synonyms) {
		int dist[] = new int[synonyms.length];
		int min = 0;
		for (int i = 0; i < dist.length; i++) {
			dist[i] = globalAlignment(s.toCharArray(), synonyms[i].toCharArray(), 2,
				1);
			if (dist[i] < dist[min]) {
				min = i;
			}
		}
		return synonyms[min];
	}
	
	/**
	 * Returns the number as a word. Zero is converted to "no". Only positive
	 * numbers from 1 to twelve can be converted. All other numbers are just
	 * converted to a String containing the number.
	 * 
	 * @param number
	 * @return
	 */
	public static String getWordForNumber(int number) {
		if ((number < Integer.MIN_VALUE) || (Integer.MAX_VALUE < number)) {
			return Integer.toString(number);
		}
		// TODO: Localize!
		switch (number) {
			case 0:
				return "no";
			case 1:
				return "one";
			case 2:
				return "two";
			case 3:
				return "three";
			case 4:
				return "four";
			case 5:
				return "five";
			case 6:
				return "six";
			case 7:
				return "seven";
			case 8:
				return "eight";
			case 9:
				return "nine";
			case 10:
				return "ten";
			case 11:
				return "eleven";
			case 12:
				return "twelve";
			default:
				return Integer.toString(number);
		}
	}
	
	/**
	 * 
	 * @param squery
	 * @param ssubject
	 * @param indel
	 * @param gapExt
	 * @return
	 */
	public static int globalAlignment(char squery[], char ssubject[],
			int indel, int gapExt) {
		int insert = indel, delete = indel, match = 0, replace = insert
				+ delete, i, j;
		int costMatrix[][] = new int[squery.length + 1][ssubject.length + 1]; // Matrix
		// CostMatrix

		/*
		 * Variables for the traceback
		 */
		StringBuffer[] align = { new StringBuffer(), new StringBuffer() };
		StringBuffer path = new StringBuffer();

		// construct the matrix:
		costMatrix[0][0] = 0;

		/*
		 * If we want to have affine gap penalties, we have to initialise
		 * additional matrices: If this is not necessary, we won't do that
		 * (because it's expensive).
		 */
		if ((gapExt != delete) || (gapExt != insert)) {

			int[][] E = new int[squery.length + 1][ssubject.length + 1]; // Inserts
			int[][] F = new int[squery.length + 1][ssubject.length + 1]; // Deletes

			E[0][0] = F[0][0] = Integer.MAX_VALUE; // Double.MAX_VALUE;
			for (i = 1; i <= squery.length; i++) {
				// costMatrix[i][0] = costMatrix[i-1][0] + delete;
				E[i][0] = Integer.MAX_VALUE; // Double.POSITIVE_INFINITY;
				costMatrix[i][0] = F[i][0] = delete + i * gapExt;
			}
			for (j = 1; j <= ssubject.length; j++) {
				// costMatrix[0][j] = costMatrix[0][j - 1] + insert;
				F[0][j] = Integer.MAX_VALUE; // Double.POSITIVE_INFINITY;
				costMatrix[0][j] = E[0][j] = insert + j * gapExt;
			}
			for (i = 1; i <= squery.length; i++)
				for (j = 1; j <= ssubject.length; j++) {
					E[i][j] = Math.min(E[i][j - 1], costMatrix[i][j - 1]
							+ insert)
							+ gapExt;
					F[i][j] = Math.min(F[i - 1][j], costMatrix[i - 1][j]
							+ delete)
							+ gapExt;
					costMatrix[i][j] = min(E[i][j], F[i][j],
							costMatrix[i - 1][j - 1]
									- ((squery[i-1] == ssubject[j-1]) ? -match
											: -replace));
				}
			/*
			 * Traceback for affine gap penalties.
			 */
			boolean[] gap_extend = { false, false };
			j = costMatrix[costMatrix.length - 1].length - 1;

			for (i = costMatrix.length - 1; i > 0;) {
				do {
					// only Insert.
					if (i == 0) {
						align[0].insert(0, '~');
						align[1].insert(0, ssubject[j--]);
						path.insert(0, ' ');

						// only Delete.
					} else if (j == 0) {
						align[0].insert(0, squery[--i]);
						align[1].insert(0, '~');
						path.insert(0, ' ');

						// Match/Replace
					} else if ((costMatrix[i][j] == costMatrix[i - 1][j - 1]
							- ((squery[i-1] == ssubject[j-1]) ? -match : -replace))
							&& !(gap_extend[0] || gap_extend[1])) {
						if (squery[i-1] == ssubject[j-1])
							path.insert(0, '|');
						else
							path.insert(0, ' ');
						align[0].insert(0, squery[--i]);
						align[1].insert(0, ssubject[--j]);

						// Insert || finish gap if extended gap is
						// opened
					} else if (costMatrix[i][j] == E[i][j] || gap_extend[0]) {
						// check if gap has been extended or freshly
						// opened
						gap_extend[0] = (E[i][j] != costMatrix[i][j - 1]
								+ insert + gapExt);

						align[0].insert(0, '-');
						align[1].insert(0, ssubject[--j]);
						path.insert(0, ' ');

						// Delete || finish gap if extended gap is
						// opened
					} else {
						// check if gap has been extended or freshly
						// opened
						gap_extend[1] = (F[i][j] != costMatrix[i - 1][j]
								+ delete + gapExt);

						align[0].insert(0, squery[--i]);
						align[1].insert(0, '-');
						path.insert(0, ' ');
					}
				} while (j > 0);
			}
			/*
			 * No affine gap penalties, constant gap penalties, which is much
			 * faster and needs less memory.
			 */
		} else {
			for (i = 1; i <= squery.length; i++) {
				costMatrix[i][0] = costMatrix[i - 1][0] + delete;
			}
			for (j = 1; j <= ssubject.length; j++) {
				costMatrix[0][j] = costMatrix[0][j - 1] + insert;
			}
			for (i = 1; i <= squery.length; i++) {
				for (j = 1; j <= ssubject.length; j++) {
					costMatrix[i][j] = min(costMatrix[i - 1][j]
							+ delete, costMatrix[i][j - 1] + insert,
							costMatrix[i - 1][j - 1]
									- ((squery[i-1] == ssubject[j-1]) ? -match
											: -replace));
				}
			}
			/*
			 * Traceback for constant gap penalties.
			 */
			j = costMatrix[costMatrix.length - 1].length - 1;
			for (i = costMatrix.length - 1; i > 0;) {
				do {
					// only Insert.
					if (i == 0) {
						align[0].insert(0, '~');
						align[1].insert(0, ssubject[j--]);
						path.insert(0, ' ');

						// only Delete.
					} else if (j == 0) {
						align[0].insert(0, squery[i--]);
						align[1].insert(0, '~');
						path.insert(0, ' ');

						// Match/Replace
					} else if (costMatrix[i][j] == costMatrix[i - 1][j - 1]
							- ((squery[i-1] == ssubject[j-1]) ? -match : -replace)) {

						if (squery[i-1] == ssubject[j-1]) {
							path.insert(0, '|');
						} else {
							path.insert(0, ' ');
						}
						align[0].insert(0, squery[i--]);
						align[1].insert(0, ssubject[j--]);

						// Insert
					} else if (costMatrix[i][j] == costMatrix[i][j - 1]
							+ insert) {
						align[0].insert(0, '-');
						align[1].insert(0, ssubject[j--]);
						path.insert(0, ' ');

						// Delete
					} else {
						align[0].insert(0, squery[i--]);
						align[1].insert(0, '-');
						path.insert(0, ' ');
					}
				} while (j > 0);
			}
		}
		return costMatrix[costMatrix.length - 1][costMatrix[costMatrix.length - 1].length - 1];
	}

	/**
	 * <p>
	 * Returns the concatenated strings of the array separated with the given
	 * delimiter. See {@link #implode(String[], String)} for more details.
	 * </p>
	 * 
	 * @param ary
	 *        the list of strings to concatenate
	 * @param delim
	 *        the delimiter string between the single strings (the "glue").
	 * @return the concatenated string separated by the delimiter
	 */
	public static String implode(Collection<?> list, String delim) {
	  // This code is too slow.
		//String[] ary = new String[list.size()];
		//list.toArray(ary);
		//return implode(ary, delim);
    StringBuffer out = new StringBuffer();
    for (Object object : list) {
      if (out.length()>0) out.append(delim);
      out.append(object);
    }
    return out.toString();
	}
  
  /**
	 * <p>
	 * Returns the concatenated strings of the array separated with the given
	 * delimiter. Useful for constructing queries from arrays.
	 * </p>
	 * <p>
	 * Example: The array {@code {"a", "b", "c"}} and delimiter
	 * {@code "--"} will results in {@code "a--b--c"}.
	 * 
	 * @param ary
	 *        the array of strings to concatenate
	 * @param delim
	 *        the delimiter string between the single strings (the "glue").
	 * @return the concatenated string separated by the delimiter
	 */
	public static String implode(Object[] ary, String delim) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < ary.length; i++) {
			if (i > 0) {
			  out.append(delim);
			}
			out.append(ary[i].toString());
		}
		return out.toString();
	}
	
	/**
   * Perform an indexOf with mutliple strings.
   * @param string
   * @param lookFor multiple strings to search for. Returns 
   * @return the first occurance of any of {@code lookFor} strings.
   */
  public static int indexOf(String string, String... lookFor) {
    int firstMatch = -1;
    if (lookFor!=null) {
      for (String s: lookFor) {
        int pos = string.indexOf(s);
        if (pos==0) return pos; // can not get lower
        else if (pos>0 && (pos<firstMatch || firstMatch<0)) {
          firstMatch = pos;
        }
      }
    }
    
    return firstMatch;
  }
	
	/**
   * See same method in {@link String}. Helper method used by other methods.
   * @return
   */
  private static int indexOfIgnoreCase(char[] source, int sourceOffset, int sourceCount,
    char[] target, int targetOffset, int targetCount,
    int fromIndex) {
    if (fromIndex >= sourceCount) {
      return (targetCount == 0 ? sourceCount : -1);
    }
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    if (targetCount == 0) {
      return fromIndex;
    }
    
    char lCaseFirst  = Character.toLowerCase(target[targetOffset]);
    int max = sourceOffset + (sourceCount - targetCount);
    
    for (int i = sourceOffset + fromIndex; i <= max; i++) {
      /* Look for first character. */
      if (Character.toLowerCase(source[i]) != lCaseFirst) {
        while (++i <= max && Character.toLowerCase(source[i]) != lCaseFirst);
      }
      
      /* Found first character, now look at the rest of v2 */
      if (i <= max) {
        int j = i + 1;
        int end = j + targetCount - 1;
        for (int k = targetOffset + 1; j < end && Character.toLowerCase(source[j]) == Character.toLowerCase(target[k]); j++, k++);
        
        if (j == end) {
          /* Found whole string. */
          return i - sourceOffset;
        }
      }
    }
    return -1;
  }
	
  /**
   *
   * @param   source      the string to search in (full length).
   * @param   str   any string.
   * @return  if the string argument occurs as a substring within this
   *          object, then the index of the first character of the first
   *          such substring is returned; if it does not occur as a
   *          substring, {@code -1} is returned.
   */
  public static int indexOfIgnoreCase(String source, String str) {
    return indexOfIgnoreCase(source, str, 0);
  }
	
	/**
   *
   * @param   source      the string to search in (full length).
   * @param   str         the substring for which to search.
   * @param   fromIndex   the index from which to start the search.
   * @return  the index within this string of the first occurrence of the
   *          specified substring, starting at the specified index.
   */
  public static int indexOfIgnoreCase(String source, String str, int fromIndex) {
    return indexOfIgnoreCase(source.toCharArray(), 0, source.length(),
      str.toCharArray(), 0, str.length(), fromIndex);
  }

  /**
	 * @see #insertLineBreaksAndCount(String, int, String, String, boolean)
	 * @param message
	 * @param lineBreak
	 * @param lineBreakSymbol
	 */
	public static String insertLineBreaks(String message, int lineBreak,
		String lineBreakSymbol) {
		return insertLineBreaks(message, lineBreak, lineBreakSymbol, null);
	}

  /**
   * @param message
   * @param lineBreak
   * @param lineBreakSymbol
   * @param padString
   */
  public static String insertLineBreaks(String message, int lineBreak,
    String lineBreakSymbol, String padString) {
    return insertLineBreaksAndCount(message, lineBreak, lineBreakSymbol, null, false).getA();
  }

  /**
   * @see #insertLineBreaksAndCount(String, int, String, String, boolean)
   * @param message
   * @param lineBreak breaks AFTER this number of characters is exceeded.
   * @param lineBreakSymbol
   * @return String with lineBreaks and number of inserted lineBreaks
   */
  public static ValuePair<String, Integer> insertLineBreaksAndCount(
    String message, int lineBreak, String lineBreakSymbol) {
      return insertLineBreaksAndCount(message, lineBreak, lineBreakSymbol, null);
    }

  /**
	 * @see #insertLineBreaksAndCount(String, int, String, String, boolean)
	 * @param message
   * @param lineBreak breaks AFTER this number of characters is exceeded.
   * @param lineBreakSymbol
   * @param padString TODO
	 * @return String with lineBreaks and number of inserted lineBreaks
	 */
  public static ValuePair<String, Integer> insertLineBreaksAndCount(
    String message, int lineBreak, String lineBreakSymbol, String padString) {
    return insertLineBreaksAndCount(message, lineBreak, lineBreakSymbol, padString, false);
  }

  /**
   * @param message
   * @param lineBreak
   * @param lineBreakSymbol
   * @param breakBeforeLineBreak if false, breaks after a line is longer than
   * {@code lineBreak} characters. If true, ensures that no line is longer
   * than {@code lineBreak} characters, i.e., breaks before that number
   * of chars.
   * @return String with lineBreaks and number of inserted lineBreaks
   */
  public static ValuePair<String, Integer> insertLineBreaksAndCount(
            String message, int lineBreak, String lineBreakSymbol, boolean
            breakBeforeLineBreak) {
    return insertLineBreaksAndCount(message, lineBreak, lineBreakSymbol, null, breakBeforeLineBreak);
  }

  /**
   * @param message
   * @param lineBreak
   * @param lineBreakSymbol
   * @param padString TODO
   * @param breakBeforeLineBreak if false, breaks after a line is longer than
   * {@code lineBreak} characters. If true, ensures that no line is longer
   * than {@code lineBreak} characters, i.e., breaks before that number
   * of chars.
   * @return String with lineBreaks and number of inserted lineBreaks
   */
  public static ValuePair<String, Integer> insertLineBreaksAndCount(
            String message, int lineBreak, String lineBreakSymbol,
            String padString, boolean breakBeforeLineBreak) {
    StringBuilder sb = new StringBuilder();
    StringTokenizer st = new StringTokenizer(message != null ? message : ""," ");
    if (st.hasMoreElements()) {
      sb.append(st.nextElement().toString());
    }
    int length = sb.length();
    int pos;
    int count = 0;
    while (st.hasMoreElements()) {
      String tmp = st.nextElement().toString();
      
      if ((lineBreak < Integer.MAX_VALUE) && (
          (length >= lineBreak) || 
          (breakBeforeLineBreak && (length + tmp.length()) >= lineBreak) )) {
        sb.append(lineBreakSymbol);
        if( padString != null ) {
          sb.append(padString);
        }
        count++;
        length = 0;
      }
      else {
        sb.append(' ');
      }

      // Append current element
      sb.append(tmp);

      // Change length
      if ((pos = tmp.indexOf(lineBreakSymbol)) >= 0) {
        length = tmp.length() - pos - lineBreakSymbol.length();
      }
      else {
        length += tmp.length() + 1;
      }
    }
    return new ValuePair<String, Integer>(sb.toString(), Integer.valueOf(count));
  }

  /**
   * For simplicity this method returns false if the char is in any operating system
   * invalid.
   * @param c
   * @return true if and only if this character can be used in a file name.
   */
  private static boolean isValidFileSystemCharacter(char c) {
    /*
     *     if (OS.isWindows()) { invalidChars = "\\/:*?\"<>|";
     *     } else if (OS.isMacOSX()) { invalidChars = "/:";
     *     } else { // assume Unix/Linux 
     *     invalidChars = "/";}
     */
    if (c=='/' || c=='\\' || c=='?' || c=='*' || c==':' || c=='<' || c=='>' || c=='|' || c=='"' || c=='\n' )
      return false;
    
    // Furthermore, control characters are not valid.
    if ((c < '\u0020') || (c > '\u007e' && c < '\u00a0'))
       return false;
    
    return true;
  }

  /**
	 * @param c
	 * @return True if the given character is a vocal and false if it is a
	 *         consonant.
	 */
	public static boolean isVowel(char c) {
		c = Character.toLowerCase(c);
		return (c == 'a') || (c == 'e') || (c == 'i') || (c == 'o') || (c == 'u');
	}

  /**
   * Append or increment a numbered suffix to {@code newString} if
   * it already exists in {@code existingStrings}
   * <p>Example: existingStrings contains "A", "A_1" and "B".
   * newString is "A". Return value will be "A_2".
   * <p> Detects the following suffixes (examples):
   * <ul><li>_1</li><li>[space]1</li><li>1</li>
   * <li>(_1)</li><li>([space]1)</li><li>(1)</li></ul>and automatically
   * keeps this style.
   * @param existingStrings
   * @param newString
   * @return unique string
   */
  public static String makeUnique(Iterable<String> existingStrings, String newString) {
    Pattern numberedSuffix = Pattern.compile("(.+?)(\\s|_)?\\(?(\\d+)\\)?$");
    Iterator<String> it = existingStrings.iterator();
    int maxNumber = Integer.MIN_VALUE;
    boolean found = false;
    String maxNumberMatch = "";
    String useSeparator = " ";
    while (it.hasNext()) {
      String s = it.next();
      if (s.equals(newString)) {
        found = true;
      }
      Matcher m = numberedSuffix.matcher(s);
      while (m.find()) {
        if (m.group(1).equals(newString)) {
          found = true;
          if (m.group(3)!=null) {
            int num = Integer.parseInt(m.group(3));
            if (num>maxNumber) {
              maxNumber = num;
              useSeparator = m.group(2)==null?"":m.group(2);
              maxNumberMatch = s;
            }
          }
        }
      }
    }
    
    if (!found) {
      // This string is not yet contained in existingStrings.
      return newString;
    } else {
      boolean brackets = true; // place number in brackets
      if (maxNumberMatch.length()>0) { // same style as previous
        brackets = maxNumberMatch.endsWith(")");
        maxNumber++;
      } else {
        maxNumber=2; // initial second item
      }
      
      // We found a string already with numbered suffix
      StringBuilder ret = new StringBuilder(newString);
      ret.append(useSeparator);
      if (brackets) ret.append('(');
      ret.append(maxNumber);
      if (brackets) ret.append(')');
      return ret.toString();
    }
  }

  /**
   * Matches a regular expression agains a String array and
   * returns the number of matches.
   * @param regExpression
   * @param content
   * @return number of matches.
   */
  public static int matches(String regExpression, String[] content) {
    Pattern pat = Pattern.compile(regExpression);
    
    int matches=0;
    for (String s: content) {
      if (pat.matcher(s).matches()) {
        matches++;
      }
    }
    
    return matches;
  }
  
  /**
	 * Merges the two given arrays of Strings according to a lexicographic
	 * order.
	 * 
	 * @param n1
	 *            An array of strings
	 * @param n2
	 *            Another array of strings to be merged with the first argument.
	 * @return
	 */
	public static String[] merge(String[] n1, String... n2) {
		Arrays.sort(n1);
		Arrays.sort(n2);
		Vector<String> l = new Vector<String>();
		int i = 0, j = 0, lex;
		while ((i < n1.length) || (j < n2.length)) {
			if ((i < n1.length) && (j < n2.length)) {
				lex = n1[i].compareTo(n2[j]);
				l.add(lex <= 0 ? n1[i++] : n2[j++]);
				if (lex == 0) {
					j++;
				}
			} else {
				l.add(i == n1.length ? n2[j++] : n1[i++]);
			}
		}
		return l.toArray(new String[] {});
	}
  
  /**
	 * This just computes the minimum of three integer values.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return Gives the minimum of three integers
	 */
	private static int min(int x, int y, int z) {
		if ((x < y) && (x < z)) {
			return x;
		}
		if (y < z) {
			return y;
		}
		return z;
	}
  
  /**
	 * 
	 * @return
	 */
	public static String newLine() {
		return newLine;
	}
  
  /**
   * Removes all non-digit chars from input string.
   * @param input - input string
   * @return  input string with only digits.
   */
  public static String removeAllNonDigits(String input) {
    StringBuffer ret = new StringBuffer();
    for (char c:  input.toCharArray()) {
      if (Character.isDigit(c)) ret.append(c);
    }
    return ret.toString();
  }

  /**
   * Removes all character from the string that are not valid in file names.
   * @param outFile
   * @return
   */
  public static String removeAllNonFileSystemCharacters(String outFile) {
    StringBuffer ret = new StringBuffer();
    for (char c: outFile.toCharArray()) {
      if (isValidFileSystemCharacter(c)) ret.append(c);
    }
    return ret.toString();
  }
  
  /**
   * Removes XML annotations such as &lt;HTML&gt; from any string.
   * @param string
   * @return string without any xml annotations.
   */
  public static String removeXML(String string) {
    return string.replaceAll("\\<.*?\\>", "");
  }
	
  /**
	 * Returns a HTML formated String without inserting linebreaks.
	 * 
	 * @param string
	 * @return
	 */
	public static String toHTML(String string) {
		return toHTML(string, Integer.MAX_VALUE);
	}
	
	/**
   * Returns a HTML formated String, in which each line is at most lineBreak
   * symbols long.
   * 
   * @param string
   * @param lineBreak
   * @return
   */
	public static String toHTML(String string, int lineBreak) {
	  return toHTML(string, lineBreak, true);
	}

  /**
   * Returns a HTML formated String, in which each line is at most lineBreak
   * symbols long.
   * 
   * @param string
   * @param lineBreak
	 * @param preserveExistingLinebreaks
	 * @return
	 */
	public static String toHTML(String string, int lineBreak, boolean preserveExistingLinebreaks) {
	  String lineBreakSymbol = "<br/>";
		if (string == null) {
			return "<html><body>null</body></html>";
		}
		
		// Preserve existing linebreaks.
		if (preserveExistingLinebreaks) {
		  string = string.replace("\r", "").replace("\n", lineBreakSymbol);
		}
		
		StringBuilder sb = new StringBuilder();
			if (!string.startsWith("<html><body>")) {
			sb.insert(0, "<html><body>");
		}
		sb.append(insertLineBreaks(string, lineBreak, lineBreakSymbol));
		if (!string.endsWith("</body></html>")) {
			sb.append("</body></html>");
		}
		
		return sb.toString();
	}

  /**
   * Creates an appropriate tool tip text using simple {@link String#format(String, Object...)}.
   * 
   * @param format A {@link String} that might contain format commands
   * @param args optional replacements according to the format {@link String}
   * @return An HTML string with line breaks after {@link #TOOLTIP_LINE_LENGTH}
   */
	public static String toHTMLToolTip(String format, Object... args) {
		return toHTML(String.format(format, args), TOOLTIP_LINE_LENGTH);
	}
	
	/**
   * Creates an appropriate tool tip text using {@link MessageFormat}.
   * 
   * @param format A {@link String} that might contain format commands
   * @param args optional replacements according to the format {@link String}
   * @return An HTML string with line breaks after {@link #TOOLTIP_LINE_LENGTH}
   */
	public static String toHTMLMessageToolTip(String format, Object... args) {
		return toHTML(MessageFormat.format(format, args), TOOLTIP_LINE_LENGTH);
	}

	/**
	 * Formats a number for display purposes.
	 * @param v
	 * @param maxIntDigits
	 * @param minFracDigits
	 * @param maxFracDigits
	 * @return
	 */
	public static String toString(double v) {
		if (Double.isNaN(v)) {
			return "NaN";
		} else if (Double.isInfinite(v)) {
			return (v < 0) ? "-\u221E" : "\u221E";
		} else if (((int) v) - v == 0) { 
			return String.format("%d", Integer.valueOf((int) v)); 
		}
		Locale locale = Locale.getDefault();
		DecimalFormat df;
		if ((Math.abs(v) < 1E-5f) || (1E5f < Math.abs(v))) {
			df = new DecimalFormat(StringUtil.SCIENTIFIC_FORMAT,
				new DecimalFormatSymbols(locale));
		} else {
			df = new DecimalFormat(StringUtil.DECIMAL_FORMAT,
				new DecimalFormatSymbols(locale));
		}
		return df.format(v);
	}

  /**
   * Same as {@link isWord} !!!
   */
  public static boolean containsWord(String containingLine, String containedString) {
    return isWord(containingLine, containedString);
  }

  /**
   * Returns, wether the containedString does occur somewhere in containingLine as a word.
   * E.g. containingLine = "12.ENOA_MOUSE ABC". "NOA_MOUSE" is no word, but "ENOA_MOUSE"
   * is a word.
   * The function could also be called "containsWord".
   * @param containingLine
   * @param containedString
   * @return true if and only if containedString is contained in containingLine and is not
   * sourrounded by a digit or letter.
   */
  public static boolean isWord(String containingLine, String containedString) {
    return isWord(containingLine, containedString, false);
  }

  /**
   * Returns, wether the containedString does occur somewhere in containingLine as a word.
   * E.g. containingLine = "12.ENOA_MOUSE ABC". "NOA_MOUSE" is no word, but "ENOA_MOUSE"
   * is a word.
   * @param containingLine
   * @param containedString
   * @param ignoreDigits - if false, digits will be treated as part of a word (default case).
   * If true, digits will be treated as NOT being part of a word (a word splitter, like a space).
   * @return true if and only if containedString is contained in containingLine as word.
   */
  public static boolean isWord(String containingLine, String containedString, boolean ignoreDigits) {
    // Check if it's a word
    int pos = -1;
    while (true) {
      if (pos+1>=containingLine.length()) break;
      pos = containingLine.indexOf(containedString, pos+1);
      if (pos<0) break;
      
      boolean leftOK = true;
      if (pos>0) {
        char l = containingLine.charAt(pos-1);
        if ((Character.isDigit(l) && !ignoreDigits) || Character.isLetter(l)) leftOK = false;
      }
      boolean rechtsOK = true;
      if (pos+containedString.length()<containingLine.length()) {
        char l = containingLine.charAt(pos+containedString.length());
        if ((Character.isDigit(l) &&!ignoreDigits) || Character.isLetter(l)) rechtsOK = false;
      }
      
      if (rechtsOK && leftOK) return true;
    }
    return false;
  }

  /**
   * @param containingLine
   * @param startPosition
   * @param ignoreDigits
   * @return the next word in {@code containingLine}, starting from {@code startPosition}
   * only including letters and if {@code ignoreDigits} is false, also digits.
   */
  public static String getWord(String containingLine, int startPosition, boolean ignoreDigits) {
    // get next word
    int pos = startPosition;
    if (pos<0) return null;
    
    StringBuffer ret = new StringBuffer();
    while (pos<=containingLine.length()) {
      char c = containingLine.charAt(pos);
      if (Character.isLetter(c)) ret.append(c);
      else if (!ignoreDigits && Character.isDigit(c)) ret.append(c);
      else {
        // End of word
        break;
      }
      pos++;
    }
    return ret.toString();
  }

  /**
   * 
   * @param c
   * @param times
   * @return
   */
  public static StringBuffer replicateCharacter(char c, int times) {
    StringBuffer s = new StringBuffer();
    for (int i=0; i<times; i++)
      s.append(c);
    return s;
  }

  /**
   * 
   * @param ch
   * @param times
   * @return
   */
  public static String replicateCharacter(String ch, int times) {
    StringBuilder retval = new StringBuilder();
    for (int i=0; i<times;i++) {
      retval.append(ch);
    }
    return retval.toString();
  }

  /**
   * Returns the reverse of a string.
   * @param s
   * @return
   */
  public static String reverse(String s) {
    StringBuffer a = new StringBuffer(s);
    return a.reverse().toString();
  }

  /**
   * Replaces all occurences of {@code toReplace} in 
   * {@code containingString} with {@code replaceWith}.
   * <p>Replace is case-insensitive.
   * @param containingString
   * @param toReplace
   * @param replaceWith
   * @return
   */
  public static String replaceIgnoreCase(String containingString, String toReplace, String replaceWith) {
    StringBuilder sb = new StringBuilder(containingString.length());
    
    int i=0, lastI=0;
    while ( (i = indexOfIgnoreCase(containingString, toReplace, i)) >=0) {
      sb.append(containingString.substring(lastI, i));
      sb.append(replaceWith);
      i+=toReplace.length();
      lastI=i;
    }
    
    // Append suffix
    sb.append(containingString.substring(lastI));
    
    return sb.toString();
  }

  /**
   * Allows only digits, letters and underscores. Removes all other
   * characters (thereby replacing spaces with underscores)
   * from the given string and returns it.
   * <p>Example: "5hallo Fak!e " would be returned as "5hallo_Fake".
   * @param string
   * @return
   */
  public static String toWord(String string) {
    if (string==null) {
      return "";
    }
    string = string.trim();
    StringBuilder ret2 = new StringBuilder(string.length());
    char c;
    
    // May contain letters, digits or '_'
    for (int i = 0; i < string.length(); i++) {
      c = string.charAt(i);
      if (c==' ') {
        c='_'; // Replace spaces with "_"
      }
      
      // Character.isLetter(c) also accepts ß and other unusual chars...
      if ((c>=97 && c<=122) || (c>=65 && c<=90)) {
        ret2.append(c);
      } else if (Character.isDigit(c) || c == '_') {
        ret2.append(c);
      } // else: skip invalid characters
    }
    
    return ret2.toString();
  }
  
}