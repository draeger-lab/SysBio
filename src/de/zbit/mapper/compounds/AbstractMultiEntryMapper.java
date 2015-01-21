/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.mapper.compounds;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.mapper.AbstractMapper;
import de.zbit.util.ArrayUtils;
import de.zbit.util.Timer;
import de.zbit.util.prefs.Option;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * An abstract mapper that can download a csv file or read an
 * supplied / already downloaded file and build an internal map
 * from one column to another. Compared to the {@link AbstractMapper}
 * an entry in a column can be separated by {@link #getEntrySeparator()},
 * which is necessary, e.g., if a cell contains multiple  HMDBIDs
 * for a certain InChiKey. For sources, this can be done by multiple source
 * columns but sometimes it is not clear how many columns we will need
 * so its easier to add another level of separation. Furthermore, more files
 * require more space.
 * It might happen that the target column can be separated by
 * {@link #getEntrySeparator()} too. Consequently the mapper assumes the
 * targets to be sets of the target types and all targets will be added
 * Afterwards, one sourceIdentifier can be mapped to the corresponding
 * targetIdentifier
 * In general, this mapper easily supports n:n mappings by using the separator
 * @author Lars Rosenbaum
 * @version $Rev$
 */
public abstract class AbstractMultiEntryMapper<SourceType,TargetType> extends AbstractMapper<SourceType, Set<TargetType>> {
  private static final long serialVersionUID = 8025808915520843979L;
  public static final Logger log = Logger.getLogger(AbstractMultiEntryMapper.class.getName());
  
  /**
   * Targets might have multiple entries and are consequently represented as sets
   * This variable indicates which types are stored in the set
   */
  private final Class<TargetType> innerTargetType;
  
  /**
   * Initializes the mapper. Downloads and reads the mapping
   * file automatically as required.
   * @throws IOException
   */
  public AbstractMultiEntryMapper(Class<SourceType> sourceType, Class<TargetType> targetType) throws IOException {
    this(sourceType,targetType,null);
  }
  
  /**
   * Initializes the mapper. Downloads and reads the mapping
   * file automatically as required.
   * @see AbstractMapper#AbstractMapper(Class, Class)
   */
  @SuppressWarnings("unchecked")
  public AbstractMultiEntryMapper(Class<SourceType> sourceType, Class<TargetType> targetType, AbstractProgressBar progress) throws IOException {
    super(sourceType,(Class<Set<TargetType>>)(Class<?>)Set.class,progress);
    this.innerTargetType = targetType;
  }
  
  /**
   * Separator that separates entries in a single column entry
   * @return
   */
  public abstract String getEntrySeparator();
  
  /**
   * Because of multiple targetValues per entry, the target Values are stored in a set
   * This functions returns the type of object stored in the set.
   * @return
   */
  public Class<TargetType> getInnerTargetType(){
  	return this.innerTargetType;
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
    
    // Parse all files.
    Timer t = new Timer();
    String[] localFiles = ArrayUtils.merge(getLocalFiles(), getLocalFile(), getTempLocalFile());
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
      	
        //XXX: The splitting should better be handled similar to csv reader
      	//but this will solve it for now
        String[] entry = line[targetColumn].split(getEntrySeparator());
        
        Set<TargetType> targetSet;
      	targetSet = (Set<TargetType>) new HashSet<TargetType>(); 
      	
        for(String targetString: entry){
        	
        	//Catch empty target Strings
        	if(targetString.isEmpty()){
        		continue;
        	}
        	
        	TargetType target;
          if (Collection.class.isAssignableFrom(innerTargetType)) {
            // Mapping from x to a collection.
             target = (TargetType) new HashSet(); // Do not store the same target multiple times
             // Remark: This works also for non-Strings, since the type of the collection is
             // erased on runtime. Thus, postProcessTargetID() should re-convert to desired
             // real target type.
            ((Collection)target).add(preProcessTargetID(targetString));
          } else {
            target = Option.parseOrCast(innerTargetType, preProcessTargetID(targetString));
          }
          if (target==null) {
            log.warning("Invalid target content in " + getMappingName() + " mapping file: " + ((line.length>targetColumn)?line[targetColumn]:"line too short."));
            continue;
          }

        	targetSet.add(target);
	
        }
        
        //no targets so there is nothing to add to the mapping
        if(targetSet.isEmpty())
        	continue;
	        
        // Optional method that allow customization.
        targetSet = postProcessTargetID(targetSet);

        // Add mapping for all source columns
        int lastSourceColumn=0;
        for (int sourceColumn: multiSourceColumn) {
        	if (sourceColumn<line.length) {
        		addToMapping(line[sourceColumn], targetSet);
        	} else if (sourceColumn==Integer.MAX_VALUE) {
        		// This is interpreted as "Add all cells from the last one to
        		// the end of the file as source columns".
        		// Don't forget to configureReader() when using this method.
        		// CSVReader fails to autodetect properties of tables with
        		// variable number of columns!
        		for (int col=lastSourceColumn+1;col<line.length; col++) {
        			addToMapping(line[col], targetSet);
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
   * the current mapping, allows a CSV cell to be separated
   * by {@link #getEntrySeparator()} to generate multiple source
   * entries at once
   * @param sourceCell
   * @param target
   */
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void addToMapping(String sourceCell, Set<TargetType> target) {
    //XXX: The splitting should better be handled similar to csv reader
  	//but this will solve it for now
  	String[] split = sourceCell.split(getEntrySeparator());
  	for(String sourceEntry: split){
	  	// Get source ID
	    SourceType source = null;
	    try {
	      source = Option.parseOrCast(getSourceType(), preProcessSourceID(sourceEntry));
	    } catch (Throwable e) {}
	    if (source==null) {
	      log.warning("Invalid source content in " + getMappingName() + " mapping file: " + (sourceEntry!=null?sourceEntry:"null.") );
	      return;
	    }
	    source = postProcessSourceID(source);
	    if (source==null) return;
	    
	    // Allow multiple target elements in collections
	    if (Collection.class.isAssignableFrom(getTargetType())) {
	      Collection c = (Collection) getMapping().get(source);
	      if (c!=null) {
	        c.addAll((Collection)target);
	        return;
	      }
	    }
	    
	    getMapping().put(source, target);
  	}
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
	 */
  @Override
  public String getRemoteURL() {
	  // TODO Auto-generated method stub
	  return null;
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getLocalFile()
	 */
  @Override
  public String getLocalFile() {
	  // TODO Auto-generated method stub
	  return null;
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getMappingName()
	 */
  @Override
  public String getMappingName() {
	  // TODO Auto-generated method stub
	  return null;
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.csv.CSVReader)
	 */
  @Override
  public int getTargetColumn(CSVReader r) {
	  // TODO Auto-generated method stub
	  return 0;
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.csv.CSVReader)
	 */
  @Override
  public int getSourceColumn(CSVReader r) {
	  // TODO Auto-generated method stub
	  return 0;
  }

}
