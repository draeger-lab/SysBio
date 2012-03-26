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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.sbml.jsbml.CVTerm;

import de.zbit.kegg.api.KeggInfos;
import de.zbit.util.DatabaseIdentifiers.IdentifierDatabases;

/**
 * This class conatins various Tools for {@link DatabaseIdentifiers}.
 * 
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class DatabaseIdentifierTools {
  private static final transient Logger log = Logger.getLogger(DatabaseIdentifierTools.class.getName());
  
  /**
   * 
   * @param ids either a {@link String} (for single identifiers) or an Array of Strings 
   * or any {@link Iterable} instance of strings, for multiple identifiers per
   * database.
   * @param myCVterm
   * @param miriam_URNPrefix
   */
  @SuppressWarnings("rawtypes")
  private static void appendAllIds(Object ids, CVTerm myCVterm, String miriam_URNPrefix) {
    // check if we have a single identifier
    if (ids==null) return;
    
    else if (ids instanceof String) {
      String urn = miriam_URNPrefix + KeggInfos.suffix(ids.toString());
      if (!myCVterm.getResources().contains(urn)) {
        myCVterm.addResource(urn);
      }
    }
    
    else if (ids.getClass().isArray()) {
      ids = Arrays.asList(ids);
      appendAllIds(ids, myCVterm, miriam_URNPrefix);
      
    } else if (ids instanceof Iterable) {
      Iterator it = ((Iterable)ids).iterator();
      while (it.hasNext()) {
        appendAllIds(it.next(), myCVterm, miriam_URNPrefix);
      }
      
    } else {
      log.warning("Can not add an identifier of type " + ids.getClass().getSimpleName());
    }
  }
  
  
  /**
   * Convert a map of various {@link DatabaseIdentifiers} to MRIAM URNs
   * in CVTerms.
   * @param ids a map that points from {@link DatabaseIdentifiers} to either
   * {@link String}s (for single identifiers) or an Array of Strings or
   * any {@link Iterable} instance of strings, for multiple identifiers per
   * database.
   * @return a list of {@link CVTerm}s containing all given
   * identifiers.
   */
  public List<CVTerm> getCVTerms(Map<DatabaseIdentifiers.IdentifierDatabases, Object> ids) {
    List<CVTerm> ret = new ArrayList<CVTerm>();
    if (ids==null) return ret;
    
    for (IdentifierDatabases db: ids.keySet()) {
      CVTerm mycv = new CVTerm();
      mycv.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
      mycv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
      
      // Append all CV Terms
      Object id = ids.get(db);
      // TODO: Get miriam_URNPrefix from DatabaseIdentifiers
      String miriam_URNPrefix = "TODO:";
      appendAllIds(id, mycv, miriam_URNPrefix);
      
      // Set BQB to IS or Has_Version
      if (mycv.getResourceCount() > 1) {
        // Multiple proteins in one node
        mycv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_HAS_VERSION);
      } else {
        mycv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
      }
    }
    
    return ret;
  }
  
}
