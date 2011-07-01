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
package de.zbit.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import de.zbit.util.FileDownload;
import de.zbit.util.Reflect;
import de.zbit.util.SortedArrayList;

/*
 * TODO:
 * Implement retrieve file from other jar (hast not been used/implemented yet).
 * Should be 5 minutes max: http://www.exampledepot.com/egs/java.net/JarUrl.html
 */


/**
 * Class to Handle file inputs. No matter where (URL, FileSystem, Internet,... ) or what (raw,txt,gz,tar.gz,zip,...) they are.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class OpenFile {
  /**
   * A cache that provides already downloaded files and saves the user from having to readownload them.
   */
  private static SortedArrayList<String[]> downloadedFiles = new SortedArrayList<String[]>();
  
  /**
   * Contains <code>System.getProperty("user.dir")</code>
   */
  public final static String curDir;;
  
  /**
   * Initializes the <code>curDir</code> variable.
   */
  static {
    String cd = System.getProperty("user.dir");
    if (!cd.endsWith(File.separator)) cd+=File.separator;
    curDir = cd;
  }

  
  /**
   * 
   * @param URL
   * @return
   */
  public static String doDownload(String URL) {
    return doDownload(URL, null);
  }
  
  /**
   * ACHTUNG: Delteted on exit!!
   * @param URL
   * @param toLocalFile
   * @return
   */
  public static String doDownload(String URL, String toLocalFile) {
    int pos = downloadedFiles.indexOf(URL);
    if (pos>=0) {
      // Cache. Aber dateien koennen ja auch nachtraeglich geloescht werden => double check.
      if (new File(downloadedFiles.get(pos)[1]).exists())
        return downloadedFiles.get(pos)[1];
      else
        downloadedFiles.remove(pos);
    }
    
    String filename = URL;
    FileDownload.StatusLabel=null; FileDownload.ProgressBar=null;
    if (toLocalFile==null || toLocalFile.length()==0)
      filename=FileDownload.download(URL);
    else
      filename=FileDownload.download(URL, toLocalFile);
    
    if (new File(filename).exists()) {
      downloadedFiles.add(new String[]{URL, filename});
      new File(filename).deleteOnExit(); // DOWNLOADED FILE WIRD ON EXIT DELETED!!
    }
    
    //System.out.println(URL + " \tFilesize: " +  new File(filename).length()/1024.0/1024.0);
    return filename;
  }
  
  /**
   * Tries to open the file myFile and return a format description object.
   * filename is needed for jar internal reference if file is not in Filesystem (myFile=null).
   * @param filename
   * @param myFile
   * @return FormatDescription object
   */
  @SuppressWarnings("unused")
  private static FormatDescription fetchDescription(String filename, File myFile) {
    FormatDescription desc = null;
    if (myFile!=null) desc = FormatIdentification.identify(myFile);
    else {
      try {
        InputStream in = searchFileAndGetInputStream(filename);
        if (in!=null) {
          desc = FormatIdentification.identify(new BufferedReader(new InputStreamReader(in)));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return desc;
  }
  
  
  /**
   * 
   * @param myStream
   * @return
   */
  private static FormatDescription fetchDescription(InputStream myStream) {
    FormatDescription desc=null;
    try {
      if (myStream!=null)
        desc = FormatIdentification.identify(new BufferedReader(new InputStreamReader(myStream)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return desc;
  }
  
  /**
   * Returns a readble, standard ASCII BufferedReader Object. No Matter the "filename" is an URL,
   * FileSystem Adress, inside external jar or inside internal jar Adress.
   * ZIP, GZ, TAR, BZ2, TAR.GZ and TAR.BZ2 will be automatically "decrypted" (not extracted fully,
   * instead (what's much better) you're getting a stream with clearly readable (extracted) ACII content).
   * @param filename
   * @return BufferedReader
   */
  public static BufferedReader openFile(String filename) {
    return openFile(filename, Reflect.getParentClass());
  }
  
  /**
   * @param searchInputRelativeToResource - allows giving file names, relative to other
   * packages inside a jar.
   * @see #openFile(String)
   * @return
   */
  public static BufferedReader openFile(String filename, Class<?> searchInputRelativeToResource) {
    BufferedReader ret=null;
    
    // Ensure consistent behavious with other methods in this class
    if (searchInputRelativeToResource==null) searchInputRelativeToResource = Reflect.getParentClass();
    
    // Trivial checks
    if (filename==null || filename.trim().length()==0) return ret;
    filename = filename.trim();
    
    // Try to download file if it's an URL
    if (filename.length()>5 && filename.substring(0, 5).equalsIgnoreCase("http:")) {
      filename = doDownload(filename);
    } else if (filename.length()>4 && filename.substring(0, 4).equalsIgnoreCase("ftp:")) {
      filename = doDownload(filename);
    }
    
    // Identify format...
    InputStream myStream=null;
    try {
      myStream = searchFileAndGetInputStream(filename, searchInputRelativeToResource);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    FormatDescription desc = null;
    desc = fetchDescription(myStream);
    
    // 2nd try. Bugfixing accidently added slashes (not so seldomly...)
    if (desc==null) {
      // remove accidently added double slashes. Do NOT do this before checking if it's an URL
      // May lead to problems on non http urls (jar urls e.g. "jar:http://xyz.de/my.jar!/com/...")
      filename = filename.replace(File.separator+File.separator, File.separator).replace("//", "/");
      try {
        myStream = searchFileAndGetInputStream(filename, searchInputRelativeToResource);
      } catch (IOException e) {
        e.printStackTrace();
      }
      desc = fetchDescription(myStream);
    }
    //System.out.println(filename + " => " + (desc==null?"null":desc.getShortName()));
    
    
    //...  and return Input Stream
    try {
      ZIPUtils.parentClass=searchInputRelativeToResource;
      if (desc!=null && desc.getShortName().equalsIgnoreCase("GZ") || desc==null && filename.toLowerCase().trim().endsWith(".gz")) {
        // Gzipped files do sadly not always contain the "magic bytes". That's why also the extension is considered if desc=null.
        ret = ZIPUtils.GUnzipStream(searchFileAndGetInputStream(filename, searchInputRelativeToResource));
        FormatDescription desc2 = FormatIdentification.identify( ret );
        if (desc2!=null) { // Tar.GZ Archives
          if (desc2.getShortName().equalsIgnoreCase("TAR")){ // Extract GZ completely and return tar stream.
            ret.close();
            ret = ZIPUtils.TARunCompressStream(new ByteArrayInputStream(ZIPUtils.GUnzipData(filename).toByteArray()));
          }
        }
      } else if (desc!=null && desc.getShortName().equalsIgnoreCase("ZIP") ) {
        ret = ZIPUtils.ZIPunCompressStream(filename);
      } else if (desc!=null && desc.getShortName().equalsIgnoreCase("BZ2") ) {
        ret = ZIPUtils.BZ2unCompressStream(filename);
        FormatDescription desc2 = FormatIdentification.identify( ret );
        if (desc2!=null) { // Tar.BZ Archives
          if (desc2.getShortName().equalsIgnoreCase("TAR")) {
            ret.close();
            ret = ZIPUtils.TARunCompressStream(new ByteArrayInputStream(ZIPUtils.BZ2unCompressData(filename).toByteArray()));
          }
        }
      } else if (desc!=null && desc.getShortName().equalsIgnoreCase("TAR") ) {
        ret = ZIPUtils.TARunCompressStream(filename);
      }
      
      // Native text file OR ret is not ready if file wasn't really a zip file.
      if (ret==null || !ret.ready()) {
        if (myStream!=null) ret = new BufferedReader(new InputStreamReader(searchFileAndGetInputStream(filename)));
      }
    } catch (Exception e) {e.printStackTrace();}
    if (ret==null) System.err.println("Error opening file '" + filename + "'. Probably this file does not exist.");    
    
    return ret;
  }


  /**
   * Searches for the file
   * a) directly tries to open it by name.
   * b) in the same jar / same project
   * c) relative to the path inside or outside a jar file of the calling class
   * d) relative to the user dir.
   * 
   * WARNING: a File object can not be built upon a resource inside a jar. Use the
   * {@link #searchFileAndGetInputStream(String)} method instead.
   * 
   * @param infile
   * @return the actual file object or null if it does not exist / could not be found.
   * @throws URISyntaxException  - if the resource is inside a jar-file.
   */
  public static File searchFile(String infile) throws URISyntaxException {
    Class<?> parentClass = Reflect.getParentClass();
    
    if (new File (infile).exists()) { // Load from Filesystem
      return new File (infile);
    } else if (OpenFile.class.getClassLoader().getResource(infile)!=null) { // Load from jar
      return new File(OpenFile.class.getClassLoader().getResource(infile).toURI());
    } else if (parentClass!=null && parentClass.getResource(infile)!=null) {
      return new File(parentClass.getResource(infile).toURI());
    } else if (new File (curDir+infile).exists()) { // Load from Filesystem, relative to program path
      return new File (curDir+infile);
    }
    
    return null;
  }
  
  /**
   * Searches for the file
   * a) directly tries to open it by name.
   * b) in the same jar / same project
   * c) relative to the user dir.
   * @param infile - the file name and path to search for.
   * @return InputStream of the given file or null, if not found.
   * @throws IOException 
   */
  public static InputStream searchFileAndGetInputStream(String infile) throws IOException {
    return searchFileAndGetInputStream(infile, Reflect.getParentClass());
  }
  
  /**
   * @see #searchFileAndGetInputStream(String)
   * @param infile
   * @param class1 use the classLoader from this class and search the infile path relative to the
   * path of this class.
   * @return
   * @throws IOException
   */
  public static InputStream searchFileAndGetInputStream(String infile, Class<?> class1) throws IOException {
    if (new File (infile).exists()) { // Load from Filesystem
      return  new FileInputStream (infile);
    } else if (class1.getClassLoader().getResource(infile)!=null) { // Load from jar - root
      return (class1.getClassLoader().getResource(infile).openStream());
    } else if (class1.getResource(infile)!=null) { // Load from jar - relative
      return (class1.getResource(infile).openStream());
    } else if (new File (curDir+infile).exists()) { // Load from Filesystem, relative to program path
      return new FileInputStream (curDir+infile);
    }
    
    return null;
  }

  /**
   * Reads the given file completely and returns the content as StringBuffer.
   * @param filename
   * @return file content.
   * @throws IOException 
   */
  public static StringBuffer readFile(String filename) throws IOException {
    BufferedReader r = openFile(filename);
    String line;
    StringBuffer ret = new StringBuffer();
    while (r!=null && (line=r.readLine())!=null) {
      ret.append(line);
      ret.append('\n');
    }
    if (r!=null && r instanceof Closeable) r.close();
    
    return ret;
  }
  
}
