/*
 * $Id$
 * $URL$
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
package de.zbit.sequence.region;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import de.zbit.util.ArrayUtils;

/**
 * Tools to convert a chromosome from and to different condings.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public final class ChromosomeTools {
  public static final transient Logger log = Logger.getLogger(ChromosomeTools.class.getName());
  
  /**
   * Converts the chromosome to a {@link Byte} representation.
   * @param chromosome
   * @return Chromsome number or -1 = X; -2 = Y; -3 = M; Or
   * {@link Chromosome#default_Chromosome_byte} if anything went wrong.
   */
  public static byte getChromosomeByteRepresentation(String chromosome) {
    if (chromosome==null || chromosome.equals(Chromosome.default_Chromosome_string)) {
      return Chromosome.default_Chromosome_byte;
    }
    
    chromosome = parseChromosomeFromString(chromosome);
    
    if (chromosome!=null) {
      if (chromosome.equalsIgnoreCase("X")) return -1;
      else if (chromosome.equalsIgnoreCase("Y")) return -2;
      else if (chromosome.equalsIgnoreCase("M")) return -3;
      else  {
        try {
          return (byte) Integer.parseInt(chromosome);
        } catch (NumberFormatException e) {
          log.warning(String.format("Unknown Chromosome \"%s\".", chromosome));
          return Chromosome.default_Chromosome_byte;
        }
      }
      
    } else {
      log.warning(String.format("Unknown Chromosome \"%s\".", chromosome));
    }
    
    return Chromosome.default_Chromosome_byte;
  }
  
  /**
   * Parse a chromosome from any string (using the {@link Chromosome#chromosome_regex}).
   * @param chromosome
   * @return {@code null} if the regex did not match any expression.
   */
  public static String parseChromosomeFromString(String chromosome) {
    Matcher m = Chromosome.chromosome_regex.matcher(chromosome.trim());
    if (m.find()) {
      return m.group(2);
    }
    return null;
  }
  
  /**
   * Converts a byte chromosome representation (created with
   * {@link #getChromosomeByteRepresentation(String)}) to a
   * String representation
   * @param chromosome
   * @return e.g. "chr5"
   */
  public static String getChromosomeStringRepresentation(byte chromosome) {
    if (chromosome==-1) return "chrX";
    else if (chromosome==-2) return "chrY";
    else if (chromosome==-3) return "chrM";
    else  {
      if (chromosome==Chromosome.default_Chromosome_byte) {
        return Chromosome.default_Chromosome_string;
      } else {
        return "chr"+Byte.toString(chromosome);
      }
    }
  }

  /**
   * Splits a list of regions by their chromosomal location
   * @param <T>
   * @param regions
   * @return a map from chromosome to list of all regions
   * on that chromosome
   */
  public static <T extends Chromosome> Map<Byte, List<T>> splitByChromosome(Iterable<T> regions) {
    Map<Byte, List<T>> ret = new HashMap<Byte, List<T>>();
    for (T region: regions) {
      ArrayUtils.addToList(ret, new Byte(region.getChromosomeAsByteRepresentation()), region);
    }
    return ret;
  }
  
}
