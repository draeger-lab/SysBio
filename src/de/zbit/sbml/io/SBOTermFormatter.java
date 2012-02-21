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
package de.zbit.sbml.io;

import org.sbml.jsbml.SBO.Term;
import org.sbml.jsbml.util.StringTools;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-12-14
 * @version $Rev$
 */
public class SBOTermFormatter {

  /**
   * @param term
   * @return
   */
  public static String getShortDefinition(Term term) {
    String def = term.getDefinition();
    String definition = def.toString().replace("\\, ", ", ");
    if (definition.startsWith("\"")) {
      definition = definition.substring(1);
    }
    int pos = definition.length() - 1;
    String endWords[] = new String[] { "\n", "xmlns=", "[", "\"" };
    for (String word : endWords) {
      int end = definition.indexOf(word);
      if ((0 < end) && (end < pos)) {
        pos = end;
      }
    }
    if (pos > 0) {
      definition = definition.subSequence(0, pos).toString();
    }
    return StringTools.firstLetterUpperCase(definition.trim());
  }

}
