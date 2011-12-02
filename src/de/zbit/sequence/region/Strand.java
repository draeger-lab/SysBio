/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sequence.region;

/**
 * Interface to store strand orientation information.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public interface Strand {
  
  /**
   * Strand orientation is added to {@link NameAndSignals#addData(String, Object)} with this key.
   */
  public final static String strandKey = "reverse_orientation";
  
  /**
   * @return <code>TRUE</code> if element is on forward strand.
   */
  public boolean isOnForwardStrand();
  
  /**
   * @return <code>TRUE</code> if element is on reverse strand.
   */
  public boolean isOnReverseStrand();

  /**
   * @return <code>TRUE</code> if orientation is known.
   */
  public boolean isStrandKnown();

  /**
   * Set the orientation
   * @param onReverse <code>TRUE</code> if element is on reverse strand,
   * <code>FALSE</code> if element is on forward strand.
   */
  public void setIsOnReverseStrand(boolean onReverse);
  
}
