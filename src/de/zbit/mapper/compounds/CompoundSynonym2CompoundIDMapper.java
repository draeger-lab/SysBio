/*
 * $Id:  CompoundSynonym2CompoundIDMapper.java 15:22:17 rosenbaum $
 * $URL: CompoundSynonym2CompoundIDMapper.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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
import java.util.logging.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.mapper.AbstractMapper;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Tries to map compound synonyms and names to Compound IDs.
 * CAS (Chemical Abstracts Service) is a division of the American Chemical
 * Society and is the producer of comprehensive databases of chemical information.
 
 * @author Lars Rosenbaum
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class CompoundSynonym2CompoundIDMapper extends AbstractMapper<String,Integer> {

  private static final long serialVersionUID = 3761835058241496109L;
	public static final Logger log = Logger.getLogger(CompoundSynonym2CompoundIDMapper.class.getName());
  
  public CompoundSynonym2CompoundIDMapper() throws IOException {
  	this(null);
  }
  
	public CompoundSynonym2CompoundIDMapper(AbstractProgressBar progress) throws IOException {
	  super(String.class, Integer.class, progress);
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
		return "2013-04-17_HMDB_3_0_Mapping_synonyms.zip";
	}

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getMappingName()
	 */
	@Override
	public String getMappingName() {
		return "CompoundSynonym2CompoundID";
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
    return 1; // Never called if getMultiSourceColumn() is implemented.
  }
	
	@Override
	protected String preProcessTargetID(String string) {
    return string.substring(4);
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

}
