/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.mapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import de.zbit.io.FileDownload;
import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.ArrayUtils;
import de.zbit.util.Timer;
import de.zbit.util.prefs.Option;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * An abstract mapper that can download a csv file or read an
 * supplied / already downloaded file and build an internal map
 * from one column to another.
 * Afterwards, one sourceIdentifier can be mapped to the
 * corresponding targetIdentifier.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public abstract class AbstractMapper<SourceType, TargetType> implements Serializable, Mapper<SourceType, TargetType> {
  private static final long serialVersionUID = -1940567043534334136L;
  public static final Logger log = Logger.getLogger(AbstractMapper.class.getName());
  
  private Class<TargetType> targetType;
  private Class<SourceType> sourceType;
  
  /**
   * A progress Bar that is used while downloading a File.
   * If null, no progress will be displayed.
   */
  protected AbstractProgressBar progress=null;
  
  /**
   * A boolean flag, wether {@link #readMappingData()} has been
   * called or not.
   */
  protected boolean isInizialized=false;
  
  /**
   * Sometimes, this method can not download a file to a write
   * protected folder and thus, redirects it to the temp folder.
   * In this case, this variable holds the path to the file in
   * the temp folder.
   * <p>Note: This should be always null, only if there is really
   * nothing to read/write from {@link #getLocalFile()} OR
   * {@link #getLocalFiles()}, this should be set to a temp file!
   */
  private String tempLocalFile = null;
  
  /**
   * Contains a mapping from RefSeq to GeneID.
   * XXX: Hier eventuell eine initial Capacity oder load factor angeben, falls BottleNeck.
   */
  private Map<SourceType, TargetType> mapping = new HashMap<SourceType, TargetType>();
  

  /**
   * Inintializes the mapper. Downloads and reads the mapping
   * file automatically as required.
   * @throws IOException
   */
  public AbstractMapper(Class<SourceType> sourceType, Class<TargetType> targetType) throws IOException {
    this(sourceType,targetType,null);
  }
  
  /**
   * Inintializes the mapper. Downloads and reads the mapping
   * file automatically as required.
   * @see AbstractMapper#AbstractMapper(Class, Class)
   */
  public AbstractMapper(Class<SourceType> sourceType, Class<TargetType> targetType, AbstractProgressBar progress) throws IOException {
    super();
    this.progress = progress;
    this.sourceType=sourceType;
    this.targetType=targetType;
  }
  
  
  /**
   * Returns the HTTP URL of the latest mapping file.
   * @return
   */
  public abstract String getRemoteURL();
  
  /**
   * Returns the local file name where the downloaded file should be saved to.
   * In most cases, this should be simply:
   * <pre> return "res/" + FileTools.getFilename(getRemoteURL()); </pre>
   * @return
   */
  public abstract String getLocalFile();
  
  /**
   * This may be overwritten instead of {@link #getLocalFile()}
   * to read encrypted files. This method will be preferred if it
   * does not return null;
   * @return
   */
  public String getEncryptedLocalFile(){
  	return null;
  }
  
  /**
   * This may be overwritten instead of {@link #getLocalFile()}.
   * This method is preferred if it does not return null.
   * 
   * For eventual downloads, the return value of {@link #getLocalFile()}
   * is still used!
   * @return
   */
  public String[] getLocalFiles() {
    return null;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.Mapper#getMappingName()
   */
  public abstract String getMappingName();
  
  /**
   * @param r - the CSVReader can OPTIONALLY be used to infere the column number.
   * @return the target column (e.g. 2)
   */
  public abstract int getTargetColumn(CSVReader r);
  /**
   * @param r - the CSVReader can OPTIONALLY be used to infere the column number.
   * @return the source column (e.g. 1)
   */
  public abstract int getSourceColumn(CSVReader r);
  
  /**
   * This may be overwritten instead of {@link #getSourceColumn(CSVReader)}.
   * This method is preferred if it does not return null.
   * @param r
   * @return
   */
  public int[] getMultiSourceColumn(CSVReader r) {
    return null;
  }
  

  
  /**
   * Returns true if the {@link #localFile} has already been downloaded from
   * {@link #downloadURL}.
   * @return
   */
  public boolean isCachedDataAvailable() {
    // Check multi files
    if (getLocalFiles()!=null) {
      for (String localFile: getLocalFiles()) {
        if (localFile!=null && localFile.length()>0) {
          if (FileTools.checkInputResource(localFile, this.getClass())) return true;
        }
      }
    }
    
    // Check encrypted single file
    if (FileTools.checkInputResource(getEncryptedLocalFile(), this.getClass())) {
      return true;
    }
    
    // Check single file
    if (FileTools.checkInputResource(getLocalFile(), this.getClass())) {
      return true;
    }
    
    // Check the fallback temp file
    if (tempLocalFile!=null && FileTools.checkInputResource(tempLocalFile, this.getClass())) {
      return true;
    }
    
    return false;
  }
  
  /**
   * Downloads the {@link #downloadURL} and stores the path of the downloaded
   * file in {@link #localFile}.
   * If the download was successful can be checked with {@link #isCachedDataAvailable()}.
   * <p>Please note that sometimes the desired {@link #getLocalFile()} is write
   * protected. In this case, the method downloads the desired file to a temporary
   * folder. This is why this method returns the path of the downloaded file. It
   * it a good idea to eventually replace the local file with the string returned
   * by this method.
   * @return downloaded local file path and name.
   */
  private String downloadData() {
    String localFile = getLocalFile();
    if (localFile==null) {
      if (getLocalFiles()!=null && getLocalFiles().length>0 && getLocalFiles()[0]!=null) {
        localFile = getLocalFiles()[0];
      }
    }
    FileDownload.ProgressBar = progress;
    try {
      // Create parent directories
      new File(new File(localFile).getParent()).mkdirs();
    } catch (Throwable t) {};
    String localf = FileDownload.download(getRemoteURL(), localFile);
    localFile = localf;
    if (FileDownload.ProgressBar!=null) {
      ((AbstractProgressBar)FileDownload.ProgressBar).finished();
    }
    return localFile;
  }

  
  /**
   * Reads the mapping from {@link #localFile} into the {@link #mapping} set.
   * Downloads data automatically from {@link #downloadURL} as required.
   * @return true if and only if everything was without critical errors.
   * @throws IOException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public boolean readMappingData() throws IOException {
    isInizialized=true;
    
    // Download input file, if it is not there
    if (!ensureLocalFileIsAvailable()) {
      throw new IOException("Could not download or read required resources.");
    }
    
    boolean useEncryptedFile = getEncryptedLocalFile()!=null;
    String storedLocalFile = useEncryptedFile?getEncryptedLocalFile():getLocalFile();
        
    // Parse all files.
    Timer t = new Timer();
    String[] localFiles = ArrayUtils.merge(getLocalFiles(), storedLocalFile, tempLocalFile);
    for (String localFile: localFiles) {
      if (!FileTools.checkInputResource(localFile, this.getClass())) {
        log.config("Skipping " + getMappingName() + " mapping file " + (localFile==null?"null":localFile));
        continue;
      }
      log.config("Reading " + getMappingName() + " mapping file " + localFile);
      CSVReader r = new CSVReader(localFile);
      r.setUseParentPackageForOpeningFiles(this.getClass());
      r.setDisplayProgress(progress!=null);
      r.setProgressBar(progress);
      r.setIsEncrypted(useEncryptedFile);
      configureReader(r);
      int[] multiSourceColumn = getMultiSourceColumn(r);
      if (multiSourceColumn==null || multiSourceColumn.length<1)
        multiSourceColumn = new int[]{getSourceColumn(r)};
      int targetColumn = getTargetColumn(r);
      
      // Get maximal col number
      int maxColumn = targetColumn;
      for (int sourceColumn: multiSourceColumn)
        if (sourceColumn!=Integer.MAX_VALUE)
          maxColumn = Math.max(maxColumn, sourceColumn);
      
      if (targetColumn<0 || ArrayUtils.indexOf(multiSourceColumn, -1)>=0) {
        log.severe("Could not get columns for '" + localFile + "' mapping file.");
        return false;
      }
      
      // Read Source <=> Target mapping.
      String[] line;
      r.open();
      while ((line = r.getNextLine())!=null) {
        if (line.length<=maxColumn) {
          log.severe("Incomplete entry in mapping file '" + localFile + "'. Please try to delete this file and execute this application again.");
          continue;
        }
        if (skipLine(line)) continue;
        
        
        // Get target ID
        if (line[targetColumn].length()==0) {
          log.finest("Empty target in " + getMappingName() + " mapping file.");
          continue;
        }
        
        TargetType target;
        if (Collection.class.isAssignableFrom(targetType)) {
          // Mapping from x to a collection.
           target = (TargetType) new HashSet(); // Do not store the same target multiple times
           // Remark: This works also for non-Strings, since the type of the collection is
           // erased on runtime. Thus, postProcessTargetID() should re-convert to desired
           // real target type.
          ((Collection)target).add(preProcessTargetID(line[targetColumn]));
        } else {
          target = Option.parseOrCast(targetType, preProcessTargetID(line[targetColumn]));
        }
        if (target==null) {
          log.warning("Invalid target content in " + getMappingName() + " mapping file: " + ((line.length>targetColumn)?line[targetColumn]:"line too short."));
          continue;
        }
        
        // Optional method that allow customization.
        target = postProcessTargetID(target);
        
        // Add mapping for all source columns
        int lastSourceColumn=0;
        for (int sourceColumn: multiSourceColumn) {
          if (sourceColumn<line.length) {
            addToMapping(line[sourceColumn], target);
          } else if (sourceColumn==Integer.MAX_VALUE) {
            // This is interpreted as "Add all cells from the last one to
            // the end of the file as source columns".
            // Don't forget to configureReader() when using this method.
            // CSVReader fails to autodetect properties of tables with
            // variable number of columns!
            for (int col=lastSourceColumn+1;col<line.length; col++) {
              addToMapping(line[col], target);
            }
          }
          lastSourceColumn = sourceColumn;
        }
        
        
      }
    }
    
    log.config("Parsed " + getMappingName() + " mapping file in " + t.getNiceAndReset()+". Read " + ((getMapping()!=null)?getMapping().size():"0") + " mappings.");
    return (getMapping()!=null && getMapping().size()>0);
  }

  /**
   * Add content of a cell from the CSV file and a target to
   * the current mapping
   * @param sourceCell
   * @param target
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void addToMapping(String sourceCell, TargetType target) {
    // Get source ID
    SourceType source = null;
    try {
      source = Option.parseOrCast(sourceType, preProcessSourceID(sourceCell));
    } catch (Throwable e) {}
    if (source==null) {
      log.warning("Invalid source content in " + getMappingName() + " mapping file: " + (sourceCell!=null?sourceCell:"null.") );
      return;
    }
    source = postProcessSourceID(source);
    if (source==null) return;
    
    // Allow multiple target elements in collections
    if (Collection.class.isAssignableFrom(targetType)) {
      Collection c = (Collection) getMapping().get(source);
      if (c!=null) {
        c.addAll((Collection)target);
        return;
      }
    }
    
    getMapping().put(source, target);
  }

  /**
   * Ensure that {@link #isCachedDataAvailable()} returns true, i.e.,
   * {@link #getLocalFile()} is available. This method downloads
   * the {@link #getRemoteURL()} if it is not available.
   * @return true if {@link #isCachedDataAvailable()}
   */
  protected boolean ensureLocalFileIsAvailable() {
    if (!isCachedDataAvailable()) {
      if (getRemoteURL()!=null) {
        tempLocalFile  = downloadData();
      } else {
        log.severe("Mapping file for " + getMappingName() + " not available and no download URL is known.");
      }
      if (!isCachedDataAvailable()) {
        return false;
      }
      
      log.info(String.format("Downloaded required mapping file for %s. Reading this file now...", getMappingName()) );
    }
    return true;
  }

  /**
   * This method can be overwritten to customize
   * the {@link CSVReader}.
   * @param r
   */
  protected void configureReader(CSVReader r) {
    // Intentionally left blank.
  }

  /**
   * Allows to skip certain entries while reading the mapping file.
   * @param line
   * @return
   */
  protected boolean skipLine(String[] line) {
    return false;
  }

  /**
   * Allows to modify source IDs directly after reading from input
   * file and before parsing them into the SourceType.
   * @param string
   * @return
   */
  protected String preProcessSourceID(String string) {
    return string;
  }
  
  /**
   * Allows to modify target IDs directly after reading from input
   * file and before parsing them into the TargetType.
   * @param string
   * @return
   */
  protected String preProcessTargetID(String string) {
    return string;
  }


  /**
   * Optional method that allow customizations.
   * @param target
   * @return
   */
  protected TargetType postProcessTargetID(TargetType target) {
    return target;
  }
  
  /**
   * Optional method that allow customizations.
   * @param source
   * @return
   */
  protected SourceType postProcessSourceID(SourceType source) {
    return source;
  }
  

  /* (non-Javadoc)
   * @see de.zbit.mapper.Mapper#map(SourceType)
   */
  public TargetType map(SourceType sourceID) throws Exception {
    if (!isInizialized) init();
    if (!isReady()) throw new Exception(getMappingName()+" mapping data has not been read successfully.");
    SourceType trimmedInput = postProcessSourceID(sourceID);
    TargetType ret = getMapping().get(trimmedInput);
    // Because log message is created, even if nobody listens to it, dont't
    // waste resources in this often called method by building a string!
    //log.finest("map: " + trimmedInput + ", to: " + ret);
    return ret;
  }

  protected void init() throws IOException {
    if (!readMappingData()) mapping=null;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.Mapper#isReady()
   */
  public boolean isReady() {
    return getMapping()!=null && getMapping().size()>0;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.Mapper#size()
   */
  public int size() {
    return getMapping().size();
  }
  
  
  /**
   * This will CLEAR the current {@link #mapping} and take all
   * entries from the given map and add them with keys and
   * values reversed.
   * @param map existing map that should be reversed.
   */
  protected void reverse(AbstractMapper<TargetType, SourceType> map) {
    getMapping().clear();
    
    for(Map.Entry<TargetType, SourceType> entry : map.getMapping().entrySet())
        getMapping().put(entry.getValue(), entry.getKey());
  }

  /**
   * @return the source type
   */
  public Class<SourceType> getSourceType() {
    return sourceType;
  }
  
  /**
   * @return the target type
   */
  public Class<TargetType> getTargetType() {
    return targetType;
  }

  /**
   * As this method returns the internal data structure,
   * use it with caution, or better, avoid using this
   * method!
   * @return
   */
  public final Map<SourceType, TargetType> getMapping() {
    return mapping;
  }

  /**
   * Public method that may be called before {@link #isReady()} to
   * ensure that the mapper has been inizialized.
   * @throws IOException 
   */
  public void initialize() throws IOException {
    if (!isInizialized) init();
  }
  
  /**
   * Allow access to templocalfile for subclasses
   * @return
   */
  protected String getTempLocalFile(){
  	return this.tempLocalFile;
  }
  
}
