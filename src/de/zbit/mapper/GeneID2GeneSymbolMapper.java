/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import de.zbit.io.csv.CSVReader;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * NCBI GeneID 2 GeneSymbol mapping.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class GeneID2GeneSymbolMapper extends AbstractMapper<Integer, String> {
  private static final long serialVersionUID = -2886703519596230180L;

  public static final Logger log = Logger.getLogger(GeneID2GeneSymbolMapper.class.getName());

  /**
   * Organism in non-scientific format ("human", "mouse" or "rat").
   */
  String organism;
  
  /**
   * @param organism in non-scientific format ("human", "mouse" or "rat").
   * @throws IOException
   */
  public GeneID2GeneSymbolMapper(String organism) throws IOException {
    this(organism, null);
  }
  
  /**
   * @param organism in non-scientific format ("human", "mouse" or "rat").
   * @param progress
   * @throws IOException
   */
  public GeneID2GeneSymbolMapper(String organism, AbstractProgressBar progress) throws IOException {
    super(Integer.class, String.class, progress);
    this.organism = organism;
  }
  
  
  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    GeneID2GeneSymbolMapper mapper = new GeneID2GeneSymbolMapper("human");
    System.out.println(mapper.map(1576));
    System.out.println(mapper.map(0));
    System.out.println(mapper.map(123456236));
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#init()
   */
  @Override
  protected void init() throws IOException {
    // Read GeneID 2 KEGG GENES ID mapping
    GeneSymbol2GeneIDMapper gs2gi = new GeneSymbol2GeneIDMapper(organism, progress);
    gs2gi.init();
    
    // Reverse this mapping and use for this class.
    reverse(gs2gi);
    isInizialized = true;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return null; // Not required. See GeneID2KeggIDMapper.java
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "GeneID2GeneSymbol";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return null; // Not required. See GeneID2KeggIDMapper.java
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 0; // Not required. See GeneID2KeggIDMapper.java
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 0; // Not required. See GeneID2KeggIDMapper.java
  }

}
