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

import java.awt.Panel;
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

import de.zbit.util.SBProperties;

/**
 * Abstract super class for any {@link SettingsPanel}s, i.e., a GUI element on
 * which the user may manipulate some preferences. All these are gathered in a
 * {@link Properties} object. There is one such {@link Properties} element for
 * the default settings and one that only contains those key-value pairs of
 * interest here. All key-value pairs that are not required by this class are
 * removed from the current {@link Properties} element before initialization.
 * The {@link #init()} method creates the layout of this {@link Panel}. You may
 * consider to override the method {@link #initConstantFields(Properties)} to
 * extract other key-value pairs than those that are directly needed for
 * manipulation. The {@link #getProperties()} method may also be overridden in
 * case that not all {@link Properties} can be updated while this object is
 * still visible. In some cases this method may have to gather information from
 * other GUI elements. Furthermore, this element already implements several
 * methods to notify listeners about changes. This is absolutely required, but
 * you may want to override these methods, such as {@link #keyPressed(KeyEvent)}
 * or {@link #itemStateChanged(ItemEvent)}. Please make sure not to forget to
 * call these methods from the super class also.
 * 
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
	 * A list of {@link ChangeListener}s to be notified in case that values
	 * change on this {@link SettingsPanel}.
	 */
	private List<ChangeListener> changeListeners;
	/**
	 * A list of {@link ItemListener}s to be notified when switching items on
	 * this {@link SettingsPanel}.
	 */
	private List<ItemListener> itemListeners;

	/**
	 * The settings to be changed by the user including default settings as a
	 * backup.
	 */
	protected SBProperties properties;

	/**
	 * Creates a new {@link SettingsPanel}.
	 * 
	 * @param properties
	 *            The current user properties. These will be filtered to contain
	 *            only accepted elements. Access to other elements is possible
	 *            by overriding {@link #initConstantFields(Properties)}.
	 * @see #accepts(Object)
	 */
	public SettingsPanel(SBProperties properties) {
		super();
		if (!properties.isSetDefaults()) {
			properties.setDefaults((Properties) properties.clone());
		}
		changeListeners = new LinkedList<ChangeListener>();
		itemListeners = new LinkedList<ItemListener>();
		setProperties(properties);
	}

	/**
	 * This method decides whether or not this {@link SettingsPanel} accepts the
	 * given key parameter as a valid key for which an option can be shown on
	 * this panel. This method is necessary to filter those settings that are
	 * not supported to avoid later write conflicts.
	 * 
	 * @param key
	 *            A key parameter for which it is to be decided whether it
	 *            constitutes a valid option here.
	 * @return True if the given key corresponds to a valid option, false
	 *         otherwise.
	 */
	public abstract boolean accepts(Object key);

	/**
	 * Adds the given {@link ChangeListener} to this element's list of this kind
	 * of listeners.
	 * 
	 * @param listener
	 *            the element to be added.
	 */
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Adds the given {@link ItemListener} to this element's list of this kind
	 * of listeners.
	 * 
	 * @param listener
	 *            the element to be added.
	 */
	public void addItemListener(ItemListener listener) {
		itemListeners.add(listener);
	}

	/**
	 * The default {@link Properties} are the standard values to be used if the
	 * user wants to re-initialize this object. With this method you can access
	 * these elements. Note that the default properties are never filtered and
	 * may therefore contain many additional elements in comparison to the
	 * current properties of this {@link SettingsPanel}
	 * 
	 * @return The {@link Properties} object containing the default key-value
	 *         pairs of user settings.
	 * @see #getProperties()
	 */
	public Properties getDefaultProperties() {
		return properties.getDefaults();
	}

	/**
	 * A derived class may override this method because it might be necessary to
	 * gather information from some GUI elements in the properties field
	 * variable. By default, this method returns a pointer to the properties
	 * field assuming that during the manipulation of all fields by the user the
	 * entries within this {@link Properties} object have been updated already.
	 * 
	 * @return A pointer to the currently set properties of this class.
	 */
	public SBProperties getProperties() {
		return properties;
	}

	/**
	 * Returns a meaningful human-readable title for this {@link SettingsPanel}.
	 * 
	 * @return A representative title for this element.
	 */
	public abstract String getTitle();

	/**
	 * Initializes the layout and GUI of this {@link SettingsPanel}. This method
	 * should use the values stored in the field variable {@link #properties}.
	 * Please note, although the {@link #accepts(Object)} method is used to
	 * filter possible values from the given {@link Properties}, there is no
	 * guarantee that a desired key-value pair is set. You can also access the
	 * {@link #defaultProperties} field here. Please make sure, all GUI elements
	 * used on this {@link SettingsPanel} provide some method to update the
	 * {@link #properties} field as this is the interesting value to be returned
	 * by the {@link #getProperties()} method.
	 */
	public abstract void init();

	/**
	 * This method allows an implementing class to extract key-value pairs from
	 * the given properties to set the values of non-modifiable elements. For
	 * instance, the default open directory for certain file types may not be
	 * set in this {@link SettingsPanel}, but the path to this directory may be
	 * required for something else. To avoid writing conflicts, all key-value
	 * pairs that may cause clashes are removed from the current properties
	 * using the {@link #accepts(Object)} method. Hence, the field
	 * {@link #properties} may not contain other important data. In this method
	 * it is possible to extract these. In the default case, this method will be
	 * an empty method. If you want to extract other fields, just override this
	 * method.
	 * 
	 * @param properties
	 *            The originally given {@link Properties} before filtering.
	 */
	public void initConstantFields(Properties properties) {
		// Empty implementation provided so that implementing classes
		// are not forced to implement this method.
	}

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
	 * Removes the given {@link ChangeListener} from the list of this kind of
	 * listeners in this object.
	 * 
	 * @param listener
	 *            the element to be removed.
	 * @return true if the list contained the specified element.
	 */
	public boolean removeChangeListener(ChangeListener listener) {
		return changeListeners.remove(listener);
	}

	/**
	 * Removes the given {@link ItemListener} from the list of this kind of
	 * listeners in this object.
	 * 
	 * @param listener
	 *            the element to be removed.
	 * @return true if the list contained the specified element.
	 */
	public boolean removeItemListener(ItemListener listener) {
		return itemListeners.remove(listener);
	}

	/**
	 * Switches all properties back to the given default values and
	 * re-initializes the graphical user interface.
	 */
	public void restoreDefaults() {
		setProperties((Properties) getDefaultProperties().clone());
	}

	/**
	 * Filters the given properties and only keeps those key-value pairs that
	 * are accepted by this class. Then it initializes the layout of this GUI
	 * element and also allows to select non-modifiable but important key-value
	 * pairs from the given {@link Properties} by overriding the method
	 * {@link #initConstantFields(Properties)}.
	 * 
	 * @param properties
	 *            All available current properties of the user.
	 * @see #accepts(Object)
	 */
	public void setProperties(Properties properties) {
		this.properties = new SBProperties(getDefaultProperties());
		for (Object key : properties.keySet()) {
			if (accepts(key)) {
				this.properties.put(key, properties.get(key));
			}
		}
		if (!this.properties.isSetDefaults()) {
			this.properties.setDefaults((Properties) this.properties.clone());
		}
		initConstantFields(properties);
		init();
	}

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
