package de.zbit.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import de.zbit.util.FileDownload;
import de.zbit.util.SortedArrayList;

public class OpenFile {
  private static SortedArrayList<String[]> downloadedFiles = new SortedArrayList<String[]>();

  public static String doDownload(String URL) {
    return doDownload(URL, null);
  }
  // ACHTUNG: Delteted on exit!!
  public static String doDownload(String URL, String toLocalFile) {
    int pos = downloadedFiles.indexOf(URL);
    if (pos>=0) {
      // Cache. Aber dateien k�nnen ja auch nachtr�glich gel�scht werden => double check.
      if (new File(downloadedFiles.get(pos)[1]).exists())
        return downloadedFiles.get(pos)[1];
      else
        downloadedFiles.remove(pos);
    }
    
    String filename = URL;
    FileDownload.StatusLabel=null; FileDownload.ProgressBar=null;
    if (toLocalFile==null || toLocalFile.isEmpty())
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
   * Entpackt automatich zip oder gzip files.
   * @param filename
   * @return
   */
  public static BufferedReader openFile(String filename) {
    //String c = filename.toLowerCase();
    BufferedReader ret=null;
    
    // Try to download file if it's an URL
    if (filename.length()>5 && filename.substring(0, 5).equalsIgnoreCase("http:")) {
      filename = doDownload(filename);
    } else if (filename.length()>4 && filename.substring(0, 4).equalsIgnoreCase("ftp:")) {
      filename = doDownload(filename);
    }
    filename = filename.replace(File.separator+File.separator, File.separator).replace("//", "/"); // remove accidently added double slashes.
    
    // Identify format...
    File myFile = searchFile(filename);
    FormatDescription desc = null;
    if (myFile==null && (OpenFile.class.getClassLoader().getResource(filename)!=null))
      try {
        desc = FormatIdentification.identify(new BufferedReader(new InputStreamReader(OpenFile.class.getClassLoader().getResource(filename).openStream())));
      } catch (IOException e1) {e1.printStackTrace();}
    else
      if (myFile!=null) desc = FormatIdentification.identify(myFile);
    //System.out.println(filename + " => " + (desc==null?"null":desc.getShortName()));
    
    
    //...  and return Input Stream
    try {
      if (desc!=null && desc.getShortName().equalsIgnoreCase("GZ") ) { //(c.endsWith(".gz")) {
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
      
      if (ret==null || !ret.ready()) { // ret is not ready if file wasn't really a zip file.
        String curDir = System.getProperty("user.dir");
        if (!curDir.endsWith(File.separator)) curDir+=File.separator;
        
        if (new File (filename).exists()) {
          ret = new BufferedReader(new FileReader(filename));
          
        } else if (new File (curDir+filename).exists()) { // Load from Filesystem, relative to program path
          ret = new BufferedReader(new FileReader(curDir+filename));

        } else if (OpenFile.class.getClassLoader().getResource(filename)!=null) {// Load from same jar
          InputStream x = OpenFile.class.getClassLoader().getResource(filename).openStream();
          ret = new BufferedReader(new InputStreamReader(x));
        }
      }
    } catch (Exception e) {e.printStackTrace();}
    if (ret==null) System.err.println("Error opening file '" + filename + "'. Probably this file does not exist.");    
    //if (c.endsWith(".tar") || c.endsWith(".tgz") || c.endsWith(".tar.gz")) System.out.println("Warning: Your input file '" + filename + "' seems to be a TAR archive. TAR archives are not supported!");
    
    return ret;
  }
  
  public static File searchFile(String infile) {
    String curDir = System.getProperty("user.dir");
    if (!curDir.endsWith(File.separator)) curDir+=File.separator;
    
    if (new File (infile).exists()) // Load from Filesystem
      return new File (infile);
    else if (new File (curDir+infile).exists()) // Load from Filesystem, relative to program path
      return new File (curDir+infile);
    else if (OpenFile.class.getClassLoader().getResource(infile)!=null)
      try {
        return new File(OpenFile.class.getClassLoader().getResource(infile).toURI());
      } catch (URISyntaxException e) {}
    
    return null;
  }
  
}
