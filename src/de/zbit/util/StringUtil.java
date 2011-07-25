/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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
  private static final String newLine = System.getProperty("line.separator");
	
	/**
	 * 
	 */
	private static final char fileSeparator = System.getProperty("file.separator").charAt(0);
	
	/**
	 * 
	 * @return
	 */
	public static String newLine() {
		return newLine;
	}
	
	/**
	 * 
	 * @return
	 */
	public static char fileSeparator() {
		return fileSeparator;
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
	 * Return the given string filled up to the given length with the given
	 * character. If the length of the input string is equal or greater than the
	 * target length, it will be return unchanged, i.e., it will NOT be shortened.
	 * The <code>prepend</code> parameter defines whether the fill character
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
		 * runtime and o(n) memory usage. This is the later implementation.
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
	 * Returns the number as a word. Zero is converted to "no". Only positive
	 * numbers from 1 to twelve can be converted. All other numbers are just
	 * converted to a String containing the number.
	 * 
	 * @param number
	 * @return
	 */
	public static String getWordForNumber(int number) {
		if ((number < Integer.MIN_VALUE) || (Integer.MAX_VALUE < number)) { return Integer
				.toString(number); }
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
	 * Adds a prefix or/and suffix to each element.
	 * @param list
	 * @param prefix - may be null.
	 * @param suffix - may be null.
	 * @return List<String> - the list.
	 */
	public static List<String> addPrefixAndSuffix(Collection<?> list, String prefix, String suffix) {
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
	 * Example: The array <code>{"a", "b", "c"}</code> and delimiter
	 * <code>"--"</code> will results in <code>"a--b--c"</code>.
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
	 * @see #insertLineBreaksAndCount(String, int, String, boolean)
	 * @param message
	 * @param lineBreak
	 * @param lineBreakSymbol
	 * @param sb
	 */
	public static String insertLineBreaks(String message, int lineBreak,
		String lineBreakSymbol) {
		return insertLineBreaksAndCount(message, lineBreak, lineBreakSymbol, false).getA();
	}

	/**
	 * @see #insertLineBreaksAndCount(String, int, String, boolean)
	 * @param message
	 * @param lineBreak breaks AFTER this number of characters is exceeded.
	 * @param lineBreakSymbol
	 * @return String with lineBreaks and number of inserted lineBreaks
	 */
  public static ValuePair<String, Integer> insertLineBreaksAndCount(
    String message, int lineBreak, String lineBreakSymbol) {
    return insertLineBreaksAndCount(message, lineBreak, lineBreakSymbol, false);
  }
  
  /**
   * @param message
   * @param lineBreak
   * @param lineBreakSymbol
   * @param breakBeforeLineBreak if false, breaks after a line is longer than
   * <code>lineBreak</code> characters. If true, ensures that no line is longer
   * than <code>lineBreak</code> characters, i.e., breaks before that number
   * of chars.
   * @return String with lineBreaks and number of inserted lineBreaks
   */
  public static ValuePair<String, Integer> insertLineBreaksAndCount(
            String message, int lineBreak, String lineBreakSymbol, boolean
            breakBeforeLineBreak) {
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
	 * @param c
	 * @return True if the given character is a vocal and false if it is a
	 *         consonant.
	 */
	public static boolean isVocal(char c) {
		c = Character.toLowerCase(c);
		return (c == 'a') || (c == 'e') || (c == 'i') || (c == 'o') || (c == 'u');
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
	  String lineBreakSymbol = "<br>";
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
   * Removes XML annotations such as &lt;HTML&gt; from any string.
   * @param string
   * @return string without any xml annotations.
   */
  public static String removeXML(String string) {
    return string.replaceAll("\\<.*?\\>", "");
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
   * @param string
   * @param toCount
   * @return number of occurences of <code>toCount</code> in <code>string</code>.
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
  
}