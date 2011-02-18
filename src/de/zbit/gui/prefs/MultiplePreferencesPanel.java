/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.prefs;

import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import de.zbit.gui.GUITools;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;

/**
 * A {@link JPanel} containing a {@link JTabbedPane} with several options for
 * the configuration of the {@link Properties} in a GUI.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.0 (originates from SBMLsqueezer 1.3)
 * @date 2009-09-22
 * @version $Rev$
 */
public class MultiplePreferencesPanel extends PreferencesPanel {

	/**
	 * Load all available {@link PreferencesPanel}s. This array is constructed
	 * by querying the current project and the calling project as well.
	 */
	private static Class<PreferencesPanel> classes[] = Reflect
			.getAllClassesInPackage(MultiplePreferencesPanel.class.getPackage()
					.getName(), true, true, PreferencesPanel.class, System
					.getProperty("user.dir")
					+ File.separatorChar, true);

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 3189416350182046246L;

	/**
	 * @return the classes
	 */
	public static Class<PreferencesPanel>[] getClasses() {
		return classes;
	};

	/**
	 * Counts how many tabs can be created inside of this
	 * {@link MultiplePreferencesPanel} due to the number of
	 * {@link PreferencesPanel} classes found in this package.
	 * 
	 * @return The number of tabs that will be automatically created and added to
	 *         this {@link MultiplePreferencesPanel} when initializing.
	 */
	public static int getPossibleTabCount() {
		int tabCount = 0;
		for (int i = 0; i < classes.length; i++) {
			if (!classes[i].equals(MultiplePreferencesPanel.class)) {
				try {
					Class<PreferencesPanel> c = classes[i];
					for (Constructor<?> constructor : c.getConstructors()) {
						if (constructor.getParameterTypes().length == 0) {
							tabCount++;
							break;
						}
					}
				} catch (Exception exc) {
				}
			}
		}
		return tabCount;
	}

	/**
	 * Needed to structure this {@link PreferencesPanel}.
	 */
	private JTabbedPane tab;

	/**
	 * @param properties
	 * @param defaultProperties
	 * @throws IOException
	 */
	public MultiplePreferencesPanel() throws IOException {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object key) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.zbit.gui.cfg.PreferencesPanel#addChangeListener(javax.swing.event.
	 * ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			getPreferencesPanel(i).addChangeListener(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.zbit.gui.cfg.PreferencesPanel#addItemListener(java.awt.event.ItemListener
	 * )
	 */
	@Override
	public void addItemListener(ItemListener listener) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			getPreferencesPanel(i).addItemListener(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			getPreferencesPanel(i).addKeyListener(listener);
		}
	}

	/**
	 * @param index
	 * @return
	 */
	public PreferencesPanel getPreferencesPanel(int index) {
		return (PreferencesPanel) ((JScrollPane) tab.getComponentAt(index))
				.getViewport().getComponent(0);
	}

	/**
	 * Gives the number of {@link PreferencesPanel}s displayed on this element.
	 * 
	 * @return 0 if there is no panel or if this element has not yet been
	 *         initialized properly.
	 */
	public int getPreferencesPanelCount() {
		return tab == null ? 0 : tab.getTabCount();
	}

	/**
	 * @return
	 */
	public int getSelectedIndex() {
		return tab.getSelectedIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS)
				.getString("USER_PREFERENCES");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#init()
	 */
	public void init() {
		tab = new JTabbedPane();
		PreferencesPanel settingsPanel;
		for (int i = 0; i < classes.length; i++) {
			if (!classes[i].equals(getClass())) {
				try {
					Class<PreferencesPanel> c = classes[i];
					Constructor<PreferencesPanel> con = c.getConstructor();
					settingsPanel = con.newInstance();
					tab.addTab(settingsPanel.getTitle(), new JScrollPane(
							settingsPanel,
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
				} catch (NoSuchMethodException exc) {
					// Do nothing.
				} catch (Exception exc) {
						GUITools.showErrorMessage(this, exc);
				}
			}
		}
		this.add(tab);
		addItemListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#isDefaultConfiguration()
	 */
	@Override
	public boolean isDefaultConfiguration() {
		for (int i = 0; i < getPreferencesPanelCount(); i++) {
			if (!getPreferencesPanel(i).isDefaultConfiguration()) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#isUserConfiguration()
	 */
	@Override
	public boolean isUserConfiguration() {
		for (int i = 0; i < getPreferencesPanelCount(); i++) {
			if (!getPreferencesPanel(i).isUserConfiguration()) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#loadPreferences()
	 */
	@Override
	protected SBPreferences loadPreferences() throws IOException {
		// This class only gathers other preferences panels.
		// It therefore does not have any own preferences.
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#persist()
	 */
	@Override
	public void persist() throws BackingStoreException {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			getPreferencesPanel(i).persist();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#restoreDefaults()
	 */
	@Override
	public void restoreDefaults() {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			getPreferencesPanel(i).restoreDefaults();
		}
		validate();
	}

	/**
	 * Sets the selected {@link PreferencesPanel} to the given index.
	 * 
	 * @param tab
	 *            The index of the {@link PreferencesPanel} to be selected.
	 */
	public void setSelectedIndex(int tab) {
		this.tab.setSelectedIndex(tab);
	}
}
