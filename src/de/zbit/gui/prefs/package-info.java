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
 * <p>
 * This package is intended to provide useful helper classes that support the
 * configuration of graphical user interfaces. It provides a generic dialog and
 * a generic panel for creating any kind of settings dialogs based on the Java
 * Properties hashtable.
 * </p>
 * <p>
 * The principle is to create a class derived from SettingsPanel that exchanges
 * its settings using the Properties class and to put it into a package with the
 * same name as this one, i.e., de.zbit.gui.cfg. The configuratin dialog will
 * automatically find it using java Reflection and will include it into the user
 * interface.
 * </p>
 * <p>
 * All you have to do to make use of this is to declare how your specific
 * settings panel should look like and which elements should be on top of it.
 * Wrap all the user input in a Properties hashtable and that's it.
 * </p>
 * @version $Rev$
 */
package de.zbit.gui.prefs;
