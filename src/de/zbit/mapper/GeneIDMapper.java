package de.zbit.mapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.CSVReader;
import de.zbit.util.FileDownload;
import de.zbit.util.FileTools;
import de.zbit.util.ProgressBar;

/**
 * This class downloads or loads (if available) mapping data
 * to map RefSeq IDs (e.g., NM_X) to NCBI Gene IDs (Entrez).
 * 
 * @author buechel
 */
public class GeneIDMapper implements Serializable {
  private static final long serialVersionUID = -4951755727304781666L;

  public static final Logger log = Logger.getLogger(GeneIDMapper.class.getName());
  
  /**
   * The Base url to get the directory listing and search for a file "X2geneid.gz"
   */
  private static String downloadBaseURL = "ftp://ftp.ncbi.nih.gov/refseq/release/release-catalog/";
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   */
  private final static String downloadURL = getLatestReleaseMappingFile();
  
  /**
   * The downloaded and cached local mapping file.
   * MUST contain a folder.
   */
  private static String localFile = "res/" + FileTools.getFilename(downloadURL);
  
  /**
   * Contains a mapping from RefSeq to GeneID.
   * XXX: Hier eventuell eine initial Capacity oder load factor angeben, falls BottleNeck.
   */
  Map<String, Integer> mapping = new HashMap<String, Integer>();
  
  
  /**
   * Inintializes the mapper from RefSeq to Gene ids. Downloads and reads the mapping
   * file automatically as required.
   * @throws IOException
   */
  public GeneIDMapper() throws IOException {
    super();
    if (!readMappingData()) mapping=null;
  }
  
  
  public void test() {
    boolean checkLowerVersionExists=true;
    boolean differentVersionsHaveDifferentTargets=true;
    
    
    for (String key: mapping.keySet()) {
      int pos = key.indexOf('.');
      if (pos<0) continue;
      int version = Integer.parseInt(key.substring(pos+1));
      
      if (checkLowerVersionExists && version>1) {
        for (int i=version-1; i>0; i--) {
          String newKey = key.subSequence(0, pos+1) + Integer.toString(i);
          if (mapping.containsKey(newKey)) {
            System.out.println("Lower version exists (z.B. " + key + " => " + newKey);
            checkLowerVersionExists = false;
          }
        }
      }
      
      if (differentVersionsHaveDifferentTargets) {
        for (int i=version-1; i>0; i--) {
          String newKey = key.subSequence(0, pos+1) + Integer.toString(i);
          if (mapping.containsKey(newKey)) {
            
            Integer oldVersionTarget = mapping.get(newKey);
            Integer aktVersionTarget = mapping.get(key);
            
            if ( ((int)oldVersionTarget)!=  ((int)aktVersionTarget) ) {
              
              System.out.println("Different target exists z.B. \n" + key + " => " + aktVersionTarget + "\n" + newKey + " => " + oldVersionTarget);
              differentVersionsHaveDifferentTargets = false;
              
            }
            
          }
        }
      }
      
      if (!differentVersionsHaveDifferentTargets && !checkLowerVersionExists) break;
    }
    
    if (checkLowerVersionExists)
      System.out.println("No Lower version exist in file.");
    if (differentVersionsHaveDifferentTargets)
      System.out.println("No Lower versions have different targerts.");
  }
  
  public static void main (String[] args) throws IOException {
    GeneIDMapper mapper = new GeneIDMapper();
    mapper.test();
    
    
  }
  

  public static String getLatestReleaseMappingFile() {
    OutputStream out = new ByteArrayOutputStream();
    String baseUrl = downloadBaseURL;
    try {
      FileDownload.download(baseUrl, out);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not get RefSeq file listing.", e);
      out = null;
    }
    if (out == null || !out.toString().contains("\n")) {
      log.severe("Could not get mapping file from refSeq Server.");
      System.exit(1);
    }
    
    for (String file: out.toString().split("\n")) {
      // file is e.g. "-r--r--r--   1 ftp      anonymous 146709259 Mar 11 10:14 RefSeq-release46.catalog.gz"
      // Get real file name
      int pos = file.lastIndexOf(' ');
      if (pos>0) file = file.substring(pos);
      file = file.trim();
     
      // Check if it is the mapping file and return it, if true.
      if (file.endsWith("2geneid.gz")) {
        return baseUrl + (baseUrl.endsWith("/")?"":"/") + file;
      }
      
    }
    
    // we could  not find a mapping file on the RegSeq Server (downloadBaseURL) that ends with "2geneid.gz".
    log.severe("Could not find mapping file on RefSeq Server.");
    System.exit(1);
    
    return null;
  }
  
