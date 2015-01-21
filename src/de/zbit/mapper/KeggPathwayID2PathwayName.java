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
package de.zbit.mapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.kegg.api.KeggAdaptor;
import de.zbit.kegg.api.cache.KeggFunctionManagement;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * A mapping from KEGG pathway identifier to human readable name
 * for that pathway.
 * 
 * <p>XXX: KEGG Pathway FTP is no more available. You'll have to use
 * cached KEGG mapping files for this class to work. Or you could
 * simply use the {@link KeggAdaptor} (with Cache:
 * {@link KeggFunctionManagement}) to get names for pathway ids!
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class KeggPathwayID2PathwayName extends AbstractMapper<String, String> {
  private static final long serialVersionUID = -2431134548856641204L;

  public static final Logger log = Logger.getLogger(KeggPathwayID2PathwayName.class.getName());
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   * @return 
   */
  private final static String downloadURL = "ftp://ftp.genome.jp/pub/kegg/pathway/map_title.tab";
  
  /**
   * If this is true, will make a  reverse (pathwayName 2 Id) mapping.
   */
  private final boolean reverseMapping;
  
  /**
   * @param sourceType
   * @param targetType
   * @throws IOException
   */
  public KeggPathwayID2PathwayName() throws IOException {
    this(null);
  }
  
  public KeggPathwayID2PathwayName(AbstractProgressBar progress) throws IOException {
    this(false, progress);
  }
  
  public KeggPathwayID2PathwayName(boolean reverseMapping, AbstractProgressBar progress) throws IOException {
    // This constructor is called from every other!
    super(String.class, String.class, progress);
    this.reverseMapping = reverseMapping;
    init();
  }
  
  
  /**
   * TESTS ONLY!
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    
    KeggPathwayID2PathwayName mapper = new KeggPathwayID2PathwayName();
    System.out.println(mapper.map("path:mmu05416"));
    System.out.println(mapper.map("path:mmu04810"));
    System.out.println(mapper.map("path:mmu04530"));
    
    mapper = new KeggPathwayID2PathwayName(true, null);
    System.out.println(mapper.map("Viral myocarditis"));
    System.out.println(mapper.map("Regulation of actin cytoskeleton"));
    System.out.println(mapper.map("Tight junction"));
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return  "res/" + FileTools.getFilename(getRemoteURL());
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return reverseMapping?"PathwayName2KeggPathwayID":"KeggPathwayID2PathwayName";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return downloadURL;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return reverseMapping?1:0;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return reverseMapping?0:1;
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#postProcessSourceID(java.lang.Object)
   */
  @Override
  protected String postProcessSourceID(String source) {
    // Only the 5-digit number at the end of a string is relevant
    if (!reverseMapping && source.length()>5)
      return source.substring(source.length()-5, source.length());
    else
      return source;
  }
  
}
