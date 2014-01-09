/*
 * $Id$ $URL$
 * --------------------------------------------------------------------- This
 * file is part of the SysBio API library.
 * 
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.mapper;

/**
 * Generic interface for {@link AbstractMapper}.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @param <SourceType>
 * @param <TargetType>
 */
public interface Mapper<SourceType, TargetType> {
  
  /**
   * Return a simple name what is mapped to what
   * (e.g. "RefSeq2GeneID").
   * @return
   */
  public abstract String getMappingName();
  
  /**
   * Returns the TargetID for the given SourceID.
   * @param sourceID
   * @return TargetType targetID
   * @throws Exception - if mapping data could not be read (in general).
   */
  public abstract TargetType map(SourceType sourceID) throws Exception;
  
  /**
   * @return true if and only if the data has been read and
   * mapping data is available.
   */
  public abstract boolean isReady();
  
  /**
   * @return number of available mappings.
   */
  public abstract int size();
  
}
