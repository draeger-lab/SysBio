package de.zbit.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import de.zbit.util.AbstractProgressBar;
import de.zbit.util.FileReadProgress;

/**
 * Reads a CSV (e.g. tab delimited) file.
 * 
 * There are two use cases:
 * Read the file at once, or line by line.
 * 
 * Examples:
 * 1:  Read the whole file at once:
 *   CSVReader a = new CSVReader("test.txt");
 *   a.setContainsHeaders(false); // Important!
 *   a.setDisplayProgress(false); // Optional, false by default
 *   
 *   a.read();
 *   
 *   a.getData(); // The whole table, except headers
 *   a.getHeader(); // The header (if available)
 *   a.getPreamble(); // Everything, before actual table start
 *   
 * 2: Read the file, line by line (more memory save)
 *   a.open();
 *   String[] line;
 *   try {
 *     while ((line = a.getNextLine())!=null) {
 *       // Do something with the line
 *     }
 *   } catch (Exception e) {e.printStackTrace();}
 *   
 *   a.getData(); // Returns null
 *   // All other getters are available as in case 1.
 * 
 * @author wrzodek
 */
public class CSVReader implements Serializable {
  private static final long serialVersionUID = 7784651184371604357L;

  /**
   * All lines, starting with one of these characters will be skipped.
   * By default, "#" and ";" will be added (you may remove them manually).
   */
  private ArrayList<Character> CommentIndicators = new ArrayList<Character>();
  
  /**
   * Indicates, that this file contains headers
   */
  private boolean containsHeaders = true;
  
  /**
   * The OpenFile Method has many advantages. It automatically downloads http urls,
   * it can extract files from zips and the url might lay within the same jar or is
   * an external file. Everything is handled automatically.
   */
  private boolean useOpenFileMethod=true;
  
  /**
   * Removes " or ' symbols at cell start and end.
   */
  private boolean removeStringIndiciatorsAtCellStartEnd = true;
  
  /**
   * The column separator char. If not set ('\u0000'), it will be infered automatically.
   */
  private char separatorChar='\u0000'; // Default Value. Equals: char separatorChar;
  
  /**
   * ... if separatorChar is infered automatically, one of these chars in the given order
   * is chosen, based on a stable count of the these chars across multiple lines.
   * 
   * Remark: The order of these chars (or the chars itself) should NOT be modified
   * without notice to the author of this class.
   */
  private char[] trennzeichen = new char[]{'\t', ',', ';', '|', '/', ' '};
  
  /**
   * First line with "consistent number of separator chars". This is the line, where
   * comments end and headers start (if headers are in the file).
   * Starting from zero!
   */
  private int firstConsistentLine = -1;
  
  /**
   * Number of columns.
   */
  private int numCols=-1;
  
  /**
   * The number of data lines (all lines in the file, that do not belong to the preamble,
   * that are no comment lines and no header line).
   */
  private int numDataLines=-1;
  
  /**
   * If a file is currently open, this enables to read it line by line and not the whole file.
   */
  private BufferedReader currentOpenFile=null;
  
  /**
   * Display the progress, while reading the file.
   */
  private boolean displayProgress=false;
  private FileReadProgress progress=null;
  private AbstractProgressBar progressBar=null;
  
  /**
   * The file to read.
   */
  private String filename;
  
  private String[] headers;
  private String[][] data;
  
  /**
   * Used to store the preamble (everything before table start).
   */
  private StringBuffer preamble = new StringBuffer();
  

