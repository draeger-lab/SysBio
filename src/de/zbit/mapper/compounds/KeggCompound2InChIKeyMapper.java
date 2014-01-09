/*
 * $Id: KeggCompound2InChIKeyMapper.java 15:22:17 rosenbaum $
 * $URL: KeggCompound2InChIKeyMapper.java $
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
import java.util.logging.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Maps KEGG Compound IDs to InChIKey.
 * @author Lars Rosenbaum
 * @version $Rev$
 */
public class KeggCompound2InChIKeyMapper extends AbstractMultiEntryMapper<String, String> {

  private static final long serialVersionUID = 3761835058241496109L;
	public static final Logger log = Logger.getLogger(KeggCompound2InChIKeyMapper.class.getName());
  
  public KeggCompound2InChIKeyMapper() throws IOException {
  	this(null);
  }
  
	public KeggCompound2InChIKeyMapper(AbstractProgressBar progress) throws IOException {
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
		return "2013-09-20_CompoundData.zip";
	}

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getMappingName()
	 */
	@Override
	public String getMappingName() {
		return "KeggCompound2InChIKey";
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
		return 4;
	}
	
  /* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#preProcessSourceID(java.lang.String)
	 */
  @Override
  protected String preProcessSourceID(String string) {
  	if(string==null || string.length() == 0) return string;

	  // "CPD:C00523" => "C00523"
    if (string.length()>6) {
      string = string.substring(string.length()-6);
    }
    if (string.charAt(0)=='c') {
      string = string.toUpperCase();
    }
    return string;
  }

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#postProcessSourceID(java.lang.Object)
	 */
  @Override
  protected String postProcessSourceID(String source) {
	  if(source==null || source.isEmpty()) return null;
	  
	  // "CPD:C00523" => "C00523"
    if (source.length()>6) {
      source = source.substring(source.length()-6);
    }
    if (source.charAt(0)=='c') {
      source = source.toUpperCase();
    }
    
    return source;
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
