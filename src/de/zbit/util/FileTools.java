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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager.Location;

import de.zbit.io.OpenFile;

/**
 * This class acts just like the linux or unix which command.
 * @author wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class FileTools {
  
  /**
   * Just for testing.
   */
  public static void main(String[] args) {
    
    try {
//      FileTools.splitFile("H:/ValidationData/proDGe_DROME_raw.txt", "H:/ValidationData", "proDGe_DROME_raw", ".txt", 8000000);     
      FileTools.splitFile("H:/ValidationData/proDGe_RAT_raw.txt", "H:/ValidationData", "proDGe_RAT_raw", ".txt", 8000000);
      FileTools.splitFile("H:/ValidationData/proDGe_MOUSE_raw.txt", "H:/ValidationData", "proDGe_MOUSE_raw_", ".txt", 8000000);
      FileTools.splitFile("H:/ValidationData/proDGe_HUMAN_raw.txt", "H:/ValidationData", "proDGe_HUMAN_raw", ".txt", 8000000);
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    if(true)return;
    
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
    String[] paths = path==null?new String[0]:
       path.split(Pattern.quote(File.pathSeparator));
    
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
      if (files==null) continue; // In webstart sometimes null
      
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

  /**
   * This method divides a file into several smaller files
   * 
   * @param input: file, that should be splitted
   * @param outputFolder: where to save the file
   * @param filename: name of the file, followed by a nummer created during splitting
   * @param fileExtension: ending of the new files 
   * @param lineSplit: no after how many lines a new file should be created
   * @throws IOException 
   */
  public static void splitFile(String input, String outputFolder, String filename, String fileExtension,  int lineSplit) throws IOException{
    int fileNo = 1;
    int lineCounter = 0;
    String line = "";
    BufferedReader br = new BufferedReader(new FileReader(input));
    BufferedWriter bw = new BufferedWriter(new FileWriter(outputFolder + File.separator + filename + 
                                  String.valueOf(fileNo) + fileExtension));
    while((line=br.readLine())!=null){
      bw.append(line + "\n");
      lineCounter++;
      if(lineCounter == lineSplit){
        bw.close();
        fileNo++;
        lineCounter = 0;
        bw = new BufferedWriter(new FileWriter(outputFolder + File.separator + filename + 
            String.valueOf(fileNo) + fileExtension));
      }
    }
        
    bw.close();
    br.close();
  }
  
  /**
   * Trims everything but the file name from a url or path.
   * e.g. "ftp://asf.com/asf/frg.zip" would return "frg.zip".
   * or "c:\asg\fgj" would return "fgj".
   * 
   * @param downloadurl
   * @return
   */
  public static String getFilename(String downloadurl) {
    char sep = File.separatorChar;
    int pos = downloadurl.lastIndexOf(sep);
    if (sep!='/') {
      // In windows, BOTH \\ and / can be used as separators.
      pos = Math.max(downloadurl.lastIndexOf(sep), downloadurl.lastIndexOf('/'));
    }
    if (pos<0) return downloadurl;
    else return downloadurl.substring(pos+1);
  }

  /**
   * Checks wether a input resource (a file in the file system
   * or a resource in the/a jar file) exists and data is available
   * (in case of files, checks if the file size is greater than 0).
   * <p>Note:<br/>
   * This method should be used in combination with {@link OpenFile},
   * because both searches for the input resources at the same locations.
   * </p>
   * @param localFile
   * @return true, if the resource is available.
   */
  public static boolean checkInputResource(String localFile) {
    return checkInputResource(localFile, FileTools.class);
  }
  

  /**
   * Checks wether a input resource (a file in the file system
   * or a resource in the/a jar file) exists and data is available
   * (in case of files, checks if the file size is greater than 0).
   * <p>Note:<br/>
   * This method should be used in combination with {@link OpenFile},
   * because both searches for the input resources at the same locations.
   * </p>
   * @param localFile
   * @param sourcePackage - if the file is inside a jar, searches for the file,
   * relative to the given sourcePackage.
   * @return
   */
  public static boolean checkInputResource(String localFile, Class<?> sourcePackage) {
    if (localFile==null || localFile.length()<1) return false;
    
    File f  = new File(localFile);
    if (f.exists()) {
      return (f.length()>0);
    } else if (sourcePackage.getClassLoader().getResource(localFile)!=null) { // Load from jar - root
      return true;
    } else if (sourcePackage.getResource(localFile)!=null) { // Load from jar - relative
      return true;
    } else { // Load from Filesystem, relative to program path
      String curDir = System.getProperty("user.dir");
      if (!curDir.endsWith(File.separator)) curDir+=File.separator;
      f = new File (curDir+localFile);
      if (f.exists() && (f.length()>0)){
        return true;
      }
    }
    
    return false;
  }
  
}
