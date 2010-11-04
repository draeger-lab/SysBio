package de.zbit.util;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class containing frequently used utility functions for String processing.
 * 
 * @author Florian Mittag
 * @author wrzodek
 * @author draeger
 */
public class StringUtil {
	
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
	 * Returns a lower-case {@link String} who's first letter is now in upper
	 * case.
	 * 
	 * @param name
	 * @return
	 */
	public static String firstLetterUpperCase(String name) {
		char c = name.charAt(0);
		if (Character.isLetter(c)) {
			c = Character.toUpperCase(c);
		}
		if (name.length() > 1) {
			name = Character.toString(c) + name.substring(1).toLowerCase();
		} else {
			return Character.toString(c);
		}
		return name;
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
	public static int[] getLongestCommonLength(String[] b) {
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
	
	public static String getLongestCommonPrefix(String[] b,
		boolean ignoreEmptyStrings) {
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
				
				if (c != b[l].charAt(i)) break;
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
	
	public static String getLongestCommonSuffix(String[] b,
		boolean ignoreEmptyStrings) {
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
	 * <p>
	 * Returns the concatenated strings of the array separated with the given
	 * delimiter. See {@link #implode(String, String)} for details.
	 * </p>
	 * 
	 * @param ary
	 *        the list of strings to concatenate
	 * @param delim
	 *        the delimiter string between the single strings (the "glue").
	 * @return the concatenated string separated by the delimiter
	 */
	public static String implode(List<String> list, String delim) {
		String[] ary = new String[list.size()];
		list.toArray(ary);
		return implode(ary, delim);
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
	public static String implode(String[] ary, String delim) {
		String out = "";
		for (int i = 0; i < ary.length; i++) {
			if (i != 0) {
				out += delim;
			}
			out += ary[i];
		}
		return out;
	}
	
	/**
	 * @param string
	 * @param lineBreak
	 * @param string2
	 * @param sb
	 */
	public static StringBuilder insertLineBreaks(String string, int lineBreak,
		String lineBreakSymbol) {
		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(string != null ? string : "", " ");
		if (st.hasMoreElements()) {
			sb.append(st.nextElement().toString());
		}
		int length = sb.length();
		while (st.hasMoreElements()) {
			if ((length >= lineBreak) && (lineBreak < Integer.MAX_VALUE)) {
				sb.append(lineBreakSymbol);
				length = 0;
			} else {
				sb.append(' ');
			}
			String tmp = st.nextElement().toString();
			length += tmp.length() + 1;
			sb.append(tmp);
		}
		return sb;
	}
	
	/**
	 * Returns a HTML formated String, in which each line is at most lineBreak
	 * symbols long.
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
		StringBuilder sb = new StringBuilder();
		if (!string.startsWith("<html><body>")) {
			sb.insert(0, "<html><body>");
		}
		sb.append(insertLineBreaks(string, lineBreak, "<br>"));
		if (!string.endsWith("</body></html>")) {
			sb.append("</body></html>");
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param objects
	 * @return
	 */
	public static StringBuilder concat(Object... objects) {
		return append(new StringBuilder(), objects);
	}
	
}
