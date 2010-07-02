package de.zbit.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Reads a CSV (e.g. tab delimited) file.
 * @author wrzodek
 */
public class ReadCSVData {
  private ArrayList<Character> CommentIndicators = new ArrayList<Character>();
  private boolean containsHeaders = true;
  private boolean removeStringIndiciatorsAtCellStartEnd = true;
  private char separatorChar='\u0000'; // Default Value. Equals: char separatorChar;
  
  private String[] headers;
  private String[][] data;

  public ReadCSVData(String filename, boolean containsHeaders) {
    this();
    this.containsHeaders = containsHeaders;
    this.read(filename);
  }
  public ReadCSVData(String filename) {
    this();
    this.read(filename);
  }
  public ReadCSVData() {
    init();
  }
  /**
   * Adds a new Symbol, which indicates that this line should be treated as a comment (=>ignored)
   */
  public void addCommentInidicator (char c) {
    if (!(CommentIndicators.contains((c))))
      CommentIndicators.add((c));
  }
  public void removeCommentIndicator (char c) {
    while (CommentIndicators.contains((c)))
      CommentIndicators.remove((c));
  }
  
  /**
   * Does your CSV file contains a header line?
   * @param b yes (true) or false (no). Default: yes
   */
  public void setContainsHeaders(boolean b) {
    containsHeaders = b;
  }
  /**
   * Set wether you want to remove the char " or ' when it occurs at the start and end of a cell.
   * @param b
   */
  public void setRemoveStringIndiciatorsAtCellStartEnd(boolean b) {
    removeStringIndiciatorsAtCellStartEnd = b;
  }
  /**
   * Set the separator char, by which columns are separated. If not set, the algorithm will try to infer this automatically.
   * @param c = column separator char.
   */
  public void setSeparatorChar(char c) {
    separatorChar = c;
  }
  
  /**
   * Remove columns. This will free memory by removing unused data.
   * @param column numbers.
   */
  public void setNull(int[] columns) {
    for (int i=0; i<data.length; i++)
      for (int j=0; j<columns.length; j++)
        data[i][columns[j]] = null;
  }
  
  /**
   * Initialize and set default values, which were not set before.
   */
  private void init() {
    CommentIndicators.add(';');
    CommentIndicators.add('#');
  }
  
  public String[][] getData(){
    return this.data;
  }
  public String[] getHeader(){
    return this.headers;
  }
  
