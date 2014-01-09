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

/**
 * A collection of classes for dealing with user-defined options and
 * configuration key, either from the command line or from some system
 * configuration. Thereby, three layers are considered:
 * <ol>
 * <li>User options from the command line</li>
 * <li>Options that are currently in memory but not yet persistent</li>
 * <li>Persistently saved user configuration</li>
 * </ol>
 * The idea is to define a key-value pair for the option and an associated
 * user-defined value and to re-use this pair in graphical user interfaces, on
 * the command line and also at other positions within the program.
 * @version $Rev$
 */
package de.zbit.util.prefs;