  public CSVReader(String filename, boolean containsHeaders) {
    this(filename);
    this.containsHeaders = containsHeaders;
  }
  public CSVReader(String filename) {
    init();
    this.filename = filename;
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
   * Set it to '\u0000' to infere it automatically.
   * @param c = column separator char.
   */
  public void setSeparatorChar(char c) {
    separatorChar = c;
  }
  
  /**
   * Returns the separator char, that separates the columns.
   * The char equals '\u0000' if it still needs to be infered automatically.
   * @return
   */
  public char getSeparatorChar() {
    return separatorChar;
  }
  
  /**
   * Set if you want to use the openFile Method. True by default.
   * The OpenFile Method has many advantages. It automatically downloads http urls,
   * it can extract files from zips and the url might lay within the same jar or is
   * an external file. Everything is handled automatically.
   * @param b
   * @return 
   */
  public void setUseOpenFileMethod(boolean b) {
    this.useOpenFileMethod=b;
  }
  
  /**
   * Remove columns (more specific: set them to null. This does not shift indices).
   * This will free memory by removing unused data.
   * @param column numbers.
   */
  public void setNull(int[] columns) {
    if (data==null) return;
    for (int i=0; i<data.length; i++)
      if (data[i]!=null)
        for (int j=0; j<columns.length; j++)
          data[i][columns[j]] = null;
  }
  
  /**
   * Remove the given column (more specific: set it to null. This does not shift indices).
   * This will free memory by removing unused data.
   * @param column numbers.
   */
  public void setNull(int column) {
    if (data==null) return;
    for (int i=0; i<data.length; i++)
      if (data[i]!=null)
        data[i][column] = null;
  }
  
  /**
   * Initialize and set default values, which were not set before.
   */
  private void init() {
    CommentIndicators.add(';');
    CommentIndicators.add('#');
  }
  
  /**
   * Allows to override the progressBar.
   * Warning: A progressBar is only initialized if
   * 1 displayProgress is enabled (by setDisplayProgress(boolean b) )
   * 2 the "open" method
   */
  public void setProgressBar(AbstractProgressBar progress) {
    progressBar = progress;
    if (this.progress!=null && progressBar!=null) {
      this.progress.setProgressBar(progressBar);
    }
  }
  
  /**
   * Display a progress, while reading the file.
   * @param b
   */
  public void setDisplayProgress(boolean b) {
    displayProgress = b;
  }
  
  /**
   * Get the whole file content (except headers and preamble/comments).
   * Will read the file, if not already done.
   * @return
   */
  public String[][] getData(){
    if (data==null) read();
    return this.data;
  }
  
  /**
   * Get the file headers, if file contains headers (to be set manually).
   * Null else.
   * @return
   */
  public String[] getHeader(){
    if (containsHeaders && headers==null) {
      try {
        initialize();
      } catch (Exception e) {e.printStackTrace();}
    }
    return this.headers;
  }
  
  /**
   * Returns the preamble of the file.
   * the preable is everyhing before the data or the header starts.
   * REQUIRES read or open called.
   * @return
   */
  public String getPreamble() {
    return this.preamble.toString();
  }
  
  /**
   * Returns the number of columns.
   */
  public int getNumberOfColumns() {
    if (this.numCols<0)  {
      try {
      initialize();
      } catch (Exception e) {e.printStackTrace();}
    }
    return this.numCols;
  }
  
  /**
   * If column headers have been read, this function will return the column
   * number, containing string @param s case INsensitive.
   * 
   * If no headers have been read or if the string can not be found in any
   * column, returns -1
   *  
   * @return integer, column number
   */
  public int getColumnContaining(String s) {
    if (headers==null) return -1;
    for (int i=0; i<headers.length;i++) {
      String c = new String(headers[i]).toLowerCase();
      if (c.toLowerCase().contains(s.toLowerCase())) return i;
    }
    return -1;
  }
  
  /**
   * If column headers have been read, this function will return the column
   * number, that equals string @param s case INsensitive.
   * 
   * If no headers have been read or if the string does not equal any
   * column, returns -1
   *  
   * @return integer, column number
   */
  public int getColumn(String s) {
    if (headers==null) return -1;
    for (int i=0; i<headers.length;i++) {
      if (headers[i].equalsIgnoreCase(s)) return i;
    }
    return -1;
  }
  
  private BufferedReader getAndResetInputReader(String filename) throws FileNotFoundException {
    if (useOpenFileMethod) {
      // Use the OpenFile Method to automatically extract ZIP archives and such.
      return OpenFile.openFile(filename);
    } else {
      return new BufferedReader(new FileReader(filename));
    }
  }
  
  /**
   * Initializes the class, based on the given file. More specific:
   * - Inferes the separator char (if not set)
   * - Sets the line number, where the data starts (firstConsistentLine)
   * - Sets the number of columns (numCols)
   * - Reads the headers
   * @param filename
   * @throws Exception if file appears not to be a valid CSV file.
   */
  private void initialize() throws Exception {
    BufferedReader in = getAndResetInputReader(filename);
    
    // Number of lines, a separator char needs to have static occurences in-a-row.
    int threshold = 25; // So oft müssen Zeilen mit einem trennzeichen getrennt werden, dass es als offizielles trennzeichen gilt.
    
    // Make a list of separator chars to analyze
    char[] separatorChars;
    if (separatorChar=='\u0000') {// Need to be infered
      separatorChars = this.trennzeichen.clone();
    } else {
      separatorChars = new char[]{separatorChar};
    }
    
    // initialize variables
    int consistentCounts[] = new int[separatorChars.length]; // Seit sovielen zeilen schon genau "count"
    int firstConsistentLines[] = new int[separatorChars.length]; // Erste
    String[] firstConsistentLineString = new String[separatorChars.length]; // For reading headers
    int counts[] = new int[separatorChars.length]; // Anzahl eines Trennzeichens (= #Spalten)
    for (int i=0; i<separatorChars.length; i++)
    {consistentCounts[i] = 0; counts[i] = 0;}
    firstConsistentLine = -1;
    numCols=-1;
    
    // Read a part of the file and fill variables.
    int j = -1;
    while (in.ready() && firstConsistentLine<0) { //  && separatorChar=='\u0000'
      j++;
      String line = in.readLine().trim();
      char firstChar = line.charAt(0);
      //if (CommentIndicators.contains(firstChar)) continue; // <=breakes header detection
      
      // Infere separator char
      // Sucht 25x das selbe Zeichen genau gleich oft. Wenn dem so ist, ist dies unser separatorChar.
      for (int i=0; i<separatorChars.length; i++) {
        // Count chars for the current separator chars, ignoring occurences in strings.
        int aktCounts = countCharAndIgnoreStrings(line, separatorChars[i]);
        if (counts[i]==aktCounts && aktCounts>0 && !CommentIndicators.contains(firstChar)) {
          consistentCounts[i]++;
        } else {
          consistentCounts[i]=0;
          counts[i]=aktCounts;
          firstConsistentLines[i]=j;
          firstConsistentLineString[i] = line;
        }
        if (consistentCounts[i]>threshold) {
          if (separatorChar=='\u0000') separatorChar = separatorChars[i];
          numCols = counts[i]+1; // +1 because number of columns is number of separator chars in line+1
          firstConsistentLine = firstConsistentLines[i];
          
          // Fill the headers eventually.
          if (this.containsHeaders) {
            headers = firstConsistentLineString[i].split(Pattern.quote(Character.toString(separatorChar)));
            // Headers do often start with a comment symbol.
            if (CommentIndicators.contains(headers[0].charAt(0))) headers[0] = headers[0].substring(1); 
          }
          
          break;
        }
      }
    }
    in.close();
    
    // File had less then "threshold" consistent lines in total; Take the max.
    int max=0;
    if (firstConsistentLine<0) {
      for (int i=0; i<separatorChars.length; i++) {
        if (consistentCounts[i]>max) {
          if (separatorChar=='\u0000') separatorChar = separatorChars[i];
          numCols = counts[i]+1; // +1 because number of columns is number of separator chars in line+1
          firstConsistentLine = firstConsistentLines[i];
          
          // Fill the headers eventually.
          if (this.containsHeaders) {
            headers = firstConsistentLineString[i].split(Pattern.quote(Character.toString(separatorChar)));
            // Headers do often start with a comment symbol.
            if (CommentIndicators.contains(headers[0].charAt(0))) headers[0] = headers[0].substring(1); 
          }
        }
      }
    }
    
    if (firstConsistentLine<0) throw new Exception ("Could not analyze input file. Probably not a CSV file.");
  }
  
  /**
   * This incredible cool function can be used to guess columns by its
   * content. E.g. you can use it to find a column where all strings end
   * with "_at" (which will be a column containing affy ids). Or a column
   * starting with "ens" (ensembl ids), etc.
   * In other words, get column ids by content, instead of headers or
   * static variables. 
   * 
   * WARNING: Does RESET your file position.
   * If you planning not to read the whole file into memory,
   * use this method before reading data from the file at all!
   * 
   * If you read the whole file anyway, this function does not affect anything.
   * @param regex - regular expression to match the column content.
   * @return winning column (column with most matches). Or -1 (no matches).
   */
  public int getColumnByMatchingContent(String regex) {
    return getColumnByMatchingContent(regex,0);
  }
  /**
   * See {@link #getColumnByMatchingContent(String)}
   * @param regex - Regular expression
   * @param patternOptions - Static options as in Pattern.[Option]. E.g.
   * Pattern.CASE_INSENSITIVE
   * @return column number
   */
  public int getColumnByMatchingContent(String regex, int patternOptions) {
    Pattern pat;
    if (patternOptions>0) {
      pat = Pattern.compile(regex, patternOptions);
    } else {
      pat = Pattern.compile(regex);  
    }
    int threshold = 25; // Number of lines to match.
    
    if (this.data==null) {
      open(); // Open/ Reset the file if not already read in.
    }
    
    // Match pattern agains content
    int[] matches = new int[numCols];
    int matchesMax = 0;
    int matchesMaxId = -1;
    int lineNr=0;
    while (matchesMax<threshold) {
      
      // Get the next data line
      String[] line;
      if (this.data!=null) {
        if (lineNr==data.length) break; // EOF
        line = data[lineNr];
        lineNr++;
      } else {
        try {
          line = getNextLine();
        } catch (Exception e) {
          e.printStackTrace();
          break;
        }
        if (line==null) break; // EOF
      }
      if (line==null) continue;
      // ---
      
      // Match against pattern
      for (int j=0; j<line.length; j++) {
        if (line[j]==null) continue;
        
        if (pat.matcher(line[j]).matches()) {
          matches[j]++;
          if (matches[j]>matchesMax) {
            matchesMax = matches[j];
            matchesMaxId=j;
          }
        }
      }
    }
    
    // Reset pointer
    if (this.data==null) {
      open();
    }
     
    return matchesMaxId;
  }
  
  /**
   * Reads the whole file into memory.
   * Enables to use the getData() function.
   * @param filename
   */
  public void read() {
    try {
      
      // Initializes and opens the file
      open();
      
      // Get total number of lines in the file
      countNumberOfLines();
      
      // Finally... get the data
      data = new String[numDataLines][numCols];
      int nline=-1;
      String[] line;
      while ((line = getNextLine())!=null) {
        data[++nline] = line;
      }
      
    } catch (Exception ex) {ex.printStackTrace();}
  }
  
  /**
   * Opens the file.
   * Enables to use the getNextLine() function.
   * Reads the preamble.
   * @param filename
   */
  public void open() {
    try {
      
      // Infere separator char, get header and data start, etc.
      initialize();
      
      // Initialize a progress bar
      if (displayProgress) {
        progress = new FileReadProgress(filename);
        if (this.progressBar!=null) { // Custom progress bar
          this.progress.setProgressBar(progressBar);
        }
      }
      
      // Finally... get the data
      this.currentOpenFile = getAndResetInputReader(filename);
      int j=-1;
      preamble = new StringBuffer();
      while (currentOpenFile.ready()) {
        j++;
        if (j==firstConsistentLine && !containsHeaders || (j>firstConsistentLine) && containsHeaders ) {
          // We reached the first data line (don't read it!).
          break;
        }
        
        String line = currentOpenFile.readLine();
        if (displayProgress && progress!=null) progress.progress(line);
        line = line.trim();
        if (!(containsHeaders && j==firstConsistentLine)) {
          // Else, this line is the header.
          preamble.append(line+'\n');
        }
        
      }
      
    } catch (Exception ex) {ex.printStackTrace();}
  }
  
  /**
   * Requires to call open(file) first.
   * Returns the next line in the currently opened file, or null if the end of
   * the file has been reached. Throws an Exception, if no file is currently opened.
   * 
   * @return the next line in the currently opened file.
   * @throws Exception
   */
  public String[] getNextLine() throws Exception {
    if (currentOpenFile==null) throw new Exception("No file is currently opened.");
    if (!currentOpenFile.ready()) {
      close();
      return null;
    }
    
    String line = currentOpenFile.readLine();
    if (displayProgress && progress!=null) {
      progress.progress(line);
    }
    line = line.trim();
    String [] data = line.split(Pattern.quote(Character.toString(separatorChar)));
    
    // Post Process (trim and remove string indicators).
    for (int i=0; i<data.length; i++) {
      data[i] = data[i].trim();
      if (removeStringIndiciatorsAtCellStartEnd && ((data[i].startsWith("\"") && data[i].endsWith("\"")) ||
          (data[i].startsWith("'") && data[i].endsWith("'")))) {
        if (data[i].length()>2)
          data[i] = data[i].substring(1, data[i].length()-1);
        else data[i] = "";
      }
    }
    return data;
  }
  public void close() throws IOException {
    if (currentOpenFile!=null) {
      currentOpenFile.close();
      progress=null; // Might not be serializable.
    }
  }
  
  /**
   * Fill the numDataLines variable.
   * Requires: class has to be initialized (initialize() method).
   * @param filename
   * @throws IOException
   */
  private void countNumberOfLines() throws Exception {
    if (firstConsistentLine<0) initialize();
    
    // Count lines
    BufferedReader in = getAndResetInputReader(filename);
    int j=-1;
    int numDataLines = 0;
    long totalFileLength=0;
    while (in.ready()) {
      j++;
      String line = in.readLine();
      totalFileLength += line.length()+1; // +1 for \n
      line = line.trim();
      if (j<firstConsistentLine) continue; // Skip Preamble :-)
      
      char firstChar = line.charAt(0);
      if (this.containsHeaders && j==firstConsistentLine) {
        // The initialize function is setting the header. 
        continue;
      }
      // Are comments allowed to occur later on in the file?
      // => Only if the number of columns does not match the usual number.
      if (CommentIndicators.contains(firstChar)) {
        if (countCharAndIgnoreStrings(line, separatorChar)!=(numCols-1) ) continue;
      }
      
      numDataLines++;
    }
    this.numDataLines = numDataLines;
    in.close();
    
    // Update the total file length (more accurate).
    if (displayProgress) {
      if (progress!=null) {
        progress.setFileLength(totalFileLength);
      } else {
        progress = new FileReadProgress(totalFileLength);
        if (this.progressBar!=null) { // Custom progress bar
          this.progress.setProgressBar(progressBar);
        }
      }
    }    
  }
  
  /**
   * Returns the number of data lines in the file.
   * (All lines in the file, that do not belong to the preamble,
   * that are no comment lines and no header line).
   * @return
   */
  public int getNumberOfDataLines() {
    if (numDataLines<0) {
      // Only the case, if open has been called or neither open nor read.
      try {
        countNumberOfLines();
      } catch (Exception e) {e.printStackTrace();}
    }
    
    return numDataLines;
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
    if (s==null || s.length()<1) return false;
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
