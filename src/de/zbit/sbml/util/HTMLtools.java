/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.SBMLtools;

/**
 * @author Andreas Dr&auml;ger
 * @date 18:59:02
 * @since 1.1
 * @version $Rev$
 */
public class HTMLtools {
  
  /**
   * 
   * @param sbase
   * @return
   */
  public static String createTooltip(SBase sbase) {
    return sbase.isSetNotes() ? toTooltip(SBMLtools.toXML(sbase.getNotes())) : null;
  }
  
  /**
   * 
   * @param notesString
   * @return
   */
  public static String toTooltip(String notesString) {
    int firstIndex = notesString.indexOf("<html");
    int secondIndex = notesString.indexOf("</notes");
    if ((firstIndex > 0) && (secondIndex > 0)) {
      String ns = notesString.substring(firstIndex, secondIndex).trim();
      ns = ns.replace(" xmlns=\"http://www.w3.org/1999/xhtml\"", "");
      return ns;
    }
    return null;
  }
  
}
