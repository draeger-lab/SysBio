/*
 * $Id:  InChIKey2CompoundNameMapper.java 14:03:34 rosenbaum $
 * $URL: InChIKey2CompoundNameMapper.java $
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
 * Maps InChIKey to Compound names
 * @author Lars Rosenbaum
 * @version $Rev$
 */
public class InChIKey2CompoundNameMapper extends AbstractMapper<String, String> {
  private static final long serialVersionUID = 4692835227710212531L;
  public static final Logger log = Logger.getLogger(InChIKey2CompoundNameMapper.class.getName());
  
  public InChIKey2CompoundNameMapper() throws IOException {
  	this(null);
  }
  
	public InChIKey2CompoundNameMapper(AbstractProgressBar progress) throws IOException {
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
	  return "2013-09-06_CompoundData.zip";
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getMappingName()
	 */
  @Override
  public String getMappingName() {
	  return "InChIKey2CompoundName";
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.csv.CSVReader)
	 */
  @Override
  public int getTargetColumn(CSVReader r) {
	  return 1;
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.csv.CSVReader)
	 */
  @Override
  public int getSourceColumn(CSVReader r) {
	  return 0;
  }
  
  /* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#preProcessSourceID(java.lang.String)
	 */
  @Override
  protected String preProcessSourceID(String string) {
  	if(string==null || string.length() == 0) return string;
  	else
  		return string.toUpperCase();
  }

}
