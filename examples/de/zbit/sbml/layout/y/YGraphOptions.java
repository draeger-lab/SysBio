/*
 * $Id$
 * $URL$
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
package de.zbit.sbml.layout.y;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * @author Alex Thomas
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public interface YGraphOptions extends KeyProvider {
  
  /**
   * This option decides if the nodes of a graph or network need to be laid out.
   */
  public static final Option<Boolean> NEED_TO_LAYOUT = new Option<Boolean>("NEED_TO_LAYOUT", Boolean.class, "This option decides if the nodes of a graph or network need to be laid out.");
  
}
