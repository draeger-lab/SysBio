package de.zbit.gui.cfg;

import java.awt.Component;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import de.zbit.gui.JColumnChooser;
import de.zbit.gui.LayoutHelper;
import de.zbit.gui.cfg.FileSelector.Type;
import de.zbit.util.Directory;
import de.zbit.util.Option;
import de.zbit.util.Reflect;
import de.zbit.util.SBPreferences;
import de.zbit.util.SBProperties;
import de.zbit.util.StringUtil;
import de.zbit.util.Utils;

/**
 * Abstract super class for any {@link PreferencesPanel}s, i.e., a GUI element
 * on which the user may manipulate some preferences. All these are gathered in
 * a {@link Properties} object. There is one such {@link Properties} element for
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
 * @author Clemens Wrzodek
 * @date 2010-04-13
 */
public abstract class PreferencesPanel extends JPanel implements KeyListener,
		ItemListener, ChangeListener {
	
	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 1852850798328875230L;

	/**
	 * Changes an optionName by replacing all underscores with a space, setting
	 * the whole string to lowercase and changing the first letter to uppercase.
	 * E.g. "REMOVE_ORPHANS" => "Remove orphans"
	 * 
	 * @param optionName
	 * @return reformatted option string
	 */
	protected static final String formatOptionName(String optionName) {
		String ret = optionName;
		ret = ret.replace("_", " ");
		ret = ret.toLowerCase().trim();
		ret = Character.toUpperCase(ret.charAt(0)) + ret.substring(1);
		return ret;
	}

	/**
	 * A list of {@link ChangeListener}s to be notified in case that values
	 * change on this {@link PreferencesPanel}.
	 */
	private final List<ChangeListener> changeListeners;

	/**
	 * A list of {@link ItemListener}s to be notified when switching items on
	 * this {@link PreferencesPanel}.
	 */
	private final List<ItemListener> itemListeners;
	/**
	 * These are the persistently saved user-preferences of which some ore all
	 * elements are possibly to be changed in this panel. But only if the user
	 * wants. Hence, we have to first manipulate the field {@link #properties}
	 * and can maybe persist these changes.
	 */
	protected SBPreferences preferences;

	/**
	 * The settings to be changed by the user including default settings as a
	 * backup.
	 */
	protected SBProperties properties;

	/**
	 * Creates a new {@link PreferencesPanel}.
	 * 
	 * @param properties
	 *            The current user properties. These will be filtered to contain
	 *            only accepted elements. Access to other elements is possible
	 *            by overriding {@link #initConstantFields(Properties)}.
	 * @throws IOException
	 * @see #accepts(Object)
	 */
	public PreferencesPanel() throws IOException {
		super();
		changeListeners = new LinkedList<ChangeListener>();
		itemListeners = new LinkedList<ItemListener>();
		properties = new SBProperties(new SBProperties());
		preferences = loadPreferences();
		if (preferences != null) {
			String k;
			for (Object key : preferences.keySetFull()) {
				if (accepts(key)) {
					k = key.toString();
					properties.put(k, preferences.get(k));
					properties.getDefaults().setProperty(k,
							preferences.getDefault(k));
				}
			}
		}
		init();
	}

	/**
	 * This method decides whether or not this {@link PreferencesPanel} accepts
	 * the given key parameter as a valid key for which an option can be shown
	 * on this panel. This method is necessary to filter those settings that are
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
	 * Automatically builds an option panel, based on the static Option fields
	 * of the preferences.keyProvider.
	 * 
	 * @return all options that could not automatically be converted into a
	 *         JComponent.
	 */
	public List<Option<?>> autoBuildPanel() {
		List<Option<?>> unprocessedOptions = new LinkedList<Option<?>>();
		Class<?> keyProvider = preferences.getKeyProvider();
		LayoutHelper lh = new LayoutHelper(this);

		for (Field field : keyProvider.getDeclaredFields()) {
			try {
				Object fieldValue = field.get(keyProvider);
				if (fieldValue instanceof Option<?>) {
					Option<?> o = (Option<?>) fieldValue;

					// Create swing option based on field type
					JComponent jc = getJComponentForOption(o);
					if (jc != null) {
						lh.add(jc);
					} else {
						// Remember unprocessed options
						unprocessedOptions.add(o);
					}

				}
			} catch (Exception exc) {
				exc.printStackTrace();
				// ignore non-static fields
			}
		}

		return unprocessedOptions;
	}

	/**
	 * This method checks if all key-value pairs to be configured in this
	 * {@link PreferencesPanel} are valid and acceptable. For each inappropriate
	 * key-value pair this method creates one error message and then returns a
	 * list of all these messages with a statement of how to solve the problem.
	 * 
	 * @return An empty {@link List} if all key-value pairs in the
	 *         {@link #properties} of this {@link PreferencesPanel} are valid
	 *         and acceptable. Otherwise a list of error messages will be
	 *         returned.
	 */
	public abstract List<String> checkPreferences();

	/**
	 * The default {@link Properties} are the standard values to be used if the
	 * user wants to re-initialize this object. With this method you can access
	 * these elements. Note that the default properties are never filtered and
	 * may therefore contain many additional elements in comparison to the
	 * current properties of this {@link PreferencesPanel}
	 * 
	 * @return The {@link Properties} object containing the default key-value
	 *         pairs of user settings.
	 * @see #getProperties()
	 */
	public Properties getDefaultProperties() {
		return properties != null ? properties.getDefaults() : new Properties();
	}

	/**
	 * Automatically generates a JComponent for the given option. This includes:
	 * <ul>
	 * <li>Setting the text</li>
	 * <li>Setting the name</li>
	 * <li>Setting the tool tip text</li>
	 * <li>Setting the default value</li>
	 * <li>Adding this panel as item-listener</li>
	 * </ul>
	 * 
	 * @param o
	 *            - option to build the JComponent for.
	 * @return JComponent or NULL if the getRequiredType() is unknown.
	 */
	@SuppressWarnings("unchecked")
	public JComponent getJComponentForOption(Option<?> o) {
		// Create swing option based on field type
		JComponent jc = null;
		String optionTitle = formatOptionName(o.getOptionName());
		
		// If a range is specified, get all possible values.
		String[] values = null;
		
		if (o.getRange() != null) {
			List<?> val = o.getRange().getAllAcceptableValues();
			if (val != null) {
				values = new String[val.size()];
				for (int i = 0; i < val.size(); i++) {
					values[i] = val.get(i).toString();
				}
			}
		}
		
		// TODO: Guppieren, Testen, Accept automatieren
		Class<?> clazz = o.getRequiredType();
		if (Boolean.class.isAssignableFrom(clazz)) {
			jc = new JCheckBox();
			((AbstractButton) jc).setSelected(((Option<Boolean>)o).getValue(preferences));
			
			//((AbstractButton) jc).setSelected(Boolean.parseBoolean(properties
			//		.get(o.getOptionName()).toString()));
		} else if (File.class.isAssignableFrom(clazz)) {
			// Infere type
			Type ty;
			String check = optionTitle.toLowerCase();
			if (check.contains("open")  || check.contains("load")
					|| check.contains("input") ) {
				ty = Type.OPEN;
			} else {
				ty = Type.SAVE;
			}
			
			// Get default value
			String defPath=null;
			Object def = o.getValue(preferences);
			if (def==null) defPath=null;
			else if (def instanceof File) defPath = ((File)def).getPath();
			else defPath = def.toString();
			
			jc = new FileSelector(ty, defPath, (o.getRequiredType() != Directory.class),(FileFilter[]) null);
			((FileSelector)jc).setLabelText(optionTitle);
			
		} else if (Character.class.isAssignableFrom(clazz)) {
			jc = new JColumnChooser(optionTitle, true, values);
			((JColumnChooser)jc).setAcceptOnlyIntegers(false);
			((JColumnChooser)jc).setDefaultValue(o.getValue(preferences).toString());
			//JComponent cs = ((JColumnChooser)jc).getColumnChooser();
			// TODO: Limit maximum size to one (don't accept inputs after that).
			
		} else if (String.class.isAssignableFrom(clazz)) {
			jc = new JColumnChooser(optionTitle, true, values);
			((JColumnChooser)jc).setAcceptOnlyIntegers(false);
			((JColumnChooser)jc).setDefaultValue(o.getValue(preferences).toString());

		} else if (Number.class.isAssignableFrom(clazz)) {
			jc = new JColumnChooser(optionTitle, true, values);
			if (!Utils.isInteger(o.getRequiredType())) {
   			// TODO: implement Box for doubles.
				// For doulbes, we need to allow ',' and '.'.
				((JColumnChooser)jc).setAcceptOnlyIntegers(false);
			}
			((JColumnChooser)jc).setDefaultValue(o.getValue(preferences).toString());
		}

		// Check if the option could be converted to a JComponent
		if (jc != null) {
			if (jc instanceof AbstractButton) {
				((AbstractButton) jc).setText(optionTitle);
			//} else if (jc instanceof JColumnChooser) {
				//((JColumnChooser) jc).setTitle(optionTitle);
				//((JColumnChooser) jc).setDefaultValue(o.getValue(preferences));
			}
			
			Reflect.invokeIfContains(jc, "addItemListener", ItemListener.class, this);
		  Reflect.invokeIfContains(jc, "addChangeListener", ChangeListener.class, this);
			jc.setName(o.getOptionName());
			jc.setToolTipText(StringUtil.toHTML(o.getDescription(), 60));
			jc.addKeyListener(this);
			
			//jc.setBorder(new TitledBorder("test"));
		}

		return jc;
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
	 * Returns a meaningful human-readable title for this
	 * {@link PreferencesPanel}.
	 * 
	 * @return A representative title for this element.
	 */
	public abstract String getTitle();

	/**
	 * Initializes the layout and GUI of this {@link PreferencesPanel}. This
	 * method should use the values stored in the field variable
	 * {@link #properties}. Please note, although the {@link #accepts(Object)}
	 * method is used to filter possible values from the given
	 * {@link Properties}, there is no guarantee that a desired key-value pair
	 * is set. You can also access the {@link #defaultProperties} field here.
	 * Please make sure, all GUI elements used on this {@link PreferencesPanel}
	 * provide some method to update the {@link #properties} field as this is
	 * the interesting value to be returned by the {@link #getProperties()}
	 * method.
	 */
	public abstract void init();

	/**
	 * Method to test whether the current properties equal the default
	 * configuration.
	 * 
	 * @return true if the values of all current properties equal the values of
	 *         the defaults.
	 */
	public boolean isDefaultConfiguration() {
		for (Entry<Object, Object> e : properties.entrySet()) {
			if (!e.getValue().equals(properties.getDefaults().get(e.getKey()))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method to test whether the current properties equal the user's current
	 * configuration.
	 * 
	 * @return true if the values of all current properties equal the current
	 *         persistent user configuration.
	 */
	public boolean isUserConfiguration() {
		for (Entry<Object, Object> e : properties.entrySet()) {
			if (!e.getValue().toString().equals(
					preferences.get(e.getKey().toString()))) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		setProperty(e.getSource());
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
		setProperty(e.getSource());
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
		setProperty(e.getSource());
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
		setProperty(e.getSource());
		for (KeyListener kl : getKeyListeners()) {
			kl.keyTyped(e);
		}
	}

	/**
	 * This method loads an {@link SBPreferences} object whose key-value pairs
	 * are to be manipulated by the user. In this way, this method tells this
	 * class where these preferences are located.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected abstract SBPreferences loadPreferences() throws IOException;

	/**
	 * Persistently stores the currently set options, i.e., key-value pairs in
	 * the user's configuration. Depending on the operating system, the way how
	 * this information is actually stored can vary.
	 * 
	 * @throws BackingStoreException
	 */
	public void persist() throws BackingStoreException {
		preferences.putAll(properties);
		preferences.flush();
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
		setProperties(preferences != null ? preferences.getDefaults()
				: new SBProperties());
	}

	/**
	 * Filters the given properties and only keeps those key-value pairs that
	 * are accepted by this class. Then it initializes the layout of this GUI
	 * element and also allows to select non-modifiable but important key-value
	 * pairs from the given {@link Properties} by overriding the method
	 * {@link #initConstantFields(Properties)}.
	 * 
	 * @param map
	 *            All available current properties of the user.
	 * @see #accepts(Object)
	 */
	public void setProperties(Map<Object, Object> map) {
		for (Object key : map.keySet()) {
			if (accepts(key)) {
				this.properties.setProperty(key.toString(), map.get(key)
						.toString());
			}
		}
		removeAll();
		init();
	}

	/**
	 * Attempts to set the new value of the changed element in this panel's
	 * properties. To this end, it is required that the name property is set for
	 * each graphical component that may change.
	 * 
	 * @param source
	 *            The element whose value has been changed.
	 */
	private void setProperty(Object source) {
		if (source instanceof Component) {
			Component c = (Component) source;
			String name = c.getName();
			System.out.println(name);
			if ((name != null) && (properties.containsKey(name))) {
				String value = null;
				if (c instanceof AbstractButton) {
					value = Boolean.toString(((AbstractButton) c).isSelected());
				} else if (c instanceof JColorChooser) {
					value = ((JColorChooser) c).getColor().toString();
				} else if (c instanceof JComboBox) {
					value = ((JComboBox) c).getSelectedItem().toString();
				} else if (c instanceof JFileChooser) {
					value = ((JFileChooser) c).getSelectedFile()
							.getAbsolutePath();
				} else if (c instanceof JList) {
					value = ((JList) c).getSelectedValue().toString();
				} else if (c instanceof JSlider) {
					value = Integer.toString(((JSlider) c).getValue());
				} else if (c instanceof JSpinner) {
					value = ((JSpinner) c).getValue().toString();
				} else if (c instanceof FileSelector) {
					try {
						value = ((FileSelector)c).getSelectedFile().getPath();
						System.out.println(value);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (c instanceof JTable) {
					JTable tab = (JTable) c;
					value = tab.getModel().getValueAt(tab.getSelectedRow(),
							tab.getSelectedColumn()).toString();
				} else if (c instanceof JTextComponent) {
					value = ((JTextComponent) c).getText();
				}
				properties.setProperty(name, value);
			}
		}
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
