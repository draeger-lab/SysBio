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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.zbit.gui.cfg;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-13
 * 
 */
public abstract class SettingsPanel extends JPanel implements KeyListener,
		ItemListener, ChangeListener {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 1852850798328875230L;
	/**
	 * 
	 */
	private List<ChangeListener> changeListeners;
	/**
	 * 
	 */
	private List<ItemListener> itemListeners;

	/**
	 * The settings to be changed by the user and the default settings as a
	 * backup.
	 */
	protected Properties settings, defaultSettings;

	/**
	 * 
	 * @param properties
	 * @param defaultProperties
	 */
	public SettingsPanel(Properties properties, Properties defaultProperties) {
		super();
		changeListeners = new LinkedList<ChangeListener>();
		itemListeners = new LinkedList<ItemListener>();
		this.defaultSettings = defaultProperties;
		setProperties(properties);
	}

	/**
	 * 
	 * @param listener
	 */
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * 
	 * @param listener
	 */
	public void addItemListener(ItemListener listener) {
		itemListeners.add(listener);
	}

	/**
	 * @return
	 */
	public Properties getDefaultProperties() {
		return defaultSettings;
	}

	/**
	 * 
	 * @return
	 */
	public abstract Properties getProperties();

	/**
	 * Returns a meaningful human-readable title for this {@link SettingsPanel}.
	 * 
	 * @return A representative title for this element.
	 */
	public abstract String getTitle();

	/**
	 * Initializes the layout and GUI of this {@link SettingsPanel}.
	 */
	public abstract void init();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		for (ItemListener i : itemListeners) {
			i.itemStateChanged(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		for (KeyListener i : getKeyListeners()) {
			i.keyPressed(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		for (KeyListener kl : getKeyListeners()) {
			kl.keyReleased(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		for (KeyListener kl : getKeyListeners()) {
			kl.keyTyped(e);
		}
	}

	/**
	 * 
	 * @param listener
	 * @return
	 */
	public boolean removeChangeListener(ChangeListener listener) {
		return changeListeners.remove(listener);
	}

	/**
	 * 
	 * @param listener
	 * @return
	 */
	public boolean removeItemListener(ItemListener listener) {
		return itemListeners.remove(listener);
	}

	/**
	 * Switches all properties back to the given default values and
	 * re-initializes the graphical user interface.
	 */
	public void restoreDefaults() {
		setProperties((Properties) defaultSettings.clone());
	}

	/**
	 * 
	 * @param settings
	 */
	public abstract void setProperties(Properties settings);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		for (ChangeListener cl : changeListeners) {
			cl.stateChanged(e);
		}
	}

}
