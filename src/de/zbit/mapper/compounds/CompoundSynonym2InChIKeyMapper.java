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

import org.apache.log4j.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * @author Lars Rosenbaum
 * @version $Rev$
 */
public class CompoundSynonym2InChIKeyMapper extends AbstractMultiEntryMapper<String, String> {
  private static final long serialVersionUID = -752885457570068938L;
  public static final Logger log = Logger.getLogger(CompoundSynonym2InChIKeyMapper.class.getName());
  
  
  public CompoundSynonym2InChIKeyMapper() throws IOException {
  	this(null);
  }
  
	public CompoundSynonym2InChIKeyMapper(AbstractProgressBar progress) throws IOException {
	  super(String.class, String.class, progress);
	  init();
  }
  

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
	 */
	@Override
	public String getRemoteURL() {
		//only available locally
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getLocalFile()
	 */
	@Override
	public String getLocalFile() {
		return "2013-09-20_CompoundSynonyms.zip";
	}

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getMappingName()
	 */
	@Override
	public String getMappingName() {
		return "CompoundSynonym2InChIKey";
	}

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.csv.CSVReader)
	 */
	@Override
	public int getTargetColumn(CSVReader r) {
		return 0;
	}

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMultiSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int[] getMultiSourceColumn(CSVReader r) {
    // From the second(1) to the end of the line [1..n]
    return new int[]{1, Integer.MAX_VALUE};
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return -1; // Never called if getMultiSourceColumn() is implemented.
  }
  
  @Override
  protected String postProcessSourceID(String source) {
  	if(source==null || source.isEmpty()) return null;
    return source.toLowerCase();
  }
  
  @Override
	protected String preProcessSourceID(String string) {
    return string.toLowerCase();
  }
	
  /* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#preProcessTargetID(java.lang.String)
	 */
  @Override
  protected String preProcessTargetID(String string) {
  	if(string==null || string.length() == 0) return string;
  	else
  		return string.toUpperCase();
  }
	
	 /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#configureReader(de.zbit.io.csv.CSVReader)
   */
  @Override
  protected void configureReader(CSVReader r) {
    r.setSeparatorChar('\t');
    r.setContainsHeaders(true);
    r.setSkipLines(0);
    r.setAutoDetectContentStart(false);
  }

  /* (non-Javadoc)
	 * @see de.zbit.mapper.compounds.AbstractMultiSourceEntryMapper#getEntrySeparator()
	 */
  @Override
  public String getEntrySeparator() {
	  return "\\|\\|";
  }

}
