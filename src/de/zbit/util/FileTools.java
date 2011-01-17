/**
 *
 * @author wrzodek
 */
package de.zbit.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 * This class acts just like the linux or unix which command.
 * @author wrzodek
 */
public class FileTools {
  
  /**
   * Just for testing.
   */
  public static void main(String[] args) {
    System.out.println(which("pdflatex"));
  }
  
  /**
   * Acts just like the linux or unix which command.
   * 
   * Searches for an executable of the filename on the system path variable
   * and in the current directory.
   * 
   * @param filename
   * @return the executable file if found, or null if not.
   */
  public static File which(String filename) {
    
    // Get paths from envivronment variable
    String path = System.getenv("PATH");
    String[] paths = path.split(Pattern.quote(File.pathSeparator));
    
    // Append the current working directory
    String[] morePaths = new String[paths.length+1];
    morePaths[0] = System.getProperty("user.dir");
    System.arraycopy(paths, 0, morePaths, 1, paths.length);
    
    // Search for the file
    return searchExecutableFile(morePaths, filename);
  }


  /**
   * Searches the given paths for an executable file, with the given name.
   * 
   * Will look for an executable extension on windows systems and look
   * if the file is executable on unix systems.
   * 
   * @param morePaths
   * @param filename - without extension (on windows)
   * @return null if not found, else the absolute filename.
   */
  public static File searchExecutableFile(String[] paths, String filename) {
    boolean isWindows = (System.getProperty("os.name").toLowerCase().contains("windows"));
    if (isWindows) filename = filename.toLowerCase();
      
    for (String path: paths) {
      String[] files = new File(path).list();
      for (String file:files) {
        File fullFile = new File(Utils.ensureSlash(path) + file);
        if (fullFile.isDirectory()) continue;
        
        /*
         * On Windows: compare names and look if it has an executable extension
         */
        if (isWindows) {
          file = file.toLowerCase();
          if (file.endsWith(".com") ||  file.endsWith(".exe") || file.endsWith(".bat")) {
            if (file.startsWith(filename) && file.length() == filename.length()+4) {
              return fullFile;
            }
          }
        } else {
          /*
           * On Linux / Mac / other, don't add an extension, simply look if it is executable.
           */
          if (file.equals(filename) && fullFile.canExecute()) return fullFile;
        }
        
        
      }
      
    }
    
    return null;
  }
  
}
