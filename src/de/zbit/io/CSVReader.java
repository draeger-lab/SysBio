package de.zbit.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.zbit.util.AbstractProgressBar;
import de.zbit.util.FileReadProgress;
import de.zbit.util.StringUtil;

/**
 * Reads a CSV (e.g. tab delimited) file.
 * 
 * Many optios are provided for customization. It is recommended to leave
 * all default values, if you are unsure what you're doing.
 * 
 * There are two use cases:
 * Read the file at once, or line by line.
 * 
 * Examples:
 * 1:  Read the whole file at once:
 *   CSVReader a = new CSVReader("test.txt");
 *   a.setDisplayProgress(false); // Optional, false by default
 *   
 *   a.getData(); // The whole table, except headers
 *   a.getHeader(); // The header (if available)
 *   a.getPreamble(); // Everything, before actual table start
 *   
 * 2: Read the file, line by line (more memory save)
 *   String[] line;
 *   try {
 *     while ((line = a.getNextLine())!=null) {
 *       // Do something with the line
 *     }
 *   } catch (Exception e) {e.printStackTrace();}
 *   
 *   a.getHeader(); // The header (if available)
 *   a.getPreamble(); // Everything, before actual table start
 * 
 * @author wrzodek
 */
public class CSVReader implements Serializable, Cloneable {
  private static final long serialVersionUID = 7784651184371604357L;

  /**
   * All lines, starting with one of these characters will be skipped.
   * By default, "#" and ";" will be added (you may remove them manually).
   */
  private ArrayList<Character> CommentIndicators = new ArrayList<Character>();
  
  /**
   * Contains columns indices. All column indices in this array will
   * always be set to null directly after reading.
   * E.g. if this contains just [1] and reading the line "A B C"
   * it will result is a returned split of "[A], [null], [C]".
   */
  private ArrayList<Integer> setToNull = new ArrayList<Integer>();
  
  /**
   * Indicates, that this file contains headers
   */
  private boolean containsHeaders = true;
  
  /**
   * Auto detects {@link #containsHeaders}
   */
  private boolean autoDetectContainsHeaders = true;
  
  /**
   * The OpenFile Method has many advantages. It automatically downloads http urls,
   * it can extract files from zips and the url might lay within the same jar or is
   * an external file. Everything is handled automatically.
   */
  private boolean useOpenFileMethod=true;
  
  /**
   * Removes " symbol at cell start and end.
   * WARNING: You should use " for strings. Using ' is delicate because of terms
   * like "it's".
   */
  private boolean removeStringIndiciatorsAtCellStartEnd = true;
  
  /**
   * If true, treates multiple consecutive separators as one.
   * E.g. "H,A,,O" => "H,A,O"
   * If false - E.g. "H,A,,O" => "[H],[A],[],[O]"
   * 
   * Usually, no getter or setter required. Value is infered automatically.
   */
  private boolean treatMultipleConsecutiveSeparatorsAsOne=false;
  
  /**
   * Auto detects {@link #treatMultipleConsecutiveSeparatorsAsOne}
   */
  private boolean autoDetectTreatMultipleConsecutiveSeparatorsAsOne=true;
  
  
  /**
   * Performs a trim operation on each line after reading. Has crucial
   * impact if space (' ') is a separator char.
   * 
   * E.g.
   * trimLinesAfterReading=false: " Hallo" => "Hallo" is in column 2
   * trimLinesAfterReading=true: " Hallo" => "Hallo" is in column 1
   * 
   * Default: false
   */
  private boolean trimLinesAfterReading=false;
  
  /**
   * This should always be true.
   * Automatically detects where the content start (stable count of columns).
   */
  private boolean autoDetectContentStart = true;
  
  /**
   * If greater than 0 skips this number of lines directly after
   * opening the file for all operations.
   */
  private int skipLines=0;
  
  /**
   * A flag if the initialize method has already been calles successfuly.
   */
  private boolean isInitialized=false;
  
  /**
   * The column separator char.
   * If not set ('\u0000' = Null Character), it will be infered automatically.
   * '\u0001' equals the Regex whitespace (\\s).
   */
  private char separatorChar='\u0000';
  
  /**
   * ... if separatorChar is infered automatically, one of these chars in the given order
   * is chosen, based on a stable count of the these chars across multiple lines.
   * 
   * Remark: The order of these chars (or the chars itself) should NOT be modified
   * without notice to the author of this class.
   * '\u0001' equals the Regex whitespace (\\s).
   */
  private char[] trennzeichen = new char[]{'\t', ',', ';', '|', '/', ' ', '\u0001'};
  
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
  private transient BufferedReader currentOpenFile=null;
  
  /**
   * Display the progress, while reading the file.
   */
  private boolean displayProgress=false;
  private transient FileReadProgress progress=null;
  private transient AbstractProgressBar progressBar=null;
  
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
  

  /**
   * Initializes a CSV Reader.
   * 
   * The separator char, if multiple separator chars should be merged to one,
   * the data start line, etc. - Everything will be infered automatically.
   * 
   * @param filename
   * @param containsHeaders
   */
  public CSVReader(String filename, boolean containsHeaders) {
    this(filename);
    setContainsHeaders(containsHeaders);
  }
  public CSVReader(String filename) {
    isInitialized=false;
    CommentIndicators.add(';');
    CommentIndicators.add('#');
    
    this.filename = filename;
    autoDetectContainsHeaders=true;
  }
  
  /**
   * Adds a new Symbol, which indicates that this line should be treated as a comment (=>ignored)
   */
  public void addCommentInidicator (char c) {
    if (!(CommentIndicators.contains((c)))) {
      CommentIndicators.add((c));
      isInitialized=false;
    }
  }
  public void removeCommentIndicator (char c) {
    while (CommentIndicators.contains((c))) {
      CommentIndicators.remove((c));
      isInitialized=false;
    }
  }
  
