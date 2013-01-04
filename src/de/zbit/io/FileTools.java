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
package de.zbit.io;



import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import de.zbit.util.Utils;

/**
 * This class contains various {@link File}-related tools and utils.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class FileTools {
  
  /**
   * Checks whether a input resource (a file in the file system
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
   * Checks whether a input resource (a file in the file system
   * or a resource in the/a jar file) exists and data is available
   * (in case of files, checks if the file size is greater than 0).
   * <p>Note:<br/>
   * This method should be used in combination with {@link OpenFile},
   * because both searches for the input resources at the same locations.
   * </p>
   * @param localFile
   * @param sourcePackage - if the file is inside a jar, searches for the file,
   * relative to the given sourcePackage.
   * @return true if and only if the file or URL denoted by <code>localFile</code>
   * is available and contains data.
   */
  public static boolean checkInputResource(String localFile, Class<?> sourcePackage) {
    if (localFile==null || localFile.length()<1) return false;
    
    File f  = new File(localFile);
    if (f.exists() && (f.length()>0) && f.canRead() && f.isFile()) {
      return true;
    } else if (sourcePackage.getClassLoader().getResource(localFile)!=null) { // Load from jar - root
      return true;
    } else if (sourcePackage.getResource(localFile)!=null) { // Load from jar - relative
      return true;
    } else { // Load from Filesystem, relative to program path
      String curDir = System.getProperty("user.dir");
      if (!curDir.endsWith(File.separator)) curDir+=File.separator;
      f = new File (curDir+localFile);
      if (f.exists() && (f.length()>0) && f.canRead() && f.isFile()){
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * @param name any filename
   * @return file extension of <code>name</code>
   */
  public static String getExtension(String name) {
    if ((name != null) && (name.length() > 0)) {
      int pos = name.lastIndexOf('.');
      if (pos >= 0) {
        return name.substring(pos + 1);
      }
    }
    return "";
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
    if (sep != '/') {
      // In windows, BOTH \\ and / can be used as separators.
      pos = Math.max(downloadurl.lastIndexOf(sep), downloadurl.lastIndexOf('/'));
    }
    if (pos < 0) {
    	return downloadurl;
    } else {
    	return downloadurl.substring(pos + 1);
    }
  }
  
  /**
   * If the input has a file extension, it is removed. else, the input is
   * returned.
   * 
   * @param fileName
   * @return
   */
  public static String removeFileExtension(String fileName) {
    int pos = fileName.lastIndexOf('.');
    return (pos > 0) ? fileName.substring(0, pos) : fileName;
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
    if (isWindows) {
      filename = filename.toLowerCase();
    }
      
    for (String path: paths) {
      String[] files = new File(path).list();
      if (files == null) {
        continue; // In webstart sometimes null
      }
      
      for (String file : files) {
        File fullFile = new File(Utils.ensureSlash(path) + file);
        if (fullFile.isDirectory()) {
          continue;
        }
        
        /*
         * On Windows: compare names and look if it has an executable extension
         */
        if (isWindows) {
          file = file.toLowerCase();
          if (file.endsWith(".com") ||  file.endsWith(".exe") || file.endsWith(".bat")) {
            if (file.startsWith(filename)
                && (file.length() == filename.length() + 4)) {
              return fullFile;
            }
          }
        } else {
          /*
           * On Linux / Mac / other, don't add an extension, simply look if it is executable.
           */
          if (file.equals(filename) && fullFile.canExecute()) {
            return fullFile;
          }
        }
      }
    }
    return null;
  }
  
  /**
   * Shuffles the lines in the file with the given filename. This method first
   * writes the shuffled lines into a temporary file and then renames it back to
   * it original name to prevent data loss.
   * 
   * @param fileName
   * @param rnd
   * @throws IOException
   */
  public static void shuffleFile(String fileName, Random rnd) throws IOException {
    File f = new File(fileName);
    File tmpFile = File.createTempFile(f.getName(), "shuffle.tmp", f.getParentFile().getAbsoluteFile());
    shuffleFile(fileName, tmpFile.getAbsolutePath(), rnd);
    if( !f.delete() ) {
      throw new IOException("Failed to replace the original file with the shuffled one, because it couldn't be deleted");
    }
    if( !tmpFile.renameTo(f) ) {
      throw new IOException("Failed to rename the shuffled file '" + tmpFile.getAbsolutePath() + "' to its original name '" + f.getAbsolutePath() + "'");
    }
  }

  /**
   * Shuffles the lines in the file with the given filename. This method first
   * reads the whole infile into memory, shuffles it, and then writes it again.
   * This means that input and output file may be the same, but this could lead
   * to data loss when overwriting has already started but is then aborted
   * somehow.
   * 
   * @param inFileName
   * @param outFileName
   * @param rnd
   * @throws IOException
   */
  public static void shuffleFile(String inFileName, String outFileName, Random rnd) throws IOException {
    
    // if no random number generator was given, create one
    if( rnd == null ) {
      rnd = new Random();
    }
    
    String line = "";
    List<String> lines = new LinkedList<String>();
    BufferedReader br = new BufferedReader(new FileReader(inFileName));
    
    // read the file
    while( (line = br.readLine()) != null ) {
      lines.add(line);
    }
    br.close();
    
    // shuffle lines
    Collections.shuffle(lines, rnd);
    
    // write the file
    BufferedWriter bw = new BufferedWriter(new FileWriter(outFileName));
    for( String s : lines ) {
      bw.append(s).append('\n');
    }
    bw.close();
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
   * Removes the file extension from any filename.
   * @param name
   * @return name without the last dot and everything behind it.
   */
  public static String trimExtension(String name) {
    if ((name != null) && (name.length() > 0)) {
      int pos = name.lastIndexOf('.');
      if (pos >= 0) {
        return name.substring(0, pos);
      }
    }
    return name;
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
    String[] paths = path == null ? new String[0]:
       path.split(Pattern.quote(File.pathSeparator));
    
    // Append the current working directory
    String[] morePaths = new String[paths.length+1];
    morePaths[0] = System.getProperty("user.dir");
    System.arraycopy(paths, 0, morePaths, 1, paths.length);
    
    // Search for the file
    return searchExecutableFile(morePaths, filename);
  }

  private static void write(Object toWrite, Appendable out) throws IOException {
    if (toWrite.getClass().isArray()) {
      for (int i=0; i<Array.getLength(toWrite); i++) {
        if (i>0) out.append('\t');
        Object o2 = Array.get(toWrite, i);
        out.append(o2.toString());
      }
    } else {
      out.append(toWrite.toString());
    }
  }
  
  /**
   * Writes an Array or a single String to a file.
   * @param toWrite - may be a one or 2D array, or a simple string.
   * @param filename - outFile to write.
   * @throws IOException 
   */
  public static void write(Object toWrite, String filename) throws IOException {
    Appendable out = new BufferedWriter(new FileWriter(filename));
    if (toWrite.getClass().isArray()) {
      for (int i=0; i<Array.getLength(toWrite); i++) {
        if (i>0) out.append('\n');
        Object o2 = Array.get(toWrite, i);
        write(o2, out);
      }
    } else {
      write(toWrite, out);
    }
    if (out instanceof Closeable)((Closeable)out).close();
  }

  /**
   * Copies a file. Does NOT check if out already exists. Will overwrite out if it already exists.
   * @param in
   * @param out
   * @return success.
   */
  public static boolean copyFile(File in, File out) {
    if (!in.exists()) {System.err.println("File '" + in.getName() + "' does not exist."); return false;}
    boolean success=false;
    try {
      FileChannel inChannel = new FileInputStream(in).getChannel();
      FileChannel outChannel = new FileOutputStream(out).getChannel();
      // magic number for Windows, 64Mb - 32Kb)
      int maxCount = (64 * 1024 * 1024) - (32 * 1024);
      long size = inChannel.size();
      long position = 0;
      while (position < size) {
        position += inChannel.transferTo(position, maxCount, outChannel);
      }
      if (inChannel != null) inChannel.close();
      if (outChannel != null) outChannel.close();
      if (in.length()==out.length()) success=true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return success;
  }

  /**
   * Copy a stream to a file. This enables e.g. copying of
   * resources inside jar-files to files.
   * 
   * Note: It's a good idea to buffer the input stream.
   * 
   * @param is - resource to read
   * @param f - file to write
   * @return true, if everything went fine.
   */
  public static void copyStream(final InputStream is, final File f) throws IOException {
    
      OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
      // Copy from input to output-stream.
      byte[] buffer = new byte[4096];
      int length;
      while ((length = is.read(buffer)) > 0) {
          os.write(buffer, 0, length);
      }
      os.close();
      is.close();
  
  }

  /**
   * Returns the relative path, in which this file is contained.
   * E.g. if fn is "res/home.dat", the return value will be "res/".
   * 
   * It is NOT recommended to use "new File(X).getParent()" because
   * the path can be inside a jar and no real path.
   * 
   * @param fn - any input file and path combination.
   * @return
   */
  public static String getPath(String fn) {
    if (fn.contains("/")) {
      // Exclisive file separator char, can be used in any os.
      return fn.substring(0, fn.lastIndexOf("/")+1);
    } else if (System.getProperty("file.separator").equals("\\") &&
        fn.contains("\\")) {
      // Unix filesystems can use \ to escape e.g. a whitespace. So
      // this one should only be parsed when it is a valud file separator
      return fn.substring(0, fn.lastIndexOf("\\")+1);
    } else {
      return "";
    }
  }
  
  public static InputStream bufferedReader2InputStream(final BufferedReader r) {
    return new InputStream() {
      
      
      /* (non-Javadoc)
       * @see java.io.InputStream#close()
       */
      @Override
      public void close() throws IOException {
        super.close();
        r.close();
      }
      
      /* (non-Javadoc)
       * @see java.io.InputStream#mark(int)
       */
      @Override
      public synchronized void mark(int readlimit) {
        //super.mark(readlimit);
        try {
          r.mark(readlimit);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      
      /* (non-Javadoc)
       * @see java.io.InputStream#markSupported()
       */
      @Override
      public boolean markSupported() {
        return r.markSupported();
      }
      
      /* (non-Javadoc)
       * @see java.io.InputStream#read(byte[])
       */
      @Override
      public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
      }
      
      /* (non-Javadoc)
       * @see java.io.InputStream#read(byte[], int, int)
       */
      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        char[] bToC = new char[b.length];
        int ret = r.read(bToC, off, len);
        for (int i=off; i<ret; i++) {
          b[i] = (byte) bToC[i];
        }
        return ret;
      }
      
      /* (non-Javadoc)
       * @see java.io.InputStream#reset()
       */
      @Override
      public synchronized void reset() throws IOException {
        r.reset();
      }
      
      /* (non-Javadoc)
       * @see java.io.InputStream#skip(long)
       */
      @Override
      public long skip(long n) throws IOException {
        return r.skip(n);
      }
      
      @Override
      public int read() throws IOException {
        return r.read();
      }
    };
  }
  
}
