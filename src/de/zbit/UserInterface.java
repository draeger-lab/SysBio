/*
 * $Id$
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
package de.zbit;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * A very simple user interface.
 * 
 * @author Andreas Dr&auml;ger
 * @date 08:27:18
 * @since 1.1
 * @version $Rev$
 */
public interface UserInterface extends PropertyChangeListener {

	/**
	 * Closes a {@link File} that is currently open.
	 * 
	 * @return Whether or not calling this method lead to any change on this
	 *         {@link UserInterface}.
	 */
	public abstract boolean closeFile();

	/**
	 * The name of this program.
	 * 
	 * @return
	 */
	public abstract String getApplicationName();

	/**
	 * This is required to automatically include a list of possible command-line
	 * options into the online help. The array of {@link KeyProvider} classes
	 * contains all those {@link KeyProvider}s whose {@link Option} entries are
	 * valid keys for the command line.
	 * 
	 * @return
	 */
	public abstract Class<? extends KeyProvider>[] getCommandLineOptions();

	/**
	 * The version number of this program. This must be a {@link String}
	 * containing only digits and at least one dot or at most two dots. For
	 * instance "1.2" or "1.2.3".
	 * 
	 * @return
	 */
	public abstract String getDottedVersionNumber();

	/**
	 * This method creates a title from the values of
	 * {@link #getApplicationName()} and {@link #getDottedVersionNumber()}. The
	 * value returned may be an empty {@link String} but never {@code null}. In case
	 * neither an application name nor a dotted version number are defined, an
	 * empty {@link String} will be returned. If both values are defined, the
	 * returned value will be application name white space version number. In case
	 * that one of the values is missing, the returned {@link String} will be
	 * shorter.
	 * 
	 * @return Creates and returns a {@link String} that combines the value
	 *         returned by {@link #getApplicationName()} and
	 *         {@link #getDottedVersionNumber()} to identify this program.
	 */
	public abstract String getProgramNameAndVersion();

	/**
	 * The {@link URL} where the about message for this program is located, i.e.,
	 * an HTML file containing information about the people in charge for this
	 * program.
	 * 
	 * @return
	 */
	public abstract URL getURLAboutMessage();

	/**
	 * The {@link URL} of the license file under which this application is
	 * distributed.
	 * 
	 * @return
	 */
	public abstract URL getURLLicense();

	/**
	 * The {@link URL} of the online help file.
	 * 
	 * @return
	 */
	public abstract URL getURLOnlineHelp();

	/**
	 * <p>
	 * The online update expects to find a file called {@code latest.txt}
	 * containing only the version number of the latest release of this program as
	 * a {@link String} of digits that contains exactly one dot or at most two
	 * dots. Furthermore, on the given destination must be a second file, called
	 * {@code releaseNotes&lt;VersionNumber&gt;.htm[l]}, which contains more
	 * detailed information about the latest release.
	 * </p>
	 * <p>
	 * Summarizing, the web address or other directory address where we can find
	 * at least the following two files:
	 * <ul>
	 * <li>latest.txt</li>
	 * <li>releaseNotesX.Y.Z.htm</li>
	 * </ul>
	 * The file {@code latest.txt} contains exactly the dotted version number
	 * of the latest release of this software; nothing else! The release notes
	 * file contains HTML code describing the latest changes and the file name
	 * MUST end with the latest version number of the release.
	 * </p>
	 * 
	 * @return The {@link URL} to some directory where to look for the online update.
	 */
	public abstract URL getURLOnlineUpdate();

	/**
	 * This method opens a {@link File} and then memorizes the files returned by
	 * this method in the file history, which is then made accessible to the user.
	 * It also tries to store the open directory for the next access. This is only
	 * possible if all files returned by this method have the same parent
	 * directory. In case that an error occurs while making the new base directory
	 * persistent, a dialog will be displayed to the user that shows the precise
	 * error message.
	 * 
	 * @param files
	 *        any number of files that should be opened.
	 * @return the accepted files.
	 */
	public abstract File[] openFileAndLogHistory(File... files);

	/**
	 * Saves some results or the current work in some {@link File}.
	 * 
	 * @return the {@link File} into which the content has been saved. If the
	 *         returned value is not {@code null}, the directory in which the
	 *         {@link File} should be stored.
	 */
	public abstract File saveFileAs();

	/**
	 * Shows the about message, i.e., information about the authors of this
	 * program to the user.
	 */
	public abstract void showAboutMessage();

	/**
	 * Displays the license under which this program is distributed to the user.
	 */
	public abstract void showLicense();

	/**
	 * Displays the online help to the user.
	 */
	public abstract void showOnlineHelp();
	
}