  /**
   * Does your CSV file contains a header line?
   * @param b yes (true) or false (no). Default: yes
   */
  public void setContainsHeaders(boolean b) {
    if (b!=containsHeaders) isInitialized=false;
    containsHeaders = b;
    autoDetectContainsHeaders=false;
  }
  
  public boolean getContainsHeaders() {
    if (!isInitialized && autoDetectContainsHeaders) {
      try {
        initialize();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }  
    
    return this.containsHeaders;
  }
  
  /**
   * Auto detects if your file contains headers. After initialization
   * (through .open() or .read() ), you can get the predicted value with
   * getContainsHeaders().
   * @param b yes (true) or false (no). Default: yes
   */
  public void setAutoDetectContainsHeaders(boolean b) {
    if (b!=autoDetectContainsHeaders) isInitialized=false;
    autoDetectContainsHeaders = b;
  }
  
  /**
   * Automatically detects where the content start (stable count of columns).
   * IT IS STRONGLY RECOMMENDED TO KEEP THIS TRUE!
   * @param b
   */
  public void setAutoDetectContentStart(boolean b) {
    if (b!=autoDetectContentStart) {
      isInitialized=false;
      firstConsistentLine=-1;
    }
    this.autoDetectContentStart = b;
  }
  
  /**
   * @return line number where the content starts (Consistent counts
   * of the separator char, below preamble).
   * Can be set indirectly by using {@link #setSkipLines(int)}.
   * Is detected automatically by the initialize method. Do NOT try
   * to set this value directly.
   */
  public int getContentStartLine() {
    if (!isInitialized && firstConsistentLine<0) {
      try {
        initialize();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    return firstConsistentLine;
  }
  
  /**
   * Performs a trim operation on each line after reading. Has crucial
   * impact if space (' ') is a separator char.
   * 
   * E.g.
   * trimLinesAfterReading=false: " Hallo" => "Hallo" is in column 2
   * trimLinesAfterReading=true: " Hallo" => "Hallo" is in column 1
   * 
   * Default: false
   */
  public void setTrimLinesAfterReading(boolean trimLinesAfterReading) {
    if (trimLinesAfterReading!= this.trimLinesAfterReading)
      isInitialized=false;
    this.trimLinesAfterReading = trimLinesAfterReading;
  }
  /**
   * If greater than 0 skips this number of lines directly after
   * opening the file for all operations.
   * 
   * This does NOT disalbe autoDetectContentStart. That means, that the
   * actual table may start SOMEWHERE below this number of lines but not
   * before that much lines.
   * 
   * Default: 0.
   */
  public void setSkipLines(int skipLines) {
    if (skipLines>firstConsistentLine ||
        !autoDetectContentStart && skipLines!=firstConsistentLine)
      isInitialized=false;
    this.skipLines = skipLines;
  }
  
  /**
   * Set the file no open with this reader.
   * Use this method with caution - it does not reset any settings. For instance,
   * if on the last file a separator has been infered automatically. It will NOT 
   * be infered again. Use a new instance of this class to infere all values
   * automatically again.
   * @param filename
   */
  public void setFilename(String filename) {
    this.filename = filename;
    isInitialized=false;
  }
  
  /**
   * @return the filename currently opened with this CSVReader instance.
   */
  public String getFilename() {
    return this.filename;
  }
  
  /**
   * Sets the skipLines variable (see {@link #setSkipLines(int)})
   * automatically to start reading the file in the line, below the
   * line that equals s. Overrides already set vales from setSkipLines().
   * @param s - any string that should equal a line in the inFile.
   * @throws IOException 
   */
  public void setSkipLinesUntilString(String s) throws IOException {
    this.skipLines=0;
    
    if (trimLinesAfterReading) s = s.trim();
    BufferedReader r = getAndResetInputReader(this.filename);
    String line;
    int toSkip=0;
    while ((line = r.readLine())!=null) {
      toSkip++;
      if (trimLinesAfterReading) line = line.trim();
      if (line.equalsIgnoreCase(s)) break;
    }
    r.close();
    
    if (line==null) throw new IOException("Line '" +s+ "' not found in file '" + filename+"'.");
    setSkipLines(toSkip);
  }
  
  /**
   * If true, treates multiple consecutive separators as one.
   * E.g. "H,A,,O" => "H,A,O"
   * If false - E.g. "H,A,,O" => "[H],[A],[],[O]"
   * 
   * Usually, no getting or setting required. Value is infered automatically.
   * @return current value. May be changed after inizialization if
   * autoDetectTreatMultipleConsecutiveSeparatorsAsOne is set.
   */
  public boolean getTreatMultipleConsecutiveSeparatorsAsOne() {
    if (!isInitialized && autoDetectTreatMultipleConsecutiveSeparatorsAsOne) {
      try {
        initialize();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    return treatMultipleConsecutiveSeparatorsAsOne;
  }
  /**
   * See {@link #getTreatMultipleConsecutiveSeparatorsAsOne()}
   * 
   * Using this function disables auto detection.
   * 
   * @param treatMultipleConsecutiveSeparatorsAsOne
   */
  public void setTreatMultipleConsecutiveSeparatorsAsOne(boolean treatMultipleConsecutiveSeparatorsAsOne) {
    if (treatMultipleConsecutiveSeparatorsAsOne!=
      this.treatMultipleConsecutiveSeparatorsAsOne) isInitialized=false;
    this.treatMultipleConsecutiveSeparatorsAsOne = treatMultipleConsecutiveSeparatorsAsOne;
    autoDetectTreatMultipleConsecutiveSeparatorsAsOne = false;
  }
  
  /**
   * Will infere automatically if multiple consecutive separators should
   * be treated as one.
   * See {@link #getTreatMultipleConsecutiveSeparatorsAsOne()} for more
   * information. 
   * 
   * Default: true
   * @param autoDetectTreatMultipleConsecutiveSeparatorsAsOne
   */
  public void setAutoDetectTreatMultipleConsecutiveSeparatorsAsOne(boolean autoDetectTreatMultipleConsecutiveSeparatorsAsOne) {
    if (autoDetectTreatMultipleConsecutiveSeparatorsAsOne!=
      this.autoDetectTreatMultipleConsecutiveSeparatorsAsOne) isInitialized=false;
    
    this.autoDetectTreatMultipleConsecutiveSeparatorsAsOne = autoDetectTreatMultipleConsecutiveSeparatorsAsOne;
  }
  /**
   * Set wether you want to remove the char " or ' when it occurs at the start and end of a cell.
   * @param b - if false will return e.g. ["hallo a"]; if true e.g.  [hallo a]. Default: true.
   */
  public void setRemoveStringIndiciatorsAtCellStartEnd(boolean b) {
    removeStringIndiciatorsAtCellStartEnd = b;
  }
  /**
   * Set the separator char, by which columns are separated. If not set, the algorithm will try to infer this automatically.
   * Set it to '\u0000' to infere it automatically.
   * Set it to '\u0001' for any whitespace character (Regex \\s).
   * @param c = column separator char.
   */
  public void setSeparatorChar(char c) {
    isInitialized=false;
    separatorChar = c;
  }
  
  /**
   * Returns the separator char, that separates the columns.
   * Special return values:
   * - The char equals '\u0000' if it still needs to be infered automatically.
   * - The char equals '\u0001' if it is the regex pattern "\\s" (any whitespace character).
   * @return
   */
  public char getSeparatorChar() {
    if (!isInitialized && separatorChar=='\u0000') {
      try {
        initialize();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
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
    if (useOpenFileMethod!= b) isInitialized=false;
    this.useOpenFileMethod=b;
  }
  
  /**
   * If data has already been read - remove the given column
   * (more specific: set it to null. This does not shift indices).
   * If data has not been read, this will permanently set the column to
   * null, directly after reading it (getNextLine(int column) will always
   * be null).
   *  
   * The intention is freeing memory by removing unused data.
   * @param column numbers.
   */
  public void setNull(int[] columns) {
    if (data==null) {
      for (int i=0; i<columns.length; i++)
        setNull(columns[i]);
    } else {
    for (int i=0; i<data.length; i++)
      if (data[i]!=null)
        for (int j=0; j<columns.length; j++)
          data[i][columns[j]] = null;
    }
  }
  
  /**
   * If data has already been read - remove the given column
   * (more specific: set it to null. This does not shift indices).
   * If data has not been read, this will permanently set the column to
   * null, directly after reading it (getNextLine(int column) will always
   * be null).
   *  
   * The intention is freeing memory by removing unused data.
   * @param column numbers.
   */
  public void setNull(int column) {
    if (data==null) {
      if (!setToNull.contains(column))
        setToNull.add(column);
    } else {
      for (int i=0; i<data.length; i++)
        if (data[i]!=null)
          data[i][column] = null;
    }
  }
  
  
  /**
   * Allows to override the progressBar.
   * Warning: A progressBar is only initialized if
   * 1 displayProgress is enabled (by setDisplayProgress(boolean b) )
   * 2 the "open" method
   */
  public void setProgressBar(AbstractProgressBar progress) {
    progressBar = progress;
    if (this.progress!=null) {
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
    if (data==null) try {
      readUsingArrayList();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this.data;
  }

  /**
   * Get the whole file content (except headers and preamble/comments).
   * Will NOT read the file, if not already done.
   * @return
   */
  public String[][] getDataButDoNotReadIfNotAvailable(){
    return this.data;
  }
  
  /**
   * Get the file headers, if file contains headers (to be set manually).
   * Null else.
   * @return
   */
  public String[] getHeader(){
    if (containsHeaders && headers==null ||
        !isInitialized) {
      try {
        initialize();
      } catch (IOException e) {e.printStackTrace();}
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
        if (!isInitialized) initialize();
      } catch (IOException e) {e.printStackTrace();}
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
    if (!isInitialized) {
      try {
        initialize();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
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
    if (!isInitialized) {
      try {
        initialize();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    if (headers==null) return -1;
    for (int i=0; i<headers.length;i++) {
      if (headers[i].equalsIgnoreCase(s)) return i;
    }
    return -1;
  }
  
  private BufferedReader getAndResetInputReader(String filename) throws IOException {
    return getAndResetInputReader(filename, null);
  }
  private BufferedReader getAndResetInputReader(String filename, StringBuffer preamble) throws IOException {
    BufferedReader in;
    if (useOpenFileMethod) {
      // Use the OpenFile Method to automatically extract ZIP archives and such.
      in = OpenFile.openFile(filename);
    } else {
      in = new BufferedReader(new FileReader(filename));
    }
    
    // Eventually skip first x lines.
    if (skipLines>0) {
      String line; int curLine=0;
      while (curLine<skipLines && (line=in.readLine())!=null) {
        if (preamble!=null) preamble.append(line+'\n');
        curLine++;
      }
    }
    
    return in;
  }
  
  /**
   * Initializes the class, based on the given file. More specific:
   * - Inferes the separator char (if not set)
   * - Sets the line number, where the data starts (firstConsistentLine)
   * - Sets the number of columns (numCols)
   * - Reads the headers
   * @param filename
   * @throws IOException if file appears not to be a valid CSV file.
   */
  private void initialize() throws IOException {
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
    int dimension = autoDetectTreatMultipleConsecutiveSeparatorsAsOne?separatorChars.length*2:separatorChars.length;
    int consistentCounts[] = new int[dimension]; // Seit sovielen zeilen schon genau "count"
    int firstConsistentLines[] = new int[dimension]; // Erste
    String[] firstConsistentLineString = new String[dimension]; // For reading headers
    int counts[] = new int[dimension]; // Anzahl eines Trennzeichens (= #Spalten)
    for (int i=0; i<consistentCounts.length; i++) 
    {consistentCounts[i] = 0; counts[i] = 0;}
    
    if (autoDetectContainsHeaders) containsHeaders = true; // will be set later
    firstConsistentLine = -1;
    numCols=-1;
    
    // Read a part of the file and fill variables.
    int max=0; // If file had less then "threshold" consistent lines in total; Take the max.
    int j = -1 + this.skipLines;
    String firstConsistentLineStringOfMax=null;
    while (in.ready() && max<=threshold) { //  && separatorChar=='\u0000'
      j++;
      String line = in.readLine();
      if (trimLinesAfterReading && line!=null) line = line.trim();
      if (line.length()<1 && autoDetectContentStart) continue;
      char firstChar = line.length()>0?line.charAt(0):'\u0000';
      //if (CommentIndicators.contains(firstChar)) continue; // <=breakes header detection
      
      // Infere separator char
      // Sucht 25x(=threshold) das selbe Zeichen genau gleich oft. Wenn dem so ist, ist dies unser separatorChar.
      for (int i=0; i<dimension; i++) {
        char curSepChar = separatorChars[i % separatorChars.length];
        boolean treatMultipleConsecutiveSeparatorsAsOne=this.treatMultipleConsecutiveSeparatorsAsOne;
        if (autoDetectTreatMultipleConsecutiveSeparatorsAsOne)
          treatMultipleConsecutiveSeparatorsAsOne=(i>=separatorChars.length);
        
        // Count chars for the current separator chars, ignoring occurences in strings.
        int aktCounts=0;
        aktCounts = countChar(line, curSepChar, treatMultipleConsecutiveSeparatorsAsOne, true);
        
        // If number of columns is consistent, increment counter. If not, reset.
        if (counts[i]==aktCounts && aktCounts>0 && !CommentIndicators.contains(firstChar)) {
          consistentCounts[i]++;
        } else {
          // Headers are allowed to start with a comment indicator.
          consistentCounts[i]=1; // 0: Requires at least 2 data rows (more reliable detection. 1:also reads file with just one data row
          counts[i]=aktCounts;
          firstConsistentLines[i]=j;
          firstConsistentLineString[i] = line;
        }
        
        // If more consistent counts, or same but less columns, take this char.
        if (consistentCounts[i]>max || consistentCounts[i]==max && numCols<=aktCounts) {
          separatorChar = separatorChars[i % separatorChars.length];
          this.treatMultipleConsecutiveSeparatorsAsOne=treatMultipleConsecutiveSeparatorsAsOne;
          numCols = counts[i]+1; // +1 because number of columns is number of separator chars in line+1
          if (autoDetectContentStart) {
            firstConsistentLine = firstConsistentLines[i];
            firstConsistentLineStringOfMax=firstConsistentLineString[i];
          } else if (firstConsistentLineStringOfMax==null) {
            firstConsistentLineStringOfMax=firstConsistentLineString[i];
            firstConsistentLine = firstConsistentLines[i];
          }
          max = consistentCounts[i];
          
          // Breakup here if threshold consistent lines reached.
          if (consistentCounts[i]>threshold) break;
        }
      }
    }
    in.close();
    
    // Fill the headers variable
    if (this.containsHeaders && firstConsistentLineStringOfMax!=null) {
      headers=getSplits(firstConsistentLineStringOfMax, separatorChar, this.treatMultipleConsecutiveSeparatorsAsOne, true);
      // Headers do often start with a comment symbol.
      if (headers!=null && headers.length>0 && headers[0].length()>0)
        if (CommentIndicators.contains(headers[0].charAt(0))) headers[0] = headers[0].substring(1); 
    } else {
      headers=null;
    }
    
    // Give an error in the very unlikely case that no separator char matches. 
    if (firstConsistentLine<0 || max<1) {
      throw new IOException ("Could not analyze input file. Probably not a CSV file.");      
    }
    
    isInitialized=true;
    
    // Auto detect if file contains headers.
    if (autoDetectContainsHeaders) {
      containsHeaders = containsHeaders();
      if (!containsHeaders) headers=null;
    }
    
    /*if (separatorChar=='\u0001')
      System.out.print("Winner: <RegexWS>");
    else if (separatorChar=='\u0000')
      System.out.print("No Winner.");
    else
      System.out.print("Winner: '" + separatorChar + "'");
    System.out.println(" Max counts: " + max + " Cols: " + numCols);
    System.out.println("Treat consecutive as one: " + treatMultipleConsecutiveSeparatorsAsOne);*/
    
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
   * @throws IOException 
   */
  public int getColumnByMatchingContent(String regex) throws IOException {
    return getColumnByMatchingContent(regex,0);
  }
  /**
   * See {@link #getColumnByMatchingContent(String)}
   * @param regex - Regular expression
   * @param patternOptions - Static options as in Pattern.[Option]. E.g.
   * Pattern.CASE_INSENSITIVE. 0 for no options.
   * @return column number
   * @throws IOException 
   */
  public int getColumnByMatchingContent(String regex, int patternOptions) throws IOException {
    return getColumnByMatchingContent(regex, patternOptions, 20000);
  }
  
  /**
   * See {@link #getColumnByMatchingContent(String, int)}
   * @param regex - Regular expression
   * @param patternOptions - Static options as in Pattern.[Option]. E.g.
   * Pattern.CASE_INSENSITIVE. 0 for no options.
   * @param maxLinesToCheck - Maximum number of lines to check. Terminates
   * this search if more lines than this value have been read and no
   * matching column has been found. 0 to disable. Default: 20,000
   * @return column number
   * @throws IOException
   */
  public int getColumnByMatchingContent(String regex, int patternOptions, int maxLinesToCheck) throws IOException {
    Pattern pat;
    if (patternOptions>0) {
      pat = Pattern.compile(regex, patternOptions);
    } else {
      pat = Pattern.compile(regex);  
    }
    int threshold = 25; // Number of lines to match.
    
    FileReadProgress tempProgress = this.progress;;
    if (this.data==null) {
      this.progress = null;
      open(); // Open/ Reset the file if not already read in.
    }
    
    // Match pattern agains content
    int[] matches = new int[numCols];
    int matchesMax = 0;
    int matchesMaxId = -1;
    int lineNr=0; int linesRead=0;
    while (matchesMax<threshold) {
      linesRead++;
      if (maxLinesToCheck>0 && linesRead>maxLinesToCheck) break;
      
      // Get the next data line
      String[] line;
      if (this.data!=null) {
        if (lineNr==data.length) break; // EOF
        line = data[lineNr];
        lineNr++;
      } else {
        try {
          line = getNextLine();
        } catch (IOException e) {
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
      close();
      this.progress=tempProgress;
      open();
    }
     
    return matchesMaxId;
  }
  
  /**
   * Reads the whole file into memory.
   * Enables to use the getData() function.
   * @param filename
   * @throws IOException 
   */
  public void read() throws IOException {
    
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
  }
  
  /**
   * Reads the whole file into memory.
   * Enables to use the getData() function.
   * 
   * read() reads the file twice, but declares a perfectly
   * matching array, speeding up for fast hard drives and
   * small files.
   * 
   * readUsingArrayList() reads the file once, but using an
   * ArrayList and is slower on fast computers or small files.
   * 
   * @param filename
   * @throws IOException 
   */
  public void readUsingArrayList() throws IOException {
    
    // Initializes and opens the file
    open();
    
    // Finally... get the data
    ArrayList<String[]> arr = new ArrayList<String[]>();
    String[] line;
    while ((line = getNextLine())!=null) {
      arr.add(line);
    }
    
    data = arr.toArray(new String[0][0]);
  }
  
  /**
   * Opens the file.
   * Enables to use the getNextLine() function.
   * Reads the preamble.
   * @param filename
   */
  public void open() throws IOException {
    
    // Infere separator char, get header and data start, etc.
    if (!isInitialized) initialize();
    
    // Initialize a progress bar
    if (displayProgress) {
      progress = new FileReadProgress(filename);
      if (this.progressBar!=null) { // Custom progress bar
        this.progress.setProgressBar(progressBar);
      }
    }
    
    // Finally... get the data
    preamble = new StringBuffer();
    this.currentOpenFile = getAndResetInputReader(filename, preamble);
    int j=-1+skipLines;
    while (currentOpenFile.ready()) {
      j++;
      if (j==firstConsistentLine && !containsHeaders || (j>firstConsistentLine) && containsHeaders ) {
        // We reached the first data line (don't read it!).
        break;
      }
      
      String line = currentOpenFile.readLine();
      if (displayProgress && progress!=null) progress.progress(line);
      if (trimLinesAfterReading) line = line.trim();
      if (!(containsHeaders && j==firstConsistentLine)) {
        // Else, this line is the header.
        preamble.append(line+'\n');
      }
    }
    
  }
  
  /**
   * Returns the next line in the currently opened file, or null if the end of
   * the file has been reached. Throws an Exception, if no file is currently opened.
   * 
   * Remark: It is possible that trailing empty cells will be removed!
   * 
   * @return the next line in the currently opened file.
   * @throws IOException
   */
  public String[] getNextLine() throws IOException {
    if (currentOpenFile==null) {
      open(); //throw new Exception("No file is currently opened.");
      if (currentOpenFile==null) return null; // Open() threw exception.
    }
    if (!currentOpenFile.ready()) {
      close();
      return null;
    }
    
    // Read next line, draw progress, split into columns
    String line = currentOpenFile.readLine();
    if (displayProgress && progress!=null) progress.progress(line);
    if (trimLinesAfterReading) line = line.trim();
    String [] data;
    data = getSplits(line,separatorChar,this.treatMultipleConsecutiveSeparatorsAsOne, true);
    
    // Post Process (trim and remove string indicators).
    for (int i=0; i<data.length; i++) {
      if (setToNull!=null && setToNull.contains(i)) {
        data[i]=null;
        continue;
      }
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
      currentOpenFile=null;
      if (progress!=null) {
        progress.finished();
        progress=null; // Might not be serializable.
      }
    }
  }
  
  private boolean containsHeaders() {
    try {
      // Number of lines to "peek" into file.
      int threshold = 25;
      
      // Infere separator char, get header and data start, etc.
      if (!isInitialized) {
        // If calling from initialize.. prevent loop here
        boolean b = new Boolean(autoDetectContainsHeaders);
        autoDetectContainsHeaders = false;
        initialize();
        autoDetectContainsHeaders = b;
      }
      
      // Declate variables to examine
      String[] headerLine = null; // Potential header
      String[][] dataLine = new String[threshold][]; // Potential data line.
      
      // Finally... get the data, NOT Using the global variable
      BufferedReader currentOpenFile = getAndResetInputReader(filename);
      int j=-1+skipLines;
      while (currentOpenFile.ready()) {
        j++;
        
        String line = currentOpenFile.readLine();
        if (trimLinesAfterReading) line = line.trim();
        if (j==firstConsistentLine) {
          headerLine = getSplits(line, separatorChar, treatMultipleConsecutiveSeparatorsAsOne, true);
        } else if (j>firstConsistentLine) {
          if (j-firstConsistentLine-1>=dataLine.length) break;
          dataLine[j-firstConsistentLine-1] = getSplits(line, separatorChar, treatMultipleConsecutiveSeparatorsAsOne, true);
        }
      }
      currentOpenFile.close();
      
      return containsHeaders(headerLine, dataLine);
    } catch (Exception ex) {ex.printStackTrace();}
    return false;
  }
  
  private boolean containsHeaders(String[] headerLine, String[][] dataLine) {
    if (headerLine==null || dataLine==null || dataLine.length<1) return false;
    /*
     * Different posibilities (each via versa):
     * 1 Header cell is no number, data cell is number
     * 2 Longest common prefix / suffix
     * 3 Every data cell has the same length. Header cell not.
     * 4 Data cells are binary enum, Header not.
     */
    int nonNullLines=0;
    
    // For 1
    int[] isNumber = new int[headerLine.length];
    
    // For 2
    String[] prefix = new String[headerLine.length];
    int[] numPrefix = new int[headerLine.length];
    String[] suffix = new String[headerLine.length];
    int[] numSuffix = new int[headerLine.length];
    
    // For 3
    int[] sameLength = new int[headerLine.length]; // length
    int[] maxSameLength = new int[headerLine.length]; // Number of elements
    
    // For 4
    boolean[] isBinary = new boolean[headerLine.length];
    String[] stringOne = new String[headerLine.length]; // If binary, string1
    String[] stringTwo = new String[headerLine.length]; // If binary, string2
    
    // Fill 1 - isNumber variable
    for (int row=0; row<dataLine.length; row++) {
      if (dataLine[row]==null) continue;
      nonNullLines++;
      for (int col=0; col<Math.min(dataLine[row].length, headerLine.length); col++) {
        if (isNumber(dataLine[row][col], false)) isNumber[col]+=1;
      }
    }
    
    // Fill 2-4
    if (nonNullLines>0) {
      for (int col=0; col<headerLine.length; col++) {
        String[] column = StringUtil.getColumn(dataLine, col);
        
        // 2
        prefix[col]  = StringUtil.getLongestCommonPrefix(column, true);
        suffix[col]  = StringUtil.getLongestCommonPrefix(column, true);
        if (prefix[col].length()>0 || suffix[col].length()>0) {
          for (int i=0; i<column.length; i++) {
            if (column[i]==null) continue;
            if (column[i].startsWith(prefix[col])) numPrefix[col]++;
            if (column[i].endsWith(suffix[col])) numSuffix[col]++;
          }
        }
        
        // 3
        int [] r = StringUtil.getLongestCommonLength(column);
        sameLength[col] = r[0];
        maxSameLength[col] = r[1];
        
        // 4
        String[] re = containsBinaryData(column, stringOne[col], stringTwo[col]);
        isBinary[col] = Boolean.parseBoolean(re[0]); stringOne[col] = re[1]; stringTwo[col] = re[2];
      }
    }
    if (nonNullLines<1) return false;
    
    // Compare to potential header
    int headerMatchesData=0; int headerNOTMatchesData=0;
    for (int col=0; col<headerLine.length; col++) {
      String cell = headerLine[col];
      
      // If 90% have a "attribute", it's relevant
      short checkAttribute=0; // 0=attribute seems to be random.
      
      // Check "isNumber" attribute.
      int perc = isNumber[col]/nonNullLines;
      if (perc>=0.9) checkAttribute=1; // yes
      if (perc<=0.1) checkAttribute=2; // yes, but the other way round
      if (checkAttribute!=0) {
        boolean b = isNumber(cell, false);
        if (checkAttribute==1 && b) headerMatchesData++;
        else if (checkAttribute==2 && !b) headerMatchesData++;
        else headerNOTMatchesData++;
      }

      // Check "prefix" attribute.
      checkAttribute=0; // 0=attribute seems to be random.
      perc = numPrefix[col]/nonNullLines;
      if (perc>=0.9) checkAttribute=1; // yes
      if (prefix[col].length()>0 && checkAttribute!=0) {
        if (cell.startsWith(prefix[col])) headerMatchesData++;
        else headerNOTMatchesData++;
      }
      
      // Check "suffix" attribute.
      checkAttribute=0; // 0=attribute seems to be random.
      perc = numSuffix[col]/nonNullLines;
      if (perc>=0.9) checkAttribute=1; // yes
      if (suffix[col].length()>0 && checkAttribute!=0) {
        if (cell.endsWith(suffix[col])) headerMatchesData++;
        else headerNOTMatchesData++;
      }
      
      // Check "length" attribute.
      checkAttribute=0; // 0=attribute seems to be random.
      perc = maxSameLength[col]/nonNullLines;
      if (perc>=0.9) checkAttribute=1; // yes
      if (checkAttribute!=0) {
        if (cell.length()==sameLength[col]) headerMatchesData++;
        else headerNOTMatchesData++;
      }
      
      // Check "binary data" attribute 
      checkAttribute=0; // 0=attribute seems to be random.
      if (isBinary[col] && stringOne[col]!=null && stringOne[col].length()>0) checkAttribute=1; // yes
      if (checkAttribute!=0) {
        if (cell.equalsIgnoreCase(stringOne[col]) || cell.equalsIgnoreCase(stringTwo[col])) headerMatchesData++;
        else headerNOTMatchesData++;
      }
    }
    
    return (headerNOTMatchesData>headerMatchesData);
  }

  
  /**
   * Skips null or empty cells!
   * @param s - String[] of a column.
   * @return if the column contains binary data
   */
  @SuppressWarnings("unused")
  private static boolean containsBinaryData(String[] s) {
    return Boolean.parseBoolean(containsBinaryData(s, new String(), new String())[0]);
  }
  /**
   * Checks if a column contains binary data (enum).
   * @param s - String[] of a column.
   * @param one - binary enum 1 (will be filled, should initially be null).
   * @param two - binary enum 2 (will be filled, should initially be null).
   * @return String[]{true/false, one, two};
   */
  private static String[] containsBinaryData(String[] s, String one, String two) {
    one=null; two=null;
    for (int i=0; i<s.length; i++) {
      if (s[i]==null || s[i].length()<1) continue;
      if (one==null) one = s[i];
      else if (two==null && !s[i].equalsIgnoreCase(one)) two = s[i];
      else if (s[i].equalsIgnoreCase(one) || s[i].equalsIgnoreCase(two)) continue;
      else return new String[]{"false", one,two};
    }
    return new String[]{"true", one,two};
  }

  
  /**
   * Fill the numDataLines variable.
   * Requires: class has to be initialized (initialize() method).
   * @param filename
   * @throws IOException
   */
  private void countNumberOfLines() throws IOException {
    if (!isInitialized) initialize();
    
    // Count lines
    BufferedReader in = getAndResetInputReader(filename);
    int j=-1+skipLines;
    int numDataLines = 0;
    long totalFileLength=0;
    Pattern whitespace = Pattern.compile("\\s");
    while (in.ready()) {
      j++;
      String line = in.readLine();
      if (trimLinesAfterReading) line = line.trim();
      totalFileLength += line.length()+1; // +1 for \n
      //line = line.trim();
      if (j<firstConsistentLine) continue; // Skip Preamble :-)
      
      char firstChar = line.length()>0?line.charAt(0):'\u0000';
      if (this.containsHeaders && j==firstConsistentLine) {
        // The initialize function is setting the header. 
        continue;
      }
      // Are comments allowed to occur later on in the file?
      // => Only if the number of columns does not match the usual number.
      if (CommentIndicators.contains(firstChar)) {
        
        int aktCounts=0;
        if (separatorChar=='\u0001') { // = Regex whitespace (Pattern.compile("\\s")).
          aktCounts = countChar(line, whitespace, treatMultipleConsecutiveSeparatorsAsOne, true);
        } else {
          aktCounts = countChar(line, separatorChar, treatMultipleConsecutiveSeparatorsAsOne, true);
        }
        
        if (aktCounts!=(numCols-1) ) continue;
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
    if (numDataLines<0 || !isInitialized) {
      // Only the case, if open has been called or neither open nor read.
      try {
        countNumberOfLines();
      } catch (IOException e) {e.printStackTrace();}
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
  private static int countChar(String input, char toCount, boolean treatMultipleConsecutiveCharsAsOne, boolean IgnoreStrings){
    if (toCount=='\u0001') return countChar(input, Pattern.compile("\\s"), treatMultipleConsecutiveCharsAsOne, IgnoreStrings);
    int counter = 0;
    boolean skip1 = false, skip2 = false;
    char lastC = '\u0000'; // \u0000 = The null character.
    for(char c: input.toCharArray()){
      if (c=='"' && IgnoreStrings) skip1 = !skip1;
      //if (c=='\'') skip2 = !skip2;//Complicated because of terms like "it's"
      if(c==toCount && !skip1 && !skip2) {
        if (treatMultipleConsecutiveCharsAsOne && lastC!=c
            || !treatMultipleConsecutiveCharsAsOne) {
          counter++;
        }
      }
      lastC=c;
    }
    return counter;
  }
  
  /**
   * Counts the number of hits of the compiled Pattern pat int the input-String. 
   * @param input
   * @param pat
   * @param treatMultipleConsecutiveCharsAsOne
   * @return
   */
  private static int countChar(String input, Pattern pat, boolean treatMultipleConsecutiveCharsAsOne, boolean IgnoreStrings){
    // Count matches, skip strings.
    int counter=0;
    String newLine = "";
    boolean skip1 = false, skip2 = false;
    for(char c: input.toCharArray()){
      if (c=='"' && IgnoreStrings) skip1 = !skip1;
      //if (c=='\'') skip2 = !skip2;//Complicated because of terms like "it's"
      
      if(!skip1 && !skip2) {
        newLine+=c;
      } else {
        if (newLine.length()>0) {
          counter += countMatches(pat.matcher(newLine), treatMultipleConsecutiveCharsAsOne);        
          newLine = "";
        }
      }
    }
    
    // Remaining string (or full string, if no internal strings)
    if (newLine.length()>0) {
      counter += countMatches(pat.matcher(newLine), treatMultipleConsecutiveCharsAsOne);
    }
    
    return counter;
  }


  /**
   * Helper method for {@link #countCharAndIgnoreStrings(String, Pattern, boolean)}.
   * Counts the number of matches.
   * @param m - Matcher with a pattern compiled on a target string.
   * @param skipConsecutiveMatches
   * @return number of matches of this pattern, skipts consecutive matches if flag is set.
   */
  private static int countMatches(Matcher m, boolean skipConsecutiveMatches) {
    // Count matches, eventually ignore consecutive ones.
    int counter=0;
    int lastEnd = -1;
    while (m.find()) {
      if (!skipConsecutiveMatches) counter++;
      else {
        if (m.start()!=lastEnd) counter++;
        lastEnd = m.end();
      }
    }
    return counter;
  }
  
  private static String[] getSplits(String input, char separator, boolean skipConsecutiveMatches, boolean skipMatchesInStrings) {
    if (separator=='\u0001') return getSplits(input, Pattern.compile("\\s"), skipConsecutiveMatches, skipMatchesInStrings);
    // Get columns. A little bit more flexible than a simple .split()!
    String splitter = Pattern.quote(Character.toString(separator));
    ArrayList<String> splits = new ArrayList<String>();
    
    String newLine = ""; String stringColumn="";
    boolean skip1 = false, skip2 = false;
    Character lastC='\u0000';
    for(char c: input.toCharArray()){
      if (c=='"' && skipMatchesInStrings) {
        skip1 = !skip1;
        stringColumn+=c;
        continue;
      }
      //if (c=='\'') skip2 = !skip2;//Complicated because of terms like "it's"
      
      if(!skip1 && !skip2) {
        if (stringColumn.length()>0) {
          splits.add(stringColumn);
          stringColumn="";
        }
        if (!(skipConsecutiveMatches && c==separator && lastC==c)) {
          newLine+=c;
        }
        lastC=c;
      } else {
        stringColumn+=c;
        
        if (newLine.length()>0) {
          // Add all except the last hit (because: 'a "b"' => newLine is here 'a ' and would result in [a] [].
          String[] ret = newLine.split(splitter);
          
          // Unfortunately, ret is emty if it equals splitter or consecutive instances of it.
          // Try to reconstruct empty cells.
          if (ret.length==0) {
            String s = newLine.replace(Character.toString(separator), "");
            if (s.length()>0) ret = new String[]{s};
            else {
              ret = new String[countChar(newLine, separator, skipConsecutiveMatches, skipMatchesInStrings)];
            }
          }
            
          for (String s: ret)
            splits.add((s!=null?s:""));
          //if (ret.length==0) 
            //splits.add(newLine.replace(Character.toString(separator), ""));
          newLine = "";
          //skipFirstMatch = true;
        }        
      }
    }
    
    // Remaining string (or full string, if no internal strings)
    if (newLine.length()>0) {
      // Take last, skip first if splits isn't empty.
      String[] ret = newLine.split(splitter);
      for (String s: ret)
        splits.add(s);
    }
    
    
    return splits.toArray(new String[0]);
    
  }
  
  
  private static String[] getSplits(String input, Pattern separator, boolean skipConsecutiveMatches, boolean skipMatchesInStrings) {
    // Get columns. A little bit more flexible than a simple .split()!
    ArrayList<String> splits = new ArrayList<String>();
    
    /*
     * skipFirstMatch Explanation (separator char is a space (' ')):
     * String: ' a b' => return: [],[a],[b]
     * String: ' a "b c" d' => return: [],[a],[b c], [d]
     * => when calling getSplitsNoSpecialStringTreatment with ' d'
     * ignore first match. when calling with ' a b', don't ignore it.
     */
    
    String newLine = ""; String stringColumn="";
    boolean skip1 = false, skip2 = false;
    for(char c: input.toCharArray()){
      if (c=='"' && skipMatchesInStrings) {
        skip1 = !skip1;
        stringColumn+=c;
        continue;
      }
      //if (c=='\'') skip2 = !skip2;//Complicated because of terms like "it's"
      
      if(!skip1 && !skip2) {
        if (stringColumn.length()>0) {
          splits.add(stringColumn);
          stringColumn="";
        }
        newLine+=c;
      } else {
        stringColumn+=c;
        if (newLine.length()>0) {
          // Add all except the last hit (because: 'a "b"' => newLine is here 'a ' and would result in [a] [].
          splits.addAll(getSplitsNoSpecialStringTreatment(newLine, separator, skipConsecutiveMatches, true, (splits.size()>0) ));        
          newLine = "";
        }
      }
    }
    
    // Remaining string (or full string, if no internal strings)
    if (newLine.length()>0) {
      splits.addAll(getSplitsNoSpecialStringTreatment(newLine, separator, skipConsecutiveMatches, false, (splits.size()>0)));
    }
    
    return splits.toArray(new String[0]);
  }
  
  private static ArrayList<String> getSplitsNoSpecialStringTreatment(String input, Pattern separator, boolean skipConsecutiveMatches, boolean skipLastMatch, boolean skipFirstMatch) {
    Matcher m = separator.matcher(input);
    
    // Count matches, eventually ignore consecutive ones.
    ArrayList<String> splits = new ArrayList<String>();
    int lastEnd = (skipFirstMatch?0:-1);
    while (m.find()) {
      if (!skipConsecutiveMatches) {
        splits.add(input.substring(Math.max(0, lastEnd), m.start()));
      }
      else {
        if (m.start()!=lastEnd) {
          splits.add(input.substring(Math.max(0, lastEnd), m.start()));
        }
      }
      lastEnd = m.end();
    }
    // Don't forget the last match.
    // Removing the last match makes sense, because when this function
    // is called, the last character is usually a separator char.
    if (!skipLastMatch) {
      splits.add(input.substring(Math.max(0, lastEnd), input.length()));
    }
    
    return splits;
  }
  
  public Object clone() throws CloneNotSupportedException {
    CSVReader clone=(CSVReader)super.clone();

    return clone;
  }
  
  // Nicht ganz korrekt da auch 4.34-5,2.1 als nummer erkannt wird, aber das reicht mir so.
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
  public static boolean isIntegerNumber(String s) {
    if (s==null || s.length()<1) return false;
    try{
      Integer.parseInt(s);
    } catch (Exception e) {return false;}
    return true;
  }
  
  
}
