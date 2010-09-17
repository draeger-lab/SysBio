package de.zbit.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import de.zbit.util.FileDownload;
import de.zbit.util.SortedArrayList;

/*
 * TODO:
 * Implement retrieve fiel from other jar (hast not been used/implemented yet).
 * Should be 5 minutes max: http://www.exampledepot.com/egs/java.net/JarUrl.html
 */


/**
 * Class to Handle file inputs. No matter where (URL, FileSystem, Internet,... ) or what (raw,txt,gz,tar.gz,zip,...) they are.
 * @author wrzodek
 */
public class OpenFile {
  /**
   * 
   */
  private static SortedArrayList<String[]> downloadedFiles = new SortedArrayList<String[]>();

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
  private static FormatDescription fetchDescription(String filename, File myFile) {
    FormatDescription desc = null;
    if (myFile==null && (OpenFile.class.getClassLoader().getResource(filename)!=null))
      try {
        desc = FormatIdentification.identify(new BufferedReader(new InputStreamReader(OpenFile.class.getClassLoader().getResource(filename).openStream())));
      } catch (IOException e1) {e1.printStackTrace();}
    else
      if (myFile!=null) desc = FormatIdentification.identify(myFile);
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
    BufferedReader ret=null;
    
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
    File myFile = searchFile(filename);
    FormatDescription desc = null;
    desc = fetchDescription(filename, myFile);
    
    // 2nd try. Bugfixing accidently added slashes (not so seldomly...)
    if (desc==null) {
      // remove accidently added double slashes. Do NOT do this before checking if it's an URL
      // May lead to problems on non http urls (jar urls e.g. "jar:http://xyz.de/my.jar!/com/...")
      filename = filename.replace(File.separator+File.separator, File.separator).replace("//", "/");
      myFile = searchFile(filename);
      desc = fetchDescription(filename, myFile);
    }
    //System.out.println(filename + " => " + (desc==null?"null":desc.getShortName()));
    
    
    //...  and return Input Stream
    try {
      if (desc!=null && desc.getShortName().equalsIgnoreCase("GZ") || desc==null && filename.toLowerCase().trim().endsWith(".gz")) {
        // Gzipped files do sadly not always contain the "magic bytes". That's why also the extension is considered if desc=null.
        ret = ZIPUtils.GUnzipStream(filename);
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
        if (myFile!=null) ret = new BufferedReader(new FileReader(myFile));
      }
    } catch (Exception e) {e.printStackTrace();}
    if (ret==null) System.err.println("Error opening file '" + filename + "'. Probably this file does not exist.");    
    
    return ret;
  }
  
  /**
   * Searches for the file
   * a) directly tries to open it by name.
   * b) in the same jar / same project
   * c) relative to the user dir.
   * @param infile
   * @return the actual file object or null if it does not exist / could not be found.
   */
  public static File searchFile(String infile) {
    String curDir = System.getProperty("user.dir");
    if (!curDir.endsWith(File.separator)) curDir+=File.separator;
    
    if (new File (infile).exists()) { // Load from Filesystem
      return new File (infile);
    } else if (OpenFile.class.getClassLoader().getResource(infile)!=null) { // Load from jar
      try {
        return new File(OpenFile.class.getClassLoader().getResource(infile).toURI());
      } catch (URISyntaxException e) {}
    } else if (new File (curDir+infile).exists()) { // Load from Filesystem, relative to program path
      return new File (curDir+infile);
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
