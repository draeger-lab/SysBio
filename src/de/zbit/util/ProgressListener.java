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

import java.util.EventListener;

/**
 * Provides methods to listen for changes on a
 * {@link AbstractProgressBar}.
 * @author Clemens Wrzodek
 * @version $Rev$
 */

public interface ProgressListener extends EventListener {

  /**
   * Invoked when the percentage changed.
   */
  public void percentageChanged(int percent, double miliSecondsRemaining, String additionalText);

}

