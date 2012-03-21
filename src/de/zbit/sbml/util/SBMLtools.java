/*
 * $Id:  SBMLtools.java 17:32:00 draeger$
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

package de.zbit.sbml.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.filters.NameFilter;

import de.zbit.util.ResourceManager;

/**
 * Useful tools for working with SBML.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah Rachel M&uuml;ller vom Hagen
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.1
 */
public class SBMLtools {

  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(SBMLtools.class.getName());
  
  /**
   * 
   * @param sbase
   * @param term
   */
  public static final void setSBOTerm(SBase sbase, int term) {
    if (-1 < sbase.getLevelAndVersion().compareTo(Integer.valueOf(2),
      Integer.valueOf(2))) {
      sbase.setSBOTerm(term);
    } else {
    	ResourceBundle bundle = ResourceManager.getBundle("de.zbit.sbml.locales.Messages");
      logger.warning(MessageFormat.format(
          bundle.getString("COULD_NOT_SET_SBO_TERM"), 
          SBO.sboNumberString(term), sbase.getElementName(), sbase.getLevel(), sbase.getVersion()));
    }
  }
  
  /**
   * 
   * @param <T>
   * @param listOf
   * @param element
   */
  public static final <T extends NamedSBase> void addOrReplace(ListOf<T> listOf, T element) {
    T prev = listOf.firstHit(new NameFilter(element.getId()));
    if (prev != null) {
      listOf.remove(prev);
    }
    listOf.add(element);
  }
  
  /**
   * 
   * @param sbase
   * @param level
   * @param version
   */
  public static final void setLevelAndVersion(SBase sbase, int level, int version) {
    sbase.setVersion(version);
    sbase.setLevel(level);
    for (int i = 0; i<sbase.getChildCount(); i++) {
      TreeNode child = sbase.getChildAt(i);
      if (child instanceof SBase) {
        setLevelAndVersion((SBase) child, level, version);
      }
    }
  }
  
}