package de.zbit.util;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Iterating through a directory (you may include subdirectories).
 * @author wrzodek
 */
public class DirectoryParser implements Iterator<String> {
  private String path = ".";
  private String extension = "";
  private int curPos=0;
  private String[] contents = null;
  
  private boolean recurseIntoSubdirectories = false;
  
  public DirectoryParser(String path, String extension) {
    this(path);
    setExtension(extension);
  }
  public DirectoryParser(String path) {
    this();
    setPath(path);
  }
  public DirectoryParser() {}
  
  
  
  public boolean isRecurseIntoSubdirectories() {
    return recurseIntoSubdirectories;
  }
  public void setRecurseIntoSubdirectories(boolean recurseIntoSubdirectories) {
    if (this.recurseIntoSubdirectories != recurseIntoSubdirectories) {
      this.recurseIntoSubdirectories = recurseIntoSubdirectories;
      reset();
    }
  }
  private String getPath(String path) {
    path = appendSlash(path);
    return path;
  }
  private String appendSlash(String path) {
    if (!path.endsWith("\\") && !path.endsWith("/"))
      if (path.contains("/")) path+="/";
      else if (path.contains("\\")) path+="\\";
      else path+="/";
    return path;
  }
  public String getPath() {
    return getPath(this.path);
  }
  public void setPath(String path) {
    this.path = getPath(path); // Append / or \\
    reset();
  }
  public String getExtension() {
    return extension;
  }
  public void setExtension(String extension) {
    if (extension.contains("*")) extension = extension.replace("*", ""); // Prevent things like "*.dat"
    this.extension = extension;
    reset();
  }
  
  private ArrayList<String> readDir(){
    return readDir(this.path);
  }
  private ArrayList<String> readDir(String path){
    path = appendSlash(path);
    if (!new File(path).isDirectory()) {System.err.println("'" + path + "' is not a directory."); return null;}
    String[] allFiles = new File(path).list();
    
    // Add all files which match extension and recurse into subdirs (appending the right path).
    String pathPrefix = path.replace(this.path, "");
    ArrayList<String> myFiles = new ArrayList<String>();
    for (String file: allFiles) {
      if (recurseIntoSubdirectories && new File(path + file).isDirectory()) {
        myFiles.addAll(readDir(path + file));
      } else {
        if (extension==null || extension.trim().isEmpty()) {
          myFiles.add(pathPrefix + file);
        } else {
          if (file.toLowerCase().endsWith(extension.toLowerCase())) myFiles.add(pathPrefix + file);
        }
      }
    }
    
    // To designated Array.
    if (path.equals(appendSlash(this.path))) {
      contents = new String[myFiles.size()];
      contents = myFiles.toArray(contents);    
      Arrays.sort(contents); // XXX: Sort Alphabetically
    }
    
    return myFiles;
  }
  
  
  public void reset(){
    curPos=0;
    contents=null;
  }
  
  @Override
  public boolean hasNext(){
    if (contents==null) {
      readDir();
      if (contents==null) return false;
    }
    if (curPos>=contents.length) return false;
    return true;
  }
  public boolean hasPrevious(){
    if (contents==null) {
      readDir();
      if (contents==null) return false;
    }
    if (curPos<=0) return false;
    return true;
  }
  public void jumpToStart(){
    curPos=0;
  }
  public void jumpToEnd(){
    if (contents==null) readDir();
    if (contents!=null) curPos=contents.length;
  }
  
  @Override
  public String next(){
    if (curPos<(contents.length)) {
      return contents[curPos++];
    }
    else
      return null;
  }
  public String previous(){
    if (curPos>0) {
      return contents[--curPos];
    }
    else
      return null;    
  }
  public String[] getAll(){
    if (contents==null) readDir();
    if (contents==null) return null;
    return this.contents.clone();
  }
  public int getCount(){
    if (contents==null) readDir();
    if (contents==null) return 0;
    return this.contents.length;
  }
  public boolean contains(String item, boolean caseSensitive) {
    if (contents==null) readDir();
    if (contents==null) return false;
    
    for (String s: this.contents)
      if (caseSensitive?s.equals(item):s.equalsIgnoreCase(item)) return true;
    return false;
  }
  
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    DirectoryParser d = new DirectoryParser("S:\\SVM\\tools", "ft");
    while(d.hasNext())
      System.out.println(d.next());
  }
  
  @Override
  public void remove() {
    System.err.println("Remove not supported.");
  }
  
}
