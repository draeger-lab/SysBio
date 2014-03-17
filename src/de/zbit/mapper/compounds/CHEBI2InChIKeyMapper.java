/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Maps CHEBI Compound IDs to InChIKeys.
 * @author Lars Rosenbaum
 * @version $Rev$
 */
public class CHEBI2InChIKeyMapper extends AbstractMultiEntryMapper<String, String> {
  private static final long serialVersionUID = -6922730507527500121L;
	public static final Logger log = Logger.getLogger(CHEBI2InChIKeyMapper.class.getName());
  
  public CHEBI2InChIKeyMapper() throws IOException {
  	this(null);
  }
  
	public CHEBI2InChIKeyMapper(AbstractProgressBar progress) throws IOException {
	  super(String.class, String.class , progress);
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
  	return "2013-09-20_CompoundData.zip";
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getMappingName()
	 */
  @Override
  public String getMappingName() {
	  return "CHEBI2InChIKey";
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.csv.CSVReader)
	 */
  @Override
  public int getTargetColumn(CSVReader r) {
	  return 0;
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.csv.CSVReader)
	 */
  @Override
  public int getSourceColumn(CSVReader r) {
	  return 5;
  }
  
  /* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#postProcessSourceID(java.lang.Object)
	 */
  @Override
  protected String postProcessSourceID(String source) {
	  if(source==null || source.isEmpty()) return null;
	  String s = source.toUpperCase();
		if(!source.startsWith("CHEBI:"))
			return "CHEBI:".concat(s);
		else
			return s;
  }
  
  /* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#preProcessSourceID(String)
	 */
	@Override
	protected String preProcessSourceID(String string) {
		String s = string.toUpperCase();
		if(!string.startsWith("CHEBI:"))
			return "CHEBI:".concat(s);
		else
			return s;
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
	 * @see de.zbit.mapper.compounds.AbstractMultiSourceEntryMapper#getEntrySeparator()
	 */
  @Override
  public String getEntrySeparator() {
	  return "\\|\\|";
  }

}
