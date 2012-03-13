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
package de.zbit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class contains names and regular expressions to match various
 * identifiers from bioinformatics databases. For example, one can
 * get a regular expression to extract an ensembl id from any string.
 * 
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class DatabaseIdentifiers {
  private static final transient Logger log = Logger.getLogger(DatabaseIdentifiers.class.getName());
  
  /**
   * Contains a map from any {@link IdentifierDatabases}
   * to the corresponding regular expression.
   */
  private static Map<IdentifierDatabases, String> regExMap = new HashMap<IdentifierDatabases, String>();
  
  
  /**
   * An enumeration of different gene identifiers.
   * 
   * @author Clemens Wrzodek
   * @author Finja B&uuml;chel
   */
  public static enum IdentifierDatabases {
    NCBI_GeneID, RefSeq, Ensembl, KeggGenes, GeneSymbol;
  }  
  
  /**
   * Initialize the {@link #regExMap}.
   */
  static {
    // Do NOT append prefixes (^) or suffixes ($) or braces around the regex! 
    regExMap.put(IdentifierDatabases.NCBI_GeneID, "\\d+");
    regExMap.put(IdentifierDatabases.RefSeq, "(NC|AC|NG|NT|NW|NZ|NM|NR|XM|XR|NP|AP|XP|ZP)_\\d+");
    regExMap.put(IdentifierDatabases.Ensembl, "ENS[A-Z]*[FPTG]\\d{11}");
    regExMap.put(IdentifierDatabases.KeggGenes, "\\w+:[\\w\\d\\.-]*");
  }
  
  
  /**
   * Return a regular expression to identify a certain identifier.
   * @param identifier
   * @param contains specify if you want to have a "IS" regex (i.e. any string
   * IS any identifer) or an "CONTAINS" identifier (i.e. any string contains
   * any identifier). If you get a "CONTAINS" identifier, you can extract the
   * identifier from group number 1!
   * @return String with regular expression or null, if none available.
   */
  public static String getRegularExpressionForIdentifier(IdentifierDatabases identifier, boolean contains) {
    String regEx = regExMap.get(identifier);
    if (regEx!=null) {
      if (!contains) {
        // Create a "IS" identifier
        regEx = String.format("^%s$", regEx);
      } else {
        // Create a "contains" identifier
        regEx = String.format(".*?(%s).*?", regEx);
      }
    }
    return regEx;
  }
  
}