  public void read(String filename) {
    char separatorChar = this.separatorChar; // Permits using this function multiple times
    try {
      BufferedReader in = new BufferedReader(new FileReader(filename));
      
      int threshold = 25; // So oft m�ssen Zeilen mit einem trennzeichen getrennt werden, dass es als offizielles trennzeichen gilt.
      
      
      // infere separator char
      char[] trennzeichen = new char[]{'\t', ',', ';', '|', '/', ' '};
      int consistentCounts[] = new int[trennzeichen.length]; // Seit sovielen zeilen schon genau "count"
      int firstConsistentLines[] = new int[trennzeichen.length]; // Erste
      int counts[] = new int[trennzeichen.length]; // Anzahl eines Trennzeichens (= #Spalten)
      int firstConsistentLine = 0, numCols=0;
      for (int i=0; i<trennzeichen.length; i++)
      {consistentCounts[i] = 0; counts[i] = 0;}
      
      int j = 0;
      while (in.ready() && separatorChar=='\u0000') {
        j++;
        String line = in.readLine().trim();
        char firstChar = line.charAt(0);
        //if (CommentIndicators.contains(firstChar)) continue; // <=breakes header detection
        
        // Infere separator char
        if ((separatorChar=='\u0000')) {
          // Sucht 25x das selbe Zeichen genau gleich oft. Wenn dem so ist, ist dies unser separatorChar.
          for (int i=0; i<trennzeichen.length; i++) {
            int aktCounts = countCharAndIgnoreStrings(line, trennzeichen[i]);
            if (counts[i]==aktCounts && aktCounts>0 && !CommentIndicators.contains(firstChar)) consistentCounts[i]++;
            else {consistentCounts[i]=0; counts[i]=aktCounts; firstConsistentLines[i]=j;}
            if (consistentCounts[i]>threshold) {
              separatorChar = trennzeichen[i];
              numCols = counts[i];
              firstConsistentLine = firstConsistentLines[i];
              break;
            }
          }
        }
      }
      
      
      // Get Heading, Count lines
      in = new BufferedReader(new FileReader(filename));
      j=0;
      int numDataLines = 0;
      while (in.ready()) {
        j++;
        String line = in.readLine().trim();
        char firstChar = line.charAt(0);
        if (this.containsHeaders && j==firstConsistentLine) {
          headers = line.split(Pattern.quote(Character.toString(separatorChar)));
          if (CommentIndicators.contains(headers[0].charAt(0))) headers[0] = headers[0].substring(1); // Headers do often start with a comment symbol. 
          continue;
        }
        if (CommentIndicators.contains(firstChar)) continue;
        numDataLines++;
      }
      
      // Finally... get the data
      data = new String[numDataLines][numCols];
      in = new BufferedReader(new FileReader(filename));
      j=0; int nline=0;
      while (in.ready()) {
        j++;
        String line = in.readLine().trim();
        char firstChar = line.charAt(0);
        if (this.containsHeaders && j==firstConsistentLine) continue;
        if (CommentIndicators.contains(firstChar)) continue;
        
        data[nline] = line.split(Pattern.quote(Character.toString(separatorChar)));
        
        // Post Process (trim and remove string indicators).
        for (int i=0; i<data[nline].length; i++) {
          data[nline][i] = data[nline][i].trim();
          if (removeStringIndiciatorsAtCellStartEnd && ((data[nline][i].startsWith("\"") && data[nline][i].endsWith("\"")) ||
              (data[nline][i].startsWith("'") && data[nline][i].endsWith("'")))) {
            if (data[nline][i].length()>2)
              data[nline][i] = data[nline][i].substring(1, data[nline][i].length()-1);
            else data[nline][i] = "";
          }
        }
        nline++;
      }
      
      in.close();
    }catch (Exception ex) {ex.printStackTrace();}
  }
  
  
  @SuppressWarnings("unused")
  private static int countChar(String input, char toCount){
    int counter = 0;
    for(char c: input.toCharArray()){
      if(c==toCount) counter++;
    }
    return counter;
  }
  
  /**
   * Counts the occurences of a specific char in a string. Skips all chars which occur in a string (between " and " or ' and ').
   * @param input    StringSequence
   * @param toCount  Character to count
   * @return Occurences of the character in the string text, skipping all occurences in strings.
   */
  private static int countCharAndIgnoreStrings(String input, char toCount){
    int counter = 0;
    boolean skip1 = false, skip2 = false;
    for(char c: input.toCharArray()){
      if (c=='"') skip1 = !skip1;
      if (c=='\'') skip2 = !skip2;
      if(c==toCount && !skip1 && !skip2) counter++;
    }
    return counter;
  }
  
  // Nicht ganz korrekt da auch 4.345,2.1 als nummer erkannt wird, aber das reicht mir so.
  // macht MIT ABSICHT kein trim!
  public static boolean isNumber(String s, boolean onlyDigits) {
    if (s==null || s.isEmpty()) return false;
    char[] a = s.toCharArray();
    for (int i=0; i< a.length; i++) {
      if (onlyDigits){
        if (Character.isDigit(a[i])) continue; else return false;
      } else {
        if (Character.isDigit(a[i])) continue;
        if (a[i]=='-' || a[i]=='.' || a[i]==',' || a[i]=='E' || a[i]=='e') continue;
        return false;
      }
    }
    return true;
  }
  
}
