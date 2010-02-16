package filetype;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/*
 * Command line program to download data from URLs and save
 * it to local files. Run like this:
 * java FileDownload http://schmidt.devlib.org/java/file-download.html
 * @author Marco Schmidt
 */
public class FileDownload {
  public static Object ProgressBar;
  public static Object StatusLabel;
  public static void setProgressBar(Object o) {
    ProgressBar = o;
  }
  public static void setStatusLabel(Object o) {
    StatusLabel = o;
  }
  
  public static String download(String address, String localFileName) {
    OutputStream out = null;

    try {
      out = new BufferedOutputStream(new FileOutputStream(localFileName));
    } catch (Throwable t) {
      final String tempDir = System.getProperty("java.io.tmpdir");
      localFileName = tempDir + (tempDir.endsWith(File.separator)?"":File.separator) + localFileName;
      try {
        out = new BufferedOutputStream(new FileOutputStream(localFileName));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    
    download(address, out);
    
    return localFileName;
  }
  
  public static void download(String address, OutputStream out) {
    if (out==null) return;
    URLConnection conn = null;
    InputStream  in = null;
    String statusLabelText = "Downloading '" + address + "' ";
    System.out.println(statusLabelText);
    
    int retry = 0;
    while (true) {
      try {
        URL url = new URL(address);
        int status = 0;
        if (address.toLowerCase().startsWith("http:"))
          status = ((HttpURLConnection) url.openConnection()).getResponseCode();
        else //if (address.toLowerCase().startsWith("ftp:"))
          url.openConnection(); //status = ((URLConnection) url.openConnection()).getResponseCode();
        
        if (status>=400) {
          System.out.println("Failed: HTTP error (code " + status + ").");
          
          break; //404 und sowas ... >400 nur error codes. Normal:200 =>OK
        }
        
        conn = url.openConnection();
        
        //conn.getContentLength()
        
        in = conn.getInputStream();
        byte[] buffer = new byte[1024];
        int numRead;
        long numWritten = 0;
        //guiOperations.SetProgressBarMAXThreadlike(Math.max(conn.getContentLength(), in.available()), ProgressBar);
        
        int calls = 0;
        while ((numRead = in.read(buffer)) != -1) {
          if ((calls%10) == 0) { // Alle 10kb
            //if (ProgressBar!=null) guiOperations.SetProgressBarVALUEThreadlike((int)numWritten, true, ProgressBar);
            //double mb = (Math.round(numWritten/1024.0/1024.0*10.0)/10.0);
        	  //if (StatusLabel!=null) guiOperations.SetStatusLabelThreadlike(statusLabelText + "(" + mb + " MB)", StatusLabel);
            //System.out.println(statusLabelText + "(" + mb + " MB)");
            // System.out.println((Math.round(numWritten/1024/1024)));
          }
          
          out.write(buffer, 0, numRead);
          numWritten += numRead;
          calls++;
        }
        //System.out.println(address + " \t " + calls  + " \t " + (numWritten/1024.0/1024.0));
        
        break;
      } catch (Exception exception) {
        exception.printStackTrace();
        if (retry >=2) {
          exception.printStackTrace();
          break;
        }
        retry++;
        
      } finally {
        try {
          if (in != null) in.close();
          if (out != null) out.close();
          if (conn != null) conn = null;
        } catch (IOException ioe) {}
        if (retry >=2) break;
      }
    }
    return;
  }

  public static boolean isHTMLcontent(String address) {
    URLConnection conn = null;
    InputStream  in = null;
    
    int retry = 0;
    boolean ret = false;
    while (true) {
      if (ret) break;
      try {
        URL url = new URL(address);
        int status = 0;
        if (address.toLowerCase().startsWith("http:"))
          status = ((HttpURLConnection) url.openConnection()).getResponseCode();
        else //if (address.toLowerCase().startsWith("ftp:"))
          url.openConnection(); //status = ((URLConnection) url.openConnection()).getResponseCode();
        
        if (status>=400) {
          System.out.println("Failed: HTTP error (code " + status + ").");          
          break; //404 und sowas ... >400 nur error codes. Normal:200 =>OK
        }
        conn = url.openConnection();
        
        in = conn.getInputStream();
        
        byte[] buffer = new byte[1024];
        int reads = 0;
        while ((in.read(buffer)) != -1) {
          String read = new String(buffer).trim();
          //if (read.contains("HTML") && read.contains(">") && read.contains("<")) return true;
          if (read.toUpperCase().replace(" ", "").contains("<HTML>")) {ret=true; break;}
          if ((reads++) >3 ) break; // Nur "peeken" nicht komplette datei lesen.
        }
        
        break;
      } catch (Exception exception) {
        if (retry >=2) {
          exception.printStackTrace();
          break;
        }
        retry++;
        
      } finally {
        try {
          if (in != null) in.close();
          if (conn != null) conn = null;
        } catch (IOException ioe) {}
        if (retry >=2 || ret) break;
      }
    }
    
    return ret;
  }
  
  public static String download(String address) {
    int lastSlashIndex = address.lastIndexOf('/');
    String localFile=null;
    if (lastSlashIndex >= 0 &&
        lastSlashIndex < address.length() - 1) {
      localFile = address.substring(lastSlashIndex + 1);
      if (localFile.contains("?") || localFile.contains("=")) localFile = "temp.tmp";
      localFile = download(address, localFile);
    } else {
      localFile = "Temp.tmp";
      System.err.println("Could not figure out local file name for " + address + ". Calling it '" + localFile + "'.");
      localFile = download(address, localFile);
    }
    return localFile;
  }

  public static void main(String[] args) {
    System.out.println(isHTMLcontent("http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=GSE9786"));
    System.out.println(isHTMLcontent("http://rsat.ulb.ac.be/rsat/data/genomes/Arabidopsis_thaliana/oligo-frequencies/5nt_upstream-noorf_Arabidopsis_thaliana-ovlp-2str.freq.gz"));
    if (true) return;
    
    
    download("http://rsat.ulb.ac.be/rsat/data/genomes/Arabidopsis_thaliana/oligo-frequencies/5nt_upstream-noorf_Arabidopsis_thaliana-ovlp-2str.freq.gz");

    for (int i = 0; i < args.length; i++) {
      download(args[i]);
    }
  }
  
}

