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
package de.zbit.mapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.zbit.io.CSVReader;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.FileDownload;
import de.zbit.util.ProgressBar;
import de.zbit.util.Timer;
import de.zbit.util.prefs.Option;

/**
 * An abstract mapper that can download a csv file or read an
 * supplied / already downloaded file and build an internal map
 * from one column to another.
 * Afterwards, one sourceIdentifier can be mapped to the
 * corresponding targetIdentifier.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public abstract class AbstractMapper<SourceType, TargetType> implements Serializable {
  private static final long serialVersionUID = -1940567043534334136L;
  
  public static final Logger log = Logger.getLogger(AbstractMapper.class.getName());
  
  private Class<TargetType> targetType;
  private Class<SourceType> sourceType;
  
  /**
   * A progress Bar that is used while downloading a File.
   * If null, no progress will be displayed.
   */
  private AbstractProgressBar progress=null;
  
  
  /**
   * Contains a mapping from RefSeq to GeneID.
   * XXX: Hier eventuell eine initial Capacity oder load factor angeben, falls BottleNeck.
   */
  protected Map<SourceType, TargetType> mapping = new HashMap<SourceType, TargetType>();

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
    if (!readMappingData()) mapping=null;
  }
  
  
  /**
   * Returns the HTTP URL of the latest mapping file.
   * @return
   */
  public abstract String getRemoteURL();
  /**
   * Returns the local file name where the downloaded file should be saved to.
   * @return
   */
  public abstract String getLocalFile();

  /**
   * Return a simple name what is mapped to what
   * (e.g. "RefSeq2GeneID").
   * @return
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
   * Returns true if the {@link #localFile} has already been downloaded from
   * {@link #downloadURL}.
   * @return
   */
  public boolean isCachedDataAvailable() {
    if (getLocalFile()==null || getLocalFile().length()<0) {
      return false;
    }
    
    File f  = new File(getLocalFile());
    return f.exists() && f.length()>0;
  }
  
  /**
   * Downloads the {@link #downloadURL} and stores the path of the downloaded
   * file in {@link #localFile}.
   * If the download was successfull can be checked with {@link #isCachedDataAvailable()}.
   */
  private void downloadData() {
    String localFile = getLocalFile();
    FileDownload.ProgressBar = progress;
    try {
      // Create parent directories
      new File(new File(localFile).getParent()).mkdirs();
    } catch (Throwable t){};
    String localf = FileDownload.download(getRemoteURL(), localFile);
    if (localFile!=null && localFile.length()>0) localFile = localf;
    if (FileDownload.ProgressBar!=null) {
      ((ProgressBar)FileDownload.ProgressBar).finished();
    }
  }

  
  /**
   * Reads the mapping from {@link #localFile} into the {@link #mapping} set.
   * Downloads data automatically from {@link #downloadURL} as required.
   * @return true if and only if everything was without critical errors.
   * @throws IOException
   */
  public boolean readMappingData() throws IOException {
    String localFile = getLocalFile();
    
    if (!isCachedDataAvailable()) {
      downloadData();
      if (!isCachedDataAvailable()) {
        return false;
      }
    }
    log.config("Reading " + getMappingName() + " mapping file " + localFile);
    Timer t = new Timer();
    CSVReader r = new CSVReader(localFile);
    int sourceColumn = getSourceColumn(r);
    int targetColumn = getTargetColumn(r);
    // Read RefSeq <=> Gene ID mapping.
    String[] line;
    r.open();
    // XXX: When using a progressBar here with a compressed File, the bar Fails!
    while ((line = r.getNextLine())!=null) {
      if (line.length<(Math.max(sourceColumn, targetColumn)+1)) {
        log.severe("Incomplete entry in mapping file '" + localFile + "'. Please try to delete this file and execute this application again.");
        continue;
      }
      
      
      // Get target ID
      TargetType target = Option.parseOrCast(targetType, line[targetColumn]);
      if (target==null) {
        log.warning("Invalid target content in " + getMappingName() + " mapping file: " + ((line.length>1)?line[targetColumn]:"line too short."));
        continue;
      }

      // Get source ID
      SourceType source = Option.parseOrCast(sourceType, line[sourceColumn]);
      if (source==null) {
        log.warning("Invalid source content in " + getMappingName() + " mapping file: " + ((line.length>1)?line[sourceColumn]:"line too short."));
        continue;
      }
      
      // Optional methods that allow customization.
      source = postProcessSourceID(source);
      target = postProcessTargetID(target);
      
      mapping.put(source, target);
    }
    
    log.config("Parsed " + getMappingName() + " mapping file in " + t.getNiceAndReset()+". Read " + ((mapping!=null)?mapping.size():"0") + " mappings.");
    return (mapping!=null && mapping.size()>0);
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
  

  /**
   * Returns the TargetID for the given SourceID.
   * @param sourceID
   * @return TargetType targetID
   * @throws Exception - if mapping data could not be read (in general).
   */
  public TargetType map(SourceType sourceID) throws Exception {
    if (!isReady()) throw new Exception(getMappingName()+" mapping data has not been read successfully.");
    SourceType trimmedInput = postProcessSourceID(sourceID);
    TargetType ret = mapping.get(trimmedInput);
    log.finest("map: " + trimmedInput + ", to: " + ret);
    return ret;
  }

  /**
   * @return true if and only if the data has been read and
   * mapping data is available.
   */
  public boolean isReady() {
    return mapping!=null && mapping.size()>0;
  }
  
}
