/*
 * $Id:  CompoundID2KeggCompoundMapper.java 15:22:17 rosenbaum $
 * $URL: CompoundID2KeggCompoundMapper.java $
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
 * Maps CompoundID to KEGG Compound IDs.
 * CAS (Chemical Abstracts Service) is a division of the American Chemical
 * Society and is the producer of comprehensive databases of chemical information.
 
 * @author Lars Rosenbaum
 * @version $Rev$
 */
public class CompoundID2KeggCompoundMapper extends AbstractMapper<Integer,String> {

  private static final long serialVersionUID = 3761835058241496109L;
	public static final Logger log = Logger.getLogger(CompoundID2KeggCompoundMapper.class.getName());
  
  public CompoundID2KeggCompoundMapper() throws IOException {
  	this(null);
  }
  
	public CompoundID2KeggCompoundMapper(AbstractProgressBar progress) throws IOException {
	  super(Integer.class, String.class, progress);
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
		return "2013-04-17_HMDB_3_0_Mapping.zip";
	}

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getMappingName()
	 */
	@Override
	public String getMappingName() {
		return "CompoundID2KeggCompound";
	}

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.csv.CSVReader)
	 */
	@Override
	public int getTargetColumn(CSVReader r) {
		return 2;
	}

	/* (non-Javadoc)
	 * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.csv.CSVReader)
	 */
	@Override
	public int getSourceColumn(CSVReader r) {
		return 0;
	}
	
	@Override
	protected String preProcessSourceID(String string) {
    return string.substring(4);
  }

}