  /**
   * Reads the mapping from {@link #localFile} into the {@link #mapping} set.
   * Downloads data automatically from {@link #downloadURL} as required.
   * @return true if and only if everything was without critical errors.
   * @throws IOException
   */
  public boolean readMappingData() throws IOException {
    if (!isCachedDataAvailable()) {
      downloadData();
      if (!isCachedDataAvailable()) {
        return false;
      }
    }
    log.config("Reading RefSeq2GeneID mapping file " + localFile);
    int geneIDColumn = 1;
    int refSequColumn = 2;
    // Read RefSeq <=> Gene ID mapping.
    CSVReader r = new CSVReader(localFile);
    String[] line;
    while ((line = r.getNextLine())!=null) {
      if (line.length<(Math.max(geneIDColumn, refSequColumn)+1)) {
        log.severe("Incomplete entry in mapping file '" + localFile + "'. Please try to delete this file and execute this application again.");
        continue;
      }
      
      // Get gene ID
      Integer gene_id;
      try {
        gene_id = Integer.parseInt(line[geneIDColumn]);
      } catch (NumberFormatException e) {
        log.warning("Invalid number format in RefSeq2GeneID mapping file: " + ((line.length>1)?line[geneIDColumn]:"line too short."));
        continue;
      }
      
      // get RefSeq ID (trim version number from id)
      String refSeq = trimVersionNumberFromRefSeq(line[refSequColumn]);
      
      mapping.put(refSeq, gene_id);
    }
    
    log.config("Finished parsing RefSeq2GeneID mapping file. Read " + ((mapping!=null)?mapping.size():"0") + " mappings.");
    return (mapping!=null && mapping.size()>0);
  }
  
  /**
   * Removes the version number from a RefSeq Identifier.
   * @param refSeq (e.g. NM_23424.1)
   * @return e.g. NM_23424
   */
  public static String trimVersionNumberFromRefSeq(String refSeq) {
    int pos = refSeq.indexOf('.');
    if (pos>0) refSeq = refSeq.substring(0, pos);
    return refSeq;
  }


  /**
   * Downloads the {@link #downloadURL} and stores the path of the downloaded
   * file in {@link #localFile}.
   * If the download was successfull can be checked with {@link #isCachedDataAvailable()}.
   */
  private void downloadData() {
    FileDownload.ProgressBar = new ProgressBar(0);
    try {
      // Create parent directories
      new File(new File(localFile).getParent()).mkdirs();
    } catch (Throwable t){};
    String localf = FileDownload.download(downloadURL, localFile);
    if (localFile!=null && localFile.length()>0) localFile = localf;
    ((ProgressBar)FileDownload.ProgressBar).finished();
  }


  /**
   * Returns true if the {@link #localFile} has already been downloaded from
   * {@link #downloadURL}.
   * @return
   */
  public boolean isCachedDataAvailable() {
    if (localFile==null || localFile.length()<0) {
      return false;
    }
    
    File f  = new File(localFile);
    return f.exists() && f.length()>0;
  }
  
  /**
   * Returns the GeneID for the given RefSeqID.
   * Returns -1 if no mapping for the RefSeqID is available.
   * @param RefSeqID
   * @return NCBI Gene ID (or -1 if none available).
   * @throws Exception - if mapping data could not be read (in general).
   */
  public int map(String RefSeqID) throws Exception {
    
    if (!isReady()) throw new Exception("RefSeq2GeneID mapping data has not been read successfully.");
    String trimmedInput = trimVersionNumberFromRefSeq(RefSeqID).trim();
    Integer ret = mapping.get(trimmedInput);
    log.finest("map: " + trimmedInput + ", ret: " + ret);
    return ret==null?-1:ret;
  }

  /**
   * @return true if and only if the data has been read and
   * mapping data is available.
   */
  public boolean isReady() {
    return mapping!=null && mapping.size()>0;
  }

}
