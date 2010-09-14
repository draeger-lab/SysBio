/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.zbit.gui.cfg;

import java.awt.GridLayout;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import de.zbit.gui.GUITools;
import de.zbit.util.Reflect;

/**
 * A {@link JPanel} containing a {@link JTabbedPane} with several options for
 * the configuration of the {@link Properties} in a GUI.
 * 
 * @author Andreas Dr&auml;ger
 * @since This was part of SBMLsqueezer 1.3
 * @date 2009-09-22
 */
public class SettingsPanelAll extends SettingsPanel {

	/**
	 * Load all available {@link SettingsPanel}s. This array is constructed by
	 * querying the current project and the calling project as well.
	 */
	private static Class<SettingsPanel> classes[] = Reflect
			.getAllClassesInPackage(SettingsPanelAll.class.getPackage()
					.getName(), true, true, SettingsPanel.class, System
					.getProperty("user.dir")
					+ File.separatorChar, true);

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 3189416350182046246L;;

	/**
	 * Needed to structure this {@link SettingsPanel}.
	 */
	private JTabbedPane tab;

	/**
	 * 
	 * @param properties
	 * @param defaultProperties
	 */
	public SettingsPanelAll(Properties properties, Properties defaultProperties) {
		super(properties, defaultProperties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#addChangeListener(javax.swing.event
	 * .ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener) {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			getSettingsPanelAt(i).addChangeListener(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.squeezer.gui.SettingsPanel#addItemListener(java.awt.event.
	 * ItemListener)
	 */
	public void addItemListener(ItemListener listener) {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			getSettingsPanelAt(i).addItemListener(listener);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			getSettingsPanelAt(i).addKeyListener(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getProperties()
	 */
	public Properties getProperties() {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			this.settings.putAll(getSettingsPanelAt(i).getProperties());
		}
		return settings;
	}

	/**
	 * 
	 * @return
	 */
	public int getSelectedIndex() {
		return tab.getSelectedIndex();
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public SettingsPanel getSettingsPanelAt(int index) {
		return (SettingsPanel) ((JScrollPane) tab.getComponentAt(index))
				.getViewport().getComponent(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return "All settings";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#init()
	 */
	@Override
	public void init() {
		tab = new JTabbedPane();
		SettingsPanel settingsPanel;
		for (int i = 0; i < classes.length; i++) {
			if (!classes[i].equals(getClass())) {
				try {
					Class<SettingsPanel> c = classes[i];
					Constructor<SettingsPanel> con = c.getConstructor(settings.getClass(),
							defaultSettings.getClass());
					settingsPanel = con.newInstance(settings, defaultSettings);

					tab.addTab(settingsPanel.getTitle(), new JScrollPane(
							settingsPanel,
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
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
	 * @see org.sbml.squeezer.gui.SettingsPanel#restoreDefaults()
	 */
	@Override
	public void restoreDefaults() {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			getSettingsPanelAt(i).restoreDefaults();
		}
		validate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#setProperties(java.util.Properties)
	 */
	@Override
	public void setProperties(Properties properties) {
		int tabIndex = 0;
		if (tab != null) {
			tabIndex = getSelectedIndex();
			removeAll();
			setLayout(new GridLayout(1, 1));
		}
		this.settings = properties;
		init();
		if (tabIndex < tab.getComponentCount()) {
			setSelectedIndex(tabIndex);
		}
	}

	/**
	 * 
	 * @param tab
	 */
	public void setSelectedIndex(int tab) {
		this.tab.setSelectedIndex(tab);
	}
}
