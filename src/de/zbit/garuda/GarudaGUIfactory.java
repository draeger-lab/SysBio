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
package de.zbit.garuda;

import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import de.zbit.gui.GUITools;
import de.zbit.util.ResourceManager;

/**
 * <p>
 * Provides convenient factory methods to create user interfaces for the interaction with a Garuda Core.
 * <p>
 * When first launched, this class loads the Garuda icons into the {@link UIManager}. You can access the icons
 * using the keys in your {@link UIManager}.
 * <ul>
 * <li>{@code ICON_GARUDA_16}</li>
 * <li>{@code ICON_GARUDA_32}</li>
 * <li>{@code ICON_GARUDA_48}</li>
 * </ul>
 * 
 * @author Andreas Dr&auml;ger
 * @date 12:43:29
 * @since 1.1
 * @version $Rev$
 */
public class GarudaGUIfactory {
	
	/**
	 * Localization support.
	 */
	public static final transient ResourceBundle GARUDA_BUNDLE = ResourceManager.getBundle("de.zbit.garuda.locales.Labels");
	
	static {
		loadGarudaIcons();
	}
	
	/**
	 * 
	 * @param listener
	 * @return
	 */
	public static JMenu createGarudaMenu(ActionListener listener) {
		JMenuItem sentFile = GUITools.createJMenuItem(listener, GarudaActions.SENT_TO_GARUDA, false);
		JMenu garudaMenu = GUITools.createJMenu(GARUDA_BUNDLE.getString("GARUDA"), sentFile);
		garudaMenu.setToolTipText(GARUDA_BUNDLE.getString("GARUDA_TOOLTIP"));
		return garudaMenu;
	}

	/**
	 * Loads the Garuda icons into the {@link UIManager}. You can access the icons
	 * using the keys
	 * <ul>
	 * <li>{@code ICON_GARUDA_16}</li>
	 * <li>{@code ICON_GARUDA_32}</li>
	 * <li>{@code ICON_GARUDA_48}</li>
	 * </ul>
	 */
	private static void loadGarudaIcons() {
		String iconPaths[] = {
				"ICON_GARUDA_16.png",
				"ICON_GARUDA_32.png",
				"ICON_GARUDA_48.png"
		};
		for (String path : iconPaths) {
			URL u = GarudaSoftwareBackend.class.getResource("img/" + path);
			if (u != null) {
				UIManager.put(path.substring(0, path.lastIndexOf('.')), new ImageIcon(u));
			}
		}
	}
	
}
